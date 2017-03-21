package com.chronopost.vision.microservices.traitementRetard;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildInsert;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableDateLivraisonEstimeeLt;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.Futures;

/** @author unknown : JJC getSession + LOGGER caps.  + import min.**/
public class TraitementRetardDaoImpl implements ITraitementRetardDao {

    /** Log Caps for sonar.     */
	private static final Logger LOGGER = LoggerFactory.getLogger(TraitementRetardDaoImpl.class);

    /** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }
    
	/** Format retournée par le Calcul Retard : dd/MM/yyyy */
	private static final SimpleDateFormat CALCULRETARD_FMT = new SimpleDateFormat("dd/MM/yyyy");

	private final PreparedStatement psInsertDLE;
	private final PreparedStatement psSelectMaxDLE;

	private TraitementRetardDaoImpl() {
		psInsertDLE = getSession().prepare(buildInsert(
				ETableDateLivraisonEstimeeLt.TABLE_NAME, Arrays.asList(ETableDateLivraisonEstimeeLt.NO_LT,
						ETableDateLivraisonEstimeeLt.DATE_ESTIMEE, ETableDateLivraisonEstimeeLt.DATE_INSERTION),
				TTL.TRAITEMENTRETARD.getTimelapse()));

		psSelectMaxDLE = getSession().prepare(buildSelect(ETableDateLivraisonEstimeeLt.TABLE_NAME,
				Arrays.asList(ETableDateLivraisonEstimeeLt.DATE_ESTIMEE), ETableDateLivraisonEstimeeLt.NO_LT).getQuery()
						.limit(1));
	}

	/**
	 * Singleton
	 */
	static class InstanceHolder {
		public static ITraitementRetardDao service;
		static {
			if (service == null) {
				service = new TraitementRetardDaoImpl();
			}
		}
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static ITraitementRetardDao getInstance() {
		return InstanceHolder.service;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chronopost.vision.microservices.traitementRetard.TraitementRetardDao
	 * #insertDLE(java.util.List)
	 */
	@Override
	public boolean insertDLE(final List<TraitementRetardWork> retards) throws FunctionalException {
		final List<ResultSetFuture> futures = Lists.newArrayList();
		for (final TraitementRetardWork retard : retards) {
			final String dateLivS = retard.getResultCR().getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee();
			try {
				futures.add(getSession().executeAsync(
						psInsertDLE.bind(retard.getLt().getNoLt(), CALCULRETARD_FMT.parse(dateLivS), new Date())));
			} catch (ParseException e) {
				LOGGER.error("Date livraison estimée au mauvais format pour la lt" + retard.getLt().getNoLt(), e);
				throw new FunctionalException(
						"Date livraison estimée au mauvais format pour la lt" + retard.getLt().getNoLt(), e);
			}
		}

		/* On attend que les acquittements des inserts reviennent */
		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chronopost.vision.microservices.traitementRetard.TraitementRetardDao
	 * #selectMaxDLE(java.util.List)
	 */
	@Override
	public List<TraitementRetardWork> selectMaxDLE(final List<TraitementRetardInput> pRetardsInput) {
		final List<ResultSetFuture> futures = new ArrayList<>();
		final List<TraitementRetardWork> retards = new ArrayList<>();

		if (pRetardsInput == null) {
			LOGGER.error("Liste de retard = null");
			return null;
		}
		for (final TraitementRetardInput retard : pRetardsInput) {
			if (retard.getLt() != null && retard.getLt().getNoLt() != null) {
				// logger.info("Recherche retard max pour
				// <"+retard.getLt().getNo_lt()+">");
				final TraitementRetardWork retardWork = new TraitementRetardWork(null, retard);
				retards.add(retardWork);
				futures.add(getSession().executeAsync(psSelectMaxDLE.bind(retard.getLt().getNoLt())));
			}
		}

		/*
		 * Puis on attend le retour de toutes les requetes et on traite les
		 * résultats
		 */
		final Future<List<ResultSet>> results = Futures.successfulAsList(futures);
		final Iterator<TraitementRetardWork> iterRetard = retards.iterator();
		Date maxDLETrouvee;

		/*
		 * on récupère la maxDLE par LT, et on le positionne dans l'objet
		 * TraitementRetardWork correspondant
		 */
		try {
			for (final ResultSet resultSet : results.get()) {
				final TraitementRetardWork retard = iterRetard.next();
				final Row row = resultSet.one();
				maxDLETrouvee = (row != null)
						? row.getTimestamp(ETableDateLivraisonEstimeeLt.DATE_ESTIMEE.getNomColonne()) : null;
				// TraitementRetardWork retard = new
				// TraitementRetardWork(maxDLETrouvee, retardInput);
				// retards.add(retard);
				retard.setMaxDLE(maxDLETrouvee);
			}
		} catch (final InterruptedException | ExecutionException e) {
			return null;
		}
		return retards;
	}
}
