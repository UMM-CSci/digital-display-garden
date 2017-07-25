/**
 * Represents a Plant and all of its respective data used throughout the Digital Display Garden.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
export class Plant {

    /* _id is a unique ID stored within the database and is not shown here */
    /* _id: string; */

    /** The ID for the Plant within the spreadsheet. (Alternanthera has an ID of 16001) */
    id: string;

    /** Common name for the plant within the spreadsheet. */
    commonName: string;

    /** Cultivar for the plant within the spreadsheet. */
    cultivar: string;

    /** Source of the plant within the spreadsheet. */
    source: string;

    /** Garden Location (or Bed Name) for the plant within the spreadsheet. */
    gardenLocation: string;


    /**
     * Creates a Plant with all of the provided data.
     * @param id
     * @param commonName
     * @param cultivar
     * @param source
     * @param gardenLocation - also known as the bed name
     */
    constructor(id: string, commonName: string, cultivar: string, source: string, gardenLocation: string) {
        this.id = id;
        this.commonName = commonName;
        this.cultivar = cultivar;
        this.source = source;
        this.gardenLocation = gardenLocation;
    }

}