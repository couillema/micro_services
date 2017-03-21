package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.EAnomalie;
import com.chronopost.vision.model.getsynthesetournees.v1.InfoTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.chronopost.vision.model.rules.TourneeRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.chronopost.vision.transco.TranscoderService;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Implémentation de SyntheseTourneeService
 * 
 * @author jcbontemps / ftemplier
 */
public enum SyntheseTourneeServiceImpl implements SyntheseTourneeService {
    /*
     * Singleton
     */
    INSTANCE;

    private SyntheseTourneeDao dao;
    private final Logger log = LoggerFactory.getLogger(SyntheseTourneeServiceImpl.class);

    public SyntheseTourneeService setDao(final SyntheseTourneeDao dao) {
        this.dao = dao;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.
     * SyntheseTourneeService#getSyntheseTourneeQuantite(java.util.List)
     */
    @Override
    public Map<String, SyntheseTourneeQuantite> getSyntheseTourneeQuantite(final List<String> idTournees) {
        final Map<String, SyntheseTourneeQuantite> map = new HashMap<>();

        try {
            // appels parallélisés de la commande syntheseTourneeQuantiteFutures
            final Map<String, Future<SyntheseTourneeQuantite>> syntheseTourneeQuantiteFutures = new HashMap<>();
            for (final String idTournee : idTournees)
                syntheseTourneeQuantiteFutures.put(idTournee, new SyntheseTourneeQuantiteCommand(idTournee, this).queue());

            // enregistrement des résultats
            for (final String id : syntheseTourneeQuantiteFutures.keySet()) {
            	final Future<SyntheseTourneeQuantite> futur = syntheseTourneeQuantiteFutures.get(id);
                if (futur != null)
                	map.put(id, futur.get());
                else
                	map.put(id, SyntheseTourneeQuantite.NONE);
            }
        } catch (InterruptedException e) {
            throw new MSTechnicalException("Une erreur de type InterruptedException est intervenue dans " + this.getClass().getCanonicalName(), e);
        } catch (ExecutionException e) {
            throw new MSTechnicalException("Une erreur de type ExecutionException est intervenue dans " + this.getClass().getCanonicalName(), e);
        }

        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.
     * SyntheseTourneeService#getSyntheseTourneeActivite(java.lang.String)
     */
    @Override
    public List<PointTournee> getSyntheseTourneeActivite(final String idTournee) throws InterruptedException, ExecutionException {
        final Tournee tournee = dao.getPointsTournee(idTournee);

        return calculAnomaliesTournee(tournee);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.
     * SyntheseTourneeService
     * #getSyntheseTourneeActiviteEtQuantite(java.lang.String)
     */
    @Override
    public InfoTournee getSyntheseTourneeActiviteEtQuantite(final String idTournee) throws InterruptedException, ExecutionException {
        final InfoTournee infoTournee = new InfoTournee();

        final Tournee tournee = dao.getPointsTournee(idTournee);

        // Calcul List de PointTournee
        final List<PointTournee> pointsTournee = calculAnomaliesTournee(tournee);

        // Calcul SyntheseTourneeQuantite
        final CollectionTournee collection = genereCollectionTournee(tournee);
        final SyntheseTourneeQuantite syntheseTourneeQuantite = calculSyntheseTournee(collection);
        syntheseTourneeQuantite.setIdC11(tournee.getIdentifiantTournee());
        infoTournee.setPoints(pointsTournee);
        infoTournee.setSynthese(syntheseTourneeQuantite);

        return infoTournee;
    }

	@Override
	public Map<String, InfoTournee> getSyntheseTourneeActivitesEtQuantites(final List<String> tourneeIds) {
		final Map<String, InfoTournee> syntheseTournees = new HashMap<>();
		for (final String idTournee : tourneeIds) {
			try {
				syntheseTournees.put(idTournee, getSyntheseTourneeActiviteEtQuantite(idTournee));
			} catch (Exception e) {
				log.error("Erreur lors de getSyntheseTourneeActiviteEtQuantite pour idTournee " + idTournee, e);
			}
		}
		return syntheseTournees;
	}

    /**
     * Récupére la liste de points List<PointTournee> de la tournée
     * (getPointsTournee) Génére les collections tournée
     * (genereCollectionsTournee) Calcule la synthèse tournée
     * (calculSynthseTournee)
     * 
     * @param idTournee
     *            id d'une tournée
     * @return la synthèse de la tournée
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public SyntheseTourneeQuantite getOneSyntheseQuantiteTournee(final String idTournee) throws InterruptedException, ExecutionException {
        final Tournee tournee = dao.getPointsTournee(idTournee);
        final CollectionTournee collection = genereCollectionTournee(tournee);
        final SyntheseTourneeQuantite calculSyntheseTournee = calculSyntheseTournee(collection);
        calculSyntheseTournee.setIdC11(tournee.getIdentifiantTournee());
        return calculSyntheseTournee;
    }

    /**
     * NbPtTA = pointsPrepares.size() NbColisTA = colisPrepares.size()
     * NbPtVisites = pointsTraites.size() NbPtTANonVisites =
     * Ensemble(pointsPrepares - pointsTraites).size() NbColisTraites =
     * colisTraites.size()
     * 
     * @param collection
     *            un objet CollectionTournee contenant certaines statistiques
     * @return un objet SyntheseTourneeQuantite contenant toutes les
     *         statistiques demandées par le MS
     */
    public SyntheseTourneeQuantite calculSyntheseTournee(final CollectionTournee collection) {
        final SyntheseTourneeQuantite qtte = new SyntheseTourneeQuantite();

        final Set<String> colisRetour = Sets.union(collection.getColisEchecPresentation(),
                Sets.difference(collection.getColisPrepares(), collection.getColisDeposes()));

        final Builder<String> builderPointsAnomalies = ImmutableSet.builder();
        final Set<String> pointsAnomalies = builderPointsAnomalies.addAll(collection.getPointsEchecPresentation())
                .addAll(Sets.difference(collection.getPointsPrepares(), collection.getPointsTraites()))
                .addAll(collection.getPointsAnoConsigneNonRespectee()).addAll(collection.getPointsAnoEvtNonPermis())
                .addAll(collection.getPointsAnoMadPresNonRespectee()).addAll(collection.getPointsAnoMadNonPermise())
                .addAll(collection.getPointsAnoSWAPDSansP()).addAll(collection.getPointsAnoSWAPDEtP()).addAll(collection.getPointsHorsDateContractuelle())
                .addAll(collection.getPointsHorsETA()).build();

        final Map<String, Integer> presentationsInfructueuses = new HashMap<>();
        for (final Map.Entry<String, Set<String>> entry : collection.getPresentationsInfructueuses().entrySet()) {
            presentationsInfructueuses.put(entry.getKey(), entry.getValue().size());
        }

        qtte.setNbPtTA(Integer.valueOf(collection.getPointsPrepares().size()));
        qtte.setNbColisTA(Integer.valueOf(collection.getColisPrepares().size()));
        qtte.setNbPtVisites(Integer.valueOf(collection.getPointsTraites().size()));
        qtte.setNbPtTANonVisites(Integer.valueOf(Sets.difference(collection.getPointsPrepares(), collection.getPointsTraites()).size()));
        qtte.setNbColisHorsDateContractuelle(collection.getColisHorsDateContractuelle().size());
        qtte.setColisHorsDateContractuelle(collection.getColisHorsDateContractuelle());
        qtte.setNbColisAvecConsigne(collection.getColisAvecConsigne().size());
        qtte.setNbColisRetour(colisRetour.size());
        qtte.setNbColisSecurisesRetour(Sets.intersection(colisRetour, collection.getColisSensibles()).size());
//        qtte.setNbPointsAvecColisRetour(Sets.union(collection.getPointsEchecPresentation(),
//                Sets.difference(collection.getPointsPrepares(), collection.getPointsTraites())).size());
        qtte.setNbPointsAvecColisRetour(collection.getPointAvecColisRetour().size());
        qtte.setNbColisSpecifiques(collection.getColisAnoSpecifSWAP().size() + collection.getColisAnoSpecifREP().size()
                + collection.getColisAnoSpecifTAXE().size());
        qtte.setNbPtAnomalie(pointsAnomalies.size());
        qtte.setNbColisTraites(collection.getColisTraites().size());
        qtte.setNbPointsAnomalieTracabilite(Sets.union(collection.getPointsAnoConsigneNonRespectee(), collection.getPointsAnoEvtNonPermis()).size());
        qtte.setNbPtMisADispoBureau(collection.getPointsMisADispoBureauAvecPresNecessaire().size()
                + collection.getPointsMisADispoBureauSansPresNecessaire().size());
        qtte.setNbHorsDelai(collection.getPointsHorsDateContractuelle().size() + collection.getPointsHorsETA().size());
        qtte.setPresentationsInfructueuses(presentationsInfructueuses);
        qtte.setNbColisAvecETA(collection.getColisAvecETA().size());
        qtte.setNbColisHorsETA(collection.getColisHorsETA().size());
        qtte.setNbColisRetourTraite(Sets.intersection(colisRetour, collection.getColisVusEnRetour()).size());
        qtte.setNbColisAvecConsigneNonRespectee(collection.getColisAnoConsigneNonRespectee().size());
        qtte.setPsm(collection.getPsm());
        
        return qtte;
    }

    /**
     * positionnement des numPointDistri (incrémentation) - Si un point possède
     * un événement de type TA, alors on ajoute l’identifiant du point à la
     * collection <pointsPrepares> - Si un un événement est de type TA, alors on
     * ajoute l’identifiant du colis (no_lt) à la collection <colisPrepares> -
     * Si un point possède un événement de type autre que TA, alors on ajoute
     * l’identifiant du point à la collection <pointsTraites> - Si un événement
     * est de type autre que TA, alors on ajoute l’identifiant du colis (no_lt)
     * à la collection <colisTraites>
     * 
     * @param points
     *            une liste de Points d'une tournée
     * @return un objet CollectionTournee contenant certaines statistiques
     */
    public CollectionTournee genereCollectionTournee(final Tournee tournee) {
        final int depassement_max_eta = Integer.parseInt(TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.DEPASSEMENT_MAX_ETA.toString()));

        final int depassement_min_eta = Integer.parseInt(TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.DEPASSEMENT_MIN_ETA.toString()));

        final String evtPresentationPositive = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.EVT_PRESENTATION_POSITIVE.toString());
        final String evtPresentationNegative = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.EVT_PRESENTATION_NEGATIVE.toString());
        final String evtMiseADispositionBureau = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU.toString());

        int numPointDistri = 1;
        final CollectionTournee collection = new CollectionTournee();
        
        final boolean estTourneeDOM = TourneeRules.isTourneeDOM(tournee.getIdentifiantTournee());

        for (final PointTournee point : tournee.getPoints()) {
            // mise à jour des numéros des points de distribution si date passage non null
            if (point.getDatePassage() != null) {
                point.setNumPointDistri(numPointDistri++);
            }

            for (final ColisPoint colis : point.getColisPrevus()) {
                // RG-MSGetSyntTournee-002
                // Si un point possède un événement de type TA, alors on ajoute
                // l’identifiant du point à la collection <pointsPrepares>
                collection.addToPointsPrepares(point.getIdentifiantPoint());
                log.debug("RG-MSGetSyntTournee-002 - Add point '{}' to pointsPrepares", point.getIdentifiantPoint());

                // RG-MSGetSyntTournee-003
                // Si un événement est de type TA, alors on ajoute l’identifiant
                // du colis (no_lt) à la collection <colisPrepares>
                collection.addToColisPrepares(colis.getNo_lt());
                log.debug("RG-MSGetSyntTournee-003 - Add colis '{}' to colisPrepares", colis.getNo_lt());
            }

            // On ne garde que les EVT D+ de point.colisPresent
            final SortedSet<ColisPoint> evtsDPlus = filterColisPoint(Sets.newTreeSet(point.getColisPresents()),
                    getPredicateOnlyEvts(ParametresMicroservices.EVT_D_PLUS));
            for (final ColisPoint colis : evtsDPlus) {
                // RG-MSGetSyntTournee-004
                // Si un point possède un événement de type D+ (autre que TA, TE
                // PC et PE), alors on ajoute l’identifiant du point à la
                // collection <pointsTraites>
                collection.addToPointsTraites(point.getIdentifiantPoint());
                log.debug("RG-MSGetSyntTournee-004 - Add point '{}' to pointsTraites", point.getIdentifiantPoint());

                // RG-MSGetSyntTournee-005
                // Si un événement est de type D+ (autre que TA, TE PC et PE),
                // alors on ajoute l’identifiant du colis (no_lt) à la
                // collection <colisTraites>
                collection.addToColisTraites(colis.getNo_lt());
                log.debug("RG-MSGetSyntTournee-005 - Add colis '{}' to colisTraites", colis.getNo_lt());
            }
        }

        /* Parcours des colis */
        for (final Map.Entry<String, SortedSet<ColisPoint>> colisEvts : tournee.getColisEvenements().entrySet()) {
            final String noLt = colisEvts.getKey();
            final SortedSet<ColisPoint> evts = colisEvts.getValue();
            final ColisPoint premierEvt = getPremierEvtColis(evts);
            /* Le dernier evt TA du colis sur la tournée */
            final ColisPoint evtTA = getEvtTA(evts);
            final ColisPoint dernierEvt = getDernierEvtColis(evts);
            final String dernierCodeEvt = String.format("|%s|", dernierEvt.getCodeEvenement());
            final SpecifsColis specifsColis = tournee.getColisSpecifs().get(noLt);
            final String psm = dernierEvt.getOutilSaisie();
            final Date psmLastDate = dernierEvt.getDateEvt();
            boolean testHDContratEffectue = false;

            // TODO : D'après les spec, date de dernière utilisation, c'est quoi ???
            // Ajout des PSM et de sa date d'événement si cette date est supérieure à la dernière date mémorisée
			if (psm != null
					&& (collection.getPsm().get(psm) == null || collection.getPsm().get(psm).before(psmLastDate))) {
				collection.putToPsm(psm, psmLastDate);
			}
            
            // RG-MSGetSyntTournee-006
            // Si un événement est de type evt_presentation_positive, alors si
            // c'est le dernier evt du colis, on ajoute le colis à la collection
            // <colisDeposes>
            if (evtPresentationPositive.contains(dernierCodeEvt)) {
                collection.addToColisDeposes(noLt);
                log.debug("RG-MSGetSyntTournee-006 - Add colis '{}' to colisDeposes", noLt);
            }

            if (evtPresentationNegative.contains(dernierCodeEvt)) {
                // RG-MSGetSyntTournee-007
                // Si un événement est de type
                // evt_presentation_domicile_negative, alors
                // si c'est le dernier evt du colis, on ajoute le colis à la
                // collection <colisEchecPresentation>
                collection.addToColisEchecPresentation(noLt);
                log.debug("RG-MSGetSyntTournee-007 - Add colis '{}' to colisEchecPresentation", noLt);

                collection.addToPointsEchecPresentation(dernierEvt.getIdentifiantPoint());
                log.debug("RG-MSGetSyntTournee-007 - Add point '{}' to pointsEchecPresentation", dernierEvt.getIdentifiantPoint());

                // RG-MSGetSyntTournee-016
                // Si un colis a comme dernier evénement une présentation
                // infructueuse (evt_presentation_domicile_negative) l’ajouter à
                // la collection (multimap) <presentationsInfructueuses>
                collection.putToPresentationsInfructueuses(dernierEvt.getCodeEvenement(), noLt);
                log.debug("RG-MSGetSyntTournee-016 - Add date/colis '{}/{}' to presentationsInfructueuses", dernierEvt.getCodeEvenement(), noLt);
                
                // RG-MSGetSyntTournee-025
                // Si un colis a comme dernier evénement une présentation infructueuse 
                // ajouter le point à la collection <pointAvecColisRetour>
                collection.addToPointAvecColisRetour(dernierEvt.getIdentifiantPoint());
                log.debug("RG-MSGetSyntTournee-025 - Add point '{}' to pointAvecColisRetour", dernierEvt.getIdentifiantPoint());
            }

            // RG-MSGetSyntTournee-008
            // Si un événement est de type evt_mise_a_dispo, alors ajouter le
            // colis à la collection <colisMisADispoBureau> si le colis
            // nécessitait une présentation (RG-MSGetSyntTournee-507) ajouter le
            // point à la collection <pointMisADispoBureauAvecPresNecessaire>
            // sinon ajouter le point à la collection
            // <pointMisADispoBureauSansPresNecessaire>
            if (evtMiseADispositionBureau.contains(dernierCodeEvt)) {
                collection.addToColisMisADispoBureau(noLt);

                if (SpecifsColisRules.aMettreEnInstance(specifsColis, dernierEvt.getDateEvt()) == false) {
                    collection.addToPointsMisADispoBureauAvecPresNecessaire(dernierEvt.getIdentifiantPoint());
                    log.debug("RG-MSGetSyntTournee-008 - Add point '{}' to pointsMisADispoBureauAvecPresNecessaire", dernierEvt.getIdentifiantPoint());

                    // RG-MSGetSyntTournee-021
                    // si pointsMisADispoBureauAvecPresNecessaire
                    // (RG-MSGetSyntTournee-008)
                    // si il n’existe pas d’événement de présentation pour ce
                    // colis ajouter le point à <pointAnoMadPresNonRespectee>
                    final SortedSet<ColisPoint> evtsPresentationDomicile = filterColisPoint(evts,
                            getPredicateOnlyEvts(ParametresMicroservices.EVT_PRESENTATION_DOMICILE));
                    if (evtsPresentationDomicile.isEmpty()) {
                        collection.addToPointsAnoMadPresNonRespectee(dernierEvt.getIdentifiantPoint());
                        log.debug("RG-MSGetSyntTournee-021 - Add point '{}' to pointsAnoMadPresNonRespectee", dernierEvt.getIdentifiantPoint());
                    }
                } else {
                    collection.addToPointsMisADispoBureauSansPresNecessaire(dernierEvt.getIdentifiantPoint());
                    log.debug("RG-MSGetSyntTournee-008 - Add point '{}' to pointsMisADispoBureauSansPresNecessaire", dernierEvt.getIdentifiantPoint());
                }
            }

            // RG-MSGetSyntTournee-011
            // Si un événement est le 1er evt D+ du colis
            // Si cet événement indique un diffETA
            // ajouter le colis à la collection <colisAvecETA>
            //
            // Si ce diffETA > depassement_max_eta ou diffETA <
            // depassement_min_eta alors
            // ajouter le colis à la collection <colisHorsETA>
            // ajouter le point à la collection <pointHorsETA>
            // (Le point est le point sur lequel l’événement à eu lieu)
            final ColisPoint premierEvtDPlus = getPremierEvtDPlus(evts);
            if (premierEvtDPlus != null) {
                    final Integer diffETA = premierEvtDPlus.getDiffETA();
                    if (diffETA != null) {
                        collection.addToColisAvecETA(noLt);
                        log.debug("RG-MSGetSyntTournee-011 - Add colis '{}' to colisAvecETA", noLt);

                        if (diffETA > depassement_max_eta || diffETA < depassement_min_eta) {
                            collection.addToColisHorsETA(noLt);
                            log.debug("RG-MSGetSyntTournee-011 - Add colis '{}' to colisHorsETA", noLt);

                            if (premierEvtDPlus.getIdentifiantPoint() != null)
                            	collection.addToPointsHorsETA(premierEvtDPlus.getIdentifiantPoint());
                            log.debug("RG-MSGetSyntTournee-011 - Add point '{}' to pointsHorsETA", premierEvtDPlus.getIdentifiantPoint());
                        }
                    }
            }

            // RG-MSGetSyntTournee-010 (partie RDV) selon RG-MSGetSyntTournee-533
            // si le colis est RDV et possède un créneau alors
            // si  l’heure de la date evt hors heures créneau alors 
            //	 ajouter au PointTournee.anomalies la String “HDCONTRAT”
            //	 ajouter au ColisPoint.anomalies la String “HDCONTRAT”
            String creneauDebut,creneauFin;
			if (premierEvtDPlus != null && evtTA != null && "RDV".equals(evtTA.getPrecocite())
					&& (creneauDebut = evtTA.getInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT)) != null
					&& (creneauFin = evtTA.getInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN)) != null) {
				testHDContratEffectue = true;
				if (DateRules.estHorsCreneauHoraire(creneauDebut, creneauFin, premierEvtDPlus.getDateEvt())) {
					collection.addToColisHorsDateContractuelle(noLt);
					log.debug("RG-MSGetSyntTournee-010 - Add colis '{}' to colisHorsDateContractuelle", noLt);

					collection.addToPointsHorsDateContractuelle(premierEvtDPlus.getIdentifiantPoint());
					log.debug("RG-MSGetSyntTournee-010 - Add point '{}' to pointsHorsDateContractuelle",
							premierEvtDPlus.getIdentifiantPoint());
				}
			}
            
            if (specifsColis != null) {
                final ColisPoint evtD = getEvtD(evts);
                final Collection<String> specifsEvts = SpecifsColisRules.getSpecifEvt(specifsColis, dernierEvt.getDateEvt());
                final Collection<String> specifsService = SpecifsColisRules.getCaracteristiqueColisSansPreco(specifsColis, premierEvt.getDateEvt());

                // RG-MSGetSyntTournee-009
                // Si un colis est sécurisé (contient la spécificité SENSIBLE
                // dans specifsColis.specifsEvenements) l'ajouter dans la
                // collection <colisSensibles>
                if (ESpecificiteColis.SENSIBLE.isPresentIn(specifsEvts)) {
                    collection.addToColisSensibles(noLt);
                    log.debug("RG-MSGetSyntTournee-009 - Add colis '{}' to colisSensibles", noLt);
                }

                // RG-MSGetSyntTournee-010 selon RG-MSGetSyntTournee-533
                // Si le cas 10 n'est pas déjà traité au dessus par le cas RDV 
                // et si un événement est le 1er evt D+ du colis et que cet événement
                // a dépassé l'heure de précocité alors
                // ajouter le colis à la collection <colisHorsDateContractuelle>
                // ajouter le point à la collection <pointHorsDateContractuelle>
                // (Le point est le point sur lequel l’événement à eu lieu)
                if (!testHDContratEffectue && premierEvtDPlus != null) {
                	if (SpecifsColisRules.estEvtDPlusHorsDelai(premierEvtDPlus, specifsColis, colisEvts)) {
                		collection.addToColisHorsDateContractuelle(noLt);
						log.debug("RG-MSGetSyntTournee-010 - Add colis '{}' to colisHorsDateContractuelle", noLt);
						collection.addToPointsHorsDateContractuelle(premierEvtDPlus.getIdentifiantPoint());
						log.debug("RG-MSGetSyntTournee-010 - Add point '{}' to pointsHorsDateContractuelle",
								premierEvtDPlus.getIdentifiantPoint());
                	}
				}

                // RG-MSGetSyntTournee-012
                // Si un colis possède une spécificité SWAP (Contient la
                // spécificité dans specifsColis.specifsEvenements) et qu’il a
                // un événement D sur la tournée, l’ajouter dans la collection
                // <colisAnoSpecifSWAP>
                if (evtD != null && ESpecificiteColis.SWAP.isPresentIn(specifsService)) {
                    collection.addToColisAnoSpecifSWAP(noLt);
                    log.debug("RG-MSGetSyntTournee-012 - Add colis '{}' to colisAnoSpecifSWAP", noLt);

                    final String noLtRetour = specifsColis.getInfoSupp().get(EInfoSupplementaire.NO_LT_RETOUR.getCode());
                    final SortedSet<ColisPoint> evtsColisRetour = tournee.getColisEvenements(noLtRetour);
                    final SortedSet<ColisPoint> evtsColisRetourPCOuPE = filterColisPoint(evtsColisRetour, getPredicateOnlyEvts(EVT_PC, EVT_PE));
                    if (evtsColisRetourPCOuPE.isEmpty()) {
                        final SortedSet<ColisPoint> evtsColisAllerPCOuPE = filterColisPoint(evts, getPredicateOnlyEvts(EVT_PC, EVT_PE));
                        if (evtsColisAllerPCOuPE.isEmpty()) {
                            // RG-MSGetSyntTournee-023
                            // Si un colis répond à RG-MSGetSyntTournee-012 pour
                            // le SWAP, alors récupérer l’identifiant du colis
                            // retour prévu pour ce colis (dans
                            // specifsColis.infSupp("NO_LT_RETOUR"))
                            // Si il n’existe pas d’événement PC ou PE dans la
                            // tournée pour ce colis retour ni pour le colis
                            // aller, alors ajouter le point à la collection
                            // <pointAnoSWAPDSansP>
                            collection.addToPointsAnoSWAPDSansP(evtD.getIdentifiantPoint());
                            log.debug("RG-MSGetSyntTournee-023 - Add point '{}' to pointsAnoSWAPDSansP", evtD.getIdentifiantPoint());
                        } else {
                            // RG-MSGetSyntTournee-024
                            // Si un colis répond à RG-MSGetSyntTournee-012 pour
                            // le SWAP, alors récupérer l’identifiant du colis
                            // retour prévu pour ce colis (dans
                            // specifsColis.infSupp("NO_LT_RETOUR"))
                            // Si il n’existe pas d’événement PC ou PE dans la
                            // tournée pour ce colis retour mais qu’il existe un
                            // PC ou PE pour le colis aller, ajouter le point à
                            // la collection <pointAnoSWAPDEtP>
                            collection.addToPointsAnoSWAPDEtP(evtD.getIdentifiantPoint());
                            log.debug("RG-MSGetSyntTournee-024 - Add point '{}' to pointsAnoSWAPDEtP", evtD.getIdentifiantPoint());
                        }
                    }
                }

                // RG-MSGetSyntTournee-013
                // Si un colis possède une spécificité REP (Contient la
                // spécificité dans specifsColis.specifsEvenements) et qu’il a
                // un événement D sur la tournée, l’ajouter dans la collection
                // <colisAnoSpecifREP>
                if (evtD != null && ESpecificiteColis.REP.isPresentIn(specifsService)) {
                    collection.addToColisAnoSpecifREP(noLt);
                    log.debug("RG-MSGetSyntTournee-013 - Add colis '{}' to colisAnoSpecifREP", noLt);
                }

                // RG-MSGetSyntTournee-014
                // Si un colis possède une spécificité TAXE (Contient la
                // spécificité dans specifsColis.specifsEvenements) et qu’il a
                // un événement D sur la tournée, l’ajouter dans la collection
                // <colisAnoSpecifTAXE>
                if (evtD != null && ESpecificiteColis.TAXE.isPresentIn(specifsEvts)) {
                    collection.addToColisAnoSpecifTAXE(noLt);
                }

                // RG-MSGetSyntTournee-015
                // Si un colis possède une spécificité CONSIGNE (Contient la
                // spécificité dans specifsColis.specifsEvenements), l’ajouter
                // dans la collection <colisAvecConsigne>
                if (ESpecificiteColis.CONSIGNE.isPresentIn(specifsEvts)) {
                    collection.addToColisAvecConsigne(noLt);
                }

                // RG-MSGetSyntTournee-029
                // Si métropole et colis possède une spécificité TAXE ou SENSIBLE et un evt RB IP ou RG
                // l’ajouter dans la collection <colisAnoEvtNonPermis>
                if ( !estTourneeDOM && (ESpecificiteColis.TAXE.isPresentIn(specifsEvts) || ESpecificiteColis.SENSIBLE.isPresentIn(specifsEvts))) {
                	Set<ColisPoint> evtsNonPermis = getEvtsCodes(evts, "RB","RG","IP");
                	if (evtsNonPermis != null && !evtsNonPermis.isEmpty())
						for (final ColisPoint cp : evtsNonPermis) {
							collection.addToColisAnoEvtNonPermis(cp.getNo_lt());
							collection.addToPointsAnoEvtNonPermis(cp.getIdentifiantPoint());
						}
                }
                // RG-MSGetSyntTournee-030
                // Si DOM et colis possède une spécificité TAXE et un evt RG
                // l’ajouter dans la collection <colisAnoEvtNonPermis>
                if (estTourneeDOM && ESpecificiteColis.TAXE.isPresentIn(specifsEvts)) {
                	final Set<ColisPoint> evtsNonPermis = getEvtsCodes(evts, "RG");
                	if (evtsNonPermis != null && !evtsNonPermis.isEmpty())
                		for(final ColisPoint cp: evtsNonPermis){
                			collection.addToColisAnoEvtNonPermis(cp.getNo_lt());
                			collection.addToPointsAnoEvtNonPermis(cp.getIdentifiantPoint());
                		}
                }
            }

            // RG-MSGetSyntTournee-017 && RG-MSGetSyntTournee-018
            // Si un colis possède une consigne de type <RemiseBureau> ou
            // <MiseADispoAgence> et un evt de type <evt_presentation_domicile>
            // l’ajouter à la collection <colisAnoConsigneNonRepectee> et
            // ajouter le point à la collection <pointAnoConsigneNonRespectee>
            final SortedSet<ColisPoint> evtsPresentationDomicile = filterColisPoint(colisEvts.getValue(),
                    getPredicateOnlyEvts(ParametresMicroservices.EVT_PRESENTATION_DOMICILE));
            for (final ColisPoint evt : evtsPresentationDomicile) {
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null
                        && (EConsigne.REMISE_BUREAU.isEquals(consigne.get(SpecifsColisRules.CONSIGNE)) || EConsigne.REMISE_POINT_RELAIS.isEquals(consigne.get(SpecifsColisRules.CONSIGNE)) || EConsigne.MISE_A_DISPO_AGENCE.isEquals(consigne
                                .get(SpecifsColisRules.CONSIGNE)))) {
                    collection.addToColisAnoConsigneNonRespectee(noLt);
                    log.debug("RG-MSGetSyntTournee-017/018 - Add colis '{}' to colisAnoConsigneNonRespectee", noLt);

                    collection.addToPointsAnoConsigneNonRespectee(evt.getIdentifiantPoint());
                    log.debug("RG-MSGetSyntTournee-017/018 - Add point '{}' to pointsAnoConsigneNonRespectee", evt.getIdentifiantPoint());
                }
            }

            final SortedSet<ColisPoint> evtsMiseADispoBureau = filterColisPoint(evts, getPredicateOnlyEvts(ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU));
            final SortedSet<ColisPoint> evtEchecLivraison = filterColisPoint(evts, getPredicateOnlyEvts(ParametresMicroservices.EVT_ECHEC_LIVRAISON));
            for (final ColisPoint evt : evtsMiseADispoBureau) {
                // RG-MSGetSyntTournee-019
                // Si un colis possède une consigne de type <RemiseTiers> et un
                // evt de type <evt_mise_a_disposition_bureau> l’ajouter à la
                // collection <colisAnoConsigneNonRepectee> et ajouter le point
                // à la collection <pointAnoConsigneNonRespectee>
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null && EConsigne.REMISE_TIERS.isEquals(consigne.get(SpecifsColisRules.CONSIGNE))) {
                    collection.addToColisAnoConsigneNonRespectee(noLt);
                    collection.addToPointsAnoConsigneNonRespectee(evt.getIdentifiantPoint());
                }

                // RG-MSGetSyntTournee-022
                // si codeEvenement appartient à param.
                // evt_mise_a_disposition_bureau alors
                // si le colis a eu un événement evt_echec_livraison ajouter le
                // point à la collection <pointAnoMadNonPermise>
                if (!evtEchecLivraison.isEmpty()) {
                    collection.addToPointsAnoMadNonPermise(evt.getIdentifiantPoint());
                }
            }

            // RG-MSGetSyntTournee-020
            // Si un colis possède l’anomalie TRACEVTNONPERMIS l’ajouter à la
            // collection <colisAnoEvtNonPermis> et ajouter le point à la
            // collection <pointAnoEvtNonPermis>
            for (final ColisPoint evt : colisEvts.getValue()) {
                if (evt.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode())) {
                    collection.addToColisAnoEvtNonPermis(noLt);
                    collection.addToPointsAnoEvtNonPermis(evt.getIdentifiantPoint());
                }
            }

            // RG-MSGetSyntTournee-026
            // Si un colis a comme dernier evénement un evt TA 
            // ajouter le point à la collection <pointAvecColisRetour>
            if(dernierCodeEvt.contains(EVT_TA)) {
                collection.addToPointAvecColisRetour(dernierEvt.getIdentifiantPoint());
                log.debug("RG-MSGetSyntTournee-026 - Add point '{}' to pointAvecColisRetour", dernierEvt.getIdentifiantPoint());
            }
            
			// RG-MSGetSyntTournee-027
			// Si le colis a eu un evt de retour agence sur la tournée
			// Ajouter le colis à la collection <colisVusEnRetour>
			if (SpecifsColisRules.retourAgence(specifsColis, dernierEvt.getDateEvt()).size() > 0) {
				collection.addToColisVusEnRetour(dernierEvt.getNo_lt());
				log.debug("RG-MSGetSyntTournee-027 - Add colis '{}' to colisVusEnRetour", dernierEvt.getNo_lt());
			}
		}
		return collection;
	}

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.
     * SyntheseTourneeService
     * #calculAnomaliesTournee(com.chronopost.vision.microservices
     * .tournee.getsynthesetournees.v1.Tournee)
     */
    @Override
    public List<PointTournee> calculAnomaliesTournee(final Tournee tournee) {
    	final boolean estTourneeDOM = TourneeRules.isTourneeDOM(tournee.getIdentifiantTournee());
    	final int depassement_max_eta = Integer.parseInt(TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.DEPASSEMENT_MAX_ETA.toString()));
        final int depassement_min_eta = Integer.parseInt(TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.DEPASSEMENT_MIN_ETA.toString()));
        final String evtPresentationNegative = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.EVT_PRESENTATION_NEGATIVE.toString());
        final String evtMiseADispositionBureau = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU.toString());

        final Multimap<String, EAnomalie> pointsEnAnomalies = HashMultimap.create();

        /* Parcours des colis (avec leur liste d'événements sur la tournée) */
        for (final Map.Entry<String, SortedSet<ColisPoint>> colisEvts : tournee.getColisEvenements().entrySet()) {
        	final SortedSet<ColisPoint> colisPoints = colisEvts.getValue();
        	/* Dernier evt D */
			final ColisPoint evtD = getEvtD(colisPoints);
            /* Permier evt D+  */
            final ColisPoint premierEvtDPlus = getPremierEvtDPlus(colisPoints);
            /* Dernier evt (non TE) */
            final ColisPoint dernierEvt = getDernierEvtColis(colisPoints);
            /* Dernier evt de présentation domicile négatif */
            final ColisPoint dernierEvtPresentationDomicileNegatif = getDernierEvtPresentationDomicileNegative(colisPoints);
            /* Dernier evt de présentation négatif (domicile ou non) */
            final ColisPoint dernierEvtPresentationNegatif = getDernierEvtPresentationNegative(colisPoints);
            /* Le specifColis du colis */
            final SpecifsColis specifsColis = tournee.getColisSpecifs().get(colisEvts.getKey());
            /* Le dernier evt TA du colis sur la tournée s'il existe */
            final ColisPoint evtTA = getEvtTA(colisPoints);
            
            // RG-MSGetSyntTournee-504
            // Pour chaque ColisPoint de Tournee.colisEvenements
            // si le dernier événement du colis (hors evt TE) est un événement
            // de présentation infructueuse
            // (param.evt_presentation_domicile_negative)
            // alors
            // ajouter au PointTournee.anomalies "RETPRESINF"
            // ajouter au ColisPoint.anomalies "RETPRESINF"
            if (dernierEvt != null && evtPresentationNegative.contains("|" + dernierEvt.getCodeEvenement() + "|")) {
                dernierEvt.addToAnomalies(EAnomalie.RET_PRES_INF);
                pointsEnAnomalies.put(dernierEvt.getIdentifiantPoint(), EAnomalie.RET_PRES_INF);
            }

            // RG-MSGestSyntTournee-505
            // Pour chaque ColisPoint de Tournee.colisEvenements
            // si le dernier événement du colis (hors evt TE) est un TA (evt de
            // départ en tournée)
            // ajouter au PointTournee.anomalies "RETNONSAISI"
            // ajouter au ColisPoint.anomalies "RETNONSAISI"
            if (dernierEvt != null && EVT_TA.equals(dernierEvt.getCodeEvenement())) {
                // RG-MSGestSyntTournee-532
               	// Pour chaque Colis dont dern evt sur la tournée = TA
               	// Si le colis a eu un evt de retour agence
               	// positionner l’attribut vueEnRetour à Date|CodeEvt
                if (specifsColis != null) {
                	final Map<Date, String> retourAgence = SpecifsColisRules.retourAgence(specifsColis, dernierEvt.getDateEvt());
                	if (!retourAgence.isEmpty()) {
                		dernierEvt.setVueEnRetour(retourAgence);
                	}
                }
                
                // RG-MSGetSyntTournee-535
            	// Si la TA est suivie d'un BL dans la meme journée, on ajoute l'anomalie BL
				final DateTime finJourTA = new DateTime(dernierEvt.getDateEvt()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
				final SortedMap<Date, String> etapesInPeriode = SpecifsColisRules.getEtapesInPeriode(specifsColis, dernierEvt.getDateEvt(), finJourTA.toDate());
				final ImmutableSortedMap<Date, String> exclusions = SpecifsColisRules.getEtapesFromType(etapesInPeriode, EEtapesColis.EXCLUSION);
				final Iterator<Entry<Date, String>> iterator = exclusions.entrySet().iterator();
				while (iterator.hasNext()) {
					final Entry<Date, String> next = iterator.next();
					// si étape commence par EXCLUSION|BL
					final String codeEvt = SpecifsColisRules.extractCodeEvtFromEtape(next.getValue());
					if (EAnomalie.BL.getCode().equals(codeEvt)) {
						dernierEvt.addToAnomalies(EAnomalie.BL);
						pointsEnAnomalies.put(dernierEvt.getIdentifiantPoint(), EAnomalie.BL);
					}
				}
				if (!dernierEvt.getAnomalies().contains(EAnomalie.BL.getCode())) {
					dernierEvt.addToAnomalies(EAnomalie.RET_NON_SAISI);
	                pointsEnAnomalies.put(dernierEvt.getIdentifiantPoint(), EAnomalie.RET_NON_SAISI);
				}
            }

            // RG-MSGestSyntTournee-506
            // Pour chaque ColisPoint de Tournee.colisEvenements
            // si le dernier événement du colis (hors evt TE) est un événement
            // de mise à diposition bureau (param.evt_mise_a_disposition_bureau)
            // alors
            // si le colis nécessitait une présentation
            // (RG-MSGetSyntTournee-507)
            // ajouter au PointTournee.anomalies "MADPRES"
            // ajouter au ColisPoint.anomalies "MADPRES"
            // sinon
            // ajouter au PointTournee.anomalies "MADSANSPRES"
            // ajouter au ColisPoint.anomalies "MADSANSPRES"
            if (dernierEvt != null && evtMiseADispositionBureau.contains("|" + dernierEvt.getCodeEvenement() + "|")) {
                if (SpecifsColisRules.aMettreEnInstance(specifsColis, dernierEvt.getDateEvt()) == false) {
                    dernierEvt.addToAnomalies(EAnomalie.MAD_PRES);
                    pointsEnAnomalies.put(dernierEvt.getIdentifiantPoint(), EAnomalie.MAD_PRES);
                } else {
                    dernierEvt.addToAnomalies(EAnomalie.MAD_SANS_PRES);
                    pointsEnAnomalies.put(dernierEvt.getIdentifiantPoint(), EAnomalie.MAD_SANS_PRES);
                }
            }

            if (dernierEvtPresentationDomicileNegatif != null) {
                final String idAvisPassage = dernierEvtPresentationDomicileNegatif.getInfoSupplementaire(ID_AVIS_PASSAGE);
                final Date datePresentationDomicileNegative = dernierEvtPresentationDomicileNegatif.getDateEvt();
                final String codeEvtPresentationDomicileNegative = dernierEvtPresentationDomicileNegatif.getCodeEvenement();

                // Récupère tous les événements de mise à dispo bureau suivant
                // le dernier événement présentation domicile
                final SortedSet<ColisPoint> evtsSuivantPresentationDocimile = colisPoints.tailSet(dernierEvtPresentationDomicileNegatif);
                final SortedSet<ColisPoint> evtsMiseADispoBureau = filterColisPoint(evtsSuivantPresentationDocimile,
                        getPredicateOnlyEvts(ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU));

                for (final ColisPoint evtMiseADispoBureau : evtsMiseADispoBureau) {
                    // RG-MSGestSyntTournee-508
                    // Pour chaque Colis de Tournee.colisEvenements considérer
                    // le dernier évt de présentation infructueuse du colis
                    // (param evt_presentation_domicile_negative) s’il existe
                    // récupérer l’identifiant de l’avis de passage
                    // (ID_AVIS_PASSAGE dans infosSupp) dans tous les evts de
                    // dépot bureau suivant du colis, ajouter l’info
                    // supplémentaire ID_AVIS_PASSAGE récupérée.
                    if (idAvisPassage != null) {
                        evtMiseADispoBureau.putToInfosSupplementaires(ID_AVIS_PASSAGE, idAvisPassage);
                    }

                    // RG-MSGestSyntTournee-509
                    // Pour chaque Colis de Tournee.colisEvenements considérer
                    // le dernier évt de présentation infructueuse du colis
                    // (param evt_presentation_domicile_negative) s’il existe
                    // récupérer les attributs dateEvt et codeEvenement dans
                    // tous les evts de dépot bureau suivant du colis, ajouter
                    // dans presentationEffectuee le couple dateEvt ;
                    // codeEvenement récupérés
                    if (datePresentationDomicileNegative != null && codeEvtPresentationDomicileNegative != null) {
                        evtMiseADispoBureau.putToPresentationEffectuee(datePresentationDomicileNegative, codeEvtPresentationDomicileNegative);
                    }
                }
            }
            
            if (dernierEvtPresentationNegatif != null) {
            	// RG-MSGestSyntTournee-510
                // Pour chaque Colis de Tournee.colisEvenements considérer le
                // dernier évt de présentation infructueuse du colis (param
                // evt_presentation_negative) s’il existe vérifier si
                // le colis a eu un evt de retour agence
                // (RG-MSGetSyntTournee-511) positionner l’attribut vueEnRetour
                // à true si le colis a eu un evt de retour agence sinon le
                // positionner à false.
                final Date datePresentationNegative = dernierEvtPresentationNegatif.getDateEvt();
                final Map<Date, String> retourAgence = SpecifsColisRules.retourAgence(specifsColis, datePresentationNegative);
                if (!retourAgence.isEmpty()) {
                	dernierEvtPresentationNegatif.setVueEnRetour(retourAgence);
                }
            }

            if (evtD != null) {
                // RG-MSGetSyntTournee-518
                // Si le ColisPoint possède la spécificité TAXE alors, si dans
                // les spécificités du colis il existe une valeur pour la clé
                // “TAXE_VALEUR” de infosSupplementaires, copier cette valeur
                // dans ColisPoint.infosSupplementaires(“TAXE_VALEUR”)
                final Date dateCaracteristiqueTaxe = SpecifsColisRules.containsCaracteristique(specifsColis, evtD.getDateEvt(), ESpecificiteColis.TAXE);
                if (dateCaracteristiqueTaxe != null && specifsColis != null) {
                    final String taxeValeur = specifsColis.getInfoSupp().get(EInfoSupplementaire.TAXE_VALEUR.getCode());
                    if (taxeValeur != null) {
                        evtD.putToInfosSupplementaires(EInfoSupplementaire.TAXE_VALEUR.getCode(), taxeValeur);
                    }
                }
                
                // RG-MSGetSyntTournee-523 && RG-MSGetSyntTournee-524 &&
                // RG-MSGetSyntTournee-525
                // Si un colis répond à RG-MSGetSyntTournee-515 pour le SWAP,
                // alors récupérer l’identifiant du colis retour prévu pour ce
                // colis dans specifsColis.infSupp("NO_LT_RETOUR")
                final Date dateCaracteristiqueSwap = SpecifsColisRules.containsCaracteristique(specifsColis, evtD.getDateEvt(), ESpecificiteColis.SWAP);
                if (dateCaracteristiqueSwap != null && specifsColis != null) {
                    final String noLtRetour = specifsColis.getInfoSupp().get(EInfoSupplementaire.NO_LT_RETOUR.getCode());
                    final SortedSet<ColisPoint> evtsColisRetour = tournee.getColisEvenements(noLtRetour);
                    final SortedSet<ColisPoint> evtsColisAllerPCOuPE = filterColisPoint(colisPoints, getPredicateOnlyEvts(EVT_PC, EVT_PE));
                    final SortedSet<ColisPoint> evtsColisRetourPCOuPE = filterColisPoint(evtsColisRetour, getPredicateOnlyEvts(EVT_PC, EVT_PE));

                    // RG-MSGetSyntTournee-523
                    // Si il n’existe pas d’événement PC ou PE dans la tournée
                    // pour ce colis retour ni pour le colis aller, alors
                    // ajouter au PointTournee.anomalies la String
                    // "SPECSWAPDSANSP"
                    // ajouter au ColisPoint.anomalies la String
                    // "SPECSWAPDSANSP"
                    if (evtsColisRetourPCOuPE.isEmpty() && evtsColisAllerPCOuPE.isEmpty()) {
                        evtD.addToAnomalies(EAnomalie.SPEC_SWAP_D_SANS_P);
                        pointsEnAnomalies.put(evtD.getIdentifiantPoint(), EAnomalie.SPEC_SWAP_D_SANS_P);
                    }

                    // RG-MSGetSyntTournee-524
                    // Si il n’existe pas d’événement PC ou PE dans la tournée
                    // pour ce colis retour mais qu’il existe un PC ou PE pour
                    // le colis aller, alors
                    // ajouter au PointTournee.anomalies la String"SPECSWAPDETP"
                    // ajouter au ColisPoint.anomalies la String "SPECSWAPDETP"
                    else if (evtsColisRetourPCOuPE.isEmpty() && !evtsColisAllerPCOuPE.isEmpty()) {
                        evtD.addToAnomalies(EAnomalie.SPEC_SWAP_D_ET_P);
                        pointsEnAnomalies.put(evtD.getIdentifiantPoint(), EAnomalie.SPEC_SWAP_D_ET_P);
                    }

                    // RG-MSGetSyntTournee-525
                    // Si il existe un événement PC ou PE dans la tournée pour
                    // ce colis retour, alors
                    // ajouter au PointTournee.anomalies la String
                    // "SPECSWAPOK"
                    // ajouter au ColisPoint.anomalies la String "SPECSWAPOK"
                    else if (!evtsColisRetourPCOuPE.isEmpty()) {
                        evtD.addToAnomalies(EAnomalie.SPEC_SWAP_OK);
                        pointsEnAnomalies.put(evtD.getIdentifiantPoint(), EAnomalie.SPEC_SWAP_OK);
                    }
                }
                
                //RG-MSGetSyntTournee-527
                //Si un colis possède la spécificité REP et que son dernier
                //événement est un événement de livraison D
                //ajouter au PointTournee.anomalies la String “SPECREP”
                //ajouter au ColisPoint.anomalies la String “SPECREP”
                if(dernierEvt != null && EVT_D.equals(dernierEvt.getCodeEvenement()) &&
                		SpecifsColisRules.containsCaracteristique(specifsColis, evtD.getDateEvt(), ESpecificiteColis.REP)!=null) {
                	evtD.addToAnomalies(EAnomalie.SPEC_REP);
                    pointsEnAnomalies.put(evtD.getIdentifiantPoint(), EAnomalie.SPEC_REP);
                }
                
                //RG-MSGetSyntTournee-528
                //Si un colis possède la spécificité TAXE et que son dernier 
                //événement est un événement de livraison D
                //ajouter au PointTournee.anomalies la String “SPECTAXE”
                //ajouter au ColisPoint.anomalies la String “SPECTAXE”
                if(dernierEvt != null && EVT_D.equals(dernierEvt.getCodeEvenement()) &&
                		SpecifsColisRules.containsCaracteristique(specifsColis, evtD.getDateEvt(), ESpecificiteColis.TAXE)!=null) {
                	evtD.addToAnomalies(EAnomalie.SPEC_TAXE);
                    pointsEnAnomalies.put(evtD.getIdentifiantPoint(), EAnomalie.SPEC_TAXE);
                }
            }

            if (premierEvtDPlus != null) {
                // RG-MSGestSyntTournee-513
                // Pour chaque Colis de Tournee.colisEvenements considérer le
                // 1er évt evt_Dplus
                // si heure evt > heureContractuelle(colisSpecif,dateEvt) alors
                // ajouter au PointTournee.anomalies la String "HDCONTRAT"
                // ajouter au ColisPoint.anomalies la String "HDCONTRAT"
				String creneauDebut, creneauFin;
            	// RG-MSGestSyntTournee-533 cas d'une TA comportant un créneau
            	if   (evtTA != null // TA présent
            		&& "RDV".equals(evtTA.getPrecocite()) // precocité RDV 
            		&& (creneauDebut = evtTA.getInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT)) != null // créneau debut défini 
            		&& (creneauFin = evtTA.getInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN)) != null) { // créneau fin défini

            		// En retard ou non, on reporte le créneau sur le 1er evt D+
            		premierEvtDPlus.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,creneauDebut);
                    premierEvtDPlus.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,creneauFin);

            		if (DateRules.estHorsCreneauHoraire(creneauDebut, creneauFin, premierEvtDPlus.getDateEvt())) {  // Si 1er D+ hors creneau
            			premierEvtDPlus.addToAnomalies(EAnomalie.HD_CONTRAT);
                        pointsEnAnomalies.put(premierEvtDPlus.getIdentifiantPoint(), EAnomalie.HD_CONTRAT);
            		}
				} else if (SpecifsColisRules.estEvtDPlusHorsDelai(premierEvtDPlus, specifsColis, colisEvts)) {
					premierEvtDPlus.addToAnomalies(EAnomalie.HD_CONTRAT);
					pointsEnAnomalies.put(premierEvtDPlus.getIdentifiantPoint(), EAnomalie.HD_CONTRAT);
				}
            	
            	// RG-MSGetSyntTournee-503
            	// Pour chaque premier evt D+ de chaque colis
                //     Si diffETA> depassement_max_eta 
            	//     ou diffETA<depassement_min_eta alors 
            	//         ajouter au PointTournee.anomalies la String “HDETA”
            	if (premierEvtDPlus.getDiffETA() != null && (premierEvtDPlus.getDiffETA() > depassement_max_eta || premierEvtDPlus.getDiffETA() < depassement_min_eta)) {
                  pointsEnAnomalies.put(premierEvtDPlus.getIdentifiantPoint(), EAnomalie.HD_ETA);
            	}
            }

            // RG-MSGetSyntTournee-519
            // Si un colis possède une consigne de type <RemiseBureau> 
            // et un evt de type <evt_presentation_domicile>
            // ajouter au PointTournee.anomalies la String “TRACCONSIGNEKO”
            // ajouter au ColisPoint.anomalies la String “TRACCONSIGNEKO”
            final SortedSet<ColisPoint> evtsPresentationDomicile = filterColisPoint(colisPoints,
                    getPredicateOnlyEvts(ParametresMicroservices.EVT_PRESENTATION_DOMICILE));
            for (final ColisPoint evt : evtsPresentationDomicile) {
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null
                        && (EConsigne.REMISE_BUREAU.isEquals(consigne.get(SpecifsColisRules.CONSIGNE)) || EConsigne.REMISE_POINT_RELAIS.isEquals(consigne.get(SpecifsColisRules.CONSIGNE)))) {
                    evt.addToAnomalies(EAnomalie.TRAC_CONSIGNE_KO);
                    pointsEnAnomalies.put(evt.getIdentifiantPoint(), EAnomalie.TRAC_CONSIGNE_KO);
                }
            }

            // RG-MSGetSyntTournee-520
            // Si un colis possède une consigne de type <MiseADispoAgence> 
            // et un evt de type <evt_presentation>
            // ajouter au PointTournee.anomalies la String “TRACCONSIGNEKO”
            // ajouter au ColisPoint.anomalies la String “TRACCONSIGNEKO”
            final SortedSet<ColisPoint> evtsPresentation = filterColisPoint(colisPoints,
                    getPredicateOnlyEvts(ParametresMicroservices.EVT_D_PLUS));
            for (final ColisPoint evt : evtsPresentation) {
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null
                        && (EConsigne.MISE_A_DISPO_AGENCE.isEquals(consigne.get(SpecifsColisRules.CONSIGNE)))) {
                    evt.addToAnomalies(EAnomalie.TRAC_CONSIGNE_KO);
                    pointsEnAnomalies.put(evt.getIdentifiantPoint(), EAnomalie.TRAC_CONSIGNE_KO);
                }
            }

            // RG-MSGetSyntTournee-521
            // Si un colis possède une consigne de type <RemiseTiers> et un
            // evt de type <evt_mise_a_disposition_bureau>
            // ajouter au PointTournee.anomalies la String “TRACCONSIGNEKO”
            // ajouter au ColisPoint.anomalies la String “TRACCONSIGNEKO”
            final SortedSet<ColisPoint> evtsMiseADispoBureau = filterColisPoint(colisPoints,
                    getPredicateOnlyEvts(ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU));
            final boolean echecLivraison = !filterColisPoint(colisPoints, getPredicateOnlyEvts(ParametresMicroservices.EVT_ECHEC_LIVRAISON)).isEmpty();
            for (final ColisPoint evt : evtsMiseADispoBureau) {
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null && EConsigne.REMISE_TIERS.isEquals(consigne.get(SpecifsColisRules.CONSIGNE))) {
                    evt.addToAnomalies(EAnomalie.TRAC_CONSIGNE_KO);
                    pointsEnAnomalies.put(evt.getIdentifiantPoint(), EAnomalie.TRAC_CONSIGNE_KO);
                }

                // RG-MSGetSyntTourne-517
                // Pour chaque PointTournee
                // Pour chaque ColisPoint
                // si codeEvenement appartient à param
                // evt_mise_a_disposition_bureau
                // alors si le colis a eu un événement evt_echec_livraison
                // ajouter au PointTournee.anomalies la String "MADNONPERMISE"
                // ajouter au ColisPoint.anomalies la String "MADNONPERMISE"
                if (echecLivraison) {
                    evt.addToAnomalies(EAnomalie.MAD_NON_PERMISE);
                    pointsEnAnomalies.put(evt.getIdentifiantPoint(), EAnomalie.MAD_NON_PERMISE);
                }
            }

            // RG-MSGetSyntTournee-522
            // Si un colis possède un l’anomalie TRACEVTNONPERMIS, ajouter
            // l’anomalie au PointTournee.anomalies
            for (final ColisPoint evt : colisPoints) {
                if (evt.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode())) {
                    pointsEnAnomalies.put(evt.getIdentifiantPoint(), EAnomalie.EVT_NON_PERMIS);
                }
                //
                // RG-MSGetSyntTournee-526
                // Si un colis possède une consigne à la date de l’evt récupérer
                // l’id de la consigne et la consigne (via
                // specifscolisrules.consigneCaracteristique) et la placer dans
                // infosSupplementaires(‘ID_CONSIGNE’) =  (idConsigne!=null && idConsigne!="null" && idConsigne!="") ? idConsigne : "0",
                // infosSupplementaires(‘CONSIGNE’) = consigne
                final Map<String, String> consigne = SpecifsColisRules.consigneCaracteristique(specifsColis, evt.getDateEvt());
                if (consigne != null) {
                    evt.putToInfosSupplementaires(CONSIGNE, consigne.get(SpecifsColisRules.CONSIGNE));
                    evt.putToInfosSupplementaires(ID_CONSIGNE, getIdConsigne(consigne.get(SpecifsColisRules.ID_CONSIGNE)) );
                }
            }

			if (dernierEvt != null) {
            	// RG-MSGetSyntTournee-529
            	// Pour la métropole si un colis possède la spécificité TAXE ou SENSIBLE et qu’il possède un evt RB RG ou IP sur la tournée
            	// ajouter au PointTournee.anomalies la String “TRACEVTNONPERMIS”
            	// ajouter au ColisPoint.anomalies   la String “TRACEVTNONPERMIS”
            	final Date dateCaracteristiqueTaxe = SpecifsColisRules.containsCaracteristique(specifsColis, dernierEvt.getDateEvt(), ESpecificiteColis.TAXE);
            	final Date dateCaracteristiqueSensible = SpecifsColisRules.containsCaracteristique(specifsColis, dernierEvt.getDateEvt(), ESpecificiteColis.SENSIBLE);
            	if (!estTourneeDOM && (dateCaracteristiqueTaxe != null || dateCaracteristiqueSensible != null)) {
            		final Set<ColisPoint> evtNonPermis = getEvtsCodes(colisEvts.getValue(),"RB","RG","IP");
            		if (containsEvtsCodes(colisEvts.getValue(),"RB","RG","IP")){
            			if (evtNonPermis != null && !evtNonPermis.isEmpty()) {
            				for(ColisPoint cp: evtNonPermis){
            					cp.addToAnomalies(EAnomalie.EVT_NON_PERMIS);
            					pointsEnAnomalies.put(cp.getIdentifiantPoint(), EAnomalie.EVT_NON_PERMIS);
            				}
            			}
            		}
            	}
            	// RG-MSGetSyntTournee-530
            	// Pour les DOM si un colis possède la spécificité TAXE et qu’il possède un evt RG sur la tournée
            	if ( estTourneeDOM && dateCaracteristiqueTaxe != null) {
            		final Set<ColisPoint> evtNonPermis = getEvtsCodes(colisEvts.getValue(),"RG");
            		if (evtNonPermis != null && !evtNonPermis.isEmpty()) {
            			for(ColisPoint cp: evtNonPermis){
            				cp.addToAnomalies(EAnomalie.EVT_NON_PERMIS);
            				pointsEnAnomalies.put(cp.getIdentifiantPoint(), EAnomalie.EVT_NON_PERMIS);
            			}
            		}
            	}
            }
        } // Fin de la boucle sur les colis

        int numPointDistri = 1;
        for (final PointTournee point : tournee.getPoints()) {
            // Maj num point distri si date passage non null
            if (point.getDatePassage() != null) {
                point.setNumPointDistri(numPointDistri++);
            }

            //
//            if (point.getDiffETA() != null && (point.getDiffETA().intValue() > depassement_max_eta || point.getDiffETA().intValue() < depassement_min_eta)) {
//                point.addToAnomalies(EAnomalie.HD_ETA);
//            }

            // RG-MSGetSyntTourne-504 & 505 & 506
            // Ajout des anomalies dans PointTournee.anomalies
            if (pointsEnAnomalies.containsKey(point.getIdentifiantPoint())) {
                point.addAllToAnomalies(pointsEnAnomalies.get(point.getIdentifiantPoint()));
            }
        }

        return tournee.getPoints();
    }

    /**
     * 
     * @param points
     *            une liste de ColisPoint
     * @return le dernier événement présentation domicile négative
     */
	private ColisPoint getDernierEvtPresentationDomicileNegative(final SortedSet<ColisPoint> points) {
		final SortedSet<ColisPoint> filteredCollection = filterColisPoint(points,
				getPredicateOnlyEvts(ParametresMicroservices.EVT_PRESENTATION_DOMICILE_NEGATIVE));
		// Si la liste filtré n'est pas vide on récupère le dernier ColisPoint
		if (!filteredCollection.isEmpty()) {
			return filteredCollection.last();
		}
		return null;
	}
    
    /**
     * 
     * @param points
     *            une liste de ColisPoint
     * @return le dernier événement présentation domicile négative
     */
	private ColisPoint getDernierEvtPresentationNegative(final SortedSet<ColisPoint> points) {
		final SortedSet<ColisPoint> filteredCollection = filterColisPoint(points,
				getPredicateOnlyEvts(ParametresMicroservices.EVT_PRESENTATION_NEGATIVE));
		// Si la liste filtré n'est pas vide on récupère le dernier ColisPoint
		if (!filteredCollection.isEmpty()) {
			return filteredCollection.last();
		}
		return null;
	}

    private SortedSet<ColisPoint> filterColisPoint(final SortedSet<ColisPoint> set, final Predicate<ColisPoint> predicate) {
        // Filtre la liste pour ne conserver que les ColisPoint avec un code
        // événement présent dans codeEvts
        // !!! ATTENTION !!! Il faut créer une copy du Set filtré, sinon il y a
        // un risque de blocage lors de son parcours (.last(), boucle, etc.)
        return Sets.newTreeSet(Iterables.filter(set, predicate));
    }

    private Predicate<ColisPoint> getPredicateExcludeEvts(final String... codeEvts) {
        final Set<String> filterValues = Sets.newHashSet(codeEvts);

        return new Predicate<ColisPoint>() {
            public boolean apply(ColisPoint colis) {
                return !filterValues.contains(colis.getCodeEvenement());
            }
        };
    }

    /**
     * Création d'une méthode de filtrage d'evts selon des codes evts
     * 
     * @param codeEvts : codes évts recherchés
     * @return une méthode de filtrage adéquat 
     *
     * @author LGY
     */
    private Predicate<ColisPoint> getPredicateOnlyEvts(final String... codeEvts) {
        final Set<String> filterValues = Sets.newHashSet(codeEvts);

        return new Predicate<ColisPoint>() {
            public boolean apply(ColisPoint colis) {
                return filterValues.contains(colis.getCodeEvenement());
            }
        };
    }

    private Predicate<ColisPoint> getPredicateOnlyEvts(final ParametresMicroservices parametre) {
        final String filterValues = TranscoderService.INSTANCE.getTranscoder(DIFFUSION_VISION)
                .transcode(PARAMETRE_MICROSERVICES, parametre.toString());

        return new Predicate<ColisPoint>() {
            public boolean apply(ColisPoint colis) {
                final String codeEvenement = String.format("|%s|", colis.getCodeEvenement());
                return filterValues.contains(codeEvenement);
            }
        };
    }

    /**
     * Trouve le dernier evt qui n'est pas TE
     * 
     * @param colis
     *            <<<<<<< HEAD la liste des événements d'un colis triés par date
     *            d'événement
     * @return le dernier ColisPoint de la liste qui n'a pas d'évènement TE
     *         ======= une liste de ColisPoint
     * @return le dernier ColisPoint de la liste qui n'a pas d'évènement TE, ou
     *         ColisPoint.NONE si aucun événement
     * 
     *         >>>>>>> feature/VIS-63_getSyntheseLOT4
     */
    private ColisPoint getDernierEvtColis(final SortedSet<ColisPoint> colisPoints) {
        ColisPoint dernierColis = ColisPoint.NONE;
        final SortedSet<ColisPoint> evts = filterColisPoint(colisPoints, getPredicateExcludeEvts(EVT_TE));

        if (!evts.isEmpty()) {
            dernierColis = evts.last();
        }

        return dernierColis;
    }

    /**
     * Trouve le premier événement D+ d'un colis
     * 
     * @param colisPoints
     *            la liste des événements d'un colis triés par date d'événement
     * @return le premier événement D+ d'un colis
     */
	private ColisPoint getPremierEvtDPlus(final SortedSet<ColisPoint> colisPoints) {
		final SortedSet<ColisPoint> evts = filterColisPoint(colisPoints, getPredicateOnlyEvts(ParametresMicroservices.EVT_D_PLUS));
		if (!evts.isEmpty()) {
			return evts.first();
		}
		return null;
	}

    /**
     * Trouve le premier événement d'un colis
     * 
     * @param colisPoints
     *            la liste des événements d'un colis triés par date d'événement
     * @return le premier événement d'un colis
     */
    private ColisPoint getPremierEvtColis(final SortedSet<ColisPoint> colisPoints) {
        if (colisPoints != null && !colisPoints.isEmpty()) {
            return colisPoints.first();
        }
        return null;
    }

    /**
     * 
     * @param colisPoints
     * @return Le dernier événement D rencontré 
     * 
     *
     * @author LGY
     */
    private ColisPoint getEvtD(final SortedSet<ColisPoint> colisPoints) {
        final SortedSet<ColisPoint> evts = filterColisPoint(colisPoints, getPredicateOnlyEvts(EVT_D));
        if (!evts.isEmpty()) {
            return evts.last();
        }
        return null;
    }
    
    /**
     * 
     * @param colisPoints
     * @return Le dernier événement TA rencontré 
     * 
     *
     * @author LGY
     */
    private ColisPoint getEvtTA(final SortedSet<ColisPoint> colisPoints) {
        final SortedSet<ColisPoint> evts = filterColisPoint(colisPoints, getPredicateOnlyEvts(EVT_TA));
        if (!evts.isEmpty()) {
            return evts.last();
        }
        return null;
    }
    
    /**
     * 
     * @param colisPoints : le colisPoint à analyser
     * @param evtsCodes : liste des codes événements recherché
     * @return true si le colisPoint contient un des codes événements fournis
     *
     * @author LGY
     */
    private boolean containsEvtsCodes(final SortedSet<ColisPoint> colisPoints, final String... evtsCodes) {
        final SortedSet<ColisPoint> evts = filterColisPoint(colisPoints, getPredicateOnlyEvts(evtsCodes));
        if (!evts.isEmpty()) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param colisPoints : le colisPoint à analyser
     * @param evtsCodes : liste des codes événements recherchés
     * @return une liste de colis contenant les evts recherchés
     *
     * @author LGY
     */
    private Set<ColisPoint> getEvtsCodes(final SortedSet<ColisPoint> colisPoints, final String... evtsCodes) {
        return filterColisPoint(colisPoints, getPredicateOnlyEvts(evtsCodes));
    }
    
    /**
     * Declare un appel au MS
     * @param nbTrt
     * @param NbFail
     */
    public void declareAppelMS(){
    	if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    		dao.updateCptHitMS();
    	}
    }

    public void declareFailMS(){
    	if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    		dao.updateCptFailMS();
    	}
    }
    
	/**
	 * Renvoie l'idConsigne si different de null, la chaîne de caractère "null" et du ""(vide) sinon
	 * renvoie la chaîne de caractère zéro ("0")
	 * 
	 * @param idConsigne
	 * @return
	 */
	private String getIdConsigne(final String idConsigne) {
		return (StringUtils.isNotBlank(idConsigne) && !idConsigne.equals("null") )? idConsigne : "0";
	}

}
