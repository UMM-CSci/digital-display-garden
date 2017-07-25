package umm3601.digitalDisplayGarden;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Photos {

    private final MongoCollection<Document> plantCollection;
    private final PlantController plantController;

    public Photos(MongoCollection<Document> plantCollection, PlantController plantController) {

        this.plantCollection = plantCollection;
        this.plantController = plantController;
    }


    public boolean savePhoto(String plantId, String gardenLocation, RenderedImage photo, String uploadID){
        try {
            if (Files.notExists(Paths.get(".photos/"))){
                Files.createDirectory(Paths.get(".photos/"));
            }

            String photosPath = ".photos/"+uploadID;
            if (Files.notExists(Paths.get(photosPath))) {
                Files.createDirectory(Paths.get(photosPath));
            }

            String[] beds = plantController.getGardenLocations(uploadID);

            for(String bed: beds)
            {
                String bedPath = photosPath + '/' + bed;
                if (Files.notExists(Paths.get(bedPath))) {
                    Files.createDirectory(Paths.get(bedPath));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            String filePath = ".photos/" + uploadID + '/' + gardenLocation + '/' + plantId + ".jpeg";
            File outputFile = new File(filePath);
            String relPath = outputFile.getPath();
            ImageIO.write(photo, "jpeg", outputFile);


            Document filterDoc = new Document();
            filterDoc.append("id", plantId);
            filterDoc.append("gardenLocation", gardenLocation);
            filterDoc.append("uploadId", uploadID);

            Document photoPath = new Document();
            photoPath.append("photoPath", relPath);

            plantCollection.findOneAndUpdate(filterDoc,new Document("$set", photoPath));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Could not write some Images to disk, exiting.");
            return false;
        }
        return true;
    }



}
