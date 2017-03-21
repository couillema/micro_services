package com.chronopost.vision.microservices.insertC11;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.insertC11.TourneeC11;
import com.datastax.driver.core.exceptions.DriverException;

public class InsertC11ServiceImplTest {

	private IInsertC11Service service;
	private IInsertC11Dao insertC11DaoMock;
	private TourneeC11 tourneeC11;

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		insertC11DaoMock = mock(IInsertC11Dao.class);
		service = InsertC11ServiceImpl.INSTANCE;
		service.setDao(insertC11DaoMock);
	}

	@BeforeMethod
	public void initTourneeC11() {
		tourneeC11 = new TourneeC11();
	}

	@Test
	public void test_miseAJourTourneeWithException() {
		when(insertC11DaoMock.miseAJourTournee(tourneeC11)).thenThrow(new DriverException("Error when update tournee"));
		try {
			service.traitementC11(new TourneeC11());
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "Error when update tournee");
		}
	}

	@Test
	public void test_miseAJourIdxTourneeJourWithException() {
		when(insertC11DaoMock.miseAJourIdxTourneeJour(tourneeC11))
				.thenThrow(new DriverException("Error when update idx"));
		try {
			service.traitementC11(new TourneeC11());
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "Error when update idx");
		}
	}

	@Test
	public void test_miseAJourPointsWithException() {
		when(insertC11DaoMock.miseAJourPoints(tourneeC11)).thenThrow(new DriverException("Error when update points"));
		try {
			service.traitementC11(new TourneeC11());
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "Error when update points");
		}
	}
}
