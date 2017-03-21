package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;

public class TourneeTest {

    private Tournee tournee;

    @BeforeClass
    public void setUpBeforeClass() {
        tournee = new Tournee();
        tournee.setDateTournee(new DateTime(2015, 1, 1, 10, 0, 0).toDate());
        tournee.setIdC11("idC11");
        tournee.setCodeTournee("codeTournee");
        tournee.setLtsDeLaTournee(Arrays.asList("EE000000001FR", "EE000000002FR", "EE000000003FR"));
        PositionGps position1 = new PositionGps().setCoordonnees(new Position().setLati(4).setLongi(2))
                .setDateRelevePosition(new Date());
        PositionGps position2 = new PositionGps().setCoordonnees(new Position().setLati(5).setLongi(3))
                .setDateRelevePosition(new Date());
        tournee.setRelevesGps(Arrays.asList(position1, position2));
    }

    @Test
    public void getDateTournee() {
        assertEquals(tournee.getDateTournee(), new DateTime(2015, 1, 1, 10, 0, 0).toDate());
    }

    @Test
    public void getCodeTournee() {
        assertEquals(tournee.getCodeTournee(), "codeTournee");
    }

    @Test
    public void getIdC11() {
        assertEquals(tournee.getIdC11(), "idC11");
    }

    @Test
    public void getLtsDeLaTournee() {
        List<String> lts = tournee.getLtsDeLaTournee();
        assertEquals(lts.size(), 3);
    }

    @Test
    public void getRelevesGps() {
        assertEquals(tournee.getRelevesGps().size(), 2);
        assertEquals(tournee.getRelevesGps().get(0).getCoordonnees().getLati(), (double) 4);
        assertEquals(tournee.getRelevesGps().get(1).getCoordonnees().getLati(), (double) 5);
        assertEquals(tournee.getRelevesGps().get(0).getCoordonnees().getLongi(), (double) 2);
        assertEquals(tournee.getRelevesGps().get(1).getCoordonnees().getLongi(), (double) 3);
    }
}
