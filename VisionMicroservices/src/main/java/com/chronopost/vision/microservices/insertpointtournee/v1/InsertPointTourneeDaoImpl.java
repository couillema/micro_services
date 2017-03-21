package com.chronopost.vision.microservices.insertpointtournee.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.CassandraClauseBuilder;
import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableEvt;
import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.cassandra.type.ETypeEvtPoint;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.Tournee;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.getsynthesetournees.v1.EAnomalie;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Futures;

/** @author unknown : JJC getSession + log caps.  + import min.**/
public enum InsertPointTourneeDaoImpl implements IInsertPointTourneeDao {
    INSTANCE;

    private final int prioriteTA = 1000;

	/** Log */
	private final Logger log = LoggerFactory.getLogger(InsertPointTourneeDaoImpl.class);

	/** requete d'ajout d'un evenement sur le point */
	private final PreparedStatement psAddEvent;
	/** requete de mise à jour de la liste des points dans la tournee */
	private final PreparedStatement psAddPoint;
	/** requete de mise à jour de la liste des colis dans la tournee */
	private final PreparedStatement psAddColis;
	/** requete de mise à jour de la date de début de tournee */
	private final PreparedStatement psUpdDebutTournee;
	/** requete de mise à jour de la date de fin de tournee */
	private final PreparedStatement psUpdFinTournee;
	/** requete de mise à jour de psm */
	private final PreparedStatement psUpdPsm;
	/** requete de maintient de l'index tournee par jour */
	private final PreparedStatement psInsertIdxTourneeJour;
    /**  **/
    private final PreparedStatement psTrouverDerniersEvts;
    /**  **/
    private final PreparedStatement psTrouverDernieresTournees;
    /** Mise à jour du compteur de microservice **/
    private final PreparedStatement psUpdateCptTrtTrtFailMS;
    private final PreparedStatement psUpdateCptFailMS;
    private final PreparedStatement psUpdateCptHitMS;
    private final PreparedStatement psInsertErreur;

	/** Type événement de point (ce type est issue de la base et utilisé dans la colonne evts de la table tournee_point) */
	private final UserType evtPointUDT;
	
    /** referentiel des codes services fourni par injection */
	private CacheManager<CodeService> cacheCodeService = null;
	private CacheManager<Agence> cacheManagerAgence = null;
	private CacheManager<Parametre> cacheManagerParametre = null;

	private InsertPointTourneeDaoImpl() {
		psAddEvent = getSession().prepare("update " + ETableTourneePoint.TABLE_NAME + " USING TTL "
				+ TTL.POINTTOURNEE.getTimelapse() + " set " + ETableTourneePoint.EVENEMENTS.getNomColonne() + " = "
				+ ETableTourneePoint.EVENEMENTS.getNomColonne() + " +  ? " + " WHERE "
				+ ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");

		psAddPoint = getSession()
				.prepare("update " + ETableTournee.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse()
						+ " set " + ETableTournee.POINTS.getNomColonne() + " = " + ETableTournee.POINTS.getNomColonne()
						+ " +  ? " + " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		psAddColis = getSession()
				.prepare("update " + ETableTournee.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse()
						+ " set " + ETableTournee.COLIS.getNomColonne() + " = " + ETableTournee.COLIS.getNomColonne()
						+ " +  ? " + " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		psUpdDebutTournee = getSession().prepare("update " + ETableTournee.TABLE_NAME + " USING TTL "
				+ TTL.POINTTOURNEE.getTimelapse() + " set " + ETableTournee.DEBUT_TOURNEE.getNomColonne() + " =  ? "
				+ " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		psUpdFinTournee = getSession().prepare("update " + ETableTournee.TABLE_NAME + " USING TTL "
				+ TTL.POINTTOURNEE.getTimelapse() + " set " + ETableTournee.FIN_TOURNEE.getNomColonne() + " =  ? "
				+ " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		psUpdPsm = getSession()
				.prepare("update " + ETableTournee.TABLE_NAME + " USING TTL " + TTL.POINTTOURNEE.getTimelapse()
						+ " set " + ETableTournee.PSM.getNomColonne() + " = " + ETableTournee.PSM.getNomColonne()
						+ " +  ? " + " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

		psInsertIdxTourneeJour = getSession().prepare("insert into " + ETableIdxTourneeJour.TABLE_NAME + "( "
				+ ETableIdxTourneeJour.AGENCE.getNomColonne() + ", " + ETableIdxTourneeJour.JOUR.getNomColonne() + ", "
				+ ETableIdxTourneeJour.CODE_TOURNEE.getNomColonne() + ", "
				+ ETableIdxTourneeJour.DATE_TOURNEE.getNomColonne() + ", "
				+ ETableIdxTourneeJour.ID_TOURNEE.getNomColonne() + ") values (?, ?, ?, ?, ?) " + "USING TTL "
				+ TTL.POINTTOURNEE.getTimelapse());

		/*
		 * On récupère la structure du type utilisateur evtpoint qui est définit
		 * en base
		 */
		evtPointUDT = getSession().getCluster().getMetadata().getKeyspace(getSession().getLoggedKeyspace())
				.getUserType("evtpoint");

		psTrouverDerniersEvts = getSession().prepare(buildSelect(ETableEvt.TABLE_NAME,
				Arrays.asList(ETableEvt.NO_LT, ETableEvt.DATE_EVT, ETableEvt.DATE_CREATION_EVT, ETableEvt.CODE_EVT,
						ETableEvt.INFOSCOMP, ETableEvt.SS_CODE_EVT),
				CassandraClauseBuilder.buildEqClause(ETableEvt.NO_LT),
				CassandraClauseBuilder.buildEqClause(ETableEvt.PRIORITE_EVT),
				CassandraClauseBuilder.buildIsGreaterOrEqualsClause(ETableEvt.DATE_EVT),
				CassandraClauseBuilder.buildIsLowerClause(ETableEvt.DATE_EVT)).getQuery());

		psTrouverDernieresTournees = getSession().prepare(buildSelect(ETableIdxTourneeJour.TABLE_NAME,
				ETableIdxTourneeJour.AGENCE, ETableIdxTourneeJour.JOUR, ETableIdxTourneeJour.CODE_TOURNEE).getQuery());

		psInsertErreur = getSession().prepare("insert into microservice_erreur(jour,microservice,methode,objet,date,exception ) values (?,?,?,?,?,?) USING TTL "+TTL.ERREUR.getTimelapse()).setConsistencyLevel(LOCAL_ONE);
		psUpdateCptTrtTrtFailMS = ETableMicroServiceCounters.getUpdateTRT();
		psUpdateCptHitMS = ETableMicroServiceCounters.getIncrementHit();
		psUpdateCptFailMS = ETableMicroServiceCounters.getIncrementFail();
	}

    /** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chronopost.vision.microservices.traitementRetard.TraitementRetardDao
	 * #insertDLE(java.util.List)
	 */
	@Override
	public boolean addEvtDansPoint(@NotNull final List<Evt> evenements) {
		final List<ResultSetFuture> futuresInsertIdx = new ArrayList<>();
		final Set<String> idC11IndexesRecemment = new HashSet<>();
		final List<ResultSetFuture> futuresAddEvts = new ArrayList<>();
		Exception lastError = null;
		
		final Boolean counterActif = FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false);
		
		for (final Evt evenement : evenements) {
			String idPointC11 = null;
			String idC11 = null;
			final Map<String,String> infoSupp = new HashMap<>();
			final Set<String> newPoint = new HashSet<>();
			final Set<String> newColis = new HashSet<>();
			final Set<String> newPsm = new HashSet<>();
			final Map<String, String> infoComp = evenement.getInfoscomp();
			try {
				/* Si un idPointC11 conforme est disponible dans l'evt, on le prend comme idPoint */
				if (null != infoComp && (idPointC11 = infoComp.get(EInfoComp.ID_POINT_C11.getCode())) != null
						&& idPointC11.length() == 25) {
					/* Récupération des infoscomp disponibles */
					final String diffeta = infoComp.get(EInfoComp.DIFF_ETA.getCode());
					final String diffgps = infoComp.get(EInfoComp.DIFF_GPS.getCode());
					final String eta = infoComp.get(EInfoComp.ETA.getCode());
					final String debutCreneau = infoComp.get(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode());
					final String finCreneau = infoComp.get(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode());
					
					// Préparation du batch
					final BatchStatement batchAddEvts = new BatchStatement();

					/* Encodage de l'evt dans un user type : evtpoint */
					final UDTValue evtPoint = evtPointUDT.newValue();
					evtPoint.setTimestamp("date_evt", evenement.getDateEvt());
					evtPoint.setString("type_evt", EvtRules.getCodeEvenementPointDeVueTournee(evenement));
					evtPoint.setString("no_lt", evenement.getNoLt());
					evtPoint.setString("outil_saisie", evenement.getCreateurEvt());
					evtPoint.setString("date_tournee", evenement.getDateTournee());
					evtPoint.setString("code_tournee", evenement.getSsCodeEvt());
					evtPoint.setString("heure_debut_point", evenement.getHeureDebutPoint());
					evtPoint.setString("heure_fin_point", evenement.getHeureFinPoint());
					
					/* Si le nom destinataire et/ou code regate est disponible on le mémorise */
					/* On concatene nomDesti et codeRegate */
					String nomDesti = infoComp.get(EInfoComp.NOM_DESTINATAIRE.getCode());
					final String codeRegate = infoComp.get(EInfoComp.IDENTIFIANT_POINT_RELAIS.getCode());
					if (codeRegate != null) {
						if (nomDesti != null)
							nomDesti = nomDesti + " " + codeRegate;
						else
							nomDesti = codeRegate;
					}
					if (nomDesti != null) {
						evtPoint.setString("nom_receptionnaire", nomDesti);
					}

					evtPoint.setString("type_receptionnaire", infoComp.get(EInfoComp.TYPE_DESTINATAIRE.getCode()));

					/* Si diff_eta dispo : mémorise */
					if (diffeta != null && !diffeta.isEmpty())
						try {
							/* Simple test pour ne pas véhiculer une valeur qui n'est pas un entier */
							Integer.parseInt(diffeta);
							evtPoint.setString("diff_eta", diffeta);
						} catch (NumberFormatException e){
							log.warn("La valeur de l'infocomp diff_eta n'est pas un entier (no_lt/evt_code/diff_eta)=("+evenement.getNoLt()+"/"+evenement.getCodeEvt()+"/"+diffeta);
						}
					/* Si diff_gps dispo : mémorise */
					if (diffgps != null)
						try {
							/* Simple test pour ne pas véhiculer une valeur qui n'est pas un entier */
							Integer.parseInt(diffgps);
							evtPoint.setString("diff_gps", diffgps);
						} catch (NumberFormatException e) {
							log.warn("La valeur de l'infocomp diff_gps n'est pas un entier (no_lt/evt_code/diff_eta)=("+evenement.getNoLt()+"/"+evenement.getCodeEvt()+"/"+diffgps);
						}
					/* Si ETA dispo : mémorise */
					if (eta != null)
						try {
							/* Simple test pour ne pas véhiculer une valeur qui n'est pas correcte */
							final String[] etaSplit = eta.split(":");
							Integer.parseInt(etaSplit[0]);
							Integer.parseInt(etaSplit[1]);
							if (etaSplit.length == 2)
								evtPoint.setString("eta", eta);
						} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
							log.warn("La valeur de l'infocomp eta n'est pas conforme (HH:MM)=(" + evenement.getNoLt()
									+ "/" + evenement.getCodeEvt() + "/" + eta);
						}

					/* Si l'événement n'est pas autorisé pour le codeservice du colis ==> erreur */
					if (cacheCodeService.getValue(evenement.getCodeService()) != null
							&& cacheCodeService.getValue(evenement.getCodeService()).getEvtNonPermis() != null
							&& cacheCodeService.getValue(evenement.getCodeService()).getEvtNonPermis()
									.contains(evenement.getCodeEvt())) {
						final Set<String> anomalies = new HashSet<>();
						anomalies.add(EAnomalie.EVT_NON_PERMIS.getCode());
						evtPoint.setSet(ETypeEvtPoint.ANOMALIES.getNomColonne(), anomalies);
					}
					
					/* Traitement des infos_supplémentaires */
					if (infoComp.get(EInfoComp.AVIS_PASSAGE.getCode()) != null)
						infoSupp.put(EInfoSupplementaire.AVIS_PASSAGE.getCode(), infoComp.get(EInfoComp.AVIS_PASSAGE.getCode()));
					if (infoComp.get(EInfoComp.RESULTAT_APPEL.getCode()) != null)
						infoSupp.put(EInfoSupplementaire.RESULTAT_APPEL.getCode(), infoComp.get(EInfoComp.RESULTAT_APPEL.getCode()));
					if (infoComp.get(EInfoComp.NUMERO_APPEL.getCode()) != null)
						infoSupp.put(EInfoSupplementaire.NUMERO_APPEL.getCode(), infoComp.get(EInfoComp.NUMERO_APPEL.getCode()));
					if (infoComp.get(EInfoComp.DUREE_APPEL.getCode()) != null)
						infoSupp.put(EInfoSupplementaire.DUREE_APPEL.getCode(), infoComp.get(EInfoComp.DUREE_APPEL.getCode()));
					// RG-MSInsPoint-016
					if (debutCreneau != null && debutCreneau.length() > 16)
						infoSupp.put(EInfoSupplementaire.CRENEAU_DEBUT.getCode(), debutCreneau.substring(11, 16));
					// RG-MSInsPoint-017
					if (finCreneau != null && finCreneau.length() > 16)
						infoSupp.put(EInfoSupplementaire.CRENEAU_FIN.getCode(), finCreneau.substring(11, 16));
					
					if (!infoSupp.isEmpty())
						evtPoint.setMap("info_supp", infoSupp);
					
					final Set<UDTValue> setEvt = new HashSet<>();
					setEvt.add(evtPoint);

					/* On sauve l'evt dans le point (et le point par la meme occasion s'il n'existait pas déjà) */
					batchAddEvts.add(psAddEvent.bind(setEvt, idPointC11));
					
					/* Maintenant on ajoute le point dans la liste des points, le colis dans la liste des colis 
					 * et le psm dans la liste des psm */
					idC11 = idC11FromIdPointC11(idPointC11);
					newPoint.add(idPointC11);
					newColis.add(evenement.getNoLt());
					if (evenement.getCreateurEvt() != null)
						newPsm.add(evenement.getCreateurEvt());

					batchAddEvts.add(psAddPoint.bind(newPoint, idC11));
					batchAddEvts.add(psAddColis.bind(newColis, idC11));
					if (evenement.getCreateurEvt() != null)
						batchAddEvts.add(psUpdPsm.bind(newPsm, idC11));
                    
					/* Maintient de l'index tournee par jour (si on ne vient pas de le faire récemment) */
					String codetournee = evenement.getSsCodeEvt();
					if (codetournee == null) {
						if (idC11.length() == 19)
							codetournee = idC11.substring(0, 5);
						else if (idC11.length() == 22)
							codetournee = idC11.substring(3, 8);
					}

					if (idC11IndexesRecemment.contains(codetournee + idC11) == false) {
						if (idC11.length() == 19)
							futuresInsertIdx
									.add(getSession().executeAsync(psInsertIdxTourneeJour.bind(evenement.getLieuEvt(),
											idC11.substring(9, 13) + idC11.substring(7, 9) + idC11.substring(5, 7),
											codetournee, DateRules.toDateIdC11(idC11.substring(5, 19)), idC11)));
						else
							futuresInsertIdx
									.add(getSession().executeAsync(psInsertIdxTourneeJour.bind(evenement.getLieuEvt(),
											idC11.substring(12, 16) + idC11.substring(10, 12) + idC11.substring(8, 10),
											codetournee, DateRules.toDateIdC11(idC11.substring(8, 22)), idC11)));
						idC11IndexesRecemment.add(codetournee + idC11);
					}
					if (batchAddEvts.size() > 0) {
						futuresAddEvts.add(getSession().executeAsync(batchAddEvts));
					}
				}
				else {
					log.info(
							"InsertPointTournee - insertEvt : Impossible de placer cet evenement dans une tournée ou un point tournée:"
									+ evenement.getNoLt() + " " + evenement.getCodeEvt());
				}
			} catch (final NoHostAvailableException | QueryExecutionException | QueryValidationException
					| UnsupportedFeatureException | ParseException e) {
				final StringBuilder errorBuilder = new StringBuilder();
				errorBuilder.append("Mise à jour de  la table <" + ETableIdxTourneeJour.TABLE_NAME + "> impossible");
				errorBuilder.append(" # Evenement : " + evenement);
				errorBuilder.append(" # idC11 : " + idC11);
				errorBuilder.append(" # idPointC11 : " + idPointC11);
				errorBuilder.append(" # Lieu : " + evenement.getLieuEvt());
				if (idC11 != null)
					errorBuilder.append(" # Date : " + idC11.substring(5, 19));
				errorBuilder.append(" # PSM : " + Joiner.on(",").join(newPsm));
				log.error(errorBuilder.toString(), e);
				
				if (counterActif) {
					lastError = e;
					declareErreur(evenement, "addEvtDansPoint", e);
				} else {
					throw new MSTechnicalException(
							"Mise à jour de  la table <" + ETableIdxTourneeJour.TABLE_NAME + "> impossible.", e);
				}
			} catch(final Exception e) { // TODO : vraiment utile de catch Exception en plus de toutes celle déjà intercepté au dessus ?
				final StringBuilder errorBuilder = new StringBuilder();
				errorBuilder.append("Mise à jour de  la table <" + ETableIdxTourneeJour.TABLE_NAME + "> impossible");
				errorBuilder.append(" # Evenement : " + evenement);
				errorBuilder.append(" # idC11 : " + idC11);
				errorBuilder.append(" # idPointC11 : " + idPointC11);
				errorBuilder.append(" # Lieu : " + evenement.getLieuEvt());
				if (idC11 != null)
					errorBuilder.append(" # Date : " + idC11.substring(5, 19));
				errorBuilder.append(" # PSM : " + Joiner.on(",").join(newPsm));
				log.error(errorBuilder.toString(), e);

				if (counterActif) {
					lastError = e;
					declareErreur(evenement, "addEvtDansPoint", e);
				} else {
					throw new MSTechnicalException(e);
				}
			}
		}

		/* On traite les retours selon counterMS */
		if (counterActif) {
			for (final ResultSetFuture future : futuresInsertIdx) {
				try {
					future.getUninterruptibly();
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e) {
					log.error("Insertion index tournee impossible ", e);
				}
			}
			int nbEvtInseres = 0;
			int nbEvtFail = 0;
			for (final ResultSetFuture future : futuresAddEvts) {
				try {
					future.getUninterruptibly();
					nbEvtInseres++;
				} catch (NoHostAvailableException | QueryExecutionException | QueryValidationException e) {
					log.error("Insertion point tournee evt impossible ", e);
					nbEvtFail++;
					lastError = e;
					declareErreur(null, "addEvtDansPoint", e);
				}
			}
			/*
			 * On mémorise (si possible le travail effectué, et celui que l'on a
			 * pas réussit
			 */
			updateCptTrtTrtFailMS(nbEvtInseres, nbEvtFail);

			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null)
				throw new MSTechnicalException(lastError);
		}
		else {
			for (final ResultSetFuture future : futuresInsertIdx) {
				future.getUninterruptibly();
			}
			for (final ResultSetFuture future : futuresAddEvts) {
				future.getUninterruptibly();
			}
		}
		
		return true;
	}

	/** @param idPointC11 : Un identifiant C11 sur 25 caractères (vérifié par l'appelant SVP).
	 *  @return un idC11 (identifiant tournee) à partir d'un idPointC11 (identifiant de point) */
	@NotNull
	private String idC11FromIdPointC11(@NotNull final String idPointC11) {
		/*  Si FF is ON && ParamDateActivation exists && ParamDateActivation < DatePoint  ==> idC11+ sinon idC11*/
		if (FeatureFlips.INSTANCE.getBoolean("idC11Plus", false)) {
				// RG-MSInsPoint-022
			if (cacheManagerParametre.getValue("DatePassageIdC11Plus") != null) {
				/* On prend la date du point et on la reformatte en YYYYMMDD */
				// RG-MSInsPoint-022
				final String datePoint = idPointC11.substring(15, 19)+idPointC11.substring(13, 15)+idPointC11.substring(11, 13);
				if (cacheManagerParametre.getValue("DatePassageIdC11Plus").getValue().compareTo(datePoint) <= 0)
				{
					// RG-MSInsPoint-020
					return idPointC11.substring(0, 8) + idPointC11.substring(11);
				}
			}
		}

		// RG-MSInsPoint-021
		return idPointC11.substring(3, 8) + idPointC11.substring(11);
	}

	@Override
	public boolean miseAJourTournee(final List<Evt> evenements) {
		Map<String, String> infoComp;
		final List<ResultSetFuture> futures = new ArrayList<>();
		final Set<String> newPsm = new HashSet<>();
		Exception lastError = null;
		int nbTrtFail=0;
		
		final Boolean counterActif = FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false);
		
		for (final Evt evenement : evenements) {
			String idC11 = null;
			infoComp = evenement.getInfoscomp();

            /* Si le code tournée est défini */
			if (infoComp != null && (idC11 = infoComp.get(EInfoComp.ID_C11.getCode())) != null) {
                try {
                	final String codeAgence = evenement.getLieuEvt();
                	// Si idC11Plus activé, RG-MSInsPoint-019
					if (idC11.length() == 19 && FeatureFlips.INSTANCE.getBoolean("idC11Plus", false)) {
						// RG-MSInsPoint-019
						// RG-MSInsPoint-022 Si la parametre definissant la date de passage a l'idC11+ existe
						if (cacheManagerParametre.getValue("DatePassageIdC11Plus") != null) {
							/* On prend la date du l'idC11 et on la reformatte en YYYYMMDD */
							// RG-MSInsPoint-022 et que la date de la tournee est postérieure a la date de passage a l'idC11+
							final String dateTourneeS = idC11.substring(9, 13) + idC11.substring(7, 9)
									+ idC11.substring(5, 7);
							if (cacheManagerParametre.getValue("DatePassageIdC11Plus").getValue()
									.compareTo(dateTourneeS) <= 0) {
								final Agence agence = cacheManagerAgence.getValue(codeAgence);
								idC11 = agence.getTrigramme() + idC11;
							}
						}
					}
					
					/* Récupération du code tournée (depuis l'evt sinon depuis l'idC11) */
					String codetournee = evenement.getSsCodeEvt();
					if (codetournee == null) {
						if (idC11.length() == 19)
							codetournee = idC11.substring(0, 5);
						else if (idC11.length() == 22)
							codetournee = idC11.substring(3, 8);
					}

					// String codetournee = idC11.substring(0,5);
					String jourTournee;
					Date dateTournee;
					if (idC11.length() == 19) {
						jourTournee = idC11.substring(9, 13) + idC11.subSequence(7, 9) + idC11.substring(5, 7);
						dateTournee = DateRules.toDateIdC11(idC11.substring(5, 19));
					} else {
						jourTournee = idC11.substring(12, 16) + idC11.subSequence(10, 12) + idC11.substring(8, 10);
						dateTournee = DateRules.toDateIdC11(idC11.substring(8, 22));
					}
			        
                    /* Maintient de l'index tournee par jour */
                    futures.add(getSession().executeAsync(psInsertIdxTourneeJour.bind(codeAgence, jourTournee, codetournee, dateTournee, idC11)));

					/* ajout des date debut / fin de tournee */
					if (EvtRules.estUnEvtFictifDebutDeTournee(evenement).booleanValue() == true)
						futures.add(getSession().executeAsync(psUpdDebutTournee.bind(evenement.getDateEvt(), idC11)));
					else if (EvtRules.estUnEvtFictifFinDeTournee(evenement).booleanValue() == true)
						futures.add(getSession().executeAsync(psUpdFinTournee.bind(evenement.getDateEvt(), idC11)));
					/* ajout du PSM */
					newPsm.clear();
					if (evenement.getCreateurEvt() != null)
						newPsm.add(evenement.getCreateurEvt());
					futures.add(getSession().executeAsync(psUpdPsm.bind(newPsm, idC11)));

				} catch (final NoHostAvailableException | QueryExecutionException | QueryValidationException
						| UnsupportedFeatureException | ParseException e) {
					if (counterActif) {
						lastError = new MSTechnicalException("Mise à jour des tables <" + ETableTournee.TABLE_NAME
								+ "> et <" + ETableIdxTourneeJour.TABLE_NAME + "> impossible", e);
						nbTrtFail++;
						declareErreur(evenement, "miseAJourTournee", e);
					} else {
						throw new MSTechnicalException("Mise à jour des tables <" + ETableTournee.TABLE_NAME + "> et <"
								+ ETableIdxTourneeJour.TABLE_NAME + "> impossible", e);
					}
				} catch (final Exception e) {
					log.error(String.format(
							"Mise à jour des tables %s et %s impossible # idC11 = %s # Lieu = %s # Date = %s # PSM = %s",
							ETableTournee.TABLE_NAME, ETableIdxTourneeJour.TABLE_NAME, idC11, evenement.getLieuEvt(),
							idC11.substring(5, 19), evenement.getCreateurEvt()));
					if (counterActif) {
						lastError = new MSTechnicalException(e);
						nbTrtFail++;
						declareErreur(evenement, "miseAJourTournee", e);
					} else {
						throw new MSTechnicalException(e);
					}
				}
			}
		}

		if (counterActif) {
			for (final ResultSetFuture future : futures) {
				try {
					future.getUninterruptibly();
				} catch (Exception e) {
					nbTrtFail++;
					lastError = new MSTechnicalException(e);
					declareErreur(null, "miseAJourTournee", e);
				}
			}
			/* Si on a rencontré une exception, on la remonte */
			if (lastError != null) {
				updateCptTrtTrtFailMS(0, nbTrtFail);
				throw new MSTechnicalException(lastError);
			}
		} else {
			for (final ResultSetFuture future : futures) {
				future.getUninterruptibly();
			}
		}
		return true;
	}

    @Override
    public Evt trouverDernierEvtTA(Evt evenement) {
    	Evt dernierEvtTA = null;

    	try {
			final DateTime dateTime = new DateTime(evenement.getDateEvt());
			final Date startDate = dateTime.withTimeAtStartOfDay().toDate();
			final ResultSet resultSet = getSession().execute(
					psTrouverDerniersEvts.bind(evenement.getNoLt(), prioriteTA, startDate, evenement.getDateEvt()));

    		/* On parcours tous les TA du colis du même jour, pour en déduire quel est le dernier TA (les evts sont dans l'ordre chronologique inverse) */
    		/* Subtilité : il peut y avoir plusieurs TA de même date evt. Dans ce cas, il faut prendre la derniere insérée (qui n'est pas forcément la premiere que l'on voit. */
    		for (final Row row : resultSet.all()) {
    			if ("TA".equals(row.getString(3))) {
    				final Evt evt = new Evt() 
    						.setNoLt(row.getString(0))
    						.setCodeEvt(row.getString(3))
    						.setInfoscomp(row.getMap(4, String.class, String.class))
    						.setSsCodeEvt(row.getString(5))
    						.setDateEvt(row.getTimestamp(1))
    						.setDateCreationEvt(row.getString(2));

    				/* Si c'est le 1er TA rencontré, on considère que pour le moment c'est le dernier */
    				if (dernierEvtTA == null)
    					dernierEvtTA = evt;
    				else { // Sinon, si la date evt est egale, on prend la dernier inséré des deux (on compare les dates jusqu'a la seconde, pas plus)
						if (dernierEvtTA.getDateEvt().getTime() / 1000 == evt.getDateEvt().getTime() / 1000) {
							if (dernierEvtTA.getDateCreationEvt().compareTo(evt.getDateCreationEvt()) < 0) {
								dernierEvtTA = evt;
							}
						} else // Si on est sur des evts plus vieux (date evt)
								// alors pas la peine de continuer
							break;
    				}
    			}
    		}
    	} catch (final NoHostAvailableException | QueryExecutionException | QueryValidationException | UnsupportedFeatureException e) {
    		throw new MSTechnicalException("Requête sur la table <" + ETableEvt.TABLE_NAME + "> impossible", e);
    	}
    	return dernierEvtTA;
    }
    
    @Override
	public Tournee trouverDerniereTournee(final Evt evenement) {
		Tournee derniereTournee = null;

		try {
			final List<ResultSetFuture> futures = new ArrayList<>();

			final DateTime dateTime = new DateTime(evenement.getDateEvt());
			final String jourEvt = dateTime.toString("yyyyMMdd");

			futures.add(getSession().executeAsync(
					psTrouverDernieresTournees.bind(evenement.getLieuEvt(), jourEvt, evenement.getSsCodeEvt())));

			final Future<List<ResultSet>> results = Futures.successfulAsList(futures);
			for (final ResultSet resultSet : results.get()) {
				if (resultSet != null) {
					for (final Row row : resultSet.all()) {
						final Date dateTournee = row.getTimestamp(ETableIdxTourneeJour.DATE_TOURNEE.getNomColonne());
						if (!dateTournee.after(evenement.getDateEvt()) && (derniereTournee == null ||
								dateTournee.after(derniereTournee.getDateTournee()))) {
							final Tournee tournee = new Tournee();
							tournee.setAgence(row.getString(ETableIdxTourneeJour.AGENCE.getNomColonne()));
							tournee.setCodeTournee(row.getString(ETableIdxTourneeJour.CODE_TOURNEE.getNomColonne()));
							tournee.setDateTournee(dateTournee);
							tournee.setIdC11(row.getString(ETableIdxTourneeJour.ID_TOURNEE.getNomColonne()));
							derniereTournee = tournee;
						}
					}
				}
			}
		} catch (final NoHostAvailableException | QueryExecutionException | QueryValidationException
				| UnsupportedFeatureException | InterruptedException | ExecutionException e) {
			throw new MSTechnicalException("Requête sur la table <" + ETableEvt.TABLE_NAME + "> impossible", e);
		}

		return derniereTournee;
	}
    
    /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
    @Override
	public void updateCptTrtTrtFailMS(final int nbTrt, final int nbTrtFail) {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptTrtTrtFailMS.bind((long) nbTrt, (long) nbTrtFail, "insertPointTournee",
					jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}
    
    /**
     * Mise à jour du compteur de MS 
     * @param evenement
     * @return
     */
    @Override
	public void updateCptHitMS() {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptHitMS.bind(new Long(1), "insertPointTournee", jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}

    @Override
	public void updateCptFailMS() {
		final DateTime dt = new DateTime();
		final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
		final String heure = String.format("%02d", dt.getHourOfDay());
		final String minute = String.format("%02d", dt.getMinuteOfHour()).substring(0, 1);
		try {
			getSession().execute(psUpdateCptFailMS.bind(new Long(1), "insertPointTournee", jour, heure, minute));
		} catch (Exception e) {
			log.warn("Can't write into microservice_counters");
		}
	}

    /**
     * Declaration d'une erreur dans la table des erreurs 
     * @param evenement
     * @return
     */
    @Override
    public void declareErreur(final Evt evt, final String methode, final Exception except) {
    	final DateTime dt = new DateTime();
    	final String jour = String.format("%04d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
    	String objetString;
    	if (FeatureFlips.INSTANCE.getBoolean("ErreurMS_Actif", true)){
    		if (evt != null)
    			objetString = evt.getNoLt()+" "+evt.getCodeEvt()+" "+evt.getDateEvt();
    		else 
    			objetString = " - ";

    		try {
    			getSession().execute(psInsertErreur.bind(jour, "insertPointTournee",methode,objetString,dt,except.toString()));
    		} catch (Exception e) {
    			log.warn("Can't write into microservice_erreur");
    		}
    	}
    }

	@Override
	public IInsertPointTourneeDao setRefentielCodeService(final CacheManager<CodeService> cacheManager) {
		this.cacheCodeService = cacheManager;
		return this;
	}

	@Override
	public IInsertPointTourneeDao setRefentielAgence(final CacheManager<Agence> cacheManagerAgence) {
		this.cacheManagerAgence = cacheManagerAgence;
		return this;
	}

	@Override
	public IInsertPointTourneeDao setRefentielParametre(final CacheManager<Parametre> cacheManagerParametre) {
		this.cacheManagerParametre = cacheManagerParametre;
		return this;
	}
}
