package com.chronopost.vision.microservices.suivibox;

import java.util.List;

/**
 * Interface de communication avec une base de données
 * @author jcbontemps
 */
public interface SuiviBoxDao {

    /**
     * Insertion des enregistrements dans boxagence à partir d'une liste d'objets SuiviBoxAgence
     * @param listeBox liste des box à insérer
     * @return true si l'insertion a bien été effectuée
     */
	public boolean updateAgenceBox(List<SuiviBoxAgence> listeBox);

}