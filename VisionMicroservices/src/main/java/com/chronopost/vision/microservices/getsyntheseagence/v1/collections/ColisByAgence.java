package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class ColisByAgence {

	private final Set<String> colisSaisis = new HashSet<>();
	private final Set<String> colisASaisir = new HashSet<>();
	private final Set<String> colisRestantTg2 = new HashSet<>();

	/**
	 * @return an immutable copy of colisSaisis
	 */
	public ImmutableSet<String> getColisSaisis() {
		return ImmutableSet.copyOf(colisSaisis);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSaisis(Collection<String> c) {
		colisSaisis.addAll(c);
	}

	/**
	 * @return an immutable copy of colisASaisir
	 */
	public ImmutableSet<String> getColisASaisir() {
		return ImmutableSet.copyOf(colisASaisir);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisASaisir(Collection<String> c) {
		colisASaisir.addAll(c);
	}

	/**
	 * @return an immutable copy of colisRestantTg2
	 */
	public ImmutableSet<String> getColisRestantTg2() {
		return ImmutableSet.copyOf(colisRestantTg2);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisRestantTg2(Collection<String> c) {
		colisRestantTg2.addAll(c);
	}
	
	/**
	 * @return Le nombre total de colis (saisis + a saisir + restant tg2)
	 */
	public int getNbreAllColis() {
		int a=0,b=0,c=0;
		
		if (colisSaisis != null)
			a = colisSaisis.size();
		if (colisASaisir != null)
			b = colisASaisir.size();
		if (colisRestantTg2 != null)
			c = colisRestantTg2.size();
		
		return a+b+c;
	}
}
