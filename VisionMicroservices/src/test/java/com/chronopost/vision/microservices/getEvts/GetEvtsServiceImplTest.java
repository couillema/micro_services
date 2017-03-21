package com.chronopost.vision.microservices.getEvts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Evt;

public class GetEvtsServiceImplTest {

	private IGetEvtsService service;
	private IGetEvtsDao daoMock;

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		daoMock = mock(IGetEvtsDao.class);
		service = GetEvtsServiceImpl.INSTANCE;
		service.setDao(daoMock);
	}

	/**
	 * Dao retourne 4 evts dans la liste. Vérifier que les evts retournés
	 * ensuite par le service sont bien triès selons leurs dates
	 * @throws Exception 
	 */
	@Test
	public void test_orderEvts() throws Exception {
		// GIVEN
		final List<Evt> evts = new ArrayList<>();
		final Evt evt_1 = new Evt();
		evt_1.setCodeEvt("code1");
		evt_1.setDateEvt(new DateTime().minusDays(2).toDate());
		evts.add(evt_1);
		final Evt evt_2 = new Evt();
		evt_2.setCodeEvt("code2");
		evt_2.setDateEvt(new DateTime().minusDays(3).toDate());
		evts.add(evt_2);
		final Evt evt_3 = new Evt();
		evt_3.setCodeEvt("code3");
		evt_3.setDateEvt(new DateTime().toDate());
		evts.add(evt_3);
		final Evt evt_4 = new Evt();
		evt_4.setCodeEvt("code4");
		evt_4.setDateEvt(new DateTime().minusDays(1).toDate());
		evts.add(evt_4);
		when(daoMock.getLtEvts("noLt")).thenReturn(evts);
		
		// WHEN
		final List<Evt> evt_triés = service.getEvts("noLt");
		
		// THEN
		assertEquals(evt_triés.get(0).getCodeEvt(), "code2");
		assertEquals(evt_triés.get(1).getCodeEvt(), "code1");
		assertEquals(evt_triés.get(2).getCodeEvt(), "code4");
		assertEquals(evt_triés.get(3).getCodeEvt(), "code3");
	}
}
