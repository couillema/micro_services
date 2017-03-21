package com.chronopost.vision.microservices.traitementRetard;

import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;

public interface ITraitementRetardDao {

	/**
	 * Mémorise dans la table <dateLivraisonEstimeeLt> les retards fournis par le WS CalculRetard
	 * 
	 * @param retards: la liste des <TraitementRetardInput> avec (LT + Resultat du WSCalculRetard)
	 * @return : boolean true=ok
	 * @throws FunctionalException : Si la date estimée n'est pas au format dd/mm/yyyy
	 * 
	 */
	abstract boolean insertDLE(List<TraitementRetardWork> retards) throws FunctionalException;

	/**
	 * Pour chaque TraitementRetardInput 
	 * génère une TraitementRetardWork en positionnant la valeur de maxDLE à partir de la base.
	 * 
	 * @param retards : 
	 * @return true si les dates ont été positionnées, false sinon.
	 */
	abstract List<TraitementRetardWork> selectMaxDLE(List<TraitementRetardInput> retards);
}