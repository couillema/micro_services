package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.cassandra.type.ETypeEvtPoint;
import com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeService.ParametresMicroservices;
import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.chronopost.vision.transco.TranscoderService;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;

/**
 * Mapping de certains objets avec les données C*
 * 
 * @author jcbontemps
 */
public class Mapper {
	
	private final static Logger log = LoggerFactory.getLogger(Mapper.class);

    /**
     * Remplit l'objet PointTournee à partir des données de la base c*
     * 
     * @param row
     *            : une ligne de données C* au format renseigné par
     *            ESelectPointTournee
     * @param point
     *            : le PointTournee à remplir avec les données extraites
     * @param specifsColis
     *            : l'ensemble des specifColis des colis de la tournée
     * @param codeTournee : evenement.getSsCodeEvt()
     * @param dateTournee : format YYYYMMDD
     * @return la liste des colis avec leurs événements sur ce PointTournee
     */
    @SuppressWarnings("boxing")
	public static Multimap<String, ColisPoint> makeMapDeColisPoint(final Row row, final PointTournee point,
			final Map<String, SpecifsColis> specifsColis, final String codeTournee, final String dateTournee) {
    	final Multimap<String, ColisPoint> colisEvts = HashMultimap.create();
        final String idPoint = row.getString(ETableTourneePoint.ID_POINT.getNomColonne());
		point.setIdentifiantPoint(idPoint);
        try {
        	point.setNumPointTA(Integer.parseInt(point.getIdentifiantPoint().substring(8, 11)));
        } catch (final NumberFormatException e){
        	point.setNumPointTA(null);
        }
        
        // RG-MSGetSyntTournee-0304
        // variables contenant, pour les evt DPlus, les valeurs les plus anciennes
        String eta = null;
    	Integer diffETA = null;
    	Integer diffGPS = null;
    	Date heureDebutPoint = null;
    	Date heureFinPoint = null;
        String nomDestinataire = null;
        String typeDestinataire = null;

        final Set<UDTValue> uSet = (Set<UDTValue>) row.getSet(ETableTourneePoint.EVENEMENTS.getNomColonne(), UDTValue.class);
        for (final UDTValue evtPoint : uSet) {
        	// RG-MSGetSyntTournee-0303
        	// Considére dans la tournée seuls les points dont codeTournee et dateTournee = ceux de la tournee
        	final String evtCodeTournee = evtPoint.getString(ETypeEvtPoint.CODE_TOURNEE.getNomColonne());
        	final Date dateEvt = evtPoint.getTimestamp(ETypeEvtPoint.DATE_EVENEMENT.getNomColonne());
        	final String dateEvtStr = DateRules.formatDateYYYYMMDD(dateEvt);
        	final String outilSaisie = evtPoint.getString(ETypeEvtPoint.OUTIL_SAISIE.getNomColonne());
        	
        	if (StringUtils.isNotBlank(codeTournee) && StringUtils.isNotBlank(dateTournee)) {
				if (!codeTournee.equals(evtCodeTournee) || !dateTournee.equals(dateEvtStr)) {
					continue;
				}
			} else if (StringUtils.isNotBlank(codeTournee)) {
				if (!codeTournee.equals(evtCodeTournee)) {
					continue;
				}
			} else if (StringUtils.isNotBlank(dateTournee)) {
				if (!dateTournee.equals(dateEvtStr)) {
					continue;
				}
			}
        	
        	final String typeEvt = evtPoint.getString(ETypeEvtPoint.TYPE_EVENEMENT.getNomColonne());
        	final String noLt = evtPoint.getString(ETypeEvtPoint.NO_LT.getNomColonne());
            
            // si code tournee et date tournee alors le code d'en bas, sinon prochain UDTValue evtPoint
            final ColisPoint colis = new ColisPoint();
            colis.setCodeEvenement(typeEvt);
            colis.setNo_lt(noLt);
            colis.setDateEvt(dateEvt);
            colis.setIdentifiantPoint(point.getIdentifiantPoint());
            colis.setInfosSupplementaires(evtPoint.getMap(ETypeEvtPoint.INFO_SUPP.getNomColonne(), String.class, String.class));
            colis.setOutilSaisie(outilSaisie);
            colis.setDiffETA(evtPoint.getString(ETypeEvtPoint.DIFFERENTIEL_ETA.getNomColonne()));
            
            /*
             * Concaténation des specifs colis applicable à la date de l'evt et
             * placement dans les caractéristiques
             */
            if (specifsColis != null && specifsColis.get(colis.getNo_lt()) != null) {
                colis.addAllCaracteristiques(SpecifsColisRules.getCaracteristiqueColisSansPreco(specifsColis.get(colis.getNo_lt()), dateEvt));
                colis.addInfosSupplementaires(specifsColis.get(colis.getNo_lt()).getInfoSupp());
                colis.setCodeService(SpecifsColisRules.getService(specifsColis.get(colis.getNo_lt()), dateEvt));
                colis.setPrecocite(SpecifsColisRules.getPrecocite(specifsColis.get(colis.getNo_lt()), dateEvt));
            }

            if (evtPoint.getSet(ETypeEvtPoint.ANOMALIES.getNomColonne(), String.class) != null) {
            	colis.setAnomalies(evtPoint.getSet(ETypeEvtPoint.ANOMALIES.getNomColonne(), String.class));
                point.getAnomalies().addAll(evtPoint.getSet(ETypeEvtPoint.ANOMALIES.getNomColonne(), String.class));
            }

            /* On prend la date du dernier evt (hors TA) sur le point comme date de passage */
            if (point.getDatePassage() == null && dateEvt != null && "TA".equals(typeEvt) == false)
                point.setDatePassage(dateEvt);
            if (point.getDatePassage() != null && dateEvt != null && point.getDatePassage().before(dateEvt) && "TA".equals(typeEvt) == false)
                point.setDatePassage(dateEvt);

            if ("TA".equals(colis.getCodeEvenement())) {
                point.getColisPrevus().add(colis);
            } else {
                point.getColisPresents().add(colis);
            }

            
        	if (eta == null && evtPoint.getString("eta") != null) {
				eta = evtPoint.getString("eta");
			}

			if (nomDestinataire == null && evtPoint.getString(ETypeEvtPoint.NOM_RECEPTIONNAIRE.getNomColonne()) != null) {
				nomDestinataire = evtPoint.getString(ETypeEvtPoint.NOM_RECEPTIONNAIRE.getNomColonne());
			}
			if (typeDestinataire == null && evtPoint.getString(ETypeEvtPoint.TYPE_RECEPTIONNAIRE.getNomColonne()) != null) {
				typeDestinataire = evtPoint.getString(ETypeEvtPoint.TYPE_RECEPTIONNAIRE.getNomColonne());
			}

            // RG-MSGetSyntTournee-0304
            // les evt sont ordonnés du plus vieux au plus récent
            // on veut les plus vieilles valeurs des evtDPlus
            if (isEvtDPlus(typeEvt)) {
				if (diffETA == null && evtPoint.getString(ETypeEvtPoint.DIFFERENTIEL_ETA.getNomColonne()) != null) {
					diffETA = Integer.parseInt(evtPoint.getString(ETypeEvtPoint.DIFFERENTIEL_ETA.getNomColonne()));
				}
				if (diffGPS == null && evtPoint.getString(ETypeEvtPoint.DIFFERENTIEL_GPS.getNomColonne()) != null) {
					diffGPS = Integer.parseInt(evtPoint.getString(ETypeEvtPoint.DIFFERENTIEL_GPS.getNomColonne()));
				}
				if (heureDebutPoint == null && evtPoint.getString(ETypeEvtPoint.HEURE_DEBUT_POINT.getNomColonne()) != null) {
					if (dateEvt != null) {
						try {
							heureDebutPoint = DateRules.toDatePoint(dateEvt, evtPoint.getString(ETypeEvtPoint.HEURE_DEBUT_POINT.getNomColonne()));
						} catch (final ParseException e) {
							log.error("Error when parse date_evt of Tournee_Point with id : " + idPoint, e);
						}
					}
				}
				if (heureFinPoint == null && evtPoint.getString(ETypeEvtPoint.HEURE_FIN_POINT.getNomColonne()) != null) {
					if (dateEvt != null) {
						try {
							heureFinPoint = DateRules.toDatePoint(dateEvt, evtPoint.getString(ETypeEvtPoint.HEURE_FIN_POINT.getNomColonne()));
						} catch (final ParseException e) {
							log.error("Error when parse date_evt of Tournee_Point with id : " + idPoint, e);
						}
					}
				}
			}

            // Ajoute ColisPoint dans colisEvts pour alimenter Tournee.colisEvenement
            colisEvts.put(colis.getNo_lt(), colis);
        }
        
        // RG-MSGetSyntTournee-0304
        // mets à jour le point avec les infos les plus anciennes issues des Evt DPlus
        if (diffETA != null)
        	point.setDiffETA(diffETA);
        if (eta != null)
        	point.setEta(eta);
        if (diffGPS != null)
        	point.setDiffGPS(diffGPS);
        if (nomDestinataire != null)
        	point.setNomDestinataire(nomDestinataire);
        if (typeDestinataire != null)
        	point.setTypeDestinataire(typeDestinataire);
        if (heureDebutPoint != null) {
        	point.setDateDebutPoint(heureDebutPoint);
        } else {
        	point.setDateDebutPoint(point.getDatePassage());
        }
        if (heureFinPoint != null) {
        	point.setDateFinPoint(heureFinPoint);
        } else {
        	point.setDateFinPoint(point.getDatePassage());
        }

        return colisEvts;
    }

    /**
     * 
     * @param row
     *            : row d'un select ayant comme clause select
     *            ESelectColisSpecificiation.getSelectClause()
     * @param specifColis
     * @return une objet SpecifColis remplit à partir de la row fournit
     */
	public static SpecifsColis makeSpecifColis(@NotNull final Row row) {
		final SpecifsColis specifColis = new SpecifsColis();
		specifColis.setNoLt(row.getString(ETableColisSpecifications.NO_LT.getNomColonne()));
		specifColis.setSpecifsEvt(
				row.getMap(ETableColisSpecifications.SPECIFS_EVT.getNomColonne(), Date.class, String.class));
		specifColis.setSpecifsService(
				row.getMap(ETableColisSpecifications.SPECIFS_SERVICE.getNomColonne(), new TypeToken<Date>() {
					private static final long serialVersionUID = -2211301558495036908L;
				}, new TypeToken<Set<String>>() {
					private static final long serialVersionUID = -2454091878589962535L;
				}));
		specifColis
				.addAllEtapes(row.getMap(ETableColisSpecifications.ETAPES.getNomColonne(), Date.class, String.class));
		specifColis.setConsignesTraitees(
				row.getMap(ETableColisSpecifications.CONSIGNES_TRAITEES.getNomColonne(), Date.class, String.class));
		specifColis.addAllInfoSupp(
				row.getMap(ETableColisSpecifications.INFO_SUPP.getNomColonne(), String.class, String.class));
		specifColis.addAllServices(
				row.getMap(ETableColisSpecifications.SERVICE.getNomColonne(), Date.class, String.class));
		return specifColis;
	}

    public static boolean isEvtDPlus(final String evt) {
        final String filterValues = TranscoderService.INSTANCE.getTranscoder("DiffusionVision")
                .transcode("parametre_microservices", ParametresMicroservices.EVT_D_PLUS.toString());
        return filterValues.contains(String.format("|%s|", evt));
    }
}
