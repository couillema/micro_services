package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.assertj.core.util.Lists;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.collect.Maps;

/**
 * Implémentation d'un DAO ITourneeDao JJC getSession and reformate.
 */
public enum UpdateTourneeDaoImpl implements IUpdateTourneeDao {
    INSTANCE;

    private final String TYPE_INFORMATION_EVT = "evt";

    private PreparedStatement prepStatementUpdateTourneeTa;
    private PreparedStatement prepStatementUpdateTourneeDplus;
    private PreparedStatement prepStatementUpdateTourneeCollecte;
    private PreparedStatement prepStatementInsertInfoTournee;
    private PreparedStatement prepStatementInsertAgenceTournee;
    private PreparedStatement prepStatementInsertTourneeC11;
    private PreparedStatement prepStatementInsertColisTourneeAgence;
    private PreparedStatement prepStatementDebutTournee;
    private PreparedStatement prepStatementFinTournee;
    private PreparedStatement prepStatementTourneeCodeService;
    private PreparedStatement prepStatementBorneColisRisque;
    private PreparedStatement prepStatementChauffeurTournee;

    private UpdateTourneeDaoImpl() {
		prepStatementUpdateTourneeTa		= getSession().prepare("update tournees USING TTL ? set ta = ta + ?  where code_tournee = ? and date_jour = ?");
		prepStatementUpdateTourneeDplus		= getSession().prepare(	"update tournees USING TTL ? set distri = distri + ?  where code_tournee = ? and date_jour = ?");
		prepStatementUpdateTourneeCollecte	= getSession().prepare(	"update tournees USING TTL ? set collecte = collecte + ?  where code_tournee = ? and date_jour = ?");
		
		prepStatementInsertInfoTournee			= getSession().prepare("INSERT INTO info_tournee(code_tournee, date_heure_transmission, informations, type_information, date_jour, id_information) " +  "values (?, ?, ?, ?, ?, ?) USING TTL ?");
		prepStatementInsertAgenceTournee		= getSession().prepare("INSERT INTO agence_tournee(code_agence, code_tournee, date_jour) values(?,?,?) USING TTL ?");
		prepStatementInsertTourneeC11 			= getSession().prepare("INSERT INTO tournee_c11(code_tournee_c11, code_tournee_agence, date_maj) values(?, ?, ?) USING TTL ?");
		prepStatementInsertColisTourneeAgence	= getSession().prepare("INSERT INTO colis_tournee_agence(id_tournee, date_maj, numero_lt,id_c11) values(?, ?, ?, ?) USING TTL ?");
		
		prepStatementDebutTournee		= getSession().prepare("UPDATE tournees USING TTL ? set informations['debut'] = ?     WHERE code_tournee = ? and date_jour = ? ");
		prepStatementChauffeurTournee	= getSession().prepare("UPDATE tournees USING TTL ? set informations['chauffeur'] = ? WHERE code_tournee = ? and date_jour = ? ");
		prepStatementFinTournee 		= getSession().prepare("UPDATE tournees USING TTL ? set informations['fin'] = ?       WHERE code_tournee = ? and date_jour = ? ");
		
		prepStatementTourneeCodeService	= getSession().prepare("INSERT INTO tournees_par_code_service (code_tournee, date_jour, code_service) values (?, ?, ?) USING TTL ? ");
		prepStatementBorneColisRisque	= getSession().prepare("INSERT INTO lt_avec_creneau_par_agence (date_jour, code_agence, type_borne_livraison, borne_livraison, no_lt, code_tournee) values (?, ?, ?, ?, ?, ?) USING TTL ? ");
    }

    /**
     * 
     * @return VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core )
     */
    private Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

	/* (non-Javadoc)
	 * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.ITourneeDao#updateTournee(java.util.List)
	 */
    @Override
    public boolean updateTournee(List<Evt> evts) throws ParseException {
        List<ResultSetFuture> futures = Lists.newArrayList();

        for (Evt evt : evts) {
            // Evenements de mise en distri (TA)
            if (EvtRules.estUnEvtMiseEnDistri(evt) && EvtRules.getIdC11(evt) != null && !EvtRules.estUnColisFictif(evt)) {
                Set<String> evtSet = new TreeSet<>();
                evtSet.add(evt.getNoLt());
				futures.add(getSession().executeAsync(prepStatementUpdateTourneeTa.bind(TTL.TOURNEE.getTimelapse(), evtSet, EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), DateRules.toDateSortable(evt.getDateEvt()) )));
            }

            // Evenements de mise en distri (TA) avec créneau de livraison (RDV)
            if (EvtRules.estUnEvtMiseEnDistri(evt) && EvtRules.getIdC11(evt) != null && !EvtRules.estUnColisFictif(evt) && EvtRules.getCreneauMaxRdv(evt)!=null){
				futures.add(getSession().executeAsync(prepStatementBorneColisRisque.bind(DateRules.toDateSortable(evt.getDateEvt()), EvtRules.getCodeAgence(evt), TypeBorneCreneau.BORNE_SUP.getTypeBorne(), EvtRules.getCreneauMaxRdv(evt), evt.getNoLt(), EvtRules.getCodeTournee(evt), TTL.TOURNEE.getTimelapse())));
				futures.add(getSession().executeAsync(prepStatementBorneColisRisque.bind(DateRules.toDateSortable(evt.getDateEvt()), EvtRules.getCodeAgence(evt), TypeBorneCreneau.BORNE_INF.getTypeBorne(), EvtRules.getCreneauMinRdv(evt), evt.getNoLt(), EvtRules.getCodeTournee(evt), TTL.TOURNEE.getTimelapse())));
            }

            // Evenements de transport pendant la distri (D+)
            if (EvtRules.estUnEvtDplus(evt) && EvtRules.getIdC11(evt) != null && !EvtRules.estUnColisFictif(evt)) {
                Set<String> evtSet = new TreeSet<>();
                evtSet.add(evt.getNoLt());
				futures.add(getSession().executeAsync(prepStatementUpdateTourneeDplus.bind(TTL.TOURNEE.getTimelapse(), evtSet, EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), DateRules.toDateSortable(evt.getDateEvt()) )));
            }

            // Evenements de collecte
            if (EvtRules.estUnEvtCollecte(evt) && EvtRules.getIdC11(evt) != null && !EvtRules.estUnColisFictif(evt)) {
                Set<String> evtSet = new TreeSet<>();
                evtSet.add(evt.getNoLt());
				futures.add(getSession().executeAsync(prepStatementUpdateTourneeCollecte.bind(TTL.TOURNEE.getTimelapse(), evtSet, EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), DateRules.toDateSortable(evt.getDateEvt()) )));
            }

            // Evenements fictifs de début de tournée
            if (EvtRules.estUnEvtFictifDebutDeTournee(evt)) {
                String chauffeur = EvtRules.getChauffeur(evt);

				futures.add(getSession().executeAsync(prepStatementDebutTournee.bind(TTL.TOURNEE.getTimelapse(), DateRules.toDateAndTimeSortable(evt.getDateEvt()), TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", evt.getLieuEvt()) + evt.getSsCodeEvt(), DateRules.toDateSortable(evt.getDateEvt()) )));
				if ( chauffeur != null) {
				    futures.add(getSession().executeAsync(prepStatementChauffeurTournee.bind(TTL.TOURNEE.getTimelapse(), chauffeur, TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", evt.getLieuEvt()) + evt.getSsCodeEvt(), DateRules.toDateSortable(evt.getDateEvt()) )));           
				}
            }

            // Evenements fictifs de fin de tournée
            if (EvtRules.estUnEvtFictifFinDeTournee(evt)) {
				futures.add(getSession().executeAsync(prepStatementFinTournee.bind(TTL.TOURNEE.getTimelapse(), DateRules.toDateAndTimeSortable(evt.getDateEvt()), TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", evt.getLieuEvt()) + evt.getSsCodeEvt(), DateRules.toDateSortable(evt.getDateEvt()) )));
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }

    public boolean updateTourneeCodeService(List<Evt> evts) {
        List<ResultSetFuture> futures = Lists.newArrayList();

        for (Evt evt : evts) {
            if (EvtRules.estUnEvtMiseEnDistri(evt) && EvtRules.getIdC11(evt) != null && evt.getCodeService() != null && !EvtRules.estUnColisFictif(evt)) {
                if (evt.getCodeService().length() >= 3) {
					futures.add(getSession().executeAsync(prepStatementTourneeCodeService.bind(EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), DateRules.toDateSortable(evt.getDateEvt()), evt.getCodeService(), TTL.TOURNEE.getTimelapse() )));
                }
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }

	/* (non-Javadoc)
	 * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.ITourneeDao#insertInfoTournee(java.util.List)
	 */
    @Override
    public boolean insertInfoTournee(List<Evt> evts) throws ParseException {
        List<ResultSetFuture> futures = Lists.newArrayList();
        for (Evt evt : evts) {
            if (EvtRules.getIdC11(evt) != null && !EvtRules.estUnColisFictif(evt)) {
                Map<String, String> informations = Maps.newHashMap();
                informations.put("code_evt", evt.getCodeEvt());
                informations.put("id_c11", EvtRules.getIdC11(evt));
                if (EvtRules.getLatitudeDistri(evt) != null) {
                    informations.put("latitude", EvtRules.getLatitudeDistri(evt));
                }
                if (EvtRules.getLongitudeDistri(evt) != null) {
                    informations.put("longitude", EvtRules.getLongitudeDistri(evt));
                }
                futures.add(getSession().executeAsync(
									prepStatementInsertInfoTournee.bind(EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), evt.getDateEvt(), informations, TYPE_INFORMATION_EVT, DateRules.toDateSortable(evt.getDateEvt()), evt.getNoLt(), TTL.INFO_TOURNEE.getTimelapse())
							)
				);
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }

	/* (non-Javadoc)
	 * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.ITourneeDao#insertAgenceTournee(java.util.List)
	 */
    @Override
    public boolean insertAgenceTournee(List<Evt> evts) throws ParseException {
        List<ResultSetFuture> futures = Lists.newArrayList();

        for (Evt evt : evts) {
            if (EvtRules.getCodeAgence(evt)!=null && EvtRules.getCodeTournee(evt)!=null  && EvtRules.getIdC11(evt)!=null && !EvtRules.estUnColisFictif(evt)){
				futures.add(getSession().executeAsync(prepStatementInsertAgenceTournee.bind(EvtRules.getCodeAgence(evt), EvtRules.getCodeTournee(evt), DateRules.toDateSortable(evt.getDateEvt()), TTL.AGENCE_TOURNEE.getTimelapse() )));
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }

	/* (non-Javadoc)
	 * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.ITourneeDao#insertTourneeC11(java.util.List)
	 */
    @Override
    public boolean insertTourneeC11(List<Evt> evts) throws ParseException {
        List<ResultSetFuture> futures = Lists.newArrayList();

        for (Evt evt : evts) {
            if (EvtRules.getCodeAgence(evt)!=null && EvtRules.getCodeTournee(evt)!=null && EvtRules.getIdC11(evt)!=null && !EvtRules.estUnColisFictif(evt)){				
				futures.add(getSession().executeAsync(prepStatementInsertTourneeC11.bind(EvtRules.getIdC11SansCodeAgence(evt), EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt) , evt.getDateEvt(), TTL.TOURNEE_C11.getTimelapse() )));
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.ITourneeDao#insertColisTourneeAgence(java.util.List)
     */
    @Override
    public boolean insertColisTourneeAgence(List<Evt> evts) throws ParseException {
        List<ResultSetFuture> futures = Lists.newArrayList();

        for (Evt evt : evts) {
            if (EvtRules.getCodeAgence(evt)!=null && EvtRules.getCodeTournee(evt)!=null && EvtRules.getIdC11(evt)!=null && !EvtRules.estUnColisFictif(evt)){				
				futures.add(getSession().executeAsync(prepStatementInsertColisTourneeAgence.bind(EvtRules.getCodeAgence(evt) + EvtRules.getCodeTournee(evt), evt.getDateEvt() , evt.getNoLt(), EvtRules.getIdC11(evt), TTL.COLIS_TOURNEE_AGENCE.getTimelapse() )));				
            }
        }

        for (ResultSetFuture future : futures) {
            future.getUninterruptibly();
        }

        return true;
    }
}
