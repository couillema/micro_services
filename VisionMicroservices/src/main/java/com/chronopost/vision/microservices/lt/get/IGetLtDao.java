package com.chronopost.vision.microservices.lt.get;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.chronopost.vision.model.Lt;

/** @author unknown , JJC: remove unused import + add doc. warning on (Attention must call setBuilder once at least).   */
public interface IGetLtDao {

	/** @param lts liste des numéros de LTs (Attention must call setBuilder once at least).
     * @return les LTs correspondantes au numéros demandées .     */
    public Map<String, Lt> getLtsFromDatabase(List<String> lts);
    
    /**@param lts liste des numéros de LTs (Attention must call setBuilder once at least).
     * @param smallQuery récupère-t-on tous les champs ou seulement les colonnes NO_LT, SYNONYME_MAITRE, IDX_DEPASSEMENT et CODE_SERVICE ?
     * @return les LTs correspondantes au numéros demandées. */
    public Map<String, Lt> getLtsFromDatabase(List<String> lts, Boolean smallQuery);
    
    
    /** Attention must call setBuilder once at least. 
     * Recherche de numéros LT correspondant à un des champs indexés dans la table wordIndex.
     * @param champRecherche champ dans lequel faire la recherche
     * @param valeurRecherche valeur à matcher dans le champ recherché
     * @param dateDebutRecherche date de début de recherche
     * @param dateFinRecherche date de fin de recherche
     * @return la liste des numéros des LTs répondant aux critères de recherche   */
    public List<String> rechercheLt(final String champRecherche ,final String valeurRecherche,final Date dateDebutRecherche ,final Date dateFinRecherche);
	
    /** Must be called at least once before using any getters and searchers. */
    public IGetLtDao setLtBuilder(final GetLtBuilder ltBuilder);
}
