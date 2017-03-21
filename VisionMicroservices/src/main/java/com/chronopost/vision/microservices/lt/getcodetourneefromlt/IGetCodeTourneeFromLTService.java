package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import java.util.Date;

import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;

public interface IGetCodeTourneeFromLTService {

	public GetCodeTourneeFromLTResponse findTourneeBy(String noLT, Date dateHeureSearch) throws GetCodeTourneeFromLTException;
//	public IGetCodeTourneeFromLTService setGetLtV1Endpoint(String getLtV1Endpoint);
}
