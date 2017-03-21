package com.chronopost.vision.microservices.reference;

import java.util.Set;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;

public interface IReferenceService {
	Set<CodeService> getCodesService();

	Set<Evenement> getEvenements();

	Set<Agence> getAgences();

	ReferenceServiceImpl setRefentielCodeService(final CacheManager<CodeService> cacheManager);

	ReferenceServiceImpl setRefentielEvenement(final CacheManager<Evenement> cacheEvenement);

	ReferenceServiceImpl setRefentielAgence(final CacheManager<Agence> cacheAgence);

	ReferenceServiceImpl setRefentielParametre(final CacheManager<Parametre> cacheParametre);
	
	String getParametreValue(final String paramName);
}
