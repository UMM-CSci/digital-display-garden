//Credit to http://stackoverflow.com/questions/36352405/file-upload-with-angular2-to-rest-api/39862337#39862337
//Thanks Brother Woodrow!
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Http } from '@angular/http';

@Component({
    selector: 'file-upload',
    template: '<input type="file" [multiple]="multiple" #fileInput>'
})
export class FileUploadComponent {
    @Input() multiple: boolean = false;
    @ViewChild('fileInput') inputEl: ElementRef;

    private url: string = process.env.API_URL;

    constructor(private http: Http) {}

    uploadForImport() {
        let inputEl: HTMLInputElement = this.inputEl.nativeElement;
        let fileCount: number = inputEl.files.length;
        let formData = new FormData();
        if (fileCount > 0) { // a file was selected
            for (let i = 0; i < fileCount; i++) {
                formData.append('file[]', inputEl.files.item(i));
            }
            return this.http.post(this.url + "admin/import", formData, {withCredentials: true});
        }
    }

    uploadForPatch() {
        let inputEl: HTMLInputElement = this.inputEl.nativeElement;
        let fileCount: number = inputEl.files.length;
        let formData = new FormData();
        if (fileCount > 0) { // a file was selected
            for (let i = 0; i < fileCount; i++) {
                formData.append('file[]', inputEl.files.item(i));
            }
            return this.http.post(this.url + "admin/patch", formData, {withCredentials: true});
        }
    }

    uploadPhoto(id, bed) {
        let inputEl: HTMLInputElement = this.inputEl.nativeElement;
        let fileCount: number = inputEl.files.length;
        let formData = new FormData();
        if (fileCount > 0) { // a file was selected
            for (let i = 0; i < fileCount; i++) {
                formData.append('file[]', inputEl.files.item(i));
            }
            return this.http.post(this.url + "admin/plant/" + bed + '/' + id + "/importPhoto", formData, {withCredentials: true});
        }
    }
}