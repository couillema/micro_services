package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.getsynthesetournees.v1.InfoTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Resource SyntheseTourneeResource utilisé pour récupérer les indicateurs de différentes tournées
 * 
 * @author jcbontemps
 *
 */
@Path("/")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SyntheseTourneeResource {

    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(SyntheseTourneeResource.class);

    private SyntheseTourneeService service;

    /**
     * Constructeur
     */
    public SyntheseTourneeResource() {
        super();
    }

    /**
     * 
     * @param service
     *            une implémentation de SyntheseTourneeService
     * @return Cet objet ressource (pratique pour injecter à la création)
     */
    public SyntheseTourneeResource setService(SyntheseTourneeService service) {
        this.service = service;
        return this;
    }

    /**
     * Retourne un map objets de SyntheseTourneeQuantite avec comme clé, le numéro de tournée fournit en entrée. Map<idTournee, SyntheseTourneeQuantite>. Chaque objet SynthèseTourneeQuantite contient les décompte résumant la tournée.
     * @param idTournee la liste des identifiants de tournée
     * @return une réponse HTTP avec un Map<idTournee, SyntheseTourneeQuantite> dans le corps si 200 
     */
    @POST
    @Path("/getSyntheseTournee/Quantite")
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retourne un map objets de SyntheseTourneeQuantite avec comme clé, le numéro de tournée fournit en entrée. Map<idTournee, SyntheseTourneeQuantite>. Chaque objet SynthèseTourneeQuantite contient les décompte résumant la tournée.")
    public Response getSyntheseTourneeQuantite(@ApiParam(required = true, value = "") List<String> idTournee) {

        try {
        	service.declareAppelMS();        	
            Map<String, SyntheseTourneeQuantite> synthese = service.getSyntheseTourneeQuantite(idTournee);
            return Response.status(HttpStatus.SC_OK).entity(synthese).build();
        } catch (MSTechnicalException e) {
            logger.error(
                    "Erreur Technique lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeQuantite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
        } catch (Exception e) {
            logger.error(
                    "Erreur inconnue lors de l'appel à com.chronopost.vision.microservicestournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeQuantite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        }

    }

    /**
     * Retourne un Liste d’objets PointTournee List<PointTournee>
     * @param idTournee l'identifiant de la tournée
     * @return une réponse HTTP avec un List<PointTournee> dans le corps si 200
     */
    @POST
    @Path("/getSyntheseTournee/Activite")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retourne une liste d’objets PointTournee")
    public Response getSyntheseTourneeActivite(@ApiParam(required = true) String idTournee) {

        try {
        	service.declareAppelMS();        	
            List<PointTournee> points = service.getSyntheseTourneeActivite(idTournee);
            return Response.status(HttpStatus.SC_OK).entity(points).build();
        } catch (MSTechnicalException e) {
            logger.error(
                    "Erreur Technique lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActivite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
        } catch (Exception e) {
            logger.error(
                    "Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActivite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        }
    }

    /**
     * Retourne un InfoTournee
     * @param idTournee l'identifiant de la tournée
     * @return une réponse HTTP avec une InfoTournee dans le corps si 200
     */
    @POST
    @Path("/getSyntheseTournee/ActiviteEtQuantite")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retourne une liste d’objets InfoTournee")
    public Response getSyntheseTourneeActiviteEtQuantite(@ApiParam(required = true) String idTournee) {

        try {
        	service.declareAppelMS();
            InfoTournee infoTournee = service.getSyntheseTourneeActiviteEtQuantite(idTournee);
            return Response.status(HttpStatus.SC_OK).entity(infoTournee).build();
        } catch (MSTechnicalException |InterruptedException |ExecutionException  e) {
            logger.error(
                    "Erreur Technique lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActiviteEtQuantite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
        } catch (Exception e) {
            logger.error(
                    "Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActiviteEtQuantite",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        }
    }

    /**
     * Retourne une map <IdTournee, InfoTournee>
     * @param tourneeIds liste d'idTournee à traiter
     * @return une réponse HTTP avec une une map <IdTournee, InfoTournee> dans le corps si 200
     */
    @POST
    @Path("/getSyntheseTournee/ActivitesEtQuantites")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retourne une liste d’objets InfoTournee")
    public Response getSyntheseTourneeActivitesEtQuantites(@ApiParam(required = true) final List<String> tourneeIds) {
        try {
        	service.declareAppelMS();        	
        	final Map<String, InfoTournee> tourneesInfos = service.getSyntheseTourneeActivitesEtQuantites(tourneeIds);
            return Response.status(HttpStatus.SC_OK).entity(tourneesInfos).build();
        } catch (MSTechnicalException e) {
            logger.error(
                    "Erreur Technique lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActivitesEtQuantites",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
        } catch (Exception e) {
            logger.error(
                    "Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource.getSyntheseTourneeActivitesEtQuantites",
                    e);
            service.declareFailMS();
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
        }
    }
}
