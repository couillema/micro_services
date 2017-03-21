package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

/**
 * On a 2 tests sur des appels de base Cassandra (MapperTest et SyntheseTourneeDaoTest)
 * dans ce test on crée beaucoup d'entrées dans la base qui servent au test et on a également besoin de les consulter et les effacer
 * Cette test regroupe donc tous les appels utiles et répétitif pour simplifier la lecture du code 
 * @author jcbontemps
 *
 */
public class SyntheseTourneeDaoUtils {

    private final static UserType evtPointUDT;

    /** requete d'ajout d'un evenement sur le point */
    private final static PreparedStatement psAddEvent;
    /** requete de mise à jour du type de destinataire du point */
    private final static PreparedStatement psSetTypeDestinataire;
    /** requete de mise à jour du nom de destinataire du point */
    private final static PreparedStatement psSetNomDestinataire;
    /** requete de mise à jour de la liste des points dans la tournee */
    private final static PreparedStatement psAddPoint;
    /** requete de mise à jour de la liste des colis dans la tournee */
    private final static PreparedStatement psAddColis;
    /** PreparedStatement pour récupérer un point */
    private final static PreparedStatement getOnePoint;
    /** requete de suppression d'un enregistrement dans la liste des points dans la tournee */
    private final static PreparedStatement psDelPoint;
    /** requete de suppression d'un enregistrement dans la liste des tournees */
    private final static PreparedStatement psDelTournee;

    static {
        psAddEvent = getSession().prepare("update "
                + ETableTourneePoint.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse() + " set "
                + ETableTourneePoint.EVENEMENTS.getNomColonne() + " = " + ETableTourneePoint.EVENEMENTS.getNomColonne()
                + " +  ? " + " WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");

        psSetTypeDestinataire = getSession().prepare("update "
                + ETableTourneePoint.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse() + " set "
                + ETableTourneePoint.TYPE_DESTINATAIRE.getNomColonne() + " = ? " + " WHERE "
                + ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");

        psSetNomDestinataire = getSession().prepare("update "
                + ETableTourneePoint.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse() + " set "
                + ETableTourneePoint.NOM_DESTINATAIRE.getNomColonne() + " = ? " + " WHERE "
                + ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");

        psAddPoint = getSession().prepare("update " + ETableTournee.TABLE_NAME
                + " USING TTL " + TTL.POINTTOURNEE.getTimelapse() + " set " + ETableTournee.POINTS.getNomColonne()
                + " = " + ETableTournee.POINTS.getNomColonne() + " +  ? " + " WHERE "
                + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

        psAddColis = getSession().prepare("update " + ETableTournee.TABLE_NAME
                + " USING TTL " + TTL.POINTTOURNEE.getTimelapse() + " set " + ETableTournee.COLIS.getNomColonne()
                + " = " + ETableTournee.COLIS.getNomColonne() + " +  ? " + " WHERE "
                + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		/* PrepareStatement pour les test */
		getOnePoint = getSession().prepare("SELECT * FROM "
				+ ETableTourneePoint.TABLE_NAME + " WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = ?");
        
        psDelPoint = getSession().prepare("delete from "
                + ETableTourneePoint.TABLE_NAME + " WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");

        psDelTournee = getSession().prepare("delete from "
                + ETableTournee.TABLE_NAME + " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

        evtPointUDT = getSession()
                .getCluster()
                .getMetadata()
                .getKeyspace(getSession().getLoggedKeyspace())
                .getUserType("evtpoint");
    }

    /**
     * Efface un point en base
     * @param idPointC11 id du PointTournee à effacer en base
     */
    protected static void delPointTournee(String idPointC11) {
        String idTournee = idC11FromIdPointC11(idPointC11) ;
        getSession().execute(psDelTournee.bind(idTournee));
        getSession().execute(psDelPoint.bind(idPointC11));
    }

    /**
     * Insertion d'un colis dans une tournée. Si la tournée n'existe pas celle-ci est créée
     * @param noLt numéro du colis
     * @param type_destinataire Type de destinataire du colis (E=entreprise, P=particulier)
     * @param nom_destinataire Nom du dstinataire
     * @param dateEvt date de l'événement associée au colis. Cette valeur peut être à null (date inconnue)
     * @param codeEvt code de l'événement associé au colis
     * @param diffETA diffETA associée au colis. Cette valeur peut être à null pour non renseignée
     * @param diffGPS diffGPS  associée au colis. Cette valeur peut être à null pour non renseignée
     * @param outilSaisie : Outil de saisie
     */
    protected static void insertPoint(String noLt, String type_destinataire, String nom_destinataire, Date dateEvt,
			String codeEvt, String diffETA, String diffGPS, String idPointC11, String codeTournee, String dateTournee,
			String heureDebutPoint, String heureFinPoint, String eta, String outilSaisie) {

        Set<String> newPoint = new HashSet<>();
        Set<String> newColis = new HashSet<>();

        /* Encodage de l'evt dans un user type : evtpoint */
        UDTValue evtPoint = evtPointUDT.newValue();
        evtPoint.setTimestamp("date_evt", dateEvt);
        evtPoint.setString("type_evt", codeEvt);
        if (StringUtils.isNoneBlank(codeTournee)) {
            evtPoint.setString("code_tournee", codeTournee);
		}
        if (StringUtils.isNoneBlank(dateTournee)) {
            evtPoint.setString("date_tournee", dateTournee);
		}

        if (StringUtils.isNoneBlank(heureDebutPoint)) {
            evtPoint.setString("heure_debut_point", heureDebutPoint);
		}
        if (StringUtils.isNoneBlank(heureFinPoint)) {
            evtPoint.setString("heure_fin_point", heureFinPoint);
		}
        if (StringUtils.isNoneBlank(eta)) {
            evtPoint.setString("eta", eta);
		}
        if (StringUtils.isNoneBlank(outilSaisie)) {
            evtPoint.setString("outil_saisie", outilSaisie);
		}
        
        if (diffETA != null) evtPoint.setString("diff_eta", diffETA);
        if (diffGPS != null) evtPoint.setString("diff_gps", diffGPS);
        if (nom_destinataire != null) evtPoint.setString("nom_receptionnaire", nom_destinataire);
        if (type_destinataire != null) evtPoint.setString("type_receptionnaire", type_destinataire);
        evtPoint.setString("no_lt", noLt);
        Set<UDTValue> setEvt = new HashSet<>();
        setEvt.add(evtPoint);

        /*
         * On sauve l'evt dans le point (et le point par la meme occasion s'il
         * n'existait pas déjà)
         */
        getSession().execute(psAddEvent.bind(setEvt, idPointC11));

        /*
         * Si le type de client (E/P : entreprise/particulier) est disponible on
         * le mémorise
         */
        if (type_destinataire != null) getSession().execute(psSetTypeDestinataire.bind(type_destinataire, idPointC11));
        /* Si le nom destination est disponible on le mémorise */
        if (nom_destinataire != null) getSession().execute(psSetNomDestinataire.bind(nom_destinataire, idPointC11));

        /*
         * Maintenant on ajoute le point dans la liste des points et le colis
         * dans la liste des colis
         */
        final String idC11 = new IdPointC11(idPointC11).getIdC11();
        newPoint.add(idPointC11);
		newColis.add(noLt);

		getSession().execute(psAddPoint.bind(newPoint, idC11));
		getSession().execute(psAddColis.bind(newColis, idC11));
    }
	
	protected static void insertPoint(String noLt, String type_destinataire, String nom_destinataire, Date dateEvt,
			String codeEvt, String diffETA, String diffGPS, String idPointC11, String outilSaisie, String dateTournee) {
		insertPoint(noLt, type_destinataire, nom_destinataire, dateEvt, codeEvt, diffETA, diffGPS, idPointC11, null, dateTournee, null, null, null, outilSaisie);
	}

    /**
     * Récupère un Point en base 
     * @param idPoint : identifiant point (notamment un idPointC11)
   * @return 
     */
    protected static Row getPointEnBase(String idPoint) {
        ResultSet evtResult = getSession().execute(getOnePoint.bind(idPoint));
        if (evtResult != null )
            return evtResult.one();
        else 
            return null;
    }

    /**
     * Renvoi un idC11 (identifiant tournee) à partir d'un idPointC11
     * (identifiant de point)
     * 
     * @param idPointC11
     * @return
     */
    private static String idC11FromIdPointC11(@NotNull final String idPointC11) {
        return new IdPointC11(idPointC11).getIdC11() ;
    }
    
    private static Session getSession() {
    	return VisionMicroserviceApplication.getCassandraSession();
    }
}
