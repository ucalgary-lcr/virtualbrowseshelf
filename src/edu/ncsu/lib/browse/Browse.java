// Authors:	Andreas Orphanides, andreas_orphanides@ncsu.edu
//			Emily Lynema, emily_lynema@ncsu.edu
//			Stephen Cole
// 
// Terms of Use: MIT License/X11 License
// 
// Copyright (C) 2010  NCSU Libraries, Raleigh, NC, http://www.lib.ncsu.edu/
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

// Browse.java
// This servlet acts as the controller for the virtual browse JSP application.
// Valid parameters are:
// batchId: Index identifier for desired item from virtual shelf browse service
// callNumber: Item call number
// classType: "LC" for library of congress, "SUDOC" for gov't doc, default "LC"
// before: Number of items to return before requested item, default "14"
// after: Number of items to return after requested item, default "15"
// displayType: "full" -- request a full HTML document; "popup" -- request HTML 
// 		suitable for a popup box "data: -- request HTML suitable for loading data 
//		for AJAX update; default "full"
//
// batchId or callNumber must be provided.
// The configuration parameter "vsiUrl" must be set in web.xml to point to an 
//		instance of the Virtual Shelf Index service.

package edu.ncsu.lib.browse;

import java.io.*;	// AMP
import java.net.*;  // AMP
import java.io.IOException;
//import java.net.URLEncoder;
/* AMP */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
/* AMP */

/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;*/
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Browse extends HttpServlet {
	
	String address, service;
    ServletConfig config;
    ServletContext context;
    Logger log = LoggerFactory.getLogger(Browse.class);
    String vsiUrl = "";
    public BrowseDataBean browseData;
    
    // methods
    public void init() {
    
    	// get reference to servlet config & context
        config = getServletConfig();
        context = config.getServletContext();
        vsiUrl = config.getInitParameter("vsiUrl");
    }
    
    public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
		if(!StringUtils.isNotBlank(httpRequest.getParameter("batchId")) && !StringUtils.isNotBlank(httpRequest.getParameter("callNumber"))){
			throw new ServletException("Invalid request: neither <batchId> or <callNumber> parameter were specified");
		}

		String matchType = "slice";
		String classType = "LC";
		String batchId = httpRequest.getParameter("batchId");
		String callNumber = httpRequest.getParameter("callNumber");

		// prevent html injection by stripping '<' and '>'
		if (callNumber != null) {
			callNumber = callNumber.replaceAll("<", "").replaceAll(">", "");
		}
		try {
			if(!StringUtils.isNotEmpty(batchId)){
				if(httpRequest.getParameter("classType")!=null){
					classType = httpRequest.getParameter("classType");
				}
				String[] batchIdArray = retrieveBatchId(callNumber, classType);
				batchId = batchIdArray[0];
				if(batchId==null){
					log.error("CatKey or callNumber was not found in virtual browse index");
					dispatchError(httpRequest, httpResponse);
					return;
				}
				matchType = batchIdArray[1];
				httpRequest.getSession().setAttribute("virtualBrowseCallNumber", callNumber);
			}
			int numberBefore = 14;
			if(httpRequest.getParameter("before")!=null){
				numberBefore = Integer.parseInt(httpRequest.getParameter("before"));
			}
			int numberAfter = 15;
			if(httpRequest.getParameter("after")!=null){
				numberAfter = Integer.parseInt(httpRequest.getParameter("after"));
			}
	//		JSONObject result = getData(vsiUrl + "slice?batchId=" + batchId + "&numBefore=" + numberBefore + "&numAfter=" + numberAfter);
			JSONObject result = getData(vsiUrl + "router.php?service=slice&batchId=" + batchId + "&numBefore=" + numberBefore + "&numAfter=" + numberAfter); 
	//		log.info(vsiUrl + "slice?batchId=" + batchId + "&numBefore=" + numberBefore + "&numAfter=" + numberAfter);
//			log.info(vsiUrl + "router.php?service=slice&batchId=" + batchId + "&numBefore="   + numberBefore + "&numAfter=" + numberAfter);
			JSONArray results = result.getJSONArray("results");
			//log.info("Got " + results.length() + " results");
	
			ArrayList<String> requestKeys = new ArrayList<String>();
			int start = 0;
			int end = results.length();
			//adjust the start and end point to not include the current value if the numberBefore or the numberAfter is 0
			if(numberBefore==0){ start = 1;}
			if(numberAfter==0){ end = results.length()-1;}
			StringBuffer buf = new StringBuffer();
			// int firstBatchId = 0;
			LinkedHashMap<Integer, String> currentCatkeys = new LinkedHashMap<Integer, String>();
			LinkedHashMap<Integer, String> currentTiTles = new LinkedHashMap<Integer, String>(); //AMPd
			LinkedHashMap<Integer, String> currentIsBns = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentUrls = new LinkedHashMap<Integer, String>();  
			LinkedHashMap<Integer, String> currentAuThors = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentAvailables = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentYears = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentLocations = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentDues = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentShelFLocations = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentShortTitles = new LinkedHashMap<Integer, String>(); 
			LinkedHashMap<Integer, String> currentMultipleCopies = new LinkedHashMap<Integer, String>(); 
			HashMap<Integer, String> currentCallNums = new HashMap<Integer, String>();
				String xmlTitle="";
				String xmlISBN="";
				String xmlURL="";
				String xmlAuthor="";
				String xmlAvailable="";
				String xmlYear="";
				String xmlLocation="";
				String xmlShelfLocation="";
				String xmlDue="";
				String xmlMultipleCopies="";
			for(int i=start; i<end; i++){
				JSONObject item = results.getJSONObject(i);
				int currentBatchId = item.getInt("batchId");
				currentCallNums.put(new Integer(currentBatchId), item.getString("minCallNum"));
//	System.out.println ("\n\n First line of the record Call num first: " +  item.getString("minCallNum"));				
				String catkey = item.getString("catKey");//String catkey = "NCSU" + item.getString("catKey");
				currentCatkeys.put(new Integer(currentBatchId), catkey);
				requestKeys.add(catkey);
				buf.append(catkey + ", ");
				/*  AMP call SIRSI webservices AND Parse the XML data http://www.tek-tips.com/viewthread.cfm?qid=1404124&page=8  */
				try {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
					InputStream stream;
					URL url = new URL("http://sirsi1.lib.ucalgary.ca:8080/symws/rest/standard/lookupTitleInfo?clientID=SymWSTestClient&titleID="+catkey+"&includeOPACInfo=true&includeItemInfo=true&marcEntryFilter=856&includeItemInfo=true&includeAvailabilityInfo=true&includeCatalogingInfo=true&libraryFilter=UCALGARY");
					stream = url.openStream();							
					if (stream == null) { System.out.println ("Could not load definitions from " + url);}
					Document doc = docBuilder.parse(stream);
					doc.getDocumentElement ().normalize ();
//System.out.println ("Andrew Root element of the doc is " +  doc.getDocumentElement().getNodeName());
					NodeList listOfPersons2 = doc.getElementsByTagName("TitleInfo");

					String currentCallNum=item.getString("minCallNum");		// the call num for each item	(for multiple callnums: it takes the call num from the URL)
		//AMP displays all Call num locations
					NodeList nodeLst = doc.getElementsByTagName("CallInfo");
		if (nodeLst.getLength() > 1)		// if only one call number is assigned to the record then skip this
		{
			// Which call number is in the URL?  ie: From the call num in the URL let's match to one of our many call nums assigned to this record
			//		iterate through each xml call number to find a match
			int s;
			for (s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);    
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {  
					  Element fstElmnt = (Element) fstNode;
					  NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("callNumber");
					  Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
					  NodeList fstNm = fstNmElmnt.getChildNodes();
					  //System.out.println("xml callNumber : "  + ((Node) fstNm.item(0)).getNodeValue());
					  
					  // compare the Call num from the batch load and that in the xml (amongst all the xml call nums in this record)
					  //System.out.println("Are they equal? : " + currentCallNum + "  " + ((Node) fstNm.item(0)).getNodeValue());
					  if (currentCallNum.equals(((Node) fstNm.item(0)).getNodeValue()))		
					  {
							//System.out.println("They are equal : " + currentCallNum + "  " + ((Node) fstNm.item(0)).getNodeValue());
							break;			 
					  }			  
				}
			}
//System.out.println("Out of for loop with an s val of " + s + " total size " + nodeLst.getLength());
		}
		// End AMP displays all Call num locations

					//AMP iterate all Call num locations
					Boolean multipleCallNums = false;	
					NodeList nodeLst1 = doc.getElementsByTagName("CallInfo");
					//System.out.println("Call Number " + nodeLst1.getLength());
					for (int t = 0; t < nodeLst1.getLength(); t++) {
						Node fstNode = nodeLst1.item(t);    

						if (fstNode.getNodeType() == Node.ELEMENT_NODE) {  
							   Element fstElmnt = (Element) fstNode;
					NodeList fst1NmElmntLst = fstElmnt.getElementsByTagName("callNumber");
						  Element fst1NmElmnt = (Element) fst1NmElmntLst.item(0);
						  NodeList fst1Nm = fst1NmElmnt.getChildNodes();
						  //System.out.println("  callNumber in for : "  + ((Node) fst1Nm.item(0)).getNodeValue());

						  //See if the call numbers are equal, if so then see if the item is available, if not iterate to the next item
						  if (currentCallNum.equals(((Node) fst1Nm.item(0)).getNodeValue()))		
						  {
							  //System.out.println("Found the right call number : " + currentCallNum + "  " + ((Node) fst1Nm.item(0)).getNodeValue());
							  NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("itemID");
							  Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
							  NodeList fstNm = fstNmElmnt.getChildNodes();
							  //System.out.println("  itemID : "  + ((Node) fstNm.item(0)).getNodeValue());
							  NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("currentLocationID");
							  Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
							  NodeList lstNm = lstNmElmnt.getChildNodes();
							  //System.out.println("  currentLocationID : " + ((Node) lstNm.item(0)).getNodeValue());
							  xmlLocation=((Node) lstNm.item(0)).getNodeValue();
							  multipleCallNums=true;		// found a location for that specific call number
						  }
						}
					}
					// End AMP iterate all Call num locations



						// XML parsing from SIRSI WebServices - normalize text representation
						doc.getDocumentElement ().normalize ();
//System.out.println ("Root element of the doc is " +	 doc.getDocumentElement().getNodeName());
						NodeList listOfPersons = doc.getElementsByTagName("TitleInfo");
						int totalPersons = listOfPersons.getLength();
//System.out.println("Total no of people : " + totalPersons);
						for(int t=0; t<listOfPersons.getLength() ; t++){
							Node firstPersonNode = listOfPersons.item(t);
							if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE){
								Element firstPersonElement = (Element)firstPersonNode;
								//-------
								NodeList firstNameList = firstPersonElement.getElementsByTagName("title");
								Element firstNameElement = (Element)firstNameList.item(0);
								NodeList textFNList = firstNameElement.getChildNodes();
								//System.out.println("\nTitle : " +  ((Node)textFNList.item(0)).getNodeValue().trim());  
								xmlTitle=((Node)textFNList.item(0)).getNodeValue().trim();					
								
								
								/*	AMP - an empty ISBN causes a null point exception
									http://webapps2.ucalgary.ca:9090/browse/?callNumber=BK5105.8885.A63 	http://www.exampledepot.com/egs/java.lang/HasSubstr.html
								*/
								//-------
								NodeList isBnList = firstPersonElement.getElementsByTagName("ISBN");
								Element isBnElement = (Element)isBnList.item(0);
								NodeList textIBList = isBnElement.getChildNodes();
								Boolean bISBN = isBnElement.hasChildNodes();	// AP check if there is an ISBN 
								//System.out.println("has ChildNodes : " + bISBN);  
								if (bISBN==true)
								{
									xmlISBN=((Node)textIBList.item(0)).getNodeValue().trim();
// System.out.println("Just assigned a valid isbn: " + xmlISBN);	
								}
								else
								{
									xmlISBN="";
								}
// System.out.println("Here's what xmlISBN is: " + xmlISBN);	

								//-------
								NodeList AuThorList = firstPersonElement.getElementsByTagName("author");
								Element AuThorElement = (Element)AuThorList.item(0);
								NodeList textATList = AuThorElement.getChildNodes();
								//System.out.println("Auth : " + ((Node)textATList.item(0)).getNodeValue().trim());  
								xmlAuthor=((Node)textATList.item(0)).getNodeValue().trim();
							
								//-------
								NodeList AvaiLList = firstPersonElement.getElementsByTagName("itemTypeID");
								Element AvaiLElement = (Element)AvaiLList.item(0);
								NodeList textAVList = AvaiLElement.getChildNodes();
// System.out.println("Avail:" + ((Node)textAVList.item(0)).getNodeValue().trim());  
								xmlAvailable=((Node)textAVList.item(0)).getNodeValue().trim();
								//System.out.println("Avail:" + xmlAvailable);  
								if (xmlAvailable.equals("INTERNET"))
								{
									//-------
									NodeList URLList = firstPersonElement.getElementsByTagName("url");
									Element URLElement = (Element)URLList.item(0);
									NodeList textURList = URLElement.getChildNodes();
									//System.out.println("URL : " + ((Node)textURList.item(0)).getNodeValue().trim());  
									xmlURL=((Node)textURList.item(0)).getNodeValue().trim();
								}
								//-------
								NodeList YearList = firstPersonElement.getElementsByTagName("yearOfPublication");
								Element YearElement = (Element)YearList.item(0);
								NodeList textYRList = YearElement.getChildNodes();
								//System.out.println("Year : " +  ((Node)textYRList.item(0)).getNodeValue().trim());  
								xmlYear=((Node)textYRList.item(0)).getNodeValue().trim();
								//-------  Get Location from Call Num
// //System.out.println("Current Call Num in URL" + callNumber);


								//-------
					if (multipleCallNums==false)	// If doesn't have multiple call numbers then handle the record normally
					{					
								NodeList LocationList = firstPersonElement.getElementsByTagName("currentLocationID");
								Element LocationElement = (Element)LocationList.item(0);
								NodeList textLOList = LocationElement.getChildNodes();
								//System.out.println("Location : " + ((Node)textLOList.item(0)).getNodeValue().trim());  
								xmlLocation=((Node)textLOList.item(0)).getNodeValue().trim();
								//System.out.println("B4 xmlLocation " + xmlLocation);
					}
								if (xmlLocation.equals("CHECKEDOUT"))
								{
									//-------
									NodeList DueList = firstPersonElement.getElementsByTagName("dueDate");
									Element DueElement = (Element)DueList.item(0);
									NodeList textDUList = DueElement.getChildNodes();
									//System.out.println("DUE : " + ((Node)textDUList.item(0)).getNodeValue().trim());  
									xmlDue=((Node)textDUList.item(0)).getNodeValue().trim();
								}
								else{
									xmlLocation = locationPolicyLookup(xmlLocation);		// Andrew - get the full location name from my custom function
// System.out.println("Back from function " + xmlLocation);  
								}

								//System.out.println("Full Location " + xmlLocation);	
								//-------
								NodeList DYearList = firstPersonElement.getElementsByTagName("homeLocationID");
								Element DYearElement = (Element)DYearList.item(0);
								NodeList textDYRList = DYearElement.getChildNodes();
								//System.out.println("Shelf locat : " + ((Node)textDYRList.item(0)).getNodeValue().trim());  
								xmlShelfLocation=((Node)textDYRList.item(0)).getNodeValue().trim();
								//-------
								NodeList MultCopiesList = firstPersonElement.getElementsByTagName("totalCopiesAvailable");
								Element MultCopiesElement = (Element)MultCopiesList.item(0);
								NodeList textMCList = MultCopiesElement.getChildNodes();
								//System.out.println("Multiple Copies : " +  ((Node)textMCList.item(0)).getNodeValue().trim());  
								xmlMultipleCopies=((Node)textMCList.item(0)).getNodeValue().trim();
							}//end of if clause
						}//end of for loop with s var
					}catch (SAXParseException err) {
						System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
						System.out.println(" " + err.getMessage ());
					}catch (SAXException e) {
						System.out.println ("Hey you");
						Exception x = e.getException ();
						((x == null) ? e : x).printStackTrace ();
					}catch (Throwable t) {
						//System.out.println ("There was a NullPointerException uncomment line 388 of Browse.java to see this error");
						//t.printStackTrace ();
					}							  
				/* end of AMP XML parsing from WebServices */

				//String title = "Alice in Wonderland";	//AMP
				String title = xmlTitle;
				currentTiTles.put(new Integer(currentBatchId),title);
				requestKeys.add(title);

				String shortTitle = "";
				int length = title.length();
				//System.out.println("Short Title length " + length);
				int Maxsize=140;		// Short Title size
				if (length > Maxsize)
				{
					shortTitle=title.substring(0,Maxsize)+" ...";
				} 
				currentShortTitles.put(new Integer(currentBatchId),shortTitle);
				requestKeys.add(shortTitle);
				
				currentIsBns.put(new Integer(currentBatchId),xmlISBN);
				requestKeys.add(xmlISBN);
				
				currentAuThors.put(new Integer(currentBatchId),xmlAuthor);
				requestKeys.add(xmlAuthor);			
				
				currentAvailables.put(new Integer(currentBatchId),xmlAvailable);
				requestKeys.add(xmlAvailable);
	
				currentUrls.put(new Integer(currentBatchId),xmlURL);
				requestKeys.add(xmlURL);
		
				currentYears.put(new Integer(currentBatchId),xmlYear);
				requestKeys.add(xmlYear);
	
				currentLocations.put(new Integer(currentBatchId),xmlLocation);
				requestKeys.add(xmlLocation);

				currentMultipleCopies.put(new Integer(currentBatchId),xmlMultipleCopies);
				requestKeys.add(xmlMultipleCopies);
//System.out.println("B4 my mods xmlDue:" + xmlDue);  
//String junk = xmlDue;
//System.out.println("B4 my mods junk:" + junk);  
if (!(xmlDue.equals("")))// AMPd - format the Due Date
{

		String year = xmlDue.substring(0,4);//System.out.println("year: " + year);  System.out.println("B4 my mods 2 xmlDue:" + junk);     
		String mo = xmlDue.substring(5,7);	//System.out.println("mo: " + mo);   
		String day = xmlDue.substring(8,10);  // System.out.println("day: " + day);
		int aInt = Integer.parseInt(mo);  //System.out.println("Day:" + aInt);    
		String NewMo="";
       switch (aInt) {
            case 1:  NewMo="Jan."; break;
            case 2:  NewMo="Feb."; break;
            case 3:  NewMo="Mar."; break;
            case 4:  NewMo="Apr."; break;
            case 5:  NewMo="May"; break;
            case 6:  NewMo="June"; break;
            case 7:  NewMo="July"; break;
            case 8:  NewMo="Aug."; break;
            case 9:  NewMo="Sep."; break;
            case 10: NewMo="Oct."; break;
            case 11: NewMo="Nov."; break;
            case 12: NewMo="Dec."; break;
            default: NewMo="";break;
        }// System.out.println("NewMo:" + NewMo); 
		xmlDue=" " + NewMo + " " + day + ", " + year;
}//System.out.println("xmlDue:" + xmlDue);  
				currentDues.put(new Integer(currentBatchId),xmlDue);
				requestKeys.add(xmlDue);
				xmlDue="";
				
				currentShelFLocations.put(new Integer(currentBatchId),xmlShelfLocation);
				requestKeys.add(xmlShelfLocation);
			}		
			
//			long startTime = System.currentTimeMillis();
//			log.info("Identified neighboring catkeys: " + buf.toString() + "finished in " + calculateTime(startTime));
			
			browseData = new BrowseDataBean(Integer.parseInt(batchId));
			//Map<String, Object> model = new HashMap<String, Object>();
			//model.put("returnedBatchId", Integer.parseInt(batchId));
//System.out.println("Current Dues" + currentDues);			
			browseData.setMatchType(matchType);
			browseData.setCatKeys(currentCatkeys);
			browseData.setCallNums(currentCallNums);				
			browseData.setTiTles(currentTiTles); //AMP
			browseData.setIsBns(currentIsBns); //AMP
//System.out.println("Current URls" + currentUrls);
			browseData.setUrLs(currentUrls); //AMP
			browseData.setAuThors(currentAuThors); //AMP
			//model.put("matchType", matchType);
			//model.put("currentCatkeys", currentCatkeys);
			//model.put("currentCallNums", currentCallNums);
//System.out.println("Current Shelf Locations" + currentShelFLocations);
			browseData.setSlocations(currentShelFLocations); //AMP			
			browseData.setAvailables(currentAvailables); //AMP
			browseData.setYears(currentYears); //AMP
//System.out.println("Current Locations" + currentLocations);
			browseData.setLocations(currentLocations); //AMP
			browseData.setDues(currentDues); //AMP
			browseData.setShortTitles(currentShortTitles); //AMP
//System.out.println("Current Multiple Copies" + currentMultipleCopies);
			browseData.setMultipleCopies(currentMultipleCopies); //AMP

	
			String jsp;
			String displayType = httpRequest.getParameter("displayType");
			if(displayType!=null && displayType.equals("data")){
				jsp = "browseData";
			}else if(displayType!=null && displayType.equals("popup")){
				jsp = "browsePopup";
			}else{ //if(displayType.equals("full")){
				if(callNumber==null){
					if (httpRequest.getSession().getAttribute("virtualBrowseCallNumber") != null) {
						callNumber = httpRequest.getSession().getAttribute("virtualBrowseCallNumber").toString();
					}
					if(callNumber==null){
						callNumber = results.getJSONObject(numberBefore).getString("minCallNum");
					}
				}
	
				jsp = "browse";
			}

			
			browseData.setDisplayType(displayType);
			browseData.setLibrary("University of Calgary Library");
			browseData.setLibraryShort("Library");
			httpRequest.setAttribute("browseData",browseData);
			//model.put("displayType", displayType);
			//model.put("library", "Your library");
			//model.put("libraryShort", "Library");
			//log.info("TRLN processed request for " + buf.toString() + " in " + calculateTime(fullTime));
			jsp = "jsp/" + jsp + ".jsp";
			RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(jsp);
			try {
				dispatcher.forward(httpRequest, httpResponse);
			}
			catch (Exception e) {
				dispatchError(httpRequest, httpResponse);
			}


		}
		catch(JSONException e) {
			log.error("Just threw a JSON exception.");
			e.printStackTrace();
			dispatchError(httpRequest, httpResponse);
		}
	}

	private String[] retrieveBatchId(String callNumber, String classType) throws IOException, JSONException {
		String url = vsiUrl + "router.php?service=start&classType="+classType+"&callNum=" + URLEncoder.encode(callNumber, "UTF-8"); 
		//String url = vsiUrl + "start?classType="+classType+"&callNum=" + URLEncoder.encode(callNumber, "UTF-8");
		JSONObject result = getData(url);
		
		JSONArray array = result.getJSONArray("results");
		
		if(array==null || array.length()==0){ System.out.println("Can't connect to the database, call Derrick Woo!");
			return null;
		}else{
			String[] output = new String[2];
			output[0] = array.getJSONObject(0).getString("batchId");
			output[1] = result.getJSONObject("parameters").getString("matchType");
			return output;
		}
	}
		
	private JSONObject getData(String url) throws IOException, JSONException{
		//log.info("Polling url: " + url);
    	HttpClient client = new HttpClient();
    	GetMethod method = new GetMethod(url);
    	int statusCode = client.executeMethod(method);
    	if (statusCode != HttpStatus.SC_OK) {
	    	log.error("Call to vsi start failed with result: "+ method.getStatusLine());
	    	throw new IOException("Error communicating with VSI service");
	    }
    	//String response = method.getResponseBodyAsString();
		InputStreamReader in2 = new
		InputStreamReader(method.getResponseBodyAsStream(), "UTF-8");
        StringWriter sw = new StringWriter();
        int x;
        while((x = in2.read()) != -1){
            sw.write(x);
        }
        in2.close();
        String response = sw.toString(); 
		return new JSONObject(new JSONTokener(response));
	}
	
	private void validate(HttpServletRequest r) throws ServletException {
		if(!StringUtils.isNotBlank(r.getParameter("before"))){
			throw new ServletException("Invalid request: <before> parameter not specified");
		}
		if(!StringUtils.isNotBlank(r.getParameter("after"))){
			throw new ServletException("Invalid request: <after> parameter not specified");
		}
		if(!StringUtils.isNotBlank(r.getParameter("displayType"))){
			throw new ServletException("Invalid request: <displayType> parameter not specified");
		}

	}
	
	private String calculateTime(long startTime) {
		long time = System.currentTimeMillis() - startTime;
		long minutes = time / 60000;
		long seconds = (time % 60000)/1000;
		//long millis = (time % 60000);
		return minutes + " minutes " + seconds + " seconds" ;
	}

    private void dispatchError(HttpServletRequest request, HttpServletResponse response) {
        RequestDispatcher errDispatcher = request.getRequestDispatcher("jsp/error.jsp");
        try {
           errDispatcher.forward(request, response);
        } catch (Exception e) {
            //System.out.println("Error forwarding to error.jsp");
            e.printStackTrace();
        }
    }


	private String locationPolicyLookup(String shortLookUp) {		/* AMP new - Location lookup full name */
		String longLookUp="";
		try {
				DocumentBuilderFactory docBuilderFactoryLoc = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilderLoc = docBuilderFactoryLoc.newDocumentBuilder();
				URL urlLoc = new URL("http://sirsi1.lib.ucalgary.ca:8080/symws/rest/admin/lookupLocationPolicyList?clientID=SymWSTestClient");
				InputStream streamLoc = urlLoc.openStream();
				Document docLoc = docBuilderLoc.parse(streamLoc);
				
				String xmlpolNum="";
				String xmlpolID="";
				String xmlpolDesc="";
				 docLoc.getDocumentElement ().normalize ();
				//System.out.println ("Andrew2 Root element of the doc is " +  docLoc.getDocumentElement().getNodeName());
				NodeList listOfPersons2Loc = docLoc.getElementsByTagName("policyDescription");
				NodeList policyNum = docLoc.getElementsByTagName("policyNumber");
				NodeList policyID = docLoc.getElementsByTagName("policyID");
				NodeList policyDesc = docLoc.getElementsByTagName("policyDescription");

				NodeList listOfLocations = docLoc.getElementsByTagName("policyInfo");
				int totalLocations = listOfLocations.getLength();
				////System.out.println("Total no of locations : " + totalLocations);


				for(int t=0; t<listOfLocations.getLength() ; t++){
					Node locationsNode = listOfLocations.item(t);
					if(locationsNode.getNodeType() == Node.ELEMENT_NODE){
						Element locationsElement = (Element)locationsNode;
						//-------
						NodeList locNameList = locationsElement.getElementsByTagName("policyNumber");
						Element locElement = (Element)locNameList.item(0);
						NodeList textFNList = locElement.getChildNodes();
				//		//System.out.println("\nPolicy Number : " +  ((Node)textFNList.item(0)).getNodeValue().trim());  
						xmlpolNum=((Node)textFNList.item(0)).getNodeValue().trim();
						//-------
						NodeList locIDList = locationsElement.getElementsByTagName("policyID");
						Element locIDElement = (Element)locIDList.item(0);
						NodeList textIDList = locIDElement.getChildNodes();
				//		System.out.println("Policy ID : " +  ((Node)textIDList.item(0)).getNodeValue().trim());  
						xmlpolID=((Node)textIDList.item(0)).getNodeValue().trim();
						//-------
						NodeList locDescList = locationsElement.getElementsByTagName("policyDescription");
						Element locDescElement = (Element)locDescList.item(0);
						NodeList textDescList = locDescElement.getChildNodes();
				//		System.out.println("Policy Desc : " +  ((Node)textDescList.item(0)).getNodeValue().trim());  
						xmlpolDesc=((Node)textDescList.item(0)).getNodeValue().trim();
						if (xmlpolID.equals(shortLookUp))
						{
							longLookUp=xmlpolDesc;
						}
					}//end of if clause							
				 }//end of for loop with s var

	
			}catch (SAXParseException err) {
				System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
				System.out.println(" " + err.getMessage ());
			}catch (SAXException e) {
				Exception x = e.getException ();
				((x == null) ? e : x).printStackTrace ();
			}catch (Throwable t) {
				t.printStackTrace ();
			}	
		return longLookUp;
	}
}

