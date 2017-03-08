TERMS OF USE
    
    MIT/X11 License
    See included LICENSE.txt or http://www.opensource.org/licenses/mit-license.php

VIRTUAL SHELF BROWSE APPLICATION

    The browse-shelf directory contains the complete code necessary to implement
    the Virtual Shelf Browse web application as part of a Java/JSP application.
	It relies on connectivity with a Virtual Shelf Index to provide index data
	and call number display.
    
DIRECTORIES AND FILES

	browse-shelf/ -- top-level directory for browse application
		build.xml -- ANT build file for compiling a WAR of the application
		README.txt -- this file
		web.xml -- Configuration file for Virtual Shelf Browse web application
		
		images/ -- image files required for web application
			book_1.jpg .. book_5.jpg -- background images for placeholder covers
			book_shadow*.gif (15 files) -- Images for drop-shadow effects on covers
			browse_next*.gif, browse_next*.png -- Navigation controls
		
		include/ -- CSS and JavaScript files
			browse.css
			screen.css
			jquery.jcarousel.css
			jquery.tooltip.css
			
			browse.js
			jquery.cookie.js
			jquery.jcarousel.pack.js
			jquery.simpletip.js
			
			jcarousel.skin/
				jquery.jcarousel.skin.css
				next-*.png, prev-*.png
		
		jsp/ -- JSP files for page render
			browse.jsp
			browseData.jsp
			browsePopup.jsp
			error.jsp
		
		lib/ -- Included Java packages
			commons-codec-1.3.jar
			commons-httpclient-3.1.jar
			commons-lang-2.1.jar
			jcl-over-slf4j-1.5.6.jar
			json-20080701.jar
			log4j-1.2.15.jar
			log4j-over-slf4j-1.5.6.jar
			slf4j-api-1.5.6.jar
			slf4j-jdk14-1.5.6.jar
			
		src/ -- Java application source files
			edu.ncsu.lib.browse.Browse.java -- Servlet for browse application
			edu.ncsu.lib.browse.BrowseDataBean.java -- Bean for data transport between servlet and JSP


CONFIGURATION

Specify the url for an instance of the Virtual Shelf Index service in web.xml, under the servlet
	declaration for the Browse servlet, in the init-param named vsiUrl.
	 
	NCSU's test implementation of the Virtual Shelf Index is available at
	http://webdev.lib.ncsu.edu/virtualshelfindex/ for testing purposes (service live at the time of 
	this writing, but availability not guaranteed).
	
To see the shelf browse in operation with dummy bibliographic data, build the application (with the 
	above change in place) with ANT and load the WAR file into your favorite web application server.

You can get some debugging information by adding a "debug=true" parameter to your query string. This 
	will display some of the search parameters in the application window and will also indicate the 
	Batch ID value for each item returned.
	
Sample URL for the browse application: 
	http://virtualshelfhost/browse/?batchId=500
	http://virtualshelfhost/browse/?callNumber=PS332&debug=true
	
For more information on valid requests to the virtual browse application, see notes in Browse.java

The virtual browse application will have to be linked to your ILS and data loaded into the JSP files
	for true bibliographic and availability data to be displayed. We have not provided methods for this
	process due to the complexity and variability of ILS configurations. To pull in bibliographic data
	you will need to take the catalog keys returned from the Virtual Shelf Index JSON response and make
	a query to your ILS; map the metadata to the corresponding batch IDs provided by the virtual shelf
	index; then write the correct data into the JSPs. BrowseDataBean.java provides a useful space to
	share data between Browse.java and the the JSPs. 
	
	We have provided placeholder data in the JSP files for display purposes, so that the application
	functionality may be tested without linking to an ILS.

GENERAL PROJECT INFO

    http://www.lib.ncsu.edu/dli/projects/virtualshelfindex/

