package com.chronopost.vision.microservices.supervision;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.exceptions.InvalidParameterException;
import com.chronopost.vision.model.supervision.SnapShotVision;

public enum SupervisionServiceImpl implements ISupervisionService {
	INSTANCE;
	
	private final Logger LOGGER = LoggerFactory.getLogger(SupervisionServiceImpl.class);

	private final static SimpleDateFormat jour_SDF = new SimpleDateFormat("yyyyMMdd");

	private ISupervisionDao dao;

	@Override
	public void setDao(final ISupervisionDao dao) {
		this.dao = dao;
	}

	/**
	 * Récupére la liste des relevés en base pour un jour donné. Un objet de la liste retournée
	 * représente une tranche de 10 minutes en base.
	 * @throws ParseException 
	 */
	@Override
	public List<SnapShotVision> getSnapShotsVisionForADay(String jour) throws Exception {
		Date snapShotDate;
		Boolean isToday = false;
		try {
			if (StringUtils.isBlank(jour)) {
				snapShotDate = new Date();
				isToday = true;
			} else {
				if (new DateTime(jour_SDF.parse(jour)).compareTo(new DateTime()) > 0) {
					throw new InvalidParameterException("Paramètre jour doit être dans le passé ou aujourd'hui", null);
				}
				snapShotDate = jour_SDF.parse(jour);
			}
		} catch (ParseException e) {
			LOGGER.error("Format du paramètre jour incorrect. Doit être yyyyMMdd et non " + jour, e);
			throw new InvalidParameterException("Format du paramètre jour incorrect. Doit être yyyyMMdd", e);
		}
		return dao.getSnapShotVisionForDay(snapShotDate, isToday);
	}

	/**
	 * Récupére les relevés en base de la dernière tranche de 10 minutes terminée.
	 */
	@Override
	public SnapShotVision getSnapShotVisionForLast10Minutes() throws Exception {
		// se place sur la tranche des dix dernières minutes terminées
		// RG-MSSupervision-002 : La vitesse instantanée est toujours déduite de la dernière tranche de dix minutes échues
		DateTime dateTime = new DateTime().minusMinutes(10);
		String jour = jour_SDF.format(dateTime.toDate());
		String heure = String.valueOf(dateTime.get(DateTimeFieldType.hourOfDay()));
		String minute = String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour()));
		if (minute.length() == 2) {
			minute = String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).substring(0, 1);
		} else {
			minute = "0";
		}
		return dao.getSnapShotByKey(jour, String.format("%02d", Integer.valueOf(heure)), minute);
	}

	/**
	 * Retourne un objet contenant les relevés moyens des counters en base pour un jour donné.
	 */
	@Override
	public SnapShotVision getSnapShotsAverageForADay(String jour) throws Exception {
		List<SnapShotVision> snapShots = new ArrayList<>();
		Date snapShotDate = null;
		Boolean isToday = false;
		try {
			if (StringUtils.isBlank(jour)) {
				snapShotDate = new Date();
				isToday = true;
			} else {
				if (new DateTime(jour_SDF.parse(jour)).compareTo(new DateTime()) > 0) {
					throw new InvalidParameterException("Paramètre jour doit être dans le passé ou aujourd'hui", null);
				}
				// si le jour demandé est aujourd'hui, s'assure qu'on prend les données uniquement passées
				if (jour_SDF.format(new Date()).equals(jour)) {
					isToday = true;
					snapShotDate = new Date();
				} else {
					snapShotDate = jour_SDF.parse(jour);
				}
			}
		} catch (ParseException e) {
			LOGGER.error("Format du paramètre jour incorrect. Doit être yyyyMMdd et non " + jour, e);
			throw new InvalidParameterException("Format du paramètre jour incorrect. Doit être yyyyMMdd", e);
		}
		snapShots = dao.getSnapShotVisionForDay(snapShotDate, isToday);
		SnapShotVision averageSnapShotVision = getAverageForSnapShotVision(snapShots, snapShotDate, isToday);
		averageSnapShotVision.setJour(jour_SDF.format(snapShotDate));
		return averageSnapShotVision;
	}
	
	/**
	 * Calcul les relevés moyens des counters en base pour chaque tranche de 10 minutes.
	 * Un objet de la liste représente une tranche de 10 minutes en base.
	 * @param snapShots
	 * @param jour 
	 * @param isToday 
	 * @return
	 */
	private SnapShotVision getAverageForSnapShotVision(List<SnapShotVision> snapShots, Date jour, Boolean isToday) {
		long avgAskEvt = 0;
		long avgDiffEvt = 0;
		long avgHitDiffEvt = 0;
		long avgHitEvt = 0;
		long avgInsertEvt = 0;
		long avgAskLt = 0;
		long avgInsertLt = 0;
		long avgHitLt = 0;
		for (SnapShotVision snapShot : snapShots) {
			// vérifie si pour le snapShot en cours il y a des données provenant de Evt_counters
			avgAskEvt = avgAskEvt + snapShot.getAskEvt();
			avgDiffEvt = avgDiffEvt + snapShot.getDiffEvt();
			avgHitDiffEvt = avgHitDiffEvt + snapShot.getHitDiffEvt();
			avgHitEvt = avgHitEvt + snapShot.getHitEvt();
			avgInsertEvt = avgInsertEvt + snapShot.getInsertEvt();
			// vérifie si pour le snapShot en cours il y a des données provenant de Lt_counters
			avgAskLt = avgAskLt + snapShot.getAskLt();
			avgInsertLt = avgInsertLt + snapShot.getInsertLt();
			avgHitLt = avgHitLt + snapShot.getHitLt();
		}
		// calcul les relevés moyens en fonction du nombre de tranche de 10 minutes présentes en base avec des données
		// divise les counters par un Double
		Long nbTranche10Minute = getNbTranche10MinutesForDay(isToday);
		SnapShotVision averageSnapShot = new SnapShotVision();
		averageSnapShot.setAskEvt(avgAskEvt/nbTranche10Minute);
		averageSnapShot.setDiffEvt(avgDiffEvt/nbTranche10Minute);
		averageSnapShot.setHitDiffEvt(avgHitDiffEvt/nbTranche10Minute);
		averageSnapShot.setHitEvt(avgHitEvt/nbTranche10Minute);
		averageSnapShot.setInsertEvt(avgInsertEvt/nbTranche10Minute);
		averageSnapShot.setAskLt(avgAskLt/nbTranche10Minute);
		averageSnapShot.setInsertLt(avgInsertLt/nbTranche10Minute);
		averageSnapShot.setHitLt(avgHitLt/nbTranche10Minute);
		return averageSnapShot;
	}

	/**
	 * Renvoie le nombre de dizaine de minutes (une ligne en base par dizaine)
	 * écoulées dans une journée. Si isToday = true, se place sur l'heure
	 * actuelle pour trouver les dizaines de minutes écoulées de la journée
	 * 
	 * @param isToday
	 */
	private long getNbTranche10MinutesForDay(Boolean isToday) {
		if (!isToday) {
			return 24*6;
		} else {
			DateTime dateTime = new DateTime();
			int heureMax = dateTime.get(DateTimeFieldType.hourOfDay());
			int minuteMax = Integer.valueOf(String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).substring(0, 1));
			long nbTranche = 0;
			// parcourt les 24 heures d'une journée
	        for (int heure = 0; heure < 24; heure++) {
	        	// parcourt les 6 dizaines de minutes d'une heure
	            for (int minute = 0; minute < 6; minute++) {
	            	// vérifie qu'on ne compte pas une dizaine de minutes non achevées ou dans le futur
					// RG-MSSupervision-003 : Dans les vitesses moyenne du jour,
					// la dernière tranche de dix minute n’est pas comptabilisée
					// si elle n’est pas échue. Sinon, il y a risque d’abaisser
					// la moyenne.
	            	if (heure < heureMax || (heure == heureMax && minute < minuteMax)) {
	            		nbTranche++;
					}
	            }
			}
			return nbTranche;
		}
	}

	@Override
	public boolean getMSStatus() throws Exception {
		return dao.checkSessionActive();
	}
}
