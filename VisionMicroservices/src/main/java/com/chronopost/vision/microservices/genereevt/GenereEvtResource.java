package com.chronopost.vision.microservices.genereevt;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.genereevt.v1.GenereEvtV1Output;
import com.codahale.metrics.annotation.Timed;

/**
 * Classe de Ressource REST Offre des méthodes permettant de créer des evts dans
 * le SI
 * 
 * @author jcbontemps
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GenereEvtResource {

    private static final Logger logger = LoggerFactory.getLogger(GenereEvtResource.class);

    private IGenereEvtService service;

    public GenereEvtResource setService(IGenereEvtService service) {
        this.service = service;
        return this;
    }

    /**
     * Génère les evts dans le SI et renvoi le résultat des traitements
     * 
     * @param evts
     *            évènements à injecter dans le SI Vision
     * @param injectionVision
     *            doit-on également injecter les evts dans la base vision avec
     *            le MS insertEvts
     * @return true si l'opération s'est déroulée correctement, un code erreur
     *         HTTP sinon (404 / 500 / 503)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/genereEvt/{injectionVision}")
    public Response genereEvts(List<Evt> evts, @PathParam("injectionVision") Boolean injectionVision) {
        try {
            if (!service.genereEvt(evts, injectionVision).containsValue(false)) {
                return Response.status(HttpStatus.SC_OK).entity(new GenereEvtV1Output().setStatus(true)).build();
            }
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new MSTechnicalException("Insertion impossible...")).build();
        } catch (MSTechnicalException e) {
            logger.error(
                    "Erreur Technique lors de l'appel à com.chronopost.vision.microservices.genereevt.GenereEvtResource.genereEvts",
                    e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
        } catch (FunctionalException e) {
            logger.error(
                    "Erreur fonctionnelle lors de l'appel à com.chronopost.vision.microservices.genereevt.GenereEvtResource.genereEvts",
                    e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        } catch (Exception e) {
            logger.error(
                    "Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.genereevt.GenereEvtResource.genereEvts",
                    e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        }
    }
}
