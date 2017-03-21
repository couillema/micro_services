package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

public class GetCodeTourneeFromLTException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2824531160368599378L;

    private String codeErreur;

    public final static String TOURNEE_NOT_FOUND = "TOURNEE_NOT_FOUND";
    public final static String LT_NOT_FOUND = "LT_NOT_FOUND";

    public GetCodeTourneeFromLTException(String codeErreur) {
        super();

        this.codeErreur = codeErreur;
    }

    public String getCodeErreur() {
        return codeErreur;
    }

}
