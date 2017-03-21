package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.concurrent.ExecutionException;


/**
 * Dao du Microservice GetSyntheseTournees
 * @author jcbontemps
 *
 */
public interface SyntheseTourneeDao {
    /**
     * @param idTournee id d'une tournée
     * @return la tournée
     * @throws InterruptedException, ExecutionException 
     */
    Tournee getPointsTournee(String idTournee) throws InterruptedException,ExecutionException;
    
	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * @param nbTrt
	 * @param nbFail
	 */
	void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	void updateCptHitMS();

	void updateCptFailMS();
}
