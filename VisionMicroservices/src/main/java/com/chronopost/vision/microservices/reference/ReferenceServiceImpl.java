package com.chronopost.vision.microservices.reference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;

/**
 * @author xrenaux Lis les infos dans cache manager pour retourner des listes de
 *         code service, événement, agence et parametre
 */
public enum ReferenceServiceImpl implements IReferenceService {
	INSTANCE;

	private CacheManager<CodeService> cacheCodeService = null;
	private CacheManager<Evenement> cacheEvenement = null;
	private CacheManager<Agence> cacheAgence = null;
	private CacheManager<Parametre> cacheParametre = null;
	
	@Override
	public Set<CodeService> getCodesService() {
		final Map<String, CodeService> map = cacheCodeService.getCache();
		final Set<CodeService> codesService = new HashSet<>();
		codesService.addAll(map.values());
		return codesService;
	}

	@Override
	public Set<Evenement> getEvenements() {
		final Map<String, Evenement> map = cacheEvenement.getCache();
		final Set<Evenement> evenements = new HashSet<>();
		evenements.addAll(map.values());
		return evenements;
	}

	@Override
	public Set<Agence> getAgences() {
		final Map<String, Agence> map = cacheAgence.getCache();
		final Set<Agence> agences = new HashSet<>();
		agences.addAll(map.values());
		return agences;
	}

	@Override
	public String getParametreValue(final String paramName) {
		return cacheParametre.getValue(paramName).getValue();
	}

	@Override
	public ReferenceServiceImpl setRefentielCodeService(final CacheManager<CodeService> cacheCodeService) {
		this.cacheCodeService = cacheCodeService;
		return this;
	}

	@Override
	public ReferenceServiceImpl setRefentielEvenement(final CacheManager<Evenement> cacheEvenement) {
		this.cacheEvenement = cacheEvenement;
		return this;
	}

	@Override
	public ReferenceServiceImpl setRefentielAgence(final CacheManager<Agence> cacheAgence) {
		this.cacheAgence = cacheAgence;
		return this;
	}

	@Override
	public ReferenceServiceImpl setRefentielParametre(final CacheManager<Parametre> cacheParametre) {
		this.cacheParametre = cacheParametre;
		return this;
	}
}
