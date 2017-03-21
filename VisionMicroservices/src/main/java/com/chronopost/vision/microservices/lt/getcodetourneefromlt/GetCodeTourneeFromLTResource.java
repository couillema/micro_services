package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import java.text.ParseException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.rules.DateRules;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Recherche d'une tournee par son code
 * 
 * @author vdesaintpern
 *
 */
@Path("/")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GetCodeTourneeFromLTResource {

	private IGetCodeTourneeFromLTService service;

	public GetCodeTourneeFromLTResource setService(IGetCodeTourneeFromLTService service) {
		this.service = service;
		return this;
	}

	public GetCodeTourneeFromLTResource() {
		super();
	}

	@GET
	@Timed
	@Path("/GetCodeTourneeFromLT/{no_lt}/{dateHeureSearch}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recherche du code de la tournée ayant en charge une LT à la date passée en paramètres",
	notes = "Ce service renvoie le code de la dernière tournée ayant renvoyé un événement pour le numéro de LT recherché, à la date passée en paramètres.<br/>"
			+ "Si une autre tournée a pris en charge le colis après cette date, elle est ignorée.") 
    	public Response findTourneeBy(@ApiParam(required=true, value="Numéro de LT", defaultValue="EE000000001FR") @PathParam("no_lt") String noLT,
    			@ApiParam(required=true, value="Date heure de recherche", defaultValue="2015-12-24T23:00:00.000") @PathParam("dateHeureSearch") String dateHeureSearch) {

		// checkking parameters
		if (noLT == null || noLT.trim().equals("")) {
			return Response.status(400).entity("No de LT non fourni. Ce paramètre est obligatoire.").build();
		}

		if (dateHeureSearch == null || dateHeureSearch.trim().equals("")) {
			return Response.status(400).entity("Date de recherche non fournie. Ce paramètre est obligatoire.").build();
		}

		Date dateHeure = null;

		try {
			dateHeure = DateRules.toDateWS(dateHeureSearch);
		} catch (ParseException e) {
			return Response.status(400)
					.entity("Erreur de parsing de la date de recherche. Merci de fournir une date au format yyyy-MM-ddTHH:mm:ss")
					.build();
		}

		// looking the database
		GetCodeTourneeFromLTResponse tournee = null;

		try {
			tournee = service.findTourneeBy(noLT, dateHeure);

		} catch (GetCodeTourneeFromLTException e) {

			if (e.getCodeErreur() != null) {
				if (e.getCodeErreur().equals(GetCodeTourneeFromLTException.LT_NOT_FOUND)) {
					// bad request
					return Response.status(404).entity("Numéro LT introuvable").build();
				} else if (e.getCodeErreur().equals(GetCodeTourneeFromLTException.TOURNEE_NOT_FOUND)) {
					// not found
					return Response.status(404).entity(new GetCodeTourneeFromLTResponse()).build();
				}
			}

			// on purpose, should fail in this case with a 500
			throw new RuntimeException("Erreur systeme (bug)", e);
		}

		// not so useful but just in case the service goes tits up
		if (tournee == null) {
			// not found
			return Response.status(404).entity("Tournée introuvable").build();
		}

		// we're all good
		return Response.status(200).entity(tournee).build();
	}
}
