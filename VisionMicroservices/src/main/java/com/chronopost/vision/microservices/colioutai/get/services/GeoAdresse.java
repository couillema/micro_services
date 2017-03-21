package com.chronopost.vision.microservices.colioutai.get.services;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Pattern en accès direct car limité au package j'assume, c'est un pattern
 * issue des devs récents JS et iOS. si ça ne plait pas, je crée les getters /
 * setters
 * 
 * @author vdesaintpern
 *
 */
public class GeoAdresse {

	public String nom1;

	public String nom2;

	public String adresse1;

	public String adresse2;

	public String cp;

	public String ville;

	public GeoAdresse() {
		super();
	}
	
	public GeoAdresse(String nom1, String nom2, String adresse1, String adresse2,
			String cp, String ville) {
		
		this.nom1 = nom1;
		this.nom2 = nom2;
		this.adresse1 = adresse1;
		this.adresse2 = adresse2;
		this.cp = cp;
		this.ville = ville;
	}

	@Override
	public String toString() {
		return "[geoAdresse : " + nom1 + " " + nom2 + " " + adresse1 + " - " + adresse2 + " - " + cp + " - " + ville
				+ "]";
	}

	/**
	 * Nettoyage d'une adresse
	 * 
	 * @param lt
	 * @return
	 */
	public static String parseAddress(GeoAdresse geoAdresse) {

		String parsedAddress = "";

		String address1 = geoAdresse.adresse1;
		String address2 = geoAdresse.adresse2;
		String cp = geoAdresse.cp;
		String city = geoAdresse.ville;

		if (address1 != null) {
			
			if(address1.toLowerCase().contains("digicode")) {
				address1 = address1.substring(0, address1.toLowerCase().indexOf("digicode"));
			}
			
			if(!address1.trim().equals("")) {
				parsedAddress += address1 + "<br>";
			}
		}

		if (address2 != null && !address2.trim().equals("")) {
			
			if(address2.toLowerCase().contains("vt=")) {
				address2 = address2.substring(0, address2.toLowerCase().indexOf("vt="));
			}
			
			if(address2.toLowerCase().contains("vtri")) {
				address2 = address2.substring(0, address2.toLowerCase().indexOf("vtri"));
			}
			
			if(!address2.trim().equals("")) {
				parsedAddress += address2.trim() + "<br>";
			}
		}

		if (cp != null) {
			parsedAddress += cp + " ";
		}

		if (city != null) {
			parsedAddress += city;
		}

		return parsedAddress;
	}

	@Override
	public boolean equals(Object obj) {
		
		if(obj != null && obj instanceof GeoAdresse) {
			
			GeoAdresse geoAdresse = (GeoAdresse) obj;
			
			return new EqualsBuilder()
					.append(this.nom1, geoAdresse.nom1)
					.append(this.nom2, geoAdresse.nom2)
					.append(this.adresse1, geoAdresse.adresse1)
					.append(this.adresse2, geoAdresse.adresse2)
					.append(this.cp, geoAdresse.cp)
					.append(this.ville, geoAdresse.ville)
					.isEquals();
		}
		
		return false;
	}
	
	
}
