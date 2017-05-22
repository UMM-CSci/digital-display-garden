/**
 * Provides the ability to request Plant data from a server and to also post metadata such as likes, dislikes
 * and comments for specific plants to said server.
 *
 * @author Iteration 1 - Team Rayquaza
 * @editor Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 * @editor Iteration 4 - Team Revolver en Guardia++
 */

import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Plant } from './plant';
import { Observable } from "rxjs";
import {PlantFeedback} from "./plant-feedback";

@Injectable()
export class PlantService {

    private readonly URL: string = API_URL + "plant/";

    constructor(private http:Http) { }

    /**
     * Request that the database send over the plant specified by the provided id.
     * @param id - the id of the requested plant
     * @returns {Observable<Plant>} - upon successful respons from the server, an observable of the returned plant
     */
    getPlantById(id: string, bed: string): Observable<Plant> {
        return this.http.request(this.URL + bed + "/" + id).map(res => res.json());
    }

    /**
     * Rates the plant specified by the provided id with the provided rating.
     * @param id - the id of the plant to be rated
     * @param bed - the garden location of the plant
     * @param rating - the rating to rate the plant with
     * @returns {Observable<Boolean>} - true if the plant was successfully rated
     *                                - false if the plant rating failed
     */
    ratePlant(id: string, bed: string, rating: boolean): Observable<Boolean> {
        let ratingObject = {
            id: id,
            gardenLocation: bed,
            like: rating
        };
        return this.http.post(this.URL + "rate", JSON.stringify(ratingObject)).map(res => res.json());
    }

    /**
     * Comments on the plant specified by the provided id with the provided comment.
     * @param id
     * @param bed
     * @param comment
     * @returns {Observable<Boolean>} - true if the plant was successfully commented on
     *                                - false if the plant commenting failed
     */
    commentPlant(id: string, bed: string, comment: string): Observable<Boolean> {
        let returnObject = {
            plantId: id,
            gardenLocation: bed,
            comment: comment
        };
        return this.http.post(this.URL + "leaveComment", JSON.stringify(returnObject)).map(res => res.json());
    }

    /**
     * Requests the PlantFeedback data for a plant of id be retrieved from the server.
     * @param bed - the garden location of the plant to leave feedback on
     * @param id - the id of the plant to receive feedback for
     * @returns {Observable<PlantFeedback>}
     */
    getFeedbackForPlantByPlantID(id: string, bed: string): Observable<PlantFeedback> {
        return this.http.request(this.URL + bed + "/" + id + "/counts").map(res => res.json());
    }

}