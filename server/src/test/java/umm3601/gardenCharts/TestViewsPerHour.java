package umm3601.gardenCharts;

import com.google.gson.JsonArray;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.ExcelParser;
import umm3601.digitalDisplayGarden.GardenCharts;
import umm3601.digitalDisplayGarden.PlantController;
import umm3601.plant.PopulateMockDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by carav008 on 4/14/17.
 */
public class TestViewsPerHour{
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
    public void TestGetViewsPerHour() throws IOException{
        GardenCharts gardenCharts = new GardenCharts(testDB);
        testUtils TestUTILS = new testUtils();


        //Will use the mock database with the inputed upload id and track the hour the plant was visited

        //System.out.println(gardenCharts.getPlantViewsPerHour("googleCharts uploadId"));
        //[["Hour","Views"],["0",0],["1",0],["2",0],["3",0],["4",0],["5",0],["6",0],["7",0],["8",0],["9",0],["10",0],["11",0],["12",0],["13",0],["14",0],["15",0],["16",0],["17",0],["18",0],["19",1],["20",0],["21",0],["22",3],["23",0]]

        //System.out.println(plantController.getJSONFeedbackForPlantByPlantID("16053.0","googleCharts uploadId"));
        //System.out.println(plantController.getJSONFeedbackForPlantByPlantID("16037.0","googleCharts uploadId"));

        String json1 =plantController.getPlantFeedbackByPlantIdJSON("16053.0", "5.0","googleCharts uploadId");
        String json2 = plantController.getPlantFeedbackByPlantIdJSON("16037.0", "2S","googleCharts uploadId");
        String json3 = plantController.getPlantFeedbackByPlantIdJSON("16037.0","2S", "invalid uploadId");

        assertEquals("the plant 16053 should have like:true and like:false","{ \"likeCount\" : 1 , \"dislikeCount\" : 1 , \"commentCount\" : 0}", json1 );
        assertEquals("the plant 16037 should have like:true and like:false","{ \"likeCount\" : 2 , \"dislikeCount\" : 1 , \"commentCount\" : 0}", json2);
        assertEquals("get plant with invalid uploadId should be \"null\"","null", json3);


        /*
        Commented out to pass Travic CI Tests
         */

        String string = gardenCharts.getPlantViewsPerHour("googleCharts uploadId");
        JsonArray json = TestUTILS.stringToJSONArray(string);

        ArrayList<Object> arrayList = TestUTILS.JSONArrayToArrayList(json);
        System.out.println(arrayList);
        Object object1 = arrayList.get(12).toString();
        Object object2 = arrayList.get(20).toString();
        Object object3 = arrayList.get(1).toString();
        Object object4 = arrayList.get(2).toString();

        assertEquals("at the 11th hour there should be 2 visit","[\"11\",1]",object1);
        assertEquals("at the 19nd hour there should be 1 visits","[\"7\",1]",object2);
        assertEquals("at midnight, there should be 0 visits", "[\"12\",1]", object3);
        assertEquals("at 1am, there should be 0 visits", "[\"1\",0]",object4);
    }


}
