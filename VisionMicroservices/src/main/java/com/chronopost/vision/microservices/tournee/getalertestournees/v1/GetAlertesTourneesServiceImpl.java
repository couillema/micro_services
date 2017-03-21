package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.joda.time.DateTime;

import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.microservices.tournee.getalertestournees.v1.commands.GetLtCommand;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.rules.LtRules;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GetAlertesTourneesServiceImpl implements IGetAlertesTourneesService {

    private IGetAlertesTourneesDao dao;
    private final long INTERVALLE_ALERTE = 1800000;

    /**
     * Singleton
     */
    static class InstanceHolder {

        public static IGetAlertesTourneesService service = new GetAlertesTourneesServiceImpl();

    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IGetAlertesTourneesService getInstance() {

        return InstanceHolder.service;
    }

    public IGetAlertesTourneesService setDao(IGetAlertesTourneesDao dao) {
        this.dao = dao;
        return this;
    }

    @Override
    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getalertestournees.v1.
     * IGetAlertesTourneesService#getAlertesTournees(java.util.List,
     * java.util.Date)
     */
    public GetAlertesTourneesOutput getAlertesTournees(List<String> codesTournee, Date dateVerification)
            throws NotFoundException, ParseException, InterruptedException, ExecutionException, TimeoutException {

        GetAlertesTourneesOutput output = new GetAlertesTourneesOutput();

        output.setAlertes(getAlertesTourneesDao(codesTournee, dateVerification));

        return output;
    }

    /**
     * Nouvelle méthode qui utilise la table d'indexation des colis avec créneau
     * 
     * @param codesTournee
     * @param dateVerification
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Map<String, Integer> getAlertesTourneesDao(List<String> codesTournee, Date dateVerification)
            throws InterruptedException, ExecutionException {

        // On crée le tableau des alertes à remplir et on l'initialise sur toute
        // les tournées
        Map<String, Integer> alertesTournees = Maps.newHashMap();
        for (String codeTournee : codesTournee) {
            alertesTournees.put(codeTournee, 0);
        }

        List<Future<Map<String, Lt>>> futures = Lists.newArrayList();

        // extraction des agences puis récupération des Lts en alerte
        Date dateMax = new DateTime().withMillis(dateVerification.getTime() + INTERVALLE_ALERTE).toDate();
        for (String codeAgence : getCodesAgence(codesTournee)) {
            List<String> ltsCandidates = dao.getNoLtAvecCreneauPourAgence(codeAgence, dateVerification, dateMax,
                    TypeBorneCreneau.BORNE_SUP);
            if (ltsCandidates.size() > 0) {
                futures.add(new GetLtCommand(ltsCandidates).queue());
            }
        }

        for (Future<Map<String, Lt>> future : futures) {
            // Scan des lt retournées pour trouver celles qui n'ont pas
            // encore de D+
            Map<String, Lt> lts = future.get();
            for (Entry<String, Lt> ltEntry : lts.entrySet()) {
                if (!LtRules.estPresenteOuLivre(ltEntry.getValue())) {
                    // Si le colis n'est pas encore livré
                    Evt ta = LtRules.getEvenementTaDuJour(ltEntry.getValue(), dateVerification);
                    if (ta != null) {
                        String codeTourneeComplet = EvtRules.getCodeAgence(ta) + EvtRules.getCodeTournee(ta);
                        if (alertesTournees.containsKey(codeTourneeComplet)) {
                            alertesTournees.put(codeTourneeComplet, alertesTournees.get(codeTourneeComplet) + 1);
                        }
                    }
                }
            }
        }

        return alertesTournees;
    }

    /**
     * Permet de récupérer une liste dédoublonnée des codes agence correspondant
     * à une liste de codes tournée.
     * 
     * @param codesTournee
     * @return
     */
    private Set<String> getCodesAgence(List<String> codesTournee) {
        Set<String> codesAgence = Sets.newTreeSet();
        for (String codeTournee : codesTournee) {
            codesAgence.add(codeTournee.substring(0, 3));
        }

        return codesAgence;
    }

}
