package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableColisAgence;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;

/** @author unknown : JJC getSession + log caps.  + import min.**/
public enum InsertAgenceColisDaoImpl implements IInsertAgenceColisDao {
	INSTANCE;

	/** Log */
	private final Logger log = LoggerFactory.getLogger(InsertAgenceColisDaoImpl.class);

	/** requete d'ajout d'un colis saisi dans une agence */
	private PreparedStatement psUpdateColisSaisis;
	/** requete d'ajout d'un colis a saisir dans une agence */
	private PreparedStatement psUpdateColisASaisir;
	/** requête d'ajout des colis restant Tg2 dans pour une agence à 23h50 */
	private PreparedStatement psUpdateColisRestantTG2;
    /** Mise à jour du compteur de microservice **/
    private PreparedStatement psUpdateCptTrtTrtFailMS;
    private PreparedStatement psUpdateCptFailMS;
    private PreparedStatement psUpdateCptHitMS;

	private InsertAgenceColisDaoImpl() {
		psUpdateColisSaisis = getSession().prepare("update " + ETableColisAgence.TABLE_NAME + " USING TTL "
				+ TTL.COLISAGENCE.getTimelapse() + " set " + ETableColisAgence.COLIS_SAISIS.getNomColonne() + " = "
				+ ETableColisAgence.COLIS_SAISIS.getNomColonne() + " +  ? " + " WHERE "
				+ ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? " + " AND   "
				+ ETableColisAgence.JOUR.getNomColonne() + " = ? " + " AND   " + ETableColisAgence.HEURE.getNomColonne()
				+ " = ? " + " AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = ? ");

		psUpdateColisASaisir = getSession().prepare("update " + ETableColisAgence.TABLE_NAME + " USING TTL "
				+ TTL.COLISAGENCE.getTimelapse() + " set " + ETableColisAgence.COLIS_A_SAISIR.getNomColonne() + " = "
				+ ETableColisAgence.COLIS_A_SAISIR.getNomColonne() + " +  ? " + " WHERE "
				+ ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? " + " AND   "
				+ ETableColisAgence.JOUR.getNomColonne() + " = ? " + " AND   " + ETableColisAgence.HEURE.getNomColonne()
				+ " = '00' " + " AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '0' ");

		psUpdateColisRestantTG2 = getSession().prepare("update " + ETableColisAgence.TABLE_NAME + " USING TTL "
				+ TTL.COLISAGENCE.getTimelapse() + " set " + ETableColisAgence.COLIS_RESTANT_TG2.getNomColonne() + " = "
				+ ETableColisAgence.COLIS_RESTANT_TG2.getNomColonne() + " +  ? " + " WHERE "
				+ ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? " + " AND   "
				+ ETableColisAgence.JOUR.getNomColonne() + " = ? " + " AND   " + ETableColisAgence.HEURE.getNomColonne()
				+ " = '23' " + " AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '5' ");
		
		psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
	}

	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chronopost.vision.microservices.traitementRetard.TraitementRetardDao
	 * #insertDLE(java.util.List)
	 */
	@Override
	public boolean addColisInSaisisAgence(@NotNull List<Evt> evenements) {
		boolean result = true;
		Set<String> noLtSaisi = new HashSet<String>();
		String pcAgence = "";
		String dateCompleteS;
		String jour = "";
		String heure;
		String minute;
		List<ResultSetFuture> futures = new ArrayList<>();
		/* Gestion des exceptions */
		Exception lastError = null;
		int nbEvtInseres = 0;
		int nbEvtFail = 0;

		for(Evt evenement: evenements) {
			try{
				if (evenement.getDateEvt() != null){
					noLtSaisi.clear();
					noLtSaisi.add(evenement.getNoLt());
					pcAgence = evenement.getLieuEvt();
					dateCompleteS = DateRules.toDateHeureCompact(evenement.getDateEvt());
					jour = dateCompleteS.substring(0,8);
					heure = dateCompleteS.substring(8,10);
					minute = dateCompleteS.substring(10,11);

					futures.add(getSession().executeAsync(psUpdateColisSaisis.bind(noLtSaisi,pcAgence,jour,heure,minute)));
				}
			} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException | UnsupportedFeatureException  e){
				if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
					lastError = e;
					nbEvtFail++;
				}
				else {
					throw new MSTechnicalException("Mise à jour de  la table <" + ETableColisAgence.TABLE_NAME + "> impossible.", e);
				}
			}
		}

		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			for(ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbEvtInseres++;
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e){
					log.error("Insertion colis agence (saisis) impossible ",e);
					nbEvtFail++;
					lastError=e;
				}
			}

			/* On mémorise (si possible le travail effectué, et celui que l'on a pas réussit */
			updateCptTrtTrtFailMS(nbEvtInseres, nbEvtFail);

			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null)
				throw new MSTechnicalException(lastError);
		}
		else {
			for(ResultSetFuture future:futures){
				future.getUninterruptibly();
			}
		}

		return result;
	}

	@Override
	public boolean addColisInASaisirAgence(@NotNull List<EvtExclus> colisExclus) {
		boolean result = true;
		List<ResultSetFuture> futures = new ArrayList<>();
		Set<String> noLtASaisir = new HashSet<String>();
		String dateCompleteS;
		String jour = "";
		/* Gestion des exceptions */
		Exception lastError = null;
		int nbEvtInseres = 0;
		int nbEvtFail = 0;

		for(EvtExclus colisExclu : colisExclus){
			try{
				noLtASaisir.clear();
				noLtASaisir.add(colisExclu.getNoLt());
				dateCompleteS = DateRules.toDateHeureCompact(colisExclu.getDateRepriseEnCompte());
				jour = dateCompleteS.substring(0,8);
				futures.add(getSession().executeAsync(psUpdateColisASaisir.bind(noLtASaisir,colisExclu.getPcAgence(),jour)));
			} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException | UnsupportedFeatureException  e){
				if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
					lastError = e;
					nbEvtFail++;
				}
				else {
					throw new MSTechnicalException("Mise à jour de  la table <" + ETableColisAgence.TABLE_NAME + "> impossible.", e);
				}
			}
		}

		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			for(ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbEvtInseres++;
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e){
					log.error("Insertion colis agence (a saisir) impossible ",e);
					nbEvtFail++;
					lastError=e;
				}
			}

			/* On mémorise (si possible le travail effectué, et celui que l'on a pas réussit */
			updateCptTrtTrtFailMS(nbEvtInseres, nbEvtFail);

			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null)
				throw new MSTechnicalException(lastError);
		}
		else {
			for(ResultSetFuture future:futures){
				try {
					future.getUninterruptibly();
				} catch (NoHostAvailableException|QueryExecutionException|QueryValidationException e){
					log.error("Erreur sur le traitement d'un colis à saisir",e);
					result = false;
				}
			}
		}

		return result;
	}

	@Override
	public boolean updateColisRestantTG2(final String agence, final String jour, final Set<String> noLts) {
		boolean result = true;
		List<ResultSetFuture> futures = new ArrayList<>();
		/* Gestion des exceptions */
		Exception lastError = null;
		int nbEvtInseres = 0;
		int nbEvtFail = 0;


		try{
			futures.add(getSession().executeAsync(psUpdateColisRestantTG2.bind(noLts, agence, jour)));
		} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException | UnsupportedFeatureException  e){
			if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
				lastError = e;
				nbEvtFail++;
			}
			else {
				throw new MSTechnicalException("Mise à jour de  la table <" + ETableColisAgence.TABLE_NAME + "> impossible.", e);
			}
		}

		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			for(ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbEvtInseres++;
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e){
					log.error("Insertion colis agence (a saisir) impossible ",e);
					nbEvtFail++;
					lastError=e;
				}
			}

			/* On mémorise (si possible le travail effectué, et celui que l'on a pas réussit */
			updateCptTrtTrtFailMS(nbEvtInseres, nbEvtFail);

			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null)
				throw new MSTechnicalException(lastError);
		}
		else {
			for (ResultSetFuture future : futures) {
				try {
					future.getUninterruptibly();
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e) {
					log.error("Erreur sur l'update du colis restant TG2", e);
					result = false;
				}
			}
		}
		return result;
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
    			psUpdateCptTrtTrtFailMS.bind((long)nbTrt, (long)nbTrtFail, "insertAgenceColis", jour, heure, minute)
    			);
    	} catch (Exception e){
    		log.warn("Can't write into microservice_counters");
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
    			psUpdateCptHitMS.bind(new Long(1), "insertAgenceColis", jour, heure, minute)
    			);
    	} catch (Exception e){
    		log.warn("Can't write into microservice_counters");
    	}
    }

    public void updateCptFailMS() {
    	DateTime dt = new DateTime();
    	String jour = String.format("%04d%02d%02d", dt.getYear(),dt.getMonthOfYear(),dt.getDayOfMonth());
    	String heure = String.format("%02d", dt.getHourOfDay());
    	String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0,1);
    	try{
    	getSession().execute(
    			psUpdateCptFailMS.bind(new Long(1), "insertAgenceColis", jour, heure, minute)
    			);
    	} catch (Exception e){
    		log.warn("Can't write into microservice_counters");
    	}
    }
}
