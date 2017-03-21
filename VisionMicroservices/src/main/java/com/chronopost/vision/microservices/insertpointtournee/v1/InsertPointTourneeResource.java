package com.chronopost.vision.microservices.insertpointtournee.v1;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
 * Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework
 * Dropwizard Ressource de traitement de l'insertion/maintenance des points des
 * tournées (table tournee_point)
 * 
 * @author LGY
 */
@Path("/")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class InsertPointTourneeResource {

    /**
     * Log
     */
	private static final Logger logger = LoggerFactory.getLogger(InsertPointTourneeResource.class);

    /**
     * Service d'insertion des point GC
     */
    protected IInsertPointTourneeService service;

    public InsertPointTourneeResource() {
    }

    /**
     * Injection de la classe service.
     * 
     * @param pService
     *            : le service InsertPointTournee instancié
     * @return l'objet resource initialisé
     */
    public InsertPointTourneeResource setService(IInsertPointTourneeService pService) {
        this.service = pService;
        return this;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/InsertPointTournee/v1")
    @ApiOperation(value = "Maintenance des points des tournées.", notes = "Seuls les événements de livraison (TA et D+) sont considérés ici. "
            + "Si le point indiqué par l'événement n'existe pas, il est créé, sinon il est complété.")
    public Response insertPointTournee(
            @ApiParam(required = true, value = "[ { \"no_lt\": \"EE000000000FR\",     \"priorite_evt\":1000,     \"date_evt\": \"2015-09-25T17:20:00.006\",     \"cab_evt_saisi\": \"\",     \"cab_recu\": \"%0033076SK682493840248819250\",     \"code_evt\": \"SC\",     \"code_evt_ext\": \"\",     \"code_postal_evt\": \"\",     \"code_raison_evt\": \"\",     \"code_service\": \"\",     \"createur_evt\": \"SACAP01\",     \"date_creation_evt\": \"2015-09-25T18:20:53\",     \"id_acces_client\": 0,     \"id_extraction_evt\": \"1485386704\",     \"id_ss_code_evt\":null,     \"idbco_evt\": 6,     \"infoscomp\": {},    \"libelle_evt\": \"Tri effectué dans l''''agence de départ\",     \"libelle_lieu_evt\": \"\",     \"lieu_evt\": \"68999\",     \"position_evt\": null,     \"prod_cab_evt_saisi\": null,     \"prod_no_lt\": 1,     \"ref_extraction\": \"\",     \"ref_id_abonnement\": \"EVT_CHR\",     \"ss_code_evt\": \"68BOD\",     \"status_envoi\": \"\",     \"status_evt\": \"Acheminement en cours\" } ]") List<Evt> evenements) {

        // TODO traitement du retour false.
        try {
        	service.declareAppelMS();
            if (service.traiteEvenement(evenements) == true){
            	return Response.status(Status.OK).entity(Boolean.TRUE).build();
            }
            else {
            	service.declareFailMS();
            	return Response.status(Status.INTERNAL_SERVER_ERROR).entity(Boolean.FALSE).build();
            }
        } catch (Exception e) {
            logger.error("Erreur insertPointTournee v1 : " + e.getMessage(), e);
        	service.declareFailMS();
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
        }
    }

}
