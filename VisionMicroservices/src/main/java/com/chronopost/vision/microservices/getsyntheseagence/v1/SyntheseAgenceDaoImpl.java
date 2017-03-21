package com.chronopost.vision.microservices.getsyntheseagence.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static java.util.Arrays.asList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.table.ETableColisAgence;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.ColisByAgence;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.ColisSpecByAgence;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Stopwatch;
import com.google.common.reflect.TypeToken;

public enum SyntheseAgenceDaoImpl implements ISyntheseAgenceDao {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(SyntheseAgenceDaoImpl.class);
	private final PreparedStatement psColisAgence;
	private final PreparedStatement psColisSaisisAgence;
	private final PreparedStatement psSpecifColis;

	private final PreparedStatement psColisAgenceColisRestantTg2;
	/** Mise à jour du compteur de microservice **/
	private final PreparedStatement psUpdateCptTrtTrtFailMS;
	private final PreparedStatement psUpdateCptFailMS;
	private final PreparedStatement psUpdateCptHitMS;

	private final static int HEURE_PASSE = 23;
	private final static int MINUTE_PASSE = 5;

	private SyntheseAgenceDaoImpl() {
		// select * from colis_agence where POSTE_COMPTABLE = ?, JOUR = ?, HEURE
		// = ?, MINUTE = ?
		psColisAgence = getSession().prepare(buildSelect(ETableColisAgence.TABLE_NAME,
				asList(ETableColisAgence.COLIS_SAISIS, ETableColisAgence.COLIS_A_SAISIR)).getQuery());

		final String selectColisSaisisAgenceQuery = QueryBuilder.select(ETableColisAgence.COLIS_SAISIS.getNomColonne())
				.from(ETableColisAgence.TABLE_NAME)
				.where(QueryBuilder.eq(ETableColisAgence.POSTE_COMPTABLE.getNomColonne(), QueryBuilder.bindMarker()))
				.and(QueryBuilder.eq(ETableColisAgence.JOUR.getNomColonne(), QueryBuilder.bindMarker()))
				.and(QueryBuilder.eq(ETableColisAgence.HEURE.getNomColonne(), QueryBuilder.bindMarker()))
				.and(QueryBuilder.eq(ETableColisAgence.MINUTE.getNomColonne(), QueryBuilder.bindMarker()))
				.getQueryString();

		psSpecifColis = getSession().prepare(buildSelect(ETableColisSpecifications.TABLE_NAME,
				asList(ETableColisSpecifications.NO_LT, ETableColisSpecifications.ETAPES,
						ETableColisSpecifications.INFO_SUPP, ETableColisSpecifications.SPECIFS_EVT,
						ETableColisSpecifications.SPECIFS_SERVICE, ETableColisSpecifications.CONTRAT,
						ETableColisSpecifications.PRODUIT, ETableColisSpecifications.CODE_POSTAL,
						ETableColisSpecifications.SERVICE, ETableColisSpecifications.ALERTES)).getQuery());

		psColisSaisisAgence = getSession().prepare(selectColisSaisisAgenceQuery);

		// select colis_restant_tg2 from colis_agence where POSTE_COMPTABLE = ?,
		// JOUR = ?, HEURE = ?, MINUTE = ?
		psColisAgenceColisRestantTg2 = getSession().prepare(
				buildSelect(ETableColisAgence.TABLE_NAME, asList(ETableColisAgence.COLIS_RESTANT_TG2)).getQuery());

		psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	@Override
	public ColisSpecByAgence getDispersionByAgencePeriode(final String posteComptable, final Date startDate,
			final Date endDate) {
		final Stopwatch globalStopwatch = Stopwatch.createStarted();

		final ColisSpecByAgence dispersionAgence = new ColisSpecByAgence();

		try {
			final Stopwatch stopwatch = Stopwatch.createStarted();

			ColisByAgence colis = new ColisByAgence();
			try {
				colis = getColisByAgence(posteComptable, startDate, endDate);
			} catch (ParseException e) {
				log.error("Error when getColisByAgence for " + posteComptable + " - " + startDate + " - " + endDate, e);
			}

			log.debug("\tgetColisByAgence : {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
			stopwatch.reset().start();

			dispersionAgence.putAllToColisSaisis(getSpecifColis(colis.getColisSaisis()));

			log.debug("\tgetSpecifColis(COLIS_SAISIS) : {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
			stopwatch.reset().start();

			dispersionAgence.putAllToColisASaisir(getSpecifColis(colis.getColisASaisir()));
			log.debug("\tgetSpecifColis(COLIS_A_SAISIR) : {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
			stopwatch.reset().start();

			/*
			 * Mémorisation du nombre de colis rapporté dans les compteurs du
			 * microservice
			 */
			if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)) {
				updateCptTrtTrtFailMS(colis.getNbreAllColis(), 0);
			}
		} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException
				| UnsupportedFeatureException | InterruptedException | ExecutionException e) {
			throw new MSTechnicalException("Requête sur la table <" + ETableColisAgence.TABLE_NAME + "> impossible", e);
		}

		log.debug("Total : {} ms", globalStopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

		return dispersionAgence;
	}

	@Override
	public ColisSpecByAgence getDistributionByAgencePeriode(final String posteComptable, final Date startDate,
			final Date endDate) {
		final ColisSpecByAgence dispersionAgence = new ColisSpecByAgence();

		try {
			ColisByAgence colis = new ColisByAgence();
			try {
				colis = getColisSaisisByAgence(posteComptable, startDate, endDate);
			} catch (ParseException e) {
				log.error("Error when getColisByAgence for " + posteComptable + " - " + startDate + " - " + endDate, e);
			}

			dispersionAgence.putAllToColisSaisis(getSpecifColis(colis.getColisSaisis()));
		} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException
				| UnsupportedFeatureException | InterruptedException | ExecutionException e) {
			throw new MSTechnicalException("Requête sur la table <" + ETableColisAgence.TABLE_NAME + "> impossible", e);
		}

		return dispersionAgence;
	}

	@Override
	public ColisSpecByAgence getDispersionByAgencePeriodePassee(final String posteComptable, final Date startDate,
			final Date endDate) {
		final Stopwatch globalStopwatch = Stopwatch.createStarted();

		final ColisSpecByAgence dispersionAgence = new ColisSpecByAgence();

		try {
			final Stopwatch stopwatch = Stopwatch.createStarted();

			ColisByAgence colis = new ColisByAgence();
			try {
				colis = getColisByAgencePassee(posteComptable, startDate, endDate);
			} catch (ParseException e) {
				log.error(
						"Error when getColisByAgencePassee for " + posteComptable + " - " + startDate + " - " + endDate,
						e);
			}

			log.debug("\tgetColisByAgencePassee : {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
			stopwatch.reset().start();

			dispersionAgence.putAllToColisRestantTg2(getSpecifColis(colis.getColisRestantTg2()));
			log.info("\tgetColisByAgencePassee(COLIS_RESTANT_TG2) : {} ms",
					stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

		} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException
				| UnsupportedFeatureException | InterruptedException | ExecutionException e) {
			throw new MSTechnicalException("Requête sur la table <" + ETableColisAgence.TABLE_NAME + "> impossible", e);
		}

		log.debug("Total : {} ms", globalStopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

		return dispersionAgence;
	}

	/**
	 * Renvoie la liste de colis à saisir et les colis saisis
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l'agence
	 * @param startDate
	 * @param endDate
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ParseException
	 */
	private ColisByAgence getColisSaisisByAgence(final String posteComptable, final Date startDate, final Date endDate)
			throws InterruptedException, ExecutionException, ParseException {
		final ColisByAgence colis = new ColisByAgence();

		final List<ResultSetFuture> futures = new ArrayList<>();

		final DateTime start = new DateTime(startDate);
		final DateTime end = new DateTime(endDate);
		final int lowerDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(start.toDate()));
		final int upperDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(end.toDate()));
		final long lower = Long.parseLong(start.toString("yyyyMMddHHmm"));
		final long upper = Long.parseLong(end.toString("yyyyMMddHHmm"));

		for (int day = lowerDay; day <= upperDay; day = incrementDay(String.valueOf(day))) {
			for (int hour = 0; hour < 24; hour++) {
				for (int minute = 0; minute < 6; minute++) {
					long current = (day * 10000l) + (hour * 100l) + minute;
					if (lower <= current && current <= upper) {
						futures.add(getSession().executeAsync(psColisSaisisAgence.bind(posteComptable,
								Integer.toString(day), String.format("%02d", hour), Integer.toString(minute))));
					}
				}
			}
		}

		for (final ResultSetFuture resultSet : futures) {
			for (final Row row : resultSet.getUninterruptibly().all()) {
				colis.addAllToColisSaisis(row.getSet(0, String.class));
			}
		}

		return colis;
	}

	/**
	 * Renvoie la liste de colis à saisir et les colis saisis
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l'agence
	 * @param startDate
	 * @param endDate
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ParseException
	 */
	private ColisByAgence getColisByAgence(final String posteComptable, final Date startDate, final Date endDate)
			throws InterruptedException, ExecutionException, ParseException {
		final ColisByAgence colis = new ColisByAgence();

		final List<ResultSetFuture> futures = new ArrayList<>();

		final DateTime start = new DateTime(startDate);
		final DateTime end = new DateTime(endDate);
		final int lowerDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(start.toDate()));
		final int upperDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(end.toDate()));
		final long lower = Long.parseLong(start.toString("yyyyMMddHHmm"));
		final long upper = Long.parseLong(end.toString("yyyyMMddHHmm"));

		for (int day = lowerDay; day <= upperDay; day = incrementDay(String.valueOf(day))) {
			for (int hour = 0; hour < 24; hour++) {
				for (int minute = 0; minute < 6; minute++) {
					long current = (day * 10000l) + (hour * 100l) + minute;
					if (lower <= current && current <= upper) {
						futures.add(getSession().executeAsync(psColisAgence.bind(posteComptable, Integer.toString(day),
								String.format("%02d", hour), Integer.toString(minute))));
					}
				}
			}
		}

		for (final ResultSetFuture resultSet : futures) {
			for (final Row row : resultSet.getUninterruptibly().all()) {
				colis.addAllToColisSaisis(row.getSet(0, String.class));
				colis.addAllToColisASaisir(row.getSet(1, String.class));
			}
		}

		return colis;
	}

	/**
	 * Renvoie la liste des colis restant tg2 ( colis en cours) sur l'agence et
	 * sur la période.
	 * 
	 * @param posteComptable
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ParseException
	 */
	private ColisByAgence getColisByAgencePassee(final String posteComptable, final Date startDate, final Date endDate)
			throws InterruptedException, ExecutionException, ParseException {
		final ColisByAgence colis = new ColisByAgence();

		final List<ResultSetFuture> futures = new ArrayList<>();

		final DateTime start = new DateTime(startDate);
		final DateTime end = new DateTime(endDate);
		final int lowerDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(start.toDate()));
		final int upperDay = Integer.parseInt(DateRules.formatDateYYYYMMDD(end.toDate()));

		/*
		 * Récupération des restantTG2 de chaque jour de la période. Seul la
		 * tranche 23h50 contient les restants TG2
		 */
		for (int day = lowerDay; day <= upperDay; day = incrementDay(String.valueOf(day))) {
			futures.add(getSession().executeAsync(psColisAgenceColisRestantTg2.bind(posteComptable,
					Integer.toString(day), String.format("%02d", HEURE_PASSE), Integer.toString(MINUTE_PASSE))));
		}

		for (final ResultSetFuture resultSet : futures) {
			for (final Row row : resultSet.getUninterruptibly().all()) {
				colis.addAllToColisRestantTg2(row.getSet(0, String.class));
			}
		}

		return colis;
	}

	private int incrementDay(final String day) throws ParseException {
		try {
			final DateTime dateTime = new DateTime(DateRules.toDateJourAgence(day)).plusDays(1);
			return Integer.valueOf(DateRules.formatDateYYYYMMDD(dateTime.toDate()));
		} catch (ParseException e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Renvoie les spécifications colis d'une liste de noLt (uniquement les
	 * colis de cette liste. Les sac et autres contenant sont écartés)
	 * 
	 * @param colis
	 *            : une liste de noLt (colis saisis ou colis à saisir)
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Map<String, SpecifsColis> getSpecifColis(final Collection<String> colis)
			throws InterruptedException, ExecutionException {
		final Map<String, SpecifsColis> specifsColis = new HashMap<>();
		final Set<ResultSetFuture> futures = new HashSet<>();

		final Stopwatch stopwatch = Stopwatch.createStarted();

		for (String noLt : colis) {
			futures.add(getSession().executeAsync(psSpecifColis.bind(noLt)));
		}

		log.debug("\t\tadd futures : {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
		stopwatch.reset().start();

		for (final ResultSetFuture rsFuture : futures) {
			final Row row = rsFuture.getUninterruptibly().one();
			if (row != null) {
				final SpecifsColis specifColis = makeSpecifColis(row);
				if (!SpecifsColisRules.estUnContenant(specifColis))
					specifsColis.put(specifColis.getNoLt(), specifColis);
			}
		}

		log.debug("\t\tfetch resultset : {} ms {} colis", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS),
				specifsColis.size());

		return specifsColis;
	}

	/**
	 * Instanciation d'un objet SpecifColis à partir d'une Row effectuée avec la
	 * liste de colonne colonnesSpecifsColis
	 * 
	 * @param row
	 * @return
	 *
	 * @author LGY
	 */
	private SpecifsColis makeSpecifColis(final Row row) {
		final SpecifsColis specifColis = new SpecifsColis();
		specifColis.setNoLt(row.getString(0));
		specifColis.addAllEtapes(row.getMap(1, Date.class, String.class));
		specifColis.addAllInfoSupp(row.getMap(2, String.class, String.class));
		specifColis.setSpecifsEvt(row.getMap(3, Date.class, String.class));
		specifColis.setSpecifsService(row.getMap(4, new TypeToken<Date>() {
			private static final long serialVersionUID = -2211301558495036908L;
		}, new TypeToken<Set<String>>() {
			private static final long serialVersionUID = -2454091878589962535L;
		}));
		specifColis.setNoContrat(row.getString(5));
		specifColis.setCodeProduit(row.getString(6));
		specifColis.setCodePostalDestinataire(row.getString(7));
		specifColis.addAllServices(row.getMap(8, Date.class, String.class));
		specifColis.addAllToAlertes(row.getSet(9, String.class));
		return specifColis;
	}

	/**
	 * Mise à jour du compteur de MS
	 * 
	 * @param evenement
	 * @return
	 */
	public void updateCptTrtTrtFailMS(final int nbTrt, final int nbTrtFail) {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptTrtTrtFailMS.bind((long) nbTrt, (long) nbTrtFail, "getSyntheseAgence", jour,
					heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}

	/**
	 * Mise à jour du compteur de MS
	 * 
	 * @param evenement
	 * @return
	 */
	public void updateCptHitMS() {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptHitMS.bind(new Long(1), "getSyntheseAgence", jour, heure, minute));
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
			getSession().execute(psUpdateCptFailMS.bind(new Long(1), "getSyntheseAgence", jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}
}
