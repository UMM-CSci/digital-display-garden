/**
 * Tests the PlantComponent to check that it handles metadata correctly.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
import { ComponentFixture, TestBed, async } from "@angular/core/testing";
import { Plant } from "../src/plant";
import { PlantComponent } from "../src/plant.component";
import { Observable } from "rxjs";
import {PlantService} from "../src/plant.service";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {RouterTestingModule} from "@angular/router/testing";
import {FooterComponent} from "../../footer/src/footer.component";
import {PlantFeedback} from "../src/plant-feedback";

describe("Plant Component", () => {

    let plantComponent: PlantComponent;
    let fixture: ComponentFixture<PlantComponent>;

    let plantServiceStub: {
        ratePlant: (id: string, rating: string) => Observable<Boolean>;
        commentPlant: (id: string, comment: string) => Observable<Boolean>;
        getFeedbackForPlantByPlantID: (id: string) => Observable<PlantFeedback>;
    };

    beforeEach(() => {
        plantServiceStub = {
            ratePlant: (id: string, rating: string) => {

                let plant: Observable<Plant> = Observable.of([
                    {
                        id: "PlantID1",
                        commonName: "CommonName1",
                        cultivar: "Cultivar1",
                        source: "Source1",
                        gardenLocation: "BedName1",

                    },
                    {
                        id: "PlantID2",
                        commonName: "CommonName2,",
                        cultivar: "Cultivar2,",
                        source: "Source2,",
                        gardenLocation: "BedName2,",
                    },
                    {
                        id: "PlantID3",
                        commonName: "CommonName3",
                        cultivar: "Cultivar3",
                        source: "Source3",
                        gardenLocation: "BedName3",
                    }
                ].find(plant => plant.id === id));

                if(plant !== null)
                    return Observable.of(true);
                else
                    return Observable.of(false);

            },

            commentPlant: (id: string, comment: string) => {

                let plant: Observable<Plant> = Observable.of([
                    {
                        id: "PlantID1",
                        commonName: "CommonName1",
                        cultivar: "Cultivar1",
                        source: "Source1",
                        gardenLocation: "BedName1",

                    },
                    {
                        id: "PlantID2",
                        commonName: "CommonName2,",
                        cultivar: "Cultivar2,",
                        source: "Source2,",
                        gardenLocation: "BedName2,",
                    },
                    {
                        id: "PlantID3",
                        commonName: "CommonName3",
                        cultivar: "Cultivar3",
                        source: "Source3",
                        gardenLocation: "BedName3",
                    }
                ].find(plant => plant.id === id));

                if(plant !== null)
                    return Observable.of(true);
                else
                    return Observable.of(false);

            },

            getFeedbackForPlantByPlantID: (id: string) => {

                let plant: Observable<PlantFeedback> = Observable.of([
                    {
                        commentCount : 0,
                        likeCount : 1,
                        dislikeCount : 0,
                    }

                ].find(plantFeedback => plantFeedback.commentCount==0));

                return plant;
            }
        };

        TestBed.configureTestingModule({
            imports: [FormsModule, RouterTestingModule],
            declarations: [ PlantComponent, FooterComponent ],
            providers:    [ { provide: PlantService, useValue: plantServiceStub },
                            { provide: ActivatedRoute, useValue: { params: Observable.of( {id: "PlantID1"} ) } } ],
        })
    });

    beforeEach(async(() => {
        TestBed.compileComponents().then(() => {
            fixture = TestBed.createComponent(PlantComponent);
            plantComponent = fixture.componentInstance;
        });
    }));

    /**
     * Tests that a plant can be liked.
     * @author Iteration 2 - Team Omar Anwar
     */
    it("Can Like A Plant", () => {

        let wasRated: Boolean = plantComponent.isRated();
        expect(wasRated).toBe(false);

        plantComponent.rate(true);
        wasRated = plantComponent.isRated();
        expect(wasRated).toBe(true);

    });

    /**
     * Tests that a plant can be disliked.
     * @author Iteration 2 - Team Omar Anwar
     */
    it("Can Dislike A Plant", () => {

        let wasRated: Boolean = plantComponent.isRated();
        expect(wasRated).toBe(false);

        plantComponent.rate(false);
        wasRated = plantComponent.isRated();
        expect(wasRated).toBe(true);

    });

    /**
     * Tests that a plant can be commented on.
     * @author Iteration 2 - Team Omar Anwar
     */
    it("Can Comment On A Plant", () => {

        let wasCommented: Boolean = plantComponent.isCommented();
        expect(wasCommented).toBe(false);

        plantComponent.comment("");
        wasCommented = plantComponent.isCommented();
        expect(wasCommented).toBe(true);

    });

    /**
     * Check that rating bar widths are initialized correctly.
     * @author Iteration 4 - Team Revolver en Guardia++
     */
    it("Check Like and Dislike bar widths", () => {

        let likeWidth: number = plantComponent.getLikeWidth(),
            dislikeWidth: number = plantComponent.getDislikeWidth();

        expect(Number.isNaN(likeWidth)).toBe(true);
        expect(Number.isNaN(dislikeWidth)).toBe(true);

    });

});
