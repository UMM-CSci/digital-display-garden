package umm3601.plant;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.FeedbackWriter;
import umm3601.digitalDisplayGarden.PlantController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static umm3601.digitalDisplayGarden.FeedbackWriter.*;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by dansa on 4/10/2017.
 */
public class TestExportFeedback {

//    private final static String databaseName = "data-for-testing-only";
//    public MongoClient mongoClient = new MongoClient();
//    public MongoDatabase testDB = mongoClient.getDatabase(databaseName);
//    private PlantController plantController;

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
    public void testExportFeedback() throws IOException {
        int plantComments = 160;
        int plantMetadata = 500;
        int bedMetadata = 500;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        FeedbackWriter feedback = new FeedbackWriter(buffer);
        String [] input = new String[COL_CMT_FIELDS];
        for(int i = 0; i < plantComments; i++) {
            input[COL_CMT_PLANTID] = "16001";
            input[COL_CMT_COMMONNAME] = "Alternathera";
            input[COL_CMT_CULTIVAR] = "Experimental";
            input[COL_CMT_GRDNLOC] = "1S";
            input[COL_CMT_COMMENT] = "What a lovely flower! " + i;
            input[COL_CMT_DATE] = "4 10 2017"; //This isn't the format
            feedback.writeToSheet(input, SHEET_COMMENTS);
        }

        input = new String[FeedbackWriter.COL_PLANT_FIELDS];
        for(int i = 0; i < plantMetadata; i++) {
            input[COL_PLANT_PLANTID] = "16001";
            input[COL_PLANT_COMMONNAME] = "Alternathera";
            input[COL_PLANT_CULTIVAR] = "Experimental";
            input[COL_PLANT_GRDNLOC] = "1S";
            input[COL_PLANT_LIKES] = Integer.toString(i);
            input[COL_PLANT_DISLIKES] = "0";
            input[COL_PLANT_COMMENTS] = "4";
            input[COL_PLANT_PAGEVIEWS] = "4";
            feedback.writeToSheet(input, SHEET_METADATA);
        }

        input = new String[FeedbackWriter.COL_BED_FIELDS];
        for(int i = 0; i < bedMetadata; i++) {
            input[COL_BED_GRDNLOC] = "1S";
            input[COL_BED_PAGEVIEWS] = "0";
            input[COL_BED_QRSCANS] = "0";
            feedback.writeToSheet(input, SHEET_BEDMETADATA);
        }

        feedback.complete();
        //Use that output stream and produce an input stream from the bytes to
        //read the spreadsheet into an XSSFWorkbook
        ByteArrayInputStream reRead = new ByteArrayInputStream(buffer.toByteArray());
        Workbook workbook = new XSSFWorkbook(reRead);
        assertEquals("Excel Spreadsheet does not contain two Sheets", workbook.getNumberOfSheets(), 3);

        Sheet sheet;

        sheet = workbook.getSheetAt(0); //Comment Sheet
        assertEquals("Comment Sheet does not contain "+ plantComments +"+2 rows",sheet.getPhysicalNumberOfRows(), plantComments+2);

        sheet = workbook.getSheetAt(1); //Metadata Sheet
        assertEquals("Metadata Sheet does not contain "+ plantMetadata + "+2 rows",sheet.getPhysicalNumberOfRows(), plantMetadata+2);

//        sheet = workbook.getSheetAt(2); //Metadata Sheet
//        assertEquals("Bed Metadata Sheet does not contain "+ bedMetadata + "+2 rows",sheet.getPhysicalNumberOfRows(), plantMetadata+2);

    }



    @Test
    public void testWriteFeedback() throws IOException {
//        byte[] emptyFeedback = null;
//        {//Scoping
//            ByteArrayOutputStream emptyFeedbackBuffer = new ByteArrayOutputStream();
//            FeedbackWriter feedbackWriter = new FeedbackWriter(emptyFeedbackBuffer);
//            feedbackWriter.complete();
//            emptyFeedback = emptyFeedbackBuffer.toByteArray();
//        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        assertFalse("Wrote feedback to an invalid uploadId", plantController.writeFeedback(buffer, "invalid uploadId"));
        buffer.close(); buffer = new ByteArrayOutputStream();
        assertTrue("Couldn't write to a valid uploadId", plantController.writeFeedback(buffer, "first uploadId"));
        buffer.close(); buffer = new ByteArrayOutputStream();
    }

    @Test
    public void testWriteFeedbackSheet() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FeedbackWriter feedback = new FeedbackWriter(buffer);

        assertTrue("Failed to write feedback to a valid uploadId", plantController.writeToBedMetadataSheet(feedback, "first uploadId"));
        assertFalse("Wrote feedback to an invalid uploadId", plantController.writeToBedMetadataSheet(feedback, "invalid uploadId"));

        assertTrue("Failed to write feedback to a valid uploadId", plantController.writeToCommentSheet(feedback, "first uploadId"));
        assertFalse("Wrote feedback to an invalid uploadId",plantController.writeToCommentSheet(feedback, "invalid uploadId"));

        assertTrue("Failed to write feedback to a valid uploadId", plantController.writeToPlantMetadataSheet(feedback, "first uploadId"));
        assertFalse("Wrote feedback to an invalid uploadId", plantController.writeToPlantMetadataSheet(feedback, "invalid uploadId"));




    }


}

