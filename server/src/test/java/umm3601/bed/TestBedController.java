package umm3601.bed;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import junit.framework.TestCase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.BedController;
import umm3601.digitalDisplayGarden.PlantController;
import umm3601.plant.PopulateMockDatabase;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Brian on 4/11/2017.
 */
public class TestBedController {
    private final static String databaseName = "data-for-testing-only";
    private static final MongoClient mongoClient = new MongoClient();
    private static final MongoDatabase testDB = mongoClient.getDatabase(databaseName);
    private BedController bedController;
    private PlantController plantController;

    @Before
    public void populateDB() throws IOException {
        PopulateMockDatabase.clearAndPopulateDBAgain(testDB);
        bedController = new BedController(testDB);
    }

    @Test
    public void TestIncrementBedData(){

        bedController.incrementBedMetadata("7.0","pageViews","second uploadId");
        MongoCollection beds = testDB.getCollection("beds");

        FindIterable doc = beds.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd302")));
        Iterator iterator = doc.iterator();
        Document result = (Document) iterator.next();

        int bedPageViews =  (int)((Document) result.get("metadata")).get("pageViews");

        assertEquals("this bed should only have 1 pageView",1, bedPageViews);
    }


    @Test
    public void TestBedVisit(){
        bedController.addBedVisit("10.0","first uploadId");
        MongoCollection beds = testDB.getCollection("beds");

        FindIterable doc = beds.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd303")));
        Iterator iterator = doc.iterator();
        Document result = (Document) iterator.next();
        //System.out.println(result);

        List<Document> bedVisits =  (List<Document>)((Document) result.get("metadata")).get("bedVisits");
        //System.out.println(bedVisits);
        assertEquals("",1 ,bedVisits.size());

        Document visits = bedVisits.get(0);
        //System.out.println(visits.get("visit"));
        String v = visits.get("visit").toString();

        //checking to see that the type of visit is an of type/structure of ObjectID
        assertEquals("they should both be of type Date ",Date.class.getName(), visits.get("visit").getClass().getName());

    }

    @Test
    public void TestQRScansAndQRVisits(){
        //first lets test qr scans
        bedController.incrementBedMetadata("7.0","qrScans","second uploadId");
        MongoCollection beds = testDB.getCollection("beds");

        FindIterable doc = beds.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd302")));
        Iterator iterator = doc.iterator();
        Document result = (Document) iterator.next();

        int qrScans =  (int)((Document) result.get("metadata")).get("qrScans");

        assertEquals("this bed should have been scanned once ",1, qrScans);


        //now we have 1 qrScan for this gardenLocation so the next method that is called should have 2 scans but
        // report only 1 scan in qrVisits Document

        // make sure that qr bed visits works
        bedController.addBedQRVisit("7.0","second uploadId");

        FindIterable doc1 = beds.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd302")));
        Iterator iterator1 = doc1.iterator();
        Document result1 = (Document) iterator1.next();

        List<Document> qrVisits =  (List<Document>)((Document) result1.get("metadata")).get("qrVisits");
        int qrScans2 =  (int) ((Document)result1.get("metadata")).get("qrScans");

        //should also increment the pageViews as it is actually visiting the page
        int pageViews =  (int) ((Document)result1.get("metadata")).get("pageViews");
        assertEquals("this should have 1 visit via qr scanning",1 ,qrVisits.size());

        Document qrvisit = qrVisits.get(0);
        Date date = new Date();

        String s = qrvisit.get("scan").toString();

        //after already having 1 qr scan the qr visit should increment qrScans
        assertEquals("there should be two qr scans now",2,qrScans2);
        assertEquals("there should be one pageView via the qr scanned ",1,pageViews);
        //checking to see that the type of visit is an of type/structure of ObjectID
        assertEquals("they should both be of type Date ", date.getClass().getName(), qrvisit.get("scan").getClass().getName());

    }

    @Test
    public void TestGetPageViews()
    {
        int pageViews = bedController.getPageViews("7.0", "second uploadId");
        assertEquals("Pageviews at gardenLocation 7 isn't 0", 0, pageViews);
        pageViews = bedController.getPageViews("20", "third uploadId");
        assertEquals("Pageviews at gardenLocation 20 isn't 10", 10, pageViews);
    }

}
