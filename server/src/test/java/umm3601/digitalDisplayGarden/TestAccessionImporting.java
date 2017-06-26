package umm3601.digitalDisplayGarden;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static com.mongodb.client.model.Filters.*;

/**
 * Created by benek020 on 3/6/17.
 */
public class TestAccessionImporting {

    private final static String databaseName = "data-for-testing-only";

    public MongoClient mongoClient = new MongoClient();
    public MongoDatabase testDB;
    public ExcelParser parser;
    public InputStream fromFile;
    @Before
    public void clearAndPopulateDatabase(){
        mongoClient.dropDatabase(databaseName);
        testDB = mongoClient.getDatabase(databaseName);
        fromFile = this.getClass().getResourceAsStream("/Test_Accession2016.xlsx");
        parser = new ExcelParser(fromFile, testDB);
    }


    @Test
    public void testSpeadsheetToDoubleArray() throws FileNotFoundException {
        String[][] plantArray = parser.parseExcel();
        //printDoubleArray(plantArray);

        assertEquals(10, plantArray.length);
        assertEquals(plantArray[2].length, plantArray[5].length);
        assertEquals("2016 Accession List: Steve's Design", plantArray[0][1]);
        assertEquals("Begonia", plantArray[6][1]);

    }

    @Test
    public void testCollapse(){
        String[][] plantArray = parser.extractFromXLSX(fromFile);
        //System.out.println(plantArray.length);
        //printDoubleArray(plantArray);

        plantArray = parser.collapseHorizontally(plantArray);
        plantArray = parser.collapseVertically(plantArray);

        //printDoubleArray(plantArray);

        assertEquals(10, plantArray.length);
        assertEquals(8, plantArray[9].length);
        assertEquals(8, plantArray[0].length);
        assertEquals(8, plantArray[3].length);
    }

    @Test
    public void testReplaceNulls(){
        String[][] plantArray = parser.extractFromXLSX(fromFile);
        plantArray = parser.collapseHorizontally(plantArray);
        plantArray = parser.collapseVertically(plantArray);
        parser.replaceNulls(plantArray);

        for (String[] row : plantArray){
            for (String cell : row){
                assertNotNull(cell);
            }
        }
    }

    @Test
    public void testPopulateDatabase(){
        String[][] plantArray = parser.extractFromXLSX(fromFile);
        plantArray = parser.collapseHorizontally(plantArray);
        plantArray = parser.collapseVertically(plantArray);
        parser.replaceNulls(plantArray);



        String oldUploadId = ExcelParser.getLiveUploadId(testDB);
        parser.populateDatabase(plantArray, "an arbitrary ID");
        MongoCollection plants = testDB.getCollection("plants");


        try {
            assertEquals(0, plants.count(eq("commonName", "UMM"))); //Tests that x'ed flower got removed
            assertEquals(6, plants.count());
            assertEquals(4, plants.count(eq("commonName", "Begonia")));

        }
        finally {
            ExcelParser.clearUpload("an arbitrary ID", testDB);
            ExcelParser.setLiveUploadId(oldUploadId, testDB);
        }

    }
}
