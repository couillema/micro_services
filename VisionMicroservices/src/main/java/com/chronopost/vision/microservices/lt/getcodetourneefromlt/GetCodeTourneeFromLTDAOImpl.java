package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import java.util.Date;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.lt.get.GetLtDaoImpl;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class GetCodeTourneeFromLTDAOImpl implements IGetCodeTourneeFromLTDAO {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(GetLtDaoImpl.class);

    private final static String ID_TOURNEE = "id_tournee";

    private final static String DATE_MAJ = "date_maj";

    private Session session;

    private PreparedStatement statement;

    public GetCodeTourneeFromLTDAOImpl() {
        super();

        this.session = getSession();
        statement = session
                .prepare("select id_tournee, date_maj from colis_tournee_agence where numero_lt = ? and date_maj <= ? order by date_maj desc limit 1");

    }

    private static class InstanceHolder {
        static IGetCodeTourneeFromLTDAO dao;
        static {
            dao = new GetCodeTourneeFromLTDAOImpl();
        }
    }

    /** @return actually a static one to interact with Keyspace. */
    private Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * Singleton
     * 
     * @return made one.
     */
    public static IGetCodeTourneeFromLTDAO getInstance() {
        return InstanceHolder.dao;
    }

    @Override
    public GetCodeTourneeFromLTResponse findTourneeBy(String noLT, Date dateHeureSearch) {

        ResultSet resultSet = session.execute(statement.bind(noLT, dateHeureSearch));

        GetCodeTourneeFromLTResponse model = null;

        for (Row row : resultSet) {
            model = new GetCodeTourneeFromLTResponse();

            Date date = row.getTimestamp(DATE_MAJ);
            if (date != null) {
                String dateCourte = DateRules.toDateSortable(date);
                if (!dateCourte.equals(DateRules.toDateSortable(dateHeureSearch))) {
                    continue;
                }
            }

            String idTournee = row.getString(ID_TOURNEE);
            model.setCodeTournee(idTournee.substring(3, 8));
            model.setCodeAgence(idTournee.substring(0, 3));
        }

        return model;
    }

}
