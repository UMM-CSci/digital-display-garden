/**
 * Tests the PlantCollection class to ensure that it handles plant data correctly.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
import {Plant} from "../src/plant";
import {PlantCollection} from "../src/plantcollection";

describe("Test Plant Collection", () => {

    /**
     * Ensures that a PlantCollection can be created properly.
     * @author Iteration 2 - Team Omar Anwar
     * @editor Iteration 3 - Team Revolver en Guardia
     */
    it("Check PlantCollection Construction", () => {
        let plants: Plant[] = [];
        let plant1: Plant = new Plant("PlantID1", "CommonName1", "Cultivar1", "Source1", "GardenLocation1"),
            plant2: Plant = new Plant("PlantID2", "CommonName2", "Cultivar2", "Source2", "GardenLocation2"),
            plant3: Plant = new Plant("PlantID3", "CommonName3", "Cultivar3", "Source3", "GardenLocation3"),
            plant4: Plant = new Plant("PlantID4", "CommonName4", "Cultivar4", "Source4", "GardenLocation4");
        plants.push(plant1);
        plants.push(plant2);
        plants.push(plant3);

        let plantCollection: PlantCollection = new PlantCollection(plants);

        expect(plantCollection.getPlants().length).toBe(3);

        expect(plantCollection.getPlants().indexOf(plant1)).toBeGreaterThan(-1);
        expect(plantCollection.getPlants().indexOf(plant2)).toBeGreaterThan(-1);
        expect(plantCollection.getPlants().indexOf(plant3)).toBeGreaterThan(-1);

        expect(plantCollection.getPlants().indexOf(plant4)).toBe(-1);
    });
});
