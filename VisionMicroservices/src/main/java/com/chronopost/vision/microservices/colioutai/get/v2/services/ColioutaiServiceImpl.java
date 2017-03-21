package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.colioutai.get.v2.commands.InitServiceConsigneCommand;
import com.chronopost.vision.microservices.colioutai.get.v2.commands.InitServicePoiCommand;
import com.chronopost.vision.microservices.colioutai.get.v2.commands.InitServicePtvCommand;
import com.chronopost.vision.microservices.colioutai.get.v2.commands.ServiceConsigneCommand;
import com.chronopost.vision.microservices.colioutai.get.v2.commands.ServiceGoogleCommand;
import com.chronopost.vision.microservices.colioutai.get.v2.commands.ServicePoiCommand;
import com.chronopost.vision.microservices.colioutai.log.v2.dao.ColioutaiLogDao;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.insertevt.v1.commands.InitCalculRetardCommand;
import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.microservices.sdk.exception.ServiceUnavailableException;
import com.chronopost.vision.microservices.utils.SecurityUtil;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.IndiceConfiance;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;
import com.chronopost.vision.model.TypeCreneau;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.v2.ColioutaiLog;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.rules.LtRules;

import fr.chronopost.soap.calculretard.cxf.CalculRetardWS;
import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.ResultInformationsConsigne;

/**
 * Implementation du service
 * 
 * @author vdesaintpern
 *
 */
public class ColioutaiServiceImpl implements ColioutaiService {

	private final static Logger logger = LoggerFactory.getLogger(ColioutaiServiceImpl.class);

	/**
	 * Geocoder google
	 */
	private GoogleGeocoderHelper googleGeocoderHelper;

	private PoiGeocoderHelper poiGeocoderHelper;

	private PTVHelperInterface ptvHelper;


	/**
	 * Service GETLtV1
	 */
	private GetLtV1 getLTV1;

	private GetDetailTourneeV1 detailTourneeV1;

	private GetCodeTourneeFromLtV1 codeTourneeV1;

	private ConsigneServiceWS serviceConsigne;

	private String serviceConsigneEndpoint;

	private String poiServiceEndpoint;

	private String ptvServiceEndpoint;

	private ColioutaiLogDao dao;
	
	private String calculRetardEndpoint;

	private CalculRetardWS calculRetardClient;
	
	public ColioutaiService setCalculRetardEndpoint(final String calculRetardEndpoint) {
		this.calculRetardEndpoint = calculRetardEndpoint;
		return this;
	}

	public ColioutaiServiceImpl setDao(final ColioutaiLogDao dao) {
		this.dao = dao;
		return this;
	}

	public ColioutaiServiceImpl(final GoogleGeocoderHelper googleGeocodeHelper, final String poiServiceEndpoint, final GetLtV1 getLTV1,
			final GetDetailTourneeV1 detailTourneeV1, final GetCodeTourneeFromLtV1 codeTourneeV1,
			final String serviceConsigneEndpoint, final String servicePTVEndPoint) {
		super();
		this.googleGeocoderHelper = googleGeocodeHelper;
		this.getLTV1 = getLTV1;
		this.detailTourneeV1 = detailTourneeV1;
		this.codeTourneeV1 = codeTourneeV1;
		this.poiServiceEndpoint = poiServiceEndpoint;
		this.serviceConsigneEndpoint = serviceConsigneEndpoint;
		this.ptvServiceEndpoint = servicePTVEndPoint;
	}

	public ColioutaiServiceImpl(final GoogleGeocoderHelper googleGeocodeHelper, final PoiGeocoderHelper poiGeocoderHelper,
			final GetLtV1 getLTV1, final GetDetailTourneeV1 detailTourneeV1, final GetCodeTourneeFromLtV1 getCodeTourneeV1,
			final ConsigneServiceWS consigneServiceWS) {
		super();
		this.googleGeocoderHelper = googleGeocodeHelper;
		this.getLTV1 = getLTV1;
		this.detailTourneeV1 = detailTourneeV1;
		this.codeTourneeV1 = getCodeTourneeV1;
		this.poiGeocoderHelper = poiGeocoderHelper;
		this.serviceConsigne = consigneServiceWS;
	}
	
	@Override
	public ColioutaiInfoLT findInfoLT(final String noLT, final Date dateCalcul, final String mockTempsTrajet) throws ColioutaiException, MalformedURLException {
		return findInfoLT(noLT, dateCalcul, mockTempsTrajet, null);
	}

	@Override
	public ColioutaiInfoLT findInfoLT(final String noLT, final Date dateCalcul, final String mockTempsTrajet, final String client) throws ColioutaiException, MalformedURLException {
		// Initialisation des WS
		initServiceConsigneClient();
		initServicePoiClient();
		initServicePTV();
		initServiceCalculRetardClient();
		
		Lt lt = null;
		try {
			final Map<String, Lt> mapLt = getLTV1.getLt(Arrays.asList(noLT));
			lt = mapLt.get(noLT);
		} catch (Exception e) {
			throw new RuntimeException("L'appel de getLT a échoué", e);
		}

		if (lt == null) {
			throw new ColioutaiException(ColioutaiException.LT_NOT_FOUND);
		}

		ColioutaiInfoLT infoLT = mapLTtoInfoLT(lt, dateCalcul);
		// info pour savoir si on ramener le maitre
		// => pour ne pas surprendre l'utilisateur à afficher une autre LT sans
		// qu'il ne comprenne pourquoi
		if (noLT != null && lt != null && !noLT.equals(lt.getNoLt())) {
			infoLT.setShowsLTMaitre(true);
		}
		infoLT = computeInfoTournee(infoLT, dateCalcul);
		infoLT = computePositionFlashageForMultiColis(infoLT);
		infoLT = computeEtatPoint(infoLT, dateCalcul);
		if(infoLT.getTourneePositionsColis() != null && infoLT.getTourneePositionsColis().size() > 0) {
			for(final ColioutaiInfoLT colis : infoLT.getTourneePositionsColis()) {
				computeEtatPoint(colis, dateCalcul);
			}
		}


		if(infoLT.getDestinataire() == null && infoLT.getSetLTDuPoint() != null && infoLT.getSetLTDuPoint().size() > 0) {
			computeDestinataireMultiColis(infoLT);
		}

		//ici tri par defaut
		infoLT = computeDefaultTri(infoLT);

		// calcul de l'eta MAJ -> protégé par un gros try catch pour la mise au
		// point
		// a retirer quand ça marchera bien en pfi

		if (infoLT.getEtatPoint() != null && (infoLT.getEtatPoint().equals(ColioutaiInfoLT.ETAT_POINT_PENDING)
				|| infoLT.getEtatPoint().equals(ColioutaiInfoLT.ETAT_POINT_DELAYED))) {
			try {

				final CalculETAMaj calculETAMAJ = new CalculETAMaj(ptvHelper, mockTempsTrajet);

				infoLT = calculETAMAJ.calculETAMAJ(infoLT, DateRules.toTime(dateCalcul));
			} catch (Throwable e) {
				// c'est pas top je sais pas c'est le temps du debug pour pas
				// planter tout le monde
				infoLT.setEtaMaj("ERREUR");
				logger.error("Probleme de calcul de l'ETA MAJ mais on ne plante pas l'appel", e);
			}
		}
		
		if (client != null && client.equals("C"))
			infoLT = clientCreneauHelper(lt, infoLT);
		else
			infoLT = serviceClientCreneauHelper(lt, infoLT);
		infoLT = computeDernierPointLivre(infoLT);
		infoLT = computeDateDernierEvt(infoLT);

		//COLI-246: si le point consulté est réalisé, je souhaite voir la valeur de l'heure de livraison du point dans ETA MAJ
		if(infoLT.isRealise()) {
			infoLT.setHeurePassageRealise(DateRules.toDateHeureClient(infoLT.getDateDernierEvenement()));
			infoLT.setEtaMaj(DateRules.toTime(infoLT.getDateDernierEvenement()));
		}
		return infoLT;
	}

	private ColioutaiInfoLT computeDefaultTri(final ColioutaiInfoLT infoLT) {
		// TODO Auto-generated method stub
		Collections.sort(infoLT.getTourneePositionsColis(), new Comparator<ColioutaiInfoLT>() {
			@Override
			public int compare(ColioutaiInfoLT o1, ColioutaiInfoLT o2) {
				if (o2.isRealise() && o1.isRealise()) {
					return o1.getDateDernierEvenement().compareTo(o2.getDateDernierEvenement());

				}
				if (!o2.isRealise() && !o1.isRealise()) {
					if (o1.getEtaMaj() != null && o2.getEtaMaj() != null) {
						return o1.getEtaMaj().compareTo(o2.getEtaMaj());
					} else if (o1.getEtaInitial() != null && o2.getEtaInitial() != null) {
						return o1.getEtaInitial().compareTo(o2.getEtaInitial());
					} else if (o1.getPositionTournee() != null && o2.getPositionTournee() != null) {
						return o1.getPositionTournee().compareTo(o2.getPositionTournee());
					}
				}

				if (o1.isRealise() && !o2.isRealise()) {
					return -1;
				}

				if (!o1.isRealise() && o2.isRealise()) {
					return 1;
				}

				return 0;
			}
		});
		return infoLT;
	}

	private ColioutaiInfoLT computeDateDernierEvt(final ColioutaiInfoLT infoLT) {
		long dateUpper = 0;

		if (infoLT.getTourneePositionsColis() != null && infoLT.getTourneePositionsColis().size() > 0) {
			for (final ColioutaiInfoLT infoLTFound : infoLT.getTourneePositionsColis()) {
				if (infoLTFound.getDateDernierEvenement() != null
						&& infoLTFound.getDateDernierEvenement().getTime() > dateUpper) {
					dateUpper = infoLTFound.getDateDernierEvenement().getTime();
				}
			}
		}

		if (dateUpper > 0) {
			infoLT.setDateDernierEvtTournee(DateRules.toDateHeureClient(new Date(dateUpper)));
		} else if (infoLT.getDateDernierEvenement() != null) {
			infoLT.setDateDernierEvtTournee(DateRules.toDateHeureClient(infoLT.getDateDernierEvenement()));
		}
		return infoLT;
	}

	private void computeDestinataireMultiColis(final ColioutaiInfoLT infoLT) {
		for(final ColioutaiInfoLT infoLTFound : infoLT.getTourneePositionsColis()) {
			if (infoLT.getSetLTDuPoint().contains(infoLTFound.getNoLt()) && (infoLTFound.getDestinataire() != null)) {
				infoLT.setDestinataire(infoLTFound.getDestinataire());
				infoLT.setAdresseDestinataire(infoLTFound.getAdresseDestinataire());
				break;
			}
		}
	}

	private ColioutaiInfoLT computeDernierPointLivre(final ColioutaiInfoLT infoLT) {
		if (infoLT.getTourneePositionsColis() != null && infoLT.getTourneePositionsColis().size() > 0) {
			Date dateLaPlusGrande = null;
			Integer numeroPointDateLaPlusGrande = null;
			for (final ColioutaiInfoLT infoLTTournee : infoLT.getTourneePositionsColis()) {
				if (infoLTTournee.isRealise()) {
					if (dateLaPlusGrande == null
							|| dateLaPlusGrande.getTime() < infoLTTournee.getDateDernierEvenement().getTime()) {
						dateLaPlusGrande = infoLTTournee.getDateDernierEvenement();
						numeroPointDateLaPlusGrande = infoLTTournee.getNoPoint();
					}
				}
			}
			infoLT.setDernierPointLivre(numeroPointDateLaPlusGrande);
		} else {
			infoLT.setDernierPointLivre(null);
		}
		return infoLT;
	}

	ColioutaiInfoLT computeEtatPoint(final ColioutaiInfoLT infoLT, final Date dateCalcul) {
		if (infoLT.isRealise()) {
			infoLT.setEtatPoint(ColioutaiInfoLT.ETAT_POINT_DONE);
		} else if (infoLT.getStatus() != null && (infoLT.getStatus().equals("TA") || infoLT.getStatus().equals("CL"))) {
			// si toutes les LT du point sont en CL on est POSTPONED
			boolean foundCLOnAll = true;
			if (infoLT.getSetLTDuPoint() != null && infoLT.getSetLTDuPoint().size() > 0
					&& infoLT.getTourneePositionsColis() != null && infoLT.getTourneePositionsColis().size() > 0) {
				
				for (final String lt : infoLT.getSetLTDuPoint()) {
					for (final ColioutaiInfoLT ltInfo : infoLT.getTourneePositionsColis()) {
						if (ltInfo.getNoLt().equals(lt)) {
							if (!ltInfo.getStatus().equals("CL")) {
								foundCLOnAll = false;
							}
						}
					}
				}
			} else {
				// ya pas d'autres LTs sur le point
				// si on est pas sur une CL, pas de postponed
				if (!infoLT.getStatus().equals("CL")) {
					foundCLOnAll = false;
				}
			}

			if (foundCLOnAll) {
				infoLT.setEtatPoint(ColioutaiInfoLT.ETAT_POINT_POSTPONED);
			} else {
				try {
					if (infoLT.getCreneauMax() != null
							&& dateCalcul.getTime() > DateRules
							.toTodayTime(infoLT.getCreneauMax()).getTime()) {
						infoLT.setEtatPoint(ColioutaiInfoLT.ETAT_POINT_DELAYED);
					} else {
						infoLT.setEtatPoint(ColioutaiInfoLT.ETAT_POINT_PENDING);
					}
				} catch (final ParseException e) {
					logger.warn("Creneau max et / ou eta MAJ pas valide " + infoLT.getCreneauMax() + " "
							+ infoLT.getEtaMaj());
					// on passse à pending mais c'est pas très juste
					infoLT.setEtatPoint(ColioutaiInfoLT.ETAT_POINT_PENDING);
				}
			}
		}

		return infoLT;
	}

	private ColioutaiInfoLT computePositionFlashageForMultiColis(final ColioutaiInfoLT infoLT) {
		if (!infoLT.isRealise() && (infoLT.getSetLTDuPoint() != null && infoLT.getSetLTDuPoint().size() > 0)) {
			for (final String noLT : infoLT.getSetLTDuPoint()) {
				for (final ColioutaiInfoLT c : infoLT.getTourneePositionsColis()) {
					if (noLT.equals(c.getNoLt())) {
						if (c.isRealise()) {
							infoLT.setPositionFlashage(c.getPositionFlashage());
							break;
						}
					}
				}
			}
		}
		return infoLT;
	}

	/**
	 * map les champs de niveau 1 (attention à ne pas charger la liste sinon
	 * SOException !)
	 * 
	 * @param lt
	 * @return
	 */
	public ColioutaiInfoLT mapLTtoInfoLT(final Lt lt, final Date dateCalcul) {
		final ColioutaiInfoLT infoLT = new ColioutaiInfoLT();
		infoLT.setNoLt(lt.getNoLt());
		infoLT.setLibelleStatus(lt.getLibelleEvt());
		infoLT.setStatus(lt.getCodeEvt());
		infoLT.setDateDernierEvenement(lt.getDateEvt());
		infoLT.setDestinataire(getGeoPosition(lt));
		infoLT.setAccount(lt.getNoContrat());
		infoLT.setVilleExpediteur(lt.getVilleExpediteur());
		infoLT.setZipCodeExpediteur(lt.getCodePostalExpediteur());
		infoLT.setZipCodeDestinataire(lt.getCodePostalDestinataire());
		infoLT.setEtaInitial(lt.getEta());

		try {
			infoLT.setPositionTournee(Integer.parseInt(lt.getPositionC11()));
		} catch (final NumberFormatException e) {
			logger.warn("Position C11 non valide lt " + lt.getNoLt() + " " + lt.getPositionC11());
		}

		infoLT.setRealise(LtRules.isColisRealise(lt.getCodeEvt()));
		infoLT.setAdresseDestinataire(GeoAdresse.parseAddress(new GeoAdresse(null, null, lt.getAdresse1Destinataire(),
				lt.getAdresse2Destinataire(), lt.getCodePostalDestinataire(), lt.getVilleDestinataire())));

		if (infoLT.isRealise()) {
			try {
				final double lati = Double.parseDouble(lt.getLatitudeDistri());
				final double longi = Double.parseDouble(lt.getLongitudeDistri());
				infoLT.setPositionFlashage(new Position(lati, longi));
			} catch (NumberFormatException e) {
				logger.error("Erreur dans la position lati [" + lt.getLatitudeDistri() + "] et longi ["
						+ lt.getLongitudeDistri() + "] numeroLT " + lt.getNoLt());
			} catch (NullPointerException e) {
				logger.error("pas de position pour ce numeroLT " + lt.getNoLt());
			}
		}
		return infoLT;
	}
	
//	private ColioutaiInfoLT getCreneauFromCalculRetard(Lt lt, ColioutaiInfoLT infoLT){
//		try {
//			Future<ResultCalculerRetardPourNumeroLt> calculRetardFuture = new CalculRetardCommand(lt.getNoLt(), calculRetardClient).queue();
//			ResultCalculerRetardPourNumeroLt resultCalculRetard = calculRetardFuture.get();
//			if(resultCalculRetard == null){
//				return infoLT;
//			}
//			
//			String typeMas = resultCalculRetard.getResultRetard().getTypeMas();
//			String creneau_min = "";
//			String creneau_max = "";
//			if(typeMas.equals("MAS_CRENEAU")){
//				creneau_min = resultCalculRetard.getResultRetard().getHeureMin();
//				creneau_max = resultCalculRetard.getResultRetard().getHeureMax();
//			}else{
//				creneau_min = resultCalculRetard.getCalculDateDeLivraisonEstimee().getHeureMinDeLivraisonEstimee();
//				creneau_max = resultCalculRetard.getCalculDateDeLivraisonEstimee().getHeureMaxDeLivraisonEstimee();
//			}
//			infoLT.setCreneauMin(creneau_min);
//			infoLT.setCreneauMax(creneau_max);
//			infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
//			
//		} catch (MalformedURLException | InterruptedException | ExecutionException e) {
//			logger.warn("pas de calcul de créneau : " + e.getMessage());
//		} 
//		return infoLT;
//	}

	private ColioutaiInfoLT clientCreneauHelper(final Lt lt, final ColioutaiInfoLT infoLT) {
		try {
			final int fiabiliteETA = LtRules.getFiabiliteETA(lt);
			
			if (infoLT.getIndiceConfiance() == IndiceConfiance.A || infoLT.getIndiceConfiance() == IndiceConfiance.B) {
				
				final int nbMinutesAvantEtApres = LtRules.getMargeFromFiabiliteETA(lt, infoLT, fiabiliteETA);
				
				if (infoLT.getEtaMaj() == null || "ERREUR".equals(infoLT.getEtaMaj())) {
					final Evt evt = LtRules.getEvenementSMafterEvenementTA(lt);
					if (evt != null) {
						final String creneau = LtRules.getCreneauFromSMEvenement(evt);
						if (creneau != null) {
							final String[] creneau_tab = creneau.split("-");
							final String creneau_min = creneau_tab[0];
							final DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_min).getTime());
							final String creneau_max = creneau_tab[1];
							final DateTime timeFinCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_max).getTime());

							infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
							infoLT.setCreneauMax(DateRules.toTime(timeFinCreneau.toDate()));
							infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
							infoLT.setCreneauCalcule(false);
							infoLT.setEtaInitial(lt.getEta());
							infoLT.setTypeCreneau(TypeCreneau.P);
							logger.info("créneau recuperer depuis evenement SM {}", infoLT.getCreneau());
						}
					} else {
						final String eta = lt.getEta();
						if (eta != null) {
							infoLT.setEtaInitial(eta);
							if (nbMinutesAvantEtApres != 0) {
								DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(eta).getTime());
								timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);

								DateTime timeFin = new DateTime(DateRules.toTodayTime(eta).getTime());
								timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);

								infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
								infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
								infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
								infoLT.setTypeCreneau(TypeCreneau.T);
							} else {
								// ETA pas fiable delai contractuel.
							}
							logger.info("eta à {} et créneau recupèrer depuis evenement TA {} avec fiabiliteETA à {}", eta, infoLT.getCreneau(),
									fiabiliteETA);
						} else {
							infoLT.setCreneauMin("8h00");
							infoLT.setCreneauMax(DateRules.toTime(((Date) lt.getDateLivraisonContractuelle())));
							infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
							infoLT.setTypeCreneau(TypeCreneau.C);
						}
					}
				} else { 
					final String etaMaj = infoLT.getEtaMaj();
//					Evt evt = LtRules.getEvenementSMafterEvenementTA(lt);
//					if (evt != null) {
//						String creneau = LtRules.getCreneauFromSMEvenement(evt);
//						if (creneau != null) {
//							String[] creneau_tab = creneau.split("-");
//							String creneau_min = creneau_tab[0];
//							DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_min).getTime());
//							String creneau_max = creneau_tab[1];
//							DateTime timeFinCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_max).getTime());
//
//							infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
//							infoLT.setCreneauMax(DateRules.toTime(timeFinCreneau.toDate()));
//
//							infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
//							infoLT.setCreneauCalcule(false);
//							infoLT.setEtaInitial(lt.getEta());
//							
//							DateTime timeEtaMaj = new DateTime(DateRules.toTodayTime(etaMaj).getTime());
//							if (timeEtaMaj.isBefore(timeFinCreneau) && timeEtaMaj.isAfter(timeDebutCreneau)) {
//								infoLT.setMessage("Prénom, votre livreur Chronopost devrait vous livrer entre " + infoLT.getCreneauMin() + " et "
//										+ infoLT.getCreneauMax());
//							} else if (timeEtaMaj.isBefore(timeDebutCreneau)) {
//								infoLT.setMessage("annonce avance");
//							} else if (timeEtaMaj.isAfter(timeFinCreneau)) {
//								infoLT.setMessage("annonce retard");
//							}
//							logger.info("créneau recuperer depuis evenement SM {}", infoLT.getCreneau());
//						}
//					} 
//					else {
//						String eta = lt.getEta();
//						if (eta != null) {
//							int nbMinutesAvantEtApres = 0;
//							infoLT.setEtaInitial(eta);
//
//							if (fiabiliteETA == 1) {
//								nbMinutesAvantEtApres = 60;
//								infoLT.setCreneauCalcule(false);
//							} else if (fiabiliteETA == 60) {
//								nbMinutesAvantEtApres = 30;
//								infoLT.setCreneauCalcule(false);
//							}
//							if (nbMinutesAvantEtApres != 0) {
//								DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(eta).getTime());
//								timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);
//
//								DateTime timeFin = new DateTime(DateRules.toTodayTime(eta).getTime());
//								timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);
//
//								infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
//								infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
//								infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
//								infoLT.setMessage("ETA fiable creneau calcule interval predict");
//							} else {
//								infoLT.setMessage("ETA pas fiable creneau contractuel");
//							}
//							logger.info("eta à {} et créneau recupèrer depuis evenement TA {} avec fiabiliteETA à {}", eta, infoLT.getCreneau(),
//									fiabiliteETA);
//						} else {
//							logger.info("pas de calcul de créneau, eta est null ");
//						}
//					}
					
					// Eta live
					if (nbMinutesAvantEtApres != 0) {
						DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(etaMaj).getTime());
						timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);
						DateTime timeFin = new DateTime(DateRules.toTodayTime(etaMaj).getTime());
						timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);

						infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
						infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
						infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
						infoLT.setTypeCreneau(TypeCreneau.L);
					}
				}
			} else {// indice C et D
				final Evt evt = LtRules.getEvenementSMafterEvenementTA(lt);
				if (evt != null) {
					final String creneau = LtRules.getCreneauFromSMEvenement(evt);
					if (creneau != null) {
						final String[] creneau_tab = creneau.split("-");
						final String creneau_min = creneau_tab[0];
						final DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_min).getTime());
						final String creneau_max = creneau_tab[1];
						final DateTime timeFinCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_max).getTime());

						infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
						infoLT.setCreneauMax(DateRules.toTime(timeFinCreneau.toDate()));
						infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
						infoLT.setCreneauCalcule(false);
						infoLT.setEtaInitial(lt.getEta());
						logger.info("créneau recuperer depuis evenement SM {}", infoLT.getCreneau());
						infoLT.setTypeCreneau(TypeCreneau.P);
					}
				} else {
					final String eta = lt.getEta();
					if (eta != null) {
						int nbMinutesAvantEtApres = 0;
						infoLT.setEtaInitial(eta);

						if (fiabiliteETA == 1) {
							nbMinutesAvantEtApres = 60;
							infoLT.setCreneauCalcule(false);
						} else if (fiabiliteETA == 60) {
							nbMinutesAvantEtApres = 30;
							infoLT.setCreneauCalcule(false);
						}
						if (nbMinutesAvantEtApres != 0) {
							DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(eta).getTime());
							timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);
							DateTime timeFin = new DateTime(DateRules.toTodayTime(eta).getTime());
							timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);

							infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
							infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
							infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
							infoLT.setMessage("ETA fiable creneau calcule interval predict");
							infoLT.setTypeCreneau(TypeCreneau.T);
						} else {
							infoLT.setMessage("ETA pas fiable creneau contractuel");
						}
						logger.info("eta à {} et créneau recupèrer depuis evenement TA {} avec fiabiliteETA à {}", eta, infoLT.getCreneau(), fiabiliteETA);
					} else {
						infoLT.setCreneauMin("8h00");
						infoLT.setCreneauMax(DateRules.toTime(((Date) lt.getDateLivraisonContractuelle())));
						infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
						infoLT.setTypeCreneau(TypeCreneau.C);
					}
				}
			}
		} catch (final ParseException e) {
			logger.warn("pas de calcul de créneau : " + e.getMessage());
		}
		return infoLT;
	}

	private ColioutaiInfoLT serviceClientCreneauHelper(final Lt lt, final ColioutaiInfoLT infoLT) {
		logger.debug("recupèrer créneau pour la lt {}",infoLT.getNoLt());
		try {
			final List<Evt> evts = LtRules.getAllEvenementSMafterEvenementTA(lt);
			final Map<String,String> map = infoLT.getTypeAvisage();
			for(final Evt e : evts){
				map.put(e.getInfoscomp().get(EvtRules.ID_MEDIA), e.getInfoscomp().get(EvtRules.ID_ADRESSE_MEDIA));
			}			

			final Evt evt = LtRules.getEvenementSMafterEvenementTA(lt);
			if(evt != null){
				final String creneau = LtRules.getCreneauFromSMEvenement(evt);
				if(creneau != null){
					final String [] creneau_tab = creneau.split("-");
					final String creneau_min = creneau_tab[0];
					final DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_min).getTime());
					final String creneau_max = creneau_tab[1];
					final DateTime timeFinCreneau = new DateTime(DateRules.toTodayTimeFromSMCreneau(creneau_max).getTime());

					infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
					infoLT.setCreneauMax(DateRules.toTime(timeFinCreneau.toDate()));
					infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
					infoLT.setCreneauCalcule(false);
					infoLT.setEtaInitial(lt.getEta());
					infoLT.setTypeCreneau(TypeCreneau.P);
					logger.info("créneau recuperer depuis evenement SM {}",infoLT.getCreneau());
				}else{
					final Evt evenmt = LtRules.getEvenementTaDuJour(lt);
					final Date rdvMin = EvtRules.getCreneauMinRdvIDBCO(evenmt);
					final Date rdvMax = EvtRules.getCreneauMaxRdvIDBCO(evenmt);
					infoLT.setCreneauMin(DateRules.toTime(rdvMin));
					infoLT.setCreneauMax(DateRules.toTime(rdvMax));
					infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
					infoLT.setCreneauCalcule(false);
					infoLT.setTypeCreneau(TypeCreneau.R);
				}

			}else{
				final int fiabiliteETA = LtRules.getFiabiliteETA(lt);
				final String eta = lt.getEta();
				if (eta != null) {
					int nbMinutesAvantEtApres = 0;
					infoLT.setEtaInitial(eta);

					if (fiabiliteETA == 1) {
						nbMinutesAvantEtApres = 60;
						infoLT.setCreneauCalcule(false);
					} else if (fiabiliteETA == 60) {
						nbMinutesAvantEtApres = 30;
						infoLT.setCreneauCalcule(false);
					} else {
						nbMinutesAvantEtApres = 60;
						infoLT.setCreneauCalcule(true);
					}
					if (nbMinutesAvantEtApres != 0) {
						DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(eta).getTime());
						timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);
						DateTime timeFin = new DateTime(DateRules.toTodayTime(eta).getTime());
						timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);

						infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
						infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
						infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
						infoLT.setTypeCreneau(TypeCreneau.T);
					}
					logger.info("eta à {} et créneau recupèrer depuis evenement TA {} avec fiabiliteETA à {}", eta,
							infoLT.getCreneau(), fiabiliteETA);
				} else {
					logger.info("pas de calcul de créneau, eta est null ");
				}
			}
		} catch (final ParseException e) {
			logger.warn("pas de calcul de créneau : " + e.getMessage());
		}

		return infoLT;
	}

	//	private ColioutaiInfoLT computeCreneau(String eta, int fiabiliteETA, ColioutaiInfoLT infoLT, Date dateCalcul) {
	//
	//		if (fiabiliteETA == 0) {
	//			return null;
	//		} else if (fiabiliteETA >= 1 && eta != null) {
	//
	//			int nbMinutesAvantEtApres = 0;
	//
	//			if (fiabiliteETA == 1) {
	//				nbMinutesAvantEtApres = 60;
	//			} else {
	//				nbMinutesAvantEtApres = fiabiliteETA / 2;
	//			}
	//
	//			try {
	//				DateTime timeDebutCreneau = new DateTime(DateRules.toTodayTime(eta).getTime());
	//				timeDebutCreneau = timeDebutCreneau.minusMinutes(nbMinutesAvantEtApres);
	//
	//				DateTime timeFin = new DateTime(DateRules.toTodayTime(eta).getTime());
	//				timeFin = timeFin.plusMinutes(nbMinutesAvantEtApres);
	//
	//				infoLT.setCreneauMin(DateRules.toTime(timeDebutCreneau.toDate()));
	//				infoLT.setCreneauMax(DateRules.toTime(timeFin.toDate()));
	//
	//				/*if(infoLT.getCreneauMax() != null && infoLT.getCreneauMax().compareTo(DateRules.toTime(dateCalcul)) < 0) {
	//					infoLT.setCreneau("Information indisponible");
	//				} else {*/
	//					// préformatage - TODO à supprimer peut être :)
	//					infoLT.setCreneau(infoLT.getCreneauMin() + " / " + infoLT.getCreneauMax());
	//				//}
	//
	//			} catch (Exception e) {
	//				logger.warn("ETA fourni non valide - pas de calcul de créneau : " + eta);
	//			}
	//
	//		}
	//
	//		return infoLT;
	//	}

	/**
	 * Recuperation des informations de la tournee via des appels à d'autres
	 * micro services
	 * 
	 * @param infoLT
	 * @return
	 */
	private ColioutaiInfoLT computeInfoTournee(ColioutaiInfoLT infoLT, final Date dateCalcul) {
		try {
			Date dateSearch = new Date();
			final GetCodeTourneeFromLTResponse codeTourneeResponse = codeTourneeV1.getCodeTourneeFromLt(infoLT.getNoLt(),
					dateSearch);

			if (codeTourneeResponse != null) {
				dateSearch = DateRules.toTodayTime("00:00");
				final DetailTournee detailTournee = detailTourneeV1.getDetailTournee(
						codeTourneeResponse.getCodeAgence() + codeTourneeResponse.getCodeTournee(), dateSearch);
				infoLT = transformDetailTourneeInto(infoLT, detailTournee, dateCalcul);
				infoLT.setIndiceConfiance(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee));
				return infoLT;
			} else {
				// si pas de tournee et que le SDK renvoie null - est-ce
				// vraiment possible ?
				// du coup pas d'infos de tournee mais on ne plante pas
				return infoLT;
			}

			// SERIOUSLY ?? plus de LT alors qu'on vient de la trouver
		} catch (final NotFoundException e) {
			// dans tous les cas c'est soit pas normal parce que bug, soit on ne
			// peut rien faire => erreur 500
			// donc on lance une technical
			// throw new TechnicalException(
			// "Erreur lors de la tentative de recuperation de la tournee =>
			// NotFound " + e.getMessage(), e);
		} catch (final ParseException e) {
			throw new MSTechnicalException("Erreur lors de la recherche de la tournée", e);
		}

		return infoLT;
	}

	/**
	 * Transforme le detail de la tournée en infoLT
	 * 
	 * @param infoLT
	 * @param detailTournee
	 * @return
	 */
	private ColioutaiInfoLT transformDetailTourneeInto(ColioutaiInfoLT infoLT, final DetailTournee detailTournee, final Date dateCalcul) {
		if(detailTournee.getInformations() != null)
			infoLT.setNomDuChauffeur(detailTournee.getInformations().get("chauffeur"));

		infoLT.setCodeTournee(detailTournee.getCodeTournee());
		infoLT.setCodeAgence(detailTournee.getCodeAgence()); 
		infoLT = computeLTPoint(infoLT, detailTournee);

		// on s'assure que les coordonées sont remontées dans le bon ordre
		if (detailTournee.getRelevesGps() != null) {
			infoLT.setCamionPositionTourneeList(detailTournee.getRelevesGps());
			Collections.sort(infoLT.getCamionPositionTourneeList(), new Comparator<PositionGps>() {
				@Override
				public int compare(PositionGps p1, PositionGps p2) {

					if (p1.getDateRelevePosition() == null && p2.getDateRelevePosition() != null) {
						return 1;
					} else if (p1.getDateRelevePosition() == null && p2.getDateRelevePosition() != null) {
						return -1;
					} else {
						return p2.getDateRelevePosition().compareTo(p1.getDateRelevePosition());
					}
				}
			});
		}

		final List<Point> pAll = new ArrayList<Point>();

		if (detailTournee.getPointsRealises() != null) {
			pAll.addAll(detailTournee.getPointsRealises());
		}

		if (detailTournee.getPointsEnDistribution() != null) {
			pAll.addAll(detailTournee.getPointsEnDistribution());
		}

		final List<Integer> listPointNonRealise = new ArrayList<>();

		if (pAll.size() > 0) {
			infoLT.setTourneePositionsColis(new ArrayList<ColioutaiInfoLT>());
			final List<Future<Point>> calculPointFutures = new ArrayList<Future<Point>>();
			for (final Point p : pAll) {
				calculPointFutures.add(new ColioutaiPointCommand(p, detailTournee, infoLT, this, dateCalcul).queue());
			}

			for (final Future<Point> calculPointFuture : calculPointFutures) {
				Point resultPoint = null;
				try {
					resultPoint = calculPointFuture.get();
				} catch (final InterruptedException e) {
					logger.warn("error InterruptedException ColioutaiPointCommand: "+e.getMessage());
				} catch (final ExecutionException e) {
					logger.warn("error ExecutionException ColioutaiPointCommand: "+e.getMessage());
				}
				if (resultPoint != null && !resultPoint.isRealise()) {
					listPointNonRealise.add(resultPoint.getNumeroPoint());
				}
			}
		}

		Collections.sort(listPointNonRealise);

		// calcul du nombre de point avant le point à livrer
		// ils sont donc dans l'ordre (on espere)
		if (listPointNonRealise != null) {
			int toDoCounter = 0;
			for (final Integer pointNonRealise : listPointNonRealise) {
				// on ignore les postponed
				try {
					for (ColioutaiInfoLT infoLTFound : infoLT.getTourneePositionsColis()) {
						if (infoLTFound.equals(pointNonRealise)) {
							infoLTFound = computeEtatPoint(infoLTFound, dateCalcul);
							if (infoLT.equals(ColioutaiInfoLT.ETAT_POINT_POSTPONED)) {
								continue;
							}
						}
					}
				} catch (final Exception e) {
					logger.warn("Erreur dans l'ignoration des postponed", e);
				}
				if (pointNonRealise < infoLT.getNoPoint()) {
					toDoCounter++;
				}
				if (infoLT.getNoPointSuivant() == null) {
					infoLT.setNoPointSuivant(pointNonRealise);
				}
				if (pointNonRealise > infoLT.getNoPoint()) {
					break;
				}
			}
			infoLT.setNbPointsAvantLivraison(toDoCounter);
		}
		return infoLT;
	}

	public ColioutaiInfoLT computeLTPoint(final ColioutaiInfoLT infoLT, final DetailTournee detailTournee) {
		final Set<String> setPointsLT = new HashSet<>();
		final List<Point> listPoints = new ArrayList<>();
		if (detailTournee.getPointsEnDistribution() != null) {
			listPoints.addAll(detailTournee.getPointsEnDistribution());
		}
		if (detailTournee.getPointsRealises() != null) {
			listPoints.addAll(detailTournee.getPointsRealises());
		}
		for (final Point pt : listPoints) {
			if (pt.getLtsDuPoint() != null) {
				for (final Lt ltInitial : pt.getLtsDuPoint()) {
					if (ltInitial.getNoLt().equals(infoLT.getNoLt())) {
						infoLT.setNoPoint(pt.getNumeroPoint());
						for (final Lt lt : pt.getLtsDuPoint()) {
							if (!lt.getNoLt().equals(infoLT.getNoLt())) {
								setPointsLT.add(lt.getNoLt());
							}
						}
					}
				}
			}

		}
		infoLT.setSetLTDuPoint(setPointsLT);
		return infoLT;
	}

	/**
	 * get Geocode position from: 1- LT 2- POI 3- google protected pour les
	 * tests
	 * 
	 * @param lt
	 * @return
	 */
	private Position getGeoPosition(final Lt lt) {
		//logger.info("getGeoPosition from lt number: " + lt.getNoLt());
		Position position = null;
		boolean positionRecupereeDepuisLaConsigne = false;
		final boolean ltAvecConsigne = LtRules.aRecuUneConsigne(lt);

		// est-ce qu'il y a une CL
		if (ltAvecConsigne) {
			// recup adresse depuis la consigne
			final GeoAdresse adresseConsigne = retrieveAdresseConsigne(lt);
			if (adresseConsigne != null) {
				// on résoud la position via les moyens externes
				position = geocode(adresseConsigne);
				// on flaggue le fait qu'on ait récupéré une position à partir
				// de la consigne
				positionRecupereeDepuisLaConsigne = (position != null);
			}
		}

		if (!ltAvecConsigne || !positionRecupereeDepuisLaConsigne) {
			// on regarde si on des coordonnées dans la LT qui vient directement
			// de la TA s'il n'y a pas de consigne ou que la consigne ne
			// comporte pas d'adresse géocodable.
			if (lt.getLatitudePrevue() != null && lt.getLongitudePrevue() != null) {
				final double lati = Double.parseDouble(lt.getLatitudePrevue());
				final double longi = Double.parseDouble(lt.getLongitudePrevue());

				position = new Position();
				position.setLati(lati);
				position.setLongi(longi);
			} else if (lt.getLatitudeDistri() != null && lt.getLongitudeDistri() != null) {
				final double lati = Double.parseDouble(lt.getLatitudeDistri());
				final double longi = Double.parseDouble(lt.getLongitudeDistri());

				position = new Position();
				position.setLati(lati);
				position.setLongi(longi);
			} else {
				// si pas de coordonnées, voir l'adresse et resoudre via les
				// moyens externes
				final GeoAdresse adresse = new GeoAdresse(lt.getNom1Destinataire(), lt.getNom2Destinataire(),
						lt.getAdresse1Destinataire(), lt.getAdresse2Destinataire(),
						lt.getCodePostalDestinataire(), lt.getVilleDestinataire());

				position = geocode(adresse);
			}
		}
		return position;
	}

	/**
	 * regle d'utilisation des geocoders
	 * 
	 * @param adresse
	 * @return
	 */
	private Position geocode(final GeoAdresse adresse) {
		Position position = null;
		try {
			final Position poiPosition = new ServicePoiCommand(poiGeocoderHelper, adresse).execute();
			if (poiPosition == null) {
				throw new PositionNotFoundException("not found " + adresse);
			}
			position = new Position();
			position.setLati(poiPosition.getLati());
			position.setLongi(poiPosition.getLongi());
		} catch (PositionNotFoundException e) {
			logger.warn("position not found from POI");
			final Position googlePosition = new ServiceGoogleCommand(googleGeocoderHelper, adresse).execute();
			if (googlePosition != null) {
				position = new Position();
				position.setLati(googlePosition.getLati());
				position.setLongi(googlePosition.getLongi());
			}
		}
		return position;
	}

	/**
	 * Recherche de la consigne
	 * 
	 * @param lt
	 * @return
	 */
	private GeoAdresse retrieveAdresseConsigne(final Lt lt) {
		final ResultInformationsConsigne consigne = new ServiceConsigneCommand(serviceConsigne, lt).execute();
		if (consigne != null) {
			final GeoAdresse adresse = new GeoAdresse();
			adresse.nom1 = consigne.getInformationsColisConsigne().getNom1Destinataire();
			adresse.nom2 = consigne.getInformationsColisConsigne().getNom2Destinataire();
			adresse.adresse1 = consigne.getInformationsColisConsigne().getRue1Destinataire();
			adresse.adresse2 = consigne.getInformationsColisConsigne().getRue2Destinataire();
			adresse.cp = consigne.getInformationsColisConsigne().getCodePostalDestinataire();
			adresse.ville = consigne.getInformationsColisConsigne().getVilleDestinataire();
			return adresse;
		}
		return null;
	}
	
	private void initServiceCalculRetardClient() throws MalformedURLException {
		if(this.calculRetardEndpoint!=null){
			if(this.calculRetardClient == null){
				this.calculRetardClient = new InitCalculRetardCommand(this.calculRetardEndpoint).execute();
			}
		}
	}

	/**
	 * Initialisation de l'appel au WS Consigne.
	 * 
	 * @param endpoint
	 * @throws MalformedURLException
	 */
	private void initServiceConsigneClient() throws MalformedURLException {
		if (this.serviceConsigne == null) {
			this.serviceConsigne = new InitServiceConsigneCommand(this.serviceConsigneEndpoint).execute();
		}
	}

	/**
	 * Initialisation de l'appel au WS POI.
	 * 
	 * @param endpoint
	 * @throws MalformedURLException
	 */
	private void initServicePoiClient() throws MalformedURLException {
		if (this.poiGeocoderHelper == null) {
			this.poiGeocoderHelper = PoiGeocoderHelper
					.getInstance(new InitServicePoiCommand(this.poiServiceEndpoint).execute());
		}
	}

	/**
	 * Initialisation de l'appel au WS POI.
	 * 
	 * @param endpoint
	 * @throws MalformedURLException
	 */
	private void initServicePTV() throws MalformedURLException {
		if (this.ptvHelper == null) {
			this.ptvHelper = PTVHelper
					.getInstance(new InitServicePtvCommand(this.ptvServiceEndpoint).execute());
		}
	}

	@Override
	public boolean insertLog(final ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException {
		return dao.insertLog(colioutaiInfoLT);
	}

	public List<ColioutaiLog> getColioutaiLog(final String typeLog, final Date from, final Date to) throws MSTechnicalException {
		return dao.getColioutaiLog(typeLog, from, to);
	}

	@Override
	public String calculLtHash(final String noLT) {
		final StringBuilder sb = new StringBuilder();
		Lt lt = null;
		try {
			final Map<String, Lt> mapLt = getLTV1.getLt(Arrays.asList(noLT));
			lt = mapLt.get(noLT);
		} catch (final MSTechnicalException | NotFoundException | ServiceUnavailableException e) {
			logger.error("Erreur calculLtHash for noLT : " + noLT, e);
		}
		
		/* Ajout d'un traitement de l'exception en respectant le comportement du code lorsque
		 * ce traitement n'existe pas */
		if (lt == null)
			throw new NullPointerException("in no_lt=" + noLT);
		sb.append(lt.getNoLt());
//		sb.append(lt.getNoContrat());
		sb.append(lt.getCodePostalDestinataire());
		return SecurityUtil.getHash(sb.toString(), "chronopostTIC2012");
	}
}

