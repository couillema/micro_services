package com.chronopost.vision.microservices.getEvts;

import java.util.List;

import com.chronopost.vision.model.Evt;

public interface IGetEvtsDao {

	List<Evt> getLtEvts(String noLt) throws Exception;

}
