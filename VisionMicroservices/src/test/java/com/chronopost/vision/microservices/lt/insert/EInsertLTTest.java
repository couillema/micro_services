package com.chronopost.vision.microservices.lt.insert;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

// import jersey.repackaged.com.google.common.collect.Sets;

import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.EInsertLT;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.stringuts.StrUtils;

/** @author JJC : simple test for coherence with Lt.class */
public class EInsertLTTest {

	/**
	 * @param clz
	 *            whose field names we retrieve
	 * @return clz.getDeclaredFields() MINUS (toExclude1 + toExclude2)
	 */
	private static final Set<String> makeFlds(final Class<?> clz) {
		final Set<String> ret = new HashSet<>();
		for (Field cur : clz.getDeclaredFields())
			ret.add(cur.getName());
		return ret;
	}

	/** Will test that all lt fields attached to Enum exist ( no null !!) */
	@Test
	public void testEInsertLTNullLtFields() {
		final Set<String> bad = new HashSet<>();
		for (EInsertLT cur : EInsertLT.values()) {
			if (cur.getField() == null)
				bad.add("\n" + cur.getDesc());
		}
		assertEquals(0, bad.size(), "EInsertLT ERROR HAS " + bad.size() + " NULL FIELDS : " + bad);
	}

	/** Fields in Lt.class and not in enumeration. */
	private static final Set<String> LT_MINUS_EInsertLT = StrUtils.mkSet(true, "NO_INFOCOMPS", "no_lt", "etaMax",
			"heure_evt", "date_evt_readable", "heureMaxLivraison", "retardEta", "creneauTourneeRecopie", "evenements");

	/**
	 * Will test that all cur.getLtField().getName() are in LT except precisely
	 * for LT_MINUS_EInsertLT.
	 */
	@Test
	public void testEInsertLTLtFieldCoverage() {
		final Set<String> made = makeFlds(Lt.class);
		// -------- REMOVE Enum names
		for (EInsertLT cur : EInsertLT.values())
			made.remove(cur.getField().getName());
		// ------ REMOVE accepted missign
		made.removeAll(LT_MINUS_EInsertLT);

		assertEquals(0, made.size(), "ERREUR LT - Eselect : LT - ENUM - LT_MINUS_EInsertLT = " + made);
	}
}
