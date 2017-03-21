package com.chronopost.vision.microservices.colioutai.get.services;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Position;

import fr.chronopost.poi.webservice.AdresseNormeDto;
import fr.chronopost.poi.webservice.AnnonceResult;
import fr.chronopost.poi.webservice.PoiService;

public class PoiGeocoderHelperTest {

	PoiService mockPoiService = Mockito.mock(PoiService.class);
	
	@Test
	public void testPoiGeocoderHelperTest() throws Exception {
		
		GeoAdresse geoAdresse = new GeoAdresse("vivien", "de saint pern", "41 rue bernard palissy", null, "92500", "Rueil-Malmaison");
		
		AdresseNormeDto adresseDto = new AdresseNormeDto();
		adresseDto.setAdrLatitudeCalculee("1.123456");
		adresseDto.setAdrLongitudeCalculee("3.123456");
		
		AnnonceResult annonceResult = new AnnonceResult();
		annonceResult.setIdAdresse(123456789l);
		
		Mockito.when(mockPoiService.annoncePOI("vivien", "de saint pern", "41 rue bernard palissy", null, "92500", "Rueil-Malmaison", ""))
		.thenReturn(annonceResult);
		
		Mockito.when(mockPoiService.findAdresseById(123456789l)).thenReturn(adresseDto);
		
		Position p = PoiGeocoderHelper.getInstance(mockPoiService).geocodeFrom(geoAdresse);
		
		assertEquals(p.getLati(), 1.123456d, 0.0000001d);
		assertEquals(p.getLongi(), 3.123456d, 0.0000001d);
		
	}
	
}
