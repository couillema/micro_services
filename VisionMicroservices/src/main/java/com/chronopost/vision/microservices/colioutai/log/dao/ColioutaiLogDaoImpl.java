package com.chronopost.vision.microservices.colioutai.log.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.ColioutaiLog;
import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.Lists;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class ColioutaiLogDaoImpl implements ColioutaiLogDao {
	 /** Logger in caps for sonar.*/
	 private static final Logger LOGGER = LoggerFactory.getLogger(ColioutaiLogDaoImpl.class);
	 
	 private Session session;
	 
	 private final Map<String,String> map = new HashMap<>();
	 
	 private PreparedStatement insertColioutaiLogStatement;
	 
	 private PreparedStatement preparedStatementGetColioutaiLog;

	/** Set session and does inti.	 */
	public ColioutaiLogDaoImpl() {
    	session = VisionMicroserviceApplication.getCassandraSession();
    	init();
	}
	/*
	 * dateLog text,
	dateHeureLog timestamp,
  	idLog timeuuid,
  	detail map<text,text>,
	 */
	private void init() {
		insertColioutaiLogStatement = session.prepare("insert into colioutai_log (type_log, date_heure_log, detail) values (?, ?, ?) USING TTL "+TTL.COLIOUTAILOG.getTimelapse()+";");
		preparedStatementGetColioutaiLog = session.prepare("select type_log, date_heure_log, detail from colioutai_log where type_log = ? and date_heure_log >= ? and date_heure_log < ?");
	}

	/**
	 * Singleton
	 */
	static class InstanceHolder {
		public static ColioutaiLogDaoImpl service;
		static {
			service = new ColioutaiLogDaoImpl();
		}
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static ColioutaiLogDaoImpl getInstance() {
		return InstanceHolder.service;
	}
	
	@Timed
	public boolean insertLog(final ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException {
		try {
			final List<ResultSetFuture> futures = Lists.newArrayList();
			final Calendar now = Calendar.getInstance();
			final Date dateDuJour = now.getTime();
			final String typeLog = colioutaiInfoLT.getTypeLog();
			final Timestamp dateHeureLog = new Timestamp(dateDuJour.getTime());
			
			map.put("typeUser", colioutaiInfoLT.getTypeLog()!= null? colioutaiInfoLT.getTypeLog() : "");
			map.put("ip", colioutaiInfoLT.getIp() != null? colioutaiInfoLT.getIp() : "");
			map.put("no_LT", colioutaiInfoLT.getNoLt() != null? colioutaiInfoLT.getNoLt() : "");
			map.put("numDossier", colioutaiInfoLT.getNumDossier() != null? colioutaiInfoLT.getNumDossier() : "");
			map.put("positionTournee", (colioutaiInfoLT.getPositionTournee() != null)? colioutaiInfoLT.getPositionTournee().toString() : "");
			map.put("codeAgence", colioutaiInfoLT.getCodeAgence() != null? colioutaiInfoLT.getCodeAgence() : "");
			map.put("codeTournee", colioutaiInfoLT.getCodeTournee() != null? colioutaiInfoLT.getCodeTournee() : "");
			map.put("etaMaj", colioutaiInfoLT.getEtaMaj() != null? colioutaiInfoLT.getEtaMaj() : "");
			map.put("indiceConfiance", colioutaiInfoLT.getIndiceConfiance() != null? colioutaiInfoLT.getIndiceConfiance().name() : "");
			map.put("etaInitial", colioutaiInfoLT.getEtaInitial() != null? colioutaiInfoLT.getEtaInitial() : "");
			map.put("creneau", colioutaiInfoLT.getCreneau() != null? colioutaiInfoLT.getCreneau() : "");
			
			futures.add(session.executeAsync(insertColioutaiLogStatement.bind(typeLog,dateHeureLog,map)));
			
			for(final ResultSetFuture future:futures){
				future.getUninterruptibly();
			}
		} catch(Exception e){
            LOGGER.error("Erreur d'insertion dans colioutai_log", e);
            throw new MSTechnicalException(e);
        }
		return true;
	}

	@Override
	public List<ColioutaiLog> getColioutaiLog(final String typeLog, final Date from, final Date to) throws MSTechnicalException {
		if(from == null || to == null)
			throw new MSTechnicalException("bad param from getColioutaiLog.");
		
		final DateTime dtfrom = new DateTime(from.getTime());		
		final DateTime dtto = new DateTime(to.getTime());		
		
		final List<ColioutaiLog> listColioutaiLog = new ArrayList<>();
		final ResultSetFuture resultSetFuture = session.executeAsync(preparedStatementGetColioutaiLog.bind(typeLog,dtfrom.withTimeAtStartOfDay().toDate(),dtto.plusDays(1).withTimeAtStartOfDay().toDate()));
		final ResultSet resultColioutaiLog = resultSetFuture.getUninterruptibly();
		for (final Row row : resultColioutaiLog) {
			ColioutaiLog colioutaiLog = new ColioutaiLog();
			colioutaiLog.setTypeUser(row.getString("type_log"));
			colioutaiLog.setDateHeureLog(row.getTimestamp("date_heure_log"));
			colioutaiLog.setCodeAgence(row.getMap("detail", String.class, String.class).get("codeAgence"));
			colioutaiLog.setCodeTournee(row.getMap("detail", String.class, String.class).get("codeTournee"));
			colioutaiLog.setCreneau( row.getMap("detail", String.class, String.class).get("creneau"));
			colioutaiLog.setEtaInitial( row.getMap("detail", String.class, String.class).get("etaInitial"));
			colioutaiLog.setEtaMaj(row.getMap("detail", String.class, String.class).get("etaMaj"));
			colioutaiLog.setIndiceConfiance( row.getMap("detail", String.class, String.class).get("indiceConfiance"));
			colioutaiLog.setIp( row.getMap("detail", String.class, String.class).get("ip"));			
			colioutaiLog.setNoLT(row.getMap("detail", String.class, String.class).get("no_LT"));
			colioutaiLog.setNumDossier(row.getMap("detail", String.class, String.class).get("numDossier"));
			colioutaiLog.setPositionTournee(row.getMap("detail", String.class, String.class).get("positionTournee"));
			colioutaiLog.setTypeUser(row.getMap("detail", String.class, String.class).get("typeUser"));
			listColioutaiLog.add(colioutaiLog);
		}
		return listColioutaiLog;
	}
}
