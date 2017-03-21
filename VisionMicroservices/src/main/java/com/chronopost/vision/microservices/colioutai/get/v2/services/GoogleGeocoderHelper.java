package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.Position;
import com.google.code.geocoder.AdvancedGeoCoder;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;

/**
 * Classe ayant pour but de geocoder en dernier recours dans le process.
 * 
 * @author vdesaintpern
 *
 */
public class GoogleGeocoderHelper {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(GoogleGeocoderHelper.class);

	/**
	 * Geocoder google
	 */
	static Geocoder geocoder = null;

	public static String proxyURL;

	public static String proxyPORT;

	public static Integer proxyTimeout;


	private GoogleGeocoderHelper() {

		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		try {
			httpClient.getHostConfiguration().setProxy(proxyURL,Integer.parseInt(proxyPORT));
			if(proxyTimeout != null){
				httpClient.getParams().setConnectionManagerTimeout(proxyTimeout.intValue() * 1000);
				httpClient.getParams().setSoTimeout(proxyTimeout.intValue() * 1000);				
			}

		} catch (Exception e) {
			logger.warn("Proxy invalide - pas de proxy utilisé "
					+ proxyURL + " port " + proxyPORT);
		}		

		geocoder = new AdvancedGeoCoder(httpClient);

	}

	private static class InstanceHolder {

		public static GoogleGeocoderHelper helper = new GoogleGeocoderHelper();

	}



	public static GoogleGeocoderHelper getInstance(String proxyURL, String proxyPORT, Integer proxyTimeout) {
		GoogleGeocoderHelper.proxyURL = proxyURL;
		GoogleGeocoderHelper.proxyPORT = proxyPORT;
		GoogleGeocoderHelper.proxyTimeout = proxyTimeout;
		return InstanceHolder.helper;
	}

	/**
	 * Retourne une position pour une LT donnée
	 * @param lt
	 * @return
	 */
	public Position geocodeFrom(GeoAdresse adresse) {

		Position newPosition = null;

		String parsedAddress = GeoAdresse.parseAddress(adresse);

		GeocodeResponse geocoderResponse = null;

		if(parsedAddress != null && parsedAddress.length() > 0){

			GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(parsedAddress)
					.setLanguage("fr").getGeocoderRequest();

			try {
				geocoderResponse = geocoder.geocode(geocoderRequest);
			} catch (IOException e) {
				logger.warn("geocoding failed " + adresse, e);
				return null;
			}

		}



		if (geocoderResponse != null && geocoderResponse.getResults() != null
				&& geocoderResponse.getResults().size() > 0) {

			newPosition = new Position();
			
			GeocoderResult result = geocoderResponse.getResults().get(0);
			newPosition.setLati(result.getGeometry().getLocation().getLat()
					.doubleValue());
			newPosition.setLongi(result.getGeometry().getLocation().getLng()
					.doubleValue());

			logger.info("fixed Lati/Longi with GOOGLE " + parsedAddress
					+ " - adresse " + adresse + " " + newPosition.getLati()
					+ "/" + newPosition.getLongi());

		} else {
			logger.warn("pas de reponse de google pour "
					+ adresse + " adresse " + parsedAddress);
		}

		return newPosition;
	}
}
