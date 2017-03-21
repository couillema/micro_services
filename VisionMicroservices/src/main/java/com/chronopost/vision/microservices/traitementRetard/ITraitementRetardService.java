package com.chronopost.vision.microservices.traitementRetard;

import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;

public interface ITraitementRetardService {

	/**
	 * Pour chaque LT fournie:
	 * <UL>
	 * <LI>Sauve en base la DLE calculée</LI>
	 * </UL>
	 * @param retards: La liste des retards (LT + Retour WSCalculRetard)
	 * @return true si les inserts se sont tous bien déroulés
	 * @throws FunctionalException 
	 * 
	 */
	abstract boolean memoriseDLE(List<TraitementRetardWork> retards) throws FunctionalException;
	
	/**
	 * Pour chaque LT fournie:
	 * <UL>
	 * <LI>Récupère en base la DLE maximale et la positionne dans le TraitementRetardInput</LI>
	 * </UL>
	 * @param retards: La liste des retards (LT + Retour WSCalculRetard = TraitementRetardInput)
	 * @return La liste des retards avec les maxDLE positionnée (TraitementRetardWork)
	 */
	abstract List<TraitementRetardWork> extractMaxDLE(List<TraitementRetardInput> retards);
	
	
	/**
	 * Positionnement de la DAO pour le service
	 * @param pDao
	 */
	ITraitementRetardService setDao(ITraitementRetardDao pDao);
	
	/**
	 * Pour chaque LT fournie:
	 * <UL>
	 * 	<LI>Compare le retard fournit DLE avec le retard maximum précédement estimé max(DLEs).</LI>
	 * <LI>Si DLE>max(DLEs) et que les dates n'indiquent plus le même jour, alors génération d'un evt RD</LI>
	 * </UL>
	 * 
	 * 
	 * 
	 * @param retards : liste des Lts et  résultat d'invocation du calcul DLE à considérer
	 * @return true si tout s'est déroulé correctement, false si un traitement au moins a été problématique.
	 */
	abstract boolean genereRD(List<TraitementRetardWork> retards);
}