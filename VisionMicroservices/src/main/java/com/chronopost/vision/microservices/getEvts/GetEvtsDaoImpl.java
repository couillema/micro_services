package com.chronopost.vision.microservices.getEvts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.chronopost.cassandra.request.builder.CassandraRequestBuilder;
import com.chronopost.cassandra.table.ETableEvt;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public enum GetEvtsDaoImpl implements IGetEvtsDao {
	INSTANCE;

	// requete de selection des evts par noLt
	private final PreparedStatement psGetEvtsByNoLt;

	private GetEvtsDaoImpl() {
		psGetEvtsByNoLt = getSession().prepare(CassandraRequestBuilder.buildSelect(ETableEvt.TABLE_NAME,
				Arrays.asList(ETableEvt.PRIORITE_EVT, ETableEvt.DATE_EVT, ETableEvt.CAB_EVT_SAISI, ETableEvt.CAB_RECU,
						ETableEvt.CODE_EVT_EXT, ETableEvt.CODE_POSTAL_EVT, ETableEvt.CODE_RAISON_EVT,
						ETableEvt.CODE_SERVICE, ETableEvt.CREATEUR_EVT, ETableEvt.DATE_CREATION_EVT,
						ETableEvt.ID_ACCES_CLIENT, ETableEvt.ID_EXTRACTION_EVT, ETableEvt.ID_SS_CODE_EVT,
						ETableEvt.IDBCO_EVT, ETableEvt.LIBELLE_EVT, ETableEvt.LIBELLE_LIEU_EVT, ETableEvt.POSITION_EVT,
						ETableEvt.PROD_CAB_EVT_SAISI, ETableEvt.PROD_NO_LT, ETableEvt.REF_EXTRACTION,
						ETableEvt.REF_ID_ABONNEMENT, ETableEvt.STATUS_ENVOI, ETableEvt.STATUS_EVT, ETableEvt.CODE_EVT,
						ETableEvt.INFOSCOMP, ETableEvt.LIEU_EVT, ETableEvt.SS_CODE_EVT),
				ETableEvt.NO_LT).getQuery());
	}

	private Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	@Override
	public List<Evt> getLtEvts(final String noLt) throws Exception {
		final List<Evt> evts = new ArrayList<>();
		final ResultSet resultSet = getSession().execute(psGetEvtsByNoLt.bind(noLt));
		for (final Row row : resultSet.all()) {
			final Evt evt = new Evt();
			evt.setNoLt(noLt);
			evt.setPrioriteEvt(row.getInt(0));
			evt.setDateEvt(row.getTimestamp(1));
			evt.setCabEvtSaisi(row.getString(2));
			evt.setCabRecu(row.getString(3));
			evt.setCodeEvtExt(row.getString(4));
			evt.setCodePostalEvt(row.getString(5));
			evt.setCodeRaisonEvt(row.getString(6));
			evt.setCodeService(row.getString(7));
			evt.setCreateurEvt(row.getString(8));
			evt.setDateCreationEvt(row.getString(9));
			evt.setIdAccesClient(row.getInt(10));
			evt.setIdExtractionEvt(row.getString(11));
			evt.setIdSsCodeEvt(row.getInt(12));
			evt.setIdbcoEvt(row.getInt(13));
			evt.setLibelleEvt(row.getString(14));
			evt.setLibelleLieuEvt(row.getString(15));
			evt.setPositionEvt(row.getInt(16));
			evt.setProdCabEvtSaisi(row.getInt(17));
			evt.setProdNoLt(row.getInt(18));
			evt.setRefExtraction(row.getString(19));
			evt.setRefIdAbonnement(row.getString(20));
			evt.setStatusEnvoi(row.getString(21));
			evt.setStatusEvt(row.getString(22));
			evt.setCodeEvt(row.getString(23));
			evt.setInfoscomp(row.getMap(24, String.class, String.class));
			evt.setLieuEvt(row.getString(25));
			evt.setSsCodeEvt(row.getString(26));
			evts.add(evt);
		}
		return evts;
	}
}
