package com.chronopost.vision.microservices.colioutai.get.services;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class GeoAdresseTest {

	@Test
	public void testParseAdress() {

		GeoAdresse adresse = new GeoAdresse();

		adresse.adresse1 = "5 place de rungis";
		adresse.adresse2 = "Vtri=1234";
		adresse.cp = "75013";
		adresse.ville = "Paris";

		String parsedAdress = GeoAdresse.parseAddress(adresse);

		assertEquals(parsedAdress, "5 place de rungis<br>75013 Paris");
	}
}
