package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.util.Date;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.DetailTournee;

public interface IGetDetailTourneeService {
	
	public IGetDetailTourneeService setDao(IGetDetailTourneeDao dao);
	public DetailTournee getDetailTournee(Date dateTournee, String codeTournee, GetLtV1 getLtV1) throws Exception;
}
