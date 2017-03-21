package com.chronopost.vision.microservices.suivibox;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.suiviBox.v1.SuiviBoxV1Output;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
*
* Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework Dropwizard
* Ressource d'insertion des Evenements GC dans la base Cassandra
* 
* @author jcbontemps
*/
@Path("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Api("/")
public class SuiviboxResource {

    /**
     * Service d'insertion des evenements GC
     */
    private SuiviboxService service;

    public SuiviboxResource() {
    }

    public SuiviboxResource(SuiviboxService service) {
        this.service = service ;
    }

    public SuiviboxResource setService(SuiviboxService service) {
        this.service = service;
        return this;
    }

    /**
     * Méthode appelée sur appel HTTP distant de l'URL /SuiviBox/ en POST 
     * la méthode fait appel au IInsertSuiviboxService service pour traiter les evts.    
     * @param evts liste d'évènements soumis à l'url 
     * @return true et code 200 si l'insertion s'est bien passée (tru si pas d'insertion car événement non GC), 500 et false si une erreur est survenue durant le process
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/SuiviBox")
    @ApiOperation(value = "Mise à jour du suivi des box sur réception des événements GC",
	notes = "Retourne true et code 200 si l'insertion s'est bien passée (true si pas d'insertion car événement non GC), 404 et false si une erreur est survenue durant le process.")     
    public Response insertLts(@ApiParam(required=true, value="[{ \"no_lt\": \"SK682493840FR\",     \"priorite_evt\":1000,     \"date_evt\": \"2015-09-25T17:20:00.006\",     \"cab_evt_saisi\": \"\",     \"cab_recu\": \"%0033076SK682493840248819250\",     \"code_evt\": \"SC\",     \"code_evt_ext\": \"\",     \"code_postal_evt\": \"\",     \"code_raison_evt\": \"\",     \"code_service\": \"\",     \"createur_evt\": \"SACAP01\",     \"date_creation_evt\": \"2015-09-25T18:20:53\",     \"id_acces_client\": 0,     \"id_extraction_evt\": \"1485386704\",     \"id_ss_code_evt\":null,     \"idbco_evt\": 6,     \"infoscomp\": {},    \"libelle_evt\": \"Tri effectué dans l'agence de départ\",     \"libelle_lieu_evt\": \"\",     \"lieu_evt\": \"68999\",     \"position_evt\": null,     \"prod_cab_evt_saisi\": null,     \"prod_no_lt\": 1,     \"ref_extraction\": \"\",     \"ref_id_abonnement\": \"EVT_CHR\",     \"ss_code_evt\": \"68BOD\",     \"status_envoi\": \"\",     \"status_evt\": \"Acheminement en cours\"}]")  List<Evt> evts) {
        Boolean insertEvtsGCInDatabase = service.insertEvtGCInDatabase(evts);
		if (insertEvtsGCInDatabase) {
			return Response.status(Status.OK).entity(new SuiviBoxV1Output().setStatus(Boolean.TRUE)).build();
		} else {
			return Response.status(Status.OK).entity(new SuiviBoxV1Output().setStatus(Boolean.FALSE)).build();
		}
    }
}
