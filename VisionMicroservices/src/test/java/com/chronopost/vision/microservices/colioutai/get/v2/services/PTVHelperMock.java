package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.text.ParseException;
import java.util.Date;

import com.chronopost.vision.microservices.colioutai.get.v2.services.PTVHelperInterface;
import com.chronopost.vision.microservices.colioutai.get.v2.services.PositionNotFoundException;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.rules.DateRules;

public class PTVHelperMock implements PTVHelperInterface {

	public String heureArrivee(Position fromPosition, String heureFrom, int tempsStop, Position toPosition)
			throws PositionNotFoundException {

		// on mocke
		int value = 0;
		try {
			value = Math.max(Math.abs((int) ((fromPosition.getLati() - 45.0d) + (fromPosition.getLongi() - 45.0)
					+ (toPosition.getLati() - 45.0d) + (toPosition.getLongi() - 45.0))), 20);
		} catch (Exception e) {
			value = 5;
		}

		try {
			return DateRules.toTime(new Date(DateRules.toTodayTime(heureFrom).getTime() + (1000 * 60 * value)));
		} catch (ParseException e) {
			return "erreur mock";
		}

	}
}
