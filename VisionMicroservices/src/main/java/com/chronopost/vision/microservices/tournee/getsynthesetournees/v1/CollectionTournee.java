package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Statistiques d'une tournée
 * 
 * @author jcbontemps
 */
public class CollectionTournee {

    /* Liste des points ayant eu une TA sur la tournée */
    private final Set<String> pointsPrepares = new HashSet<>();
    /* Liste des colis ayant eu une TA sur la tournée */
    private final Set<String> colisPrepares = new HashSet<>();
    /* Liste des points ayant reçu un evt D+ */
    private final Set<String> pointsTraites = new HashSet<>();
    /* Liste des colis ayant reçus un evt D+ */
    private final Set<String> colisTraites = new HashSet<>();
    /* Liste des colis ayant reçus un evt de présentation positif */
    private final Set<String> colisDeposes = new HashSet<>();
    /* Liste des colis ayant reçus un evt de présentation infructueux */
    private final Set<String> colisEchecPresentation = new HashSet<>();
    /*
     * Liste des points comptant au moins un evt IP ou RB avec présentation
     * nécessaire
     */
    private final Set<String> pointsMisADispoBureauAvecPresNecessaire = new HashSet<>();
    /*
     * Liste des points comptant au moins un evt IP ou RB sans présentation
     * nécessaire
     */
    private final Set<String> pointsMisADispoBureauSansPresNecessaire = new HashSet<>();
    /* Liste des points comptant au moins un evt IP ou RB */
    private final Set<String> colisMisADispoBureau = new HashSet<>();
    /* Liste des colis sécurisés */
    private final Set<String> colisSensibles = new HashSet<>();
    /*
     * Liste des colis dont la date du premier événement D+ est hors date
     * contractuelle (voir s’il faut descendre à l’heure)
     */
    private final Set<String> colisHorsDateContractuelle = new HashSet<>();
    /*
     * Liste des points dont un événement au moins est la date du premier
     * événement D+ du colis et est hors date contractuelle (voir s’il faut
     * descendre à l’heure)
     */
    private final Set<String> pointsHorsDateContractuelle = new HashSet<>();
    /* Liste des colis dont le diffETA du premier événement D+ du colis est > 30 */
    private final Set<String> colisHorsETA = new HashSet<>();
    /*
     * Liste des points dont le diffETA du premier événement D+ du colis est >
     * 30
     */
    private final Set<String> pointsHorsETA = new HashSet<>();
    /* Liste des colis dont le diffETA est connu */
    private final Set<String> colisAvecETA = new HashSet<>();
    /*
     * Liste des colis ayant la spécificité SWAP et ayant eu un événement D sur
     * la tournée
     */
    private final Set<String> colisAnoSpecifSWAP = new HashSet<>();
    /*
     * Liste des colis ayant la spécificité REP et ayant eu un événement D sur
     * la tournée
     */
    private final Set<String> colisAnoSpecifREP = new HashSet<>();
    /*
     * Liste des colis ayant la spécificité TAXE et ayant eu un événement D sur
     * la tournée
     */
    private final Set<String> colisAnoSpecifTAXE = new HashSet<>();
    /* Liste des colis ayant une consigne */
    private final Set<String> colisAvecConsigne = new HashSet<>();
    /** Liste des points dont un colis au moins est en echec présentation **/
    private final Set<String> pointsEchecPresentation = new HashSet<>();
    /*
     * MultiMap qui liste les colis de présentations infructueuses par type
     * d’evt. (key=evt/values=noLt)
     */
    private final SetMultimap<String, String> presentationsInfructueuses = HashMultimap.create();
    private final Map<String, Set<String>> presentationsInfructueusesUnmodifiableMap = Multimaps.asMap(Multimaps
            .unmodifiableSetMultimap(presentationsInfructueuses));
    /* Colis ayant un evt contraire à la consigne */
    private final Set<String> colisAnoConsigneNonRespectee = new HashSet<>();
    /* Points dont un evt indique que le colis a une consigne non respectée */
    private final Set<String> pointsAnoConsigneNonRespectee = new HashSet<>();
    /* Colis dont un evt n’est pas un evt autorisé selon le code service */
    private final Set<String> colisAnoEvtNonPermis = new HashSet<>();
    /* Point ayant au moins un colis en anomalie d’evt non permis */
    private final Set<String> pointsAnoEvtNonPermis = new HashSet<>();
    /*
     * Liste des points (IP/RB) dont la présentation nécessaire n’a pas été
     * respectée
     */
    private final Set<String> pointsAnoMadPresNonRespectee = new HashSet<>();
    /* Liste des pont (IP/RB) dont la mise à dispo n’etait pas permise */
    private final Set<String> pointsAnoMadNonPermise = new HashSet<>();
    /* Liste des colis swap pour lesquel aucun colis retour n'a été vu */
    private final Set<String> pointsAnoSWAPDSansP = new HashSet<>();
    /*
     * Liste des colis swap pour lesquels le colis retour porte le numero du
     * colis aller
     */
    private final Set<String> pointsAnoSWAPDEtP = new HashSet<>();

    /*
     * Liste des psm utilisés pendant une tournée
     */
    private final Map<String, Date> psm = new HashMap<>();
    
     /* 
      * Liste de colis qui ont été vus en retour agence
      */
    private Set<String> colisVusEnRetour = new HashSet<>();
    
    /* Liste des points ayant au moins un colis en retour 
    * (dernier evt du colis =  TA ou echec presentation) */
   private Set<String> pointAvecColisRetour = new HashSet<>();
    /**
     * @return la liste des points ayant eu une TA sur la tournée
     */
    public Set<String> getPointsPrepares() {
        return Collections.unmodifiableSet(this.pointsPrepares);
    }

    /**
     * Setter
     * 
     * @param c
     *            une liste de points ayant eu une TA sur la tournée
     */
    public void setPointsPrepares(Collection<String> c) {
        this.clearPointsPrepares();
        this.addAllToPointsPrepares(c);
    }

    /**
     * Ajoute un points à la liste des points ayant eu une TA sur la tournée
     * 
     * @param s
     *            un identifiant point
     * @return true si la liste ne contenait pas déjà le point
     */
    public boolean addToPointsPrepares(String s) {
        return this.pointsPrepares.add(s);
    }

    /**
     * Ajoute des points à la liste des points ayant eu une TA sur la tournée
     * 
     * @param c
     *            une liste de points ayant eu une TA sur la tournée
     * @return true si la liste est modifiée
     */
    public boolean addAllToPointsPrepares(Collection<String> c) {
        return this.pointsPrepares.addAll(c);
    }

    /**
     * Vide la liste des points ayant eu une TA sur la tournée
     */
    public void clearPointsPrepares() {
        this.pointsPrepares.clear();
    }

    /**
     * @return La liste des colis ayant eu une TA sur la tournée
     */
    /**
     * @return
     */
    public Set<String> getColisPrepares() {
        return Collections.unmodifiableSet(this.colisPrepares);
    }

    /**
     * Setter
     * 
     * @param colisPrepares
     *            La liste des colis ayant eu une TA sur la tournée
     */
    public void setColisPrepares(Collection<String> c) {
        this.clearColisPrepares();
        this.addAllToColisPrepares(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisPrepares(String s) {
        return this.colisPrepares.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisPrepares(Collection<String> c) {
        return this.colisPrepares.addAll(c);
    }

    /**
     * 
     */
    public void clearColisPrepares() {
        this.colisPrepares.clear();
    }

    /**
     * @return la liste des points ayant reçu un evt D+
     */
    /**
     * @return
     */
    public Set<String> getPointsTraites() {
        return Collections.unmodifiableSet(this.pointsTraites);
    }

    /**
     * Setter
     * 
     * @param pointsTraites
     *            la liste des points ayant reçu un evt D+
     */
    public void setPointsTraites(Collection<String> c) {
        this.clearPointsTraites();
        this.addAllToPointsTraites(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToPointsTraites(String s) {
        return this.pointsTraites.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToPointsTraites(Collection<String> c) {
        return this.pointsTraites.addAll(c);
    }

    /**
     * 
     */
    public void clearPointsTraites() {
        this.pointsTraites.clear();
    }

    /**
     * 
     * @return la liste des colis ayant reçus un evt D+
     */
    /**
     * @return
     */
    public Set<String> getColisTraites() {
        return Collections.unmodifiableSet(this.colisTraites);
    }

    /**
     * Setter
     * 
     * @param colisTraites
     *            la liste des colis ayant reçus un evt D+
     */
    public void setColisTraites(Collection<String> c) {
        this.clearColisTraites();
        this.addAllToColisTraites(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisTraites(String s) {
        return this.colisTraites.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisTraites(Collection<String> c) {
        return this.colisTraites.addAll(c);
    }

    /**
     * 
     */
    public void clearColisTraites() {
        this.colisTraites.clear();
    }

    /**
     * @return la liste des colis ayant reçus un evt de présentation positif
     */
    /**
     * @return
     */
    public Set<String> getColisDeposes() {
        return Collections.unmodifiableSet(this.colisDeposes);
    }

    /**
     * @param colisDeposes
     *            la liste des colis ayant reçus un evt de présentation positif
     */
    public void setColisDeposes(Collection<String> c) {
        this.clearColisDeposes();
        this.addAllToColisDeposes(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisDeposes(String s) {
        return this.colisDeposes.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisDeposes(Collection<String> c) {
        return this.colisDeposes.addAll(c);
    }

    /**
     * 
     */
    public void clearColisDeposes() {
        this.colisDeposes.clear();
    }

    /**
     * @return la liste des colis ayant reçus un evt de présentation infructueux
     */
    /**
     * @return
     */
    public Set<String> getColisEchecPresentation() {
        return Collections.unmodifiableSet(this.colisEchecPresentation);
    }

    /**
     * @param colisEchecPresentation
     *            la liste des colis ayant reçus un evt de présentation
     *            infructueux
     */
    public void setColisEchecPresentation(Collection<String> c) {
        this.clearColisEchecPresentation();
        this.addAllToColisEchecPresentation(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisEchecPresentation(String s) {
        return this.colisEchecPresentation.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisEchecPresentation(Collection<String> c) {
        return this.colisEchecPresentation.addAll(c);
    }

    /**
     * 
     */
    public void clearColisEchecPresentation() {
        this.colisEchecPresentation.clear();
    }

    /**
     * @return la liste des points comptant au moins un evt IP ou RB avec
     *         présentation nécessaire
     */
    /**
     * @return
     */
    public Set<String> getPointsMisADispoBureauAvecPresNecessaire() {
        return Collections.unmodifiableSet(this.pointsMisADispoBureauAvecPresNecessaire);
    }

    /**
     * @param pointsMisADispoBureauAvecPresNecessaire
     *            la liste des points comptant au moins un evt IP ou RB avec
     *            présentation nécessaire
     */
    public void setPointsMisADispoBureauAvecPresNecessaire(Collection<String> c) {
        this.clearPointsMisADispoBureauAvecPresNecessaire();
        this.addAllToPointsMisADispoBureauAvecPresNecessaire(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToPointsMisADispoBureauAvecPresNecessaire(String s) {
        return this.pointsMisADispoBureauAvecPresNecessaire.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToPointsMisADispoBureauAvecPresNecessaire(Collection<String> c) {
        return this.pointsMisADispoBureauAvecPresNecessaire.addAll(c);
    }

    /**
     * 
     */
    public void clearPointsMisADispoBureauAvecPresNecessaire() {
        this.pointsMisADispoBureauAvecPresNecessaire.clear();
    }

    /**
     * @return la liste des points comptant au moins un evt IP ou RB sans
     *         présentation nécessaire
     */
    /**
     * @return
     */
    public Set<String> getPointsMisADispoBureauSansPresNecessaire() {
        return Collections.unmodifiableSet(this.pointsMisADispoBureauSansPresNecessaire);
    }

    /**
     * @param pointsMisADispoBureauSansPresNecessaire
     *            la liste des points comptant au moins un evt IP ou RB sans
     *            présentation nécessaire
     */
    public void setPointsMisADispoBureauSansPresNecessaire(Collection<String> c) {
        this.clearPointsMisADispoBureauSansPresNecessaire();
        this.addAllToPointsMisADispoBureauSansPresNecessaire(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToPointsMisADispoBureauSansPresNecessaire(String s) {
        return this.pointsMisADispoBureauSansPresNecessaire.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToPointsMisADispoBureauSansPresNecessaire(Collection<String> c) {
        return this.pointsMisADispoBureauSansPresNecessaire.addAll(c);
    }

    /**
     * 
     */
    public void clearPointsMisADispoBureauSansPresNecessaire() {
        this.pointsMisADispoBureauSansPresNecessaire.clear();
    }

    /**
     * @return la liste des points comptant au moins un evt IP ou RB
     */
    /**
     * @return
     */
    public Set<String> getColisMisADispoBureau() {
        return Collections.unmodifiableSet(this.colisMisADispoBureau);
    }

    /**
     * @param pointsMisADispoBureau
     *            la liste des points comptant au moins un evt IP ou RB
     */
    public void setColisMisADispoBureau(Collection<String> c) {
        this.clearColisMisADispoBureau();
        this.addAllToColisMisADispoBureau(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisMisADispoBureau(String s) {
        return this.colisMisADispoBureau.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisMisADispoBureau(Collection<String> c) {
        return this.colisMisADispoBureau.addAll(c);
    }

    /**
     * 
     */
    public void clearColisMisADispoBureau() {
        this.colisMisADispoBureau.clear();
    }

    /**
     * @return la liste des colis sécurisés
     */
    /**
     * @return
     */
    public Set<String> getColisSensibles() {
        return Collections.unmodifiableSet(this.colisSensibles);
    }

    /**
     * @param colisSensibles
     *            la liste des colis sécurisés
     */
    public void setColisSensibles(Collection<String> c) {
        this.clearColisSensibles();
        this.addAllToColisSensibles(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisSensibles(String s) {
        return this.colisSensibles.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisSensibles(Collection<String> c) {
        return this.colisSensibles.addAll(c);
    }

    /**
     * 
     */
    public void clearColisSensibles() {
        this.colisSensibles.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisHorsDateContractuelle() {
        return Collections.unmodifiableSet(this.colisHorsDateContractuelle);
    }

    /**
     * 
     * @param colisHorsDateContractuelle
     */
    public void setColisHorsDateContractuelle(Collection<String> c) {
        this.clearColisHorsDateContractuelle();
        this.addAllToColisHorsDateContractuelle(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisHorsDateContractuelle(String s) {
        return this.colisHorsDateContractuelle.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisHorsDateContractuelle(Collection<String> c) {
        return this.colisHorsDateContractuelle.addAll(c);
    }

    /**
     * 
     */
    public void clearColisHorsDateContractuelle() {
        this.colisHorsDateContractuelle.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getPointsHorsDateContractuelle() {
        return Collections.unmodifiableSet(this.pointsHorsDateContractuelle);
    }

    /**
     * 
     * @param pointsHorsDateContractuelle
     */
    public void setPointsHorsDateContractuelle(Collection<String> c) {
        this.clearPointsHorsDateContractuelle();
        this.addAllToPointsHorsDateContractuelle(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToPointsHorsDateContractuelle(String s) {
        return this.pointsHorsDateContractuelle.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToPointsHorsDateContractuelle(Collection<String> c) {
        return this.pointsHorsDateContractuelle.addAll(c);
    }

    /**
     * 
     */
    public void clearPointsHorsDateContractuelle() {
        this.pointsHorsDateContractuelle.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisHorsETA() {
        return Collections.unmodifiableSet(this.colisHorsETA);
    }

    /**
     * 
     * @param colisHorsETA
     */
    public void setColisHorsETA(Collection<String> c) {
        this.clearColisHorsETA();
        this.addAllToColisHorsETA(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisHorsETA(String s) {
        return this.colisHorsETA.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisHorsETA(Collection<String> c) {
        return this.colisHorsETA.addAll(c);
    }

    /**
     * 
     */
    public void clearColisHorsETA() {
        this.colisHorsETA.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getPointsHorsETA() {
        return Collections.unmodifiableSet(this.pointsHorsETA);
    }

    /**
     * 
     * @param pointsHorsETA
     */
    public void setPointsHorsETA(Collection<String> c) {
        this.clearPointsHorsETA();
        this.addAllToPointsHorsETA(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToPointsHorsETA(String s) {
        return this.pointsHorsETA.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToPointsHorsETA(Collection<String> c) {
        return this.pointsHorsETA.addAll(c);
    }

    /**
     * 
     */
    public void clearPointsHorsETA() {
        this.pointsHorsETA.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisAvecETA() {
        return Collections.unmodifiableSet(this.colisAvecETA);
    }

    /**
     * 
     * @param colisAvecETA
     */
    public void setColisAvecETA(Collection<String> c) {
        this.clearColisAvecETA();
        this.addAllToColisAvecETA(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisAvecETA(String s) {
        return this.colisAvecETA.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisAvecETA(Collection<String> c) {
        return this.colisAvecETA.addAll(c);
    }

    /**
     * 
     */
    public void clearColisAvecETA() {
        this.colisAvecETA.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisAnoSpecifSWAP() {
        return Collections.unmodifiableSet(this.colisAnoSpecifSWAP);
    }

    /**
     * 
     * @param colisAnoSpecifSWAP
     */
    public void setColisAnoSpecifSWAP(Collection<String> c) {
        this.clearColisAnoSpecifSWAP();
        this.addAllToColisAnoSpecifSWAP(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisAnoSpecifSWAP(String s) {
        return this.colisAnoSpecifSWAP.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisAnoSpecifSWAP(Collection<String> c) {
        return this.colisAnoSpecifSWAP.addAll(c);
    }

    /**
     * 
     */
    public void clearColisAnoSpecifSWAP() {
        this.colisAnoSpecifSWAP.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisAnoSpecifREP() {
        return Collections.unmodifiableSet(this.colisAnoSpecifREP);
    }

    /**
     * 
     * @param colisAnoSpecifREP
     */
    public void setColisAnoSpecifREP(Collection<String> c) {
        this.clearColisAnoSpecifREP();
        this.addAllToColisAnoSpecifREP(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisAnoSpecifREP(String s) {
        return this.colisAnoSpecifREP.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisAnoSpecifREP(Collection<String> c) {
        return this.colisAnoSpecifREP.addAll(c);
    }

    /**
     * 
     */
    public void clearColisAnoSpecifREP() {
        this.colisAnoSpecifREP.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisAnoSpecifTAXE() {
        return Collections.unmodifiableSet(this.colisAnoSpecifTAXE);
    }

    /**
     * 
     * @param colisAnoSpecifTAXE
     */
    public void setColisAnoSpecifTAXE(Collection<String> c) {
        this.clearColisAnoSpecifTAXE();
        this.addAllToColisAnoSpecifTAXE(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisAnoSpecifTAXE(String s) {
        return this.colisAnoSpecifTAXE.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisAnoSpecifTAXE(Collection<String> c) {
        return this.colisAnoSpecifTAXE.addAll(c);
    }

    /**
     * 
     */
    public void clearColisAnoSpecifTAXE() {
        this.colisAnoSpecifTAXE.clear();
    }

    /**
     * 
     * @return
     */
    /**
     * @return
     */
    public Set<String> getColisAvecConsigne() {
        return Collections.unmodifiableSet(this.colisAvecConsigne);
    }

    /**
     * 
     * @param colisAvecConsigne
     */
    public void setColisAvecConsigne(Collection<String> c) {
        this.clearColisAvecConsigne();
        this.addAllToColisAvecConsigne(c);
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean addToColisAvecConsigne(String s) {
        return this.colisAvecConsigne.add(s);
    }

    /**
     * 
     * @param set
     * @return
     */
    public boolean addAllToColisAvecConsigne(Collection<String> c) {
        return this.colisAvecConsigne.addAll(c);
    }

    /**
     * 
     */
    public void clearColisAvecConsigne() {
        this.colisAvecConsigne.clear();
    }

    /**
     * @see CollectionTournee#pointsEchecPresentation
     * @param s
     * @return
     */
    public boolean addToPointsEchecPresentation(String s) {
        return this.pointsEchecPresentation.add(s);
    }

    /**
     * @see CollectionTournee#pointsEchecPresentation
     * @param c
     * @return
     */
    public boolean addAllToPointsEchecPresentation(Collection<String> c) {
        return this.pointsEchecPresentation.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsEchecPresentation
     */
    public void clearPointsEchecPresentation() {
        this.pointsEchecPresentation.clear();
    }

    /**
     * @see CollectionTournee#pointsEchecPresentation
     * @return
     */
    public Set<String> getPointsEchecPresentation() {
        return Collections.unmodifiableSet(this.pointsEchecPresentation);
    }

    /**
     * @see CollectionTournee#pointsEchecPresentation
     * @param c
     */
    public void setPointsEchecPresentation(Collection<String> c) {
        this.clearPointsEchecPresentation();
        this.addAllToPointsEchecPresentation(c);
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @return
     */
    public Map<String, Set<String>> getPresentationsInfructueuses() {
        return this.presentationsInfructueusesUnmodifiableMap;
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param m
     */
    public void setPresentationsInfructueuses(Map<String, Collection<String>> m) {
        this.clearPresentationsInfructueuses();
        this.putAllToPresentationsInfructueuses(m);
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param m
     */
    public void setPresentationsInfructueuses(Multimap<String, String> m) {
        this.clearPresentationsInfructueuses();
        this.putAllToPresentationsInfructueuses(m);
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param codeEvt
     * @param notLt
     * @return
     */
    public Collection<String> putToPresentationsInfructueuses(String codeEvt, String noLt) {
        Collection<String> oldValue = new HashSet<>(this.presentationsInfructueuses.get(codeEvt));
        this.presentationsInfructueuses.put(codeEvt, noLt);
        return oldValue;
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param codeEvt
     * @param noLts
     * @return
     */
    public Collection<String> putToPresentationsInfructueuses(String codeEvt, Collection<String> noLts) {
        Collection<String> oldValue = new HashSet<>(this.presentationsInfructueuses.get(codeEvt));
        this.presentationsInfructueuses.putAll(codeEvt, noLts);
        return oldValue;
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param m
     */
    public void putAllToPresentationsInfructueuses(Map<String, Collection<String>> m) {
        if (m != null) {
            for (Map.Entry<String, Collection<String>> entry : m.entrySet()) {
                this.presentationsInfructueuses.putAll(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param m
     */
    public void putAllToPresentationsInfructueuses(Multimap<String, String> m) {
        this.presentationsInfructueuses.putAll(m);
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     * @param codeEvt
     * @return
     */
    public Collection<String> removeFromPresentationsInfructueuses(String codeEvt) {
        return this.presentationsInfructueuses.removeAll(codeEvt);
    }

    /**
     * @see CollectionTournee#presentationsInfructueuses
     */
    public void clearPresentationsInfructueuses() {
        this.presentationsInfructueuses.clear();
    }

    /**
     * @see CollectionTournee#colisAnoConsigneNonRespectee
     * @param s
     * @return
     */
    public boolean addToColisAnoConsigneNonRespectee(String s) {
        return this.colisAnoConsigneNonRespectee.add(s);
    }

    /**
     * @see CollectionTournee#colisAnoConsigneNonRespectee
     * @param c
     * @return
     */
    public boolean addAllToColisAnoConsigneNonRespectee(Collection<String> c) {
        return this.colisAnoConsigneNonRespectee.addAll(c);
    }

    /**
     * @see CollectionTournee#colisAnoConsigneNonRespectee
     */
    public void clearColisAnoConsigneNonRespectee() {
        this.colisAnoConsigneNonRespectee.clear();
    }

    public Set<String> getColisAnoConsigneNonRespectee() {
        return Collections.unmodifiableSet(this.colisAnoConsigneNonRespectee);
    }

    /**
     * @see CollectionTournee#colisAnoConsigneNonRespectee
     * @param c
     */
    public void setColisAnoConsigneNonRespectee(Collection<String> c) {
        this.clearColisAnoConsigneNonRespectee();
        this.addAllToColisAnoConsigneNonRespectee(c);
    }

    /**
     * @see CollectionTournee#pointsAnoConsigneNonRespectee
     * @param s
     * @return
     */
    public boolean addToPointsAnoConsigneNonRespectee(String s) {
        return this.pointsAnoConsigneNonRespectee.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoConsigneNonRespectee
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoConsigneNonRespectee(Collection<String> c) {
        return this.pointsAnoConsigneNonRespectee.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoConsigneNonRespectee
     */
    public void clearPointsAnoConsigneNonRespectee() {
        this.pointsAnoConsigneNonRespectee.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoConsigneNonRespectee
     * @return
     */
    public Set<String> getPointsAnoConsigneNonRespectee() {
        return Collections.unmodifiableSet(this.pointsAnoConsigneNonRespectee);
    }

    /**
     * @see CollectionTournee#pointsAnoConsigneNonRespectee
     * @param c
     */
    public void setPointsAnoConsigneNonRespectee(Collection<String> c) {
        this.clearPointsAnoConsigneNonRespectee();
        this.addAllToPointsAnoConsigneNonRespectee(c);
    }

    /**
     * @see CollectionTournee#colisAnoEvtNonPermis
     * @param s
     * @return
     */
    public boolean addToColisAnoEvtNonPermis(String s) {
        return this.colisAnoEvtNonPermis.add(s);
    }

    /**
     * @see CollectionTournee#colisAnoEvtNonPermis
     * @param c
     * @return
     */
    public boolean addAllToColisAnoEvtNonPermis(Collection<String> c) {
        return this.colisAnoEvtNonPermis.addAll(c);
    }

    /**
     * @see CollectionTournee#colisAnoEvtNonPermis
     */
    public void clearColisAnoEvtNonPermis() {
        this.colisAnoEvtNonPermis.clear();
    }

    /**
     * @see CollectionTournee#colisAnoEvtNonPermis
     * @return
     */
    public Set<String> getColisAnoEvtNonPermis() {
        return Collections.unmodifiableSet(this.colisAnoEvtNonPermis);
    }

    /**
     * @see CollectionTournee#colisAnoEvtNonPermis
     * @param c
     */
    public void setColisAnoEvtNonPermis(Collection<String> c) {
        this.clearColisAnoEvtNonPermis();
        this.addAllToColisAnoEvtNonPermis(c);
    }

    /**
     * @see CollectionTournee#pointsAnoEvtNonPermis
     * @param s
     * @return
     */
    public boolean addToPointsAnoEvtNonPermis(String s) {
        return this.pointsAnoEvtNonPermis.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoEvtNonPermis
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoEvtNonPermis(Collection<String> c) {
        return this.pointsAnoEvtNonPermis.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoEvtNonPermis
     */
    public void clearPointsAnoEvtNonPermis() {
        this.pointsAnoEvtNonPermis.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoEvtNonPermis
     * @return
     */
    public Set<String> getPointsAnoEvtNonPermis() {
        return Collections.unmodifiableSet(this.pointsAnoEvtNonPermis);
    }

    /**
     * @see CollectionTournee#pointsAnoEvtNonPermis
     * @param c
     */
    public void setPointsAnoEvtNonPermis(Collection<String> c) {
        this.clearPointsAnoEvtNonPermis();
        this.addAllToPointsAnoEvtNonPermis(c);
    }

    /**
     * @see CollectionTournee#pointsAnoMadPresNonRespectee
     * @param s
     * @return
     */
    public boolean addToPointsAnoMadPresNonRespectee(String s) {
        return this.pointsAnoMadPresNonRespectee.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoMadPresNonRespectee
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoMadPresNonRespectee(Collection<String> c) {
        return this.pointsAnoMadPresNonRespectee.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoMadPresNonRespectee
     */
    public void clearPointsAnoMadPresNonRespectee() {
        this.pointsAnoMadPresNonRespectee.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoMadPresNonRespectee
     * @return
     */
    public Set<String> getPointsAnoMadPresNonRespectee() {
        return Collections.unmodifiableSet(this.pointsAnoMadPresNonRespectee);
    }

    /**
     * @see CollectionTournee#pointsAnoMadPresNonRespectee
     * @param c
     */
    public void setPointsAnoMadPresNonRespectee(Collection<String> c) {
        this.clearPointsAnoMadPresNonRespectee();
        this.addAllToPointsAnoMadPresNonRespectee(c);
    }

    /**
     * @see CollectionTournee#pointsAnoMadNonPermise
     * @param s
     * @return
     */
    public boolean addToPointsAnoMadNonPermise(String s) {
        return this.pointsAnoMadNonPermise.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoMadNonPermise
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoMadNonPermise(Collection<String> c) {
        return this.pointsAnoMadNonPermise.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoMadNonPermise
     */
    public void clearPointsAnoMadNonPermise() {
        this.pointsAnoMadNonPermise.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoMadNonPermise
     * @return
     */
    public Set<String> getPointsAnoMadNonPermise() {
        return Collections.unmodifiableSet(this.pointsAnoMadNonPermise);
    }

    /**
     * @see CollectionTournee#pointsAnoMadNonPermise
     * @param c
     */
    public void setPointsAnoMadNonPermise(Collection<String> c) {
        this.clearPointsAnoMadNonPermise();
        this.addAllToPointsAnoMadNonPermise(c);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDSansP
     * @param s
     * @return
     */
    public boolean addToPointsAnoSWAPDSansP(String s) {
        return this.pointsAnoSWAPDSansP.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDSansP
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoSWAPDSansP(Collection<String> c) {
        return this.pointsAnoSWAPDSansP.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDSansP
     */
    public void clearPointsAnoSWAPDSansP() {
        this.pointsAnoSWAPDSansP.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDSansP
     * @return
     */
    public Set<String> getPointsAnoSWAPDSansP() {
        return Collections.unmodifiableSet(this.pointsAnoSWAPDSansP);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDSansP
     * @param c
     */
    public void setPointsAnoSWAPDSansP(Collection<String> c) {
        this.clearPointsAnoSWAPDSansP();
        this.addAllToPointsAnoSWAPDSansP(c);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDEtP
     * @param s
     * @return
     */
    public boolean addToPointsAnoSWAPDEtP(String s) {
        return this.pointsAnoSWAPDEtP.add(s);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDEtP
     * @param c
     * @return
     */
    public boolean addAllToPointsAnoSWAPDEtP(Collection<String> c) {
        return this.pointsAnoSWAPDEtP.addAll(c);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDEtP
     */
    public void clearPointsAnoSWAPDEtP() {
        this.pointsAnoSWAPDEtP.clear();
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDEtP
     * @return
     */
    public Set<String> getPointsAnoSWAPDEtP() {
        return Collections.unmodifiableSet(this.pointsAnoSWAPDEtP);
    }

    /**
     * @see CollectionTournee#pointsAnoSWAPDEtP
     * @param c
     */
    public void setPointsAnoSWAPDEtP(Collection<String> c) {
        this.clearPointsAnoSWAPDEtP();
        this.addAllToPointsAnoSWAPDEtP(c);
    }

    public ImmutableMap<String, Date> getPsm() {
        return ImmutableMap.copyOf(psm);
    }

    public void setPsm(Map<String, Date> map) {
        clearPsm();
        putAllToPsm(map);
    }

    public Date putToPsm(String s, Date d) {
        return psm.put(s, d);
    }

    public void putAllToPsm(Map<String, Date> map) {
        psm.putAll(map);
    }

    public void clearPsm() {
        psm.clear();
    }

    public boolean addToPointAvecColisRetour(String s) {
        return pointAvecColisRetour.add(s);
    }
    
    public boolean addAllPointAvecColisRetour(Collection<String> c) {
        return pointAvecColisRetour.addAll(c);
    }
    
    public Set<String> getPointAvecColisRetour() {
        return pointAvecColisRetour;
    }

    public boolean addToColisVusEnRetour(String s){
        return colisVusEnRetour.add(s);
    }
    
    public boolean addAllColisVusEnRetour(Collection<String> c){
        return colisVusEnRetour.addAll(c);
    }
    
    public Set<String> getColisVusEnRetour() {
        return Collections.unmodifiableSet(this.colisVusEnRetour);
    }
}
