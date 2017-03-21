package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.Lists;

/** @author unknown : JJC getSession +  import min.**/
public class GetAlertesTourneesDaoImpl implements IGetAlertesTourneesDao {

    PreparedStatement prepStatementLtAvecCreneau;

    /**
     * Singleton
     */
    static class InstanceHolder {

        public static IGetAlertesTourneesDao service;

        static {
            if (service == null) {
                service = new GetAlertesTourneesDaoImpl();
            }
        }
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IGetAlertesTourneesDao getInstance() {

        return InstanceHolder.service;
    }

    private GetAlertesTourneesDaoImpl() {
        prepStatementLtAvecCreneau = VisionMicroserviceApplication.getCassandraSession()
                .prepare("SELECT date_jour, code_agence, type_borne_livraison, borne_livraison, no_lt, code_tournee FROM lt_avec_creneau_par_agence "
                        + " WHERE date_jour = ? and code_agence = ? and type_borne_livraison = ? and borne_livraison >= ? and borne_livraison <= ?");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.tournee.getalertestournees.v1.
     * IAlertesTourneesDao#getLtAvecCreneauPourAgence(java.lang.String,
     * java.util.Date, java.util.Date,
     * com.chronopost.vision.microservices.utils.TypeBorneCreneau)
     */
    @Override
    public List<String> getNoLtAvecCreneauPourAgence(String codeAgence, Date dateInf, Date dateMax,
            TypeBorneCreneau typeBorne) {
        List<String> ltCandidates = Lists.newArrayList();
        ResultSet results = VisionMicroserviceApplication.getCassandraSession().execute(prepStatementLtAvecCreneau.bind(
                DateRules.toDateSortable(dateInf), codeAgence, typeBorne.getTypeBorne(), dateInf, dateMax));
        for (Row row : results) {
            ltCandidates.add(row.getString("no_lt"));
        }
        return ltCandidates;
    }

}
