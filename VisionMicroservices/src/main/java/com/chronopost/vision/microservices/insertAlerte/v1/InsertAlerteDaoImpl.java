package com.chronopost.vision.microservices.insertAlerte.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildInsert;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildUpdate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableAlerte;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.gson.Gson;

import jersey.repackaged.com.google.common.collect.Sets;

public enum InsertAlerteDaoImpl implements IInsertAlerteDao {
	INSTANCE;

	/** requête d'insertion d'une alerte dans la table alerte */
	private final PreparedStatement psInsertAlerte;

	/** requête d'insertion d'une alerte dans la table colis_specifications */
	private final PreparedStatement psAttachAlerteToSpecifColis;

	/**
	 * @return VisionMicroserviceApplication.cassandraSession (a
	 *         com.datastax.driver.core )
	 */
	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	private InsertAlerteDaoImpl() {
		psInsertAlerte = getSession().prepare(buildInsert(
				ETableAlerte.TABLE_NAME, Arrays.asList(ETableAlerte.AGENCE, ETableAlerte.JOUR, ETableAlerte.HEURE,
						ETableAlerte.TYPE, ETableAlerte.NO_LT, ETableAlerte.DATE_INSERT),
				TTL.COLISAGENCE.getTimelapse()));

		psAttachAlerteToSpecifColis = getSession().prepare(buildUpdate(ETableColisSpecifications.TABLE_NAME,
				Arrays.asList(ETableColisSpecifications.ALERTES), TTL.COLISAGENCE.getTimelapse()));
	}

	@Override
	public void inserteAlertes(final List<Alerte> alertes)
			throws NoHostAvailableException, QueryExecutionException, QueryValidationException {
		final List<ResultSetFuture> futures = new ArrayList<>();
		for (final Alerte alerte : alertes) {
			futures.add(getSession().executeAsync(psInsertAlerte.bind(alerte.getAgence(), alerte.getJour(),
					alerte.getHeure(), alerte.getType(), alerte.getNoLt(), DateTime.now().toDate())));
		}

		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
	}

	@Override
	public void attachesAlertes(final List<Alerte> alertes)
			throws NoHostAvailableException, QueryExecutionException, QueryValidationException {

		final List<ResultSetFuture> futures = new ArrayList<>();
		final Gson gson = new Gson();

		for (final Alerte alerte : alertes) {
			final List<String> alerteSet = new ArrayList<String>();
			alerteSet.add(alerte.getAgence());
			alerteSet.add(alerte.getJour());
			alerteSet.add(alerte.getHeure());
			alerteSet.add(alerte.getType());

			final String oneAlerte = gson.toJson(alerteSet);
			final Set<String> oneAlerteJson = Sets.newHashSet(oneAlerte);
			futures.add(getSession().executeAsync(psAttachAlerteToSpecifColis.bind(oneAlerteJson, alerte.getNoLt())));
		}

		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
	}
}
