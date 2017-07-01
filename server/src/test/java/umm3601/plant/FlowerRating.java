package umm3601.plant;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.PlantController;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class FlowerRating {

    private final static String databaseName = "data-for-testing-only";
    private PlantController plantController;
    public MongoClient mongoClient = new MongoClient();
    public MongoDatabase testDB = mongoClient.getDatabase(databaseName);

    @Before
    public void populateDB() throws IOException {
        PopulateMockDatabase.clearAndPopulateDBAgain(testDB);
        plantController = new PlantController(testDB);
    }

    @Test
    public void AddFlowerRatingReturnsTrueWithValidInput() throws IOException{

        assertTrue(plantController.addFlowerRating("16001.0", "10.0", true, "first uploadId"));
        MongoCollection plants = testDB.getCollection("plants");

        FindIterable doc = plants.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd202")));
        Iterator iterator = doc.iterator();
        Document result = (Document) iterator.next();

        List<Document> ratings = (List<Document>) ((Document) result.get("metadata")).get("ratings");
        assertEquals(1, ratings.size());

        Document rating = ratings.get(0);
        //assertTrue(rating.getBoolean("like"));
        //assertEquals(new ObjectId("58d1c36efb0cac4e15afd202"),rating.get("ratingOnObjectOfId"));
    }

    @Test
    public void AddFlowerRatingReturnsFalseWithInvalidInput() throws IOException {

        assertFalse("Added plant rating with malformed plantId", plantController.addFlowerRating("jfd;laj;asjfoisaf", "13.0", true, "first uploadId"));
        assertFalse("Added plant rating with nonexistent plantId", plantController.addFlowerRating("16005.0", "5.0", true, "first uploadId"));
        assertFalse("Added plant rating with invalid uploadId", plantController.addFlowerRating("16040.0", "7.0", true, "invalid uploadId"));
        assertFalse("Added plant rating with invalid gardenLocation", plantController.addFlowerRating("16040.0", "8.0", true, "second uploadId"));
    }

    @Test
    public void AddFlowerRatingReturnsFalseWithInvalidUploadID() throws IOException {

        assertFalse(plantController.addFlowerRating("16001.0", "10.0", true, "anything"));
    }


    @Test
    public void AddFlowerRatingReturnsTrueWithValidJsonInput() throws IOException{

        String json = "{like: true, id: \"16001.0\", gardenLocation: \"10.0\"}";

        assertTrue(plantController.addFlowerRating(json, "first uploadId"));

        MongoCollection plants = testDB.getCollection("plants");

        FindIterable doc = plants.find(new Document().append("_id", new ObjectId("58d1c36efb0cac4e15afd202")));
        Iterator iterator = doc.iterator();
        Document result = (Document) iterator.next();

        List<Document> ratings = (List<Document>) ((Document) result.get("metadata")).get("ratings");
        assertEquals(1, ratings.size());

        Document rating = ratings.get(0);
        assertTrue(rating.getBoolean("like"));
        assertEquals(new ObjectId("58d1c36efb0cac4e15afd202"),rating.get("ratingOnObjectOfId"));
    }

    @Test
    public void AddFlowerRatingReturnsFalseWithInvalidJsonInput() throws IOException {

        String json = "{like: true, id: \"dkjahfjafhlkasjdf\", gardenLocation: \"10.0\"}";
        assertFalse(plantController.addFlowerRating(json, "first uploadId"));

        json = "{like: true id: \"58d1c36efb0cac4e15afd201\", gardenLocation: \"10.0\"}";
        assertFalse(plantController.addFlowerRating(json, "first uploadId"));

        json = "{ 02bv/,/33=-0 rasv< }";
        assertFalse("Added flower rating using malformed JSON", plantController.addFlowerRating(json, "first uploadId"));

        json = "";
        assertFalse("Added flower rating using malformed JSON", plantController.addFlowerRating(json, "first uploadId"));

        json = "{id: \"58d1c36efb0cac4e15afd202\", gardenLocation: \"10.0\"}";
        assertFalse("Added flower rating without like=bool", plantController.addFlowerRating(json, "first uploadId"));

        json = "{like: true, gardenLocation: \"10.0\"}";
        assertFalse("Added flower rating without id", plantController.addFlowerRating(json, "first uploadId"));

        json = "{like: true, id: \"16001.0\", gardenLocation: \"10.0\"}";
        assertFalse("Added flower rating with invalid uploadId", plantController.addFlowerRating(json, "invalid uploadId"));


    }
}
