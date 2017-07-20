/**
 * Tests the CommonNameFilter class to ensure that it filters common name data properly.
 *
 * @author Iteration 4 - Team Revolver en Guardia++
 */
import {CommonName} from "../src/common-name";
import {Plant} from "../../plant_list/src/plant";
import {Bed} from "../../bed_dropdown/src/bed";
import {CommonNameFilter} from "../src/common-name-filter";

describe("Test Common Name Filter", () => {

    /**
     * Checks that the Common Name filter properly filters Common Names
     * by the provided Bed.
     * @author Iteration 4 - Team Revolver en Guardia++
     */
    it("Test Filter Common Names By Bed", () => {

        let plants: Plant[] = [];
        plants.push(new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "Bed1", ".photos/someUploadId/10/12345.jpg"));
        plants.push(new Plant("PlantID2", "CommonName2", "Cultivar2", "Source2", "Bed1", ""));
        plants.push(new Plant("PlantID3", "CommonName3", "Cultivar3", "Source3", "Bed2", ""));
        plants.push(new Plant("PlantID4", "CommonName2", "Cultivar4", "Source4", "Bed1",""));
        plants.push(new Plant("PlantID5", "CommonName1", "Cultivar5", "Source5", "Bed3", ""));

        let commonNames: CommonName[] = [];
        let commonName1: CommonName = new CommonName("CommonName1"),
            commonName2: CommonName = new CommonName("CommonName2"),
            commonName3: CommonName = new CommonName("CommonName3");
        commonNames.push(commonName1);
        commonNames.push(commonName2);
        commonNames.push(commonName3);

        let bed1: Bed = new Bed("Bed1"),
            bed2: Bed = new Bed("Bed2"),
            bed3: Bed = new Bed("Bed3");

        let filteredCommonNames: CommonName[] = [];
        filteredCommonNames = CommonNameFilter.filterByBed(plants, commonNames, bed1);
        expect(filteredCommonNames.length).toBe(2);
        expect(filteredCommonNames[0]._id).toBe("CommonName1");
        expect(filteredCommonNames[1]._id).toBe("CommonName2");

        filteredCommonNames = [];
        filteredCommonNames = CommonNameFilter.filterByBed(plants, commonNames, bed2);
        expect(filteredCommonNames.length).toBe(1);
        expect(filteredCommonNames[0]._id).toBe("CommonName3");

        filteredCommonNames = [];
        filteredCommonNames = CommonNameFilter.filterByBed(plants, commonNames, bed3);
        expect(filteredCommonNames.length).toBe(1);
        expect(filteredCommonNames[0]._id).toBe("CommonName1");

    });

});
