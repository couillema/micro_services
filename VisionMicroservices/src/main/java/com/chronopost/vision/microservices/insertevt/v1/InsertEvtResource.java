package com.chronopost.vision.microservices.insertevt.v1;

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

@Path("/")
@Api("/")
public class InsertEvtResource {

	private static final Logger logger = LoggerFactory.getLogger(InsertEvtResource.class);
    private IInsertEvtService service;

    public InsertEvtResource() {
        super();
    }

    public InsertEvtResource setService(IInsertEvtService service) {
        this.service = service;
        return this;
    }

    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/InsertEvt/v1")
    @ApiOperation(value = "Insertion d'une liste d'événements", notes = "Effectue des appels aux services GetLt, UpdateTournee, InsertLt et TraitementRetard", response = Boolean.class)
    public Response insertEvt(
            @ApiParam(required = true, value = "[   {    \"no_lt\": \"EE000000000FR\",     \"priorite_evt\":1000,     \"date_evt\": \"2015-09-25T17:20:00.006\",     \"cab_evt_saisi\": \"\",     \"cab_recu\": \"%0033076SK682493840248819250\",     \"code_evt\": \"SC\",     \"code_evt_ext\": \"\",     \"code_postal_evt\": \"\",     \"code_raison_evt\": \"\",     \"code_service\": \"\",     \"createur_evt\": \"SACAP01\",     \"date_creation_evt\": \"2015-09-25T18:20:53\",     \"id_acces_client\": 0,     \"id_extraction_evt\": \"1485386704\",     \"id_ss_code_evt\":null,     \"idbco_evt\": 6,     \"infoscomp\": {},    \"libelle_evt\": \"Tri effectué dans l''''agence de départ\",     \"libelle_lieu_evt\": \"\",     \"lieu_evt\": \"68999\",     \"position_evt\": null,     \"prod_cab_evt_saisi\": null,     \"prod_no_lt\": 1,     \"ref_extraction\": \"\",     \"ref_id_abonnement\": \"EVT_CHR\",     \"ss_code_evt\": \"68BOD\",     \"status_envoi\": \"\",     \"status_evt\": \"Acheminement en cours\"}]") List<Evt> evts) {
        try {
        	service.declareAppelMS();
            this.service.insertEvts(evts);
        } catch (Exception e) {
            logger.error("Erreur insertEvt v1 : " + e.getMessage(), e);
            service.declareFailMS();
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
        }

        return Response.status(Status.OK.getStatusCode()).entity(true).build();
    }

}