package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** @author JJC getSession and redundant <String>. **/
public class GetDetailTourneeDaoImpl implements IGetDetailTourneeDao {

    private PreparedStatement prepStatementGetTournee;
    private PreparedStatement prepStatementGetRelevesGps;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    private GetDetailTourneeDaoImpl() {
        prepStatementGetTournee = getSession()
                .prepare(
                        "select code_tournee, date_jour, distri, collecte, ta, eta_ok, eta_ko, informations from tournees where date_jour = ? and code_tournee = ?");
        prepStatementGetRelevesGps = getSession()
                .prepare(
                        "select code_tournee, type_information, date_heure_transmission, id_information, informations from info_tournee where code_tournee = ? and type_information = 'position' and date_heure_transmission >= ?");
    }

    /**
     * Singleton
     */
    static class InstanceHolder {

        public static IGetDetailTourneeDao service;

        static {
            if (service == null) {
                service = new GetDetailTourneeDaoImpl();
            }
        }
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IGetDetailTourneeDao getInstance() {

        return InstanceHolder.service;
    }

    @Override
    public Tournee getTournee(String codeTournee, Date dateTournee) throws ParseException {

        Tournee tournee = new Tournee();

        // On exécute les requêtes en asynchrones pour paralléliser leur
        // exécution
        ResultSetFuture resultFutureTournee = getSession().executeAsync(
                prepStatementGetTournee.bind(DateRules.toDateSortable(dateTournee), codeTournee));
        ResultSetFuture resultFutureRelevesGps = getSession().executeAsync(
                prepStatementGetRelevesGps.bind(codeTournee, dateTournee));

        ResultSet resultTournee = resultFutureTournee.getUninterruptibly();
        for (Row row : resultTournee) {
            tournee.setCodeTournee(row.getString("code_tournee"));
            tournee.setDateTournee(DateRules.fromDateSortable(row.getString("date_jour")));

            LinkedHashSet<String> ltsEnTa = new LinkedHashSet<>(row.getSet("ta", String.class));
            LinkedHashSet<String> ltsDistri = new LinkedHashSet<>(row.getSet("distri", String.class));
            LinkedHashSet<String> ltsCollecte = new LinkedHashSet<>(row.getSet("collecte", String.class));

            LinkedHashSet<String> ltsDeLaTournee = new LinkedHashSet<>();
            ltsDeLaTournee.addAll(ltsEnTa);
            ltsDeLaTournee.addAll(ltsDistri);
            ltsDeLaTournee.addAll(ltsCollecte);
            tournee.setInformations(row.getMap("informations", String.class, String.class));

            tournee.setLtsDeLaTournee(new ArrayList<>(ltsDeLaTournee));

        }

        ResultSet resultRelevesGps = resultFutureRelevesGps.getUninterruptibly();
        List<PositionGps> relevesGpsTournee = new ArrayList<>();
        for (Row row : resultRelevesGps) {
            double longitude = Double.parseDouble(row.getMap("informations", String.class, String.class).get(
                    "longitude"));
            double latitude = Double
                    .parseDouble(row.getMap("informations", String.class, String.class).get("latitude"));
            PositionGps position = new PositionGps().setCoordonnees(
                    new Position().setLati(latitude).setLongi(longitude)).setDateRelevePosition(
                    row.getTimestamp("date_heure_transmission"));
            relevesGpsTournee.add(position);
        }

        tournee.setRelevesGps(relevesGpsTournee);

        return tournee;
    }

}
