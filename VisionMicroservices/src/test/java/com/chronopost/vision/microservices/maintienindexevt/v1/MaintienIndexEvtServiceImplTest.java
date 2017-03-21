package com.chronopost.vision.microservices.maintienindexevt.v1;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.maintienindexevt.v1.model.UpdateDepassementProactifInput;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.rules.DateRules;
import com.fasterxml.jackson.core.JsonProcessingException;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

public class MaintienIndexEvtServiceImplTest {

    private final IMaintienIndexEvtDao mockDao = Mockito.mock(IMaintienIndexEvtDao.class);

    @BeforeClass
    public void setUp() {
        MaintienIndexEvtServiceImpl.getInstance().setDao(mockDao);
    }

    /**
     * Test de traitement d'un colis qui n'est pas en retard dans la base mais
     * le devient sur appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurNouveauRetardProactif() throws JsonProcessingException, Exception {
        Mockito.reset(mockDao);
        MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        Lt lt = new Lt().setNoLt("XXMIE000002FR");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("02/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");

        resultCalculRetard.setResultRetard(resultRetard);

        Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(1); // Le colis est
                                                               // en retard

        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        new UpdateDepassementProactifInput()
                .setDateLivraisonContractuelle(
                        DateRules.toTimestampDateWsCalculRetard(maintienIndexEvtData.getResultatCalculRetard()
                                .getResultRetard().getDateDeLivraisonPrevue()))
                .setDateLivraisonPrevue(
                        DateRules.toTimestampDateWsCalculRetard(MaintienIndexEvtServiceImpl.getInstance()
                                .computeDateDeLivraisonEstimee(maintienIndexEvtData)))
                .setLt(maintienIndexEvtData.getLt()).setNoLt(maintienIndexEvtData.getLt().getNoLt());

        MaintienIndexEvtServiceImpl.getInstance().maintienIndexEvt(maintienIndexEvtData);

        Mockito.verify(mockDao).insertTracesDateProactif(Mockito.any(MaintienIndexEvtInput.class));
        Mockito.verify(mockDao).updateDepassementProactifParJour(Mockito.any(UpdateDepassementProactifInput.class));
    }

    /**
     * Test de traitement d'un colis qui est pas en retard dans la base mais ne
     * l'est plus suite à l'appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurFinRetardProactif() throws JsonProcessingException, Exception {
        Mockito.reset(mockDao);
        MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        Lt lt = new Lt().setNoLt("XXMIE000002FR").setIdxDepassement("2015-12-01__1");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("01/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");

        resultCalculRetard.setResultRetard(resultRetard);

        Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(0); // Le colis est
                                                               // en retard

        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        new UpdateDepassementProactifInput()
                .setDateLivraisonContractuelle(
                        DateRules.toTimestampDateWsCalculRetard(maintienIndexEvtData.getResultatCalculRetard()
                                .getResultRetard().getDateDeLivraisonPrevue()))
                .setDateLivraisonPrevue(
                        DateRules.toTimestampDateWsCalculRetard(MaintienIndexEvtServiceImpl.getInstance()
                                .computeDateDeLivraisonEstimee(maintienIndexEvtData)))
                .setLt(maintienIndexEvtData.getLt()).setNoLt(maintienIndexEvtData.getLt().getNoLt());

        MaintienIndexEvtServiceImpl.getInstance().maintienIndexEvt(maintienIndexEvtData);

        Mockito.verify(mockDao).insertTracesDateProactif(Mockito.any(MaintienIndexEvtInput.class));
        Mockito.verify(mockDao).updateDepassementProactifParJour(Mockito.any(UpdateDepassementProactifInput.class));
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base et l'est de
     * nouveau mais sur une autre date contractuelle. On attend 2 appels à
     * updateDepassementProactifParJour (la suppression et l'insertion)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifAvecChangementDeDateContractuelle() throws JsonProcessingException,
            Exception {
        Mockito.reset(mockDao);
        MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        Lt lt = new Lt().setNoLt("XXMIE000002FR").setIdxDepassement("2015-12-02__1");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("04/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("03/12/2015 18:00");

        resultCalculRetard.setResultRetard(resultRetard);

        Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(1); // Le colis est
                                                               // en retard

        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        new UpdateDepassementProactifInput()
                .setDateLivraisonContractuelle(
                        DateRules.toTimestampDateWsCalculRetard(maintienIndexEvtData.getResultatCalculRetard()
                                .getResultRetard().getDateDeLivraisonPrevue()))
                .setDateLivraisonPrevue(
                        DateRules.toTimestampDateWsCalculRetard(MaintienIndexEvtServiceImpl.getInstance()
                                .computeDateDeLivraisonEstimee(maintienIndexEvtData)))
                .setLt(maintienIndexEvtData.getLt()).setNoLt(maintienIndexEvtData.getLt().getNoLt());

        MaintienIndexEvtServiceImpl.getInstance().maintienIndexEvt(maintienIndexEvtData);

        Mockito.verify(mockDao).insertTracesDateProactif(Mockito.any(MaintienIndexEvtInput.class));
        Mockito.verify(mockDao, Mockito.times(2)).updateDepassementProactifParJour(
                Mockito.any(UpdateDepassementProactifInput.class));
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base mais est
     * livré. On attend 1 appel à updateDepassementProactifParJour (la
     * suppression)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifAvecEvtLivraison() throws JsonProcessingException, Exception {
        Mockito.reset(mockDao);
        MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        Lt lt = new Lt().setNoLt("XXMIE000002FR").setIdxDepassement("2015-12-02__1");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimeeCalculee(false);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(false);

        resultCalculRetard.setResultRetard(resultRetard);

        Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(2); // Le colis est
                                                               // en retard

        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        new UpdateDepassementProactifInput()
                .setDateLivraisonContractuelle(
                        DateRules.toTimestampDateWsCalculRetard(maintienIndexEvtData.getResultatCalculRetard()
                                .getResultRetard().getDateDeLivraisonPrevue()))
                .setDateLivraisonPrevue(
                        DateRules.toTimestampDateWsCalculRetard(MaintienIndexEvtServiceImpl.getInstance()
                                .computeDateDeLivraisonEstimee(maintienIndexEvtData)))
                .setLt(maintienIndexEvtData.getLt()).setNoLt(maintienIndexEvtData.getLt().getNoLt());

        MaintienIndexEvtServiceImpl.getInstance().maintienIndexEvt(maintienIndexEvtData);

        Mockito.verify(mockDao).insertTracesDateProactif(Mockito.any(MaintienIndexEvtInput.class));
        Mockito.verify(mockDao, Mockito.times(1)).updateDepassementProactifParJour(
                Mockito.any(UpdateDepassementProactifInput.class));
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base mais est
     * livré. On attend 1 appel à updateDepassementProactifParJour (la
     * suppression)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifSansModif() throws JsonProcessingException, Exception {
        Mockito.reset(mockDao);
        MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        Lt lt = new Lt().setNoLt("XXMIE000002FR").setIdxDepassement("2015-12-02__1");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimeeCalculee(false);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(false);

        resultCalculRetard.setResultRetard(resultRetard);

        Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(2); // Le colis est
                                                               // en retard

        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        new UpdateDepassementProactifInput()
                .setDateLivraisonContractuelle(
                        DateRules.toTimestampDateWsCalculRetard(maintienIndexEvtData.getResultatCalculRetard()
                                .getResultRetard().getDateDeLivraisonPrevue()))
                .setDateLivraisonPrevue(
                        DateRules.toTimestampDateWsCalculRetard(MaintienIndexEvtServiceImpl.getInstance()
                                .computeDateDeLivraisonEstimee(maintienIndexEvtData)))
                .setLt(maintienIndexEvtData.getLt()).setNoLt(maintienIndexEvtData.getLt().getNoLt());

        MaintienIndexEvtServiceImpl.getInstance().maintienIndexEvt(maintienIndexEvtData);

        Mockito.verify(mockDao).insertTracesDateProactif(Mockito.any(MaintienIndexEvtInput.class));

        // On vérifie qu'on n'a pas appelé updateDepassementProactifParJour
        Mockito.verify(mockDao, Mockito.never()).updateDepassementProactifParJour(
                Mockito.any(UpdateDepassementProactifInput.class));
    }

}
