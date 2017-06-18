package umm3601.digitalDisplayGarden;


import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import java.util.Date;
import java.util.Iterator;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Projections.fields;
import static umm3601.digitalDisplayGarden.FeedbackWriter.*;

import java.io.IOException;

import static com.mongodb.client.model.Updates.push;
/**
 * Used to perform bed related operations on the database.
 *
 */
public class BedController {

    private final MongoCollection<Document> bedCollection;

    public BedController(MongoDatabase database) throws IOException {
        bedCollection = database.getCollection("beds");
    }

    /**
     * Increment a metadata field within the given gardenLocation for the given uploadId
     * @param gardenLocation
     * @param field
     * @param uploadId
     * @return
     */
    public boolean incrementBedMetadata(String gardenLocation, String field, String uploadId) {

        Document searchDocument = new Document();

        searchDocument.append("gardenLocation", gardenLocation);
        searchDocument.append("uploadId", uploadId);

        Bson updateDocument = inc("metadata." + field, 1);

        return null != bedCollection.findOneAndUpdate(searchDocument, updateDocument);
    }

    /**
     * When a user views a web page a Bed page a Bed Visit is added.
     * By that we mean that bed's pageViews field is incremented and a new {visit : ObjectId()} is added to visits
     * @param gardenLocation
     * @param uploadId
     * @return
     */
    public boolean addBedVisit(String gardenLocation, String uploadId) {

        Document filterDoc = new Document();
        filterDoc.append("gardenLocation", gardenLocation);
        filterDoc.append("uploadId", uploadId);

        Document visit = new Document();
        visit.append("visit", new ObjectId());
        visit.append("date", new Date());

        incrementBedMetadata(gardenLocation, "pageViews", uploadId);

        return null != bedCollection.findOneAndUpdate(filterDoc, push("metadata.bedVisits", visit));
    }

    /**
     * When a user scans a QR code, that QR code brings them to a page that sends a POST request
     * to the server whose body contains which gardenLocation was visited via QR Code.
     *
     * This function is responsible for incrementing qrScans and adding a {scan : ObjectId()} to metadata.qrVisits
     *
     * @param gardenLocation
     * @param uploadId
     * @return
     */
    public boolean addBedQRVisit(String gardenLocation, String uploadId) {

        Document filterDoc = new Document();
        filterDoc.append("gardenLocation", gardenLocation);
        filterDoc.append("uploadId", uploadId);

        Document visit = new Document();
        visit.append("visit", new Date());

        Document scan = new Document();
        scan.append("scan", new Date());

        incrementBedMetadata(gardenLocation, "pageViews", uploadId);
        incrementBedMetadata(gardenLocation, "qrScans", uploadId);

        Document a = bedCollection.findOneAndUpdate(filterDoc, push("metadata.bedVisits", visit));
        if(a == null)
            return false;
        Document b = bedCollection.findOneAndUpdate(filterDoc, push("metadata.qrVisits", scan));

        return null != b;
    }

    /**
     * Return pageViews of a bed
     * @param gardenLocation
     * @param uploadId
     * @return
     */
    public int getPageViews(String gardenLocation, String uploadId)
    {
        Document filterDoc = new Document();
        filterDoc.append("gardenLocation", gardenLocation);
        filterDoc.append("uploadId", uploadId);

        Iterator<Document> bedItr = bedCollection.find(filterDoc).iterator();

        try {
            if(bedItr.hasNext()) {
                Document bed = bedItr.next();
                Document bedMetadata = (Document)bed.get("metadata");

                return bedMetadata.getInteger("pageViews");
            }
            else throw new RuntimeException("No bed found for gardenLocation=" + gardenLocation + " uploadId=" + uploadId);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }

    }



}
