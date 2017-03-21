package com.chronopost.vision.microservices.genereevt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.InsertEvtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class GenereEvtServiceImplTest {

    private GenereEvtServiceImpl service;
    private WireMockServer wireMockServer;
    private WireMock wireMock;
    private int httpPort = RandomUts.getRandomHttpPort2(); // ADDED JJC

    private ITranscoderDao mockTranscoderDao = mock(ITranscoderDao.class);
    private List<Evt> evts;

    @BeforeClass
    public void beforeClass() throws Exception {

        initEvts();

        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);

        service = GenereEvtServiceImpl.getInstance();
        service.setEndpoint("http://127.0.0.1:" + httpPort + "/SGESServiceWS");
        // service.setEndpoint("http://127.0.0.1:3128/mockCalculRetardServiceWSSoapBinding");

        service.setMapper(new Mapper());

        InsertEvtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);

        wireMock.register(post(urlMatching("/SGESServiceWS.*")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBody(ServiceMockResponses.readResponse("sgesservicews.xml"))));

        wireMock.register(post(urlEqualTo("/InsertEvt/v1"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        initEvts();

        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
        Map<String, String> transcosCodeBcoLink = new HashMap<>();
        transcosCodeBcoLink.put("190", "TEST190");
        transcosCodeBcoLink.put("191", "TEST191");
        transcosCodeBcoLink.put("240", "TEST240");
        transcosCodeBcoLink.put("193", "TEST193");
        map.put("code_bco_link", transcosCodeBcoLink);
        Transcoder transcoderDiffVision = new Transcoder();
        transcoderDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcoderDiffVision);

        when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
        TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
    }

    @AfterClass
    public void afterClass() {
    }

	@Test
	public void genereEvtAvecInjection() {
		try {
			assertFalse(service.genereEvt(evts, true).containsValue(false));
		} catch (MSTechnicalException e) {
			fail("TechnicalException", e);
		} catch (FunctionalException e) {
			fail("FunctionalException", e);
		}
	}

	@Test
	public void genereEvtSansInjection() {
		try {
			assertFalse(service.genereEvt(evts, false).containsValue(false));
		} catch (MSTechnicalException e) {
			fail("TechnicalException", e);
		} catch (FunctionalException e) {
			fail("FunctionalException", e);
		}
	}

    @Test
	public void genereEvtAvecException() {
		try {
            wireMock.register(post(urlMatching("/SGESServiceWS.*")).willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
							.withBody(ServiceMockResponses.readResponse("sgesservicews_error.xml"))));

			service.genereEvt(evts, true);
		} catch (MSTechnicalException e) {
			assertTrue(true);
		} catch (FunctionalException e) {
			fail("FunctionalException", e);
		} catch (FileNotFoundException e) {
			fail("Erreur en lisant le fichier de tests", e);
		}
	}

    private void initEvts() {
        Date date1;
        try {
            date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00");
        } catch (ParseException e) {
            date1 = new Date();
        }

        Map<String, String> infoscomp = new HashMap<String, String>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt1 = new Evt().setPrioriteEvt(146).setDateEvt(date1).setNoLt("NUMEROLT1")
                .setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191")
                .setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0)
                .setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0").setStatusEvt("Acheminement en cours")
                .setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service")
                .setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        Evt evt2 = new Evt().setPrioriteEvt(146).setDateEvt(date1).setNoLt("NUMEROLT2")
                .setCabRecu("%0020090NA146848396248899250").setCodeEvt("D").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191")
                .setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0)
                .setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0").setStatusEvt("Acheminement en cours")
                .setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service")
                .setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");
        evts = Arrays.asList(evt1, evt2);
    }
}
