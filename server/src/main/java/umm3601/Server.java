package umm3601;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.bson.Document;
import spark.Route;
import spark.utils.IOUtils;
import com.mongodb.util.JSON;
import umm3601.digitalDisplayGarden.Authentication.Auth;
import umm3601.digitalDisplayGarden.Authentication.Cookie;
import umm3601.digitalDisplayGarden.Authentication.ExpiredTokenException;
import umm3601.digitalDisplayGarden.Authentication.UnauthorizedUserException;
import umm3601.digitalDisplayGarden.BedController;
import umm3601.digitalDisplayGarden.GardenCharts;
import umm3601.digitalDisplayGarden.PlantController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.*;

import umm3601.digitalDisplayGarden.ExcelParser;
import umm3601.digitalDisplayGarden.QRCodes;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;


public class Server {

    public static String PUBLIC_URL;

    private static String clientId;
    private static String clientSecret;
    private static String callbackURL;

    public static String databaseName;
    public static MongoDatabase database;
    public static int serverPort;

    private static String excelTempDir = "/tmp/digital-display-garden";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {


        String configFileLocation;
        if (args.length == 0) {
            configFileLocation = "config.properties";
        } else {
            configFileLocation = args[0];
        }
        readConfig(configFileLocation);

        port(serverPort);

        // This users looks in the folder `public` for the static web artifacts,
        // which includes all the HTML, CSS, and JS files generated by the Angular
        // build. This `public` directory _must_ be somewhere in the classpath;
        // a problem which is resolved in `server/build.gradle`.
        staticFiles.location("/public");

        MongoClient client = new MongoClient();
        database = client.getDatabase(databaseName);

        PlantController plantController = new PlantController(database);
        GardenCharts chartMaker = new GardenCharts(database);
        BedController bedController = new BedController(database);
        Auth auth = new Auth(clientId, clientSecret, callbackURL);

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

        before((request, response) -> {
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Allow-Origin", PUBLIC_URL);
        });


        // Redirects for the "home" page
        redirect.get("", "/");

        Route clientRoute = (req, res) -> {
            InputStream stream = plantController.getClass().getResourceAsStream("/public/index.html");
            return IOUtils.toString(stream);
        };
        Route notFoundRoute = (req, res) -> {
            res.type("text");
            res.status(404);
            return "Sorry, we couldn't find that!";
        };

        get("/", clientRoute);


        /*///////////////////////////////////////////////////////////////////
         * BEGIN AUTHENTICATION ENDPOINTS
         *////////////////////////////////////////////////////////////////////

        get("callback", (req, res) ->{
            Map<String, String[]> params = req.queryMap().toMap();
            String[] states = params.get("state");
            String[] codes = params.get("code");
            String[] errors = params.get("error");
            if (null == states) {
                // we REQUIRE that we be passed a state
                halt(400);
                return ""; // never reached
            }

            if (null == codes ) {
                if (null == errors) {
                    // we don't have codes, but we don't have an error either, so this a garbage request
                    halt(400);
                    return ""; // never reached
                }
                else if ("access_denied".equals(errors[0])) {
                    // the user clicked "deny", so send them to the visitor page
                    res.redirect(PUBLIC_URL);
                    return ""; // send an empty body back on redirect
                }
                else {
                    // an unknown error was passed to us, so we halt
                    halt(400);
                    return ""; // not reached
                }
            }
            String state = states[0];
            String code = codes[0];

            try {
                String originatingURL = auth.verifyCallBack(state, code);
                if (null != originatingURL) {
                    Cookie c = auth.getCookie();
                    res.cookie(c.name, c.value, c.max_age);
                    res.redirect(originatingURL);
                    System.out.println("good");
                    return ""; // not reached
                } else {
                    System.out.println("bad");
                    res.status(403);
                    return "?????"; // todo: return a reasonable message
                }
            } catch (UnauthorizedUserException e) {
                res.redirect(PUBLIC_URL + "/admin/incorrectAccount");
                return ""; // not reached
            } catch (ExpiredTokenException e) {
                // send the user to a page to tell them to login faster
                res.redirect(PUBLIC_URL + "/admin/slowLogin");
                return ""; // send empty body on redirect
            }
        });

        get("api/check-authorization", (req, res) -> {
            res.type("application/json");
            res.header("Cache-Control","no-cache, no-store, must-revalidate");
            String cookie = req.cookie("ddg");
            Document returnDoc = new Document();
            returnDoc.append("authorized", auth.authorized(cookie));
            return JSON.serialize(returnDoc);
        });

        get("api/authorize", (req,res) -> {
            String originatingURLs[] = req.queryMap().toMap().get("originatingURL");
            String originatingURL;
            if (originatingURLs == null) {
                originatingURL = PUBLIC_URL;
            } else {
                originatingURL = originatingURLs[0];
            }
            res.redirect(auth.getAuthURL(originatingURL));
            // I think we could return an arbitrary value since the redirect prevents this from being used
            return res;
        });



        /*///////////////////////////////////////////////////////////////////
         * END AUTHENTICATION ENDPOINTS
         *////////////////////////////////////////////////////////////////////
        /*///////////////////////////////////////////////////////////////////
         * BEGIN VISITOR ENDPOINTS
         *////////////////////////////////////////////////////////////////////

        // Return all plants
        get("api/plants", (req, res) -> {
            res.type("application/json");
            return plantController.listPlants(req.queryMap().toMap(), getLiveUploadId());
        });

        //Get a plant by plantId
        get("api/plant/:bed/:plantID", (req, res) -> {
            res.type("application/json");
            String id = req.params("plantID");
            String bed = req.params("bed");
            return plantController.getPlantByPlantID(id, bed, getLiveUploadId());
        });

        //Get feedback counts for a plant
        get("api/plant/:bed/:plantID/counts", (req, res) -> {
            res.type("application/json");
            String id = req.params("plantID");
            String bed = req.params("bed");
            return plantController.getPlantFeedbackByPlantIdJSON(id, bed, getLiveUploadId());
        });

        //List all Beds
        get("api/gardenLocations", (req, res) -> {
            res.type("application/json");
            return plantController.getGardenLocationsJSON(getLiveUploadId());
        });

        //List all Common Names
        get("api/commonNames", (req, res) -> {
            res.type("application/json");
            return plantController.getCommonNamesJSON(getLiveUploadId());
        });

        //Post req to rate a plant
        post("api/plant/rate", (req, res) -> {
            System.out.println("api/plant/rate " + req.body());
            res.type("application/json");
            return plantController.addFlowerRating(req.body(),getLiveUploadId());
        });


        //Post req to tell Server that a bed was visited
        post("api/bedVisit", (req, res) -> {
            res.type("application/json");
            String body = req.body();
            //Increment bedCount
            bedController.addBedVisit(body, getLiveUploadId());
            return true;
        });

        //Post req to tell Server that a bed was visited by reading a QR code
        post("api/qrVisit", (req, res) -> {
            res.type("application/json");
            String body = req.body();

            //Increment bedCount
            //Increment qrForBedCount
            bedController.addBedQRVisit(body, getLiveUploadId());
            return true;
        });

        //Posting a comment
        post("api/plant/leaveComment", (req, res) -> {
            res.type("application/json");
            return plantController.storePlantComment(req.body(), getLiveUploadId());
        });

        /*///////////////////////////////////////////////////////////////////
         * END VISITOR ENDPOINTS
         *////////////////////////////////////////////////////////////////////
        /*///////////////////////////////////////////////////////////////////
         * ADMIN ENDPOINTS
         *////////////////////////////////////////////////////////////////////


        // Accept an xls file
        post("api/admin/import", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }

                res.type("application/json");
                try {
                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement(excelTempDir);
                    req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
                    Part part = req.raw().getPart("file[]");

                    ExcelParser parser = new ExcelParser(part.getInputStream(), database);

                    String id = ExcelParser.generateNewUploadId();
                    String[][] excelFile = parser.parseExcel();
                    parser.populateDatabase(excelFile, id);
                    System.out.println(id);
                    return JSON.serialize(id);

                } catch (NotOfficeXmlFileException e) {
                    res.status(500);
//                return "{" +
//                           "\"error\" : \"Was not an XLSX File\"," +
//                           "\"code\":\"NOT_XLSX\"" +
//                       "}";
                    throw e;
                } catch (NullPointerException e) {
                    res.status(500);
                    e.printStackTrace();
//                return "{" +
//                        "\"error\" : \"Empty post request\"," +
//                        "\"code\":\"MALFORMED_REQUEST\"" +
//                        "}";
                    throw e;
                } catch (Exception e) {
                    res.status(500);
                    e.printStackTrace();
//                    return "{" +
//                            "\"error\" : \"Exception thrown in api/admin/import\"," +
//                            "\"code\":\"ERROR\"" +
//                            "}";
                    throw e;
                } catch (Error e) {
                    res.status(500);
                    e.printStackTrace();
                    throw e;
                }

        });


        //Patch from spreadsheet
        post("api/admin/patch", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }

                res.type("application/json");
                try {

                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement(excelTempDir);
                    req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
                    Part part = req.raw().getPart("file[]");

                    ExcelParser parser = new ExcelParser(part.getInputStream(), database);

                    String oldUploadId = getLiveUploadId();
                    String newUploadId = ExcelParser.generateNewUploadId();
                    String[][] excelFile = parser.parseExcel();
                    parser.patchDatabase(excelFile, oldUploadId, newUploadId);

                    return JSON.serialize(newUploadId);

                } catch (NotOfficeXmlFileException e) {
                    res.status(500);
//                return "{" +
//                           "\"error\" : \"Was not an XLSX File\"," +
//                           "\"code\":\"NOT_XLSX\"" +
//                       "}";
                    throw e;
                } catch (NullPointerException e) {
                    res.status(500);
                    e.printStackTrace();
//                return "{" +
//                        "\"error\" : \"Empty post request\"," +
//                        "\"code\":\"MALFORMED_REQUEST\"" +
//                        "}";
                    throw e;
                } catch (Exception e) {
                    res.status(500);
                    e.printStackTrace();
//                    return "{" +
//                            "\"error\" : \"Exception thrown in api/admin/import\"," +
//                            "\"code\":\"ERROR\"" +
//                            "}";
                    throw e;
                } catch (Error e) {
                    res.status(500);
                    e.printStackTrace();
                    throw e;
                }

        });

        delete("api/deleteData/:uploadID", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            String uploadID = req.params("uploadID");
            try {
                return JSON.serialize(plantController.deleteUploadID(uploadID));
            } catch (IllegalStateException e) {
                Document failureStatus = new Document();
                failureStatus.append("message", e.getMessage());
                res.status(400);
                return JSON.serialize(failureStatus);
            }
        });

        get("api/admin/export", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                res.redirect(auth.getAuthURL(PUBLIC_URL + "/admin/exportData"));
                return res; // not reached
            } else {

                // Note that after flush() or close() is called on
                // res.raw().getOutputStream(), the response can no longer be
                // modified. Since writeComment(..) closes the OutputStream
                // when it is done, it needs to be the last line of this function.
                //REVISED to attempt to fix bug where first write always breaks.
                // If an exception is thrown (specifically within workbook.write() within complete() in FeedbackWriter
                // This loop will attempt to write feedback twice, writing to an intermediate buffer.
                // If the write succeeds, then write it to the response output stream
                int error = 6;
                while (error > 0) {
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
                        if (error == 0) {
                            //If all attempts fail, produce an Internal Server Error 500
                            throw e;

                        }
                    }
                }
                return res;
            }
        });

        // List all uploadIds
        get("api/admin/uploadIds", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return ExcelParser.listUploadIdsJSON(database);
        });

        //Get a file attatchment of a .zip archive of QR codes
        get("api/admin/qrcodes", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                res.redirect(auth.getAuthURL(PUBLIC_URL + "/admin"));
            }

            res.type("application/zip");

            //Creates a Zip file, found at zipPath
            String liveUploadID = getLiveUploadId();
            String zipPath = QRCodes.createQRCodesFromAllBeds(
                    liveUploadID,
                    plantController.getGardenLocations(liveUploadID),
                    PUBLIC_URL + "/bed/");

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

        //Get a string representing the liveUploadId
        get("api/admin/liveUploadId", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return JSON.serialize(getLiveUploadId());
        });


        /*///////////////////////////////////////////////////////////////////
            BEGIN CHARTS
        *////////////////////////////////////////////////////////////////////

        //Get the ViewsPerHour chart
        get("api/admin/charts/viewsPerHour", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return chartMaker.getPlantViewsPerHour(getLiveUploadId());
        });

        //Get the data to put in the plant metadata map
        get("api/admin/charts/plantMetadataMap", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return chartMaker.getBedMetadataForMap(plantController, getLiveUploadId());
        });

        //Get the data to put in the plant comboChart
        //(could be refactored to be /api/admin/charts/gardenViewsComboChart)
        get("api/admin/charts/comboChart", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return chartMaker.getComboChart(getLiveUploadId());
        });

        get("api/admin/charts/plantMetadataBubbleMap", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            return chartMaker.getBedMetadataForBubbleMap(plantController, bedController, getLiveUploadId());
        });

        get("api/admin/charts/top20Likes", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            String type = "likes";
            return chartMaker.top20Charts(plantController, getLiveUploadId(), type);
        });

        get("api/admin/charts/top20disLikes", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            String type = "dislikes";
            return chartMaker.top20Charts(plantController, getLiveUploadId(), type);
        });

        get("api/admin/charts/top20Comments", (req, res) -> {
            String cookie = req.cookie("ddg");
            if(!auth.authorized(cookie)) {
                halt(403);
            }
            res.type("application/json");
            String type = "comments";
            return chartMaker.top20Charts(plantController, getLiveUploadId(), type);
        });


        //Host the aerial image of the Garden
        get("api/admin/gardenPicture", (req, res) -> {
            res.type("application/png");
            String gardenPath = "/Garden.png";

            return plantController.getClass().getResourceAsStream(gardenPath);
        });

        /*///////////////////////////////////////////////////////////////////
            END CHARTS
        *////////////////////////////////////////////////////////////////////
        /*///////////////////////////////////////////////////////////////////
         * END ADMIN ENDPOINTS
         */ ///////////////////////////////////////////////////////////////////

        // requests starting with 'api' should always be handled
        // by Spark, so if we haven't found a match yet we
        // return a 404 error
        get("api/*", notFoundRoute);

        get("/*", clientRoute);

        // Handle "404" file not found requests:
        notFound(notFoundRoute);
    }


    public static void readConfig(String configFileLocation) {
        try {
            InputStream input = new FileInputStream(configFileLocation);
            Properties props = new Properties();
            props.load(input);

            clientId = props.getProperty("clientID");
            if (null == clientId) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: clientID property not found (Google OAuth Client ID)");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }
            clientSecret = props.getProperty("clientSecret");
            if (null == clientSecret) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: clientSecret propetry not found (Google OAuth Client secret)");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }
            PUBLIC_URL = props.getProperty("publicURL");
            if (null == PUBLIC_URL) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: publicURL property not found");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }
            callbackURL = props.getProperty("callbackURL");
            if (null == PUBLIC_URL) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: callbackURL property not found");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }
            databaseName = props.getProperty("databaseName");
            if (null == databaseName) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: databaseName property not found");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }

            String serverPort = props.getProperty("serverPort");
            if (null == serverPort) {
                System.err.println("Failed reading config.properties file");
                System.err.println("Reason: serverPort property not found");
                System.err.println("See Documentation/ServerConfiguration.md for more information");
                System.exit(1);
            }
            else
            {
                try {
                    Server.serverPort = Integer.parseInt(serverPort);
                }
                catch(NumberFormatException nfe)
                {
                    System.err.println("Failed reading config.properties file");
                    System.err.println("Reason: The serverPort was not a valid number(" + serverPort + ")");
                    System.exit(1);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Failed to open the config file for reading");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Failed to read the config file");
            System.exit(1);
        }
    }

    public static String getLiveUploadId()
    {
        return ExcelParser.getLiveUploadId(database);
    }
}
