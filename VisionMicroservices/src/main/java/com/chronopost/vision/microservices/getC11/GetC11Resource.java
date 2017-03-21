package com.chronopost.vision.microservices.getC11;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
@Path("getC11")
@Api("/")
@Consumes(APPLICATION_JSON)
public class GetC11Resource {

	private IGetC11Service service;

	/**
	 * Pour une agence et un jour donné, retourne les tournées et leurs points
	 * 
	 * @param posteComptable
	 * @param jour
	 * @return Response contenant une liste de TourneeC11
	 */
	@GET
	@Path("{posteComptable}/{jour}")
	@Timed
	@ApiOperation(value = "Get C11", notes = "Pour une agence et un jour donnés retourne la liste des tournées et leurs points")
	public Response getC11(
			@PathParam("posteComptable") @ApiParam(name = "posteComptable", value = "99999", required = true) final String posteComptable,
			@PathParam("jour") @ApiParam(name = "jour", value = "YYYYMMDD", required = true) final String jour) {
		try {
			final List<TourneeC11> tournees = service.getTournees(posteComptable, jour);
			if (null != tournees && tournees.size() > 0) {
				return Response.status(OK).entity(tournees).build();
			} else {
				return Response.status(NO_CONTENT).build();
			}
		} catch (final Exception e) {
			return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	public void setService(final IGetC11Service service) {
		this.service = service;
	}
}
