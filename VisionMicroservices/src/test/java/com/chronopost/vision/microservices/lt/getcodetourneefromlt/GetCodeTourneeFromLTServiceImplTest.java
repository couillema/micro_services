package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.Lt;

public class GetCodeTourneeFromLTServiceImplTest {

	private IGetCodeTourneeFromLTDAO daoMock = Mockito.mock(GetCodeTourneeFromLTDAOImpl.class);
	private GetLtV1 getLTV1Mock = Mockito.mock(GetLtV1.class);
	private GetCodeTourneeFromLTServiceImpl implToTest = (GetCodeTourneeFromLTServiceImpl) GetCodeTourneeFromLTServiceImpl.getInstance();

    @BeforeClass(groups={"init"})
    public void setUp() throws Exception {
        implToTest.setDao(daoMock) ;
        implToTest.setGetLtV1(getLTV1Mock) ;
    }
	
	@Test
	public void testFindBadParams() throws Exception {

		Mockito.reset(daoMock);
		Mockito.reset(getLTV1Mock);
		
		assertNull(implToTest.findTourneeBy(null, null));
		assertNull(implToTest.findTourneeBy("1234", null));
		assertNull(implToTest.findTourneeBy(null, new Date()));

	}

	@Test
	public void testLtNotFound() {

		Mockito.reset(daoMock);
		Mockito.reset(getLTV1Mock);
		
		try {
			implToTest.findTourneeBy("1234", new Date());

			// shouldn't be here !
			assertTrue(false);

		} catch (GetCodeTourneeFromLTException e) {

			assertEquals(e.getCodeErreur(), GetCodeTourneeFromLTException.LT_NOT_FOUND);
		}
	}

	@Test
	public void testTourneeNotFound() throws Exception {

		Mockito.reset(daoMock);
		Mockito.reset(getLTV1Mock);
		
		try {

			Map<String, Lt> mapLt = new HashMap<>();

			Lt lt = new Lt();
			lt.setNoLt("1234");
			mapLt.put("1234", lt);

			Mockito.when(getLTV1Mock.getLt(Arrays.asList("1234"))).thenReturn(mapLt);
			implToTest.findTourneeBy("1234", new Date());

			// shouldn't be here !
			assertTrue(false);

		} catch (GetCodeTourneeFromLTException e) {

			assertEquals(e.getCodeErreur(), GetCodeTourneeFromLTException.TOURNEE_NOT_FOUND);
		}
	}

	@Test
	public void testLTAndTourneeFound() throws Exception {

		Mockito.reset(daoMock);
		Mockito.reset(getLTV1Mock);
		
		Map<String, Lt> mapLt = new HashMap<>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		mapLt.put("1234", lt);

		Mockito.when(getLTV1Mock.getLt(Arrays.asList("1234"))).thenReturn(mapLt);

		Date date = new Date();

		GetCodeTourneeFromLTResponse modelReturned = new GetCodeTourneeFromLTResponse();
		modelReturned.setCodeAgence("AGENCE");
		modelReturned.setCodeTournee("TOURNEE");
		Mockito.when(daoMock.findTourneeBy("1234", date)).thenReturn(modelReturned);

		GetCodeTourneeFromLTResponse model = implToTest.findTourneeBy("1234", date);

		// shouldn't be here !
		assertEquals(model.getCodeAgence(), "AGENCE");
		assertEquals(model.getCodeTournee(), "TOURNEE");

	}

	@Test
	public void testLTFoundWithMaster() throws Exception {

		Mockito.reset(daoMock);
		Mockito.reset(getLTV1Mock);
		
		Map<String, Lt> mapLt = new HashMap<>();

		Lt lt = new Lt();
		lt.setNoLt("ABCD");
		mapLt.put("1234", lt);

		Mockito.when(getLTV1Mock.getLt(Arrays.asList("1234"))).thenReturn(mapLt);

		Date date = new Date();

		GetCodeTourneeFromLTResponse modelReturned = new GetCodeTourneeFromLTResponse();
		modelReturned.setCodeAgence("AGENCE");
		modelReturned.setCodeTournee("TOURNEE");
		Mockito.when(daoMock.findTourneeBy("ABCD", date)).thenReturn(modelReturned);

		GetCodeTourneeFromLTResponse model = implToTest.findTourneeBy("1234", date);

		// shouldn't be here !
		assertEquals(model.getCodeAgence(), "AGENCE");
		assertEquals(model.getCodeTournee(), "TOURNEE");

	}
}
