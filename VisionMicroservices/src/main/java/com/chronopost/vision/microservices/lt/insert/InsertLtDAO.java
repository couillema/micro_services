package com.chronopost.vision.microservices.lt.insert;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildUpdate;
import static java.util.Arrays.asList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableLtCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.refcontrat.RefContrat;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.enums.EInsertLT;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;
import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;

/**
 * Classe de communication avec le BIGDATA Cassandra
 * 
 * @author jcbontemps
 */
public class InsertLtDAO {

	private final Map<EInsertLT, PreparedStatement> preparedStatements;
	private final Map<EInsertLT, PreparedStatement> preparedStatementsSpecifColis;
	private final PreparedStatement preparedStatementsInsertLt;
	private final PreparedStatement preparedStatementsInsertSpecifEvt;
	private CacheManager<RefContrat> cacheRefContrat;
	private final Session session;

	private InsertLtDAO() {
		session = VisionMicroserviceApplication.getCassandraSession();

		try {
			this.preparedStatements = new HashMap<>();
			for (final EInsertLT value : EInsertLT.values()) {
				if (value.getType().equals("Set") || value.getType().equals("List")) {
					preparedStatements.put(value, session.prepare("UPDATE lt set " + value.getColName() + " = "
							+ value.getColName() + " + ? where no_lt = ?"));
				} else {
					preparedStatements.put(value,
							session.prepare("UPDATE lt set " + value.getColName() + " = ? where no_lt = ?"));
				}
			}
		} catch (Exception e) {
			throw new MSTechnicalException(
					"Error into init method of com.chronopost.vision.microservices.lt.insert.dao.CassandraDAO", e);
		}
		this.preparedStatementsSpecifColis = new HashMap<>();
		preparedStatementsSpecifColis.put(EInsertLT.CODE_POSTAL_DESTINATAIRE,
				session.prepare("UPDATE colis_specifications set code_postal_destinataire = ? where no_lt = ?"));
		preparedStatementsSpecifColis.put(EInsertLT.CODE_PRODUIT,
				session.prepare("UPDATE colis_specifications set code_produit = ? where no_lt = ?"));
		preparedStatementsSpecifColis.put(EInsertLT.NO_CONTRAT,
				session.prepare("UPDATE colis_specifications set no_contrat = ? where no_lt = ?"));

		preparedStatementsSpecifColis.put(EInsertLT.ID_ADRESSE_DESTINATAIRE,
				session.prepare(buildUpdate(ETableColisSpecifications.TABLE_NAME,
						asList(ETableColisSpecifications.ID_ADRESSE_DESTINATAIRE))));

		preparedStatementsSpecifColis.put(EInsertLT.ID_POI_DESTINATAIRE,
				session.prepare(buildUpdate(ETableColisSpecifications.TABLE_NAME,
						asList(ETableColisSpecifications.ID_POI_DESTINATAIRE))));

		preparedStatementsInsertSpecifEvt = session.prepare(buildUpdate(ETableColisSpecifications.TABLE_NAME,
				QueryBuilder.putAll(ETableColisSpecifications.SPECIFS_EVT.getNomColonne(), QueryBuilder.bindMarker())));

		// create update lt_counters
		final Update updateLtCounters = QueryBuilder.update(ETableLtCounters.TABLE_NAME);
		// set HIT_INSERTLT = HIT_INSERTLT + 1
		updateLtCounters.with(QueryBuilder.incr(ETableLtCounters.HIT_INSERTLT.getNomColonne(), 1L));
		// set LT_IN_INSERTLT = LT_IN_INSERTLT + ?
		updateLtCounters
				.with(QueryBuilder.incr(ETableLtCounters.LT_IN_INSERTLT.getNomColonne(), QueryBuilder.bindMarker()));
		// set LT_OUT_INSERTLT = LT_OUT_INSERTLT + ?
		updateLtCounters
				.with(QueryBuilder.incr(ETableLtCounters.LT_OUT_INSERTLT.getNomColonne(), QueryBuilder.bindMarker()));
		// where JOUR = ?
		updateLtCounters.where(QueryBuilder.eq(ETableLtCounters.JOUR.getNomColonne(), QueryBuilder.bindMarker()));
		// and HEURE = ?
		updateLtCounters.where(QueryBuilder.eq(ETableLtCounters.HEURE.getNomColonne(), QueryBuilder.bindMarker()));
		// and MINUTE = ?
		updateLtCounters.where(QueryBuilder.eq(ETableLtCounters.MINUTE.getNomColonne(), QueryBuilder.bindMarker()));
		preparedStatementsInsertLt = session.prepare(updateLtCounters.getQueryString());
	}

	/**
	 * Singleton
	 */
	static class InstanceHolder {
		public static InsertLtDAO service;
		static {
			service = new InsertLtDAO();
		}
	}

	/**
	 * Singleton
	 */
	public static InsertLtDAO getInstance() {
		return InstanceHolder.service;
	}

	/**
	 * Insertion d'une LT complète ou partielle en base. Pour éviter
	 * l'apparition de tombstones, on insère les enregistrements par update
	 * unitaires batchés ensemble. Les batchs sont envoyés un à un via requête
	 * asynchrone.
	 * 
	 * @param lts
	 * @return
	 * @throws MSTechnicalException
	 * @throws FunctionalException
	 */
	@Timed
	public boolean insertLts(final List<Lt> lts) throws MSTechnicalException, FunctionalException {
		List<ResultSetFuture> futures = new ArrayList<>();
		List<ResultSetFuture> futuresSpecifColis = new ArrayList<>();
		boolean hasSpecifToUpdated;
		final Map<String, RefContrat> contratsAvecQualificatif = cacheRefContrat != null ? cacheRefContrat.getCache()
				: null;
		final Boolean featureQualifContrat = FeatureFlips.INSTANCE.getBoolean("QualificatifContrat", false);

		// parcourt tous les lt
		for (final Lt lt : lts) {
			// filtre les colis fictifs
			if (!LtRules.estUnColisFictif(lt)) {
				final BatchStatement batch = new BatchStatement();
				final BatchStatement batchSpecifColis = new BatchStatement();
				hasSpecifToUpdated = false;
				// pour chaque champ de la LT vérifie que le champ n'est pas
				// vide
				for (final EInsertLT champEnCours : EInsertLT.values()) {
					final Object val = InsertLtMapper.getLtValue(champEnCours, lt);
					// si champ non vide alors récupére le prepStat du champ en
					// cours et le bind
					if (val != null && champEnCours != null && lt != null) {
						if (preparedStatements.containsKey(champEnCours))
							batch.add(preparedStatements.get(champEnCours).bind(val, lt.getNoLt()));
						// regarde si c'est également un champ de specif colis
						if (preparedStatementsSpecifColis.containsKey(champEnCours)) {
							hasSpecifToUpdated = true;
							// RG-MSInsertLt-007
							// champs de type Map avec préfixe sur les valeurs
							if (champEnCours == EInsertLT.ID_ADRESSE_DESTINATAIRE
									|| champEnCours == EInsertLT.ID_POI_DESTINATAIRE) {
								final Map<Date, String> mapSpecifColisUpdate = new HashMap<>();
								mapSpecifColisUpdate.put(new Date(), "L|" + val);
								batchSpecifColis.add(preparedStatementsSpecifColis.get(champEnCours)
										.bind(mapSpecifColisUpdate, lt.getNoLt()));
							} else {
								batchSpecifColis
										.add(preparedStatementsSpecifColis.get(champEnCours).bind(val, lt.getNoLt()));
								// pour le champ contrat et si FeureFlip
								// 'QualificatifContrat' activée
								if (champEnCours == EInsertLT.NO_CONTRAT && featureQualifContrat
										&& contratsAvecQualificatif != null) {
									// si le contrat de la LT posséde un
									// qualificatif depuis la table ref_contrat
									final RefContrat refContrat;
									if ((refContrat = contratsAvecQualificatif.get(val)) != null) {
										final Map<Date, String> caracMap = new HashMap<>();
										int millis = 0;
										final DateTime dateTime = new DateTime();
										for (final String carac : refContrat.getCaracteristiques()) {
											caracMap.put(dateTime.plusMillis(millis).toDate(), carac);
											millis++;
										}
										batchSpecifColis
												.add(preparedStatementsInsertSpecifEvt.bind(caracMap, lt.getNoLt()));
									}
								}
							}
						}
					}
				}
				futures.add(session.executeAsync(batch));
				if (hasSpecifToUpdated)
					futuresSpecifColis.add(session.executeAsync(batchSpecifColis));
			}
		}

		// update et incrémentations des données de LT_COUNTERS
		final SimpleDateFormat FORMAT_JOUR_HEURE_MINUTE = new SimpleDateFormat("yyyyMMddHHmm");
		final Date maintenant = new Date();
		final String maintenantString = FORMAT_JOUR_HEURE_MINUTE.format(maintenant);
		session.execute(preparedStatementsInsertLt.bind(Long.valueOf(lts.size()), Long.valueOf(futures.size()),
				maintenantString.substring(0, 8), maintenantString.substring(8, 10),
				maintenantString.substring(10, 11)));

		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
		for (final ResultSetFuture future : futuresSpecifColis) {
			future.getUninterruptibly();
		}
		return true;
	}

	/** @return variable preparedStatements initialized if init() was called. */
	protected Map<EInsertLT, PreparedStatement> getPs() {
		return preparedStatements;
	}

	public void setReferentielRefContrat(final CacheManager<RefContrat> cacheManager) {
		this.cacheRefContrat = cacheManager;
	}
}
