package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

/**
 * Implémentation du DAO
 * @author jcbontemps
 *
 */
public enum UpdateSpecificationsColisDaoImpl implements IUpdateSpecificationsColisDao {
    INSTANCE;

	/** Log */
	private final Logger log = LoggerFactory.getLogger(UpdateSpecificationsColisDaoImpl.class);

	
    /** @return VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core ) */
	private final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	private PreparedStatement psAddSpecifsService;

	private PreparedStatement psAddService;

	private PreparedStatement psAddConsigneRecue;

	private PreparedStatement psAddConsigneAnnulee;

	private PreparedStatement psAddConsigneTraitee;

	private PreparedStatement psAddEvt;

	private PreparedStatement psAddEtape;

	private PreparedStatement psAddDateContractuelle;

	private PreparedStatement psAddInfoSupp;
    /** Mise à jour du compteur de microservice **/
    private PreparedStatement psUpdateCptTrtTrtFailMS;
    private PreparedStatement psUpdateCptFailMS;
    private PreparedStatement psUpdateCptHitMS;
    private PreparedStatement psInsertErreur;

	public static UpdateSpecificationsColisDaoImpl getInstance() {
		return INSTANCE;
	}
    
    /**
     * Constructeur
     */
    private UpdateSpecificationsColisDaoImpl() {
        psAddSpecifsService = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.SPECIFS_SERVICE.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddService = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.SERVICE.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddConsigneRecue = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.CONSIGNES_RECUES.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddConsigneAnnulee = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.CONSIGNES_ANNULEES.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddConsigneTraitee = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.CONSIGNES_TRAITEES.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");
        
        psAddEvt = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.SPECIFS_EVT.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddEtape = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.ETAPES.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddDateContractuelle = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.DATE_CONTRACTUELLE.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        psAddInfoSupp = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME + " USING TTL "
                        + TTL.COLIS_SPECIFICITES.getTimelapse() + " set "
                        + ETableColisSpecifications.INFO_SUPP.getNomColonne() + "[?] = ? " + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");
        
        psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
		psInsertErreur = getSession().prepare("insert into microservice_erreur(jour,microservice,methode,objet,date,exception ) values (?,?,?,?,?,?) USING TTL "+TTL.ERREUR.getTimelapse()).setConsistencyLevel(LOCAL_ONE);
    }

    /**
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisDao
     */
    @Override
    public boolean updateSpecifsServices(final List<SpecifsColis> specs) {

//        Algo: Pour chaque colis de List<SpecifsColis> Pour chaque clé,valeur (valeur est un set<String>, généralement
//        il n’y en aura qu’un ici) de colis.specifsService update colis_specifications set specifs_service[clé] =
//        valeur (Async) update colis_specifications set specifs_service[?] = ?

		final List<ResultSetFuture> futures = new ArrayList<>();

		for (final SpecifsColis colis : specs)
			for (final Entry<Date, Set<String>> specifsService : colis.getSpecifsService().entrySet())
				futures.add(getSession().executeAsync(
						psAddSpecifsService.bind(specifsService.getKey(), specifsService.getValue(), colis.getNoLt())));

		for (final SpecifsColis colis : specs) {
			for (final Entry<Date, String> service : colis.getService().entrySet()) {
				futures.add(getSession()
						.executeAsync(psAddService.bind(service.getKey(), service.getValue(), colis.getNoLt())));
			}
		}


		
		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			MSTechnicalException lastError = null;
			int nbTrt = 0;
			int nbTrtFail = 0;
			for(final ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbTrt++;
				} catch (Exception e){
					nbTrtFail++;
					lastError = new MSTechnicalException(e);
					declareErreur(null,"updateSpecifsServices", e);
				}
			}
			updateCptTrtTrtFailMS(nbTrt, nbTrtFail);
			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null){
				throw new MSTechnicalException(lastError);
			}
		}
		else{
			for (final ResultSetFuture future : futures) {
				future.getUninterruptibly();
			}
		}

        return true;
    }

    /**
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.updatespecificationscolis.v1.IUpdateSpecificationsColisDao#updateConsignes (java.util.List)
     */
    public boolean updateConsignes(final List<SpecifsColis> specs) {

//        Algo: Pour chaque colis de List<SpecifsColis> Si colis.consignesRecues != null Pour chaque clé,valeur de
//        colis.consignesRecues update colis_specifications set consignes_recues[clé] = valeur (Async) Si
//        colis.consignesTraitees != null Pour chaque clé,valeur de colis.consignesTraitees update colis_specifications
//        set consignes_traitees[clé] = valeur (Async) Si colis.consignesAnnulees != null Pour chaque clé,valeur de
//        colis.consignesAnnulees update colis_specifications set consignes_annulees[clé] = valeur (Async)

		final List<ResultSetFuture> futures = new ArrayList<>();

		for (final SpecifsColis colis : specs) {
			if (colis.getConsignesRecues() != null) {
				for (final Entry<Date, String> consigneRecue : colis.getConsignesRecues().entrySet()) {
					futures.add(getSession().executeAsync(psAddConsigneRecue.bind(consigneRecue.getKey(),
							consigneRecue.getValue(), colis.getNoLt())));
				}
			}

			if (colis.getConsignesAnnulees() != null) {
				for (final Entry<Date, String> consigneAnnulee : colis.getConsignesAnnulees().entrySet()) {
					futures.add(getSession().executeAsync(psAddConsigneAnnulee.bind(consigneAnnulee.getKey(),
							consigneAnnulee.getValue(), colis.getNoLt())));
				}
			}

			if (colis.getConsignesTraitees() != null) {
				for (final Entry<Date, String> consigneTraitee : colis.getConsignesTraitees().entrySet()) {
					futures.add(getSession().executeAsync(psAddConsigneTraitee.bind(consigneTraitee.getKey(),
							consigneTraitee.getValue(), colis.getNoLt())));
				}
			}
		}

		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			MSTechnicalException lastError = null;
			int nbTrt = 0;
			int nbTrtFail = 0;
			for(final ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbTrt++;
				} catch (Exception e){
					nbTrtFail++;
					lastError = new MSTechnicalException(e);
					declareErreur(null,"updateConsignes", e);
				}
			}
			updateCptTrtTrtFailMS(nbTrt, nbTrtFail);
			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null){
				throw new MSTechnicalException(lastError);
			}
		}
		else{
			for (final ResultSetFuture future : futures) {
				future.getUninterruptibly();
			}
		}
        return true;
    }

    

	/**
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.updatespecificationscolis.v1.IUpdateSpecificationsColisDao#updateSpecifsEvenements(java.util.List)
     */
    @Override
	public boolean updateSpecifsEvenements(final List<SpecifsColis> specifsColis) {
		/*
		 * Algo: Pour chaque colis de List<SpecifsColis> Pour chaque clé,valeur
		 * (valeur est un set<String>, généralement il n’y en aura qu’un ici) de
		 * colis.specifsEvt update colis_specifications set specifs_evt[clé] =
		 * valeur (Async) Pour chaque clé,valeur de colis.datesContractuelles
		 * update colis_specifications set date_contractuelle[clé] = valeur
		 * (Async) Pour chaque clé,valeur de colis.infoSupp update
		 * colis_specifications set info_supp[clé] = valeur (Async)
		 */
		final List<ResultSetFuture> futures = new ArrayList<>();
		for (final SpecifsColis colis : specifsColis) {

			if (colis.getSpecifsEvt() != null) {
				for (final Entry<Date, String> specEvt : colis.getSpecifsEvt().entrySet()) {
					futures.add(getSession()
							.executeAsync(psAddEvt.bind(specEvt.getKey(), specEvt.getValue(), colis.getNoLt())));
				}
			}
			if (colis.getEtapes() != null) {
				for (final Entry<Date, String> etape : colis.getEtapes().entrySet()) {
					futures.add(getSession()
							.executeAsync(psAddEtape.bind(etape.getKey(), etape.getValue(), colis.getNoLt())));
				}
			}
			if (colis.getDatesContractuelles() != null) {
				for (final Entry<Date, Date> dateContractuelle : colis.getDatesContractuelles().entrySet()) {
					futures.add(getSession().executeAsync(psAddDateContractuelle.bind(dateContractuelle.getKey(),
							dateContractuelle.getValue(), colis.getNoLt())));
				}
			}
			if (colis.getInfoSupp() != null) {
				for (final Entry<String, String> infoSupp : colis.getInfoSupp().entrySet()) {
					futures.add(getSession()
							.executeAsync(psAddInfoSupp.bind(infoSupp.getKey(), infoSupp.getValue(), colis.getNoLt())));
				}
			}
		}

		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
			MSTechnicalException lastError = null;
			int nbTrt = 0;
			int nbTrtFail = 0;
			for(final ResultSetFuture future:futures){
				try{
					future.getUninterruptibly();
					nbTrt++;
				} catch (Exception e){
					nbTrtFail++;
					lastError = new MSTechnicalException(e);
					declareErreur(null,"updateSpecifsEvenements", e);
				}
			}
			updateCptTrtTrtFailMS(nbTrt, nbTrtFail);
			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null){
				throw new MSTechnicalException(lastError);
			}
		}
		else{
			for (final ResultSetFuture future : futures) {
				future.getUninterruptibly();
			}
		}

		return true;
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
    			psUpdateCptTrtTrtFailMS.bind((long)nbTrt, (long)nbTrtFail, "updateSpecificationsColis", jour, heure, minute)
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
    			psUpdateCptHitMS.bind(new Long(1), "updateSpecificationsColis", jour, heure, minute)
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
    			psUpdateCptFailMS.bind(new Long(1), "updateSpecificationsColis", jour, heure, minute)
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
    @Override
    public void declareErreur(final Evt evt, final String methode, final Exception except) {
    	final DateTime dt = new DateTime();
    	final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
    	String objetString;
    	if (FeatureFlips.INSTANCE.getBoolean("ErreurMS_Actif", true)){
    		if (evt != null)
    			objetString = evt.getNoLt()+" "+evt.getCodeEvt()+" "+evt.getDateEvt();
    		else 
    			objetString = " - ";

    		try {
    			getSession().execute(psInsertErreur.bind(jour, "updateSpecificationsColis",methode,objetString,dt.toDate(),except.toString()));
    		} catch (Exception e) {
    			log.warn("Can't write into microservice_erreur");
    		}
    	}
    }


}
