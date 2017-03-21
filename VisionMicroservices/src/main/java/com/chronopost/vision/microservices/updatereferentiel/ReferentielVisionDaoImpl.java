package com.chronopost.vision.microservices.updatereferentiel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratCarac;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

public enum ReferentielVisionDaoImpl implements IReferentielVisionDao {
	INSTANCE;

	private PreparedStatement insertRefContratStatement;
	private PreparedStatement getRefContratByVersionStatement;
	private PreparedStatement updateParametreVersionContrat;
	private Session session;
	private CacheManager<Parametre> cacheParametre;

	@Override
	public void setCassandraSession(final Session session) {
		this.session = session;

		// Met à jour les PreparedStatement avec la nouvelle session Cassandra
		this.updatePrepareStatements();
	}

	private void updatePrepareStatements() {
		final Insert insertRefContrat = QueryBuilder.insertInto("ref_contrat")
				.value("numero_version", QueryBuilder.bindMarker()).value("numero_contrat", QueryBuilder.bindMarker())
				.value("caracteristiques", QueryBuilder.bindMarker());
		insertRefContrat.using(QueryBuilder.ttl(QueryBuilder.bindMarker()));
		this.insertRefContratStatement = session.prepare(insertRefContrat);

		final Select selectCountRefContrat = QueryBuilder.select().countAll().from("ref_contrat");
		selectCountRefContrat.where(QueryBuilder.eq("numero_version", QueryBuilder.bindMarker()));
		this.getRefContratByVersionStatement = session.prepare(selectCountRefContrat);

		final Update updateVersionContrat = QueryBuilder.update("parametre");
		updateVersionContrat.with(QueryBuilder.set("valeur", QueryBuilder.bindMarker()));
		updateVersionContrat.where(QueryBuilder.eq("code", QueryBuilder.bindMarker()));
		this.updateParametreVersionContrat = session.prepare(updateVersionContrat);
	}

	@Override
	public void insertRefContrat(final String version, final List<ContratCarac> liste_contrats) throws DriverException {
		final BatchStatement batchInsert = new BatchStatement();
		for (final ContratCarac contratCarac : liste_contrats) {
			final Parametre ttlParam = cacheParametre.getValue("TTL_REF_CONTRAT");
			batchInsert.add(insertRefContratStatement.bind(version, contratCarac.getNoContrat(),
					new HashSet<>(Arrays.asList(contratCarac.getCaracteristiques())),
					Integer.valueOf(ttlParam.getValue())));
		}
		if (batchInsert.size() > 0) {
			session.execute(batchInsert);
		}
	}

	/**
	 * Return true si 'nbContrats' en base avec numero_version = 'version'
	 */
	@Override
	public boolean checkInsertVersionComplete(final String version, final int nbContrats) throws DriverException {
		final ResultSet resultSet = session.execute(getRefContratByVersionStatement.bind(version));
		if (resultSet.one().getLong(0) == nbContrats) {
			return true;
		}
		return false;
	}

	@Override
	public void updateParametreVersionContrat(final String version) throws DriverException {
		session.execute(updateParametreVersionContrat.bind(version, "VERSION_REF_CONTRAT"));
	}

	@Override
	public void setRefentielParametre(final CacheManager<Parametre> cacheManager) {
		this.cacheParametre = cacheManager;
	}
}
