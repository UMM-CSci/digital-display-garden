/**
 * Tests the PlantFilter class to ensure that it filters plant data correctly.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
import {PlantFilter} from "../src/plantfilter";
import {Plant} from "../src/plant";

describe("Test Plant Filter", () => {

    /**
     * Tests that the no filter flag is set correctly.
     * @author Iteration 2 - Team Omar Anwar
     * @editor Iteration 3 - Team Revolver en Guardia
     */
    it("Check No Filter Flag", () => {
        expect(PlantFilter.NO_FILTER).toBe("ALL");
    });

    /**
     * Tests proper filtering by bed name.
     * @author Iteration 2 - Team Omar Anwar
     */
    it("Filter by Bed Name", () => {
        let plants: Plant[] = [];
        plants.push(new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "BedName1"));
        plants.push(new Plant("PlantID2", "CommonName2", "Cultivar2", "Source2", "BedName1"));
        plants.push(new Plant("PlantID3", "CommonName3", "Cultivar3", "Source3", "BedName2"));

        let filteredPlants: Plant[] = PlantFilter.filterByBedName("BedName1", plants);
        expect(filteredPlants.length).toBe(2);

        filteredPlants = PlantFilter.filterByBedName("BedName2", plants);
        expect(filteredPlants.length).toBe(1);
    });

    /**
     * Tests proper filtering by common name.
     * @author Iteration 3 - Team Revolver en Guardia
     */
    it("Filter by Common Name", () => {
        let plants: Plant[] = [];
        plants.push(new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "BedName1"));
        plants.push(new Plant("PlantID2", "CommonName1", "Cultivar2", "Source2", "BedName2"));
        plants.push(new Plant("PlantID3", "CommonName2", "Cultivar3", "Source3", "BedName3"));
        plants.push(new Plant("PlantID4", "CommonName3", "Cultivar2", "Source4", "BedName4"));
        plants.push(new Plant("PlantID5", "CommonName2", "Cultivar3", "Source5", "BedName5"));
        plants.push(new Plant("PlantID6", "CommonName2", "Cultivar3", "Source6", "BedName6"));

        let filteredPlants: Plant[] = PlantFilter.filterByCommonName("CommonName1", plants);
        expect(filteredPlants.length).toBe(2);

        filteredPlants = PlantFilter.filterByCommonName("CommonName2", plants);
        expect(filteredPlants.length).toBe(3);

        filteredPlants = PlantFilter.filterByCommonName("CommonName3", plants);
        expect(filteredPlants.length).toBe(1);
    });

    /**
     * Tests proper filtering by common name.
     * @author Iteration 3 - Team Revolver en Guardia
     */
    it("Filter by Search Terms", () => {
        let plants: Plant[] = [];
        plants.push(new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "BedName1"));
        plants.push(new Plant("PlantID2", "CommonName1", "Caltivar2", "Source2", "BedName2"));
        plants.push(new Plant("PlantID3", "CommonName2", "Cultivar3", "Source3", "BedName3"));
        plants.push(new Plant("PlantID4", "CommonName3", "Caltivar2", "Source4", "BedName4"));
        plants.push(new Plant("PlantID5", "CommonName2", "Cultivar3", "Source5", "BedName5"));
        plants.push(new Plant("PlantID6", "CommonName2", "Caltivar3", "Source6", "BedName6"));

        let filteredPlants: Plant[] = PlantFilter.filterByTerms("altiva BedName", plants);
        expect(filteredPlants.length).toBe(3);

        filteredPlants = PlantFilter.filterByTerms("CommonName2", plants);
        expect(filteredPlants.length).toBe(3);

        filteredPlants = PlantFilter.filterByTerms("Cultivar Name1", plants);
        expect(filteredPlants.length).toBe(1);
    });

});
