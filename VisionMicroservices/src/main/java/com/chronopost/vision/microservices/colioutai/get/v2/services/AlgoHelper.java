package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.util.List;

public class AlgoHelper {

	public static Colis dernierColisRealiseEnDate(List<Colis> listColis) {

		Colis latestColisRealise = null;

		for (Colis colis : listColis) {

			if (colis.realise && (latestColisRealise == null
					|| colis.dateRealisation.getTime() > latestColisRealise.dateRealisation.getTime())) {
				latestColisRealise = colis;
			}
		}

		return latestColisRealise;
	}

	public static Colis premierColisARealise(List<Colis> listColisAvantDernierColisRealise) {

		for (Colis colis : listColisAvantDernierColisRealise) {

			if (!colis.realise) {
				return colis;
			}
		}

		return null;
	}
}
