package com.chronopost.vision.microservices.transcos;

import com.chronopost.vision.model.transco.GetTranscoOutput;

public interface ITranscoService {

	/**
	 * Mise à jour d'un feature flip.
	 * 
	 * @param nomFlip
	 * @param valeurFlip
	 * @return
	 */
    public boolean updateTransco(String projet, String famille, String nom, String valeur);

	   /**
     * valeur d'un feature flip
     * 
     * @param nomFlip
     * @param valeurFlip
     * @return la valeur actuelle du feature flip 
     */
    public GetTranscoOutput getTransco(String projet, String famille, String nom);

}
