package com.chronopost.vision.microservices.maintienindexevt.v1;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.maintienindexevt.v1.model.MaintienIndexEvtDTO;
import com.chronopost.vision.microservices.maintienindexevt.v1.model.UpdateDepassementProactifInput;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

public class MaintienIndexEvtDaoImpl implements IMaintienIndexEvtDao {

    private final static Logger logger = LoggerFactory.getLogger(MaintienIndexEvtDaoImpl.class);

    /**
     * VisionMicroserviceApplication.cassandraSession (a
     * com.datastax.driver.core )
     */
    private final Session cassandraSession = VisionMicroserviceApplication.getCassandraSession();
    private final PreparedStatement prepStmtInsertTracesDateProactif;
    private final PreparedStatement prepStmtUpdateDepassementProactifParJour;
    private static final ObjectMapper mapper = new ObjectMapper();

    private MaintienIndexEvtDaoImpl() {
        prepStmtInsertTracesDateProactif = cassandraSession
                .prepare("INSERT INTO traces_date_proactif(no_lt, date_maj, code_evt, date_livraison_contractuelle, date_livraison_prevue, ws_data, en_retard) "
                        + " values(?, ?, ?, ?, ?, ?, ?) USING TTL ?");

        prepStmtUpdateDepassementProactifParJour = cassandraSession
                .prepare("INSERT INTO depassement_proactif_par_jour(date_livraison_contractuelle, no_lt, infos_lt, no_contrat, deleted) "
                        + " values(?, ?, ?, ?, ?) USING TTL ?");
    }

    /**
     * Singleton
     */
    static class InstanceHolder {
        public static final IMaintienIndexEvtDao dao;
        static {
            dao = new MaintienIndexEvtDaoImpl();
        }
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IMaintienIndexEvtDao getInstance() {
        return InstanceHolder.dao;
    }

    @Override
    public void insertTracesDateProactif(final MaintienIndexEvtInput maintienIndexEvtDataDTO) throws MSTechnicalException {

        try {
            if (maintienIndexEvtDataDTO == null || maintienIndexEvtDataDTO.getEvts() == null || maintienIndexEvtDataDTO.getEvts().size() == 0)
            	return;

            final ResultCalculerRetardPourNumeroLt resultCalculerRetardPourNumeroLt = maintienIndexEvtDataDTO.getResultatCalculRetard();

            if (resultCalculerRetardPourNumeroLt != null) {
                // Gestion des null dans le retour du calcul retard
                boolean isDateDeLivraisonPrevueCalculee = false;
                boolean isDateDeLivraisonEstimeeCalculee = false;
                String dateDeLivraisonPrevue = null;
                String dateDeLivraisonEstimee = null;
                int enRetardDateEstimeeSupDateContractuelle = 0;
                CalculDateDeLivraisonEstimee calculDateDeLivraisonEstimee;
                ResultRetard resultRetard;
                Analyse analyse;
                final Evt premierEvt = maintienIndexEvtDataDTO.getEvts().get(0);
                
                calculDateDeLivraisonEstimee = resultCalculerRetardPourNumeroLt.getCalculDateDeLivraisonEstimee();
                resultRetard = resultCalculerRetardPourNumeroLt.getResultRetard();
                analyse = resultCalculerRetardPourNumeroLt.getAnalyse();

                
                if ( resultRetard != null && resultRetard.getDateDeLivraisonPrevue() != null) {
                    isDateDeLivraisonPrevueCalculee = resultRetard.isDateDeLivraisonPrevueCalculee();
                    dateDeLivraisonPrevue = resultRetard.getDateDeLivraisonPrevue();
                }

                if (calculDateDeLivraisonEstimee != null && calculDateDeLivraisonEstimee.getDateDeLivraisonEstimee() != null) {
                    isDateDeLivraisonEstimeeCalculee = calculDateDeLivraisonEstimee.isDateDeLivraisonEstimeeCalculee();
                    dateDeLivraisonEstimee = calculDateDeLivraisonEstimee.getDateDeLivraisonEstimee() + " " + calculDateDeLivraisonEstimee.getHeureMaxDeLivraisonEstimee();
                }

                if (analyse != null) {
                    enRetardDateEstimeeSupDateContractuelle = analyse.getEnRetardDateEstimeeSupDateContractuelle();
                }

                // Fin de gestion des null dans le retour du calcul retard

                cassandraSession.execute(prepStmtInsertTracesDateProactif.bind(
                		premierEvt.getNoLt(),
                        new Date(),
                        premierEvt.getCodeEvt(),
                        isDateDeLivraisonPrevueCalculee && dateDeLivraisonPrevue != null ? DateRules
                                .toTimestampDateWsCalculRetard(dateDeLivraisonPrevue) : null,
                        isDateDeLivraisonEstimeeCalculee && dateDeLivraisonEstimee != null ? DateRules
                                .toTimestampDateWsCalculRetard(dateDeLivraisonEstimee) : null, mapper
                                .writeValueAsString(resultCalculerRetardPourNumeroLt), 
                        String.valueOf(enRetardDateEstimeeSupDateContractuelle), 
                        TTL.TRACES_DATE_PROACTIF.getTimelapse())
                );

            } else {
            	if (maintienIndexEvtDataDTO!=null && maintienIndexEvtDataDTO.getEvts()!=null){
            		final Evt premierEvt = maintienIndexEvtDataDTO.getEvts().get(0);
                	cassandraSession.execute(prepStmtInsertTracesDateProactif.bind(
                		premierEvt.getNoLt(), 
                		new Date(), 
                		premierEvt.getCodeEvt(), 
                		null, null, "","", 
                		TTL.TRACES_DATE_PROACTIF.getTimelapse()));
            	}
            }
        } catch (final Exception e) {
            logger.error("Erreur insertTracesDateProactif " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDepassementProactifParJour(final UpdateDepassementProactifInput updDepassementInput) {
    	final Map<String, String> infosLt = Maps.newHashMap();
        if (updDepassementInput.getDateLivraisonPrevue() != null) {
            infosLt.put("date_livraison_prevue",
                    DateRules.toDateAndTimeSortable(updDepassementInput.getDateLivraisonPrevue()));
        }
        if (updDepassementInput.getDateLivraisonContractuelle() != null) {
            infosLt.put("date_livraison_contractuelle",
                    DateRules.toDateAndTimeSortable(updDepassementInput.getDateLivraisonContractuelle()));
        }
        if (updDepassementInput.getLt().getCodePaysDestinataire() != null) {
            infosLt.put("code_pays_destinataire", updDepassementInput.getLt().getCodePaysDestinataire());
        }
        if (updDepassementInput.getLt().getCodeService() != null) {
            infosLt.put("code_service", updDepassementInput.getLt().getCodeService());
        }

        cassandraSession.execute(prepStmtUpdateDepassementProactifParJour.bind(DateRules
                .toDateSortable(updDepassementInput.getDateLivraisonContractuelle()), updDepassementInput.getNoLt(),
                infosLt, updDepassementInput.getLt().getNoContrat() == null ? "" : updDepassementInput.getLt()
                        .getNoContrat(),
                updDepassementInput.getDeleted() == null ? "" : updDepassementInput.getDeleted(),
                TTL.DEPASSEMENT_PROACTIF.getTimelapse()));
    }

    @Override
    public MaintienIndexEvtDTO createDTO(final MaintienIndexEvtInput maintienIndexEvtData) {
        return new MaintienIndexEvtDTO().setLt(maintienIndexEvtData.getLt()).setEvts(maintienIndexEvtData.getEvts())
                .setResultCalculRetard(maintienIndexEvtData.getResultatCalculRetard());
    }
}
