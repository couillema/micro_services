package com.chronopost.vision.microservices.colioutai.get.v2.services;

import com.chronopost.vision.model.Position;

public interface PTVHelperInterface {

	public String heureArrivee(Position fromPosition, String heureFrom, int tempsStop, Position toPosition)
			throws PositionNotFoundException;
}
