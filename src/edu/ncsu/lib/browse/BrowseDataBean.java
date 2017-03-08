// Author:	Andreas Orphanides, andreas_orphanides@ncsu.edu
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

// BrowseDataBean.java
// This bean acts as a very thin model layer for transporting data between the
//		Browse.java controller and the JSP display files.

package edu.ncsu.lib.browse;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class BrowseDataBean {
	private int batchId;
	private String matchType;
	private LinkedHashMap<Integer,String> catKeys;
	private LinkedHashMap<Integer,String> tiTles;
	private LinkedHashMap<Integer,String> isBns;
	private LinkedHashMap<Integer,String> urLs;
	private LinkedHashMap<Integer,String> auThors;
	private LinkedHashMap<Integer,String> availables;
	private LinkedHashMap<Integer,String> years;
	private LinkedHashMap<Integer,String> locations;
	private LinkedHashMap<Integer,String> slocations;
	private LinkedHashMap<Integer,String> dues;
	private LinkedHashMap<Integer,String> shortTitles;
	private LinkedHashMap<Integer,String> multipleCopies;

	private HashMap<Integer,String> callNums;
	private String displayType;
	private String library;
	private String libraryShort;
	
	public BrowseDataBean(int batchId) {
		this.batchId = batchId;
	}
	
	public void setMatchType (String matchType) {
		this.matchType = matchType;
	}
	
	public void setCatKeys(LinkedHashMap<Integer,String> catKeys) {
		this.catKeys = catKeys;
	}
	/* AMPd Andrew */
	public void setTiTles(LinkedHashMap<Integer,String> tiTles) {
		this.tiTles = tiTles;
	}
	public void setIsBns(LinkedHashMap<Integer,String> isBns) {
		this.isBns = isBns;
	}
	public void setUrLs(LinkedHashMap<Integer,String> temp) {
		this.urLs= temp;
	}
	public void setAuThors(LinkedHashMap<Integer,String> auThors2) {
		this.auThors = auThors2;
	}
	
	public void setAvailables(LinkedHashMap<Integer,String> temp1) {
		this.availables= temp1;
	}
	public void setYears(LinkedHashMap<Integer,String> temp2) {
		this.years= temp2; 
	}
	public void setLocations(LinkedHashMap<Integer,String> temp3) {
		this.locations= temp3;
	}
	public void setSlocations(LinkedHashMap<Integer,String> temp4) {
		this.slocations= temp4;
	}
	public void setDues(LinkedHashMap<Integer,String> temp5) {
		this.dues= temp5;
	}
	public void setShortTitles(LinkedHashMap<Integer,String> temp6) {
		this.shortTitles= temp6;
	}
	public void setMultipleCopies(LinkedHashMap<Integer,String> temp7) {
		this.multipleCopies= temp7;
	}
	/*	End AMPd   */
	
	public void setCallNums(HashMap<Integer,String> callNums) {
		this.callNums = callNums;
	}
	
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}
	
	public void setLibrary(String library) {
		this.library = library;
	}
	
	public void setLibraryShort(String libraryShort) {
		this.libraryShort = libraryShort;
	}
	
	public int getBatchId() {
		return this.batchId;
	}
	
	public String getMatchType() {
		return this.matchType;
	}
	
	public LinkedHashMap<Integer,String> getCatKeys() {
		return this.catKeys;
	}

	/* AMPd Andrew */
	public LinkedHashMap<Integer,String> getTiTles() {
		return this.tiTles;
	}
	public LinkedHashMap<Integer,String> getIsBns() {
		return this.isBns;
	}	
	public LinkedHashMap<Integer,String> getUrLs() {
		return this.urLs;
	}
	public LinkedHashMap<Integer,String> getAuThors() {
		return this.auThors;
	}
	public LinkedHashMap<Integer,String> getAvailables() {
		return this.availables;
	}
	public LinkedHashMap<Integer,String> getYears() {
		return this.years;
	}
	public LinkedHashMap<Integer,String> getLocations() {
		return this.locations;
	}
	public LinkedHashMap<Integer,String> getSlocations() {
		return this.slocations;
	}
	public LinkedHashMap<Integer,String> getDues() {
		return this.dues;
	}
	public LinkedHashMap<Integer,String> getShortTitles() {
		return this.shortTitles;
	}
	public LinkedHashMap<Integer,String> getMultipleCopies() {
		return this.multipleCopies;
	}
	/*	End AMPd   */
	
	public HashMap<Integer,String> getCallNums() {
		return this.callNums;
	}
	
	public String getDisplayType() {
		return this.displayType;
	}
	
	public String getLibrary() {
		return this.library;
	}
	
	public String getLibraryShort() {
		return this.libraryShort;
	}
}
