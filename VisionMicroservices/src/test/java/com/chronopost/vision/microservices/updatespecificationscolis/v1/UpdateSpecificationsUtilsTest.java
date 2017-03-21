package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CT;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_DC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_H;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_I;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_IC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SK;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_ST;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_ANNUL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RBP;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RPR;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_NIMPORTE_QUOI;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;

public class UpdateSpecificationsUtilsTest {

    private Evt evt;

    private UpdateSpecificationsUtils utils = new UpdateSpecificationsUtils();

    @BeforeMethod
    public void beforeMethod() {
        evt = new Evt();

        Map<String, String> aInfoscomp = new HashMap<>();
        aInfoscomp.put(EInfoComp.TAXE_VALEUR.getCode(), "511");
        aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);
        aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), null);
        aInfoscomp.put(EInfoComp.COMMENTAIRE60.getCode(), null);
        aInfoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), null); // anciennement INFOCOMP_CONSIGNE_CT 
        aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), null);
        aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), null);
        aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), null);

        evt.setInfoscomp(aInfoscomp);
    }

    @Test
    public void getSpecifsEvt() {

        // test de la méthode getSpecifsEvt qui renvoit la valeur
        // de spécificités à insérer en base 
        // en fonction du code événement et de différentes valeurs dans les infoscomp  

        // événement CT: renvoit CONSIGNE
        evt.setCodeEvt(EVT_CT);
        assertEquals(ESpecificiteColis.CONSIGNE, utils.extractSpecifsEvt(evt));

        // événement I: renvoit rien car infoscomps vide
        evt.setCodeEvt(EVT_I);
        assertNull(utils.extractSpecifsEvt(evt));

        // infoscomp sans les valeur attendues pour I 
        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_NIMPORTE_QUOI);
        assertNull(utils.extractSpecifsEvt(evt));

        // infoscomp ANNUL: sans effet dans le cas d'un événement I 
        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_ANNUL);
        assertNull(utils.extractSpecifsEvt(evt));

        // infocomp EVT_CF_I_RPR ou EVT_CF_I_RBP: renvoit de CONSIGNE
        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_EVT_CF_I_RPR);
        assertEquals(ESpecificiteColis.CONSIGNE, utils.extractSpecifsEvt(evt));

        // infocomp EVT_CF_I_RPR ou EVT_CF_I_RBP: renvoit de CONSIGNE
        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_EVT_CF_I_RBP);
        assertEquals(ESpecificiteColis.CONSIGNE, utils.extractSpecifsEvt(evt));

        // événement IC: renvoit une INTERACTION_CLIENT
        evt.setCodeEvt(EVT_IC);
        assertEquals(ESpecificiteColis.INTERACTION_CLIENT, utils.extractSpecifsEvt(evt));

        // evénement SC: pas de consigne sans infocomp 106
        evt.setCodeEvt(EVT_SC);
        assertNull(utils.extractSpecifsEvt(evt));

        // événement SC avec un code sac :  renvoit SENSIBLE 
        evt.getInfoscomp().put(EInfoComp.CODE_SAC.getCode(), "01234345634");
        assertEquals(ESpecificiteColis.SENSIBLE, utils.extractSpecifsEvt(evt));

        // événement ST avec un montant et sans client
        evt.setCodeEvt(EVT_ST);
        assertNotNull(utils.extractSpecifsEvt(evt));

        // événement ST sans montant numérique
        evt.getInfoscomp().put(EInfoComp.TAXE_VALEUR.getCode(), "John Doe");
        assertNull(utils.extractSpecifsEvt(evt));

        // événement ST avec un montant et sans client: spécificité TAXE
        evt.getInfoscomp().put(EInfoComp.TAXE_VALEUR.getCode(), "511");
        assertEquals(ESpecificiteColis.TAXE, utils.extractSpecifsEvt(evt));

        // événement ST avec un montant mais un client: pas de spécificité
        evt.getInfoscomp().put(EInfoComp.TAXE_NO_CONTRAT.getCode(), "John Doe");
        assertNull(utils.extractSpecifsEvt(evt));

        // événement H sans infocomp 60
        evt.setCodeEvt(EVT_H);
        assertNull(utils.extractSpecifsEvt(evt));

        // événement H avec infocomp 60 mal renseigné
        evt.getInfoscomp().put(EInfoComp.COMMENTAIRE60.getCode(), "Ceci est un commentaire");
        assertNull(utils.extractSpecifsEvt(evt));

        // événement H avec infocomp 60 renseigné avec la bonne valeur
        evt.getInfoscomp().put(EInfoComp.COMMENTAIRE60.getCode(), ESpecificiteColis.ATTRACTIF.getCode());
        assertEquals(ESpecificiteColis.ATTRACTIF, utils.extractSpecifsEvt(evt));

        // événement CL : pas de spécificité
        evt.setCodeEvt(EVT_CL);
        assertNull(utils.extractSpecifsEvt(evt));
    }

    @Test
    public void isConsignesRecues() {

        // vrai ssi événement CL et infoscomp 175 différent de null et ANNUL

        evt.setCodeEvt(EVT_CT);
        assertFalse(utils.isConsignesRecues(evt));

        evt.setCodeEvt(EVT_I);
        assertFalse(utils.isConsignesRecues(evt));

        evt.setCodeEvt(EVT_SC);
        assertFalse(utils.isConsignesRecues(evt));

        evt.setCodeEvt(EVT_ST);
        assertFalse(utils.isConsignesRecues(evt));

        evt.setCodeEvt(EVT_H);
        assertFalse(utils.isConsignesRecues(evt));

        evt.setCodeEvt(EVT_CL);
        assertFalse(utils.isConsignesRecues(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_NIMPORTE_QUOI);
        assertTrue(utils.isConsignesRecues(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_ANNUL);
        assertFalse(utils.isConsignesRecues(evt));

    }

    @Test
    public void isConsignesAnnulees() {

        // vrai ssi événement CL et infoscomp 175 égal à ANNUL

        evt.setCodeEvt(EVT_CT);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.setCodeEvt(EVT_I);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.setCodeEvt(EVT_SC);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.setCodeEvt(EVT_ST);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.setCodeEvt(EVT_H);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.setCodeEvt(EVT_CL);
        assertFalse(utils.isConsignesAnnulees(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_ANNUL);
        assertTrue(utils.isConsignesAnnulees(evt));

    }

    @Test
    public void isConsignesTraiteesCT() {

        // vrai ssi événement CT et infocomp 185 renseigné

        evt.setCodeEvt(EVT_CT);
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.getInfoscomp().put(EInfoComp.ACTION_CONTENANT.getCode(), "Déposer l'éléphant sous le paillasson");
        assertTrue(utils.isConsignesTraiteesCT(evt));

        evt.setCodeEvt(EVT_I);
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), "Déposer l'éléphant sous le paillasson");
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.setCodeEvt(EVT_SC);
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.setCodeEvt(EVT_ST);
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.setCodeEvt(EVT_H);
        assertFalse(utils.isConsignesTraiteesCT(evt));

        evt.setCodeEvt(EVT_CL);
        assertFalse(utils.isConsignesTraiteesCT(evt));

    }

    @Test
    public void isConsignesTraiteesI() {

        // vrai ssi événement I et infocomp 56 renseigné

        evt.setCodeEvt(EVT_CT);
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.getInfoscomp().put(EInfoComp.ACTION_CONTENANT.getCode(), "Déposer l'éléphant sous le paillasson");
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.setCodeEvt(EVT_I);
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), "Déposer l'éléphant sous le paillasson");
        assertTrue(utils.isConsignesTraiteesI(evt));

        evt.setCodeEvt(EVT_SC);
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.setCodeEvt(EVT_ST);
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.setCodeEvt(EVT_H);
        assertFalse(utils.isConsignesTraiteesI(evt));

        evt.setCodeEvt(EVT_CL);
        assertFalse(utils.isConsignesTraiteesI(evt));

    }

    @Test
    public void getConsigne() {

        
        evt.setCodeEvt(EVT_CT);
        assertNull(utils.extractConsigne(evt));

        evt.getInfoscomp().put(EInfoComp.ACTION_CONTENANT.getCode(), "Déposer l'éléphant sous le paillasson");
        assertEquals("Déposer l'éléphant sous le paillasson", utils.extractConsigne(evt));

        evt.setCodeEvt(EVT_I);
        assertNull(utils.extractConsigne(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_I.getCode(), "Appuyez sur la sonnette en faisant le poirier");
        assertEquals("Appuyez sur la sonnette en faisant le poirier", utils.extractConsigne(evt));

        evt.setCodeEvt(EVT_SC);
        assertNull(utils.extractConsigne(evt));

        evt.setCodeEvt(EVT_ST);
        assertNull(utils.extractConsigne(evt));

        evt.setCodeEvt(EVT_H);
        assertNull(utils.extractConsigne(evt));

        evt.setCodeEvt(EVT_CL);
        assertNull(utils.extractConsigne(evt));

        evt.getInfoscomp().put(EInfoComp.CONSIGNE_EVT_CL.getCode(), "A chanter au destinataire");
        assertEquals("A chanter au destinataire", utils.extractConsigne(evt));
    }
    
    @Test
    public void getIdConsigne() {
        evt.getInfoscomp().put(EInfoComp.ID_CONSIGNE.getCode(), VALUE_NIMPORTE_QUOI);
        assertEquals(VALUE_NIMPORTE_QUOI, utils.extractIdConsigne(evt));
    }
    
     /**
     *  
     */
    @Test
    public void testInfoSupp()  {
        
//        Test des méthodes
//        isInfoSuppTaxeValeur
//        isInfoSuppNoLtRetour
//        getParamInfoSupp
//        getValeurInfoSupp

        // initialisation avec des valeurs sans incidence
        evt.setCodeEvt(EVT_SK);
        evt.getInfoscomp().put(EInfoComp.SWAP_NO_LT_RETOUR.getCode(), null);
        evt.getInfoscomp().put(EInfoComp.TAXE_VALEUR.getCode(), null);
        evt.getInfoscomp().put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);

        // il ne se passe rien
        assertNull(utils.extractInfoSuppTaxeValeur(evt)) ;
        assertNull(utils.extractInfoSuppNoLtRetour(evt)) ;

        // l'événement est un DC
        evt.setCodeEvt(EVT_DC);
        
        // mais la valeur infocomp 70 est vide
        assertNull(utils.extractInfoSuppTaxeValeur(evt)) ;
        assertNull(utils.extractInfoSuppNoLtRetour(evt)) ;
        assertTrue(utils.extractInfoSupp(evt).size() == 0)     ;

        // on remplit infocomp 70 et infocomp 111
        evt.getInfoscomp().put(EInfoComp.SWAP_NO_LT_RETOUR.getCode(), "nolt0001");
        evt.getInfoscomp().put(EInfoComp.TAXE_VALEUR.getCode(), "511");
        
        // On a un événement DC avec une infocomp 70. On a donc une info supplémentaire NO_LT_RETOUR. L'infocomp 111 n'a aucune incidence 
        assertNull(utils.extractInfoSuppTaxeValeur(evt)) ;
        assertNotNull(utils.extractInfoSuppNoLtRetour(evt)) ;
        Map<String, String> res = utils.extractInfoSupp(evt);
        assertNotNull(res);
        assertTrue(res.containsKey(EInfoSupplementaire.NO_LT_RETOUR.getCode()));

        // L'événement est un ST
        evt.setCodeEvt(EVT_ST);

        // On a un événement ST avec une infocomp 195 et pas d'infocomp 113. On a donc une info supplémentaire TAXE_VALEUR. L'infocomp 70 n'a aucune incidence 
        assertNotNull(utils.extractInfoSuppTaxeValeur(evt)) ;
        assertNull(utils.extractInfoSuppNoLtRetour(evt)) ;
        res = utils.extractInfoSupp(evt);
        assertTrue(res.containsKey(EInfoSupplementaire.TAXE_VALEUR.getCode()))   ;
        assertTrue("511".equals(res.get(EInfoSupplementaire.TAXE_VALEUR.getCode())));

        // On remplit infocomp 113
        evt.getInfoscomp().put(EInfoComp.TAXE_NO_CONTRAT.getCode(), VALUE_NIMPORTE_QUOI);
        
        // On a un événement ST avec une infocomp TAXE_NO_CONTRAT, mais l'infocomp 113 est renseignée. Cela annule donc l'infosupp TAXE_VALEUR
        assertNull(utils.extractInfoSuppTaxeValeur(evt)) ;
        assertNull(utils.extractInfoSuppNoLtRetour(evt)) ;
        res = utils.extractInfoSupp(evt);
        assertFalse(res.containsKey(EInfoSupplementaire.TAXE_VALEUR))   ;

    }
}
