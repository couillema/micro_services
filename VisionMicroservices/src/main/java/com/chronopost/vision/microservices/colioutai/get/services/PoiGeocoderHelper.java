package com.chronopost.vision.microservices.colioutai.get.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.Position;

import fr.chronopost.poi.webservice.AdresseNormeDto;
import fr.chronopost.poi.webservice.AnnonceResult;
import fr.chronopost.poi.webservice.PoiService;
import fr.chronopost.poi.webservice.ServiceException_Exception;

public class PoiGeocoderHelper {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(PoiGeocoderHelper.class);

	private static PoiService servicePOI;

	private PoiGeocoderHelper() {
	}

	private static class InstanceHolder {
		static PoiGeocoderHelper instance = new PoiGeocoderHelper();
	}

	public static PoiGeocoderHelper getInstance(PoiService servicePOI) {
		PoiGeocoderHelper.servicePOI = servicePOI;
		return InstanceHolder.instance;
	}

	/**
	 * 
	 * @param geoAdresse
	 * @return
	 * @throws PositionNotFoundException
	 */
	public Position geocodeFrom(GeoAdresse geoAdresse) throws PositionNotFoundException {

		Position newPosition = new Position();

		AnnonceResult result;
		Long idAdr = null;

		try {
			
			result = servicePOI.annoncePOI(geoAdresse.nom1, geoAdresse.nom2, geoAdresse.adresse1, geoAdresse.adresse2,
					geoAdresse.cp, geoAdresse.ville, "");

			idAdr = result.getIdAdresse();

		} catch (ServiceException_Exception e) {
			throw new PositionNotFoundException(e.getMessage());
		}

		if (idAdr != null) {

			AdresseNormeDto adresse;
			try {
				adresse = servicePOI.findAdresseById(idAdr);
			} catch (ServiceException_Exception e) {
				throw new PositionNotFoundException(e.getMessage());

			}

			if (adresse != null) {
				String latCalc = adresse.getAdrLatitudeCalculee();
				String latReel = adresse.getAdrLatitudeReel();
				String longCalc = adresse.getAdrLongitudeCalculee();
				String longReel = adresse.getAdrLongitudeReel();

				if (latCalc != null && latCalc.length() > 0 && longCalc != null && longCalc.length() > 0) {
					logger.info("fixed Lati/Longi with POI " + " - geoadresse " + geoAdresse + " latCalc: " + latCalc
							+ " latReel: " + latReel + " longCalc: " + longCalc + " longReel: " + longReel);
					newPosition.setLati(Double.parseDouble(latCalc));
					newPosition.setLongi(Double.parseDouble(longCalc));
				}
			}

		}

		if (newPosition.getLati() == 0 || newPosition.getLongi() == 0) {
			throw new PositionNotFoundException("NOT FOUND");
		}
		
		return newPosition;
	}

}
