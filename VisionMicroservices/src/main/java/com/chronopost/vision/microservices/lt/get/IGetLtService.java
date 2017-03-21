package com.chronopost.vision.microservices.lt.get;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.model.Lt;

/**
 * interface du service utilisé pour récupérer les LTs
 * 
 * @author jcbontemps
 */
public interface IGetLtService {

    /**
     * Renvoi une map de LTs correspondant aux numéros fournis en paramètres
     * 
     * @param noLts
     *            une chaine json de numéros de LTs
     * @return Une Map contenant les LTs
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts);

    /**
     * Renvoi une map de LTs correspondant aux numéros fournis en paramètres
     * avec éventuellement résolution de synonymie
     * 
     * @param noLts
     *            une chaine json de numéros de LTs
     * @param resolutionDesSynonymes
     *            doit-on renvoyer le LT ou le LT maître qui lui est associé ?
     * @param smallQuery
     *            si true alors les LTs retournées sont en format "réduit":
     *            seules les colonnes NO_LT, SYNONYME_MAITRE, IDX_DEPASSEMENT et
     *            CODE_SERVICE sont renseignées
     * @return Une Map contenant les LTs
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts, ETraitementSynonymes resolutionDesSynonymes,
            Boolean smallQuery);

    /**
     * Renvoi une map de LTs correspondant aux numéros fournis en paramètres
     * avec éventuellement résolution de synonymie
     * 
     * @param noLts
     *            une chaine json de numéros de LTs
     * @param resolutionDesSynonymes
     *            doit-on renvoyer le LT ou le LT maître qui lui est associé ?
     * @return Une Map contenant les LTs
     */
    public Map<String, Lt> getLtsFromDatabase(List<String> noLts, ETraitementSynonymes resolutionDesSynonymes);

    /**
     * Permet de rechercher une liste de LT par l'email du destinataire
     * 
     * @param adresseEmailDestinataire
     *            adresse mail du destinataire
     * @param dateDebutRecherche
     *            date de début de recherche
     * @param dateFinRecherche
     *            date de fin de recherche
     * @return Les LTs correspondant à cette recherche
     */
    public Map<String, Lt> getLtsParEmailDestinataire(String adresseEmailDestinataire, Date dateDebutRecherche,
            Date dateFinRecherche);

    /**
     * Injection du dao.
     * 
     * @param dao
     * @return
     */
    public IGetLtService setDao(IGetLtDao dao);

}
