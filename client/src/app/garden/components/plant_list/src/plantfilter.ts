/**
 * Will filter any given collection of plants based on the provided methods.
 * Used for future reuse and extensibility of filtering support.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Revolver en Guardia
 */
import {Plant} from "./plant";

export class PlantFilter {

    /**
     * Denotes that no filter should be applied
     */
    public static readonly NO_FILTER: string = "ALL";

    /**
     * Filters the provided plant collection by the provided bed name.
     * @param bedName - the bed name to filter by
     * @param plants - the plants array to filter
     * @returns {Plant[]} - the filtered plant array
     */
    public static filterByBedName(bedName: string, plants: Plant[]): Plant[]{

        // If do not filter original plant list
        if(bedName == PlantFilter.NO_FILTER)
            return plants;

        // Else, apply filter
        let filteredPlants: Plant[] = [];

        plants.forEach((plant, index) => {
            if (plant.gardenLocation == bedName) {
                filteredPlants.push(plant);
            }
        });

        return filteredPlants;
    }

    /**
     * Filters the provided plant collection by the provided common name.
     * @param commonName - the common name to filter by
     * @param plants - the plants array to filter
     * @returns {Plant[]} - the filtered plant array
     */
    public static filterByCommonName(commonName: string, plants: Plant[]): Plant[]{

        // If do not filter original plant list
        if(commonName == PlantFilter.NO_FILTER)
            // Return original plant list
            return plants;

        // Else, apply filter
        let filteredPlants: Plant[] = [];

        plants.forEach((plant, index) => {
            if (plant.commonName == commonName)
                filteredPlants.push(plant);
        });

        return filteredPlants;
    }

    /**
     * Filters the provided plant collection by the provided common name.
     * @param terms - the search terms to filter by
     * @param plants - the plants array to filter
     * @returns {Plant[]} - the filtered plant array
     */
    public static filterByTerms(terms: string, plants: Plant[]): Plant[]{

        // If do not filter original plant list
        if(terms == PlantFilter.NO_FILTER)
        // Return original plant list
            return plants;

        // Else, apply filter
        let filteredPlants: Plant[] = [];
        let termList = terms.split(" ");
        console.log(termList);

        plants.forEach((plant, index) => {
            //Loop through terms,

            //A plant passes on if ALL of the terms match something:
            //Common name, cultivar or bed
            let b : number = 0;
            termList.forEach((term, index) =>
            {
                term = term.toLocaleLowerCase();
                if (plant.commonName.toLocaleLowerCase().includes(term) ||
                    plant.cultivar.toLocaleLowerCase().includes(term) ||
                    plant.gardenLocation.toLocaleLowerCase().includes(term)) //Beds have letters sometimes
                    b++;
            });

            if(b == termList.length)
                filteredPlants.push(plant);
        });

        return filteredPlants;
    }
}