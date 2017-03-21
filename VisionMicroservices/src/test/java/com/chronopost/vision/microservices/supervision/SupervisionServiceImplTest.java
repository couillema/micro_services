package com.chronopost.vision.microservices.supervision;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.exceptions.InvalidParameterException;
import com.chronopost.vision.model.supervision.SnapShotVision;

public class SupervisionServiceImplTest {

	private final static SimpleDateFormat jour_SDF = new SimpleDateFormat("yyyyMMdd");

	private ISupervisionService service;
	private ISupervisionDao dao;

	@BeforeMethod
	public void initBeforeMethod() {
		dao = mock(ISupervisionDao.class);
		service = SupervisionServiceImpl.INSTANCE;
		service.setDao(dao);
	}

	/**
	 * Entrée : 2 lignes en Lt_counters et 2 lignes en Evt_counters
	 * Attendu : vérifie le calcul des relevés moyens
	 */
	@Test
	public void test_getSnapShotsAverageForADay() throws Exception {
		// GIVEN
		List<SnapShotVision> snapShots = new ArrayList<>();
		snapShots.add(createSnapShotVision(100L, 200L, 300L, 400L, 500L, 600L, 700L, 800L));
		// pas de lt pour cette tranche de dix minutes
		snapShots.add(createSnapShotVision(900L, 1000L, 1100L, 1200L, 1300L, 0L, 0L, 0L));
		// pas de evt pour cette tranche de dix minutes
		snapShots.add(createSnapShotVision(0L, 0L, 0L, 0L, 0L, 1400L, 1500L, 1600L));
		when(dao.getSnapShotVisionForDay(jour_SDF.parse("20161102"), false)).thenReturn(snapShots);
		// WHEN
		SnapShotVision snapShotsAverage = service.getSnapShotsAverageForADay("20161102");
		// THEN
		assertEquals(snapShotsAverage.getAskEvt(), new Long(1000/144));
		assertEquals(snapShotsAverage.getAskEvt().longValue(), 6L);
		assertEquals(snapShotsAverage.getDiffEvt(), new Long(1200/144));
		assertEquals(snapShotsAverage.getDiffEvt().longValue(), 8L);
		assertEquals(snapShotsAverage.getHitDiffEvt(), new Long(1400/144));
		assertEquals(snapShotsAverage.getHitDiffEvt().longValue(), 9L);
		assertEquals(snapShotsAverage.getHitEvt(), new Long(1600/144));
		assertEquals(snapShotsAverage.getHitEvt().longValue(), 11L);
		assertEquals(snapShotsAverage.getInsertEvt(), new Long(1800/144));
		assertEquals(snapShotsAverage.getInsertEvt().longValue(), 12L);
		assertEquals(snapShotsAverage.getAskLt(), new Long(2000/144));
		assertEquals(snapShotsAverage.getAskLt().longValue(), 13L);
		assertEquals(snapShotsAverage.getHitLt(), new Long(2200/144));
		assertEquals(snapShotsAverage.getHitLt().longValue(), 15L);
		assertEquals(snapShotsAverage.getInsertLt(), new Long(2400/144));
		assertEquals(snapShotsAverage.getInsertLt().longValue(), 16L);
	}

	/**
	 * Entrée : une date après aujourd'hui
	 * Attendu : la méthode retourne une InvalidParameterException
	 */
	@Test(expectedExceptions = { InvalidParameterException.class })
	public void test_getSnapShotsAverageForADay_jourDansLeFutur() throws Exception {
		// WHEN
		service.getSnapShotsAverageForADay(jour_SDF.format(new DateTime().plusDays(1).toDate()));
	}

	/**
	 * Entrée : pour la méthode getSnapShotsAverageForADay, paramètre jour vide
	 * Attendu : la date de recherche est celle d'aujourd'hui
	 */
	@Test
	public void test_getSnapShotsAverageForADay_jourVide() throws Exception {
		// GIVEN
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		// WHEN
		service.getSnapShotsAverageForADay("");
		// THEN
		verify(dao).getSnapShotVisionForDay(dateCaptor.capture(), Mockito.eq(true));
		Date date = dateCaptor.getAllValues().get(0);
		assertEquals(jour_SDF.format(date), jour_SDF.format(new Date()));
	}

	/**
	 * Entrée : paramètre jour 20161102
	 * Attendu : la date de recherche est bien formatée
	 */
	@Test
	public void test_getSnapShotsVisionForADay() throws Exception {
		// GIVEN
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		// WHEN
		service.getSnapShotsVisionForADay("20161102");
		// THEN
		verify(dao).getSnapShotVisionForDay(dateCaptor.capture(), Mockito.anyBoolean());
		Date date = dateCaptor.getAllValues().get(0);
		assertEquals(jour_SDF.format(date), "20161102");
	}

	/**
	 * Entrée : pour la méthode getSnapShotsVisionForADay, paramètre jour vide
	 * Attendu : la date de recherche est celle d'aujourd'hui
	 */
	@Test
	public void test_getSnapShotsVisionForADay_jourVide() throws Exception {
		// GIVEN
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		// WHEN
		service.getSnapShotsVisionForADay("");
		// THEN
		verify(dao).getSnapShotVisionForDay(dateCaptor.capture(), Mockito.eq(true));
		Date date = dateCaptor.getAllValues().get(0);
		assertEquals(jour_SDF.format(date), jour_SDF.format(new Date()));
	}

	private SnapShotVision createSnapShotVision(Long askEvt, Long diffEvt, Long hitDiffEvt, Long hitEvt, Long insertEvt,
			Long askLt, Long hitLt, Long insertLt) {
		SnapShotVision snapShotVision = new SnapShotVision();
		snapShotVision.setAskEvt(askEvt);
		snapShotVision.setDiffEvt(diffEvt);
		snapShotVision.setHitDiffEvt(hitDiffEvt);
		snapShotVision.setHitEvt(hitEvt);
		snapShotVision.setInsertEvt(insertEvt);
		snapShotVision.setAskLt(askLt);
		snapShotVision.setHitLt(hitLt);
		snapShotVision.setInsertLt(insertLt);
		return snapShotVision;
	}
}
