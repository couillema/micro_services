package com.chronopost.vision.microservices.insertC11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.insertC11.TourneeC11;
import com.datastax.driver.core.exceptions.DriverException;

public enum InsertC11ServiceImpl implements IInsertC11Service {
	INSTANCE;

	private final Logger LOGGER = LoggerFactory.getLogger(InsertC11ServiceImpl.class);

	private IInsertC11Dao dao;

	@Override
	public void setDao(final IInsertC11Dao dao) {
		this.dao = dao;
	}

	/**
	 * Met à jour les différentes tables l'une après l'autre
	 */
	@Override
	public boolean traitementC11(final TourneeC11 tourneeC11) throws Exception {
		Boolean result = null;
		try {
			result = dao.miseAJourTournee(tourneeC11);
		} catch (final DriverException e) {
			LOGGER.error("Erreur dans miseAJourTournee pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(), e);
			throw new Exception(
					"Erreur dans miseAJourTournee pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(), e);
		}
		try {
			result = dao.miseAJourIdxTourneeJour(tourneeC11) && result;
		} catch (final DriverException e) {
			LOGGER.error(
					"Erreur dans miseAJourIdxTourneeJour pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(),
					e);
			throw new Exception(
					"Erreur dans miseAJourIdxTourneeJour pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(),
					e);
		}
		try {
			result = dao.miseAJourPoints(tourneeC11) && result;
		} catch (final DriverException e) {
			LOGGER.error("Erreur dans miseAJourPoints pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(), e);
			throw new Exception(
					"Erreur dans miseAJourPoints pour id_tournee " + tourneeC11.geTourneeVision().getIdC11(), e);
		}
		return result;
	}
}
