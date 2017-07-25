package umm3601.digitalDisplayGarden;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.zip.*;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class QRCodes {
    //http://javapapers.com/core-java/java-qr-code/


    public static String qrTempPath = ".qrcode";



    /**
     * Gets all beds from the Database,
     * Forms URLS for all unique beds
     * Write QRCode images to files
     * Zip together the QRCodes
     *
     * @return the path to the new .zip file or null if there was a disk IO issue
     */
    public static String createQRCodesFromAllBeds(String uploadId, String bedNames[], String urlPrefix) throws IOException,WriterException{
        String[] bedUrls = formBedURLs(bedNames,urlPrefix);
        List<BufferedImage> qrCodeImages = createBufferedImages(bedUrls);
        writeBufferedImagesToFile(qrCodeImages,bedNames,qrTempPath);

        return writeZipPathForQRCodes(uploadId, qrTempPath);
    }


    //Get all unique beds from Database
    //Create URLs for all unique beds
    public static String[] formBedURLs(String bedNames[], String urlPrefix) throws IOException {

        final int numBeds = bedNames.length;
        String bedURLs[] = new String [numBeds];
        for(int i = 0; i < numBeds; i++) {

            //Will be needed for later to count qr scans in bed metadata
            bedURLs[i] = urlPrefix + bedNames[i] + "?qr=true";
            //bedURLs[i] = urlPrefix + bedNames[i];
        }

        return bedURLs;

    }

    //Create QRCode BufferedImages for all URLs
    public static List<BufferedImage> createBufferedImages(String bedURLs[]) throws IOException, WriterException{
        final int numBeds = bedURLs.length;

        //If there are no bedURLs to make QRCodes, return empty bufferedImage[]
        if(bedURLs.length < 0)
            return new ArrayList<BufferedImage>();

        //Create BufferedImages from all the bed URLs
        List<BufferedImage> qrCodeImages = new ArrayList<BufferedImage>();
        for(int i = 0; i < numBeds; i++) {

            try {
                qrCodeImages.add(createQRFromBedURL(bedURLs[i]));
            }
            catch(IOException ioe)
            {
                //It would be really bad for a qrCodeImage to crash out and be null.
                //TODO: We should handle this better, what should we do?
                ioe.printStackTrace();
                throw ioe;
            }
            catch(WriterException we)
            {
                we.printStackTrace();
                throw we;
            }
        }

        return qrCodeImages;
    }

    public static BufferedImage createQRFromBedURL(String url) throws IOException,WriterException{

        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        return createQRCode(url, "UTF-8", hintMap, 300,300);
    }



    public static void writeBufferedImagesToFile(List<BufferedImage> bufferedImages, String[] bedNames, String path) throws IOException {

        //If the parent folder that would house the qrCodes doesn't exist,
        //try to make it
        try {
            if (Files.notExists(Paths.get(path)))
                Files.createDirectory(Paths.get(path));
        }
        catch(IOException ioe)
        {
            //We cant test this catch without filling the harddrive
            //or removing access qualifications from the server's user.
            ioe.printStackTrace();
            System.err.println("Failed to create directory for qrcode packaging.");
            throw ioe;
        }

        //WRITE IMAGES TO FILE
        try {
            for (int i = 0; i < bufferedImages.size(); i++) {
                File outputFile = new File(path + '/' + bedNames[i] + ".png");
                ImageIO.write(bufferedImages.get(i), "png", outputFile);
            }
        } catch (IOException ioe) {
            //We cant test this catch without filling the harddrive
            //or removing access qualifications from the server's user
            ioe.printStackTrace();
            System.err.println("Could not write some Images to disk, exiting.");
            throw ioe;
        }

    }


        //Zip up all BufferedImages (We have to tar/zip them up because we don't want to spam the WCROC admin with QRCode file for every bed)
        //http://www.mkyong.com/java/how-to-convert-bufferedimage-to-byte-in-java/
        //http://www.oracle.com/technetwork/articles/java/compress-1565076.html
        //create Post request to client to download zip.
        public static String writeZipPathForQRCodes(String uploadId, String path){
        //We have the images, now Zip them up!
        //ARCHIVE AND COMPRESS TO ZIP

        final int BUFFER_SIZE = 2048;

        String zipPath = "QR Code Export " + uploadId + ".zip";

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipPath);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            //out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER_SIZE];

            // get a list of files from current directory
            File f = new File("./" + path + '/');
            String files[] = f.list();

            //Add all .png files to Zip archive
            //Delete the image
            for (int i=0; i<files.length; i++) {
                if(files[i].endsWith(".png")) {

                    FileInputStream fi = new FileInputStream(path + '/' + files[i]);
                    origin = new BufferedInputStream(fi, BUFFER_SIZE);
                    ZipEntry entry = new ZipEntry(files[i]);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                    try {
                        Files.delete(Paths.get(path + '/' + files[i]));
                    }
                    catch(IOException ioe)
                    {
                        //This catch is to make sure a race condition doesn't occur where SOMEBODY
                        //deletes the path/files[i] before it's automatically deleted here
                        //The worst thing that could happen is that a file isn't deleted
                        ioe.printStackTrace();
                        System.err.println("Failed to delete QRCode image file (permissions or doesn't exist)");
                    }
                }
            }
            out.close();
        } catch(Exception e) {

            e.printStackTrace();
            zipPath = null; //Don't return a path if it never wrote the file
        }

        return zipPath;
    }



    public static BufferedImage createQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeheight, int qrCodewidth)
            throws WriterException, IOException {
        //Create the BitMatrix representing the QR code
        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(qrCodeData.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);

        //Create BufferedImages from the QRCode BitMatricies
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

}
