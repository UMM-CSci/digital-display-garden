import { Component, OnInit } from '@angular/core';
import {AdminService} from "./admin.service";


@Component({
    selector: 'admin-component',
    templateUrl: 'admin-component.html',
})

export class AdminComponent implements OnInit {
    url : String = process.env.API_URL;
    authorized : boolean;

    constructor(private adminService: AdminService) {

    }

    ngOnInit(): void {
        this.adminService.authorized().subscribe(authorized => this.authorized = authorized);
    }
}