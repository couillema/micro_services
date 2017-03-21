package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession **/
public class TourneeDaoImplTest {
    private boolean suiteLaunch = true;
	private PreparedStatement psCleanInfoTournee;
	private PreparedStatement psCleanTournees;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        getSession().execute(
                        "INSERT INTO tournees(date_jour,code_tournee,collecte,distri,eta_ko,eta_ok,informations,ta) VALUES ('2015-07-25','AIX13M03',null,{'ME203546776EE','PM679108780JB','PM679108793JB','PM679108802JB'},null,null,{'debut':'2015-07-30 07:00:00','fin':'2015-07-30 15:00:00'},{'15501436461627U','155017871636874','15976686789859T','6M05162786021','6M05162821340','6M10657302242','6W00299606150','6W00518389871','6W00579739820','6W99615000697','6Y00159635276','6Y00192225380','6Y00223491333','AA628035880JB','AA628039626JB','AA628044762JB','AA628044833JB','BF962216678FR','BH592099678FR','BP078408398JB','BU691700995FR','DF201101239FR','DF201109582FR','DF965008277FR','DF965009140FR','DP997455072FR','EE077167863FR','EJ381421622JP','FS203557310FR','FZ033216058FR','GH333067754JB','GH691945300FR','GH984570247FR','HJ205954702FR','HZ596744029FR','HZ596747691FR','HZ818268823FR','HZ829696862FR','ME203546776EE','MH205214555FR','MR012491562FR','NC279967270JB','NC945251105FR','NC945251136FR','ND206220875FR','NE103586605FR','PJ598943362FR','PM679108780JB','PM679108793JB','PM679108802JB','PM912511403FR','SG164355684FR','SK994736944FR','ST699191389FR','TZ125575357FR','UN316384289FR','XV900267860FR','XW301578685FR','XW301841773FR','XY109391010FR','XY638901867EE','XY685237718JB'})");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:00:00','5aeb03c2-ae26-4e1a-87bd-58a8c456ba9f','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9136066','longitude':'8.72514666','numero_dernier_point':'010'});");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:05:00','78eb85e5-a440-48e4-a96f-59934eb5104f','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9135133','longitude':'8.725385','numero_dernier_point':'008'});");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:10:00','9184e039-1c4b-42e3-835a-f7f23b4045d3','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9140916','longitude':'8.72627833','numero_dernier_point':'008'});");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:15:00','1bb3d8a4-8a1e-4ef8-8858-d6389a8c4d53','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9133133','longitude':'8.72738833','numero_dernier_point':'007'});");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:20:00','707d279a-62a7-4b94-a15c-b63566391746','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9175116','longitude':'8.72963166','numero_dernier_point':'034'});");
        getSession().execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AIX13M03','position','2015-07-25 12:25:00','bd0526de-8e59-4371-a7b0-53f5161587f1','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9151966','longitude':'8.72874333','numero_dernier_point':'005'});");

		psCleanInfoTournee = getSession().prepare("DELETE FROM info_tournee where code_tournee = ?");
        psCleanTournees = getSession().prepare("DELETE FROM tournees where date_jour = ? and code_tournee = ?");
    }
    
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    @Test(groups = "slow")
    public void getTournee() throws ParseException {
        Tournee tournee = GetDetailTourneeDaoImpl.getInstance().getTournee("AIX13M03",
                new DateTime(2015, 7, 25, 0, 0, 0, DateTimeZone.forID("+01:00")).toDate());
        assertNotNull(tournee);
        assertTrue(tournee.getLtsDeLaTournee().contains("PM679108780JB"));
        assertTrue(tournee.getLtsDeLaTournee().contains("15501436461627U"));
        assertEquals(tournee.getRelevesGps().size(), 6);
        assertTrue(tournee.getInformations().containsKey("debut"));
        assertTrue(tournee.getInformations().containsKey("fin"));

        assertEquals(tournee.getInformations().get("debut"), "2015-07-30 07:00:00");
        assertEquals(tournee.getInformations().get("fin"), "2015-07-30 15:00:00");
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanInfoTournee.bind("AIX13M03"));
		getSession().execute(psCleanTournees.bind("2015-07-25", "AIX13M03"));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
