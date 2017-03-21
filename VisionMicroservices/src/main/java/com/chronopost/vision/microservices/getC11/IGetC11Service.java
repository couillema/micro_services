package com.chronopost.vision.microservices.getC11;

import java.util.List;

import com.chronopost.vision.model.insertC11.TourneeC11;

public interface IGetC11Service {

	List<TourneeC11> getTournees(String posteComptable, String jour) throws Exception;

	void setDao(IGetC11Dao getC11Dao);

}
