package umm3601.digitalDisplayGarden;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by frazi177 on 4/21/17.
 */
public class TestAccessionPatching
{

    private final static String databaseName = "data-for-testing-only";

    public MongoClient mongoClient = new MongoClient();
    public MongoDatabase testDB;
    public ExcelParser parser;

    @Before
    public void clearAndPopulateDatabase() throws IOException {
        mongoClient.dropDatabase(databaseName);
        testDB = mongoClient.getDatabase(databaseName);
        InputStream fromFile = this.getClass().getResourceAsStream("/Test_Accession2016.xlsx");
        parser = new ExcelParser(fromFile, testDB);

    }

    @Test
    public void testPatchDatabase() throws FileNotFoundException {
        MongoCollection plants = testDB.getCollection("plants");

        //Clear the db-blah-test database BEFORE the test
        //So that you view only the most recent test results
        ExcelParser.clearUpload("an arbitrary ID", testDB);
        ExcelParser.clearUpload("a totally arbitrary ID", testDB);
        ExcelParser.clearUpload("an even more arbitrary ID", testDB);

        String[][] plantArray;

        plantArray = parser.parseExcel();

        plantArray = parser.collapseHorizontally(plantArray);
        plantArray = parser.collapseVertically(plantArray);
        parser.replaceNulls(plantArray);

        String oldUploadId = ExcelParser.getLiveUploadId(testDB);
        parser.populateDatabase(plantArray, "an arbitrary ID");

        System.gc();

        try {
            assertEquals(6, plants.count(eq("uploadId", "an arbitrary ID")));
            assertEquals(4, plants.count(and(eq("commonName", "Begonia"),eq("uploadId", "an arbitrary ID"))));
            InputStream fromADDFile = this.getClass().getResourceAsStream("/Test_PatchingAccession2016_ADD_17603.xlsx");
            parser = new ExcelParser(fromADDFile, testDB);
            plantArray = parser.parseExcel();
            parser.patchDatabase(plantArray, "an arbitrary ID", "a totally arbitrary ID");

        }
        finally {
            ExcelParser.setLiveUploadId(oldUploadId, testDB);
        }

        System.gc();

        //In the ADD spreadsheet a flower by TARANTULA BLUE is uploaded
        assertEquals(7, plants.count(eq("uploadId", "a totally arbitrary ID")));
        assertEquals("Does not have 4 Begonias",4, plants.count(and(eq("commonName", "Begonia"),eq("uploadId", "a totally arbitrary ID"))));
        assertEquals("No TARANTULA found in ADD spreadsheet", 1, plants.count(eq("commonName", "TARANTULA")));



        //Add a comment so that when the database is patched it will copy over a comment
        PlantController plantController = new PlantController(testDB);
        plantController.storePlantComment("{ plantId: \"16011.0\", comment : \"Here is our comment for this test\" }","a totally arbitrary ID");


        try
        {
            InputStream fromDELETEFile = this.getClass().getResourceAsStream("/Test_PatchingAccession2016_DELETED_3_1600[1,9].xlsx");
            parser = new ExcelParser(fromDELETEFile, testDB);
            plantArray = parser.parseExcel();
            parser.patchDatabase(plantArray, "a totally arbitrary ID", "an even more arbitrary ID");


        }
        finally {
            ExcelParser.setLiveUploadId(oldUploadId, testDB);
        }
        System.gc();
        //In the DELETED spreadsheet 2 Begonias are removed
        //3 flowers in total are removed
        assertEquals(3, plants.count(eq("uploadId", "an even more arbitrary ID")));
        assertEquals("Does not have 4-2 Begonias",4-2, plants.count(and(eq("commonName", "Begonia"),eq("uploadId", "an even more arbitrary ID"))));
        assertEquals("A TARANTULA was found in this uploadId (how scary)", 0, plants.count(and(eq("commonName", "TARANTULA"), eq("uploadId", "an even more arbitrary ID"))));

        //For coverage
        ExcelParser.printArray(new String[]{});
        ExcelParser.printDoubleArray(new String[][]{});

    }


}
