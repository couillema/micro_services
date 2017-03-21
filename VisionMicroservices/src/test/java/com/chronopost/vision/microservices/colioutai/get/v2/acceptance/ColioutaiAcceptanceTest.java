package com.chronopost.vision.microservices.colioutai.get.v2.acceptance;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.testng.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.xml.ws.BindingProvider;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiException;
import com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiService;
import com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiServiceImpl;
import com.chronopost.vision.microservices.colioutai.get.v2.services.GoogleGeocoderHelper;
import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.ut.RandomUts;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import fr.chronopost.poi.webservice.PoiService;
import fr.chronopost.poi.webservice.impl.PoiWebService;
import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.ConsigneServiceWSService;

public class ColioutaiAcceptanceTest {

	private boolean suiteLaunch = true;

	private WireMockServer wireMockServer;
	private WireMock wireMock;

	/**
	 * port http d'écoute du serveur Wiremock.
	 */
	private int httpPort;

	@BeforeClass(groups = { "init" })
	public void setUp() throws Exception {
		if (!BuildCluster.clusterHasBuilt) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}

		httpPort = RandomUts.getRandomHttpPort();
		wireMockServer = new WireMockServer(httpPort);
		wireMockServer.start();
		WireMock.configureFor("127.0.0.1", httpPort);
		wireMock = new WireMock("127.0.0.1", httpPort);
	}

	@Test(groups = { "slow", "acceptance" })
	public void cas1Test1() throws FileNotFoundException, ColioutaiException, MalformedURLException, ParseException {
		wireMock.register(post(urlEqualTo("/GetLTs/true")).withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
						.withBody(ServiceMockResponses.readResponse("getltv1_get_detail_tournee_response2.json"))));

		wireMock.register(get(urlPathMatching("/GetCodeTourneeFromLT/6M05162901233/*"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
						.withBody(ServiceMockResponses.readResponse("get_code_tournee_from_lt_response1.json"))));

		wireMock.register(
				get(urlPathMatching("/GetDetailTournee/v1/*")).withHeader("Accept", equalTo("application/json"))
						.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
								.withBody(ServiceMockResponses.readResponse("get_detail_tournee_v1_response1.json"))));

		ConsigneServiceWS serviceConsigne = new ConsigneServiceWSService().getConsigneServiceWSPort();
		BindingProvider bpConsigne = (BindingProvider) serviceConsigne;
		bpConsigne.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://127.0.0.1:" + httpPort + "/consigne");

		wireMock.register(
				post(urlPathMatching("/consigne*"))
						.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
								.withBody(ServiceMockResponses.readResponse("get_consigne.xml"))));
		
		PoiService servicePOI = new PoiWebService().getPoiServiceImplPort();
		BindingProvider bpPOI = (BindingProvider) servicePOI;
		bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://127.0.0.1:" + httpPort + "/poi");

		wireMock.register(
				post(urlPathMatching("/poi*"))
						.withRequestBody(WireMock.containing("annoncePOI"))
						.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
								.withBody(ServiceMockResponses.readResponse("poi_annonce.xml"))));
		
		wireMock.register(
				post(urlPathMatching("/poi*"))
						.withRequestBody(WireMock.containing("findAdresseById"))
						.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
								.withBody(ServiceMockResponses.readResponse("poi_find_adresse.xml"))));
		
		ColioutaiService service = new ColioutaiServiceImpl(
				GoogleGeocoderHelper.getInstance("proxywebi1.chronopost.fr", "3128", 10),
				"http://127.0.0.1:" + httpPort + "/poi", GetLtV1.getInstance(), GetDetailTourneeV1.getInstance(),
				GetCodeTourneeFromLtV1.getInstance(), "http://127.0.0.1:" + httpPort + "/consigne",
				"http://wyn3e11.tlt:51090/chronopost-ws-xchrono/ws/XChrono");

		GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
		GetDetailTourneeV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
		GetCodeTourneeFromLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);

		ColioutaiInfoLT infoLT = service.findInfoLT("6M05162901233", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT);
	}

	@AfterClass(groups = { "init" })
	public void tearDownAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
