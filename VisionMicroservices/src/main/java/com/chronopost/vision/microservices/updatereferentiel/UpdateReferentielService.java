package com.chronopost.vision.microservices.updatereferentiel;

import java.util.List;
import java.util.Map;

import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratVision;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * 
 * Interface de mise à jour des transcodifications
 * 
 * @author jcbontemps
 *
 */
public interface UpdateReferentielService {

	/**
	 * Met à jour la famille id_infoscomp des transcos
	 * 
	 * @param infoscomp
	 *            une Map d'infoscomp à mettre à jour dans les
	 *            transcodifications
	 * @return une liste de resultats des insertions / mise à jour
	 */
	List<Boolean> updateInfoscomp(Map<String, String> infoscomp);

	/**
	 * Met à jour les familles evenements et code_id_evt des transcos
	 * 
	 * @param evts
	 *            une Liste de données de mise à jour des evts dans les
	 *            transcodifications
	 * @return une liste de resultats des insertions / mise à jour
	 */
	List<Boolean> updateEvt(List<DefinitionEvt> evts);

	/**
	 * @param mockTranscoderDao
	 *            le TranscoderDao utilisé pour mettre à jour les
	 *            transcodifications
	 * @return L'objet lui-même. Utile pour injecter à l'instanciation
	 */
	UpdateReferentielService setDao(ITranscoderDao mockTranscoderDao);

	void insertRefContrat(ContratVision refContrat) throws DriverException;

	boolean checkInsertVersionComplete(String version, int nbContrats) throws DriverException;

	void updateParametreVersionContrat(String version) throws DriverException;

	UpdateReferentielService setDaoVision(IReferentielVisionDao daoVision);
}
