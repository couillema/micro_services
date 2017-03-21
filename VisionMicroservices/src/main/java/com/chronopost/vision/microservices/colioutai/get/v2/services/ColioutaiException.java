package com.chronopost.vision.microservices.colioutai.get.v2.services;

public class ColioutaiException extends Exception {

	private static final long serialVersionUID = -4696004051061584275L;

	private String codeErreur;

	public final static String LT_NOT_FOUND = "LT_NOT_FOUND";

	public ColioutaiException(String codeErreur) {
		super();

		this.codeErreur = codeErreur;
	}

	public String getCodeErreur() {
		return codeErreur;
	}

}
