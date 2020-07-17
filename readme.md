# My-AV-Data
My-AV-Data is a program to display how DataManager can be used to read, visualize, and analyze commercial airplane data from the Bureau of Transportation Statistics (BST). My-AV-Data serves as an application of DataManager. DataManager, a general data input and management tool, is the majority of this program's implementation and will be discussed for the rest of the readme. My-AV-Data serves as a wrapper for the DataManager project to show off its more powerful features in the real world.

# DataManager
DataManager enables large data sets to be analyzed efficiently by only loading the data you want across multiple files. For example, DataManager allows My-AV-Data to analyze 108 million data points accross over 5.7 million airline flights with efficiency. 

DataManager instances are created my passing a String argument into the constructor. This string argument is parsed to collect the following information...

* **catagories** - The headers/collumns from the (csv) files you want to import. 
* **filter** - Specific values under each header/collumn to import. For example, if you have a catagory (header/collumn from file) of data that stores a number between 1-10, but you only want to import data rows where this catagory is equal to 6, you would pass this into DataManager as a filter.
* **dates** - The date files you want to import. For example, if you want to analyze data from January 2020 (2001) to April 2020 (2004), you would pass in this range of dates, and DataManager will import those respective files. 

Arguments are constructed into three main segments like ```catagorys:filters:dates```, where each main segment is parsed by a ```:``` delimiter. 

A complete argument follows this format: ```cat1,cat2,catN:cat1.val1,cat2.(range1,range2),cat3.[val1,val2,val3]:date1-date2```. Note the following...

* **Catagories** are passed simply as values, seperated by a ```,``` delimiter. 
* **Filters** are passed as key/value pairs with a ```.``` delimiter, seperated by a ```,``` delimiter. The key is the catagory/header/collumn you want to search for, and the value is the value you want to search for in the catagory/header/collumn defined in the key. There are three kind of filter arguments...
	* **key.value** - Value type. Self explanatory. Only searches for one value.
	* **key.(lowRangeBound-highRangeBound)** - Range type. Searches for *any* value within the range passed in.
	* **key.[value1-value2-value3-valueN]** - Series type. Searches for all values passed into the series of values. Does not search in between passed in values (use range type instead).
* **Dates** are passed simply as values or as a range, seperated by a ```,``` delimiter. There are two kinds of date arguments...
	* **date** - Value type. Self explanatory. Only searches for one value.
	* **(startDate-endDate)** - Range type. Searches for *any* value within the range passed in. Noticeably, DataManager is robust enough to NOT throw errors if you search through a date range and a corresponding file does not exist (it will simply continue looking through the range). 

DataManager's implementation is best understood through examples. For this, we'll bring back My-AV-Data and the BST's spreadsheet files.

* If you wanted to analyze delays at United Airlines from January to April 2020, you would pass into DataManager...
	* ```DEP_DELAY,ARR_DELAY:OP_UNIQUE_CARRIER.UA:(2001-2004)```
* If you wanted to analyze which cities are connected with flights between 3000 and 5000 miles in January 2020 versus April 2020, you would pass into DataManager...
	* ```ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID:DISTANCE.(3000-5000):2001,2004```
* If you wanted to analyze delays among the "big three" (American United and Delta) in Feburary 2020, you would pass into DataManager...
	* ```DEP_DELAY,ARR_DELAY:OP_UNIQUE_CARRIER.[AA-UA-DA]:2001,2004```
* If you wanted to analyze the distance of Delta's flights that depart from Hartsfield Jackson in March 2020, you would pass into DataManager...
	* ```DISTANCE:OP_UNIQUE_CARRIER.DA,ORIGIN_AIRPORT_ID.10397:2003```
