package umm3601.digitalDisplayGarden;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.*;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static java.lang.Math.max;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.print.Doc;
//import sun.text.normalizer.UTF16;

/**
 * Parses .xlsx files into a String[] and populates
 * the database with flowers corresponding to entries within the xlsx file.
 */
public class ExcelParser {

    /* Input stream to parse as an .xlsx file */
    //TODO: this should be refactored as an input to parseExcel (not doing it right now, on master)
    private InputStream stream;

    private final MongoDatabase database;
    private final MongoCollection plantCollection;
    private final MongoCollection bedCollection;
    private final MongoCollection configCollection;
    private final MongoCollection commentCollection;

    public ExcelParser(InputStream stream, MongoDatabase database){
        plantCollection = database.getCollection("plants");
        bedCollection = database.getCollection("beds");
        configCollection = database.getCollection("config");
        commentCollection = database.getCollection("comments");
        this.database = database;
        this.stream = stream;
    }

    /**
     * Takes the .xlsx file in the input stream, writes the cells into
     * an array of strings, then collapse empty rows and columns.
     * any entries that are empty are the empty string "".
     * @return
     */
    public String[][] parseExcel() {

        String[][] arrayRepresentation = extractFromXLSX(stream);

        String[][] horizontallyCollapsed = collapseHorizontally(arrayRepresentation);
        String[][] verticallyCollapsed = collapseVertically(horizontallyCollapsed);
        replaceNulls(verticallyCollapsed);
        return verticallyCollapsed;
    }

    /**
        Uses Apache POI to extract information from xlsx file into a 2D array.
        Here is where we got our starter code: http://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
        Look at example number 4, Apache POI library – Reading an Excel file.

        This file originally just printed data, that is why there are several commented out lines in the code.
        We have repurposed this method to put all data into a 2D String array and return it.
     */
    public String[][] extractFromXLSX(InputStream excelFile) throws NotOfficeXmlFileException {
        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);

            String[][] cellValues = new String[datatypeSheet.getLastRowNum() + 1]
                    [max(max(datatypeSheet.getRow(1).getLastCellNum(), datatypeSheet.getRow(2).getLastCellNum()),
                    datatypeSheet.getRow(3).getLastCellNum())];

            for (Row currentRow : datatypeSheet) {
                //cellValues[currentRow.getRowNum()] = new String[currentRow.getLastCellNum()];

                for (Cell currentCell : currentRow) {

                    //getCellTypeEnum shown as deprecated for version 3.15
                    //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        cellValues[currentCell.getRowIndex()][currentCell.getColumnIndex()] = currentCell.getStringCellValue();
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        cellValues[currentCell.getRowIndex()][currentCell.getColumnIndex()] =
                                Integer.toString((int) Math.round(currentCell.getNumericCellValue()));
                    }

                }

            }
            return cellValues;
        } catch (IOException e) {
            System.out.println("EVERYTHING BLEW UP STOP STOP STOP");
            e.printStackTrace();
            return null; //TODO: this should not return null. This should continue.
        }

    }

    // removes all columns that are completely null.
    // Identify how much to shrink the array and calls a helper function to shrink it
    public static String[][] collapseHorizontally(String[][] cellValues){
        // scanning over columns
        for(int j = cellValues[1].length - 1; j > 0; j--){
            //scanning over the three rows of our "key row"
            for(int i = 1; i <= 3; i++) {
                if(cellValues[i][j] != null){
                    return trimArrayHorizontally(cellValues, j);
                }
            }
        }
        return null;
    }

    // removes all completely null rows
    // Identify how much to shrink the array and calls a helper function to shrink it
    public static String[][] collapseVertically(String[][] cellValues){
        for(int i = cellValues.length - 1; i > 0; i--) {
            if(!(cellValues[i][0] == null)) {
                return trimArrayVertically(cellValues, i + 1);
            }
        }
        return null;
    }

    // helper function for collapseHorizontally() decreases the number of columns in the array
    public static String[][] trimArrayHorizontally(String[][] cellValues, int horizontalBound){
        String[][] trimmedArray = new String[cellValues.length][];
        for(int j = 0; j < cellValues.length; j++) {
            trimmedArray[j] = new String[horizontalBound + 1];
            for (int i = 0; i < horizontalBound + 1; i++) {
                trimmedArray[j][i] = cellValues[j][i];
            }
        }
        return trimmedArray;
    }

    /**
     * Helper function for collapseHorizontally()
     *
     * Returns a new String[verticalBound][] with the values from cellValues
     * copied into it.
     */
    public static String[][] trimArrayVertically(String[][] cellValues, int verticalBound){
        String[][] trimmedArray = new String[verticalBound][];
        for(int i = 0; i < verticalBound; i++) {
            trimmedArray[i] = new String[cellValues[i].length];
            trimmedArray[i] = cellValues[i];
        }
        return trimmedArray;
    }

    /**
     * Replaces all cells with null values with an empty string (avoids future null pointer exceptions)
     */
    public static void replaceNulls(String[][] cellValues) {
        for(int i = 0; i < cellValues.length; i++) {
            for(int j = 0; j < cellValues[i].length; j++) {
                if(cellValues[i][j] == null) {
                    cellValues[i][j] = "";
                }
            }
        }
    }

    /**
    * Helper method for populateDatabase()
    * looks at rows start through end (Remember: xlsx file is 1 based) and:
    * 1. Concatenates all content in rows 2 through 4 for a given column
    * 2. Adds all keys into a 1D string array
    * 3. Replaces certain key words so they match with the standard committee's requirements
    * @param cellValues
    * @param keyrow_start The first row that will be concatenated into a key
     * @param keyrow_end The last row that will be concatenated into a key
    */
    public static String[] getKeys(String[][] cellValues, int keyrow_start, int keyrow_end){
        String[] keys = new String[cellValues[0].length];

        for(int i = 0; i < cellValues[0].length; i++){
            keys[i] = cellValues[1][i].replaceAll(" ", "").toUpperCase();
            //With regards to the replaceAll, just trim() isn't what we want for easy
            //comparison of the key strings. We want everything to be compressed as possible
            //to limit the amount of cases to put in replaceKeyName(..) below.
            for(int j = keyrow_start; j < keyrow_end; j++){
                keys[i] = keys[i] + cellValues[j][i].replaceAll(" ", "").toUpperCase();
                System.out.println(cellValues[j][i] + "][" + keys[i]);
            }
        }

        for (int i = 0; i < keys.length; i++){
            keys[i] = replaceKeyName(keys[i]);
            //if(keys[i].contains((UTF16.valueOf(0x00AE)))) keys[i].replaceAll(UTF16.valueOf(0x00AE), "");
        }

        return keys;
    }

    /**
     * Match concatenated cell names with their database names
     * @param token
     * @return
     */
    private static String replaceKeyName(String token)
    {
        switch(token)
        {
            case "#":
            case "ID":
                return "id";

            case "COMMONNAME":
                return "commonName";

            case "CULTIVAR":
            case "CV":
                return "cultivar";

            case "SOURCE":
            case "SRC":
                return "source";

            case "GARDENLOCATION":
            case "BEDS":
            case "BED":
            case "LOCATION":
                return "gardenLocation";

            case "NOTINCLUDED":
            case "NOTVISIBLE":
                return "notIncluded";

            case "=":
                return "";
        }

        return token;
    }

    /**
     *
     * @param keys
     * @param cellValue
     * @param rowNum
     * @param column
     * @param searchBy
     * @return
     */
    public static boolean ignoreRow(String[] keys, String[][] cellValue, int rowNum, String column, String searchBy) {
        for(int i = 0; i < keys.length; i++) {
            if(keys[i].equals(column) && cellValue[rowNum][i].toLowerCase().equals(searchBy)) {
                return true;
            }
        }
        return false;
    }

    // Moves row by row through the 2D array and adds content for every flower paired with keys into a document
    // Uses the document to one at a time, add flower information into the database.
    public void populateDatabase(String[][] cellValues, String uploadId){

        //Store this in case something breaks in upload and you need to regress.
        String oldUploadId = getLiveUploadId(database);

        //Spreadsheet population:
        //Skip first row
        //Rows until a number is found in the first column are concatenated into the keys.
        //All rows from the first number until the last are entries

        //Find the first number, which tells us how many cells are used for a header
        int firstRow = 0;
        for(int i = firstRow; i < cellValues.length && firstRow < 1; i++)
        {
            try
            {
                //Find the first number
                Integer.parseUnsignedInt(cellValues[i][0]);
                firstRow = i;
            }
            catch(NumberFormatException nfe)
            {

            }
        }
        if(firstRow < 1)
        {
            throw new RuntimeException("There were no accession numbers in the first column of the accession spreadsheet!");
        }

        setLiveUploadId(uploadId, database);
        String[] keys = getKeys(cellValues, 2, firstRow);




        Document emptyMetadataDoc = new Document();
        emptyMetadataDoc.append("pageViews", 0);
        emptyMetadataDoc.append("visits", new BsonArray());
        emptyMetadataDoc.append("ratings", new BsonArray());

        Document emptyMetadataBedDoc = new Document();
        emptyMetadataBedDoc.append("pageViews", 0);
        emptyMetadataBedDoc.append("qrScans", 0);
        emptyMetadataBedDoc.append("bedVisits", new BsonArray());
        emptyMetadataBedDoc.append("qrVisits", new BsonArray());



        for (int i = firstRow; i < cellValues.length; i++){
            Document doc = new Document();
            boolean emptyBed = ignoreRow(keys,cellValues,i,"gardenLocation", "");
            boolean xed = ignoreRow(keys,cellValues,i,"notIncluded", "x");

            if(!emptyBed && !xed) {
                for (int j = 0; j < cellValues[i].length; j++) {
                    doc.append(keys[j], cellValues[i][j]);
                }

                System.out.println(doc);
                if (doc.get("gardenLocation").equals(""))
                    continue;

                // Initialize the empty metadata
                doc.append("metadata", emptyMetadataDoc);
                doc.append("uploadId", uploadId);
                plantCollection.insertOne(doc);
            }
        }

        //Get distinct list of beds
        Document uploadIdFilter = new Document();
        uploadIdFilter.append("uploadId", uploadId);
        DistinctIterable<String>  bedIterator = plantCollection.distinct("gardenLocation", uploadIdFilter, String.class);

        //Add beds to bed collection with empty metadata
        for(String gardenLocation : bedIterator) {
            Document bedDoc = new Document();
            bedDoc.append("gardenLocation", gardenLocation);
            bedDoc.append("metadata", emptyMetadataBedDoc);
            bedDoc.append("uploadId", uploadId);
            bedCollection.insertOne(bedDoc);
        }


    }

    /**
     * When the database is patched the database is populated with the new data exactly as populateDatabase
     * Then any metadata from the oldUploadId is copied from any plant, bed, and comment collections
     * and inserted into the corresponding  plant, bed, and comment collections with the newUploadId
     * Finally, the liveUploadId is set to the newUploadId
     * @param cellValues
     * @param oldUploadId
     * @param newUploadId
     */
    public void patchDatabase(String[][] cellValues, String oldUploadId, String newUploadId){

        populateDatabase(cellValues, newUploadId);

        //Migrate (copy) all plants/comments of the previous uploadID and add them

        Document filterByOldUploadId = new Document();
        filterByOldUploadId.put("uploadId", oldUploadId);
        FindIterable<Document> plantsOldId = plantCollection.find(filterByOldUploadId);

        Document filterByNewUploadId = new Document();
        filterByNewUploadId.put("uploadId", newUploadId);

        //Looop through old plants
        for(Document oldPlant : plantsOldId)
        {
            //With a plant, grab its metadata and update the new plant with the old metadata
            Document oldMetadata = ((Document)oldPlant.get("metadata"));

            Document newPlantFilter = new Document(filterByNewUploadId);
            newPlantFilter.put("id", oldPlant.getString("id"));
            newPlantFilter.put("gardenLocation", oldPlant.getString("gardenLocation"));

            //Looks for a plant that has the same plantId with the new uploadId and migrates old metadata over
            //If a plant with no such plantId can be found, it will ignore it.
            plantCollection.findOneAndUpdate(newPlantFilter,set("metadata", oldMetadata));
        }

        FindIterable<Document> commentsOldId = commentCollection.find(filterByOldUploadId);
        for(Document comment : commentsOldId)
        {
            //Take an old comment, change its' uploadId and objectID
            //PROBLEM: updating _id means that that object's effective date is _now_
            //So when you patch old comments, the date they will be said to be made is the time of patch
            comment.put("uploadId", newUploadId);

            //for now we should at least keep this as without this old comments arent saved
            comment.put("_id", new ObjectId());

            //Only re-upload a comment if the plant it points to still exists
            Document newPlantFilter = new Document(filterByNewUploadId);
            newPlantFilter.put("id", comment.get("commentOnPlant"));
            newPlantFilter.put("gardenLocation", comment.get("commentInBed"));
            if(plantCollection.find(newPlantFilter).first() != null) {
                commentCollection.insertOne(comment);
            }
        }

        FindIterable<Document> bedsOldId = bedCollection.find(filterByOldUploadId);
        for(Document bed : bedsOldId)
        {
            Document oldMetadata = ((Document)bed.get("metadata"));
            bed.put("uploadId", newUploadId);
            bed.put("_id", new ObjectId());

            Document newBedFilter = new Document(filterByNewUploadId);
            bedCollection.findOneAndUpdate(newBedFilter,set("metadata", oldMetadata));
        }

        clearUpload(oldUploadId, database);
        setLiveUploadId(newUploadId, database);
    }


    /*
    ------------------------------- UTILITIES -----------------------------------
     */

    // prints a 1D array
    public static void printArray(String[] input){
        System.out.print("[");
        for(String str : input){
            System.out.print(str + ", ");
        }
        System.out.println("]");
    }

    /*
    Prints a 2D array.
    NOTE: If you uncomment the commented out line, and comment the one beneath it,
    you will also see the indexes for every element as well as their content.
     */
    public static void printDoubleArray(String[][] input){
        for(int i = 0; i < input.length; i++){
            if (!(input[i] == (null))) {
                for (int j = 0; j < input[i].length; j++) {
                    //System.out.print(" | " + "i: " + i + " j: " + j + " value: " + input[i][j] );
                    System.out.print(" | " + input[i][j]);
                }
                System.out.println();
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }
        }
    }

    /*
    --------------------------- SERVER UTILITIES -------------------------------
     */

    /**
     * Sets the liveUploadId within config collection to uploadId within the provided database.
     * @param uploadID
     * @param database
     */
    public static void setLiveUploadId(String uploadID, MongoDatabase database){
        MongoCollection<Document> configCollection = database.getCollection("config");
        configCollection.deleteMany(exists("liveUploadId"));
        configCollection.insertOne(new Document().append("liveUploadId", uploadID));
    }

    /**
     * Creates a new uploadId from the time:
     * format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, seconds);
     * @return
     */
    public static String generateNewUploadId() {
        java.util.Date juDate = new Date();
        DateTime dt = new DateTime(juDate);

        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb);


        int day = dt.getDayOfMonth();
        int month = dt.getMonthOfYear();
        int year = dt.getYear();
        int hour = dt.getHourOfDay();
        int minute = dt.getMinuteOfHour();
        int seconds = dt.getSecondOfMinute();

        formatter.format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, seconds);
        return sb.toString();

    }

    /**
     * Deletes all data associated with an uploadId
     * @param uploadId
     */
    public static void clearUpload(String uploadId, MongoDatabase database)
    {
        MongoCollection<Document> plantCollection = database.getCollection("plants");
        MongoCollection<Document> commentCollection = database.getCollection("comment");
        MongoCollection<Document> bedCollection = database.getCollection("beds");

        Document uploadIdFilter = new Document();
        uploadIdFilter.put("uploadId", uploadId);

        plantCollection.deleteMany(uploadIdFilter);
        commentCollection.deleteMany(uploadIdFilter);
        bedCollection.deleteMany(uploadIdFilter);
    }

    /**
     *
     * @return a date-sorted List of all the distinct uploadIds in the DB
     */
    public static List<String> listUploadIds(MongoDatabase database) {
        MongoCollection<Document> plantCollection = database.getCollection("plants");

        AggregateIterable<Document> documents
                = plantCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$uploadId"),
                        Aggregates.sort(Sorts.ascending("_id"))
                ));
        List<String> lst = new LinkedList<>();
        for(Document d: documents) {
            lst.add(d.getString("_id"));
        }
        return lst;
    }

    /**
     * Get a JSON document of an array of uploadIds.
     * @param database
     * @return
     */
    public static String listUploadIdsJSON(MongoDatabase database) {
        return JSON.serialize(listUploadIds(database));
    }

    /**
     * The primary method of getting the liveUploadId
     * @param database the database to get the liveUploadId from within its' config collection
     * @return
     */
    public static String getLiveUploadId(MongoDatabase database) {
        MongoCollection<Document> configCollection = database.getCollection("config");
        try
        {
            FindIterable<Document> findIterable = configCollection.find(exists("liveUploadId"));
            Iterator<Document> iterator = findIterable.iterator();
            if(!iterator.hasNext())
                return null;
            Document doc = iterator.next();
            return doc.getString("liveUploadId");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println(" [hint] Database might be empty? Couldn't getLiveUploadId");
            throw e;
        }
    }

    /**
     * Checks whether the given uploadId is an uploadId that the system knows about.
     * @param database the database to check within (in config collection)
     * @param uploadId the uploadId to check
     * @return
     */
    public static boolean isValidUploadId(MongoDatabase database, String uploadId)
    {
        List<String> uploadIds = listUploadIds(database);
        return uploadIds.contains(uploadId);
    }


}
