package com.chronopost.vision.microservices.suivibox;

import static org.testng.AssertJUnit.* ;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.suivibox.Utils;
import com.chronopost.vision.model.Evt;

public class UtilsTest {

    private Evt evt1 ;
    private Evt evt2 ;
    private Evt evt3 ;
    private Evt evt4 ;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {

        evt1 = new Evt().setCodeEvt("PS").setProdNoLt(12) ;
        evt2 = new Evt().setCodeEvt("GC").setProdNoLt(12) ;
        evt3 = new Evt().setCodeEvt("PS").setProdNoLt(20) ;
        evt4 = new Evt().setCodeEvt("GC").setProdNoLt(20) ;

    }

    @Test
    public void isEvenementGC() {
        assertFalse(Utils.isEvenementGC(evt1)) ;
        assertFalse(Utils.isEvenementGC(evt2)) ;
        assertFalse(Utils.isEvenementGC(evt3)) ;
        assertTrue(Utils.isEvenementGC(evt4)) ;
    }
}
