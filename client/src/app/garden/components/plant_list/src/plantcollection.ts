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

    /**
     * Returns the stored plant collection
     * @returns {Plant[]} - the plant collection
     */
    public getPlant(id : string, bed : string): Plant{
        for(let i = 0; i < this.plants.length; i++)
        {
            if(this.plants[i].id == id && this.plants[i].gardenLocation == bed) {
                return this.plants[i];
            }
        }
        return undefined;
    }

    /**
     * Looks for a plant that shares the same gardenLocation and ID and replaces
     * the plant with this new one.
     *
     * On success, it returns the new plant. On fail, it returns undefined.
     * @param plant
     */
    public updatePlant(plant : Plant) : Plant
    {
        for(let i = 0; i < this.plants.length; i++)
        {
            if(this.plants[i].id == plant.id && this.plants[i].gardenLocation == plant.gardenLocation) {
                this.plants[i] = plant;
                return plant;
            }
        }
        return undefined;
    }
}