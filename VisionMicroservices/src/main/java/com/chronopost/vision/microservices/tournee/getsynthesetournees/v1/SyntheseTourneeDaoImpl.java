package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;

/**
 * Implémentation du dao SyntheseTourneeDao
 * 
 * @author jcbontemps
 */
public class SyntheseTourneeDaoImpl implements SyntheseTourneeDao {

	private final Logger log = LoggerFactory.getLogger(SyntheseTourneeDaoImpl.class);
	
	private final PreparedStatement psTournee;
	private final PreparedStatement psPointTournee;
	private final PreparedStatement psSpecifColis;
    /** Mise à jour du compteur de microservice **/
    private final PreparedStatement psUpdateCptTrtTrtFailMS;
    private final PreparedStatement psUpdateCptFailMS;
    private final PreparedStatement psUpdateCptHitMS;
    
	private SyntheseTourneeDaoImpl() {
		psPointTournee = getSession().prepare(buildSelect(ETableTourneePoint.TABLE_NAME,
				asList(ETableTourneePoint.ID_POINT, ETableTourneePoint.EVENEMENTS)).getQuery());

		psTournee = getSession().prepare(
				buildSelect(ETableTournee.TABLE_NAME, asList(ETableTournee.POINTS, ETableTournee.COLIS)).getQuery());

		psSpecifColis = getSession().prepare(buildSelect(ETableColisSpecifications.TABLE_NAME,
				asList(ETableColisSpecifications.NO_LT, ETableColisSpecifications.SPECIFS_EVT,
						ETableColisSpecifications.SPECIFS_SERVICE, ETableColisSpecifications.ETAPES,
						ETableColisSpecifications.CONSIGNES_TRAITEES, ETableColisSpecifications.INFO_SUPP,
						ETableColisSpecifications.SERVICE)).getQuery());

		psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
	}

	/**
	 * Singleton
	 */
	private static class InstanceHolder {
		public static SyntheseTourneeDao dao;
		static {
			if (dao == null) {
				dao = new SyntheseTourneeDaoImpl();
			}
		}
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static SyntheseTourneeDao getInstance() {
		return InstanceHolder.dao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.
	 * SyntheseTourneeDao#getPointsTournee(java.lang.String)
	 */
	@Override
	public Tournee getPointsTournee(final String idTournee) throws InterruptedException, ExecutionException {
		final Tournee tournee = new Tournee();

		final List<ResultSetFuture> futuresPoints = new ArrayList<ResultSetFuture>();
		final List<ResultSetFuture> futuresSpecifs = new ArrayList<ResultSetFuture>();

		/* Récupération de la tournée */
		String idC11 = idTournee;
		String dateTournee = null;
		String codeTournee = null;
		if (!FeatureFlips.INSTANCE.getBoolean("idC11Plus", false)) {
    		// RG-MSGetSyntTournee-0300 Lors du chargement de la liste des colis à
			// partir de l’idC11, tronquer l’identifiant sur les 19 derniers
			// caractères.
			if (idC11.length() > 19) {
				idC11 = idC11.substring(idC11.length() - 19, idC11.length());
			}
		} else {
			// RG-MSGetSyntTournee-0302
			// Si len(idC11) = 22 ⇒ il n’y a pas de critères
			// Si len(idC11) = 27 ⇒ les 5 premiers caractères indiquent le code tournée de selection
			// Si len(idC11) = 30 ⇒ les 8 premiers caractères indiquent la date de la tournée 
			// Si len(idC11) = 35 ⇒ les 8 premiers caractères indiquent la date de la tournée  et les 5 suivants indiquent le code tournée.
			if (idC11.length() == 27) {
				codeTournee = idC11.substring(0, 5);
				idC11 = idC11.substring(5);
			} else if (idC11.length() == 30) {
				dateTournee = idC11.substring(0, 8);
				idC11 = idC11.substring(8);
			} else if (idC11.length() == 35) {
				dateTournee = idC11.substring(0, 8);
				codeTournee = idC11.substring(8, 13);
				idC11 = idC11.substring(13);
			}
		}
		ResultSet execute = getSession().execute(psTournee.bind(idC11));
		/* Récupération des points de la tournée */
		/* 1 - select asynchrone sur l'ensemble des points et des specifcolis */

		// RG-MSGetSyntTournee-0301 Lors du chargement de la liste des colis à
		// partir de l’idC11+ (22 caractères) s’il n’existe pas de tournée avec
		// cet identifiant, réitérer la recherche en appliquant la
		// RG-MSGetSyntTournee-0300
		List<Row> tourneeListe = execute.all();
		if (idC11.length() == 22 && tourneeListe.size() == 0) {
			idC11 = idC11.substring(idC11.length() - 19, idC11.length());
			execute = getSession().execute(psTournee.bind(idC11));
			tourneeListe = execute.all();
		}
		for (final Row row : tourneeListe) {
			final Set<String> idPoints = row.getSet(ETableTournee.POINTS.getNomColonne(), String.class);
			final Set<String> idColis = row.getSet(ETableTournee.COLIS.getNomColonne(), String.class);
			for (String idOneColis : idColis)
				futuresSpecifs.add(getSession().executeAsync(psSpecifColis.bind(idOneColis)));
			for (String idPoint : idPoints)
				futuresPoints.add(getSession().executeAsync(psPointTournee.bind(idPoint)));
		}

		/* Récupération des specifsColis */
		final Future<List<ResultSet>> resultsSpecifs = Futures.successfulAsList(futuresSpecifs);
		for (final ResultSet rs : resultsSpecifs.get()) {
			if (rs != null) {
				final List<Row> all = rs.all();
				for (final Row row : all) {
					if (row != null) {
						final SpecifsColis specifColis = Mapper.makeSpecifColis(row);
						tournee.putToColisSpecifs(specifColis.getNoLt(), specifColis);
					}
				}
			}
		}

		/* Récupération des points */
		final Future<List<ResultSet>> results = Futures.successfulAsList(futuresPoints);
		final Set<PointTournee> points = new TreeSet<>();
		for (final ResultSet rs : results.get()) {
			final List<Row> all = rs.all();
			for (final Row row : all) {
				final PointTournee point = new PointTournee();
            	final Multimap<String, ColisPoint> colisEvts = Mapper.makeMapDeColisPoint(row, point, tournee.getColisSpecifs(), codeTournee, dateTournee);
            	// RG-MSGetSyntTournee-0303 Un point colis sans evt ne doit pas apparaître dans la tournée
				if (colisEvts.size() > 0) {
					points.add(point);
					tournee.putAllToColisEvenements(colisEvts);
				}
			}
		}

		tournee.setIdentifiantTournee(idC11);
		tournee.setPoints(new ArrayList<PointTournee>(points));
		
		/* Ajout du nombre de points récupérés dans la trace microservice_counters */
		updateCptTrtTrtFailMS(points.size(), 0);
		
		return tournee;
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}
	
	   /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
	public void updateCptTrtTrtFailMS(final int nbTrt, final int nbTrtFail) {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptTrtTrtFailMS.bind((long) nbTrt, (long) nbTrtFail, "getSyntheseTournee",
					jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}
    
    /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
	public void updateCptHitMS() {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptHitMS.bind(new Long(1), "getSyntheseTournee", jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}

	public void updateCptFailMS() {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptFailMS.bind(new Long(1), "getSyntheseTournee", jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}
}
