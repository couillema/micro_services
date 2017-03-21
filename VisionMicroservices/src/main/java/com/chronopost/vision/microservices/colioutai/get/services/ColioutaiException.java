package com.chronopost.vision.microservices.colioutai.get.services;

public class ColioutaiException extends Exception {

	private static final long serialVersionUID = 1334338271447293651L;

	private String codeErreur;

	public final static String LT_NOT_FOUND = "LT_NOT_FOUND";

	public ColioutaiException(final String codeErreur) {
		super();

		this.codeErreur = codeErreur;
	}

	public String getCodeErreur() {
		return codeErreur;
	}

}
