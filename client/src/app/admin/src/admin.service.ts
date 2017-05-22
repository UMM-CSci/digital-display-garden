import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from "rxjs";

@Injectable()
export class AdminService {
    private url: string = API_URL;
    constructor(private http:Http) { }

    //Authentication HTTP requests

    authorized() : Observable<boolean> {
        return this.http.get(this.url + "check-authorization", {withCredentials: true}).map(res => res.json().authorized);
    }

    //Upload ID HTTP requests

    getUploadIds(): Observable<string[]> {
        return this.http.request(this.url + "admin/uploadIds").map(res => res.json());
    }

    getLiveUploadId(): Observable<string> {
        return this.http.request(this.url + "admin/liveUploadId").map(res => res.json());
    }

    //Google Charts HTTP requests

    getViewsPerHour(): Observable<any[][]> {
        return this.http.request(this.url + "admin/charts/viewsPerHour").map(res => res.json())
    }

    getComboChart(): Observable<any [][]> {
        return this.http.request(this.url + "admin/charts/comboChart").map(res => res.json());
    }

    getBedMetadataForMap(): Observable<any[]> {
        return this.http.request(this.url + "admin/charts/plantMetadataMap").map(res => res.json())
    }

    getBedMetadataForBubble(): Observable<any[]> {
        return this.http.request(this.url + "admin/charts/plantMetadataBubbleMap").map(res => res.json())
    }

    get20MostLikes(): Observable<any[]> {
        return this.http.request(this.url + "admin/charts/top20Likes").map(res => res.json())
    }

    get20MostDisLikes(): Observable<any[]> {
        return this.http.request(this.url + "admin/charts/top20disLikes").map(res => res.json())
    }

    get20MostComments(): Observable<any[]> {
        return this.http.request(this.url + "admin/charts/top20Comments").map(res => res.json())
    }
}
