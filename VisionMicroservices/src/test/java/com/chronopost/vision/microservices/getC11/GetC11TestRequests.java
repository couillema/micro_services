package com.chronopost.vision.microservices.getC11;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildDelete;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildUpdate;
import static java.util.Arrays.asList;

import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class GetC11TestRequests {

	protected static PreparedStatement getInsertIdxAgenceJour(final Session session) {
		// Prepare insert dans idx_tournee_agence_jour
		return session.prepare(buildUpdate(ETableIdxTourneeJour.TABLE_NAME, asList(ETableIdxTourneeJour.ID_TOURNEE)));
	}

	protected static PreparedStatement getInsertTournee(final Session session) {
		return session.prepare(buildUpdate(ETableTournee.TABLE_NAME,
				asList(ETableTournee.TYPE_PRISE_EN_CHARGE, ETableTournee.DUREE_PREVUE, ETableTournee.DISTANCE_PREVUE,
						ETableTournee.DUREE_PAUSE, ETableTournee.ID_POI_STT, ETableTournee.POINTS,
						ETableTournee.DEBUT_PREVU)));
	}

	protected static PreparedStatement getInsertTourneePoint(final Session session) {
		// Prepare insert dans tournee_point
		return session.prepare(buildUpdate(ETableTourneePoint.TABLE_NAME,
				asList(ETableTourneePoint.NUMERO, ETableTourneePoint.HEURE_CONTRACTUELLE, ETableTourneePoint.DEBUT_RDV,
						ETableTourneePoint.FIN_RDV, ETableTourneePoint.TYPE_DESTINATION,
						ETableTourneePoint.NOM_DESTINATION, ETableTourneePoint.RAISON_SOCIALE_DESTINATION,
						ETableTourneePoint.PRODUIT_PRINCIPAL, ETableTourneePoint.DUREE_STOP,
						ETableTourneePoint.ID_ADRESSE_DESTINATION, ETableTourneePoint.ID_DESTINATION,
						ETableTourneePoint.IS_SPOI, ETableTourneePoint.NB_COLIS_PREVUS,
						ETableTourneePoint.GAMME_PRODUIT)));
	}

	protected static PreparedStatement getDeleteIdxAgenceJour(final Session session) {
		// Prepare delete dans idx_tournee_agence_jour
		return session.prepare(buildDelete(ETableIdxTourneeJour.TABLE_NAME));
	}

	protected static PreparedStatement getDeleteTournee(final Session session) {
		// Prepare delete dans tournee
		return session.prepare(buildDelete(ETableTournee.TABLE_NAME));
	}

	protected static PreparedStatement getDeleteTourneePoint(final Session session) {
		// Prepare delete dans tournee_point
		return session.prepare(buildDelete(ETableTourneePoint.TABLE_NAME));
	}
}
