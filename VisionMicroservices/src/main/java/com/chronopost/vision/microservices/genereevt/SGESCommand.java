package com.chronopost.vision.microservices.genereevt;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.sgesws.cxf.ResultWS;
import fr.chronopost.sgesws.cxf.SGESServiceWS;

/**
 * 
 * Classe de commande Hystrix encapsulant les appels à SGES
 * 
 * @author jcbontemps
 *
 */
public class SGESCommand extends HystrixCommand<Boolean> {

	private final SGESServiceWS service;
	private final GenererEvtDTO dto;

	protected SGESCommand(final GenererEvtDTO dto, final SGESServiceWS service) {
		super(HystrixCommandGroupKey.Factory.asKey("SGESCommand"));
		this.dto = dto;
		this.service = service;
	}

	@Override
	protected Boolean run() throws MSTechnicalException {
		final ResultWS result = service.genererEvenementICNT("VisionMicroservices", dto.getCodeSiteClient(),
				dto.getIdPosteClient(), dto.getOperateur(), dto.getNumeroObjet(), dto.getNouveauNumeroObjet(),
				dto.getTournee(), dto.getTypeEvenement(), dto.getStatusCode(), dto.getDateEvenement(),
				dto.getHeureEvenement(), dto.getIc1Code(), dto.getIc1Value(), dto.getIc2Code(), dto.getIc2Value(),
				dto.getIc3Code(), dto.getIc3Value(), dto.getIc4Code(), dto.getIc4Value(), dto.getIc5Code(),
				dto.getIc5Value(), dto.getIc6Code(), dto.getIc6Value(), dto.getIc7Code(), dto.getIc7Value(),
				dto.getIc8Code(), dto.getIc8Value(), dto.getIc9Code(), dto.getIc9Value(), dto.getIc10Code(),
				dto.getIc10Value(), dto.getIc11Code(), dto.getIc11Value(), dto.getIc12Code(), dto.getIc12Value(),
				dto.getIc13Code(), dto.getIc13Value(), dto.getIc14Code(), dto.getIc14Value(), dto.getIc15Code(),
				dto.getIc15Value());
		if (result.getErrorCode() == 0)
			return true;
		else {
			throw new MSTechnicalException(
					"Demande de création d'événement en erreur auprès du WebService SGES. Code retour:"
							+ result.getErrorCode() + " " + result.getErrorMessage());
		}
	}

	/*
	 * Réponse en cas d'échec
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.netflix.hystrix.HystrixCommand#getFallback()
	 */
	@Override
	public Boolean getFallback() {
		throw new MSTechnicalException("Erreur SGESCommand : " + getFailedExecutionException().getMessage(),
				getFailedExecutionException());
	}
}
