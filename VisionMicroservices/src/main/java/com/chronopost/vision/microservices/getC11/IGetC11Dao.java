package com.chronopost.vision.microservices.getC11;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.TourneeC11;

public interface IGetC11Dao {

	List<String> getIdxTourneesByAgenceAndJour(String posteComptable, String jour) throws Exception;

	List<TourneeC11> getTourneesById(List<String> tourneesId) throws InterruptedException, ExecutionException;

	List<PointC11> getPointsForTourneeId(List<String> idsC11) throws InterruptedException, ExecutionException;

}
