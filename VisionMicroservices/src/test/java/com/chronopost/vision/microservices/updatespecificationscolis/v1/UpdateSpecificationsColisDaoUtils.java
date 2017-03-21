package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import org.assertj.core.util.VisibleForTesting;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.vision.cachemanager.codeservice.ESelectService;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.chronopost.vision.stringuts.StrUtils;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.reflect.TypeToken;

/**
 * On a 2 tests sur des appels de base Cassandra (MapperTest et SyntheseTourneeDaoTest) dans ce test on crée beaucoup
 * d'entrées dans la base qui servent au test et on a également besoin de les consulter et les effacer Cette test
 * regroupe donc tous les appels utiles et répétitif pour simplifier la lecture du code
 * 
 * @author jcbontemps
 *
 */
public class UpdateSpecificationsColisDaoUtils {

    /** PreparedStatement pour récupérer une spécification */
    private final static PreparedStatement getSpecificationColis;

    /** PreparedStatement pour récupérer une spécification */
    private final static PreparedStatement delSpecificationColis;

    /** requete d'ajout d'une spécificité service */
    private final static PreparedStatement psAddSpecifsService;

    private final static PreparedStatement psAddService;

    private final static PreparedStatement psAddConsigneRecue;

    private final static PreparedStatement psAddConsigneAnnulee;

    private final static PreparedStatement psAddConsigneTraitee;

    private final static PreparedStatement psAddEvt;

    private final static PreparedStatement psAddEtape ;

    private final static PreparedStatement psAddDateContractuelle ;

    private final static PreparedStatement psAddInfoSupp ;

    private final static PreparedStatement psGetSpecification ;


    /** @return VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core ) */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    @VisibleForTesting static final ETableColisSpecifications[] FULL_SELECT_FIELDS = ETableColisSpecifications.values() ; 
    private static final String FULL_SELECT          =  StrUtils.mkAllNamesCommaSeparated(FULL_SELECT_FIELDS) ;
    
    @VisibleForTesting static final ESelectService[] FULL_SELECT_SERVICE_FIELDS = ESelectService.values() ; 
    private static final String FULL_SELECT_SERVICE          =  StrUtils.mkAllNamesCommaSeparated(FULL_SELECT_SERVICE_FIELDS) ;

    static {
        getSpecificationColis = getSession().prepare(
                "select "
                        + FULL_SELECT
                        + " from " + ETableColisSpecifications.TABLE_NAME + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

        delSpecificationColis = getSession().prepare(
                "delete from " + ETableColisSpecifications.TABLE_NAME + " WHERE "
                        + ETableColisSpecifications.NO_LT.getNomColonne() + " = ? ");

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

        psGetSpecification = getSession().prepare(
                "select "
                        + FULL_SELECT_SERVICE
                        + " from service where socode = ? and ascode=?");

    }

    /**
     * insère une spécification en base
     * @param colis objet SpecifsColis contenant les infos à insérer
     */
    public static void insertSpecificationColis(SpecifsColis colis) {

        if (colis.getConsignesAnnulees() != null)
            for (Entry<Date, String> entry : colis.getConsignesAnnulees().entrySet())
                getSession().execute(psAddConsigneAnnulee.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getConsignesRecues() != null)
            for (Entry<Date, String> entry : colis.getConsignesRecues().entrySet())
                getSession().execute(psAddConsigneRecue.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getConsignesTraitees() != null)
            for (Entry<Date, String> entry : colis.getConsignesTraitees().entrySet())
                getSession().execute(psAddConsigneTraitee.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getSpecifsEvt() != null)
            for (Entry<Date, String> entry : colis.getSpecifsEvt().entrySet())
                getSession().execute(psAddEvt.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getSpecifsService() != null)
            for (Entry<Date, Set<String>> entry : colis.getSpecifsService().entrySet())
                getSession().execute(psAddSpecifsService.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getService() != null)
            for (Entry<Date, String> entry : colis.getService().entrySet())
                getSession().execute(psAddService.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        if (colis.getEtapes() != null)
            for (Entry<Date, String> entry : colis.getEtapes().entrySet())
                getSession().execute(psAddEtape.bind(entry.getKey(), entry.getValue(), colis.getNoLt()));

        
        if (colis.getDatesContractuelles() != null)
            for (Entry<Date, Date> dateContractuelle: colis.getDatesContractuelles().entrySet())
                getSession().execute(psAddDateContractuelle.bind(dateContractuelle.getKey(), dateContractuelle.getValue(), colis.getNoLt()));

        if (colis.getInfoSupp() != null)
            for (Entry<String, String> infoSupp: colis.getInfoSupp().entrySet())
                getSession().execute(psAddInfoSupp.bind(infoSupp.getKey(), infoSupp.getValue(), colis.getNoLt()));

    }

    /**
     * Efface une spécification en base
     * 
     * @param noLt id de la spec
     */
    public static void delSpecificationColis(String noLt) {
        getSession().execute(delSpecificationColis.bind(noLt));
    }

    /**
     * récupère une spécification en base sous forme de Row Cassandra 
     * a des fins de test sur le mapping
     * @param noLt id de la spec
     * @return une row Cassandra correspondant à une spécification en base
     */
    public static Row getRowSpecificationColis(String noLt) {
        ResultSet evtResult = getSession().execute(getSpecificationColis.bind(noLt));
        return evtResult.one();
    }

    /**
     * récupère une spécification en base
     * @param noLt id de la spec
     * @return un objet SpecifColis en base
     */
    public static SpecifsColis getSpecificationColis(String noLt) {
        SpecifsColis colis = new SpecifsColis();

        Row one = getRowSpecificationColis(noLt);
        if (one!=null)
        	colis = makeSpecifsColis(one);

        return colis;
    }

    public static Row getRowSpecifications(String soCode, String asCode) {
        return getSession().execute(psGetSpecification.bind(soCode, asCode)).one() ;
    }

    public Set<String> getSpecifications(String soCode, String asCode) {
        return ESelectService.SPECIFS.getSet(getRowSpecifications(soCode,asCode)) ;
    }

    /**
     * Map une row en un objet SpecifColis
     * 
     * @param row : un enregistrmeent de la table
     * @return Le SpecifsColis correspondant à la row fournie
     *
     * @author LGY
     */
	private static SpecifsColis makeSpecifsColis(final Row row) {
		final SpecifsColis colis = new SpecifsColis();
		try {
			colis.setNoLt(row.getString(ETableColisSpecifications.NO_LT.getNomColonne()));
			colis.setSpecifsEvt(
					row.getMap(ETableColisSpecifications.SPECIFS_EVT.getNomColonne(), Date.class, String.class));
			colis.setConsignesAnnulees(
					row.getMap(ETableColisSpecifications.CONSIGNES_ANNULEES.getNomColonne(), Date.class, String.class));
			colis.setConsignesRecues(
					row.getMap(ETableColisSpecifications.CONSIGNES_RECUES.getNomColonne(), Date.class, String.class));
			colis.setConsignesTraitees(
					row.getMap(ETableColisSpecifications.CONSIGNES_TRAITEES.getNomColonne(), Date.class, String.class));
			colis.addAllEtapes(row.getMap(ETableColisSpecifications.ETAPES.getNomColonne(), Date.class, String.class));
			colis.setSpecifsService(
					row.getMap(ETableColisSpecifications.SPECIFS_SERVICE.getNomColonne(), new TypeToken<Date>() {
						private static final long serialVersionUID = -2211301558495036908L;
					}, new TypeToken<Set<String>>() {
						private static final long serialVersionUID = -2454091878589962535L;
					}));
			colis.setDatesContractuelles(
					row.getMap(ETableColisSpecifications.DATE_CONTRACTUELLE.getNomColonne(), Date.class, Date.class));
			colis.addAllServices(
					row.getMap(ETableColisSpecifications.SERVICE.getNomColonne(), Date.class, String.class));
			colis.addAllInfoSupp(
					row.getMap(ETableColisSpecifications.INFO_SUPP.getNomColonne(), String.class, String.class));
		} catch (Exception e) {
			return new SpecifsColis();
		}
		return colis;
	}

}
