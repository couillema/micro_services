package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

/**
 * Classe utilitaire utile pour gérer les idPointC11 des tournées avec leurs éléments
 * (facilite l'écriture des tests unitaires)
 * @author jcbontemps
 */
public class IdPointC11 {

    private String agence ;
    
    private String numPoint ;
    
    private String idC11 ;

    /**
     * Constructeur
     * @param agence l'agence de la tournée
     * @param numPoint le numéro de point de la tournée 
     * @param idC11 l'idC11 de la tournée
     */
    public IdPointC11(String agence, String numPoint, String idC11) {
        this.agence = agence ;
        this.numPoint = numPoint ;
        this.idC11 = idC11 ;
    }

    /**
     * Constructeur
     * Utile pour extraire les données de la tournée à partir de l'idPointC11
     * @param idPointC11 l'idPointC11 de la tournée
     */
    public IdPointC11(String idPointC11) {
        this.agence = idPointC11.substring(0,3) ;
        this.numPoint = idPointC11.substring(8,11) ;
        this.idC11 = idPointC11.substring(3,8) + idPointC11.substring(11);
    }

    /**
     * @return le trigramme agence de cet idPointC11
     */
    public String getAgence() {
        return agence;
    }

    /**
     * @return le numéro de point dans la tournée de cet idPointC11
     */
    public String getNumPoint() {
        return numPoint;
    }

    /**
     * @return l'id C11 de cet idPointC11
     */
    public String getIdC11() {
        return idC11;
    }

    /*
     * Utile pour construire un idPointC11 à partir de ses éléments (agence, numéro du point dans la tournée, idC11) 
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return agence + idC11.substring(0,5) + numPoint + idC11.substring(5) ;
    }
}

