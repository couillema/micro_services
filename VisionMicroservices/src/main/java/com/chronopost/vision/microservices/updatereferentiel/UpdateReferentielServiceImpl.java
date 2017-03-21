package com.chronopost.vision.microservices.updatereferentiel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratVision;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * 
 * classe de mise à jour des transcodifications
 * 
 * @author jcbontemps
 *
 */
public class UpdateReferentielServiceImpl implements UpdateReferentielService {

	private ITranscoderDao dao;
	private IReferentielVisionDao daoVision;

	/**
	 * Singleton
	 */
	static class InstanceHolder {
		public static UpdateReferentielService service = new UpdateReferentielServiceImpl();
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static UpdateReferentielService getInstance() {
		return InstanceHolder.service;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chronopost.vision.microservices.updatereferentiel.
	 * UpdateReferentielService#updateInfoscomp(java.util.Map)
	 */
	public List<Boolean> updateInfoscomp(final Map<String, String> infoscomp) {
		final List<Boolean> updatedInfoscomp = new ArrayList<Boolean>();

		try {
			// appels parallélisés du WS SGES
			final Map<String, Future<Boolean>> updateInfoscompFutures = new HashMap<>();
			for (final Map.Entry<String, String> entry : infoscomp.entrySet()) {
				updateInfoscompFutures.put(entry.getKey(),
						new UpdateReferentielInfoscompCommand(entry.getKey(), entry.getValue(), dao).queue());
			}

			// enregistrement des résultats de l'insertion par SGES
			for (final String id : updateInfoscompFutures.keySet()) {
				final Future<Boolean> futur = updateInfoscompFutures.get(id);
				updatedInfoscomp.add(futur.get());
			}
		} catch (InterruptedException e) {
			throw new MSTechnicalException(
					"Une erreur de type InterruptedException est intervenue dans com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielService.updateInfoscomp",
					e);
		} catch (ExecutionException e) {
			throw new MSTechnicalException(
					"Une erreur de type ExecutionException est intervenue dans com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielService.updateInfoscomp",
					e);
		}

		return updatedInfoscomp;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chronopost.vision.microservices.updatereferentiel.
	 * UpdateReferentielService#updateEvt(java.util.List)
	 */
	public List<Boolean> updateEvt(final List<DefinitionEvt> evts) {
		final List<Boolean> updatedEvt = new ArrayList<Boolean>();

		try {
			final List<Future<Boolean>> updateEvtFutures = new ArrayList<>();

			for (DefinitionEvt evt : evts) {
				updateEvtFutures.add(new UpdateReferentielEvtCommand(evt, dao).queue());
			}

			for (Future<Boolean> updateEvtFuture : updateEvtFutures) {
				updatedEvt.add(updateEvtFuture.get());
			}
		} catch (InterruptedException e) {
			throw new MSTechnicalException(
					"Une erreur de type InterruptedException est intervenue dans com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielServiceImpl.updateEvt",
					e);
		} catch (ExecutionException e) {
			throw new MSTechnicalException(
					"Une erreur de type ExecutionException est intervenue dans com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielServiceImpl.updateEvt",
					e);
		}

		return updatedEvt;
	}

	@Override
	public void insertRefContrat(final ContratVision refContrat) throws DriverException {
		daoVision.insertRefContrat(refContrat.getVersion(), refContrat.getListeContrats());
	}

	@Override
	public boolean checkInsertVersionComplete(final String version, final int nbContrats) throws DriverException {
		return daoVision.checkInsertVersionComplete(version, nbContrats);
	}

	@Override
	public void updateParametreVersionContrat(final String version) throws DriverException {
		daoVision.updateParametreVersionContrat(version);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chronopost.vision.microservices.updatereferentiel.
	 * UpdateReferentielService
	 * #setDao(com.chronopost.vision.transco.dao.ITranscoderDao)
	 */
	@Override
	public UpdateReferentielService setDao(final ITranscoderDao dao) {
		this.dao = dao;
		return this;
	}

	@Override
	public UpdateReferentielService setDaoVision(final IReferentielVisionDao daoVision) {
		this.daoVision = daoVision;
		return this;
	}
}
