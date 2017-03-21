package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.microservices.sdk.exception.ServiceUnavailableException;
import com.chronopost.vision.microservices.sdk.exception.TechnicalException;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;

public class GetDetailTourneeServiceImpl implements IGetDetailTourneeService {

	private IGetDetailTourneeDao tourneeDao;

	/**
	 * Singleton
	 */
	static class InstanceHolder {

		public static IGetDetailTourneeService service = new GetDetailTourneeServiceImpl();

	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static IGetDetailTourneeService getInstance() {

		return InstanceHolder.service;
	}

	@Override
	public IGetDetailTourneeService setDao(IGetDetailTourneeDao dao) {
		this.tourneeDao = dao;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chronopost.vision.microservices.tournee.getdetailtournee.v1.services
	 * .IGetDetailTourneeService#getDetailTournee(java.util.Date,
	 * java.lang.String)
	 */
	@Override
	public DetailTournee getDetailTournee(Date dateTournee, String codeTournee, GetLtV1 getLtV1) throws Exception {

		Tournee tournee = tourneeDao.getTournee(codeTournee, dateTournee);

		DetailTournee detailTournee = new DetailTournee().setIdC11(tournee.getCodeTournee())
				.setCodeAgence(codeTournee.substring(0, 3)).setCodeTournee(codeTournee.substring(3));

		// liste detail de LTs
		Map<String, Lt> ltsDeLaTournee = getDetailLts(tournee, getLtV1);

		// points en distribution
		detailTournee
				.setPointsEnDistribution(LtRules.getPoints(ltsDeLaTournee, LtRules.POINTS_NON_DISTRIBUES, dateTournee));

		// liste des points réalisés
		detailTournee.setPointsRealises(LtRules.getPoints(ltsDeLaTournee, LtRules.POINTS_DISTRIBUES, dateTournee));

		// LT collectés
		detailTournee.setLtsCollecte(LtRules.getLtsCollecte(ltsDeLaTournee, dateTournee));

		// Relevés GPS
		detailTournee.setRelevesGps(tournee.getRelevesGps());

		// informations de tournée
		detailTournee.setInformations(tournee.getInformations());

		return detailTournee;
	}

	/**
	 * Appel du service GetLtV1 pour récupérer le détail des LT de la tournée.
	 * 
	 * @param tournee
	 * @return
	 * @throws NotFoundException
	 * @throws ServiceUnavailableException
	 * @throws TechnicalException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private Map<String, Lt> getDetailLts(Tournee tournee, GetLtV1 getLtV1)
			throws TechnicalException, ServiceUnavailableException, NotFoundException {
		Map<String, Lt> lts = getLtV1.getLt(tournee.getLtsDeLaTournee());
		return lts;
	}
}
