package umm3601.digitalDisplayGarden;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.bson.BsonInvalidOperationException;
import org.bson.Document;
import org.bson.types.ObjectId;

import org.bson.conversions.Bson;

import java.io.OutputStream;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Projections.fields;
import static umm3601.digitalDisplayGarden.FeedbackWriter.*;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Updates.push;

public class PlantController {

    private final MongoCollection<Document> plantCollection;
    private final MongoCollection<Document> commentCollection;
    private final MongoCollection<Document> configCollection;
    private final MongoCollection<Document> bedCollection;
    private final MongoDatabase db;

    public PlantController(MongoDatabase database) {

        plantCollection = database.getCollection("plants");
        commentCollection = database.getCollection("comments");
        configCollection = database.getCollection("config");
        bedCollection = database.getCollection("beds");
        db = database;
    }

    /**
     * Finds a plant and atomically increments the specified field
     * in its metadata object. This method returns true if the plant was
     * found successfully (false otherwise), but there is no indication of
     * whether the field was found.
     *
     * @param plantID a ID number of a plant in the DB
     * @param field a field to be incremented in the metadata object of the plant
     * @param uploadId the uploadId of the metadata field
     * @return true if a plant was found
     * @throws com.mongodb.MongoCommandException when the id is valid and the field is empty
     */
    public boolean incrementMetadata(String plantID, String field, String uploadId) {
        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;

        Document searchDocument = new Document();
        searchDocument.append("id", plantID);
        searchDocument.append("uploadId", uploadId);

        Bson updateDocument = inc("metadata." + field, 1);

        return null != plantCollection.findOneAndUpdate(searchDocument, updateDocument);
    }

    /**
     * List all plants within the database, filtered by uploadId, gardenLocation and commonName
     * @param queryParams
     * @param uploadId
     * @return
     */
    public String listPlants(Map<String, String[]> queryParams, String uploadId) {

        if (!ExcelParser.isValidUploadId(db, uploadId))
            return "null";

        //Create a filter based on query params
        Document filterDoc = new Document();
        filterDoc.append("uploadId", uploadId);

        if (queryParams.containsKey("gardenLocation")) {
            String location =(queryParams.get("gardenLocation")[0]);
            filterDoc = filterDoc.append("gardenLocation", location);
        }


        if (queryParams.containsKey("commonName")) {
            String commonName =(queryParams.get("commonName")[0]);
            filterDoc = filterDoc.append("commonName", commonName);
        }

        FindIterable<Document> matchingPlants = plantCollection.find(filterDoc);
        matchingPlants.sort(Sorts.ascending("commonName", "cultivar"));

        return JSON.serialize(matchingPlants);
    }

    /**
     * Takes a String representing an ID number of a plant
     * and when the ID is found in the database returns a JSON document
     * as a String of the following form
     *
     * <code>
     * {
     *  "plantID"        : String,
     *  "commonName" : String,
     *  "cultivar"   : String
     * }
     * </code>
     *
     * If the ID is invalid or not found, the following JSON value is
     * returned
     *
     * <code>
     *  null
     * </code>
     *
     * @param plantID an ID number of a plant in the DB
     * @param gardenLocation the location in the garden to find the plant in
     * @param uploadID Dataset to find the plant
     * @return a string representation of a JSON value
     */
    public String getPlantByPlantID(String plantID, String gardenLocation, String uploadID) {

        if (!ExcelParser.isValidUploadId(db, uploadID))
            return "null";

        FindIterable<Document> jsonPlant;
        String returnVal;
        try {
            jsonPlant = plantCollection.find(and(eq("id", plantID),
                    eq("gardenLocation", gardenLocation),
                    eq("uploadId", uploadID)))
                    .projection(fields(include("id", "commonName", "cultivar", "gardenLocation")));

            Iterator<Document> iterator = jsonPlant.iterator();

            //When you get Plant by PlantId
            //Increase the pageViews for that plant
            //Add a visit to the plant
            if (iterator.hasNext()) {
                incrementMetadata(plantID, "pageViews", uploadID);
                addVisit(plantID, gardenLocation, uploadID);
                returnVal = iterator.next().toJson();
            } else {
                returnVal = "null";
            }

        } catch (IllegalArgumentException e) {
            returnVal = "null";
        }

        return returnVal;

    }

    public static final int PLANT_FEEDBACK_LIKES = 0,
                            PLANT_FEEDBACK_DISLIKES = 1,
                            PLANT_FEEDBACK_COMMENTS = 2;

    /**
     * This gets an array of feedback counts for plants.
     * Use PLANT_FEEDBACK_LIKES, PLANT_FEEDBACK_DISLIKES and PLANT_FEEDBACK_COMMENTS
     * to index within the returned array.
     * @param plantID
     * @param uploadID
     * @return
     */
    public long[] getPlantFeedbackByPlantId(String plantID, String gardenLocation, String uploadID) {

        Document filter = new Document();
        filter.put("commentOnPlant", plantID);
        filter.put("commentInBed", gardenLocation);
        filter.put("uploadId", uploadID);
        long comments = commentCollection.count(filter);
        long likes = 0;
        long dislikes = 0;


        //Get a plant by plantID and location
        FindIterable doc = plantCollection.find(new Document().append("id", plantID).append("gardenLocation", gardenLocation).append("uploadId", uploadID));

        Iterator iterator = doc.iterator();
        if(iterator.hasNext()) {
            Document result = (Document) iterator.next();

            //Get metadata.rating array
            List<Document> ratings = (List<Document>) ((Document) result.get("metadata")).get("ratings");

            //Loop through all of the entries within the array, counting like=true(like) and like=false(dislike)
            for(Document rating : ratings)
            {
                if(rating.get("like").equals(true))
                    likes++;
                else if(rating.get("like").equals(false)) // THIS IS IMPORTANT (do not delete)
                    dislikes++;
            }
        }
        else return null;
        long[] out = new long[3];
        out[PLANT_FEEDBACK_LIKES] = likes;
        out[PLANT_FEEDBACK_DISLIKES] = dislikes;
        out[PLANT_FEEDBACK_COMMENTS] = comments;

        return out;
    }

    /**
     *
     * @param plantID The plant to get feedback of
     * @param uploadID Dataset to find the plant
     *
     * @return JSON for the number of comments, likes, and dislikes
     * Of the form:
     * {
     *     commentCount: number
     *     likeCount: number
     *     dislikeCount: number
     * }
     */

    public String getPlantFeedbackByPlantIdJSON(String plantID, String gardenLocation, String uploadID) {
        if (!ExcelParser.isValidUploadId(db, uploadID))
            return "null";
        Document out = new Document();

        //Get feedback then package it in a JSON(BSON) Document
        long[] metadataCount = getPlantFeedbackByPlantId(plantID, gardenLocation, uploadID);

        if(metadataCount == null) //invalid gardenLocation or plantId
            return "null";

        out.put("likeCount", metadataCount[0]);
        out.put("dislikeCount", metadataCount[1]);
        out.put("commentCount", metadataCount[2]);
        return JSON.serialize(out);
    }

    /**
     * Returns an array of Strings of all garden locations for the current uploadId
     * sorted according to the BedComparator
     * @param uploadID
     * @return
     */
    public String[] getGardenLocations(String uploadID){

        if (!ExcelParser.isValidUploadId(db, uploadID))
            return null;

        //Get distinct gardenLocations for this uploadId
        Document filter = new Document();
        filter.append("uploadId", uploadID);
        DistinctIterable<String>  bedIterator = plantCollection.distinct("gardenLocation", filter, String.class);
        List<String> beds = new ArrayList<String>();
        for(String s : bedIterator)
        {
            beds.add(s);
        }
        //Then sort the gardenLocations as according to BedComparator
        beds.sort(new BedComparator());
        return beds.toArray(new String[beds.size()]);
    }

    /**
     * Returns a list of all distinct plantIds in the plants collection and with the current uploadId
     * @param uploadID
     * @return
     */
    public String[] listPlantIDs(String uploadID){
        Document filter = new Document();
        filter.append("uploadId", uploadID);
        DistinctIterable<String>  idIterator = plantCollection.distinct("id", filter, String.class);
        List<String> id = new ArrayList<String>();
        for(String s : idIterator)
        {
            id.add(s);
        }

        return id.toArray(new String[id.size()]);
    }

    /**
     * List unique cultivars
     * @param uploadID
     * @return
     */
    public String[] listCultivars(String uploadID){
        Document filter = new Document();
        filter.append("uploadId", uploadID);
        DistinctIterable<String>  idIterator = plantCollection.distinct("cultivar", filter, String.class);

        List<String> id = new ArrayList<String>();
        for(String s : idIterator)
        {
            id.add(s);
        }
        id.sort(String::compareTo);
        //Then sort the gardenLocations as according to BedComparator
        return id.toArray(new String[id.size()]);
    }

    public JsonArray getGardenLocationsJSON(String uploadID){
        if (!ExcelParser.isValidUploadId(db, uploadID))
            return new JsonArray();

        //Get garden locations and package them in a JsonArray
        String[] beds = getGardenLocations(uploadID);
        JsonArray out = new JsonArray();
        for(int i = 0; i < beds.length; i++)
        {
            JsonObject bed = new JsonObject();
            bed.addProperty("_id", beds[i]);
            out.add(bed);
        }

        return out;
    }


    /**
     * Get a json containing a list of commonNames sorted by common name
     * @param uploadID
     * @return
     */
    public String getCommonNamesJSON(String uploadID){
        if (!ExcelParser.isValidUploadId(db, uploadID))
            return "null";

        AggregateIterable<Document> documents
                = plantCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(eq("uploadId", uploadID)), //!! Order is important here
                        Aggregates.group("$commonName"),
                        Aggregates.sort(Sorts.ascending("commonName"))
                ));
        return JSON.serialize(documents);
    }

    /**
     * Adds a flower visit to the database
     * for a plant by this plantID for this uploadId
     * @param plantID
     * @param uploadId
     * @return
     */
    public boolean addVisit(String plantID, String gardenLocation, String uploadId) {
        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;
        Document filterDoc = new Document();
        filterDoc.append("id", plantID);
        filterDoc.append("gardenLocation", gardenLocation);
        filterDoc.append("uploadId", uploadId);

        //Add a {visit : Date} to the visits array
        Document visit = new Document();
        visit.append("visit", new Date());

        return null != plantCollection.findOneAndUpdate(filterDoc, push("metadata.visits", visit));
    }

    /**
     * Accepts string representation of JSON object containing
     * at least the following.
     * <code>
     *     {
     *         plantId: String,
     *         gardenLocation: String,
     *         comment: String
     *     }
     * </code>
     * If either of the keys are missing or the types of the values are
     * wrong, false is returned.
     * @param json string representation of JSON object
     * @param uploadID Dataset to find the plant
     * @return true iff the comment was successfully submitted
     */

    public boolean storePlantComment(String json, String uploadID) {
        Date now = new Date();
        if (!ExcelParser.isValidUploadId(db, uploadID))
            return false;

        try {

            Document toInsert = new Document();
            Document parsedDocument = Document.parse(json);

            //If request contains plantId get the plant json by the plantId and current uploadID
            //Add a comment to commentCollection with the plantId, uploadID, and
            if (parsedDocument.containsKey("plantId") && parsedDocument.get("plantId") instanceof String
                    && parsedDocument.containsKey("gardenLocation") && parsedDocument.get("gardenLocation") instanceof String) {

                FindIterable<Document> jsonPlant = plantCollection.find(and(eq("id",
                        parsedDocument.getString("plantId")), eq("gardenLocation", parsedDocument.getString("gardenLocation")), eq("uploadId", uploadID)));

                Iterator<Document> iterator = jsonPlant.iterator();

                if(iterator.hasNext()){
                    Document plantDoc = iterator.next();
                    toInsert.put("commentOnPlant", plantDoc.getString("id"));
                    toInsert.put("commentInBed", plantDoc.getString("gardenLocation"));
                    toInsert.put("date", now);
                } else {
                    System.err.println("Was passed malformed storePlantComment request");
                    return false;
                }

            } else {
                System.err.println("storePlantComment request does not contain plantId");
                return false;
            }

            if (parsedDocument.containsKey("comment") && parsedDocument.get("comment") instanceof String) {
                toInsert.put("comment", parsedDocument.getString("comment"));
            } else {
                System.err.println("storePlantComment request does not contain comment");
                return false;
            }

            toInsert.append("uploadId", uploadID);

            commentCollection.insertOne(toInsert);

        } catch (BsonInvalidOperationException e) {
            e.printStackTrace();
            return false;
        }
        catch (org.bson.json.JsonParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Adds a like or dislike to the specified plant.
     *
     * @param plantId a hexstring specifiying the oid
     * @param like true if this is a like, false if this is a dislike
     * @param uploadID Dataset to find the plant
     * @return true iff the operation succeeded.
     */
    public boolean addFlowerRating(String plantId, String gardenLocation, boolean like, String uploadID) {

        if (!ExcelParser.isValidUploadId(db, uploadID))
            return false;

        //Get a plant by this plantId and uploadId
        Document filterDoc = new Document();
        filterDoc.append("id", plantId);
        filterDoc.append("gardenLocation", gardenLocation);
        filterDoc.append("uploadId", uploadID);

        Document plantOID;
        Iterator<Document> itr = plantCollection.find(filterDoc).iterator();
        if(itr.hasNext()) {
            plantOID = itr.next();
        }
        else
        {
            System.err.println("Plant not found with plantId " + plantId + " for uploadId " + uploadID);
            return false;
        }

        //Push a new {like : bool, date : Date, ratingOnObjectOfId : ObjectId} to metadata.ratings for this plant
        Document rating = new Document();
        rating.append("like", like);
        rating.append("date", new Date());
        rating.append("ratingOnObjectOfId", new ObjectId(plantOID.get("_id").toString()));

        return null != plantCollection.findOneAndUpdate(filterDoc, push("metadata.ratings", rating));
    }

    /**
     * Accepts string representation of JSON object containing
     * at least the following:
     * <code>
     *     {
     *         id: String,
     *         gardenLocation: String
     *         like: boolean
     *     }
     * </code>
     *
     * @param json string representation of a JSON object
     * @param uploadID Dataset to find the plant
     * @return true iff the operation succeeded.
     */
    public boolean addFlowerRating(String json, String uploadID){

        if (!ExcelParser.isValidUploadId(db, uploadID))
            return false;

        boolean like;
        String id;
        String bed;

        try {

            Document parsedDocument = Document.parse(json);

            if(parsedDocument.containsKey("id") && parsedDocument.get("id") instanceof String
                    && parsedDocument.containsKey("gardenLocation") && parsedDocument.get("gardenLocation") instanceof String){
                id = parsedDocument.getString("id");
                bed = parsedDocument.getString("gardenLocation");
            } else {
                return false;
            }

            if(parsedDocument.containsKey("like") && parsedDocument.get("like") instanceof Boolean){
                like = parsedDocument.getBoolean("like");
            } else {
                return false;
            }

        } catch (BsonInvalidOperationException e){
            return false;
        }
        catch (org.bson.json.JsonParseException e){
            return false;
        }

        return addFlowerRating(id, bed, like, uploadID);
    }



    /**
     * Write metadata and comment information to excel spreadsheet.
     * @param outputStream
     * @param uploadId
     * @return
     * @throws IOException
     */
    public boolean writeFeedback(OutputStream outputStream, String uploadId) throws IOException {

        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;

        FeedbackWriter feedbackWriter = new FeedbackWriter(outputStream);
        writeToCommentSheet(feedbackWriter, uploadId);
        writeToPlantMetadataSheet(feedbackWriter, uploadId);
        writeToBedMetadataSheet(feedbackWriter, uploadId);

        feedbackWriter.complete();
        return true;
    }

    /**
     * Write all comments to the comments sheet
     * @param feedbackWriter
     * @param uploadId
     * @return
     */
    public boolean writeToCommentSheet(FeedbackWriter feedbackWriter, String uploadId)
    {
        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;

        //Find all comments of this uploadId
        FindIterable iter = commentCollection.find(
                and(
                        exists("commentOnPlant"),
                        exists("commentInBed"),
                        eq("uploadId", uploadId)
                ));
        Iterator iterator = iter.iterator();

        //Loop through each comment and add them as entries
        while (iterator.hasNext()) {
            Document comment = (Document) iterator.next();

            //Get the plant that this comment was for
            Iterator<Document> onPlantItr = plantCollection.find(and(eq("id", comment.getString("commentOnPlant")),eq("gardenLocation", comment.getString("commentInBed")), eq("uploadId", uploadId))).iterator();

            Document onPlant;
            onPlant = onPlantItr.next(); //NOTE: this should _not_ create an exception, and if it does, the database is corrupt
            try {
                //Prepare data to write to Comment Sheet
                String[] dataToWrite = new String[COL_CMT_FIELDS];
                dataToWrite[COL_CMT_PLANTID] = onPlant.getString("id");
                dataToWrite[COL_CMT_COMMONNAME] = onPlant.getString("commonName");
                dataToWrite[COL_CMT_CULTIVAR] = onPlant.getString("cultivar");
                dataToWrite[COL_CMT_GRDNLOC] = onPlant.getString("gardenLocation");
                dataToWrite[COL_CMT_COMMENT] = comment.getString("comment");
                dataToWrite[COL_CMT_DATE] = comment.get("date").toString();

                feedbackWriter.writeToSheet(dataToWrite, FeedbackWriter.SHEET_COMMENTS);
            } catch (Exception e) {
                System.err.println("ERROR ON PLANT " + onPlant);
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Write plant metadata to the plant metadata sheet
     * @param feedbackWriter
     * @param uploadId
     * @return
     */
    public boolean writeToPlantMetadataSheet(FeedbackWriter feedbackWriter, String uploadId)
    {
        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;

        //Loop through all plants
        FindIterable iter = plantCollection.find(
                eq("uploadId", uploadId)

        );
        Iterator iterator = iter.iterator();

        while (iterator.hasNext()) {
            Document onPlant = (Document) iterator.next();

            try {
                String[] dataToWrite = new String[COL_PLANT_FIELDS];
                Document metadata = (Document) onPlant.get("metadata");
                Document feedback = Document.parse(getPlantFeedbackByPlantIdJSON(onPlant.getString("id"), onPlant.getString("gardenLocation"), uploadId));


                Integer likeCount = feedback.getInteger("likeCount");
                Integer dislikeCount = feedback.getInteger("dislikeCount");
                Integer commentCount = feedback.getInteger("commentCount");
                Integer pageViews = metadata.getInteger("pageViews");

                dataToWrite[COL_PLANT_PLANTID] = onPlant.getString("id");
                dataToWrite[COL_PLANT_COMMONNAME] = onPlant.getString("commonName");
                dataToWrite[COL_PLANT_CULTIVAR] = onPlant.getString("cultivar");
                dataToWrite[COL_PLANT_GRDNLOC] = onPlant.getString("gardenLocation");


                dataToWrite[COL_PLANT_LIKES] = likeCount.toString();
                dataToWrite[COL_PLANT_DISLIKES] = dislikeCount.toString();
                dataToWrite[COL_PLANT_COMMENTS] = commentCount.toString();
                dataToWrite[COL_PLANT_PAGEVIEWS] = pageViews.toString();

                feedbackWriter.writeToSheet(dataToWrite, FeedbackWriter.SHEET_METADATA);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("ERROR ON PLANT " + onPlant);
            }
        }
        return true;
    }

    /**
     * Write Bed metadata to the bed metadata sheet
     * @param feedbackWriter
     * @param uploadId
     * @return
     */
    public boolean writeToBedMetadataSheet(FeedbackWriter feedbackWriter, String uploadId)
    {
        if (!ExcelParser.isValidUploadId(db, uploadId))
            return false;

        FindIterable iter = bedCollection.find(
                eq("uploadId", uploadId)

        );
        Iterator iterator = iter.iterator();

        while (iterator.hasNext()) {
            Document onBed = (Document) iterator.next();

            try {
                String[] dataToWrite = new String[COL_BED_FIELDS];
                Document metadata = (Document) onBed.get("metadata");
                //Document stuff = Document.parse(getGardenLocationsJSON(onBed.getString("id")));


                Integer pageViews = metadata.getInteger("pageViews");
                Integer qrScans = metadata.getInteger("qrScans");

                dataToWrite[COL_BED_GRDNLOC] = onBed.getString("gardenLocation");
                dataToWrite[COL_BED_PAGEVIEWS] = pageViews.toString();
                dataToWrite[COL_BED_QRSCANS] = qrScans.toString();

                feedbackWriter.writeToSheet(dataToWrite, FeedbackWriter.SHEET_BEDMETADATA);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("ERROR ON BED " + onBed);
            }
        }
        return true;
    }

    /**
     * Deletes all data associated with the specified uploadID
     * in the database.
     * Returns a Document of the following form
     * <code>
     *     {
     *         success: boolean,
     *         uploadIDs: [ String, ... ]
     *     }
     * </code>
     * Success will be true if the uploadID was found and false if it wasn't.
     * <br/>
     * uploadIDs contains a list of the remaining uploadIDs in the database
     * @param uploadID the uploadID to delete
     * @return A Document specifying the status of the operation
     * @throws IllegalStateException if the specified uploadID is currently the liveUploadID
     */
    public Document deleteUploadID (String uploadID) {

        if (ExcelParser.getLiveUploadId(db).equals(uploadID)) {
            throw new IllegalStateException("The uploadID cannot be deleted because it is the liveUploadID");
        }
        Document filterDoc = new Document();
        Document returnDoc = new Document();

        filterDoc.append("uploadId", uploadID);
        boolean deleted = plantCollection.deleteMany(filterDoc).getDeletedCount() > 0;
        deleted = deleted && (bedCollection.deleteMany(filterDoc).getDeletedCount() > 0);
        commentCollection.deleteMany(filterDoc);

        returnDoc.append("success", deleted);
        returnDoc.append("uploadIDs", ExcelParser.listUploadIds(db));

        //deleteDirectory(new File(".photos/" + uploadID));
        return returnDoc;
    }


}