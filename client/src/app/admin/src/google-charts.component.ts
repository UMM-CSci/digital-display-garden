import {Component, OnInit} from '@angular/core';
import {AdminService} from "./admin.service";
import {Observable} from "rxjs";

@Component({
    templateUrl: 'google-charts.component.html'
})

// Component class
export class GraphComponent implements OnInit {

    authorized: boolean;

    constructor(private adminService: AdminService) {
    }


    ngOnInit(): void {
        this.updateLineChart();
        this.updateBarChart();
        this.updateComboChart();
        this.updateMap();
        this.updateBubble();
        this.updatetop20likes();
        this.updatetop20dislikes();
        this.updatetop20comments();
        this.adminService.authorized().subscribe(authorized => this.authorized = authorized);
    }

    public updateLineChart(): void{
        this.adminService.getViewsPerHour()
            .subscribe(result => {
                this.line_ChartData["dataTable"] = result;
                this.line_ChartData = Object.create(this.line_ChartData);
            }, err => console.log(err));
    }

    public updateBarChart(): void{

        this.adminService.getViewsPerHour()
            .subscribe(result => {
                this.columnChartOptions["dataTable"] = result;
                this.columnChartOptions = Object.create(this.columnChartOptions);
            }, err => console.log(err));
    }

    public updateComboChart(): void {
        this.adminService.getComboChart()
            .subscribe(result => {
                this.comboChart["dataTable"] = result;
                this.comboChart = Object.create(this.comboChart);
            })
    }

    public updateMap(): void{
        this.adminService.getBedMetadataForMap()
            .subscribe(result => {
                this.mapOptions.dataTable = this.createDataTableMap(GraphComponent.processMapData(result));
                this.mapOptions = Object.create(this.mapOptions);
            }, err => console.log(err));
    }

    public updateBubble(): void{
        this.adminService.getBedMetadataForBubble()
            .subscribe(result => {
                this.bubbleOption.dataTable = this.createDataTableBubble(result);
                this.bubbleOption = Object.create(this.bubbleOption);
            }, err => console.log(err));
    }

    public updatetop20likes(): void{
        this.adminService.get20MostLikes()
            .subscribe(result => {
                    this.top20ChartDataLikes["dataTable"] = GraphComponent.createDataTableTop20(result);
                    this.top20ChartDataLikes = Object.create(this.top20ChartDataLikes);
            }, err => console.log(err));
    }

    public updatetop20dislikes(): void{
        this.adminService.get20MostDisLikes()
            .subscribe(result => {
                    this.top20ChartDataDisLikes["dataTable"] = GraphComponent.createDataTableTop20(result);
                    this.top20ChartDataDisLikes = Object.create(this.top20ChartDataDisLikes);
            }, err => console.log(err));
    }

    public updatetop20comments(): void{
        this.adminService.get20MostComments()
            .subscribe(result => {
                this.top20ChartDataComments["dataTable"] = GraphComponent.createDataTableTop20(result);
                this.top20ChartDataComments = Object.create(this.top20ChartDataComments);
            }, err => console.log(err));
    }

    public top20ChartDataLikes = {
        chartType: `ColumnChart`,
        dataTable: [['Hour', 'Views'],
            ['Sample Data',  0], ['1',  0], ['2',  0], ['3',  0], ['4',  0], ['5',  0], ['6',  0], ['7',  0], ['8',  0], ['9',  0], ['10',  0], ['11',  0], ['12',  0],
            ['13',  0], ['14',  0], ['15',  0], ['16',  0], ['17',  0], ['18',  0], ['19',  0], ['20',  0], ['21',  0], ['22',  0], ['23',  0],
        ],
        options: {'title': 'Top 20 Plants with Most Likes', hAxis : {'title' :'Cultivar Name'}, vAxis : {'title' :'Amount of Likes'}, legend:{position: 'none'},
            height: 400,
            bar: {groupWidth: '20%'}},
    };

    public top20ChartDataDisLikes = {
        chartType: `ColumnChart`,
        dataTable: [['Hour', 'Views'],
            ['0',  0], ['1',  0], ['2',  0], ['3',  0], ['4',  0], ['5',  0], ['6',  0], ['7',  0], ['8',  0], ['9',  0], ['10',  0], ['11',  0], ['12',  0],
            ['13',  0], ['14',  0], ['15',  0], ['16',  0], ['17',  0], ['18',  0], ['19',  0], ['20',  0], ['21',  0], ['22',  0], ['23',  0],
        ],
        options: {'title': 'Top 20 Plants with Most DisLikes', hAxis : {'title' :'Cultivar Name'}, vAxis : {'title' :'Amount of Dislikes'}, legend:{position: 'none'},
            height: 400, bar: {groupWidth: '20%'}},
    };

    public top20ChartDataComments = {
        chartType: `ColumnChart`,
        dataTable: [['Hour', 'Views'],
            ['0',  0], ['1',  0], ['2',  0], ['3',  0], ['4',  0], ['5',  0], ['6',  0], ['7',  0], ['8',  0], ['9',  0], ['10',  0], ['11',  0], ['12',  0],
            ['13',  0], ['14',  0], ['15',  0], ['16',  0], ['17',  0], ['18',  0], ['19',  0], ['20',  0], ['21',  0], ['22',  0], ['23',  0],
        ],
        options: {'title': 'Top 20 Plants with Most Comments', hAxis : {'title' :'Cultivar Name'}, vAxis : {'title' :'Amount of Comments'}, legend:{position: 'none'},
            height: 400, bar: {groupWidth: '20%'}},
    };

    public line_ChartData = {
        chartType: `AreaChart`,
        dataTable: [['Hour', 'Views'],
            ['0',  0], ['1',  0], ['2',  0], ['3',  0], ['4',  0], ['5',  0], ['6',  0],
            ['7',  0], ['8',  0], ['9',  0], ['10',  0], ['11',  0], ['12',  0], ['13',  0],
            ['14',  0], ['15',  0], ['16',  0], ['17',  0], ['18',  0], ['19',  0], ['20',  0], ['21',  0], ['22',  0], ['23',  0],
        ],
        options: {'title': 'Flower View Counts over Time', hAxis : {'title' :'Time (hours)'}, vAxis : {'title' :'Flower View Counts'}},
    };


    public columnChartOptions = {
        chartType: `ColumnChart`,
        dataTable: [['Hour', 'Views'],
            ['0',  0], ['1',  0], ['2',  0], ['3',  0], ['4',  0], ['5',  0], ['6',  0], ['7',  0], ['8',  0], ['9',  0], ['10',  0], ['11',  0], ['12',  0],
            ['13',  0], ['14',  0], ['15',  0], ['16',  0], ['17',  0], ['18',  0], ['19',  0], ['20',  0], ['21',  0], ['22',  0], ['23',  0],
        ],
        options: {'title': 'Flower View Counts over Time', hAxis : {'title' :'Time (hours)'}, vAxis : {'title' :'Flower View Counts'}},
    };

    public comboChart = {
        chartType: 'ComboChart',
        dataTable: [['Hour', 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Average'],
                    ['1',  0  ,      0,         1,             1,           0,      5, 1, 2],
                    ['2',  0  ,      2,         0,             0,           1,      2, 1, 0],
                    ['3',  0,        0,         0,             0,           0,      0, 0, 0],
                    ['4',  0,        0,         1,             0,           0,      0, 0, 0],
                    ['5',  6,        2,         2,             1,           2,      2, 4, 2],
                    ['6',  7,        3,         2,             3,           2,      3, 8, 4],
                    ['7',  8,        6,         5,             8,           2,      7, 7, 6],
                    ['8',  10,       4,         7,             8,           7,      5, 12, 7],
                    ['9',  25,       10,         17,          23,          18,      17, 30, 20],
                    ['10',  32,      15,         27,          31,          20,      27, 37, 27],
                    ['11',  50,      52,         57,             59,          57,      52, 54, 54],
                    ['12',  65,      70,         75,             60,          75,      70, 77, 70],
                    ['1',  97,       100,         118,             120,          118,      100, 106, 108],
                    ['2',  105,      68,         82,             97,          69,      82, 110, 87],
                    ['3',  85,       60,         75,             90,          55,      76, 95, 76],
                    ['4',  66,       50,         59,             62,          52,      60, 66, 59],
                    ['5',  55,       45,         57,             58,          50,      55, 62, 54],
                    ['6',  46,      39,         40,             45,          39,      50, 58, 45],
                    ['7',  39,      29,         34,             35,          32,      37, 41, 35],
                    ['8',  20,      15,         17,             18,          14,      22, 26, 18],
                    ['9',  11,      5,         8,             9,          6,      12, 13, 9],
                    ['10',  5,      1,         3,             5,          1,      2, 5, 3],
                    ['11',  4,      1,         2,             2,          2,      2, 2, 2]],
        options: {
            title: 'Combo Chart - Hour of the day vs. Views',
            //backgroundColor: `#ECF0F1`,
            backgroundColor: `#D2D7D9`,
            height: 400,
            hAxis: {title: 'Time (In hours)'},
            vAxis: {title: 'Views',
                    viewWindow: {min: -1},},
            seriesType: `bars`,
            series: {
                // 0: {color: `#3498DB`},
                // 1: {color: `#3498DB`},
                // 2: {color: `#317EB3`},
                // 3: {color: `#30719F`},
                // 4: {color: `#2F648B`},
                // 5: {color: `#2E5777`},
                // 6: {color: `#2D4A63`},
                // 7: {color: `#E74C3C`, type: `line`},
                0: {color: `#1E3D5A`},
                1: {color: `#303C54`},
                2: {color: `#423C4F`},
                3: {color: `#543B4A`},
                4: {color: `#663B45`},
                5: {color: `#783A40`},
                6: {color: `#8B3A3B`},
                7: {color: `#60BF17`,
                    curveType: `function`,
                    lineWidth: 3,
                    type: `line`},
            }
        },
    };


    //Latitude and Longitude of POSSIBLE gardenLocations (TODO:confirm locations with customer)
    public bedLocations = [[45.593692, -95.874986], [45.593413, -95.874997], [45.593629, -95.875193], [45.593462, -95.875180],
        [45.593700, -95.875485], [45.593712, -95.875634], [45.593300, -95.875595], [45.593514, -95.875944], [45.593196, -95.875566],
        [45.593286, -95.876253], [45.593274, -95.876567], [45.593323, -95.875212]];

    //TODO: Same as previous todo, these are kind of random bubbles
    public bedLocationsForBubble = [[15,40], [15,60], [22,41], [23,55], [32,37], [37,37],
        [36,68], [47,51], [32,71], [58,70], [68,68], [22,68]];

    public mapOptions = {
        chartType: `Map`,
        dataTable: [['Lat', 'Long', 'Views'],
            [this.bedLocations[0][0], this.bedLocations[0][1], 'Update Graph'],
        ],
        options: {'zoomLevel' : '18', title : false, showInfoWindow: true}
    };

    public bubbleOption = {
        chartType: `BubbleChart`,
        dataTable: [['Bed Number', 'X', 'Y', 'Likes (Colour)', 'Views (Size)'],
            ['1N',    36,      33,                1,             1],
            ['1S',    37,      25,                1,             1],
            ['2N',    49,      16,                1,             1],
            ['2S',    54,      56,                1,             1],
            ['5',     15,      60,                1,             1],
            ['6',    61,      40,                1,             1],
            ['7',    19,      14,                1,             1],
            ['9' ,    30,       4,                1,             1],
            ['10' ,    34,      10,                1,             1],
            ['11' ,    42,    25.5,                1,             1],
            ['13' ,    46.5,    29,                1,             1],
            ['LG',    46.1,  45.3,                1,             1]
        ],
        options: {
            backgroundColor: 'none',
            width: 1074,
            height: 636,
            chartArea: {
                left: 0,
                top: 0,
                width: '100%',
                height: '100%'
            },
            hAxis: {
                title: null,
                gridlines: {count: 0},
                minValue: 0,
                maxValue: 100,
                viewWindow: {min: 0, max: 100}},
            vAxis: {
                title: null,
                gridlines: {count: 0},
                minValue: 0,
                maxValue: 100,
                viewWindow: {min: 0, max: 100}},
            colorAxis: {colors: ['blue', 'purple']},
            bubble: {
                textStyle: {
                    fontSize: 12,
                    color: `white`,
                },
            },
        }
    };





    /**
     * Creates the tooltip HTML for each of the beds.
     * mapData is data from the Server that is an array of objects that look like
     * {likes : number, disklikes : number, comments : number}
     *
     * This function creates a pretty HTML tooltip that is displayed when you click on a bed in the Google zMap
     * @param mapData
     * @returns {Array<string>}
     */
    private static processMapData(mapData : any[]) : string[]
    {
        let buffer : Array<string> = new Array<string>(mapData.length);

        for(let i : number = 0; i < mapData.length; i++)
        {
            buffer[i] =  '<h2>Bed ' + mapData[i]["gardenLocation"] + '</h2>';
            buffer[i] += '<div><strong>Likes:</strong> ' + mapData[i]["likes"]       + '<br/>';
            buffer[i] += '<div><strong>Dislikes:</strong> ' + mapData[i]["dislikes"] + '<br/>';
            buffer[i] += '<div><strong>Comments:</strong> ' + mapData[i]["comments"] + '<br/>';
            buffer[i] += '</div>';
        }

        return buffer;
    }

    private createDataTableBubble(entry : any[]) : any[][]
    {

        let dataTable : any[][] = new Array<Array<any>>(entry.length+1);

        for(let i : number = 0; i < entry.length+1; i++)
        {
            dataTable[i] = new Array<any>(5);
        }

        dataTable[0][0] = 'Bed';
        dataTable[0][1] = 'X';
        dataTable[0][2] = 'Y';
        dataTable[0][3] = 'Likes (Colour)';
        dataTable[0][4] = 'Views (Size)';
        for(let i : number = 0; i < entry.length; i++)
        {
            dataTable[i+1][0] = entry[i]['gardenLocation'];
            dataTable[i+1][1] = this.bedLocationsForBubble[i][0];
            dataTable[i+1][2] = this.bedLocationsForBubble[i][1];
            dataTable[i+1][3] = entry[i]['likes'];
            dataTable[i+1][4] = entry[i]['pageViews'];
        }
        return dataTable;

    }

    private createDataTableMap(toolWindow: string[]) : any[][]
    {
        let dataTable : any[][] = new Array<Array<any>>(toolWindow.length+1);

        for(let i : number = 0; i < toolWindow.length+1; i++)
        {
            dataTable[i] = new Array<any>(3);
        }

        dataTable[0][0] = 'Lat';
        dataTable[0][1] = 'Long';
        dataTable[0][2] = 'ToolWindow';
        for(let i : number = 0; i < toolWindow.length; i++)
        {


            dataTable[i+1][0] = this.bedLocations[i][0];
            dataTable[i+1][1] = this.bedLocations[i][1];
            dataTable[i+1][2] = toolWindow[i];
            console.log(toolWindow[i]);
        }

        return dataTable;
    }

    private static createDataTableTop20(toolWindow: string[]): any[][]{
        let dataTable: any [][] = new Array<Array<any>>(toolWindow.length+1);
        for(let i : number = 0; i < toolWindow.length+1; i++){
            dataTable[i] = new Array<any>(2);
        }
        dataTable[0][0] = "Cultivar Name";
        dataTable[0][1] = "";
        for(let i : number = 0; i < toolWindow.length; i++)
        {
            dataTable[i+1][0] = toolWindow[i]['cultivarName'];
            dataTable[i+1][1] = toolWindow[i]['likes'];
        }

        return dataTable;
    }

}
