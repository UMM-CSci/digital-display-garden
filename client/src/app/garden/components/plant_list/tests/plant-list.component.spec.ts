/**
 * Tests the PlantListComponent class.
 * Currently no tests are implemented as the previous refactoring left the class with
 * no testable code.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
import { ComponentFixture, TestBed, async } from "@angular/core/testing";
import { Observable } from "rxjs";
import {PlantListComponent} from "../src/plant-list.component";
import {PlantListService} from "../src/plant-list.service";
import {Plant} from "../src/plant";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";

describe("Test PlantListComponent", () => {

    let plantList: PlantListComponent;
    let fixture: ComponentFixture<PlantListComponent>;

    let plantListServiceStub: {
        getPlants: () => Observable<Plant[]>
    };

    beforeEach(() => {
        plantListServiceStub = {
            getPlants: () => Observable.of([
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
                ])
        };

        TestBed.configureTestingModule({
            declarations: [ PlantListComponent ],
            providers:    [ { provide: PlantListService, useValue: plantListServiceStub } ],
            imports:      [RouterTestingModule, FormsModule]
        })
    });

    beforeEach(async(() => {
        TestBed.compileComponents().then(() => {
            fixture = TestBed.createComponent(PlantListComponent);
            plantList = fixture.componentInstance;
            fixture.detectChanges();
        });
    }));

    // No tests required because the recent refactoring left the class with nothing testable.
    // This testing class is left as a placeholder for future possible tests.

});
