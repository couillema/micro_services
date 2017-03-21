package com.chronopost.vision.microservices.supervision;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.model.supervision.SnapShotVision;

public interface ISupervisionDao {

	List<SnapShotVision> getSnapShotVisionForDay(Date snapShotDate, Boolean isToday) throws InterruptedException, ExecutionException;

	SnapShotVision getSnapShotByKey(String jour, String heure, String minute);

	boolean checkSessionActive() throws Exception;
	
	void insertMSAppelsInfos(String microService, Date date, String url, Long duree, String userAgent);

}
