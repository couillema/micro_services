package com.chronopost.vision.microservices.insertC11;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.chronopost.vision.model.insertC11.TourneeC11;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Ressource REST utilisée par le MS InsertC11 Ressource d'insert des C11
 * 
 * @author XRE
 */
@Path("C11")
@Api("/")
@Consumes(MediaType.APPLICATION_JSON)
public class InsertC11Resource {

	private IInsertC11Service service;

	/**
	 * Traite fichier C11 envoyé en POST par l'EAI
	 * @param tourneeC11
	 * @return Response with boolean body for updateDB status
	 */
	@Path("v1")
	@POST
	@Timed
	@ApiOperation(value = "Insert C11", notes = "Traite la feuille de tournée afin de créer ou compléter en base la tournée et les point de tournée")
	public Response insertC11(@ApiParam(name="tourneeC11", required=true) final TourneeC11 tourneeC11) {
		Boolean resultInsert = false;
		try {
			resultInsert = service.traitementC11(tourneeC11);
			return Response.status(Status.OK).entity(String.valueOf(resultInsert)).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	public void setService(IInsertC11Service service) {
		this.service = service;
	}
}
