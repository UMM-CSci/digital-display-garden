package umm3601;

import spark.Route;
import spark.utils.IOUtils;
import com.mongodb.util.JSON;
import umm3601.digitalDisplayGarden.BedController;
import umm3601.digitalDisplayGarden.GardenCharts;
import umm3601.digitalDisplayGarden.PlantController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

import umm3601.digitalDisplayGarden.ExcelParser;
import umm3601.digitalDisplayGarden.QRCodes;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;


public class Server {

    public static final String API_URL = "http://localhost:2538";

    public static String databaseName = "test";

    private static String excelTempDir = "/tmp/digital-display-garden";

    public static void main(String[] args) throws IOException {

        port(2538);

//        ExcelParser parser = new ExcelParser("/AccessionList2016.xlsx");
//        parser.parseExcel("Today's Database");

        // This users looks in the folder `public` for the static web artifacts,
        // which includes all the HTML, CSS, and JS files generated by the Angular
        // build. This `public` directory _must_ be somewhere in the classpath;
        // a problem which is resolved in `server/build.gradle`.
        staticFiles.location("/public");

        PlantController plantController = new PlantController(databaseName);
        GardenCharts chartMaker = new GardenCharts(databaseName);
        BedController bedController = new BedController(databaseName);

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
 
            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        // Redirects for the "home" page
        redirect.get("", "/");

        Route clientRoute = (req, res) -> {
            InputStream stream = plantController.getClass().getResourceAsStream("/public/index.html");
            return IOUtils.toString(stream);
        };

        get("/", clientRoute);

        // List plants
        get("api/plants", (req, res) -> {
            res.type("application/json");
            return plantController.listPlants(req.queryMap().toMap(), getLiveUploadId());
        });

        //Get a plant
        get("api/plant/:plantID", (req, res) -> {
            res.type("application/json");
            String id = req.params("plantID");
            return plantController.getPlantByPlantID(id, getLiveUploadId());
        });

        //Get feedback counts for a plant
        get("api/plant/:plantID/counts", (req, res) -> {
            res.type("application/json");
            String id = req.params("plantID");
            return plantController.getFeedbackForPlantByPlantID(id, getLiveUploadId());
        });

//        //Get feedback counts for a plant
//        get("api/plant/:plantID/counts", (req, res) -> {
//            res.type("application/json");
//            String id = req.params("plantID");
//            return plantController.getJSONFeedbackForPlantByPlantID(id, plantController.getLiveUploadId());
//        });

        //List all Beds
        get("api/gardenLocations", (req, res) -> {
            res.type("application/json");
            return plantController.getGardenLocationsAsJson(getLiveUploadId());
        });

        //List all Common Names
        get("api/commonNames", (req, res) -> {
            res.type("application/json");
            return plantController.getCommonNamesAsJson(getLiveUploadId());
        });

        // List all uploadIds
        get("api/uploadIds", (req, res) -> {
            res.type("application/json");
            return ExcelParser.listUploadIds(databaseName);
        });

        post("api/plant/rate", (req, res) -> {
            System.out.println("api/plant/rate " + req.body());
            res.type("application/json");
            return plantController.addFlowerRating(req.body(),getLiveUploadId());
        });

        get("api/export", (req, res) -> {
            // Note that after flush() or close() is called on
            // res.raw().getOutputStream(), the response can no longer be
            // modified. Since writeComment(..) closes the OutputStream
            // when it is done, it needs to be the last line of this function.
            //REVISED to attempt to fix bug where first write always breaks.
            // If an exception is thrown (specifically within workbook.write() within complete() in FeedbackWriter
            // This loop will attempt to write feedback twice, writing to an intermediate buffer.
            // If the write succeeds, then write it to the response output stream
            int error = 3;
            while(error > 0) {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    plantController.writeFeedback(buffer, req.queryMap().toMap().get("uploadId")[0]);

                    res.type("application/vnd.ms-excel");
                    res.header("Content-Disposition", "attachment; filename=\"Garden-Visitor data.xlsx\"");

                    OutputStream out = res.raw().getOutputStream();
                    out.write(buffer.toByteArray());
                    out.flush();
                    out.close();
                    error = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    error--;
                    if(error == 0)
                    {
                        //If all attempts fail, produce an Internal Server Error 500
                        throw e;
                    }
                }
            }


            return res;
        });

        get("api/liveUploadId", (req, res) -> {
            res.type("application/json");
            return JSON.serialize(getLiveUploadId());
        });

        post("api/bedVisit", (req, res) -> {
            res.type("application/json");
            String body = req.body();
            //Increment bedCount
            bedController.addBedVisit(body, getLiveUploadId());
            return true;
        });

        post("api/qrVisit", (req, res) -> {
            res.type("application/json");
            String body = req.body();

            //Increment bedCount
            //Increment qrForBedCount
            bedController.addBedQRVisit(body, getLiveUploadId());
            return true;
        });


        get("api/qrcodes", (req, res) -> {
            res.type("application/zip");

            String liveUploadID = getLiveUploadId();
            System.err.println("liveUploadID=" + liveUploadID);
            String zipPath = QRCodes.CreateQRCodesFromAllBeds(
                    liveUploadID,
                    plantController.getGardenLocations(liveUploadID),
                    API_URL + "/bed/");
            System.err.println(zipPath);
            if(zipPath == null)
                return null;

            res.header("Content-Disposition","attachment; filename=\"" + zipPath + "\"");

            //Get bytes from the file
            File zipFile = new File(zipPath);
            byte[] bytes = spark.utils.IOUtils.toByteArray(new FileInputStream(zipFile));

            //Delete local .zip file
            Files.delete(Paths.get(zipPath));

            return bytes;
        });

        get("api/admin/gardenPicture", (req, res) -> {
            res.type("application/png");

            String gardenPath = "/Garden.png";


            //res.header("Content-Disposition","filename=\"" + "Garden.png" + "\"");

            return plantController.getClass().getResourceAsStream(gardenPath);

        });

        // Posting a comment
        post("api/plant/leaveComment", (req, res) -> {
            res.type("application/json");
            return plantController.storePlantComment(req.body(), getLiveUploadId());
        });

        // Views per Hour
        get("api/chart/viewsPerHour", (req, res) -> {
            res.type("application/json");
            return chartMaker.getViewsPerHour(getLiveUploadId());
        });

        get("api/chart/plantMetadataMap", (req, res) -> {
            res.type("application/json");

            return chartMaker.getBedMetadataForMap(plantController, getLiveUploadId());
        });

        // Accept an xls file
        post("api/import", (req, res) -> {

            res.type("application/json");
            try {

                MultipartConfigElement multipartConfigElement = new MultipartConfigElement(excelTempDir);
                req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
                Part part = req.raw().getPart("file[]");

                ExcelParser parser = new ExcelParser(part.getInputStream(), databaseName);

                String id = ExcelParser.generateNewUploadId();
                String[][] excelFile = parser.parseExcel();
                parser.populateDatabase(excelFile, id);

                return JSON.serialize(id);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        });


        // Accept an xls file
        post("api/patch", (req, res) -> {

            res.type("application/json");
            try {

                MultipartConfigElement multipartConfigElement = new MultipartConfigElement(excelTempDir);
                req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
                Part part = req.raw().getPart("file[]");




                ExcelParser parser = new ExcelParser(part.getInputStream(), databaseName);

                String oldUploadId = getLiveUploadId();
                String newUploadId = ExcelParser.generateNewUploadId();
                String[][] excelFile = parser.parseExcel();
                parser.patchDatabase(excelFile, oldUploadId, newUploadId);

                return JSON.serialize(newUploadId);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        });


        get("/*", clientRoute);

        // Handle "404" file not found requests:
        notFound((req, res) -> {
            res.type("text");
            res.status(404);
            return "Sorry, we couldn't find that!";
        });
    }

    public static String getLiveUploadId()
    {
        return ExcelParser.getLiveUploadId(databaseName);
    }
}
