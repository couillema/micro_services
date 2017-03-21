package com.chronopost.vision.microservices.getEvts;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("getEvts")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GetEvtsResource {
	
    private IGetEvtsService service;

	public GetEvtsResource() {
        super();
    }

    /**
     * @param service Service de récupération des LTs
     * @return
     */
    public GetEvtsResource setService(final IGetEvtsService service) {
        this.service = service;
        return this;
    }

    /**
     * Retourne une liste de LTs avec résolution des synonymes
     * @param lt une liste (chaine de caractère) de numéros de LTs (séparés par des virgules) à rechercher 
     * @return La Liste des LTs recherchées avec résolution des synonymes
     */
    @GET
    @Path("{no_lt}")
    @Timed
    @ApiOperation("Pour un noLt donné, renvoie la liste de ses événements triès chronologiquement")
    public Response getEvts(@ApiParam(required = true, value = "Numéro de LT") @PathParam("no_lt") final String noLt) {
    	try {
    		final List<Evt> evts = service.getEvts(noLt);
			if (evts.size() > 0) {
				return Response.status(OK).entity(evts).build();
			} else {
				return Response.status(NO_CONTENT).build();
			}
		} catch (final Exception e) {
			return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
    }
}
