package com.chronopost.vision.microservices.featureflips;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.chronopost.vision.model.featureflips.GetFeatureFlipOutput;

public interface IFeatureFlipsService {
	
	/**
	 * Renvoie une page HTML listant les flips.
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public String getFeatureFlipsHtml(String baseUrl) throws FileNotFoundException, IOException;
	
	/**
	 * Mise à jour d'un feature flip.
	 * 
	 * @param nomFlip
	 * @param valeurFlip
	 * @return
	 */
	public boolean updateFeatureFlip(String nomFlip, String valeurFlip);

	   /**
     * valeur d'un feature flip
     * 
     * @param nomFlip
     * @param valeurFlip
     * @return la valeur actuelle du feature flip 
     */
    public GetFeatureFlipOutput getFeatureFlip(String nomFlip);
    
    
    /**
     * Suppression d'un flip de la base.
     * 
     * @param nomFlip
     * @param valeurFlip
     * @return
     */
    public boolean deleteFeatureFlip(String nomFlip);
    

}
