<!-- 
Authors:	Andreas Orphanides, andreas_orphanides@ncsu.edu
			Emily Lynerma, emily_lynema@ncsu.edu
			Troy Hurteau, troy_hurteau@ncsu.edu

Terms of Use: MIT License/X11 License

Copyright (C) 2010  NCSU Libraries, Raleigh, NC, http://www.lib.ncsu.edu/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

browsePopup.jsp
HTML for a "popup" version of the virtual browse application, suitable for loading
	into a Lightbox, etc.

Requires:
	browseData.jsp
	browse.js for javascript interaction
	browse.css
	screen.css
	
Uses:
	jQuery library
	jCarousel plugin
	SimpleTip plugin

-->

<%@ page contentType="text/html;charset=UTF-8" language="java" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="browseData" scope="request" type="edu.ncsu.lib.browse.BrowseDataBean" />

<div>
	<!--<div style="float: right;" class="viewSwitch">
		<div class="coverView active">
			Cover View
		</div>
		<div class="listView">
			List View
		</div>
	</div>-->
	
	<div class="pager">
		<a  class='browsePagerPrev' href='/browse?batchId=${browseData.batchId - 5}&displayType=${browseData.displayType}'>Previous</a> 
		<a class='browsePagerNext' href="/browse?batchId=${browseData.batchId + 5}&displayType=${browseData.displayType}">Next</a>
	</div>
	
	<div style="clear: both;"></div>
	
	<jsp:include page="browseData.jsp" />
	
	<div class="pager lowerPager">
		<a  class='browsePagerPrev' href='/browse?batchId=${browseData.batchId - 5}&displayType=${browseData.displayType}'>Previous</a> 
		<a class='browsePagerNext' href="/browse?batchId=${browseData.batchId + 5}&displayType=${browseData.displayType}">Next</a>
	</div>
</div>