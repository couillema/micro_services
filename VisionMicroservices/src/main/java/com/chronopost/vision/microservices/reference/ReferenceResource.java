package com.chronopost.vision.microservices.reference;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Ressource REST retournant les ref Agence, Code Service, Evenement et Parametre
 * 
 * @author XRE
 */
@Path("reference")
@Api("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ReferenceResource {

	private IReferenceService referenceService;

	@GET
	@Path("service")
	@Timed
	@ApiOperation(value = "getCodeService", notes = "Retourne un set contenant les codes service depuis Vision")
	public Response getCodeService() {
		final Set<CodeService> codesService = referenceService.getCodesService();
		return Response.status(Status.OK).entity(codesService).build();
	}

	@GET
	@Path("evenement")
	@Timed
	@ApiOperation(value = "getEvenements", notes = "Retourne un set contenant les evenements depuis Vision")
	public Response getEvenements() {
		final Set<Evenement> evenements = referenceService.getEvenements();
		return Response.status(Status.OK).entity(evenements).build();
	}

	@GET
	@Path("agence")
	@Timed
	@ApiOperation(value = "getAgences", notes = "Retourne un set contenant les agence depuis Vision")
	public Response getAgences() {
		final Set<Agence> agences = referenceService.getAgences();
		return Response.status(Status.OK).entity(agences).build();
	}

	@GET
	@Path("parametre/{parametreName}")
	@Timed
	@ApiOperation(value = "getParametre", notes = "Retourne un String contenant la valeur en base, liée à la clé en paramétre, dans la table Parametre")
	public Response getParametre(@PathParam("parametreName") @ApiParam(required=true, value="Nom du parametre", name="parametreName") final String paramName) {
		final String parametreValue = referenceService.getParametreValue(paramName);
		return Response.status(Status.OK).entity(parametreValue).build();
	}

	public void setService(final ReferenceServiceImpl referenceService) {
		this.referenceService = referenceService;
	}
}
