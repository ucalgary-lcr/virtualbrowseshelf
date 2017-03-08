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

browse.jsp
Complete HTML page for display of virtual browse application.

Requires:
	browsePopup.jsp
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>University of Calgary Library: Browse Shelf</title>

<link href="include/jquery.tooltip.css" rel=stylesheet type="text/css" media="screen" />
<link href="include/screen.css" rel="stylesheet" type="text/css" media="screen" />
<link href="include/jquery.jcarousel.css" rel="stylesheet" type="text/css" media="screen" />
<link href="include/jcarousel.skin/jquery.jcarousel.skin.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript" src="include/jquery.cookie.js"></script>
<script type="text/javascript" src="include/jquery.jcarousel.pack.js"></script>
<script type="text/javascript" src="include/jquery.simpletip.js"></script>
<script type="text/javascript" src="include/browse.js"></script>
<script type="text/javascript">var debugFlag = "${param.debug}";</script>

<link href="include/browse.css" rel="stylesheet" type="text/css" media="screen" />

<style type="text/css" media="screen" id="jcarousel-sizing">
<!-- 
-->
</style>
</head>
<body>

<table border="0" align="center" id="mainContent" width="95%" cellpadding="0">
	<!--<tr>
		<td>
			<div id="browseTitlebar">Click the book for more details&nbsp;--><!--<sup class="beta">GENERIC</sup>--><!--</div>
		</td>
	</tr>-->
	<tr>
		<td>
			<jsp:include page="browsePopup.jsp" />
		</td>
	</tr>
</table>
</body>
</html>
