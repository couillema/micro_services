package com.chronopost.vision.microservices.colioutai.get.services;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class PTVHelperTest {

	
	/**
	 * Ce test suppose qu'une dépendance externe est disponible alors que nous sommes dans les TU.
	 * A redévelopper avec un mock de la dépendance PTV.
	 * @throws Exception
	 */
	@Test
	public void testBasicOptim() throws Exception {

		/*XChronoWS servicePTV = new XChronoWSService().getXChronoWSPort();
		BindingProvider bpPOI = (BindingProvider) servicePTV;
		bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://wyn3e11.tlt:51090/chronopost-ws-xchrono/ws/XChrono");

		PTVHelperInterface ptvHelper = PTVHelper.getInstance(servicePTV);

		String heure = ptvHelper.heureArrivee(new Position(48.1d, 2.1d), "10:00", 1, new Position(48.5d, 2.4d));
		assertNotNull(heure);
		*/
		assertTrue(true);
	}

}
