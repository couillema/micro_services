package com.chronopost.vision.microservices.updatereferentiel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratCarac;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class ReferenceVisionDaoImplTest {

	private IReferentielVisionDao dao;
	private boolean suiteLaunch = true;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		if (!BuildCluster.clusterHasBuilt) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

		dao = ReferentielVisionDaoImpl.INSTANCE;
		dao.setCassandraSession(getSession());
		CacheManager<Parametre> mockParametre = Mockito.mock(CacheManager.class);
		Parametre param = new Parametre("TTL_REF_CONTRAT", "10");
		Mockito.when(mockParametre.getValue(Mockito.eq("TTL_REF_CONTRAT"))).thenReturn(param);
		dao.setRefentielParametre(mockParametre);
		getSession().execute("truncate ref_contrat");
	}

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	@Test(groups = { "database-needed", "slow" })
	public void insertRefContrat() {
		// GIVEN
		final List<ContratCarac> refContrats = new ArrayList<>();
		final ContratCarac ref1 = new ContratCarac("no1", "ATTRACTIF_0");
		refContrats.add(ref1);
		final ContratCarac ref2 = new ContratCarac("no2", "ATTRACTIF_1");
		refContrats.add(ref2);

		// WHEN
		dao.insertRefContrat("V1", refContrats);

		// THEN
		final PreparedStatement selectAll = getSession().prepare(
				QueryBuilder.select("numero_version", "numero_contrat", "caracteristiques").from("ref_contrat"));
		final ResultSet resultSet = getSession().execute(selectAll.bind());
		final List<Row> all = resultSet.all();
		assertEquals(all.size(), 2);
		// get numero_version
		assertEquals(all.get(0).get(0, String.class), "V1");
		assertEquals(all.get(1).get(0, String.class), "V1");
		// get numero_contrat
		assertEquals(all.get(0).get(1, String.class), "no1");
		assertEquals(all.get(1).get(1, String.class), "no2");
		// get caracteristiques
		assertEquals(all.get(0).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_0")));
		assertEquals(all.get(1).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_1")));

		// vérifie que pour l'insert précédent, on a bien 2 lignes avec
		// numero_version = 'V1'
		boolean complete = dao.checkInsertVersionComplete("V1", 2);
		assertTrue(complete);
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
