package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.chronopost.vision.model.PositionGps;

public class Tournee {

    private String agence;

    /**
     * idC11. fuck sonar :)
     */
    private String idC11;

    /**
     * codeTournee. fuck sonar :)
     */
    private String codeTournee;

    /**
     * dateTournee. fuck sonar :)
     */
    private Date dateTournee;

    /**
     * ltsDeLaTournee. fuck sonar :)
     */
    private List<String> ltsDeLaTournee;

    /**
     * relevesGps. fuck sonar :)
     */
    private List<PositionGps> relevesGps;

    /**
     * informations de la tournee.
     */
    private Map<String, String> informations;

    /**
     * Javadoc de haute voltige : Constructeur.
     */
    public Tournee() {

    }

    /**
     * getter idC11
     * 
     * @return
     */
    public String getIdC11() {
        return idC11;
    }

    /**
     * setter idC11
     * 
     * @return
     */
    public void setIdC11(String idC11) {
        this.idC11 = idC11;
    }

    /**
     * getter codeTournee
     * 
     * @return
     */
    public String getCodeTournee() {
        return codeTournee;
    }

    /**
     * setter codeTournee
     * 
     * @return
     */
    public void setCodeTournee(String codeTournee) {
        this.codeTournee = codeTournee;
    }

    /**
     * getter dateTournee
     * 
     * @return
     */
    public Date getDateTournee() {
        return dateTournee;
    }

    /**
     * getter idC11
     * 
     * @return
     */
    public void setDateTournee(Date dateTournee) {
        this.dateTournee = dateTournee;
    }

    /**
     * setter lts de la tournee.
     * 
     * @return
     */
    public List<String> getLtsDeLaTournee() {
        return ltsDeLaTournee;
    }

    /**
     * getter lts de la tournee.
     * 
     * @return
     */
    public void setLtsDeLaTournee(List<String> ltsDeLaTournee) {
        this.ltsDeLaTournee = ltsDeLaTournee;
    }

    /**
     * getter des relevés GPS.
     * 
     * @return
     */
    public List<PositionGps> getRelevesGps() {
        return relevesGps;
    }

    /**
     * setter des relevés GPS.
     * 
     * @return
     */
    public void setRelevesGps(List<PositionGps> relevesGps) {
        this.relevesGps = relevesGps;
    }

    /**
     * getter des informations.
     * 
     * @return
     */
    public Map<String, String> getInformations() {
        return informations;
    }

    /**
     * setter des informations.
     * 
     * @return
     */
    public void setInformations(Map<String, String> informations) {
        this.informations = informations;
    }

    /**
     * @return the agence
     */
    public String getAgence() {
        return agence;
    }

    /**
     * @param agence
     *            the agence to set
     */
    public void setAgence(String agence) {
        this.agence = agence;
    }
}
