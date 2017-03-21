package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesInput;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
*
* Ressource exposant le service de récupération des alertes sur colis à risque.
* 
* @author adejanovski
*/
@Path(value = "/")
@Api("/")
public class GetAlertesTourneesResource {

    private final static Logger logger = LoggerFactory.getLogger(GetAlertesTourneesResource.class);
    private IGetAlertesTourneesService service;

    public GetAlertesTourneesResource() {
        service = GetAlertesTourneesServiceImpl.getInstance();
    }

    /**
     * Surcharge de l'objet service pour injecter des mocks.
     * 
     * @param service
     */
    public GetAlertesTourneesResource setService(IGetAlertesTourneesService service) {
        this.service = service;
        return this;
    }

    @POST
    @Timed
    @Path("/GetAlertesTourneesDuJour/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Récupération du nombre de colis à risque sur les tournées demandées",
	notes = "Prend en input une liste de codes tournée, et renvoie le nombre de colis à risque à la date/heure courante.<br/>"
			+ "Un colis à risque est un colis avec livraison sur créneau, sans evt D+, ayant une borne supérieure à moins de 30 minutes.")         
    public Response getAlertesTourneesDuJour(@ApiParam(required = true, value="[\"AJA20A01\",\"AJA20A02\"]") List<String> codesTournee) {
        GetAlertesTourneesInput parametres = new GetAlertesTourneesInput();
        parametres.setCodesTournee(codesTournee);
        parametres.setDateTournee(new Date());
        return getAlertesTournees(parametres);

    }

    @POST
    @Timed
    @Path("/GetAlertesTournees/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Récupération du nombre de colis à risque sur les tournées demandées, à la date/heure demandée",
	notes = "Prend en input une liste de codes tournée, et renvoie le nombre de colis à risque à la date/heure courante.<br/>"
			+ "Un colis à risque est un colis avec livraison sur créneau, sans evt D+, ayant une borne supérieure à moins de 30 minutes.")             
    public Response getAlertesTournees(@ApiParam(required = true, value="{\"codesTournee\":[\"AJA20A01\",\"AJA20A02\"],\"dateTournee\":\"2015-12-18T07:00:00.000\"}") GetAlertesTourneesInput parametres) {
        GetAlertesTourneesOutput alertes = new GetAlertesTourneesOutput();
        try {
            alertes = service.getAlertesTournees(parametres.getCodesTournee(), parametres.getDateTournee());
        } catch (NotFoundException | ParseException | InterruptedException | ExecutionException e) {
            logger.error("Erreur lors de l'appel au service getAlertesTournees", e);
            Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
            logger.error(ExceptionUtils.getStackTrace(e));
            return response;

        } catch (TimeoutException e) {
            Response response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e).build();
            return response;
        }

        Response response = Response.status(Status.OK).entity(alertes).build();

        return response;
    }

}