package umm3601.plant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import umm3601.digitalDisplayGarden.PlantController;

import javax.print.Doc;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class PopulateMockDatabase {



    public static void clearAndPopulateDBAgain(MongoDatabase testDB) throws IOException {
        testDB.drop();
        MongoCollection plants = testDB.getCollection("plants");
        MongoCollection beds = testDB.getCollection("beds");
        MongoCollection config = testDB.getCollection("config");
        config.insertOne(new Document().append("liveUploadId", "first uploadId"));

        addFirstUploadId(plants,beds,config);
        addSecondUploadId(plants,beds,config);
        addThirdUploadId(plants,beds,config);
        addGoogleChartsUploadId(plants,beds,config);

    }

    public static void addFirstUploadId(MongoCollection plants, MongoCollection beds, MongoCollection config)
    {
        {
            Document bed10 = new Document();
            bed10.append("_id", new ObjectId("58d1c36efb0cac4e15afd303"));
            bed10.append("gardenLocation", "10.0");


            Document bed10Meta = new Document();
            bed10Meta.append("pageViews", 0);
            bed10Meta.append("qrScans", 0);
            bed10Meta.append("bedVisits", new BsonArray());
            bed10Meta.append("qrVisits", new BsonArray());
            bed10Meta.append("date", new Date());

            bed10.append("uploadId", "first uploadId");
            bed10.append("metadata", bed10Meta);
            beds.insertOne(bed10);
        }



        //First Plant Alternanthera
        Document alternanthera = new Document();
        alternanthera.append("_id", new ObjectId("58d1c36efb0cac4e15afd202"));
        alternanthera.append("uploadId", "first uploadId");

        alternanthera.append("commonName", "Alternanthera");
        alternanthera.append("cultivar", "Experimental");
        alternanthera.append("gardenLocation", "10.0");
        alternanthera.append("Comments", "Name change from Purple Prince   14x18 spreader");
        alternanthera.append("HBHangBasketCContainerWWall", "");
        alternanthera.append("id", "16001.0");
        alternanthera.append("source", "PA");
        alternanthera.append("SSeedVVeg", "S");


        Document metadataDoc = new Document();
        metadataDoc.append("pageViews", 0);
        metadataDoc.append("ratings", new BsonArray());
        metadataDoc.append("visits", new BsonArray());


        alternanthera.append("metadata", metadataDoc);
        //alternanthera.append("garden", "hello!");
        plants.insertOne(alternanthera );

        //Second Plant Begonia
        Document begonia = new Document();
        begonia.append("_id", new ObjectId("58d1c36efb0cac4e15afd203"));
        begonia.append("uploadId", "first uploadId");

        begonia.append("commonName", "Begonia");
        begonia.append("cultivar", "Megawatt Rose Green Leaf");
        begonia.append("gardenLocation", "10.0");
        begonia.append("Comments", "Grow in same sun or shade area; grow close proximity to each other for comparison");
        begonia.append("HBHangBasketCContainerWWall", "");
        begonia.append("id", "16008.0");
        begonia.append("source", "PA");
        begonia.append("SSeedVVeg", "S");

        Document metadataDoc1 = new Document();
        metadataDoc1.append("pageViews", 0);
        metadataDoc1.append("ratings", new BsonArray());


        begonia.append("metadata", metadataDoc1);
        plants.insertOne(begonia);





    }

    public static void addSecondUploadId(MongoCollection plants, MongoCollection beds, MongoCollection config)
    {

        {
            //First Garden Location bed7.0
            Document bed7 = new Document();
            bed7.append("_id", new ObjectId("58d1c36efb0cac4e15afd302"));
            bed7.append("gardenLocation", "7.0");

            Document bed7Meta = new Document();
            bed7Meta.append("pageViews", 0);
            bed7Meta.append("qrScans", 0);
            bed7Meta.append("bedVisits", new BsonArray());
            bed7Meta.append("qrVisits", new BsonArray());
            bed7Meta.append("date", new Date());

            bed7.append("uploadId", "second uploadId");
            bed7.append("metadata", bed7Meta);
            beds.insertOne(bed7);

        }
        {
            Document bed12 = new Document();
            bed12.append("_id", new ObjectId("58d1c36efb0cac4e15afd304"));
            bed12.append("gardenLocation", "12");

            Document bed12Meta = new Document();
            bed12Meta.append("pageViews", 10);
            bed12Meta.append("qrScans", 0);
            bed12Meta.append("bedVisits", new BsonArray()); //TODO should have 10 {visit : objectId}'s in here but for the test I'm writing rn don't need
            bed12Meta.append("qrVisits", new BsonArray());

            bed12.append("uploadId", "second uploadId");
            bed12.append("metadata", bed12Meta);
            beds.insertOne(bed12);
        }

        //Third Plant Dianthus
        Document dianthus = new Document();
        dianthus.append("_id", new ObjectId("58d1c36efb0cac4e15afd204"));
        dianthus.append("uploadId", "second uploadId");

        dianthus.append("commonName", "Dianthus");
        dianthus.append("cultivar", "Jolt™ Pink F1");
        dianthus.append("gardenLocation", "7.0");
        dianthus.append("Comments", "");
        dianthus.append("HBHangBasketCContainerWWall", "");
        dianthus.append("id", "16040.0");
        dianthus.append("source", "AAS");
        dianthus.append("SSeedVVeg", "S");

        Document metadataDoc2 = new Document();
        metadataDoc2.append("pageViews", 0);
        metadataDoc2.append("ratings", new BsonArray());

        dianthus.append("metadata", metadataDoc2);
        plants.insertOne(dianthus);

        //Fourth Plant PlantFour
        Document plantFour = new Document();
        plantFour.append("_id", new ObjectId("58d1c36efb0cac4e15afd278"));
        plantFour.append("uploadId", "second uploadId");

        plantFour.append("commonName", "PlantFour");
        plantFour.append("cultivar", "Some Plant");
        plantFour.append("gardenLocation", "12");
        plantFour.append("id", "16041.0");

        Document metadataDoc3 = new Document();
        metadataDoc3.append("pageViews", 0);
        metadataDoc3.append("ratings", new BsonArray());

        plantFour.append("metadata", metadataDoc3);
        plants.insertOne(plantFour);
    }

    public static void addThirdUploadId(MongoCollection plants, MongoCollection beds, MongoCollection config)
    {
        {
            Document bed20 = new Document();
            bed20.append("_id", new ObjectId("58d1c36efb0cac4e15afd305"));
            bed20.append("gardenLocation", "20");

            Document bed20Meta = new Document();
            bed20Meta.append("pageViews", 10);
            bed20Meta.append("qrScans", 0);
            bed20Meta.append("bedVisits", new BsonArray()); //TODO should have 10 {visit : objectId}'s in here but for the test I'm writing rn don't need
            bed20Meta.append("qrVisits", new BsonArray());
            bed20Meta.append("date", new Date());

            bed20.append("uploadId", "third uploadId");
            bed20.append("metadata", bed20Meta);
            beds.insertOne(bed20);

        }

        //for testing getCommonNamesJSON (third uploadID)
        //Fifth Plant PlantFive
        Document plantFive = new Document();
        plantFive.append("_id", new ObjectId("58d1c36efb0cac4e15afd280"));
        plantFive.append("uploadId", "third uploadId");

        plantFive.append("commonName", "Tulip");
        plantFive.append("cultivar", "Red");
        plantFive.append("gardenLocation", "20");
        plantFive.append("id", "16080.0");

        Document metadataDoc4 = new Document();
        metadataDoc4.append("pageViews", 0);
        metadataDoc4.append("ratings", new BsonArray());

        plantFive.append("metadata", metadataDoc4);
        plants.insertOne(plantFive);

        //Sixth Plant PlantSix
        Document plantSix = new Document();
        plantSix.append("_id", new ObjectId("58d1c36efb0cac4e15afd281"));
        plantSix.append("uploadId", "third uploadId");

        plantSix.append("commonName", "Tulip");
        plantSix.append("cultivar", "Pink");
        plantSix.append("gardenLocation", "20");
        plantSix.append("id", "16081.0");

        Document metadataDoc5 = new Document();
        metadataDoc5.append("pageViews", 0);
        metadataDoc5.append("ratings", new BsonArray());

        plantSix.append("metadata", metadataDoc5);
        plants.insertOne(plantSix);

        //Seventh Plant PlantSeven
        Document plantSeven = new Document();
        plantSeven.append("_id", new ObjectId("58d1c36efb0cac4e15afd282"));
        plantSeven.append("uploadId", "third uploadId");

        plantSeven.append("commonName", "Rose");
        plantSeven.append("cultivar", "Yellow");
        plantSeven.append("gardenLocation", "20");
        plantSeven.append("id", "16082.0");

        Document metadataDoc6 = new Document();
        metadataDoc6.append("pageViews", 0);
        metadataDoc6.append("ratings", new BsonArray());

        plantSeven.append("metadata", metadataDoc6);
        plants.insertOne(plantSeven);
    }

    public static void addGoogleChartsUploadId(MongoCollection plants, MongoCollection beds, MongoCollection config)
    {

        {
            Document bed5 = new Document();
            bed5.append("_id", new ObjectId("58d1c36efb0cac4e15afd308"));
            bed5.append("gardenLocation", "5.0");

            Document bed5Meta = new Document();
            bed5Meta.append("pageViews", 10);
            bed5Meta.append("qrScans", 0);
            bed5Meta.append("bedVisits", new BsonArray()); //TODO should have 10 {visit : objectId}'s in here but for the test I'm writing rn don't need
            bed5Meta.append("qrVisits", new BsonArray());
            bed5Meta.append("date", new Date());

            bed5.append("uploadId", "googleCharts uploadId");
            bed5.append("metadata", bed5Meta);
            beds.insertOne(bed5);
        }

        {
            Document bed2S = new Document();
            bed2S.append("_id", new ObjectId("58d1c36efb0cac4e15afd309"));
            bed2S.append("gardenLocation", "2S");

            Document bed2SMeta = new Document();
            bed2SMeta.append("pageViews", 10);
            bed2SMeta.append("qrScans", 0);
            bed2SMeta.append("bedVisits", new BsonArray()); //TODO should have 10 {visit : objectId}'s in here but for the test I'm writing rn don't need
            bed2SMeta.append("qrVisits", new BsonArray());

            bed2S.append("uploadId", "googleCharts uploadId");
            bed2S.append("metadata", bed2SMeta);
            beds.insertOne(bed2S);
        }

        /*
        Plants that will include mock pageViews and visits
        */

        //EighthPlant PlantEight
        Document plantEight = new Document();
        plantEight.append("_id", new ObjectId("58d1c36efb0cac4e15afd101"));
        plantEight.append("uploadId", "googleCharts uploadId");

        plantEight.append("commonName", "Marigold");
        plantEight.append("cultivar", "Fireball");
        plantEight.append("gardenLocation", "5.0");
        plantEight.append("id", "16053.0");

        Document metadataDoc8 = new Document();
        metadataDoc8.append("pageViews", 5);

        BsonArray ratings = new BsonArray();
        {
            BsonDocument rateDoc1 = new BsonDocument();
            rateDoc1.append("like", new BsonBoolean(false));
            rateDoc1.append("ratingOnObjectId", new BsonObjectId(new ObjectId("58f03f9aad21334329e73865")));
            ratings.add(rateDoc1);
        }
        {
            BsonDocument rateDoc2 = new BsonDocument();
            rateDoc2.append("like", new BsonBoolean(true));
            rateDoc2.append("ratingOnObjectId", new BsonObjectId(new ObjectId("58f03f9aad21334329e73866")));
            ratings.add(rateDoc2);
        }
        metadataDoc8.append("ratings", ratings);


        BsonArray bed8Visits = new BsonArray();
        BsonDocument docV8 = new BsonDocument();
        docV8.append("visit", new BsonDateTime(Date.from(Instant.parse("2023-01-05T18:18:02Z")).getTime()));
        bed8Visits.add(docV8);
        metadataDoc8.append("visits", bed8Visits);

        plantEight.append("metadata", metadataDoc8);
        plants.insertOne(plantEight);

        //Ninth plant
        Document plantNine = new Document();
        plantNine.append("_id", new ObjectId("58d1c36efb0cac4e15afd102"));
        plantNine.append("uploadId", "googleCharts uploadId");

        plantNine.append("commonName", "Coreopsis");
        plantNine.append("cultivar", "Sun Kiss");
        plantNine.append("gardenLocation", "2S");
        plantNine.append("id", "16037.0");

        Document metadataDoc9 = new Document();
        metadataDoc9.append("pageViews", 10);

        BsonArray b = new BsonArray();
        BsonDocument doc = new BsonDocument();
        doc.append("like",new BsonBoolean(false));
        doc.append("ratingOnObjectId",new BsonObjectId(new ObjectId("58f03f9aad21334329e73859")));
        doc.append("date", new BsonDateTime(new Date().getTime()));
        b.add(doc);
        metadataDoc9.append("ratings", b);

        BsonDocument doc1 = new BsonDocument();
        doc1.append("like",new BsonBoolean(true));
        doc1.append("ratingOnObjectId",new BsonObjectId(new ObjectId("58f03f9aad21334329e73860")));
        doc1.append("date", new BsonDateTime(new Date().getTime()));
        b.add(doc1);
        metadataDoc9.append("ratings", b);

        BsonDocument doc2 = new BsonDocument();
        doc2.append("like",new BsonBoolean(true));
        doc2.append("ratingOnObjectId",new BsonObjectId(new ObjectId("58f03f9aad21334329e73861")));
        doc2.append("date", new BsonDateTime(new Date().getTime()));
        b.add(doc2);
        metadataDoc9.append("ratings", b);


        BsonArray mockVisits = new BsonArray();
        BsonDocument docV1 = new BsonDocument();

        docV1.append("visit", new BsonDateTime(Date.from(Instant.parse("2023-02-20T05:34:59Z")).getTime()));
        mockVisits.add(docV1);
        metadataDoc9.append("visits", mockVisits);

        BsonDocument docV2 = new BsonDocument();
        docV2.append("visit", new BsonDateTime(Date.from(Instant.parse("2023-02-22T05:34:02Z")).getTime()));
        mockVisits.add(docV2);
        metadataDoc9.append("visits", mockVisits);

        BsonDocument docV3 = new BsonDocument();
        docV8.append("visit", new BsonDateTime(Date.from(Instant.parse("2023-06-30T14:47:03Z")).getTime()));
        mockVisits.add(docV3);

        metadataDoc9.append("visits", mockVisits);

        plantNine.append("metadata", metadataDoc9);
        plants.insertOne(plantNine);
    }


}




