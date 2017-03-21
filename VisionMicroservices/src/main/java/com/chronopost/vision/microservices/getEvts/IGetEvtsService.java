package com.chronopost.vision.microservices.getEvts;

import java.util.List;

import com.chronopost.vision.model.Evt;

public interface IGetEvtsService {

	List<Evt> getEvts(String noLt) throws Exception;

	void setDao(IGetEvtsDao getEvtsDao);

}
