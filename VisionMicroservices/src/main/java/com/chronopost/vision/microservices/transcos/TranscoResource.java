package com.chronopost.vision.microservices.transcos;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.transco.GetTranscoOutput;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Resource Transcos
 * 
 * @author jcbontemps
 *
 */
@Path("transco")
@Api("/")
public class TranscoResource {
	
	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(TranscoResource.class);

	/**
	 * Default constructeur
	 */
	public TranscoResource() {
		super();
	}

	/**
	 * Recuperation de la valeur d'une transco
	 * 
	 * @return la valeur d'une transco
	 */
	@GET
	@Path("{projet}/{famille}/{nom}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Affiche la page de modification des flips Vision")
	public Response getTransco(@PathParam("projet") String projet, @PathParam("famille") String famille, @PathParam("nom") String nom) {
	    GetTranscoOutput transco = null;
		try{
            transco = TranscoServiceImpl.getInstance().getTransco(projet, famille, nom) ;
		}
		catch (Exception e) {
			logger.error("Erreur transco",e);
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.entity(e).build();
		}
		return Response.status(Status.OK).entity(transco).build();
	}
	
	@POST
	@Path("update/{projet}/{famille}/{nom}/{valeur}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	/**
     * Modification de la valeur d'un featureFlip
	 * @param nomFlip nom du Flip à modifier
	 * @param valeurFlip nouvelle valeur à attribuer au Flip
	 * @return
	 */
	@ApiOperation(value = "Service de modification d'un flip Vision. Si le flip n'existe pas, il est créé.")
	public Response updateTransco(@ApiParam(required=true, value="projet", defaultValue="projet") @PathParam("projet") String projet, @ApiParam(required=true, value="famille", defaultValue="famille") @PathParam("famille") String famille, @ApiParam(required=true, value="Nom", defaultValue="flip1") @PathParam("nom") String nom,@ApiParam(required=true, value="Valeur", defaultValue="true") @PathParam("valeur") String valeur) {		
		try{
			return Response.status(Status.OK).entity(TranscoServiceImpl.getInstance().updateTransco(projet, famille, nom, valeur)).build();
		}
		catch (Exception e) {
			logger.error("Erreur featureFlips",e);
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.entity(e).build();
		}		
	}
}
