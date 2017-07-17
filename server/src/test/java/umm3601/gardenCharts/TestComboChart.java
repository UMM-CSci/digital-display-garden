package umm3601.gardenCharts;


import com.google.gson.JsonArray;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.GardenCharts;
import umm3601.digitalDisplayGarden.PlantController;
import umm3601.plant.PopulateMockDatabase;

import java.io.IOException;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;

public class TestComboChart {
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
    public void testComboChart() throws IOException {
        GardenCharts gardenCharts = new GardenCharts(testDB);
        testUtils TestUTILS = new testUtils();

        String string = gardenCharts.getComboChart("googleCharts uploadId");

        ArrayList<Object> arrayList = TestUTILS.JSONtoArrayList(string);

        TestUTILS.printDoubleArrayList(arrayList);

        System.out.println(arrayList);

        Object midnightSunday = ((ArrayList<Object>)arrayList.get(1)).get(3); //should be 1
        Object midnightFriday = ((ArrayList<Object>)arrayList.get(1)).get(6); //should be 0
        Object elevenSunday = ((ArrayList<Object>)arrayList.get(12)).get(1);//should be 1
        Object elevenFriday = ((ArrayList<Object>)arrayList.get(12)).get(6); //should be 1
        Object sevenTuesday = ((ArrayList<Object>)arrayList.get(20)).get(3); //should be 1
        Object oneWednesday = ((ArrayList<Object>)arrayList.get(2)).get(4); //should be 0

        Object avgMidnight = ((ArrayList<Object>)arrayList.get(1)).get(8); //should be 1
        Object avgEleven = ((ArrayList<Object>)arrayList.get(12)).get(8); //should be 1
        Object avgSeven = ((ArrayList<Object>)arrayList.get(20)).get(8); //should be 1
        Object avgFive = ((ArrayList<Object>)arrayList.get(6)).get(8); //should be 1

        assertEquals("There should be 1 view on Sunday at Midnight", new Integer(1).toString(), midnightSunday.toString());
        assertEquals("There should be 0 views on Friday at Midnight", new Integer(0).toString(),midnightFriday.toString());
        assertEquals("There should be 1 view on Sunday at 11:00AM", new Integer(1).toString(), elevenSunday.toString());
        assertEquals("There should be 1 view on Friday at 11:00AM", new Integer(1).toString(), elevenFriday.toString());
        assertEquals("There should be 1 view on Tuesday at 7:00PM", new Integer(1).toString(),sevenTuesday.toString());
        assertEquals("There should be 0 views on Wednesday at 1:00AM", new Integer(0).toString(), oneWednesday.toString());

        assertEquals("The average for Midnight should be 1", new Integer(1).toString(), avgMidnight.toString());
        assertEquals("The average for 11:00AM should be 1", new Integer(1).toString(), avgEleven.toString());
        assertEquals("The average for 7:00PM should be 1", new Integer(1).toString(), avgSeven.toString());
        assertEquals("The average for 5:00AM should be 0", new Integer(0).toString(), avgFive.toString());


    }
}
