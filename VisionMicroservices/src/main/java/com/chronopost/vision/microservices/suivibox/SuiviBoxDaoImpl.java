package com.chronopost.vision.microservices.suivibox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

/**
 * Classe de communication avec le BIGDATA Cassandra
 * 
 * @author jcbontemps
 */
public class SuiviBoxDaoImpl implements SuiviBoxDao {

    private final Logger logger = LoggerFactory.getLogger(SuiviBoxDaoImpl.class);
	/** Format yyyMMddHHmm */
	private final static SimpleDateFormat FORMAT_JOUR_HEURE_MINUTE = new SimpleDateFormat("yyyMMddHHmm");

    private PreparedStatement insertBoxAgenceStatement;
    private PreparedStatement prepStatementInsertEvtCounter; //Incrément du Compteur du nombre de box mises à jour 
    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    private SuiviBoxDaoImpl() {
        insertBoxAgenceStatement = getSession().prepare(
                "INSERT INTO boxagence (id_box, date_dernier_evt, pc_agence,action,etape,code_tournee,code_lr) values (?,?,?,?,?,?,?) USING TTL "
                        + TTL.SUIVIBOX.getTimelapse() + ";");
		prepStatementInsertEvtCounter = VisionMicroserviceApplication.getCassandraSession()
				.prepare("UPDATE evt_counters set evt_suivi_box=evt_suivi_box+? where jour=? and heure=? and minute=?;");
        
    }

    /**
     * Singleton
     */
    private static class InstanceHolder {
        private static final SuiviBoxDaoImpl dao = new SuiviBoxDaoImpl();
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static SuiviBoxDaoImpl getInstance() {
        return InstanceHolder.dao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.suivibox.insert.dao.ISuiviBoxDAO#
     * updateAgenceBox(java.util.List)
     */
    public boolean updateAgenceBox(@NotNull List<SuiviBoxAgence> listeBox) {

        try {
            List<ResultSetFuture> futures = new ArrayList<ResultSetFuture>();
    		String dateJour = FORMAT_JOUR_HEURE_MINUTE.format(new Date());

            // On execute chaque requête en asynchrone pour eviter de les
            // serialiser
            for (SuiviBoxAgence box : listeBox) {
                futures.add(getSession().executeAsync(
                        insertBoxAgenceStatement.bind
                        	( box.getIdBox()
                        	, box.getDateDernierEvt()
                        	, box.getPcAgence()
                        	, box.getAction()
                        	, box.getEtape()
                        	, box.getCodeTournee()
                        	, box.getCodeLR()
                        	)
                        )
                       );
            }

    		/* incrémentation du compteur evt_out_insertevt */
    		if (FeatureFlips.INSTANCE.getBoolean("CompteurInsertEvt", false) && dateJour.length() == 12)
    			futures.add(VisionMicroserviceApplication.getCassandraSession().executeAsync(prepStatementInsertEvtCounter.bind
    				( Long.valueOf(listeBox.size())
    				, dateJour.substring(0, 8)
    				, dateJour.substring(8, 10)
    				, dateJour.substring(10, 11)
    				)));

            // Attente du retour de chaque requête
            for (ResultSetFuture future : futures) {
                future.getUninterruptibly();
            }

        } catch (Exception e) {
            logger.error("Erreur d'insertion dans SuiviBoxAgence", e);
            throw new MSTechnicalException(e);
        }

        return true;
    }

}
