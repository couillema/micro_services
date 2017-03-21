package com.chronopost.vision.microservices.diffusionevt.v1;

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

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.diffusionevt.v1.DiffusionEvtResponse;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/")
@Api("/")
public class DiffusionEvtResource {
	
	private final static Logger logger = LoggerFactory.getLogger(DiffusionEvtResource.class);
	
	/**
	 * Service DiffusionEvt
	 */
	private IDiffusionEvtService service = null;

	public DiffusionEvtResource setService(IDiffusionEvtService service) {
		this.service = service;
		return this;
	}
	
	@POST
	@Timed
	@Path("DiffusionEvt/v1")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Diffusion d'une liste d'événements, encapsulés chacun dans un objet Lt", notes = "Diffuse les lt passés en paramètres dans un topic JMS, en générant certaines propriétés JMS pour filtrer.", response = DiffusionEvtResponse.class)
	public Response diffusionEvt(@ApiParam(name="lts", required=true) List<Lt> lts){		
		try {			
			Boolean result = service.diffusionEvt(lts);
			
			return Response.status(Status.OK).entity(new DiffusionEvtResponse(result)).build();
		} catch (Exception e) {
			logger.error("Erreur DiffusionEvt" , e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new MSTechnicalException(e)).build();
		}		
	}
	
	

}
