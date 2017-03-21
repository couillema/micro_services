package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import java.util.Date;

import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;

/**
 * Classe de DAO du getTournee
 * @author vdesaintpern
 *
 */
public interface IGetCodeTourneeFromLTDAO {

	/**
	 * Recherche d'informations basiques sur la tournée à partir
	 * @param noLT
	 * @param dateHeureSearch
	 * @return
	 */
	public GetCodeTourneeFromLTResponse findTourneeBy(String noLT, Date dateHeureSearch);
	
}
