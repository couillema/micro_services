package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * Point d'entrée du MS insertAgenceColis qui assure le maintient de la table colis_agence
 * 
 * @author LGY
 */
@Path("/InsertAgenceColis")
@Api("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class InsertAgenceColisResource {

	/**
	 * Log
	 */
	private static final Logger logger = LoggerFactory.getLogger(InsertAgenceColisResource.class);

	/**
	 * Service d'insertion colis saisis par agence
	 */
	protected IInsertAgenceColisService service;

	public InsertAgenceColisResource() {
	}

	/**
	 * Injection de la classe service.
	 * 
	 * @param pService
	 *            : le service InsertPointTournee instancié
	 * @return l'objet resource initialisé
	 */
	public InsertAgenceColisResource setService(IInsertAgenceColisService pService) {
		this.service = pService;
		return this;
	}

	@POST
	@Timed
	@Path("/v1")
	@ApiOperation(value = "Maintenance de la liste des colis saisis dans une agence sur une période de temps.", notes = "Tous les événements possédant un lieu sont considérés")
	public Response insertAgenceColis(
			@ApiParam(required = true, value = "[ { \"no_lt\": \"EE000000000FR\",     \"priorite_evt\":1000,     \"date_evt\": \"2015-09-25T17:20:00.006\",     \"cab_evt_saisi\": \"\",     \"cab_recu\": \"%0033076SK682493840248819250\",     \"code_evt\": \"SC\",     \"code_evt_ext\": \"\",     \"code_postal_evt\": \"\",     \"code_raison_evt\": \"\",     \"code_service\": \"\",     \"createur_evt\": \"SACAP01\",     \"date_creation_evt\": \"2015-09-25T18:20:53\",     \"id_acces_client\": 0,     \"id_extraction_evt\": \"1485386704\",     \"id_ss_code_evt\":null,     \"idbco_evt\": 6,     \"infoscomp\": {},    \"libelle_evt\": \"Tri effectué dans l''''agence de départ\",     \"libelle_lieu_evt\": \"\",     \"lieu_evt\": \"68999\",     \"position_evt\": null,     \"prod_cab_evt_saisi\": null,     \"prod_no_lt\": 1,     \"ref_extraction\": \"\",     \"ref_id_abonnement\": \"EVT_CHR\",     \"ss_code_evt\": \"68BOD\",     \"status_envoi\": \"\",     \"status_evt\": \"Acheminement en cours\" } ]") List<Evt> evenements) {

		try {
			service.declareAppelMS();
			if (service.traiteEvenement(evenements) == true) 
				return Response.status(Status.OK).entity(Boolean.TRUE).build();
			else 
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(Boolean.FALSE).build();
		} catch (Exception e) {
			logger.error("Erreur insertAgenceColis v1 : " + e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}
	}

	@POST
	@Timed
	@Path("/restanttg2")
	@ApiOperation(value = "Insertion des colis restants tg2")
	public Response setColisRestantTG2(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @QueryParam("agence") final String agence,
			@ApiParam(required = true, value = "Le jour concerné au format \"yyyymmdd\"", defaultValue = "20163001") @QueryParam("jour")  final String jour,
			@ApiParam(required = true, value = "[\"EE000000000FR\"]") final Set<String> noLts) {
		try {
			service.declareAppelMS();
			if (service.setRestantTG2(agence, jour, noLts) == true)
				return Response.status(Status.OK).entity(Boolean.TRUE).build();
			else {
				service.declareFailMS();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(Boolean.FALSE).build();
			}
		} catch (Exception e) {
			logger.error("Erreur insertAgenceColis restanttg2 v1 : " + e.getMessage(), e);
			service.declareFailMS();
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}
	}

}
