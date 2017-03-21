package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.rules.DateRules;

public class CalculETAMaj {

	private final static Logger logger = LoggerFactory.getLogger(CalculETAMaj.class);

	private final PTVHelperInterface ptvHelper;
	private Map<String, Integer> mockTempsTrajetMap;
	
	public CalculETAMaj(final PTVHelperInterface ptvHelper, final String mockTempsTrajet) {
		super();
		
		this.ptvHelper = ptvHelper;
		
		if(mockTempsTrajet != null) {
			mockTempsTrajetMap = new HashMap<>();
			
			Scanner scanner = new Scanner(mockTempsTrajet);
			scanner.useDelimiter("X");
			
			while(scanner.hasNext()) {
				mockTempsTrajetMap.put(scanner.next(), scanner.nextInt());
			}
			
			scanner.close();
		}
	}
	
	/**
	 * Recalcule l'ETA quand la tournée est en cours
	 * 
	 * @param colioutaiInfoLT
	 * @return
	 */
	public ColioutaiInfoLT calculETAMAJ(final ColioutaiInfoLT colioutaiInfoLT, final String heureCalculString) {
		try {
			// smoke test
			for(final ColioutaiInfoLT infoLT : colioutaiInfoLT.getTourneePositionsColis()) {
				if(infoLT.getEtaInitial() == null) {
					// pas d'optim sur les colis, on ne peut rien faire
					// A CHANGER quand on passera en prod par vide
					colioutaiInfoLT.setEtaMaj(null);
					return colioutaiInfoLT; 
				}
			}
			
			if (colioutaiInfoLT.getEtaInitial() == null || colioutaiInfoLT.getIndiceConfiance() == null
					/*|| colioutaiInfoLT.getIndiceConfiance() == IndiceConfiance.D*/ || heureCalculString == null) {

				// on boucle sur tous pour mettre tout les ETA MAJ à null
				nullifyAllETAMaj(colioutaiInfoLT);
			} else {
				// on reprojete sur toute la tournée depuis le dernier realisé
				final Date heureCalcul = DateRules.toTodayTime(heureCalculString);

				Date etaInitialPremierColis = null;

				if (colioutaiInfoLT.getTourneePositionsColis() != null
						&& colioutaiInfoLT.getTourneePositionsColis().size() > 0
						&& colioutaiInfoLT.getTourneePositionsColis().get(0) != null
						&& colioutaiInfoLT.getTourneePositionsColis().get(0).getEtaInitial() != null) {

					etaInitialPremierColis = DateRules
							.toTodayTime(colioutaiInfoLT.getTourneePositionsColis().get(0).getEtaInitial());
				}

				// on regarde l'heure de début théorique de la tournée
				// si on en a pas, on fait rien
				// et si on en a, on ne fait quelque chose que si
				// on a deja un colis realisé dans la tournée (tournée
				// commencée)
				// OU
				// aucun colis réalisé et on a dépassé cette heure théorique de
				// début (premier colis en retard !)
				if (etaInitialPremierColis != null && (heureCalcul.getTime() > etaInitialPremierColis.getTime()
						|| hasAtLeastOneReal(colioutaiInfoLT))) {

					// création des maps de temps de parcours
					// on trie par numero car eta initial c'est sur le numero
					colioutaiInfoLT.setTourneePositionsColis(triListNumeroColioutaInfoLT(colioutaiInfoLT));
					final Map<String, Integer> mapTempsParcours = calculTempsParcours(colioutaiInfoLT);

					if (mapTempsParcours != null) {
						// tri par heure de realisation puis position colis,
						// null à la fin
						final List<ColioutaiInfoLT> listColioutaiInfoLTTriee = triListColioutaInfoLT(colioutaiInfoLT);
						ColioutaiInfoLT colisPrecedent = null;
						
						// on passe sur tous les colis
						for (final ColioutaiInfoLT colis : listColioutaiInfoLTTriee) {

							if(colis.getEtatPoint() != null && colis.getEtatPoint().equals(ColioutaiInfoLT.ETAT_POINT_POSTPONED)) {
								colis.setEtaMaj(null);
								continue;
							}
							
							// on ne met à jour que les non realisés
							if (!colis.isRealise()) {
								final String etaMAJ = calculETAFromHeureConsultTempsParcoursEtColisPrecedent(heureCalcul, mapTempsParcours,
										colisPrecedent, colis);
								colis.setEtaMaj(etaMAJ);
								if (colis.getNoLt().equals(colioutaiInfoLT.getNoLt())) {
									// on recopie au niveau superieur
									colioutaiInfoLT.setEtaMaj(etaMAJ);
								}
							} else {
								// on met la date de réalisation dans l'ETA MAJ
								colis.setEtaMaj(DateRules.toTime(colis.getDateDernierEvenement()));
							}
							colisPrecedent = colis;
						}
					}
				} else {
					// tournée non commencé
					logger.debug("tournée non commencé et on n'est pas encore à l'heure théorique de début");

					// on blinde si desfois on avait des ETA MAJ alors qu'on
					// aurait pas du
					// on boucle sur tous pour mettre tout les ETA MAJ à null
					nullifyAllETAMaj(colioutaiInfoLT);
				}
			}

		} catch (final ParseException e) {
			logger.warn("heure non valide ", e);
		}

		return colioutaiInfoLT;
	}

	/**
	 * Recalcul de l'ETA (vraiment!)
	 * 
	 * @param heureCalcul
	 * @param mapTempsParcours
	 * @param positionPrecedente
	 * @param positionTournee
	 * @param etaInitial
	 * @return
	 */
	private String calculETAFromHeureConsultTempsParcoursEtColisPrecedent(final Date heureCalcul, final Map<String, Integer> mapTempsParcours,
			final ColioutaiInfoLT colisPrecedent, final ColioutaiInfoLT colisCalcul) {
		if(colisPrecedent == null) {
			// oops c'est le premier et ya rien de realisé avant
			// on a donc dépassé l'heure de début de la tournée théorique
			// on décale donc l'eta à l'heure de consultation
			logger.warn("on a depassé l'heure du debut théorique de tournée, on fait doucement avancer les ETA");
			return DateRules.toTime(heureCalcul);
		}
		
		final String keyTempsParcours = keyTempParcours(colisPrecedent, colisCalcul);
		Integer tempsParcours = mapTempsParcours.get(keyTempsParcours);
		if(tempsParcours == null) {
			try {
				tempsParcours = computeTempsBetweenPoints(colisPrecedent, colisCalcul);
				logger.info("Temps parcours entre " + colisPrecedent.getPositionTournee() + "-" + colisCalcul.getPositionTournee() + " : " + tempsParcours);
			} catch(Exception e) {
				logger.error("Calcul impossible de l'eta " + e.getMessage(), e);
				return null;
			}
		}
		
		try {
			Long newETA = DateRules.toTodayTime(colisPrecedent.getEtaMaj()).getTime() + (((long)tempsParcours) * 1000L * 60L);
			if(newETA < heureCalcul.getTime()) {
				newETA = heureCalcul.getTime();
			}
			return DateRules.toTime(new Date(newETA));
		} catch (ParseException e) {
			logger.warn("Erreur lors du calcul du nouvel eta " + e);
		}
		return null;
	}

	private Integer computeTempsBetweenPoints(final ColioutaiInfoLT colisPrecedent, final ColioutaiInfoLT colisCalcul) throws Exception {
		if(mockTempsTrajetMap != null) {
			return mockTempsTrajetMap.get(keyTempParcours(colisPrecedent, colisCalcul));
		}
		
		// temps de stop ?
		final String heureArrivee = ptvHelper.heureArrivee(colisPrecedent.getDestinataire(), colisPrecedent.getEtaMaj(), 2, colisCalcul.getDestinataire());
		
		if(heureArrivee != null) {
			return (int) (DateRules.toTodayTime(heureArrivee).getTime() - DateRules.toTodayTime(colisPrecedent.getEtaMaj()).getTime()) / (1000 * 60);
		} else {
			throw new Exception("Erreur calcul entre " + colisPrecedent + " et " + colisCalcul);
		}
	}

	/**
	 * Tri de la liste des colis pour la mise à jour des ETAs
	 * @param colioutaiInfoLT
	 * @return
	 */
	private List<ColioutaiInfoLT> triListColioutaInfoLT(final ColioutaiInfoLT colioutaiInfoLT) {
		Collections.sort(colioutaiInfoLT.getTourneePositionsColis(), new Comparator<ColioutaiInfoLT>() {
			@Override
			public int compare(final ColioutaiInfoLT o1, final ColioutaiInfoLT o2) {
				if(!o1.isRealise() && !o2.isRealise()) {
					return o1.getPositionTournee() - o2.getPositionTournee();
				}
				if(o1.isRealise() && o2.isRealise()) {
					// la soustraction devrait rester sage en terme de chiffres
					return (int)(o1.getDateDernierEvenement().getTime() - o2.getDateDernierEvenement().getTime());
				}
				// les réalisés en priorité
				if(o1.isRealise() && !o2.isRealise()) {
					return -1;
				} else if(!o1.isRealise() && o2.isRealise()) {
					return 1;
				}
				// sinon on ne sait pas
				return 0;
			}
		});
		return colioutaiInfoLT.getTourneePositionsColis();
	}
	
	/**
	 * Tri de la liste des colis calcul des temps de parcours
	 * @param colioutaiInfoLT
	 * @return
	 */
	private List<ColioutaiInfoLT> triListNumeroColioutaInfoLT(final ColioutaiInfoLT colioutaiInfoLT) {
		// la position C11 n'est pas forcement dans l'ordre...
		// on trie donc par ETA initial, puis on renumerote, puis on retrie par numero
		Collections.sort(colioutaiInfoLT.getTourneePositionsColis(), new Comparator<ColioutaiInfoLT>() {
			@Override
			public int compare(ColioutaiInfoLT o1, ColioutaiInfoLT o2) {
				try {
					return DateRules.toTodayTime(o1.getEtaInitial()).compareTo(DateRules.toTodayTime(o2.getEtaInitial()));
				}catch(Exception e) {
					logger.error("ETA initial NULL, revoir smoke test");
					return 0;
				}
			}
		});
		
		int i = 1;
		for(final ColioutaiInfoLT infoLT : colioutaiInfoLT.getTourneePositionsColis()) {
			infoLT.setPositionTournee(i++);
			
			if(infoLT.getNoLt().equals(colioutaiInfoLT.getNoLt())) {
				colioutaiInfoLT.setPositionTournee(infoLT.getPositionTournee());
			}
		}
		Collections.sort(colioutaiInfoLT.getTourneePositionsColis(), new Comparator<ColioutaiInfoLT>() {

			@Override
			public int compare(ColioutaiInfoLT o1, ColioutaiInfoLT o2) {
				return o1.getPositionTournee() - o2.getPositionTournee();
			}
		});
		return colioutaiInfoLT.getTourneePositionsColis();
	}

	/**
	 * Construction de la map des temps de parcours
	 * @param colioutaiInfoLT
	 * @return
	 */
	private Map<String, Integer> calculTempsParcours(final ColioutaiInfoLT colioutaiInfoLT) {
		try {
			if (colioutaiInfoLT.getTourneePositionsColis() != null
					&& colioutaiInfoLT.getTourneePositionsColis().size() > 0) {
				final Map<String, Integer> mapTempsParcours = new HashMap<>();
				ColioutaiInfoLT colisPrecedent = null;
				for (ColioutaiInfoLT colis : colioutaiInfoLT.getTourneePositionsColis()) {
					if (colisPrecedent != null) {
						mapTempsParcours.put(keyTempParcours(colisPrecedent, colis),
								tempsParcours(colisPrecedent.getEtaInitial(), colis.getEtaInitial()));
					}
					colisPrecedent = colis;
				}
				return mapTempsParcours;
			}
		} catch (final ParseException e) {
			logger.warn("eta initiaux non valides", e);
		}
		return null;
	}

	/**
	 * Calcul du temps de parcours en minutes à partir des ETAs
	 * 
	 * @param etaFrom
	 * @param etaTo
	 * @return
	 */
	private Integer tempsParcours(final String etaFrom, final String etaTo) throws ParseException {
		return (int) ((DateRules.toTodayTime(etaTo).getTime() - DateRules.toTodayTime(etaFrom).getTime())
				/ (1000L * 60L));
	}

	/**
	 * Construit la clé pour la map des temps de parcours
	 * @param colisPrecedent
	 * @param colis
	 * @return
	 */
	private String keyTempParcours(final ColioutaiInfoLT colisPrecedent, final ColioutaiInfoLT colis) {
		return colisPrecedent.getPositionTournee() + "-" + colis.getPositionTournee();
	}

	/**
	 * Test si on a au moins commencé la tournée
	 * @param colioutaiInfoLT
	 * @return
	 */
	private boolean hasAtLeastOneReal(final ColioutaiInfoLT colioutaiInfoLT) {
		if (colioutaiInfoLT.getTourneePositionsColis() != null
				&& colioutaiInfoLT.getTourneePositionsColis().size() > 0) {
			for (ColioutaiInfoLT colis : colioutaiInfoLT.getTourneePositionsColis()) {
				if (colis.isRealise()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Passe tous les ETA a null
	 * @param colioutaiInfoLT
	 */
	private void nullifyAllETAMaj(final ColioutaiInfoLT colioutaiInfoLT) {
		colioutaiInfoLT.setEtaMaj(null);
		if (colioutaiInfoLT.getTourneePositionsColis() != null
				&& colioutaiInfoLT.getTourneePositionsColis().size() > 0) {
			for (ColioutaiInfoLT colis : colioutaiInfoLT.getTourneePositionsColis()) {
				colis.setEtaMaj(null);
			}
		}
	}
}
