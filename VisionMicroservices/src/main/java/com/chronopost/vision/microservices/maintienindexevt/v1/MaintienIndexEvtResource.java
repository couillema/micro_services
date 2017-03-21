package com.chronopost.vision.microservices.maintienindexevt.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtOutput;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/MaintienIndexEvt/v1")
@Api("/")
public class MaintienIndexEvtResource {

	private static final Logger logger = LoggerFactory.getLogger(MaintienIndexEvtResource.class);
    private IMaintienIndexEvtService service;

    public MaintienIndexEvtResource() {
        super();
    }

    public MaintienIndexEvtResource setService(final IMaintienIndexEvtService service) {
        this.service = service;
        return this;
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Tient à jour les tables d'index à chaque traitement d'un événement", notes = "", response = MaintienIndexEvtOutput.class)
    public Response maintienIndexEvt(
            @ApiParam(required = true, value = "évenements + Lt + Résultat calcul retard") final MaintienIndexEvtInput inputData) {
        try {
            return Response.ok(this.service.maintienIndexEvt(inputData)).build();
        } catch (final Exception e) {
            logger.error("Erreur maintienIndexEvt :" + e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
        }
    }
}