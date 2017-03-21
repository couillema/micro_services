package com.chronopost.vision.microservices.supervision;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildInsert;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static com.chronopost.vision.microservices.VisionMicroserviceApplication.getCassandraSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import com.chronopost.cassandra.request.builder.CassandraSelectRequest;
import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableEvtCounters;
import com.chronopost.cassandra.table.ETableLtCounters;
import com.chronopost.cassandra.table.ETableMSAppels;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.supervision.SnapShotVision;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import com.google.common.util.concurrent.Futures;

public enum SupervisionDaoImpl implements ISupervisionDao {
	INSTANCE;

	private final static SimpleDateFormat jour_SDF = new SimpleDateFormat("yyyyMMdd");

	private final PreparedStatement psGetLtCounters;
	private final PreparedStatement psGetEvtCounters;

	private final List<ETableLtCounters> fieldsSelectLtCounters;
	private final List<ETableEvtCounters> fieldsSelectEvtCounters;

	private final PreparedStatement psInsertMSAppels;

	private SupervisionDaoImpl() {
		final CassandraSelectRequest<ETableLtCounters> selectLtCounter = buildSelect(ETableLtCounters.TABLE_NAME);
		psGetLtCounters = getSession().prepare(selectLtCounter.getQuery());
		fieldsSelectLtCounters = selectLtCounter.getFields();

		final CassandraSelectRequest<ETableEvtCounters> selectEvtCounter = ETableEvtCounters
				.getRequestSelectEvtCounter();
		psGetEvtCounters = getSession().prepare(selectEvtCounter.getQuery());
		fieldsSelectEvtCounters = selectEvtCounter.getFields();

		psInsertMSAppels = getSession().prepare(buildInsert(ETableMSAppels.TABLE_NAME, TTL.MS_APPELS.getTimelapse()));
	}

	/**
	 * Extrait les relevés des tables Evt_counters et Lt_counters pour une
	 * journée
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public List<SnapShotVision> getSnapShotVisionForDay(final Date snapShotDate, final Boolean isToday)
			throws InterruptedException, ExecutionException {
		final String jour = jour_SDF.format(snapShotDate);
		// dans le cas où snapShotDate = today, heureMax et minuteMax sont
		// utilisées pour limiter la recherche aux dizaines de minutes achevées
		final int heureMax = new DateTime(snapShotDate).get(DateTimeFieldType.clockhourOfDay());
		final int minuteMax = Integer.valueOf(
				String.valueOf(new DateTime(snapShotDate).get(DateTimeFieldType.minuteOfHour())).substring(0, 1));
		final List<ResultSetFuture> futuresEvts = new ArrayList<ResultSetFuture>();
		final List<ResultSetFuture> futuresLts = new ArrayList<ResultSetFuture>();
		// RG-MSSupervision-001 : Les compteurs sont construits par tranche de
		// dix minutes
		// parcourt les 24 heures d'une journée
		for (int heure = 0; heure < 24; heure++) {
			// parcourt les 6 dizaines de minutes d'une heure
			for (int minute = 0; minute < 6; minute++) {
				// si journée finie ou bien tranche de 10 minutes dans le passé
				// et terminée
				// RG-MSSupervision-002 : La vitesse instantanée est toujours
				// déduite de la dernière tranche de dix minutes échues
				if (!isToday || (heure < heureMax || (heure == heureMax && minute < minuteMax))) {
					futuresLts.add(getSession().executeAsync(
							psGetLtCounters.bind(jour, String.format("%02d", heure), String.valueOf(minute))));
					futuresEvts.add(getSession().executeAsync(
							psGetEvtCounters.bind(jour, String.format("%02d", heure), String.valueOf(minute))));
				}
			}
		}

		final Map<String, SnapShotVision> resultMap = new TreeMap<>();
		/* Récupération des counters LTs */
		final Future<List<ResultSet>> resultsLts = Futures.successfulAsList(futuresLts);
		final int indexLtInInsertLT = fieldsSelectLtCounters.indexOf(ETableLtCounters.LT_IN_INSERTLT);
		final int indexLtOutInsertLt = fieldsSelectLtCounters.indexOf(ETableLtCounters.LT_OUT_INSERTLT);
		final int indexHitInsertLt = fieldsSelectLtCounters.indexOf(ETableLtCounters.HIT_INSERTLT);
		final int indexHeureLt = fieldsSelectLtCounters.indexOf(ETableLtCounters.HEURE);
		final int indexMinuteLt = fieldsSelectLtCounters.indexOf(ETableLtCounters.MINUTE);
		for (final ResultSet rs : resultsLts.get()) {
			if (rs != null) {
				final List<Row> all = rs.all();
				for (final Row rowLt : all) {
					if (rowLt != null) {
						final SnapShotVision snapShotVision = new SnapShotVision();
						snapShotVision.setAskLt(rowLt.get(indexLtInInsertLT, TypeCodec.counter()));
						snapShotVision.setInsertLt(rowLt.get(indexLtOutInsertLt, TypeCodec.counter()));
						snapShotVision.setHitLt(rowLt.get(indexHitInsertLt, TypeCodec.counter()));
						snapShotVision.setJour(jour);
						snapShotVision.setHeure(rowLt.getString(indexHeureLt));
						snapShotVision.setMinute(rowLt.getString(indexMinuteLt));
						resultMap.put(jour + rowLt.getString(ETableLtCounters.HEURE.getNomColonne())
								+ rowLt.getString(ETableLtCounters.MINUTE.getNomColonne()), snapShotVision);
					}
				}
			}
		}
		/* Récupération des counters Evts */
		final Future<List<ResultSet>> resultsEvts = Futures.successfulAsList(futuresEvts);
		final int indexEvtInInsertEvt = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_IN_INSERTEVT);
		final int indexEvtOutInsertEvt = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_OUT_INSERTEVT);
		final int indexHitInsertEvt = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.HIT_INSERTEVT);
		final int indexEvtDiffuses = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_DIFFUSES);
		final int indexHitEvtDiffuses = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.HIT_EVT_DIFFUSES);
		final int indexHeure = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.HEURE);
		final int indexMinute = fieldsSelectEvtCounters.indexOf(ETableEvtCounters.MINUTE);
		for (final ResultSet rs : resultsEvts.get()) {
			if (rs != null) {
				final List<Row> all = rs.all();
				for (final Row rowEvt : all) {
					if (rowEvt != null) {
						final String keyDate = jour + rowEvt.getString(indexHeure) + rowEvt.getString(indexMinute);
						SnapShotVision snapShotVision = null;
						if (resultMap.containsKey(keyDate)) {
							snapShotVision = resultMap.get(keyDate);
						} else {
							snapShotVision = new SnapShotVision();
						}
						snapShotVision.setAskEvt(rowEvt.get(indexEvtInInsertEvt, TypeCodec.counter()));
						snapShotVision.setInsertEvt(rowEvt.get(indexEvtOutInsertEvt, TypeCodec.counter()));
						snapShotVision.setHitEvt(rowEvt.get(indexHitInsertEvt, TypeCodec.counter()));
						snapShotVision.setDiffEvt(rowEvt.get(indexEvtDiffuses, TypeCodec.counter()));
						snapShotVision.setHitDiffEvt(rowEvt.get(indexHitEvtDiffuses, TypeCodec.counter()));
						snapShotVision.setJour(jour);
						snapShotVision.setHeure(rowEvt.getString(indexHeure));
						snapShotVision.setMinute(rowEvt.getString(indexMinute));
						resultMap.put(keyDate, snapShotVision);
					}
				}
			}
		}
		return new ArrayList<SnapShotVision>(resultMap.values());
	}

	/**
	 * Extrait les relevés des tables Evt_counters et Lt_counters pour la triple
	 * clé (jour, heure, minute)
	 */
	@Override
	public SnapShotVision getSnapShotByKey(final String jour, final String heure, final String minute) {
		final SnapShotVision snapShotVision = new SnapShotVision();
		snapShotVision.setJour(jour);
		snapShotVision.setHeure(heure);
		snapShotVision.setMinute(minute);
		final Row rowLt = getCassandraSession().execute(psGetLtCounters.bind(jour, heure, minute)).one();
		if (null != rowLt) {
			snapShotVision.setAskLt(
					rowLt.get(fieldsSelectLtCounters.indexOf(ETableLtCounters.LT_IN_INSERTLT), TypeCodec.counter()));
			snapShotVision.setInsertLt(
					rowLt.get(fieldsSelectLtCounters.indexOf(ETableLtCounters.LT_OUT_INSERTLT), TypeCodec.counter()));
			snapShotVision.setHitLt(
					rowLt.get(fieldsSelectLtCounters.indexOf(ETableLtCounters.HIT_INSERTLT), TypeCodec.counter()));
		}
		final Row rowEvt = getCassandraSession().execute(psGetEvtCounters.bind(jour, heure, minute)).one();
		if (null != rowEvt) {
			snapShotVision.setAskEvt(rowEvt.get(fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_IN_INSERTEVT),
					TypeCodec.counter()));
			snapShotVision.setInsertEvt(rowEvt.get(fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_OUT_INSERTEVT),
					TypeCodec.counter()));
			snapShotVision.setHitEvt(
					rowEvt.get(fieldsSelectEvtCounters.indexOf(ETableEvtCounters.HIT_INSERTEVT), TypeCodec.counter()));
			snapShotVision.setDiffEvt(
					rowEvt.get(fieldsSelectEvtCounters.indexOf(ETableEvtCounters.EVT_DIFFUSES), TypeCodec.counter()));
			snapShotVision.setHitDiffEvt(rowEvt.get(fieldsSelectEvtCounters.indexOf(ETableEvtCounters.HIT_EVT_DIFFUSES),
					TypeCodec.counter()));
		}
		return snapShotVision;
	}

	@Override
	public boolean checkSessionActive() throws Exception {
		final ResultSet execute = getSession().execute("SELECT now() FROM system.local");
		final UUID time = execute.one().get(0, TypeCodec.timeUUID());
		return StringUtils.isNotEmpty(String.valueOf(time.timestamp()));
	}

	@Override
	public void insertMSAppelsInfos(final String microService, final Date date, final String url, final Long duree, final String userAgent) {
		getSession().execute(psInsertMSAppels.bind(microService, date, userAgent, url, duree.intValue()));
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}
}
