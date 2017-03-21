package com.chronopost.vision.microservices.getC11;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.chronopost.vision.model.insertC11.TourneeVision;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.google.common.util.concurrent.ListenableFuture;

public enum GetC11DaoImpl implements IGetC11Dao {
	INSTANCE;

	// requete de selection des id tournee par agence et jour
	private final PreparedStatement psGetIdTournees;
	// requete de selection d'une tournee par son id
	private final ListenableFuture<PreparedStatement> psGetTournee;
	// requete de selection d'un point tournee par son id
	private final ListenableFuture<PreparedStatement> psGetPointsTournee;

	private GetC11DaoImpl() throws DriverException {
		psGetIdTournees = getSession()
				.prepare(buildSelect(ETableIdxTourneeJour.TABLE_NAME, Arrays.asList(ETableIdxTourneeJour.ID_TOURNEE),
						ETableIdxTourneeJour.AGENCE, ETableIdxTourneeJour.JOUR).getQuery());

		psGetTournee = getSession().prepareAsync(buildSelect(ETableTournee.TABLE_NAME,
				Arrays.asList(ETableTournee.TYPE_PRISE_EN_CHARGE, ETableTournee.DUREE_PREVUE,
						ETableTournee.DISTANCE_PREVUE, ETableTournee.DUREE_PAUSE, ETableTournee.ID_POI_STT,
						ETableTournee.POINTS, ETableTournee.ID_TOURNEE, ETableTournee.DEBUT_PREVU),
				ETableTournee.ID_TOURNEE).getQuery());

		psGetPointsTournee = getSession().prepareAsync(buildSelect(ETableTourneePoint.TABLE_NAME,
				Arrays.asList(ETableTourneePoint.NUMERO, ETableTourneePoint.ID_POINT,
						ETableTourneePoint.HEURE_CONTRACTUELLE, ETableTourneePoint.DEBUT_RDV,
						ETableTourneePoint.FIN_RDV, ETableTourneePoint.TYPE_DESTINATION,
						ETableTourneePoint.NOM_DESTINATION, ETableTourneePoint.RAISON_SOCIALE_DESTINATION,
						ETableTourneePoint.PRODUIT_PRINCIPAL, ETableTourneePoint.DUREE_STOP,
						ETableTourneePoint.ID_ADRESSE_DESTINATION, ETableTourneePoint.ID_DESTINATION,
						ETableTourneePoint.IS_SPOI, ETableTourneePoint.NB_COLIS_PREVUS,
						ETableTourneePoint.GAMME_PRODUIT),
				ETableTourneePoint.ID_POINT).getQuery());
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	/**
	 * Retourne les id_tournee trouvés pour une agence et un jour dans
	 * idx_tournee_agence_jour
	 */
	@Override
	public List<String> getIdxTourneesByAgenceAndJour(final String posteComptable, final String jour) throws Exception {
		final List<String> idTournees = new ArrayList<>();
		final ResultSet execute = getSession().execute(psGetIdTournees.bind(posteComptable, jour));
		final List<Row> requestRows = execute.all();
		for (final Row row : requestRows) {
			idTournees.add(row.getString(0));
		}
		return idTournees;
	}

	@Override
	public List<TourneeC11> getTourneesById(final List<String> tourneesId)
			throws InterruptedException, ExecutionException {
		final List<TourneeC11> tournees = new ArrayList<>();
		final Set<ResultSetFuture> futures = new HashSet<>();
		for (final String idTournee : tourneesId) {
			futures.add(getSession().executeAsync(psGetTournee.get().bind(idTournee)));
		}
		for (final ResultSetFuture rsFuture : futures) {
			final Row row = rsFuture.getUninterruptibly().one();
			if (row != null) {
				tournees.add(mapRowToTourneeVision(row));
			}
		}
		return tournees;
	}

	@Override
	public List<PointC11> getPointsForTourneeId(final List<String> idsC11)
			throws InterruptedException, ExecutionException {
		final List<PointC11> tournees = new ArrayList<>();
		final Set<ResultSetFuture> futures = new HashSet<>();
		for (final String idPoint : idsC11) {
			futures.add(getSession().executeAsync(psGetPointsTournee.get().bind(idPoint)));
		}
		for (final ResultSetFuture rsFuture : futures) {
			final Row row = rsFuture.getUninterruptibly().one();
			if (row != null) {
				tournees.add(mapRowToPointC11(row));
			}
		}
		return tournees;
	}

	private TourneeC11 mapRowToTourneeVision(final Row row) {
		final TourneeC11 tournee = new TourneeC11();
		final TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setPriseEnCharge(row.getString(0));
		tourneeVision.setDureePrevue(row.getString(1));
		tourneeVision.setDistancePrevue(row.getString(2));
		tourneeVision.setDureePause(row.getString(3));
		tourneeVision.setIdPoiSousTraitant(row.getString(4));
		tourneeVision.setIdsPoint(row.getSet(5, String.class));
		tourneeVision.setIdC11(row.getString(6));
		tourneeVision.setDebutPrevu(row.getString(7));
		tournee.setTourneeVision(tourneeVision);
		return tournee;
	}

	private PointC11 mapRowToPointC11(final Row row) {
		final PointC11 pointC11 = new PointC11();
		pointC11.setNumeroPoint(row.getString(0));
		pointC11.setIdPtC11(row.getString(1));
		pointC11.setContrainteHoraire(row.getString(2));
		pointC11.setHeureDebutRDV(row.getString(3));
		pointC11.setHeureFinRDV(row.getString(4));
		pointC11.setDestType(row.getString(5));
		pointC11.setDestNom1(row.getString(6));
		pointC11.setDestRaisonSociale1(row.getString(7));
		pointC11.setLibelleProduitPoint(row.getString(8));
		pointC11.setTempsStopService(row.getString(9));
		pointC11.setIdAdresse(row.getString(10));
		pointC11.setIdDest(row.getString(11));
		pointC11.setIsSPOI(row.getString(12));
		pointC11.setNbOjets(row.getString(13));
		pointC11.setGammeProduit(row.getString(14));
		return pointC11;
	}
}
