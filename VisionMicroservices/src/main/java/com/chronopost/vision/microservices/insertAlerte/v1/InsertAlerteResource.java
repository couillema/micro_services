package com.chronopost.vision.microservices.insertAlerte.v1;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;


/**
* Ressource REST utilisée par le MS insertAlerte au moyen du Framework Dropwizard
* Ressource d'insertion d'alertes(table alerte)
* 
* @author bjbari
*/
@Path("/")
@Api("/")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class InsertAlerteResource {
	

    /**
     * Log
     */
	private static final Logger logger = LoggerFactory.getLogger(InsertAlerteResource.class);

    /**
     * Service d'insertion des alertes
     */
    protected IInsertAlerteService service;

    public InsertAlerteResource() {
    }

    /**
     * Injection de la classe service.
     * 
     * @param pService
     *            : le service InsertAlerte instancié
     * @return l'objet resource initialisé
     */
    public InsertAlerteResource setService(IInsertAlerteService pService) {
        this.service = pService;
        return this;
    }
    
	@POST
	@Timed
	@Path("/InsertAlertes")
	@ApiOperation(value = "Insertion d'une liste d'alertes", notes = "")
	public Response insertAlertes(
			@ApiParam(required = true, value = "[{ \"agence\": \"62999\",\"jour\": \"20170116\",\"heure\": \"13\",\"type\": \"RPTSDTA\",\"noLt\": \"EE000000001FR\"}]") List<Alerte> alertes) {

		try {
			service.insertAlertes(alertes);
			return Response.status(Status.OK).entity(Boolean.TRUE).build();
		} catch (Exception e) {
			logger.error("Erreur InsertAlerte ( Liste Alertes ) v1 : " + e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}
	}

	@POST
	@Timed
	@Path("/InsertAlerte")
	@ApiOperation(value = "Insertion d'une alerte", notes = "")
	public Response insertAlerte(
			@ApiParam(required = true, value = "{ \"agence\": \"62999\",\"jour\": \"20170116\",\"heure\": \"13\",\"type\": \"RPTSDTA\",\"noLt\": \"EE000000001FR\"}") Alerte alerte) {

		try {
			service.insertAlertes(Arrays.asList(alerte));
			return Response.status(Status.OK).entity(Boolean.TRUE).build();
		} catch (Exception e) {
			logger.error("Erreur InsertAlerte ( une Alerte ) v1 : " + e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}
	}

}	
