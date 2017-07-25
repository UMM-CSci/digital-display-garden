/**
 * Tests the Plant class to ensure that it stores plant data correctly.
 *
 * @author Iteration 3 - Team Revolver en Guardia
 */
import {Plant} from "../src/plant";

describe("Test Plant Class", () => {

    /**
     * Tests proper construction of Plant objects.
     * @author Iteration 3 - Team Revolver en Guardia
     */
    it("Check Proper Construction", () => {
        let plant1: Plant = new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "GardenLocation1"),
            plant2: Plant = new Plant("PlantID2", "CommonName2", "Cultivar2", "Source2", "GardenLocation2"),
            plant3: Plant = new Plant("PlantID3", "CommonName3", "Cultivar3", "Source3", "GardenLocation3");

        expect(plant1.id).toBe("PlantID1");
        expect(plant1.commonName).toBe("CommonName1");
        expect(plant1.cultivar).toBe("Cultivar1");
        expect(plant1.source).toBe("Source1");
        expect(plant1.gardenLocation).toBe("GardenLocation1");

        expect(plant2.id).toBe("PlantID2");
        expect(plant2.commonName).toBe("CommonName2");
        expect(plant2.cultivar).toBe("Cultivar2");
        expect(plant2.source).toBe("Source2");
        expect(plant2.gardenLocation).toBe("GardenLocation2");

        expect(plant3.id).toBe("PlantID3");
        expect(plant3.commonName).toBe("CommonName3");
        expect(plant3.cultivar).toBe("Cultivar3");
        expect(plant3.source).toBe("Source3");
        expect(plant3.gardenLocation).toBe("GardenLocation3");
    });
});
