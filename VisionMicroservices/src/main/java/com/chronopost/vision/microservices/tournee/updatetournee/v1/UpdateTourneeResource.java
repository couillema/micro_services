package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import java.util.List;

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
* Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework Dropwizard
* Ressource de mise à jour des tournées en fonction des évènements reçus
* 
 */
@Path("/")
@Api("/")
public class UpdateTourneeResource {

	private static final Logger logger = LoggerFactory.getLogger(UpdateTourneeResource.class);
    private IUpdateTourneeService service;

    public UpdateTourneeResource() {
        super();
    }

    /**
     * Unjection d'un IUpdateTourneeService
     * @param service un service implémentant l'interface IUpdateTourneeService
     * @return l'objet UpdateTourneeResource. Pratique pour injecter durant l'instanciation 
     */
    public UpdateTourneeResource setService(IUpdateTourneeService service) {
        this.service = service;
        return this;
    }

    /**
     * Mise à jour des tables de suivi des tournées.
     * Met à jour toutes les tables d'index ayant à voir avec le suivi des tournées.
     * Le service InsertEvt appelle ce service avec tous les événements qui lui sont soumis
     * et seuls ceux porteurs d'un id C11 en infos comp donnent lieu à une mise à jour des tables de tournées.
     * @param evts
     * @return
     */
    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/UpdateTournee/v1")
    @ApiOperation(value = "Mise à jour des tables de suivi des tournées.", notes = "Met à jour toutes les tables d'index ayant à voir avec le suivi des tournées.<br/>"
            + "Le service InsertEvt appelle ce service avec tous les événements qui lui sont soumis<br/>"
            + "et seuls ceux porteurs d'un id C11 en infos comp donnent lieu à une mise à jour des tables de tournées.")
    public Response InsertEvt(
            @ApiParam(required = true, value = "[   {    \"no_lt\": \"EE000000000FR\",     \"priorite_evt\":1000,     \"date_evt\": \"2015-09-25T17:20:00.006\",     \"cab_evt_saisi\": \"\",     \"cab_recu\": \"%0033076SK682493840248819250\",     \"code_evt\": \"SC\",     \"code_evt_ext\": \"\",     \"code_postal_evt\": \"\",     \"code_raison_evt\": \"\",     \"code_service\": \"\",     \"createur_evt\": \"SACAP01\",     \"date_creation_evt\": \"2015-09-25T18:20:53\",     \"id_acces_client\": 0,     \"id_extraction_evt\": \"1485386704\",     \"id_ss_code_evt\":null,     \"idbco_evt\": 6,     \"infoscomp\": {},    \"libelle_evt\": \"Tri effectué dans l''''agence de départ\",     \"libelle_lieu_evt\": \"\",     \"lieu_evt\": \"68999\",     \"position_evt\": null,     \"prod_cab_evt_saisi\": null,     \"prod_no_lt\": 1,     \"ref_extraction\": \"\",     \"ref_id_abonnement\": \"EVT_CHR\",     \"ss_code_evt\": \"68BOD\",     \"status_envoi\": \"\",     \"status_evt\": \"Acheminement en cours\"}]") List<Evt> evts) {

        try {
            this.service.updateTournee(evts);
        } catch (Exception e) {
        	logger.error("Erreur UpdateTournee", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
        }

        return Response.status(Status.OK.getStatusCode()).entity(true).build();
    }
}