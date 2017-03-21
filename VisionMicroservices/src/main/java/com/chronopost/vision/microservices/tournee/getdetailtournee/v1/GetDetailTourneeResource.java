package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.util.concurrent.TimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.rules.DateRules;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path(value = "/")
@Api("/")
public class GetDetailTourneeResource {

	private final static Logger logger = LoggerFactory.getLogger(GetDetailTourneeResource.class);
	private IGetDetailTourneeService service;
	private GetLtV1 getLt;

	public GetDetailTourneeResource() {
		service = GetDetailTourneeServiceImpl.getInstance();
	}

	/**
	 * Surcharge de l'objet service pour injecter des mocks.
	 * 
	 * @param service
	 */
	public GetDetailTourneeResource setService(IGetDetailTourneeService service) {
		this.service = service;
		return this;
	}

	/**
	 * Surcharge de l'objet getLtV1 pour injecter des mocks.
	 * 
	 * @param service
	 */
	public GetDetailTourneeResource setGetLt(GetLtV1 getLtService) {
		this.getLt = getLtService;
		return this;
	}

	@GET
	@Timed
	@Path("/GetDetailTournee/v1/{dateTournee}/{codeTournee}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@ApiOperation(value = "Récupération du détail d'une tournée.",
	notes = "Le service prend en input une date et un code tournée sur 8 caractères, code_agence (3 car.) + code tournée (5 car.)<br/>"
			+ "et renvoie une structure contenant la liste des points à réaliser, la liste des points réalisés, la liste des LT collectées<br/>"
			+ "et la liste des positions GPS remontées par le PSM.<br/>"
			+ "Le suivi complet et les infos LT de chaque colis sont disponibles dans la liste des points (aucun appel complémentaire au GetLT n'est nécessaire).")             
	public Response getDetailTourneeFromCodeTournee(@PathParam(value = "dateTournee") String dateTournee,
			@PathParam(value = "codeTournee") String codeTournee) {
		DetailTournee detailTournee = null;
		try {
			detailTournee = service.getDetailTournee(DateRules.fromDateSortable(dateTournee), codeTournee, this.getLt);
		} catch (Exception e) {
			if (e instanceof TimeoutException) {
				Response response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e).build();
				return response;
			}
			logger.error("Erreur lors de l'appel au service getDetailTournee", e);
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
			logger.error(ExceptionUtils.getStackTrace(e));
			return response;
		}
		Response response = Response.status(Status.OK).entity(detailTournee).build();
		return response;
	}
}