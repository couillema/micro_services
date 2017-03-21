package com.chronopost.vision.microservices.maintienindexevt.v1;

import java.text.ParseException;

import com.chronopost.vision.microservices.maintienindexevt.v1.model.MaintienIndexEvtDTO;
import com.chronopost.vision.microservices.maintienindexevt.v1.model.UpdateDepassementProactifInput;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IMaintienIndexEvtDao {

    /**
     * Insertions dans la table tracesDateProactif. Cette table sert à garder
     * une trace de chaque appel fait au WS calculRetard et stocker les dates
     * retournées. <br>
     * <b>L'objet passé en paramètre <i> maintienIndexEvtDataDTO </i> doit
     * impérativement être conforme et avoir été vérifié. Notamment, il doit
     * contenir une liste d'événements non vide.</B>, il doit également contenir
     * un objet <I>ResultCalculRetard</I> non null, et dont le sous-objet
     * <I>Analyse</I> et non null également.
     * 
     * @param maintienIndexEvtDataDTO
     *            : l'objet d'échange de MaintientIndex
     * @throws ParseException
     * @throws JsonProcessingException
     * @throws Exception
     */
    void insertTracesDateProactif(final MaintienIndexEvtInput maintienIndexEvtDataDTO)
            throws JsonProcessingException, ParseException, Exception;

    /**
     * Mise à jour de la table depassementProactifParJour. Cette table permet de
     * lister les colis en dépassement proactif et est partitionnée par jour de
     * date de livraison contractuelle. Pour éviter de créer des tombstones, la
     * suppression se fait par écriture dans le champ "deleted".
     * 
     * @param updDepassementInput
     */
    void updateDepassementProactifParJour(final UpdateDepassementProactifInput updDepassementInput);

    /**
     * Génération d'un DTO pour avoir un couplage faible avec
     * MaintienIndexEvtInput.
     * 
     * @param inputData
     * @return
     */
    MaintienIndexEvtDTO createDTO(final MaintienIndexEvtInput maintienIndexEvtData);
}
