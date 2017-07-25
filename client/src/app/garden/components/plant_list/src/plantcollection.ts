/**
 * Represents a collection of plants.
 * Used for future reuse and extensibility of manipulating Plant data.
 *
 * @author Iteration 2 - Team Omar Anwar
 */
import {Plant} from "./plant";

export class PlantCollection {

    constructor(private plants: Plant[]){ }

    /**
     * Returns the stored plant collection
     * @returns {Plant[]} - the plant collection
     */
    public getPlants(): Plant[]{
        return this.plants;
    }
}