package umm3601.admin;
import com.google.zxing.WriterException;
import org.junit.After;
import org.junit.Test;
import umm3601.digitalDisplayGarden.QRCodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static umm3601.digitalDisplayGarden.QRCodes.*;

/**
 * Created by Brian on 4/5/2017.
 */
public class TestQRCodes {

    String Test_Url = "http://localhost:2538" ;
    String path = "qrtest";


    @Test
    public void TestCreateQRCodesFromAllBeds()throws IOException,WriterException{
        String bedNames[] = new String[1];
        bedNames[0] = "bed1";
        String urlPrefix = this.Test_Url+ "/bed/";
        String[] theBedURl = formBedURLs(bedNames,urlPrefix);
        assertTrue("these should be the same url for test", theBedURl[0].equals("http://localhost:2538/bed/bed1?qr=true"));

        List<BufferedImage> qrCodeImages = new ArrayList<BufferedImage>();
        for(int i = 0; i < 1; i++) {
            qrCodeImages.add(createQRFromBedURL(theBedURl[i]));
        }

        writeBufferedImagesToFile(qrCodeImages,bedNames,path);

        String uploadID = "Upload ID";
        String zipPathFromBed = writeZipPathForQRCodes(uploadID,path);


        String zipPathFromURlPrefix = createQRCodesFromAllBeds(uploadID,bedNames,urlPrefix);


        assertEquals("The correct path should be ", zipPathFromURlPrefix,zipPathFromBed);
        assertFalse("This is the wrong upload Id ", createQRCodesFromAllBeds("wrong upload id",bedNames,urlPrefix).equals(zipPathFromBed));

    }

    @Test
    public void TestFormBedURLs() throws IOException, WriterException{
        String bedNames[] = new String[4];
        bedNames[0] = "bed1";
        bedNames[1] = "bed2";
        bedNames[2] = "bed3";
        bedNames[3] = "bed4";

        String urlPrefix = this.Test_Url+ "/bed/";


        //Check to see that it makes a bed url of length 4
        assertEquals("this should be 4",4, QRCodes.formBedURLs(bedNames,urlPrefix).length);
        assertEquals("this should be a url to bed1","http://localhost:2538/bed/bed1?qr=true", QRCodes.formBedURLs(bedNames,urlPrefix)[0]);
    }

    @Test
    public void TestCreateBufferedImages() throws IOException,WriterException{
        String bedURLs[] = new String[2];
        bedURLs[0]= "http://localhost:2538/bed/bed1?qr=true";
        bedURLs[1] = "http://localhost:2538/bed/bed2?qr=true";
        System.out.println(QRCodes.createBufferedImages(bedURLs).get(0));

        List<BufferedImage> qrCodeImages = new ArrayList<BufferedImage>();
        for(int i = 0; i < 2; i++) {
            qrCodeImages.add(createQRFromBedURL(bedURLs[i]));
        }

        //test to see if the width is 300 of the created buffered image
        assertEquals("they should return same buffered images", qrCodeImages.get(0).toString().contains("width = 300"),QRCodes.createBufferedImages(bedURLs).get(0).toString().contains("width = 300") );
    }

    @Test
    public void TestWriteZipPathForQRCodes() throws IOException,WriterException{
        String bedNames[] = new String[2];
        bedNames[0] = "bed1";
        bedNames[1] = "bed2";

        String bedURLs[] = new String[2];
        bedURLs[0] = "http://localhost:2538/bed/bed1?qr=true";
        bedURLs[1] = "http://localhost:2538/bed/bed2?qr=true";
        List<BufferedImage> qrCodeImages = QRCodes.createBufferedImages(bedURLs);

        assertEquals(qrCodeImages.size(), 2);

        QRCodes.writeBufferedImagesToFile(qrCodeImages,bedNames,this.path);

        String uploadId = "first uploadId";

        File f = new File(this.path);
        String files[] = f.list();
        Arrays.sort(files);
        for(int i = 0; i < files.length; i++)
            assertEquals("File names aren't equal to the bed names", bedNames[i] + ".png", files[i]);


        // Writes it to the right path.
        String qr = QRCodes.writeZipPathForQRCodes(uploadId,path);
        assertEquals("File output names should be the same","QR Code Export " + "first uploadId" + ".zip",qr);

    }

    @After
    public void cleanFiles() throws IOException
    {
        try {
            //Delete temp folder holding QRCodes
            File f = new File(path);
            String files[] = f.list();
            for (int i = 0; i < files.length; i++) {
                Files.delete(Paths.get(path + "/" + files[i]));
            }

            Path tempFolderPath = Paths.get(this.path);
            if (Files.exists(tempFolderPath))
                Files.delete(tempFolderPath);

            //Delete QRCode zip file
            f = new File("./");
            files = f.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".zip") && files[i].startsWith("QR Code Export"))
                    Files.delete(Paths.get(files[i]));
            }
        }
        catch(Exception e)
        {
            //Failed deleting a folder
            e.printStackTrace();
        }
    }


}
