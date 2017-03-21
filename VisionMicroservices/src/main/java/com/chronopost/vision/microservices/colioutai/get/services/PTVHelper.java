package com.chronopost.vision.microservices.colioutai.get.services;

import java.util.Arrays;

import com.chronopost.vision.model.Position;
import com.ptvgroup.chronopost.ws.xchrono.Optimisation;
import com.ptvgroup.chronopost.ws.xchrono.OptimisationResponse;
import com.ptvgroup.chronopost.ws.xchrono.XChronoWS;
import com.ptvgroup.chronopost.ws.xchrono.service.data.Address;
import com.ptvgroup.chronopost.ws.xchrono.service.data.ArrayOfPoint;
import com.ptvgroup.chronopost.ws.xchrono.service.data.Point;
import com.ptvgroup.chronopost.ws.xchrono.service.data.Priority;
import com.ptvgroup.chronopost.ws.xchrono.service.data.Time;
import com.ptvgroup.chronopost.ws.xchrono.service.data.TimeSlot;
import com.ptvgroup.chronopost.ws.xchrono.service.data.TimeSpan;
import com.ptvgroup.chronopost.ws.xchrono.service.data.TourParams;

public class PTVHelper implements PTVHelperInterface {

	private static XChronoWS servicePTV;

	private PTVHelper() {
	}

	private static class InstanceHolder {
		static PTVHelper instance = new PTVHelper();
	}

	public static PTVHelper getInstance(final XChronoWS servicePTV) {
		PTVHelper.servicePTV = servicePTV;
		return InstanceHolder.instance;
	}

	/**
	 * 
	 * @param geoAdresse
	 * @return
	 * @throws PositionNotFoundException
	 */
	public String heureArrivee(final Position fromPosition, final String heureFrom, final int tempsStop, final Position toPosition)
			throws PositionNotFoundException {

		final Optimisation optimisationParameters = new Optimisation();

		final Time heureDepart = new Time();
		heureDepart.setTime(heureFrom+":00");
		optimisationParameters.setHeureDepart(heureDepart);
//		TimeSlot timeSlot = new TimeSlot();
//		timeSlot.setDebut("00:00:00")
//		optimisationParameters.setPause(ti);

		final ArrayOfPoint points = new ArrayOfPoint();

		final Point fromPoint = new Point();
		fromPoint.setId("0");
		final Address adresse1 = new Address();
		adresse1.setGeocodage(false);
		fromPoint.setAdresse(adresse1);
		fromPoint.setPointDepart(true);
		fromPoint.setLat(fromPosition.getLati());
		fromPoint.setLong(fromPosition.getLongi());
		fromPoint.setArret(new TimeSpan());
		final Priority p = new Priority();
		p.setId(0);
		fromPoint.setPriorite(p);

		final Point toPoint = new Point();
		toPoint.setAdresse(adresse1);
		toPoint.setId("1");
		toPoint.setPointArrivee(true);
		toPoint.setLat(toPosition.getLati());
		toPoint.setLong(toPosition.getLongi());
		toPoint.setArret(new TimeSpan());
		final Priority p2 = new Priority();
		p2.setId(1);
		toPoint.setPriorite(p2);

		points.getPoint().addAll(Arrays.asList(fromPoint, toPoint));
		optimisationParameters.setListePoints(points);
		
		optimisationParameters.setTournee(new TourParams());
		optimisationParameters.getTournee().setId("1234");
		optimisationParameters.getTournee().setETAbrut(true);
		optimisationParameters.getTournee().setETAoptim(false);
		optimisationParameters.getTournee().setXRouteOptim(false);
		optimisationParameters.getTournee().setPonderationDeplacement(1.15d);
		optimisationParameters.getTournee().setProfil("V-ESSENCE");
		optimisationParameters.getTournee().setPeages(false);

		optimisationParameters.setPause(new TimeSlot());
		optimisationParameters.getPause().setDebut("00:00:00");
		optimisationParameters.getPause().setFin("00:00:00");
		
		final OptimisationResponse response = servicePTV.optimisation(optimisationParameters);

		if(response.getOptimisation().getTournee().getCodeErreur() == 0) {
			final String etaCalcul = response.getOptimisation().getParcoursBrut().getHeureArrivee();
			
			return etaCalcul.substring(0, 5);
		} else {
			throw new PositionNotFoundException("calcul entre les points impossible"); 
		}
	}
}
