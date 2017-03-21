package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.IndiceConfiance;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.rules.LtRules;

/**
 * Classe de calcul de l'indice de confiance
 * 
 * @author vdesaintpern
 *
 */
public class CalculIndiceConfiance {

	public static IndiceConfiance calculIndiceConfianceFrom(final DetailTournee tournee) {
		final List<Colis> listColis = buildListColisFromDetailTournee(tournee);

		// on ne peut pas calculer pour une raison ou pour une autre...
		if (listColis == null) {
			return null;
		}

		final List<Colis> retourVersLeFutur = new ArrayList<>();
		int breakCount = 0;
		final List<Colis> listOriginale = new ArrayList<>(listColis);

		// on pousse les nuls a la fin et on trie par date croissant
		Collections.sort(listColis, new Comparator<Colis>() {
			@Override
			public int compare(Colis c1, Colis c2) {
				if (c1.dateRealisation == null && c2.dateRealisation == null) {
					return 0;
				}
				if (c1.dateRealisation == null) {
					return 1;
				}
				if (c2.dateRealisation == null) {
					return -1;
				}
				return c1.dateRealisation.compareTo(c2.dateRealisation);
			}
		});

		// on trie la liste par ordre de passage de la tournée
		Collections.sort(listOriginale, new Comparator<Colis>() {
			@Override
			public int compare(Colis c1, Colis c2) {
				return c1.numero - c2.numero;
			}
		});

		// on rejoue le passé en passant sur chaque colis réalisé dans
		// l'ordre
		// 1 par 1 et on pousse les autres à non réalisé
		for (final Colis colis : listColis) {
			if (!colis.realise)
				break;
			retourVersLeFutur.add(colis);
			List<Colis> listColisSnapshot = new ArrayList<>();
			for (final Colis colisOriginal : listOriginale) {
				if (!retourVersLeFutur.contains(colisOriginal)) {
					listColisSnapshot.add(new Colis(colisOriginal.numero, null));
				} else {
					listColisSnapshot.add(colisOriginal);
				}
			}

			if (isBreak(listColisSnapshot)) {
				breakCount++;
				// je le garde pour les logs plus tard en cas de corrections
				// (fort possible)
				// SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				// Output.logLN("Break " + breakCount + " à " +
				// sdf.format(colis.dateRealisation));
			}

			if (breakCount >= 10) {
				break;
			}
		}

		if (breakCount == 0) {
			return IndiceConfiance.A;
		} else if (breakCount == 1) {
			return IndiceConfiance.B;
		} else if (breakCount == 2) {
			return IndiceConfiance.C;
		} else /* if (breakCount >= 3) */ {
			return IndiceConfiance.D;
		} /*
			 * else if (breakCount == 4) { return IndiceConfiance.E; } else if
			 * (breakCount == 5) { return IndiceConfiance.F; } else if
			 * (breakCount == 6) { return IndiceConfiance.G; } else { return
			 * IndiceConfiance.H; }
			 */
	}

	private static List<Colis> buildListColisFromDetailTournee(final DetailTournee tournee) {
		final List<Colis> listColis = new ArrayList<>();
		final List<Lt> ltAll = new ArrayList<Lt>();

		if (tournee.getPointsRealises() != null) {
			for (Point p /* pb de pneus ? ahah */ : tournee.getPointsRealises()) {
				ltAll.addAll(p.getLtsDuPoint());
			}
		}

		if (tournee.getPointsEnDistribution() != null) {
			for (Point p /* pb de pneus encore ? décidemment. */ : tournee.getPointsEnDistribution()) {
				ltAll.addAll(p.getLtsDuPoint());
			}
		}

		// on exclue ceux qui n'ont pas d'eta
		final List<Lt> ltAllWithETA = new ArrayList<Lt>();

		for (Lt lt : ltAll) {
			if (lt.getEta() != null) {
				ltAllWithETA.add(lt);
			}
		}

		if (ltAllWithETA.size() == 0) {
			return listColis;
		}

		// TODO : tri par date

		Collections.sort(ltAllWithETA, new Comparator<Lt>() {
			@Override
			public int compare(Lt lt1, Lt lt2) {
				// ils ont tous un eta a ce stade
				return lt1.getEta().compareTo(lt2.getEta());
			}
		});

		int i = 1;
		for (final Lt lt : ltAllWithETA) {
			Date dateRealisation = null;
			// realise ?
			if (lt.getEvenements() != null && isLtRealise(lt.getNoLt(), tournee.getPointsRealises())) {
				final Evt evt = LtRules.getPremierEvenementDplusDuJour(lt);
				if (evt == null || evt.getDateEvt() == null) {
					// TODO ! il faudrait plutot ne pas planter mais pour le
					// debug c'est cool
					// NOTE : il faudrait que le get detail tournée le
					// renvoie
					// se serait
					// plus sympa
					// logger.error("Date de realisation non trouvée mais colis
					// considéré comme realisé");
					// throw new RuntimeException("Bug calcul de l'indice de
					// confiance");
					continue;
				}
				dateRealisation = evt.getDateEvt();
			}

			/*
			 * if(lt.getPositionC11() == null) { // pas de position C11 sur un
			 * colis, on ne sait pas faire return null; }
			 */
			listColis.add(new Colis(i++, dateRealisation));
		}
		return listColis;
	}

	/**
	 * Est-ce que le LT est dans les points realisés
	 * 
	 * @param noLT
	 * @param pointsRealises
	 * @return
	 */
	private static boolean isLtRealise(final String noLT, final List<Point> pointsRealises) {
		// operation special pneus
		if (pointsRealises != null) {
			for (final Point p : pointsRealises) {
				if (p.getLtsDuPoint() != null) {
					for (final Lt lt : p.getLtsDuPoint()) {
						if (lt.getNoLt().equals(noLT)) {
							// algo moche mais iterations < 100 dans tous les
							// cas
							// et la deuxieme boucle est < 20 iterations
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Recherche 1 break
	 * 
	 * @param listColis
	 * @return
	 */
	private static boolean isBreak(final List<Colis> listColis) {

		// nous donne le dernier colis qui a été realise dans la liste
		final Colis dernierColisRealise = AlgoHelper.dernierColisRealiseEnDate(listColis);

		if (dernierColisRealise == null) {
			return false;
		}
		// on copie la liste et on enleve le dernier colis qu'on vient de
		// trouver
		// => photo de la liste des colis avant d'avoir realisé le dernier
		// colis
		// retour dans le passé de 1 colis
		final List<Colis> listColisAvantDernierColisRealise = new ArrayList<Colis>(listColis);
		final int indexOfDernierRealise = listColisAvantDernierColisRealise.indexOf(dernierColisRealise);
		listColisAvantDernierColisRealise.set(indexOfDernierRealise, new Colis(dernierColisRealise.numero, null));

		// on calcule le colis qui aurait du être réalisé
		// en prenant le premier non réalisé de la liste
		final Colis premierColisARealiserAvantDernierColisRealise = AlgoHelper
				.premierColisARealise(listColisAvantDernierColisRealise);

		if (premierColisARealiserAvantDernierColisRealise == null) {
			return false;
		}

		final Colis colisSuivantSerie = AlgoHelper.dernierColisRealiseEnDate(listColisAvantDernierColisRealise);

		if (colisSuivantSerie == null) {
			return false;
		}

		if (dernierColisRealise.numero != premierColisARealiserAvantDernierColisRealise.numero
				&& dernierColisRealise.numero != colisSuivantSerie.numero + 1) {
			return true;
		} else {
			return false;
		}
	}
}
