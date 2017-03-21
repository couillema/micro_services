package com.chronopost.vision.microservices.utils;

public enum TypeBorneCreneau {
	 BORNE_INF 			("min"), 
	 BORNE_SUP 			("max")
	;
	
	private final String typeBorne;
	
	private TypeBorneCreneau(String typeBorne) {
		this.typeBorne = typeBorne;
	}
	
	public String getTypeBorne(){
		return this.typeBorne;
	}
	
}
