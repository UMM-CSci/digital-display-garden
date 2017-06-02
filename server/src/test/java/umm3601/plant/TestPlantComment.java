package umm3601.plant;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.PlantController;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.*;

public class TestPlantComment {

    private final static String databaseName = "data-for-testing-only";
    public MongoClient mongoClient = new MongoClient();
    public MongoDatabase testDB = mongoClient.getDatabase(databaseName);
    private PlantController plantController;

    @Before
    public void populateDB() throws IOException {
        PopulateMockDatabase.clearAndPopulateDBAgain(testDB);
        plantController = new PlantController(testDB);
    }

    @Test
    public void successfulInputOfComment() throws IOException {
        String json = "{ plantId: \"16040.0\", gardenLocation:\"7.0\", comment : \"Here is our comment for this test\" }";

        assertTrue(plantController.storePlantComment(json, "second uploadId"));

        MongoCollection<Document> commentDocuments = testDB.getCollection("comments");

        long contains = commentDocuments.count();
        assertEquals(1, contains);

        Iterator<Document> iter = commentDocuments.find().iterator();

        Document fromDb = iter.next();

        assertEquals("Here is our comment for this test", fromDb.getString("comment"));
        assertEquals("16040.0", fromDb.get("commentOnPlant"));
        assertEquals("7.0", fromDb.get("commentInBed"));
        assertEquals("second uploadId", fromDb.get("uploadId"));
    }

    @Test
    public void failedInputOfComment() throws IOException {
        MongoCollection<Document> commentDocuments = testDB.getCollection("comments");

        String json = "{ plantId: \"58d1c36efb0cac4e15afd27\", gardenLocation:\"7.0\", comment : \"Here is our comment for this test\" }";
        assertFalse("Added a comment for a plant that doesn't exist", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Added a comment for a plant that doesn't exist", 0, commentDocuments.count());

        json = "{ plantId: \"16040.0\", gardenLocation:\"7.0\", comment : \"Here is our comment for this test\" }";
        assertFalse("Added a comment for an invalid uploadId", plantController.storePlantComment(json, "invalid uploadId"));
        assertEquals("Added a comment for a plant that doesn't exist", 0, commentDocuments.count());

        json = "{ -2i gnlr b[  34rr=634=ub1]<m.";
        assertFalse("Somehow added a malformed comment", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Somehow added a malformed comment", 0, commentDocuments.count());

        json = "{ comment : \"Here is our comment for this test\" }";
        assertFalse( "Added a comment without corresponding plant", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Added a comment without corresponding plant", 0, commentDocuments.count());

        json = "{ plantId: \"16040.0\" }";
        assertFalse( "Added a comment without an actual comment", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Added a comment without an actual comment", 0, commentDocuments.count());


        json = "{ plantId: \"16040.0\", gardenLocation:\"7.0\" }";
        assertFalse( "Added a comment without an actual comment", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Added a comment without an actual comment", 0, commentDocuments.count());

        json = "";
        assertFalse( "Added a comment with empty json", plantController.storePlantComment(json, "second uploadId"));
        assertEquals("Added a comment with empty json", 0, commentDocuments.count());
    }
}
