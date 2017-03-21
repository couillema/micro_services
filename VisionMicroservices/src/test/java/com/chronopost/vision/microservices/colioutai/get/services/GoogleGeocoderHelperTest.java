package com.chronopost.vision.microservices.colioutai.get.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Position;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

/**
 * Vérifie qu'on se mélange pas les pinceaux en appelant google maps
 * @author vdesaintpern
 *
 */
public class GoogleGeocoderHelperTest {

	protected Geocoder mockGeocoder = Mockito.mock(Geocoder.class);
	
	@Test
	public void testGeocoderGoogle() throws Exception {
		
		GoogleGeocoderHelper googleGeocoderHelper = GoogleGeocoderHelper.getInstance(null, null, 10000);
		
		GeoAdresse geoAdresse = new GeoAdresse("vivien","de saint pern", "41 rue bernard palissy", null, "92500", "Rueil-Malmaison");
		
		GoogleGeocoderHelper.geocoder = mockGeocoder;
		
		GeocoderRequest geocoderRequestExpected = new GeocoderRequest(GeoAdresse.parseAddress(geoAdresse), "fr");
		GeocodeResponse geocodeResponse = new GeocodeResponse();
		
		GeocoderResult r1 = new GeocoderResult();
		LatLng l1 = new LatLng(new BigDecimal("1.345"), new BigDecimal("2.564"));
		GeocoderGeometry g1 = new GeocoderGeometry();
		g1.setLocation(l1);
		r1.setGeometry(g1);
		
		GeocoderResult r2 = new GeocoderResult();
		LatLng l2 = new LatLng(new BigDecimal("10.345"), new BigDecimal("20.564"));
		GeocoderGeometry g2 = new GeocoderGeometry();
		g2.setLocation(l2);
		r2.setGeometry(g2);
		
		GeocoderResult r3 = new GeocoderResult();
		LatLng l3 = new LatLng(new BigDecimal("90.345"), new BigDecimal("80.564"));
		GeocoderGeometry g3 = new GeocoderGeometry();
		g3.setLocation(l3);
		r3.setGeometry(g3);
		
		List<GeocoderResult> results = Arrays.asList(r1, r2, r3);
		geocodeResponse.setResults(results);
		
		Mockito.when(mockGeocoder.geocode(geocoderRequestExpected)).thenReturn(geocodeResponse);
		
		Position p = googleGeocoderHelper.geocodeFrom(geoAdresse);
	
		assertNotNull(p);
		assertEquals(p.getLati(), 1.345d, 0.000001d);
		assertEquals(p.getLongi(), 2.564d, 0.000001d);
		
		Mockito.when(mockGeocoder.geocode(geocoderRequestExpected)).thenReturn(null);
		
		assertNull(googleGeocoderHelper.geocodeFrom(geoAdresse));
	}
}
