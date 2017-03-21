package com.chronopost.vision.microservices.insertevt.v1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

/** @author unknown : JJC getSession + LOGGER caps.  + import min.**/
public class InsertEvtDaoImpl implements IInsertEvtDao {
	
	/** logger in caps for sonar. */
	private final static Logger LOGGER = LoggerFactory.getLogger(InsertEvtDaoImpl.class);

	/** Format yyyMMddHHmm */
	private final static SimpleDateFormat FORMAT_JOUR_HEURE_MINUTE = new SimpleDateFormat("yyyyMMddHHmm");
	
	private final PreparedStatement prepStatementInsertEvt;
	private final PreparedStatement prepStatementInsertEvtCounter; //Incrément du Compteur du nombre d'evt insérés dans la table evt
	private final PreparedStatement prepStatementInsertDiffEvtCounter;

    /** Mise à jour du compteur de microservice **/
    private final PreparedStatement psUpdateCptTrtTrtFailMS;
    private final PreparedStatement psUpdateCptFailMS;
    private final PreparedStatement psUpdateCptHitMS;
    
	private CacheManager<Parametre> cacheParametre = null;

    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }
    
	/**
	 * Initialisation des prepare statement
	 */
	private InsertEvtDaoImpl() {
		prepStatementInsertEvt = getSession()
				.prepare("INSERT INTO evt(no_lt, priorite_evt, date_evt, cab_evt_saisi, cab_recu, code_evt, code_evt_ext, code_postal_evt, " + 
						 "code_raison_evt, code_service, createur_evt, date_creation_evt, id_acces_client, id_extraction_evt, id_ss_code_evt, " + 
						 "idbco_evt, infoscomp, libelle_evt, libelle_lieu_evt, lieu_evt, position_evt, prod_cab_evt_saisi, prod_no_lt, " + 
						 "ref_extraction, ref_id_abonnement, ss_code_evt, status_envoi, status_evt) " + 
						 "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
		prepStatementInsertEvtCounter = getSession()
				.prepare("UPDATE evt_counters set evt_in_insertevt=evt_in_insertevt+?,evt_out_insertevt=evt_out_insertevt+?,hit_insertevt=hit_insertevt+1,retards_cumules_hit=retards_cumules_hit+? where jour=? and heure=? and minute=?;");

		prepStatementInsertDiffEvtCounter = getSession()
				.prepare("UPDATE evt_counters set evt_diffuses = evt_diffuses + ?, hit_evt_diffuses = hit_evt_diffuses + 1 where jour= ? and heure= ? and minute= ?;");

		psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
	}

	/**
	 * Singleton
	 */
	static class InstanceHolder {
		public static final IInsertEvtDao dao;
		static {
			dao = new InsertEvtDaoImpl();
		}
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static final IInsertEvtDao getInstance() {
		return InstanceHolder.dao;
	}

	@Override
	public List<Integer> insertEvts(final List<Evt> evts) throws ParseException {
		final List<ResultSetFuture> futures = new ArrayList<>();
		final String now = DateRules.formatDateWS(new Date());
		int nbInsertsReussis = 0; /* Le nombre d'insert reussit = nb evt - nb evt a ne pas insérer - nb insert en echec */
		int nbInsertEnEchec = 0;  /* Nombre d'insert qui sont réellement des echecs */
		boolean colisAIgnorer;
		

		// récupére les exp réguliéres depuis la table Parametre
		List<String> listeDeMasque = null;
		try {
			listeDeMasque = cacheParametre.getValue("filtreColisFictifs").getListValue();
		} catch (Exception e) {
			// en cas d'erreur lors de l'utilisation des masques issus
			// de Parametre, utilise les filtres classiques
			LOGGER.error("Erreurs lors du chargement des filtres des colis fictifs depuis Parametre", e);
			listeDeMasque = null;
		}

		/* insertion de chaque evenement */
		for (final Evt evt : evts) {
			// check le noLt de l'evt pour vérifier si ce n'est pas un colis
			// fictif à ne pas insérer
			colisAIgnorer = false;
			if (FeatureFlips.INSTANCE.getBoolean("FiltreColisFictifs", false)) {
					colisAIgnorer = EvtRules.estUnColisANePasInserer(evt.getNoLt(), listeDeMasque);
			} else {
				colisAIgnorer = EvtRules.estUnColisAIgnorer(evt);
			}
			
			if (!colisAIgnorer) {
				LOGGER.debug("Insertion evt : no_lt {}, priorite_evt {}, date_evt {}, cab_evt_saisi {}, cab_recu {}, code_evt {}, code_evt_ext {}, code_postal_evt {}, " + 
						 "code_raison_evt {}, code_service {}, createur_evt {}, date_creation_evt {}, id_acces_client {}, id_extraction_evt {}, id_ss_code_evt {}, " + 
						 "idbco_evt {}, infoscomp {}, libelle_evt {}, libelle_lieu_evt {}, lieu_evt {}, position_evt {}, prod_cab_evt_saisi {}, prod_no_lt {}, " + 
						 "ref_extraction {}, ref_id_abonnement {}, ss_code_evt {}, status_envoi {}, status_evt {}",evt.getNoLt(), getPrioriteEvt(evt), evt.getDateEvt(), evt.getCabEvtSaisi(), evt.getCabRecu(), evt.getCodeEvt(), evt.getCodeEvtExt(), evt.getCodePostalEvt(), 
						evt.getCodeRaisonEvt(), evt.getCodeService(), evt.getCreateurEvt(), now, evt.getIdAccesClient(), evt.getIdExtractionEvt(), evt.getIdSsCodeEvt(), 
						evt.getIdbcoEvt(), evt.getInfoscomp(), evt.getLibelleEvt(), evt.getLibelleLieuEvt(), evt.getLieuEvt(), evt.getPositionEvt(), evt.getProdCabEvtSaisi(), evt.getProdNoLt(), 
						evt.getRefExtraction(), evt.getRefIdAbonnement(), evt.getSsCodeEvt(), evt.getStatusEnvoi(), evt.getStatusEvt());
			
				futures.add(getSession().executeAsync(prepStatementInsertEvt.bind(evt.getNoLt(), getPrioriteEvt(evt), evt.getDateEvt(), evt.getCabEvtSaisi(), evt.getCabRecu(), evt.getCodeEvt(), evt.getCodeEvtExt(), evt.getCodePostalEvt(), 
						evt.getCodeRaisonEvt(), evt.getCodeService(), evt.getCreateurEvt(), now, evt.getIdAccesClient(), evt.getIdExtractionEvt(), evt.getIdSsCodeEvt(), 
						evt.getIdbcoEvt(), evt.getInfoscomp(), evt.getLibelleEvt(), evt.getLibelleLieuEvt(), evt.getLieuEvt(), evt.getPositionEvt(), evt.getProdCabEvtSaisi(), evt.getProdNoLt(), 
						evt.getRefExtraction(), evt.getRefIdAbonnement(), evt.getSsCodeEvt(), evt.getStatusEnvoi(), evt.getStatusEvt())));
			}
			else {
				LOGGER.info("Evenement à Ne Pas insérer : "+ evt.getNoLt()+" "+evt.getCodeEvt()+" "+evt.getDateEvt());
			}
		}
		
		for (final ResultSetFuture future : futures) {
			try {
				future.getUninterruptibly();
				nbInsertsReussis++;
			} catch (final NoHostAvailableException | QueryExecutionException | QueryValidationException e) {
				LOGGER.error("Impossible d'inserer l'evenement", e);
				nbInsertEnEchec++;
			}
		}
		
		List<Integer> result = new ArrayList<Integer>();
		result.add(new Integer(nbInsertsReussis));
		result.add(new Integer(nbInsertEnEchec));
		return result;
	}
	
	@Override
	public boolean insertEvts(final List<Evt> evts, final long nbEvtsOrig) throws ParseException {
		final List<ResultSetFuture> futures = new ArrayList<>();
		final Date maintenant = new Date();
		final String maintenantString = FORMAT_JOUR_HEURE_MINUTE.format(maintenant);
		int nbInsertsReussis;
		int nbInsertsEchec;
		List<Integer> resultInsert;
		
		/* Insertion des evts */
		resultInsert = insertEvts(evts);
		nbInsertsReussis = resultInsert.get(0);
		nbInsertsEchec = resultInsert.get(1);
		
		// Calcul du retard entre la date du 1er evt a traiter et maintenant
		long retard = 0;
		try {
			if (evts.size() > 0)
				retard = (maintenant.getTime() - evts.get(0).getDateEvt().getTime())/1000L;
		} catch (final IndexOutOfBoundsException e) {
			retard = 0;
		}

		/* incrémentation du compteur evt_out_insertevt */
		futures.add(getSession().executeAsync(prepStatementInsertEvtCounter.bind
			( nbEvtsOrig 
			, Long.valueOf(nbInsertsReussis)
			, Long.valueOf(retard)
			, maintenantString.substring(0, 8)
			, maintenantString.substring(8, 10)
			, maintenantString.substring(10, 11)
			)));
		
		for (final ResultSetFuture future : futures) {
			future.getUninterruptibly();
		}
		
		return nbInsertsEchec == 0;
	}

	/**
	 * Recalcul de la priorité des événements pour reprendre la valeur BCO quoi qu'il arrive.
	 * @param evt
	 * @return
	 */
	public Integer getPrioriteEvt(final Evt evt) {		
		try{			
			return Integer.parseInt(TranscoderService.INSTANCE.getTranscoder("DiffusionVision").transcode("evenements", evt.getIdbcoEvt()+"").split("\\|")[1]);			
		} catch(final Exception e) {
			LOGGER.trace(new StringBuilder().append("Erreur dans getPrioriteEvt() sur idbco_evt ")
					.append(evt.getIdbcoEvt()).append(" : ").append(e.getMessage()).toString(), e);
			LOGGER.trace("Erreur dans getPrioriteEvt() sur evt : no_lt {}, priorite_evt {}, date_evt {}, cab_evt_saisi {}, cab_recu {}, code_evt {}, code_evt_ext {}, code_postal_evt {}, " + 
					 "code_raison_evt {}, code_service {}, createur_evt {}, id_acces_client {}, id_extraction_evt {}, id_ss_code_evt {}, " + 
					 "idbco_evt {}, infoscomp {}, libelle_evt {}, libelle_lieu_evt {}, lieu_evt {}, position_evt {}, prod_cab_evt_saisi {}, prod_no_lt {}, " + 
					 "ref_extraction {}, ref_id_abonnement {}, ss_code_evt {}, status_envoi {}, status_evt {}",evt.getNoLt(), evt.getPrioriteEvt(), evt.getDateEvt(), evt.getCabEvtSaisi(), evt.getCabRecu(), evt.getCodeEvt(), evt.getCodeEvtExt(), evt.getCodePostalEvt(), 
					evt.getCodeRaisonEvt(), evt.getCodeService(), evt.getCreateurEvt(), evt.getIdAccesClient(), evt.getIdExtractionEvt(), evt.getIdSsCodeEvt(), 
					evt.getIdbcoEvt(), evt.getInfoscomp(), evt.getLibelleEvt(), evt.getLibelleLieuEvt(), evt.getLieuEvt(), evt.getPositionEvt(), evt.getProdCabEvtSaisi(), evt.getProdNoLt(), 
					evt.getRefExtraction(), evt.getRefIdAbonnement(), evt.getSsCodeEvt(), evt.getStatusEnvoi(), evt.getStatusEvt());	
		}
		return evt.getPrioriteEvt();		
	}

	/**
	 * Alimentation de la table "evt_counters".</br>
	 * Insérer la taille de la liste des événements à diffuser dans la colonne "evt_diffuses".</br>
	 * Incrémenter le compteur ( la colonne "hit_evt_diffuses") pour chaque insertion.
	 * @param size : la taille de la liste des événements diffusés.
	 */
	public void insertDiffEvtCounter(final int size) {
		final Date maintenant = new Date();
		final String maintenantString = FORMAT_JOUR_HEURE_MINUTE.format(maintenant);

		/* incrémentation du compteur evt_out_insertevt */
		getSession().execute(prepStatementInsertDiffEvtCounter.bind(new Long(size), maintenantString.substring(0, 8),
				maintenantString.substring(8, 10), maintenantString.substring(10, 11)));
	}

    @Override
	public IInsertEvtDao setRefentielParametre(final CacheManager<Parametre> cacheParametre) {
		this.cacheParametre = cacheParametre;
		return this;
	}
    
    /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
    public void updateCptTrtTrtFailMS(final int nbTrt,final int nbTrtFail) {
    	DateTime dt = new DateTime();
    	String jour = String.format("%04d%02d%02d", dt.getYear(),dt.getMonthOfYear(),dt.getDayOfMonth());
    	String heure = String.format("%02d", dt.getHourOfDay());
    	String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0,1);
    	try{
    	getSession().execute(
    			psUpdateCptTrtTrtFailMS.bind((long)nbTrt, (long)nbTrtFail, "insertEvt", jour, heure, minute)
    			);
    	} catch (Exception e){
    		LOGGER.warn("Can't write into microservice_counters");
    	}
    }
    
    /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
    public void updateCptHitMS() {
    	DateTime dt = new DateTime();
    	String jour = String.format("%04d%02d%02d", dt.getYear(),dt.getMonthOfYear(),dt.getDayOfMonth());
    	String heure = String.format("%02d", dt.getHourOfDay());
    	String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0,1);
    	try{
    	getSession().execute(
    			psUpdateCptHitMS.bind(new Long(1), "insertEvt", jour, heure, minute)
    			);
    	} catch (Exception e){
    		LOGGER.warn("Can't write into microservice_counters");
    	}
    }

    public void updateCptFailMS() {
    	DateTime dt = new DateTime();
    	String jour = String.format("%04d%02d%02d", dt.getYear(),dt.getMonthOfYear(),dt.getDayOfMonth());
    	String heure = String.format("%02d", dt.getHourOfDay());
    	String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0,1);
    	try{
    	getSession().execute(
    			psUpdateCptFailMS.bind(new Long(1), "insertEvt", jour, heure, minute)
    			);
    	} catch (Exception e){
    		LOGGER.warn("Can't write into microservice_counters");
    	}
    }
}
