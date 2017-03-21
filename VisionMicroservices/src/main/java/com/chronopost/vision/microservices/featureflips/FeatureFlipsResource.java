package com.chronopost.vision.microservices.featureflips;

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

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Resource FeatureFlips
 * 
 * @author vdesaintpern
 *
 */
@Path("/")
@Api("/")
public class FeatureFlipsResource {

    private String baseUrl = "";

    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(FeatureFlipsResource.class);

    public FeatureFlipsResource setBaseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    /**
     * Default constructeur
     */
    public FeatureFlipsResource() {
        super();
    }

    /**
     * Recuperation de la liste des feature flips.
     * 
     * @return la liste des feature flips en HTML
     */
    @GET
    @Path("/featureFlips/list")
    @Timed
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "Affiche la page de modification des flips Vision")
    public Response listFeatureFlips() {
        String html = new String();
        try {
            html = FeatureFlipsServiceImpl.getInstance().getFeatureFlipsHtml(this.baseUrl);
        } catch (Exception e) {
            logger.error("Erreur featureFlips", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();

        }

        return Response.status(Status.OK).entity(html).build();
    }

    @GET
    @Path("/featureFlips/update/{nomFlip}/{valeurFlip}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Modification de la valeur d'un featureFlip
     * @param nomFlip nom du Flip à modifier
     * @param valeurFlip nouvelle valeur à attribuer au Flip
     * @return
     */
    @ApiOperation(value = "Service de modification d'un flip Vision. Si le flip n'existe pas, il est créé.")
    public Response updateFeatureFlips(
            @ApiParam(required = true, value = "Nom du flip", defaultValue = "flip1") @PathParam("nomFlip") String nomFlip,
            @ApiParam(required = true, value = "Valeur du flip", defaultValue = "true") @PathParam("valeurFlip") String valeurFlip) {
        try {
            return Response.status(Status.OK)
                    .entity(FeatureFlipsServiceImpl.getInstance().updateFeatureFlip(nomFlip, valeurFlip)).build();
        } catch (Exception e) {
            logger.error("Erreur featureFlips", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();

        }
    }

    @POST
    @Path("/featureFlips/{nomFlip}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Récupère la valeur d'un featureFlip
     * @param nomFlip non du Flip à modifier
     * @return la valeur actuelle de ce flip
     */
    @ApiOperation(value = "Récupération de la valeur d'un flip")
    public Response getFeatureFlips(
            @ApiParam(required = true, value = "Nom du flip", defaultValue = "flip1") @PathParam("nomFlip") String nomFlip) {
        try {
            return Response.status(Status.OK).entity(FeatureFlipsServiceImpl.getInstance().getFeatureFlip(nomFlip))
                    .build();
        } catch (Exception e) {
            logger.error("Erreur getFeatureFlips", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();

        }
    }

    @GET
    @Path("/featureFlips/delete/{nomFlip}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Récupère la valeur d'un featureFlip
     * @param nomFlip non du Flip à modifier
     * @return la valeur actuelle de ce flip
     */
    @ApiOperation(value = "Suppression d'un flip")
    public Response deleteFeatureFlips(
            @ApiParam(required = true, value = "Nom du flip", defaultValue = "flip1") @PathParam("nomFlip") String nomFlip) {
        try {
            return Response.status(Status.OK).entity(FeatureFlipsServiceImpl.getInstance().deleteFeatureFlip(nomFlip))
                    .build();
        } catch (Exception e) {
            logger.error("Erreur deleteFeatureFlips", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();

        }
    }

}
