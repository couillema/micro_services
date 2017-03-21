package com.chronopost.vision.microservices.insertC11;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildUpdate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;

public enum InsertC11DaoImpl implements IInsertC11Dao {
	INSTANCE;

	private final Logger LOGGER = LoggerFactory.getLogger(InsertC11DaoImpl.class);

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private final static SimpleDateFormat COLUMN_JOUR_FORMAT = new SimpleDateFormat("yyyyddMM");

	private CacheManager<Parametre> cacheParametre;

	/** requete d'insertion d'une d'une tournee */
	private PreparedStatement psUpdateTournee;
	/** requete d'ajout d'une tournee dans l'index des tournees */
	private PreparedStatement psIdxTourneeJour;
	/** requete d'insertion d'un point */
	private PreparedStatement psUpdatePointsTournee;
	/** requete d'ajout d'un point dans une tournee */
	private PreparedStatement psAddPointsDansTournee;

	/**
	 * Met à jour la table tournee
	 */
	@Override
	public boolean miseAJourTournee(final TourneeC11 tourneeC11) throws DriverException {
		final String idC11 = checkIdTournee(tourneeC11);

		final ResultSet result = getSession()
				.execute(psUpdateTournee.bind(tourneeC11.geTourneeVision().getPriseEnCharge(),
						tourneeC11.geTourneeVision().getDebutPrevu(), tourneeC11.geTourneeVision().getDureePrevue(),
						tourneeC11.geTourneeVision().getDistancePrevue(), tourneeC11.geTourneeVision().getDureePause(),
						tourneeC11.geTourneeVision().getIdPoiSousTraitant(), idC11));

		return result.wasApplied();
	}

	/**
	 * Met à jour la table idx_tournee_agence_jour
	 */
	@Override
	public boolean miseAJourIdxTourneeJour(final TourneeC11 tourneeC11) throws DriverException {
		try {
			final String idC11 = checkIdTournee(tourneeC11);

			final ResultSet result = getSession()
					.execute(psIdxTourneeJour.bind(idC11, tourneeC11.geTourneeVision().getIdSite(),
							formatDateToJour(formatStringToDate(tourneeC11.geTourneeVision().getDateTournee())),
							tourneeC11.geTourneeVision().getCodeTourne(),
							formatStringToDate(tourneeC11.geTourneeVision().getDateTournee())));
			return result.wasApplied();
		} catch (final ParseException e) {
			LOGGER.error("Error when parse field Date from C11 JSON", e);
			return false;
		}
	}

	/**
	 * Met à jour la table tournee_point
	 */
	@Override
	public boolean miseAJourPoints(final TourneeC11 tourneeC11) throws DriverException {
		final List<ResultSetFuture> futures = new ArrayList<>();
		final List<PointC11> pointC11s = tourneeC11.geTourneeVision().getPointC11Liste().getPointC11s();
		final String idTournee = checkIdTournee(tourneeC11);
		final Set<String> listePointUnique = new HashSet<String>();

		for (final PointC11 pointC11 : pointC11s) {
			/* Insertion du point */
			futures.add(getSession().executeAsync(psUpdatePointsTournee.bind(pointC11.getNumeroPoint(),
					pointC11.getContrainteHoraire(), pointC11.getHeureDebutRDV(), pointC11.getHeureFinRDV(),
					pointC11.getDestType(), pointC11.getDestNom1(), pointC11.getDestRaisonSociale1(),
					pointC11.getLibelleProduitPoint(), pointC11.getTempsStopService(), pointC11.getIdAdresse(),
					pointC11.getIdDest(), pointC11.getIsSPOI(), pointC11.getNbOjets(), pointC11.getGammeProduit(),
					pointC11.getIdPtC11())));
			/* Ajout de l'idPoint dans la liste des points de la tournee */
			listePointUnique.clear();
			listePointUnique.add(pointC11.getIdPtC11());
			futures.add(getSession().executeAsync(psAddPointsDansTournee.bind(listePointUnique, idTournee)));

		}

		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
		return true;
	}

	private InsertC11DaoImpl() throws DriverException {
		final List<ETableTournee> fieldsTournee = new ArrayList<>(
				Arrays.asList(ETableTournee.TYPE_PRISE_EN_CHARGE, ETableTournee.DEBUT_PREVU, ETableTournee.DUREE_PREVUE,
						ETableTournee.DISTANCE_PREVUE, ETableTournee.DUREE_PAUSE, ETableTournee.ID_POI_STT));
		psUpdateTournee = getSession()
				.prepare(buildUpdate(ETableTournee.TABLE_NAME, fieldsTournee, TTL.TOURNEE.getTimelapse()));

		psIdxTourneeJour = getSession().prepare(ETableIdxTourneeJour.buildUpdateIdTournee());

		final List<ETableTourneePoint> fieldsTourneePoints = new ArrayList<>(Arrays.asList(ETableTourneePoint.NUMERO,
				ETableTourneePoint.HEURE_CONTRACTUELLE, ETableTourneePoint.DEBUT_RDV, ETableTourneePoint.FIN_RDV,
				ETableTourneePoint.TYPE_DESTINATION, ETableTourneePoint.NOM_DESTINATION,
				ETableTourneePoint.RAISON_SOCIALE_DESTINATION, ETableTourneePoint.PRODUIT_PRINCIPAL,
				ETableTourneePoint.DUREE_STOP, ETableTourneePoint.ID_ADRESSE_DESTINATION,
				ETableTourneePoint.ID_DESTINATION, ETableTourneePoint.IS_SPOI, ETableTourneePoint.NB_COLIS_PREVUS,
				ETableTourneePoint.GAMME_PRODUIT));
		psUpdatePointsTournee = getSession().prepare(
				buildUpdate(ETableTourneePoint.TABLE_NAME, fieldsTourneePoints, TTL.POINTTOURNEE.getTimelapse()));

		psAddPointsDansTournee = getSession().prepare(
				buildUpdate(ETableTournee.TABLE_NAME, Arrays.asList(ETableTournee.POINTS), TTL.TOURNEE.getTimelapse()));
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	// format Date to dd/MM/yyyy
	private String formatDateToJour(final Date origineDate) {
		return COLUMN_JOUR_FORMAT.format(origineDate);
	}

   
    // parse String like yyyyddMM to Date
    private Date formatStringToDate(final String origineDate) throws ParseException {
    	return DATE_FORMAT.parse(origineDate);
    }
    
    /**
     * Methode permettant de récupérer l'idC11 au format idC11 ou idC11+ selon les parametres et FF en cours. 
     * @param tourneeC11
     * @return l'idC11 ou l'idC11+ en fonction du FeatureFlip, de la date et de la valeur du parametre idC11DateActivation
     */
	private String checkIdTournee(final TourneeC11 tourneeC11) {
		final String idTourneeEntree = tourneeC11.geTourneeVision().getIdC11();
		String idTourneeSortie = idTourneeEntree;

		/* Si l'idC11 est sur 19 et que le FF est activé */
		if (idTourneeEntree != null && idTourneeEntree.length() == 19 && FeatureFlips.INSTANCE.getBoolean("idC11Plus", false)) {
			/* Calcul de la date de tournée (format DD/MM/YYYY ==> YYYYMMDD */
			String date = tourneeC11.geTourneeVision().getDateTournee();
			final String[] dateTab = date.split("/");
			date = dateTab[2] + dateTab[1] + dateTab[0];

			/* Alors si la date d'activation idC11+ est révolu, on force l'idC11+ */
			if (cacheParametre.getValue("DatePassageIdC11Plus").getValue().compareTo(date) <= 0)
				idTourneeSortie = tourneeC11.geTourneeVision().getTrigramme() + idTourneeEntree;
		}

		return idTourneeSortie;
	}

    
    @Override
	public IInsertC11Dao setRefentielParametre(final CacheManager<Parametre> cacheParametre) {
		this.cacheParametre = cacheParametre;
		return this;
	}
}
