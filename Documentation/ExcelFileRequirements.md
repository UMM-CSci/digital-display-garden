# Excel File Requirements  
One of our priorities is to provide a modular future proof system for inputing an Excel file. We understand that the way you will want to use the Digital Dipslay Garden will evolve and that our code will have to be modular enough to keep pace. We have developed a parser capable of handeling formatting changes to the spreadsheets you will give to our system. With our code, you will be able to add, delete, and rearrange content with a large degree of freedom. This document walks through what our code needs from your Excel file.  

## File Type
Our code takes in a simple Excel file with a `.xlsx` file ending.

## What our code needs from the Excel File  
We allow any spreadsheet provided it contains these properties.

![ExampleSpreadSheet](Graphics/SpreadSheetRequirements.png)

### Key Row  
Our system does not search for certain keys(categories), instead, it will add every category and its values into our database. The red box in the figue above describes a region of the spreadsheet we refer to as the *key rows*. These are rows *2* through *4* in the spreadsheet. Any text in this text will be interpreted as a category, there can be text in one row in a column or in all three. For our code to parse your file correctly, it will need to have all of the categories, in some form, on these rows. Our system, does not, however, have a limit on how many categories there can be.

### First Row
None of the information from the first row will be read into our system.

### First Column
One of few assumptions we make about the format of the spreadsheet is that the first column (The accession number, #, or plant ID) has a value for every row in the spreadsheet. Beyond that the actual contents of the first row does not affect how the file will be parsed.

### Ignoring Rows  
The Accession List's primary use is for keeping track of flowers through out the WCROC staff, and there might be times in which a flower exists in the Accession List that you don't want on the website. For this reason, we will ignore a flower under two certain conditions:
  1. If the garden location is blank, the flower doesn't belong to a particular bed, so we will ignore it.
  2. You may use a column in your Excel Spreadsheet named `Not Included`.
  If there is a row you would like our system to ignore, put an **x** in the `Not Included` column of that row.  

The following picture shows an example. The `""` indicates an empty cell, and any section in red is being ignored.  

![ignored row](Graphics/ignoreRow.png)

### Text Styling
The tools we are using to read in the Excel file do not give our code information about text styling. For this reason, we can not change how our system parses based on text styling.

**Beyond that, the spreedsheet is yours!!!**
