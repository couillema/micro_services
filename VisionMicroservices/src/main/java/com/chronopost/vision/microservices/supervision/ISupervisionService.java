package com.chronopost.vision.microservices.supervision;

import java.util.List;

import com.chronopost.vision.model.supervision.SnapShotVision;

public interface ISupervisionService {

	List<SnapShotVision> getSnapShotsVisionForADay(String jour) throws Exception;

	void setDao(ISupervisionDao dao);

	SnapShotVision getSnapShotVisionForLast10Minutes() throws Exception;

	SnapShotVision getSnapShotsAverageForADay(String jour) throws Exception;

	boolean getMSStatus() throws Exception;

}
