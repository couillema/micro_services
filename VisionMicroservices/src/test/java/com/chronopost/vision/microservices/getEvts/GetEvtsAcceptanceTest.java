package com.chronopost.vision.microservices.getEvts;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildUpdate;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableEvt;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class GetEvtsAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}
	
	private final static Date DATE_1 = new DateTime().minusDays(1).toDate();
	private final static Date DATE_2 = new DateTime().toDate();
	private final static Date DATE_3 = new DateTime().minusDays(3).toDate();
	private final static Date DATE_4 = new DateTime().minusDays(2).toDate();

	private PreparedStatement psInsertEvts;
	private PreparedStatement psDeleteEvts;

	private Client client;
	private boolean suiteLaunch = true;

	@Override
	protected Application configure() {
		/*
		 * Si le cluster n'existe pas déjà, alors il faut le créer et considérer
		 * que le test est isolé (lancé seul)
		 */
		if (!BuildCluster.clusterHasBuilt) {
			try {
				BuildCluster.setUpBeforeSuite();
				suiteLaunch = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		CCMBridge.ipOfNode(1);

		/* Création de la resource et initialisation avec le service */
		final GetEvtsResource resource = new GetEvtsResource();
		resource.setService(GetEvtsServiceImpl.INSTANCE);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(resource);
		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();

		client = ClientBuilder.newClient();

		GetEvtsServiceImpl.INSTANCE.setDao(GetEvtsDaoImpl.INSTANCE);

		// Prepare insert dans idx_tournee_agence_jour
		psInsertEvts = getSession().prepare(buildUpdate(ETableEvt.TABLE_NAME,
				Arrays.asList(ETableEvt.CAB_EVT_SAISI, ETableEvt.CAB_RECU, ETableEvt.CODE_EVT_EXT,
						ETableEvt.CODE_POSTAL_EVT, ETableEvt.CODE_RAISON_EVT, ETableEvt.CODE_SERVICE,
						ETableEvt.CREATEUR_EVT, ETableEvt.DATE_CREATION_EVT, ETableEvt.ID_ACCES_CLIENT,
						ETableEvt.ID_EXTRACTION_EVT, ETableEvt.ID_SS_CODE_EVT, ETableEvt.IDBCO_EVT,
						ETableEvt.LIBELLE_EVT, ETableEvt.LIBELLE_LIEU_EVT, ETableEvt.POSITION_EVT,
						ETableEvt.PROD_CAB_EVT_SAISI, ETableEvt.PROD_NO_LT, ETableEvt.REF_EXTRACTION,
						ETableEvt.REF_ID_ABONNEMENT, ETableEvt.STATUS_ENVOI, ETableEvt.STATUS_EVT, ETableEvt.CODE_EVT,
						ETableEvt.INFOSCOMP, ETableEvt.LIEU_EVT, ETableEvt.SS_CODE_EVT)));

		psDeleteEvts = getSession().prepare(QueryBuilder.delete().from(ETableEvt.TABLE_NAME)
				.where(QueryBuilder.eq(ETableEvt.NO_LT.getNomColonne(), QueryBuilder.bindMarker())));
	}

	@Test
	public void test_getEvts() {
		// insére quatre evts en base dans le désordre
		// doit être le troisième élément de la liste
		getSession().execute(psInsertEvts.bind("cab_evt_1", "cab_recu_1", "code_evt_ext_1", "cp1", "code_raison_1",
				"code_service_1", "createur_1", "date_crea_1", 101, "id_extra_1", 201, 301, "lib1", "lib_lieu1", 401,
				501, 601, "ref_extra_1", "ref_abo_1", "envoi1", "status1", "code1", buildInfosComp(1), "lieu_1",
				"ss_code_evt_1", "noLt", 1, DATE_1));
		// plus récent, doit être le dernier élément de la liste
		getSession().execute(psInsertEvts.bind("cab_evt_2", "cab_recu_2", "code_evt_ext_2", "cp2", "code_raison_2",
				"code_service_2", "createur_2", "date_crea_2", 102, "id_extra_2", 202, 302, "lib2", "lib_lieu2", 402,
				502, 602, "ref_extra_2", "ref_abo_2", "envoi2", "status2", "code2", buildInfosComp(2), "lieu_2",
				"ss_code_evt_2", "noLt", 2, DATE_2));
		// plus ancien, doit être le premier élément de la liste
		getSession().execute(psInsertEvts.bind("cab_evt_3", "cab_recu_3", "code_evt_ext_3", "cp3", "code_raison_3",
				"code_service_3", "createur_3", "date_crea_3", 103, "id_extra_3", 203, 303, "lib3", "lib_lieu3", 403,
				503, 603, "ref_extra_3", "ref_abo_3", "envoi3", "status3", "code3", buildInfosComp(3), "lieu_3",
				"ss_code_evt_3", "noLt", 3, DATE_3));
		// doit être le deuxième élément de la liste
		getSession().execute(psInsertEvts.bind("cab_evt_4", "cab_recu_4", "code_evt_ext_4", "cp4", "code_raison_4",
				"code_service_4", "createur_4", "date_crea_4", 104, "id_extra_4", 204, 304, "lib4", "lib_lieu4", 404,
				504, 604, "ref_extra_4", "ref_abo_4", "envoi4", "status4", "code4", buildInfosComp(4), "lieu_4",
				"ss_code_evt_4", "noLt", 4, DATE_4));

		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("getEvts").path("noLt").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();

		// THEN
		final List<Evt> result = response.readEntity(new GenericType<List<Evt>>() {
		});
		assertEquals(response.getStatus(), 200);
		assertEquals(result.size(), 4);
		assertEvt(result.get(0), 3);
		assertEvt(result.get(1), 4);
		assertEvt(result.get(2), 1);
		assertEvt(result.get(3), 2);
	}

	private Map<String, String> buildInfosComp(final int numEvt) {
		final Map<String, String> infosComp = new HashMap<>();
		infosComp.put("key1", "value" + numEvt);
		return infosComp;
	}
	
	private void assertEvt(final Evt evt, final int idx) {
		assertEquals(evt.getNoLt(), "noLt");
		assertEquals(evt.getPrioriteEvt(), new Integer(idx));
		assertEquals(evt.getCabEvtSaisi(), "cab_evt_" + idx);
		assertEquals(evt.getCabRecu(), "cab_recu_" + idx);
		assertEquals(evt.getCodeEvtExt(), "code_evt_ext_" + idx);
		assertEquals(evt.getCodePostalEvt(), "cp" + idx);
		assertEquals(evt.getCodeRaisonEvt(), "code_raison_" + idx);
		assertEquals(evt.getCodeService(), "code_service_" + idx);
		assertEquals(evt.getCreateurEvt(), "createur_" + idx);
		assertEquals(evt.getDateCreationEvt(), "date_crea_" + idx);
		assertEquals(evt.getIdAccesClient(), new Integer(100 + idx));
		assertEquals(evt.getIdExtractionEvt(), "id_extra_" + idx);
		assertEquals(evt.getIdSsCodeEvt(), new Integer(200 + idx));
		assertEquals(evt.getIdbcoEvt(), new Integer(300 + idx));
		assertEquals(evt.getLibelleEvt(), "lib" + idx);
		assertEquals(evt.getLibelleLieuEvt(), "lib_lieu" + idx);
		assertEquals(evt.getPositionEvt(), new Integer(400 + idx));
		assertEquals(evt.getProdCabEvtSaisi(), new Integer(500 + idx));
		assertEquals(evt.getProdNoLt(), new Integer(600 + idx));
		assertEquals(evt.getRefExtraction(), "ref_extra_" + idx);
		assertEquals(evt.getRefIdAbonnement(), "ref_abo_" + idx);
		assertEquals(evt.getStatusEnvoi(), "envoi" + idx);
		assertEquals(evt.getStatusEvt(), "status" + idx);
		assertEquals(evt.getCodeEvt(), "code" + idx);
		assertEquals(evt.getInfoscomp(), buildInfosComp(idx));
		assertEquals(evt.getLieuEvt(), "lieu_" + idx);
		assertEquals(evt.getSsCodeEvt(), "ss_code_evt_" + idx);
		switch (idx) {
		case 1:
			assertEquals(evt.getDateEvt(), DATE_1);
			break;
		case 2:
			assertEquals(evt.getDateEvt(), DATE_2);
			break;
		case 3:
			assertEquals(evt.getDateEvt(), DATE_3);
			break;
		case 4:
			assertEquals(evt.getDateEvt(), DATE_4);
			break;
		}
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(psDeleteEvts.bind("noLt"));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
