package com.chronopost.vision.microservices.getC11;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.PointC11Liste;
import com.chronopost.vision.model.insertC11.TourneeC11;

public enum GetC11ServiceImpl implements IGetC11Service {
	INSTANCE;

	private final Logger LOGGER = LoggerFactory.getLogger(GetC11ServiceImpl.class);

	private IGetC11Dao getC11Dao;

	/**
	 * Pour un poste comptable et un jour donnée, retourne une liste de
	 * TournéeC11 avec leurs PointsC11
	 */
	@Override
	public List<TourneeC11> getTournees(final String posteComptable, final String jour) throws Exception {
		try {
			// récupére, depuis idx_tournee_agence_jour, les tournées id selon
			// le poste comptable et jour donné
			final List<String> tourneesId = getC11Dao.getIdxTourneesByAgenceAndJour(posteComptable, jour);
			// récupére la liste de tournée depuis la liste d'id tournée
			final List<TourneeC11> tourneeC11s = getC11Dao.getTourneesById(tourneesId);
			// pour chaque tournée, retrouve ses points
			for (final TourneeC11 tourneeC11 : tourneeC11s) {
				final List<PointC11> pointC11s = getC11Dao
						.getPointsForTourneeId(new ArrayList<String>(tourneeC11.geTourneeVision().getIdsPoint()));
				final PointC11Liste pointC11Liste = new PointC11Liste(pointC11s);
				tourneeC11.geTourneeVision().setPointC11Liste(pointC11Liste);
			}
			return tourneeC11s;
		} catch (final Exception e) {
			LOGGER.error("Erreur getTournees pour posteComptable : " + posteComptable + " et jour : " + jour, e);
			throw e;
		}
	}

	@Override
	public void setDao(final IGetC11Dao getC11Dao) {
		this.getC11Dao = getC11Dao;
	}
}
