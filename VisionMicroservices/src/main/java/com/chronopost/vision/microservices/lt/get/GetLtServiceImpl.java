package com.chronopost.vision.microservices.lt.get;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.google.common.collect.Maps;

/**
 * Implémentation du service IGetLtService La classe va effectuer le travail
 * demandé par les méthode en faisant appel au LTDao
 * 
 * @author jcbontemps
 */
public class GetLtServiceImpl implements IGetLtService {

	private static final Logger logger = LoggerFactory.getLogger(GetLtServiceImpl.class);

    private IGetLtDao dao;

    /**
	 * Singleton
	 */
	static class InstanceHolder {

		public static IGetLtService service = new GetLtServiceImpl();

	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static IGetLtService getInstance() {

		return InstanceHolder.service;
	}
    
    private GetLtServiceImpl() {
        super();
    }
    
    public IGetLtService setDao(IGetLtDao dao){
    	this.dao = dao;
    	return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.lt.get.IGetLtService#getLtsFromDatabase
     * (java.util.List)
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts) {
        return getLtsFromDatabase(noLts, ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.lt.get.IGetLtService#getLtsFromDatabase
     * (java.util.List,
     * com.chronopost.vision.microservices.lt.get.TraitementSynonymes)
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts, ETraitementSynonymes resolutionDesSynonymes) {
        return getLtsFromDatabase(noLts, resolutionDesSynonymes, false);

    }

    /*
     * Les Lts sont récupérées grace au DAO. puis si une résolution des
     * synonymes est demandées, on récupère une seconde fois avec le DAO les LTs
     * maîtres. Enfin, on procède au remplacement des LTs esclaves par les
     * maîtres
     * 
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.lt.get.IGetLtService#getLtsFromDatabase
     * (java.util.List,
     * com.chronopost.vision.microservices.lt.get.TraitementSynonymes,
     * java.lang.Boolean)
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts, ETraitementSynonymes resolutionDesSynonymes,
            Boolean smallQuery) {
        logger.debug("GET LT {}", noLts);
        List<String> ltsAvecMaster = new ArrayList<String>();
        List<String> noLtsMaster = new ArrayList<String>();

        Map<String, Lt> ltsFromDatabase = dao.getLtsFromDatabase(noLts, smallQuery);

        if (resolutionDesSynonymes.equals(ETraitementSynonymes.RESOLUTION_DES_SYNONYMES)) {

            // vérification des numéros des maitres pour la resolution des
            // synonymes
            for (String noLt : ltsFromDatabase.keySet()) {

                Lt lt = ltsFromDatabase.get(noLt);

                if (estUneLtEsclave(lt)) {
                    noLtsMaster.add(lt.getSynonymeMaitre());
                    ltsAvecMaster.add(noLt);
                }
            }

            // puis on récupère les LTs maitres
            Map<String, Lt> ltsMaster = dao.getLtsFromDatabase(noLtsMaster, smallQuery);

            ltsFromDatabase = remplacementDesLtEsclaves(ltsAvecMaster, ltsFromDatabase, ltsMaster);

        }

        return ltsFromDatabase;
    }

    /*
     * 
     * Dans un premier temps on fait une recherche des numéros de LTs
     * correspondant aux critères de recherche, puis avec ces numéros on appelle
     * la méthode classique avec les options demandées (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.lt.get.IGetLtService#
     * getLtsParEmailDestinataire(java.lang.String, java.util.Date,
     * java.util.Date)
     */
    @Override
    public Map<String, Lt> getLtsParEmailDestinataire(String adresseEmailDestinataire, Date dateDebutRecherche,
            Date dateFinRecherche) {
        Map<String, Lt> ltsApresFiltrage = Maps.newHashMap();
        if (adresseEmailDestinataire.contains("@")) {
            List<String> noltsCandidates = dao.rechercheLt("email_1_destinataire",
                    adresseEmailDestinataire.split("@")[0], dateDebutRecherche, dateFinRecherche);
            Map<String, Lt> lts = getLtsFromDatabase(noltsCandidates, ETraitementSynonymes.RESOLUTION_DES_SYNONYMES,
                    false);

            for (Entry<String, Lt> lt : lts.entrySet()) {
                // les adresses email étant séparées en 2 champs dans bco, on
                // doit vérifier que le domaine correspond également dans une
                // deuxième passe
                if (adresseEmailDestinataire.equalsIgnoreCase(lt.getValue().getEmail1Destinataire().trim() + "@"
                        + lt.getValue().getEmail2Destinataire().trim())) {
                    ltsApresFiltrage.put(lt.getKey(), lt.getValue());
                }
            }

            return ltsApresFiltrage;
        } else {
            throw new MSTechnicalException("Adresse email incorrecte : " + adresseEmailDestinataire);
        }

    }

    /**
     * Permet de déterminer si la LT a une LT maitre ou pas. Probablement à
     * déplacer dans LtRules.
     * 
     * @param lt
     * @return true si la LT est bien une esclave
     */
    private boolean estUneLtEsclave(Lt lt) {
        return lt.getSynonymeMaitre() != null && !lt.getSynonymeMaitre().equals(lt.getNoLt());
    }

    private Map<String, Lt> remplacementDesLtEsclaves(final List<String> ltsAvecMaster,
            final Map<String, Lt> ltsFromDatabase, final Map<String, Lt> ltsMaster) {
        Map<String, Lt> ltsDefinitives = new HashMap<String, Lt>();
        ltsDefinitives.putAll(ltsFromDatabase);

        for (String noLt : ltsAvecMaster) {
            // Remplacement de la lt esclave par la LT maitre si elle existe
            if (ltsMaster.containsKey(ltsFromDatabase.get(noLt).getSynonymeMaitre())) {
                ltsDefinitives.put(noLt, ltsMaster.get(ltsFromDatabase.get(noLt).getSynonymeMaitre()));
            }
        }

        return ltsDefinitives;
    }

}
