package umm3601.digitalDisplayGarden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.fields;

import java.io.IOException;
import java.util.*;

public class GardenCharts
{
    private final MongoCollection<Document> plantCollection;
    private final MongoCollection<Document> bedCollection;
    private final MongoCollection<Document> commentCollection;
    private final MongoCollection<Document> configCollection;

    public GardenCharts(MongoDatabase database) throws IOException {
        plantCollection = database.getCollection("plants");
        bedCollection = database.getCollection("beds");
        commentCollection = database.getCollection("comments");
        configCollection = database.getCollection("config");
    }

    public String top20Charts(PlantController plantController, String uploadID, String type){
        try{
            String[] plantID = plantController.listPlantIDs(uploadID);
            String[] cultivar = plantController.listCultivars(uploadID);
            Map<String, Integer> result = new HashMap<>();
            for (int i = 0; i < cultivar.length; i++) {

                int likes = 0;
                int dislikes = 0;
                int comments = 0;
                Document filter = new Document();
                filter.append("uploadId", uploadID);
                filter.append("id", plantID[i]);

                FindIterable<Document> iter = plantCollection.find(filter);
                for (Document plant : iter) {
                    long[] feedback = plantController.getPlantFeedbackByPlantId(plant.getString("id"), plant.getString("gardenLocation"), uploadID);
                    if (type.equals("likes")) {
                        likes += feedback[PlantController.PLANT_FEEDBACK_LIKES];
                    }
                    if (type.equals("dislikes")) {
                        dislikes += feedback[PlantController.PLANT_FEEDBACK_DISLIKES];
                    }
                    if (type.equals("comments")) {
                        comments += feedback[PlantController.PLANT_FEEDBACK_COMMENTS];
                    }

                }

                String key = cultivar[i];
                if (type.equals("likes")) {
                    Integer value = (Integer) likes;
                    result.put(key, value);
                }
                if (type.equals("dislikes")) {
                    Integer value = (Integer) dislikes;
                    result.put(key, value);
                }
                if (type.equals("comments")) {
                    Integer value = (Integer) comments;
                    result.put(key, value);
                }

            }

            Map<String, Integer> finalMap = new HashMap<>();
            finalMap = sortByValue(result);
            JsonArray finalJsonArray = new JsonArray();
            Set keyset = finalMap.keySet();
            List<?> list = new ArrayList<>(keyset);
            int count = 0;
            if (finalMap.size() < 20){
                for(int i = 0; i < finalMap.size(); i++) {
                    JsonObject plantMetadata = new JsonObject();
                    String cultivarName = "";
                    int typeOfData = 0;
                    cultivarName = (String) list.get(i);
                    typeOfData = finalMap.get(cultivarName);
                    if(typeOfData == 0){
                        cultivarName = "";
                        typeOfData = 0;
                        plantMetadata.addProperty("cultivarName", cultivarName);
                        plantMetadata.addProperty("likes", typeOfData);
                        finalJsonArray.add(plantMetadata);
                        return finalJsonArray.toString();
                    }
                    if (count == 0 && typeOfData == 0){
                        cultivarName = "";
                        typeOfData = 0;
                        plantMetadata.addProperty("cultivarName", cultivarName);
                        plantMetadata.addProperty("likes", typeOfData);
                        finalJsonArray.add(plantMetadata);
                        return finalJsonArray.toString();
                    }
                    plantMetadata.addProperty("cultivarName", cultivarName);
                    plantMetadata.addProperty("likes", typeOfData);
                    finalJsonArray.add(plantMetadata);
                    count++;
                }
                return finalJsonArray.toString();
            }
            for(int i = 0; i < 20; i++) {
                JsonObject plantMetadata = new JsonObject();
                String cultivarName = "";
                int typeOfData = 0;
                cultivarName = (String) list.get(i);
                typeOfData = finalMap.get(cultivarName);
                if (count == 0 && typeOfData == 0){
                    cultivarName = "";
                    typeOfData = 0;
                    plantMetadata.addProperty("cultivarName", cultivarName);
                    plantMetadata.addProperty("likes", typeOfData);
                    finalJsonArray.add(plantMetadata);
                    return finalJsonArray.toString();
                }
                if(typeOfData == 0){
                    return finalJsonArray.toString();
                }
                plantMetadata.addProperty("cultivarName", cultivarName);
                plantMetadata.addProperty("likes", typeOfData);
                finalJsonArray.add(plantMetadata);
                count++;
            }
            return finalJsonArray.toString();

        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }



    public String getPlantViewsPerHour(String uploadID) {

        try {
            Object[][] dataTable = new Object[24 + 1][2];

            dataTable[0][0] = "Hour";
            dataTable[0][1] = "Views";

            ArrayList<Date> dates = getDatesFromDB(uploadID);
            System.out.println("DATES = " + dates);
            ArrayList<Date>[] hoursOfDay = partitionByHour(dates);

            int[][] viewsPerHourPerDayOfWeek = averageViewsPerDayOfWeek(hoursOfDay);

            int[] viewsPerHour = flaten_averageByHour(viewsPerHourPerDayOfWeek);

            //printArray(viewsPerHour);

            int[] civilianTime = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            for (int i = 1; i < 24 + 1; i++) {
                dataTable[i][0] = Integer.toString(civilianTime[i - 1]);
                dataTable[i][1] = viewsPerHour[i - 1];
            }

            return makeJSON(dataTable);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public String getComboChart (String uploadID) {
        try {
            // This is the double array that will be sent to the client as a JSON Array
            Object[][] dataTable = new Object[24 + 1][9];

            dataTable[0][0] = "Hour";
            dataTable[0][1] = "Sunday";
            dataTable[0][2] = "Monday";
            dataTable[0][3] = "Tuesday";
            dataTable[0][4] = "Wednesday";
            dataTable[0][5] = "Thursday";
            dataTable[0][6] = "Friday";
            dataTable[0][7] = "Saturday";
            dataTable[0][8] = "Average";

            /*
            dates is an ArrayList that contains all the dates of the visits from the db.
             */
            ArrayList<Date> dates = getDatesFromDB(uploadID);

            ArrayList<Date>[] hoursOfDay = partitionByHour(dates);

            int[][] viewsPerHourPerDayOfWeek = averageViewsPerDayOfWeek(hoursOfDay);

            int[] viewsPerHour = flaten_averageByHour(viewsPerHourPerDayOfWeek);


            //String[] civilianTimeString = {"12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00"};

            int[] civilianTime = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            for (int i = 1; i < 24 + 1; i++) {
                //dataTable[i][0] = civilianTimeString[i - 1];
                dataTable[i][0] = Integer.toString(civilianTime[i - 1]);
                dataTable[i][8] = viewsPerHour[i - 1];
            }

            for(int i = 0; i < viewsPerHourPerDayOfWeek.length; i++){
                for(int j = 0; j < viewsPerHourPerDayOfWeek[i].length; j++){
                    dataTable[i + 1][j + 1] = viewsPerHourPerDayOfWeek[i][j];
                }
            }

            //System.out.println();
            //print2DArray(dataTable);

            return makeJSON(dataTable);
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Form a JSON to pass to the client to render in the Google Maps Bed Metadata Map
     *
     * This returns an array with an entry for each gardenLocation
     * {gardenLocation : string, likes : number, dislikes : number, comments : number}
     * @param plantController
     * @param uploadID
     * @return
     */
    public String getBedMetadataForMap(PlantController plantController, String uploadID) {
        // Count Flower likes
        // Count flower dislikes
        // Count flower comments

        try {


            String[] bedNames = plantController.getGardenLocations(uploadID);
            JsonArray out = new JsonArray();

            for (int i = 0; i < bedNames.length; i++) {
                int likes = 0;
                int dislikes = 0;
                int comments = 0;
                JsonObject bed = new JsonObject();
                Document filter = new Document();
                filter.append("uploadId", uploadID);
                filter.append("gardenLocation", bedNames[i]);

                FindIterable<Document> iter = plantCollection.find(filter);
                for (Document plant : iter) {
                    long[] feedback = plantController.getPlantFeedbackByPlantId(plant.getString("id"), plant.getString("gardenLocation"), uploadID);

                    likes += feedback[PlantController.PLANT_FEEDBACK_LIKES];
                    dislikes += feedback[PlantController.PLANT_FEEDBACK_DISLIKES];
                    comments += feedback[PlantController.PLANT_FEEDBACK_COMMENTS];
                }
                bed.addProperty("gardenLocation", bedNames[i]);
                bed.addProperty("likes", likes);
                bed.addProperty("dislikes", dislikes);
                bed.addProperty("comments", comments);//TODO: could be refactored to include pageViews
                out.add(bed);
            }

            return out.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Form a JSON to pass to the client to render in the BubbleMap google chart.
     *
     * This returns an array with an entry for each gardenLocation
     * {gardenLocation : string, likes : number, pageViews : number}
     * @param plantController in order to getGardenLocations and PlantFeedback to count likes
     * @param bedController in order to get Page views for a bed
     * @param uploadID
     * @return
     */
    public String getBedMetadataForBubbleMap(PlantController plantController, BedController bedController, String uploadID) {
        // Count Flower likes
        // Count flower dislikes
        // Count flower comments

        try {
            int pageViews;

            String[] bedNames = plantController.getGardenLocations(uploadID);
            JsonArray out = new JsonArray();

            for (int i = 0; i < bedNames.length; i++) {
                int likes = 0;
                JsonObject bed = new JsonObject();
                Document filter = new Document();
                filter.append("uploadId", uploadID);
                filter.append("gardenLocation", bedNames[i]);

                FindIterable<Document> iter = plantCollection.find(filter);
                for (Document plant : iter) {
                    long[] feedback = plantController.getPlantFeedbackByPlantId(plant.getString("id"), plant.getString("gardenLocation"), uploadID);
                    likes += feedback[PlantController.PLANT_FEEDBACK_LIKES];
                }

                pageViews = bedController.getPageViews(bedNames[i], uploadID);

                bed.addProperty("gardenLocation", bedNames[i]);
                bed.addProperty("likes", likes);
                bed.addProperty("pageViews", pageViews);
                out.add(bed);
            }

            return out.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }


    /*
        ******************************** UTILITIES ************************************
     */


    /**
     * Creates a two dimensional Json array from an Object[][] in.
     *
     * Valid Objects to put in the Object[][] are Strings, Numbers (bool, integer, double, etc.), and JsonElements
     *
     * @param in
     * @return
     */
    public String makeJSON(Object[][] in) {

        JsonArray outerArray = new JsonArray();
        for(int i = 0; i < in.length; i++) {
            JsonArray innerArray = new JsonArray();
            for(int j = 0; j < in[i].length; j++) {
                Object a = in[i][j];
                Class objectClass = a.getClass();
                if(objectClass == String.class)
                {
                    innerArray.add(in[i][j].toString());
                }
                else if(Number.class.isAssignableFrom(objectClass))
                {
                    innerArray.add((Number)in[i][j]);
                }
                else if(objectClass == JsonElement.class)
                {
                    innerArray.add((JsonElement)in[i][j]);
                }
                else
                {
                    throw new RuntimeException("WHAT ARE YOU");
                }
            }

            outerArray.add(innerArray);
        }
        return outerArray.toString();
    }

    public ArrayList<Date> convertToDates(ArrayList<Object> objectDates){
        try {
            ArrayList<Date> dates = new ArrayList<>();
            for(Object objectDate : objectDates){
                Date date = (Date)objectDate;
                dates.add(date);
            }
            return dates;
        }catch (Exception e){
            System.out.println("Object arrayList cannot be cast into date arrayList");
            e.printStackTrace();
            throw e;
        }
    }

    public void printPartitionByMonth(ArrayList<Date>[] in){
        System.out.println("[");
        for(int i = 0; i < in.length; i++){
            System.out.println(in[i] + ",");
        }
        System.out.println("]");
    }

    public void printPartitionByDayOfWeek(ArrayList<Date>[][] in){
        System.out.println("Partitioned by day of the week");
        System.out.println("[");
        for(int i = 0; i < in.length; i++){
            for(int j = 0; j < in[i].length; j++) {
                System.out.print(in[i][j]);
                System.out.print("  |  ");
            }
            System.out.println();
        }
        System.out.println("]");
    }

    public void print2DArray(int[][] in){
        System.out.println("Printing 2d array");
        for(int i = 0; i < in.length; i++){
            for(int j = 0; j < in[i].length; j++){
                if(j != in[i].length - 1){
                    System.out.print(in[i][j] + " | ");
                }else{
                    System.out.println(in[i][j]);
                }
            }
        }
        System.out.println();
    }
    // prints a 1D array
    public static void printArray(int[] input){
        System.out.print("[");
        for(int i = 0; i < input.length; i++){
            if(i != input.length - 1){
                System.out.print(input[i] + ", ");
            }else{
                System.out.println(input[i] + "]");
            }
        }
    }

    public ArrayList<Date> getDatesFromDB(String uploadID){
        Document filter = new Document();
        filter.put("uploadId", uploadID);

        ArrayList<Date> dates = new ArrayList<Date>();

        //Get all
        FindIterable doc = plantCollection.find(filter);

        Iterator iterator = doc.iterator();
        while(iterator.hasNext()) {
            Document result = (Document) iterator.next();

            //Get metadata.rating array
            List<Document> ratings = (List<Document>) ((Document) result.get("metadata")).get("visits");

            for(int i = 0; i < ratings.size(); i++){
                Document d = ratings.get(i);
                if(!d.isEmpty())
                    dates.add(((Date) d.get("visit")));
            }

        }
        return  dates;
    }

    public int[] flaten_averageByHour(int[][] viewsByHourAndDayOfWeek){
        int[] avgHour = new int[24];
        for(int i = 0 ; i < viewsByHourAndDayOfWeek.length; i++) {
            int avg = 0;
            int avgNumerator = 0;
            int avgDenominator = 0;
            for(int j = 0; j < viewsByHourAndDayOfWeek[i].length; j++) {
                if(viewsByHourAndDayOfWeek[i][j] > 0) {
                    avgNumerator += viewsByHourAndDayOfWeek[i][j];
                    avgDenominator++;
                }
            }
            if(avgDenominator != 0) avg = avgNumerator/avgDenominator;
            avgHour[i] = avg;
        }
        return avgHour;
    }

    public int[][] averageViewsPerDayOfWeek(ArrayList<Date>[] hoursOfDay){
        int[][] viewsPerHourPerDayOfWeek = new int[24][7];

        for(int i = 0; i < hoursOfDay.length; i++){
            int[] views1DArray = new int[7];
            if(hoursOfDay[i] != null) {
                views1DArray = averageViewsPerDayOfWeekPerHour(hoursOfDay[i]);
            }
            viewsPerHourPerDayOfWeek[i] = views1DArray;
        }
        //print2DArray(viewsPerHourPerDayOfWeek);
        return viewsPerHourPerDayOfWeek;
    }

    public int[] averageViewsPerDayOfWeekPerHour(ArrayList<Date> hoursOfDay){
        int[] views1DArray = new int[7];

        if(hoursOfDay != null) {
            ArrayList<Date> specificHour = hoursOfDay;

            ArrayList<Date>[][] threeDArray = partitionByDayOfWeek(partitionByMonth(specificHour));
            int[][] views2dArray = flaten_viewsByDayAndMonth(threeDArray);
            views1DArray = flaten_AverageByMonth(views2dArray);

            //System.out.print("Hour " + i + " ");
            //printArray(views1DArray);

        }
        return views1DArray;
    }

    public ArrayList<Date>[] partitionByHour(ArrayList<Date> dates){
        ArrayList<Date>[] hoursOfDay = new ArrayList[24];

        for(Date date : dates){
            int hour = date.getHours();
            ArrayList<Date> oneHour = new ArrayList<>();
            if(hoursOfDay[hour] != null){
                oneHour = hoursOfDay[hour];
                oneHour.add(date);
                hoursOfDay[hour] = oneHour;
            }else {
                oneHour.add(date);
                hoursOfDay[hour] = oneHour;
            }
        }

        return hoursOfDay;
    }

    public ArrayList<Date>[] partitionByMonth(ArrayList<Date> dates){
        ArrayList<Date>[] months = new ArrayList[12];
        for(Date date : dates){
            int month = date.getMonth();
            ArrayList<Date> datesInMonth;

            if(months[month] != null){
                datesInMonth = months[month];
                datesInMonth.add(date);
                months[month] = datesInMonth;
            }else {
                datesInMonth = new ArrayList<>();
                datesInMonth.add(date);
                months[month] = datesInMonth;
            }
        }
        return months;
    }

    /*
    ArrayList<Date>[dayOfWeek][month] = new int[7][12]
     */
    public ArrayList<Date>[][] partitionByDayOfWeek(ArrayList<Date>[] datesByMonth){
        ArrayList<Date>[][] datesPerDayAndMonth = new ArrayList[7][12];

        for(int i = 0; i < datesByMonth.length; i++){
            if(datesByMonth[i] != null) {
                for (Date date : datesByMonth[i]) {

                    int dayOfWeek = date.getDay();
                    int month = i;
                    ArrayList<Date> dates;

                    if (datesPerDayAndMonth[dayOfWeek][month] != null) {
                        dates = datesPerDayAndMonth[dayOfWeek][month];
                        dates.add(date);
                        datesPerDayAndMonth[dayOfWeek][month] = dates;
                    } else {
                        dates = new ArrayList<>();
                        dates.add(date);
                        datesPerDayAndMonth[dayOfWeek][month] = dates;
                    }
                }
            }
        }

        return datesPerDayAndMonth;
    }

    /*
    int[dayOfWeek][month] = new int[7][12]
     */
    public int[][] flaten_viewsByDayAndMonth(ArrayList<Date>[][] dates){
        int[][] viewsPerDayAndMonth = new int[7][12];

        for(int i = 0; i < dates.length; i++){
            if(dates[i] != null){
                for(int j = 0; j < dates[i].length; j++){
                    //System.out.println("for index " + i + ", dates[i].length is: " + dates[i].length);
                    if(dates[i][j] != null){
                        viewsPerDayAndMonth[i][j] = dates[i][j].size();
                    }
                }
            }
        }
        return viewsPerDayAndMonth;
    }


    /*
    int[dayOfWeek][month] = new int[7][12]
     */
    public int[] flaten_AverageByMonth(int[][] viewsByDayOfWeekAndMonth){
        int[] averagePerDayOfWeek = new int[7];
        for(int i = 0; i < viewsByDayOfWeekAndMonth.length; i++){

            int average = 0;
            int averageNumerator = 0;
            int averageDenominator = 0;

            for(int j = 0; j < viewsByDayOfWeekAndMonth[i].length; j++){
                if(viewsByDayOfWeekAndMonth[i][j] > 0) averageDenominator++;
                averageNumerator += viewsByDayOfWeekAndMonth[i][j];
            }

            if(averageDenominator != 0){
                average = averageNumerator/averageDenominator;
            }
            averagePerDayOfWeek[i] = average;
        }
        return averagePerDayOfWeek;
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new RatingsComparator());
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static class RatingsComparator implements Comparator<Map.Entry<String, Integer>>
    {
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
        {
            return -(o1.getValue()).compareTo(o2.getValue());
        }
    }


}


