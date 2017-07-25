/**
 * Represents a Plant detail page that contains all information related to the current Plant.
 *
 * @author Iteration 1 - Team Rayquaza
 * @editor Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 * @editor Iteration 4 - Team Revolver en Guardia++
 */
import {Component, OnInit } from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {Plant} from './plant';
import {PlantService} from './plant.service';
import 'rxjs/add/operator/switchMap';
import {PlantFeedback} from "./plant-feedback";

@Component({
    selector: 'plant-component',
    templateUrl: 'plant.component.html'
})
export class PlantComponent implements OnInit {

    // Has the current PlantComponent been liked or disliked?
    public rated: Boolean = false;

    // Is there a current submit to the server for rating submission?
    public ratingInTransit: Boolean = false;

    // Has the current PlantComponent been commented on?
    private commented: Boolean = false;

    // Placeholder plant for loading Plant data for the PlantComponent
    private plant: Plant = {id: "", commonName: "", cultivar: "", source: "", gardenLocation: "", photoPath:""};

    // Tracks the feedback for the current plant
    private plantFeedback: PlantFeedback = new PlantFeedback();

    private readonly URL: string = process.env.API_URL;

    /**
     * Creates a new PlantComponent that uses a PlantService for requesting Plant data. Also,
     * has a route to provide routing to specific PlantComponent pages that have unique URLs based on
     * the plant id.
     * @param plantService - the PlantService that provides data
     * @param route - the routing service that routes to specific PlantComponent pages
     */
    constructor(private plantService: PlantService,
                private route: ActivatedRoute ) { }


    /**
     * On initialization generates the URL with the proper plant ID. Also helps to
     * populate the PlantComponent page with the Plant data referenced by the id and
     * track feed back data
     *
     * For example, a URL for the Alternanthera would be http://localhost:9000/plant/16001
     */
    ngOnInit(): void {
        // Get the actual plant
        this.route.params
            .switchMap((params: Params) => this.plantService.getPlantById(params['id'], params['bed']))
            .subscribe(plant => this.plant = plant);

        // Get the feedback data for the plant
        this.route.params
            .switchMap((params: Params) => this.plantService.getFeedbackForPlantByPlantID(params['id'], params['bed']))
            .subscribe((plantFeedback: PlantFeedback) => this.plantFeedback = plantFeedback);
    }

    /**
     * Rates the currently loaded Plant that is displayed on the PlantComponent with either a like or dislike.
     * @param rating - the rating to be provided
     */
    public rate(rating: boolean): void {
        if (!this.rated) {
            this.ratingInTransit = true;
            this.plantService.ratePlant(this.plant.id, this.plant.gardenLocation, rating)
                .subscribe(succeeded => {
                    this.rated = succeeded;
                    this.requestFeedbackFromServer();
                    this.ratingInTransit = false;
                });
        }
    }

    /**
     * Comments on the currently loaded Plant that is displayed on the PlantComponent.
     * @param comment - the comment to be added to the plant
     */
    public comment(comment: string): void {
        if (!this.commented) {
            if (comment != null) {
                this.plantService.commentPlant(this.plant.id, this.plant.gardenLocation, comment)
                    .subscribe(succeeded => this.commented = succeeded)
                this.commented = true;
            }
        }
    }

    /**
     * Request PlantFeedback data for the current plant from the server.
     */
    private requestFeedbackFromServer(): void {
        this.route.params
            .switchMap((params: Params) => this.plantService.getFeedbackForPlantByPlantID(params['id'], params['bed']))
            .subscribe((plantFeedback: PlantFeedback) => this.plantFeedback = plantFeedback);
    }

    /**
     * Returns if the current plant was rated or not.
     * Is used primarily for testing.
     * @returns {Boolean}
     */
    public isRated(): Boolean{
        return this.rated;
    }

    /**
     * Returns if the current plant was commented or not.
     * Is used primarily for testing.
     * @returns {Boolean}
     */
    public isCommented(): Boolean{
        return this.commented;
    }

    /**
     * Returns the width of the like bar within the rating bar.
     * @returns {number} - like bar width
     */
    public getLikeWidth(): number{
        let likes: number = this.plantFeedback.likeCount,
            dislikes: number = this.plantFeedback.dislikeCount;

        return likes / (likes + dislikes) * 100;
    }

    /**
     * Returns the width of the dislike bar within the rating bar.
     * @returns {number} - dislike bar width
     */
    public getDislikeWidth(): number{
        let likes: number = this.plantFeedback.likeCount,
            dislikes: number = this.plantFeedback.dislikeCount;

        return dislikes / (likes + dislikes) * 100;
    }
}

