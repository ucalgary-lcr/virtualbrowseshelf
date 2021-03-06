// Authors:	Andreas Orphanides, andreas_orphanides@ncsu.edu
//			Emily Lynerma, emily_lynema@ncsu.edu
//			Troy Hurteau, troy_hurteau@ncsu.edu
//			Karl Doerr, karl_doerr@ncsu.edu
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
// browse.js
// Javascript functionality for virtual browse web application.
//		For use with browse.jsp and browsePopup.jsp
//
// Requires:
// jQuery library
// jCarousel plugin
// SimpleTip plugin


var numberOfCovers = 5;					// How many items to show and how many items to scroll by.
var carouselStart = 13;	// This is just for instantiating the carousel. Should only change 
										// if position changes before instantiating carousel.
var browseUrl = "../browse";			// Relative URL for the browse app, dependent on current context.
var containingDiv = '#TB_ajaxContent';	// How to find the div we're working in.
var embeddedBrowse = "true";			// True if we're in the thickbox context, false in full page context.
										// This is a string since it gets used in a URL query string.

$(document).ready(function()
{
	if (location.href.indexOf("/browse") != -1){
		embeddedBrowse = "false";
		browseUrl = "./browse";
		$("#listview").hide();
		browseShelf.initCovers();
		//browseShelf.initList();
		containingDiv = '#mainContent';
		browseShelf.setContainingDiv($(containingDiv));
	}
	var viewSwitcher = $(containingDiv + ' div.viewSwitch');
	viewSwitcher.show();
	//$('div.coverView', viewSwitcher).live("click", browseShelf.showCovers); 
	//$('div.listView', viewSwitcher).live("click", browseShelf.showList);
	$("a.browsePagerPrev").attr("href", "#")
		.attr("onclick", "javascript: return false;")
		.live("click", function()
			{
				if (!browseShelf.isCarouselReady()) {
					browseShelf.listPager("left", numberOfCovers);
				}
			}
		);
	$("a.browsePagerNext").attr("href", "#")
		.attr("onclick", "javascript: return false;")
		.live("click", function()
			{
				if (!browseShelf.isCarouselReady()) {
					browseShelf.listPager("right", numberOfCovers);
				}
			}
		);
});

var browseShelf = function()
{
	var instance = null;
	var containingDivObject = null;
	var currentListView = null;
	var bufferRatio = 2;			// How many times bigger is the buffer than the page?
	var matchedBatchId = 0;			// The item that was originally selected
	var firstBatchId = 0;			// the first and last batch IDs that currently exist in the page
	var lastBatchId = 0;			// these help prevent a race condition from occurring on prefetch
	var currentBatchId = 0;			// First visible batch ID for current list position.
	var currentPrevIndex = 0;		// helps override the silly behavior when trying to prev and add items to the beginning of the list
	//var coverAjaxNextQueue = [];	// holders for returned AJAX until animation stops.
	//var coverAjaxPrevQueue = [];
	var imageURLQueue = [];			// hold list of image URLs to retrieve so we don't flood the browser and get data requests throttled
	//var coverAjaxQueueId = 0;		// uniqueID assigned to each ajax request. Makes finding the insertion point faster
	var prevFlag = false;			// jCarousel executes its "next" callback when repositioning it, so this
									// flag is set to "1" and checked against before the "next" callback
									// is executed. That way you can prevent "next" callback from occurring
									// on reposition (which we don't want).
	var spinner = "<em>Loading...</em>";	// Spinner text for image load.
	var isMoving = 0; 				// indicates when the carousel is in a state of motion, used to defer DOM manipulation.
	var olderIE = $.browser.msie && parseFloat($.browser.version) < 8.0;
	var safariAndIeCss = (
		olderIE
		? ('\n.fakeImage .coverShadowCorner, .noResultsContents .coverShadowCorner {padding-bottom:0; height: 212px;} '
			+ '.coverContents{background-repeat: no-repeat;background-position: left top;} '
			+ '.recordImageBlock .noResultsContents td,  .recordImageBlock .fakeImage td{height: 194px;}\n'
		) : '\n.recordImageBlock .coverContents td, .recordImageBlock .noResultsContents td {height: 190px;}\n'
	);
	var additionalCoverImageWidth = ($.browser.safari || olderIE ? 10 : 15);
	var rightArray = [];
	var midRightArray = [];
	var middleArray = [];
	var midLeftArray = [];
	var leftArray = [];
	var firstCarouselItem;
	var lastCarouselItem;


	
	function initCallback(carousel, state)
	{
		firstCarouselItem = $("#mycarousel > li:first");
		lastCarouselItem = $("#mycarousel > li:last");
		setBorderBatchIds();
		instance = carousel;
		for (var i = 0; i < numberOfCovers; i++) {
			var j = i + numberOfCovers;
			var k = i + numberOfCovers * 2;
			var l = i + numberOfCovers * 3;
			var m = i + numberOfCovers * 4;
			leftArray[i] = $("#mycarousel > li:eq(" + i + ")");
			midLeftArray[i] = $("#mycarousel > li:eq(" + j + ")");
			middleArray[i] = $("#mycarousel > li:eq(" + k + ")");
			midRightArray[i] = $("#mycarousel > li:eq(" + l + ")");
			rightArray[i] = $("#mycarousel > li:eq(" + m + ")");
		}
		//alert(firstBatchId + " " + currentBatchId + " " + lastBatchId);
	}
	
	function setBorderBatchIds() {
		firstBatchId = parseInt($("div.recordImageBlock", firstCarouselItem).attr("batchid"), 10);
		lastBatchId = parseInt($("div.recordImageBlock", lastCarouselItem).attr("batchid"), 10);
	}
	
	function calculateBufferSize()
	{
		return numberOfCovers * bufferRatio;
	}
	
	function calculateMinimumBufferSize()
	{
		return numberOfCovers * bufferRatio;
	}
	
	function showListManipulation(){
		$("div.coverView", containingDivObject).addClass("active");
		$("div.listView", containingDivObject).removeClass("active");
		$("#listview", containingDivObject).show();
		$("div.pager", containingDivObject).show();
	}
	
	function show()
	{
		if (!instance) {
			$("div.recordImageBlock", containingDivObject).hide();
			$('#mycarousel').show(1,loadCarousel);
		}
		else {
			$('div.jcarousel-container').parent().show();
			instance.reload();
		}		
		$('div.listView', containingDivObject).addClass('active');
		$('div.coverView', containingDivObject).removeClass('active');
		$('div.pager', containingDivObject).hide();
	}

	function getListPos(batchId)
	{
		var listObject = $('.' + batchId, currentListView);
		return $('li',currentListView).index(listObject);
	}
	
	function getCarouselPos(batchId)
	{
		var listObject = $('#mycarousel li.' + batchId);
		return $('li',currentListView).index(listObject);
	}

	
	function getCoverPos(batchId)
	{
		var coverObject = $('.recordImageBlock.' + batchId, currentListView).parent();
		return $('li', currentListView).index(coverObject);
	}
	
	function utilAlert() {
		var numCovers = $("#mycarousel li").length;
		var numList = $("li", currentListView).length;
		alert("Covers: " + numCovers + "\nList elmts: " + numList + "\nCurrent batch ID: " + currentBatchId);
	}

	function normalizeJspListOutput(jspHtml){
		jspHtml = $(jspHtml);
		$('#mycarousel',jspHtml).remove();
		if (0 != matchedBatchId) {
			$('#listview li.' + matchedBatchId, jspHtml).addClass('selected');
		}
		$('#listview > li.hidden', jspHtml).removeClass('hidden');
		return jspHtml.html();
	}

	function normalizeJspCoverOutput(jspHtml, newCarousel){
		jspHtml = $(jspHtml);
		$('#listview',jspHtml).remove();
		if (0 != matchedBatchId) {
			$('#cover' + matchedBatchId , jspHtml).addClass('selected');
		}
		if (newCarousel) {
			$('#mycarousel > li', jspHtml).each(addImageNoThrottle);
		}
		else {
			$('#mycarousel > li', jspHtml).each(addImage);
		}
// AMP		alert("what's in imageURLQueue? " + imageURLQueue);
		return jspHtml.html();
	}

	function addTooltip()
	{
		var coverListItem = $(this);
		// remember 'this' is the coverListItem, this method is designed to be used with $().each()
		var mouseOver = $('div.mouseoverBlock', coverListItem).html();
		var options = (
			$.browser.msie
			? {
				content : mouseOver, 
				fixed : true, 
				showEffect : "none", 
				hideEffect : "none"
			} : {
				content : mouseOver, 
				position : [-5,205], 
				fixed: true, 
				showEffect : "none", 
				hideEffect : "none"
			}
			//{
			//bodyHandler: function() {
			//return $(mouseOver).html();
			//},
			//content: mouseOver,
			//position: ["0", "0"],
			// offset: [0, -60],
			//fixed: true
			//}
		);
		$('div.coverBlock',coverListItem).simpletip(options);
		coverListItem.addClass('hasTooltip');
		if ($.browser.msie) {
			var thisURL = $("a", coverListItem).attr("href");
			$("div.coverImage", coverListItem).click( function () {
				location.href = thisURL;
			});
			$('.tooltip', coverListItem).css('position', 'static');
		}
	}


	function addImageNoThrottle()
	{
		// remember 'this' is the coverListItem, this method is designed to be used with $().each()
		var coverListItem = $(this);
		var coverListItemRecord = $('.recordImageBlock',coverListItem);
		var batchId = coverListItemRecord.attr('batchid');
		var coverImage = $("div.coverImage", coverListItem);
		var coverContents = $("div.coverContents", coverListItem);
		var isbn = coverImage.attr("isbn");
		//var isbn ="0743236009";
		//var upc = coverImage.attr("upc"); AMP
		//var oclc = coverImage.attr("oclc");
		
		if ((isbn && isbn.length > 0) || (oclc && oclc.length > 0) || (upc && upc.length > 0)) {
			var link = '/browse/jsp/imageserver.jsp?isbn=' + (isbn ? isbn : '')
				//+ '&upc=' + (upc ? upc : '')
				//+ '&oclc=' + (oclc ? oclc : '')
				+ '&size=M';
			var x = $.ajax({
				type: "GET",
				timeout: 5000,
				url: link,
				success: function(data){
					var coverListItemRecord = $('#cover' + batchId);
					var coverListItem = coverListItemRecord.parent();
					var coverImage = $("div.coverImage", coverListItemRecord);
					var coverContents = $("div.coverContents", coverListItemRecord);
					coverContents.removeClass('cover1')
						.removeClass('cover2')
						.removeClass('cover3')
						.removeClass('cover4')
						.removeClass('cover5');
					coverContents.html("<div class='coverShadowRight'><div class='coverShadowBottom'><div class='coverShadowTop'><div class='coverShadowLeft'><div class='coverShadowCorner'><img src='" 
						+ data 
						+ "'/></div></div></div></div></div>");
					coverImage.removeClass('fakeImage');
					coverContents.width($('img',coverImage).width() + additionalCoverImageWidth);
					coverListItem.addClass('hasImage');
				},
				error: function(){
					//coverListItem.addClass('hasImage');
				}				
			});//alert("In addImageNoThrottle ckey:" + coverImage + coverListItemRecord + coverListItem + link + x);
			setTimeout( function () {x.abort();}, 5000);
		}
	}

	function addImage()
	{
		// remember 'this' is the coverListItem, this method is designed to be used with $().each()
		var coverListItem = $(this);
		var coverListItemRecord = $('.recordImageBlock',coverListItem);
		var batchId = coverListItemRecord.attr('batchid');
		var coverImage = $("div.coverImage", coverListItem);
		var coverContents = $("div.coverContents", coverListItem);
		var isbn = coverImage.attr("isbn");
		//var isbn ="0743236009";
		//var upc = coverImage.attr("upc"); AMP
		//var oclc = coverImage.attr("oclc");
	//alert("HEY" + oclc + isbn);	
		if ((isbn && isbn.length > 0) || (oclc && oclc.length > 0) || (upc && upc.length > 0)) {
			// Below, the 'link' variable would hold the link for your image server service.
			var link = '/browse/jsp/imageserver.jsp?isbn=' + (isbn ? isbn : '')
				//+ '&upc=' + (upc ? upc : '')
				//+ '&oclc=' + (oclc ? oclc : '')
				+ '&size=M';
			// Instead of actually requesting image here (these asynchronous requests bottle up browser's ajax connections)
			// add the image server URL to a queue to be requested in a throttled fashion
			imageURLQueue.push(link + "&batchId=" + batchId);
		}
	}
	
	function requestImage() {
		if (imageURLQueue.length > 0) {
			var currentURL = imageURLQueue[0];
			var batchId = "";
			if (currentURL.match(/&batchId=/)) {
				var urlData = currentURL.split(/&batchId=/);
				batchId = urlData[1];
				currentURL = urlData[0];
			}
			alert ("getting image for batchId " + batchId + " with URL " + currentURL);
			var x = $.ajax({
				type: "GET",
				timeout: 5000,
				url: currentURL,
				success: function(data){
					var coverListItemRecord = $('#cover' + batchId);
					var coverListItem = coverListItemRecord.parent();
					var coverImage = $("div.coverImage", coverListItemRecord);
					var coverContents = $("div.coverContents", coverListItemRecord);
					coverContents.removeClass('cover1')
						.removeClass('cover2')
						.removeClass('cover3')
						.removeClass('cover4')
						.removeClass('cover5');
					coverContents.html("<div class='coverShadowRight'><div class='coverShadowBottom'><div class='coverShadowTop'><div class='coverShadowLeft'><div class='coverShadowCorner'><img src='" 
							+ data 
							+ "'/></div></div></div></div></div>");
					coverImage.removeClass('fakeImage');
					coverContents.width($('img',coverImage).width() + additionalCoverImageWidth);
					imageURLQueue.shift();
					if (imageURLQueue.length > 0) {
						requestImage();
					}
				},
				error: function(){
					imageURLQueue.shift();
					if (imageURLQueue.length > 0) {
						requestImage();
					}
				}
				//complete: function(data, status) {
				//	imageURLQueue.shift();
				//	if (imageURLQueue.length > 0) {
				//		requestImage();
				//	}
				//}
			});
			
			// AJAX requests seem to be getting mysteriously hung up in the browser. The timeout
			// parameter in the $.ajax function doesn't fix it--it seems to time from when the request
			// actually gets sent by the browser to the remote server. The code below will kill the above
			// ajax request, after five seconds -- whether the request has gone out to the remote server 
			// or not.
			setTimeout( function () {x.abort();}, 5000);
		}
	}
	
	function animateCarousel(carousel, state) {
		
		function carouselCopy(source, dest) {
			for (var i = 0; i < numberOfCovers ; i++) {
				dest[i].html(source[i].html());
			}
		}
		
		function beforeAnim() {
			$("#mycarousel > li div.tooltip").remove();
			if (state == "next") {
				if (!isMoving) {
					isMoving = 1
					carouselCopy(midLeftArray, leftArray); 	// copy the central stuff to the left end.
					carouselCopy(middleArray, midLeftArray);
					//alert("bla");
					carousel.scroll(numberOfCovers + 1, false);		// jump to the mid-left end without animating.
					//alert(carousel.first);
					carouselCopy(midRightArray, middleArray);
					carouselCopy(rightArray, midRightArray);
					for (var i = 1; i <= numberOfCovers; i++) {
						var nextBatchId = lastBatchId + i;
						var spinnerString = "<div class='recordImageBlock " + nextBatchId + "' id='cover" + nextBatchId + "' batchid='" + nextBatchId + "'>" + spinner + "</div>";
						rightArray[i-1].html(spinnerString);
					}
					setBorderBatchIds();
				}
			}
			else if (state == "prev") {
				if (!isMoving) {
					isMoving = 1
					carouselCopy(midRightArray, rightArray);
					carouselCopy(middleArray, midRightArray); 
					carousel.scroll(numberOfCovers * 3 + 1, false);		// jump to the mid-right end without animating.
					carouselCopy(midLeftArray, middleArray);
					carouselCopy(leftArray, midLeftArray);
					for (var i = 1; i <= numberOfCovers; i++) {
						var nextBatchId = firstBatchId - i;
						var spinnerString = "<div class='recordImageBlock " + nextBatchId + "' id='cover" + nextBatchId + "' batchid='" + nextBatchId + "'>" + spinner + "</div>";
						leftArray[numberOfCovers - i].html(spinnerString);
					}
					setBorderBatchIds();
				}
			}
		}
		
		function afterAnim() {
			isMoving = 0;
			$('#mycarousel > li').each(addTooltip);
			if (state == "next") {
				loadDynamicContent(oldLastBatchId, numberOfCovers, state);
			} else if (state == "prev") {
				loadDynamicContent(oldFirstBatchId, numberOfCovers, state);
			}
			//$("#mycarousel > li > div.recordImageBlock.needsImage > div.coverBlock").each(addImage).removeClass("needsImage");
		}
		
		var oldFirstBatchId = firstBatchId;
		var oldLastBatchId = lastBatchId;
		beforeAnim();								// What to do before animating
		carousel.scroll(2 * numberOfCovers + 1, true); 	// the actual animation command
		afterAnim();								// What to do after animating
	}
	
	function loadDynamicContent(borderBatchId, howMany , dir) {
		var before = 0;
		var after = 0;
		var myQueueSpot = {};
		// Adjust first/last batch ID and set some ajax variables.
		if (dir == "prev") {
			before = howMany;
			after = 0;
			//coverAjaxPrevQueue.push(myQueueSpot);
		}
		if (dir == "next") {
			before = 0;
			after = howMany;
			//coverAjaxNextQueue.push(myQueueSpot);
		}
		$.ajax({
			url: browseUrl, 
			data: {
				batchId: borderBatchId,
				before: before,
				after: after,
				browse: "true",
				displayType: "data",
				embedded: embeddedBrowse,
				debug: debugFlag
			},
			success: function(data)
			{ 
				var normalizedData = $('<div>' + normalizeJspCoverOutput(data, false) + '</div>');
				if (dir == "next") {
					for (var i = 0; i < howMany; i++) {
						var nextBatchId = borderBatchId + (i + 1);
						var currentContents = $("ul.mycarousel > li.coverBrowse:eq(" + i + ")", normalizedData).html();
						if (currentContents && currentContents.length > 0) {
							$("#cover" + nextBatchId).parent().html(currentContents);//.addClass("needsCover");
						}
						else {
							$("#cover" + nextBatchId).html("Couldn't load this item");
						}
					}
				}
				else if (dir == "prev") {
					for (var i = 0; i < howMany; i++) {
						var j = howMany - (i + 1);
						var nextBatchId = borderBatchId - (i + 1);
						//alert(firstBatchId + " " + nextBatchId);
						var currentContents = $("ul.mycarousel > li.coverBrowse:eq(" + j + ")", normalizedData).html();
						if (currentContents && currentContents.length > 0) {
							$("#cover" + nextBatchId).parent().html(currentContents);//.addClass("needsCover");
						}
						else {
							$("#cover" + nextBatchId).html("Couldn't load this item");
						}
					}
				}
				// actually begin requesting and showing cover images - this could be done earlier in normalizeJspCoverOutput, but seems good to manipulate dom first?
				requestImage();
			},
			dataType: "html",
			timeout: 100000,
			error: function (a, b, c) {
				/*		alert('An error occured while generating additional Cover View contents.\n' 
							+ (b == 'timeout' 
								? 'The request for shelf data timed out.' 
								: ('Source of error: ' + b)
							) + "\n");*/
			}
		});
	}

	function loadStaticContent(borderBatchId, howMany, dir) {
		// loadStaticContent should do the following:
		// 0) get called from a carousel-not-instantiated context only.
		// 1) adjust the firstBatchId and lastBatchId variables to account for newly loaded items.
		// 2) Populate both the list view and the noninstantiated carousel view.
	
		//alert("Border batch ID on " + dir + ": " + borderBatchId);
		var before = 0;
		var after = 0;
		
		// Adjust firstBatchId/lastBatchId, and set some parameters for ajax. 
		if (dir == "left") {
			before = howMany;
			after = 0;
		}
		if (dir == "right") {
			before = 0;
			after = howMany;
		}
		$.ajax({
			url: browseUrl, 
			data: {
				batchId: borderBatchId,
				before: before,
				after: after,
				browse: "true",
				displayType: "data",
				embedded: embeddedBrowse,
				debug: debugFlag
			},
			success: function (data) {
				var normalizedList = $('<div>' + normalizeJspListOutput(data) + '</div>');
				if ('left' == dir) {
					for (i = howMany - 1; i >= 0; i--) {
						var nextBatchId = borderBatchId + (i - howMany);
						var newListElement = $('#listview > li.' + nextBatchId, normalizedList); 
						$('li.' + nextBatchId, currentListView).replaceWith(newListElement.addClass('hidden'));
					}
				}
				if ('right' == dir) {
					for (i = 0; i < howMany; i++) {
						var nextBatchId = borderBatchId + i + 1;
						var newListElement = $("#listview > li." + nextBatchId, normalizedList);
						$("li." + nextBatchId, currentListView).replaceWith(newListElement.addClass('hidden')); 
					}
				}
				if ( // this catches the case of a rapid clicking user.
					(borderBatchId <= currentBatchId + numberOfCovers && 'right' == dir)
					|| (borderBatchId >= currentBatchId && 'left' == dir)
				){
					browseShelf.redrawListView(currentBatchId, numberOfCovers);
				}
			},
			dataType: "html",
			timeout: 100000,
			error: function (a, b, c) {
				/*alert('An error occured while generating additional List View contents.\n' 
					+ (b == 'timeout' 
						? 'The request for shelf data timed out.' 
						: ('Source of error: ' + b)
					) + "\n");*/
			}
		});
	}
	
	function updateCurrentListReference(listId)
	{
		currentListView = (
			listId
			? $('#' + listId)
			: null
		);
	}
	
	function loadCarousel()
	{
		var clippingWidth = (
			'true' == embeddedBrowse
			? ($('#TB_ajaxContent').width() - 115) //50 for each side's button + 15 for ?
			: ( numberOfCovers * 170 + 10)
		);
		var oldCarouselStyleOverride = $('#jcarousel-sizing');
		if (oldCarouselStyleOverride){
			oldCarouselStyleOverride.remove();
		}
		$('head').append('<style id="jcarousel-sizing"><!-- '
			+ '.jcarousel-skin-tango .jcarousel-container-horizontal, .jcarousel-skin-tango .jcarousel-clip-horizontal{'
			+ 'width:' + clippingWidth + 'px;}' 
			+ ($.browser.safari || $.browser.msie ? safariAndIeCss : '')
			+ ' --></style>'
		);
		
		
		// Trim carousel elements to 15, centered around preferred item. 
		var currentCarouselPos = getCarouselPos(currentBatchId);
		var minCarousel = currentCarouselPos - 2 * numberOfCovers;
		var maxCarousel = currentCarouselPos + (3 * numberOfCovers - 1);
		$("#mycarousel > li").each( function(i) {
			if (i < minCarousel || i > maxCarousel) {
				$(this).remove();
			}
		});
		currentCarouselPos = getCarouselPos(currentBatchId);
		carouselStart = currentCarouselPos + 1;
		// end trim.
		
		
		$('#mycarousel').jcarousel({
			speed : 1000, // may be 'fast','normal','slow', int in ms
			start : carouselStart,
			scroll : numberOfCovers,
			visible : numberOfCovers,
			initCallback : initCallback
		});
		if($.browser.safari){
			instance.setup(); 
		}
		$('#mycarousel > li').each(addImageNoThrottle);
		//alert("what's in imageURLQueue? " + imageURLQueue);
		// After adding image server URL for each item to queue, actually begin requesting one at a time
		requestImage();
		$("#browseDataContent .jcarousel-prev").unbind('click').click(function () {
			animateCarousel(instance, "prev")
		});
		$("#browseDataContent .jcarousel-next").unbind('click').click(function () {
			animateCarousel(instance, "next")
		});
		$("div.recordImageBlock", containingDivObject).show();
		$('#mycarousel > li').each(addTooltip);
	}
	
	return {
		setContainingDiv : function(jQueryObject){
			containingDivObject = jQueryObject;
		},
		
		isCarouselReady : function(){
			return instance ? true : false;
		},
			
		setCoverNumber : function(number){
			if (number && ('number' == typeof(number + 1))){
				numberOfCovers = number;
			}
		},
		
		showCovers : function(eventObject)
		{
			//live.click has a bug, it triggers on all clicks. We only want left click.
			if (
				!eventObject || (!$.browser.msie && eventObject.button == 0) || $.browser.msie 
			) {
				$("div.viewSwitch div.coverView").unbind('click');
				$("div.viewSwitch div.listView").click(browseShelf.showList);
				var minBuffer = calculateMinimumBufferSize();
				$("a.browsePagerPrev").attr("href", "#").attr("onclick", "javascript: return false;");
				$("a.browsePagerNext").attr("href", "#").attr("onclick", "javascript: return false;");
				// these might have been set earlier in the fullpage context, so only set them if they're not already set.
				if (0 == firstBatchId) {
					firstBatchId = browseShelf.getFirstBatchId('mycarousel');
				}
				if (0 == lastBatchId) {
					lastBatchId = browseShelf.getLastBatchId('mycarousel');
				}
				if (0 == currentBatchId) {
					currentBatchId = browseShelf.getCurrentBatchId('mycarousel', carouselStart - 1);
				}
				if (0 == matchedBatchId && 'startsWith' == browseShelf.getMatchType()) {
					matchedBatchId = browseShelf.getMatchedBatchId();
				}		
				$("#browseDataContent").html(spinner + "<br /><br /><br />");
				// Download new content and then switch to cover view.
				$.ajax({
					url: browseUrl, 
					data: {
						batchId: currentBatchId,
						before: minBuffer,
						after: minBuffer + numberOfCovers,
						browse: "true",
						displayType: "data",
						embedded: embeddedBrowse,
						debug: debugFlag
					},
					success: function (data) {
						var normalizedData = normalizeJspCoverOutput(data, true);
						$("#browseDataContent").html(normalizedData);
						updateCurrentListReference('mycarousel');
						firstBatchId = browseShelf.getFirstBatchId('mycarousel');
						lastBatchId = browseShelf.getLastBatchId('mycarousel');
						carouselStart = minBuffer + 1;
						currentListView.hide(1, show);
					},
					dataType: "html",
					timeout: 100000,
					error: function (a, b, c) {
						/*alert('An error occured while generating initial Cover View contents.\n' 
							+ (b == 'timeout' 
								? 'The request for shelf data timed out.' 
								: ('Source of error: ' + b)
							) + "\n");*/
					}
				});		
			}
		},
		
		initList : function(){
			$("div.viewSwitch div.coverView").click(browseShelf.showCovers);
			updateCurrentListReference('listview');
			firstBatchId = browseShelf.getFirstBatchId('listview');
			lastBatchId = browseShelf.getLastBatchId('listview');
			currentBatchId = browseShelf.getCurrentBatchId('listview', carouselStart - 1);
			if ( 0 == matchedBatchId && 'startsWith' == browseShelf.getMatchType()) {
				matchedBatchId = browseShelf.getMatchedBatchId();
			}
			browseShelf.redrawListView(currentBatchId, numberOfCovers);
		},
		
		initCovers : function()
		{
			$("div.viewSwitch div.listView").click(browseShelf.showList);
			$("a.browsePagerPrev").attr("href", "#").attr("onclick", "javascript: return false;");
			$("a.browsePagerNext").attr("href", "#").attr("onclick", "javascript: return false;");
			updateCurrentListReference('mycarousel');
			// these might have been set earlier in the fullpage context, so only set them if they're not already set.
			if (0 == firstBatchId) {
				firstBatchId = browseShelf.getFirstBatchId('mycarousel');
			}
			if (0 == lastBatchId) {
				lastBatchId = browseShelf.getLastBatchId('mycarousel');
			}
			var totalCoversLoaded = lastBatchId - firstBatchId + 1; // please leave these in, they will be needed in the next refactor
			var matchedListPosition = Math.floor((totalCoversLoaded)/2) - 1; // please leave these in, they will be needed in the next refactor
			var currentListPosition = matchedListPosition - Math.floor((numberOfCovers - 1)/2); // please leave these in, they will be needed in the next refactor
			if (0 == currentBatchId ) {
				currentBatchId = browseShelf.getCurrentBatchId('mycarousel', currentListPosition); //#TODO this no longer worked as intended... hense the + 1
			}
			if (0 == matchedBatchId && 'startsWith' == browseShelf.getMatchType()) {
				matchedBatchId = browseShelf.getMatchedBatchId();
			}
			if ('true' == embeddedBrowse){
				var requiredMatchedBatchId = (
					matchedBatchId
					? matchedBatchId
					: browseShelf.getMatchedBatchId()
				);
				carouselStart = $("li", currentListView).index($('.recordImageBlock.' + requiredMatchedBatchId, currentListView).parent()) + 1 - Math.floor(numberOfCovers/2) + ((numberOfCovers + 1 ) % 2);
			}
			currentListView.hide(1, show);
		},
		
		showList : function(eventObject)
		{
			//live.click has a bug, it triggers on all clicks. We only want left click.
			if (
				!eventObject || (!$.browser.msie && eventObject.button == 0) || $.browser.msie 
			) {	
				$("div.viewSwitch div.listView").unbind('click');
				$("div.viewSwitch div.coverView").click(browseShelf.showCovers);
				var minBuffer = calculateMinimumBufferSize();
				// Get the current position of the carousel view.
				if(instance){
					carouselStart = instance.first;
					currentBatchId = parseInt($("#mycarousel li.jcarousel-item-" + instance.first + " div.recordImageBlock").attr("batchId"), 10);
				}	
				$("#browseDataContent").html(spinner + "<br /><br /><br />");
				// Download new list data based on our current batch ID.
				$.ajax({
					url: browseUrl, 
					data: {
						batchId: currentBatchId,
						before: minBuffer -1, //minBuffer,
						after: minBuffer, //minBuffer + numberOfCovers,
						browse: "true",
						displayType: "data",
						embedded: embeddedBrowse,
						debug: debugFlag
					},
					success: function (data) {
						var oldCurrentBatchId = currentBatchId;
						browseShelf.unloadCovers();
						$("#browseDataContent").html(normalizeJspListOutput(data));
						updateCurrentListReference('listview');
						firstBatchId = browseShelf.getFirstBatchId('listview');
						lastBatchId = browseShelf.getLastBatchId('listview');
						currentBatchId = oldCurrentBatchId;
						browseShelf.redrawListView(currentBatchId, numberOfCovers);
						showListManipulation();
					},
					dataType: "html",
					timeout: 100000,
					error: function (a, b, c) {
						/*alert('An error occured while generating initial List View contents.\n' 
							+ (b == 'timeout' 
								? 'The request for shelf data timed out.' 
								: ('Source of error: ' + b)
							) + "\n");*/
					}
				});				
			}
			return false;
		},
		
		unloadCovers : function(eventObject)
		{
			if(instance) {
				$(window).unbind('resize', instance.funcResize);
				updateCurrentListReference(null);
			}
			instance = null;
			firstBatchId = 0;
			lastBatchId = 0;
			currentBatchId = 0;
			coverAjaxNextQueue = [];
			coverAjaxPrevQueue = [];
		},
		
		getFirstBatchId : function(listId)
		{
			var additionalResolver = ('mycarousel' == listId ? ' div.recordImageBlock' : '');
			return parseInt($('#' + listId + ' > li:first' + additionalResolver).attr('batchId'), 10);
		},

		getLastBatchId : function(listId)
		{
			var additionalResolver = ('mycarousel' == listId ? ' div.recordImageBlock' : '');
			return parseInt($('#' + listId + ' > li:last' + additionalResolver).attr('batchId'), 10);
		},
		
		getCurrentBatchId : function(listId, listPosition)
		{
			var additionalResolver = ('mycarousel' == listId ? ' div.recordImageBlock' : '');
			return parseInt($('#' + listId + ' > li:eq(' + listPosition + ')' + additionalResolver).attr('batchId'), 10);
		},

		getMatchedBatchId : function()
		{ // #TODO seems like a dangerous way to do this
			return parseInt($("ul.debug").attr("matchedbatchid"), 10);
		},
		
		getMatchType : function()
		{ // #TODO seems like a dangerous way to do this
			return $("ul.debug").attr("matchtype");
		},
		
		listPager : function (dir, howMany)
		{
			// listPager needs to:
			// 0) Get called on click of the list view's pager.
			// 1) Execute only if carousel is not loaded.
			// 2) Add empty items to list view only
			// 3) Call a function (loadStaticContent) to populate the empty cells.
			// 4) Calculate the new currentBatchId and redraw list view appropriately.
			// 5) adjust carousel start point as necessary
			var addlBuffer = calculateBufferSize();
			var bufferStartId = -99;
			var currSize = $("#listview > li").length;
			var newListItems = [];
			// create some list cells on the left or right and populate them with spinner.
			if ('left' == dir) {
				currentBatchId -= howMany;	
				var currBuffer = getListPos(currentBatchId);
				if (currBuffer <= calculateMinimumBufferSize()) {
					//alert ("Current buffer is " + currBuffer + ". Adding buffer to " + dir);
					if(-1 == currBuffer){
						throw 'buffer underflow error';
					}
					for (i = 1; i <= addlBuffer; i++) {
						// Add new slots to list view
						var nextBatchId = firstBatchId - i;
						newListItems[newListItems.length] = "<li class='" + nextBatchId + "' style='display: none;' batchid='" + nextBatchId + "'><hr />" + spinner + "</li>";
					}
					currentListView.prepend(newListItems.reverse().join(''));
					bufferStartId = firstBatchId;
					firstBatchId = firstBatchId - addlBuffer;
				}
			}
			if ('right' == dir) {
				currentBatchId += howMany;
				var currBuffer = currSize - (getListPos(currentBatchId) + howMany);
				if (currBuffer <= calculateMinimumBufferSize()) {
					//alert ("Current buffer is " + currBuffer + ". Adding buffer to " + dir);
					for (i = 1; i <= addlBuffer; i++) {
						// add new slots to list view
						var nextBatchId = lastBatchId + i;
						newListItems[newListItems.length] = "<li class='" + nextBatchId + "' style='display: none;' batchid='" + nextBatchId + "'><hr />" + spinner + "</li>";
					}
					currentListView.append(newListItems.join(''));
					bufferStartId = lastBatchId;
					lastBatchId = lastBatchId + addlBuffer;
				}
			}
			carouselStart = getListPos(currentBatchId) + 1;
			this.redrawListView(currentBatchId, howMany);
			if(-99 != bufferStartId){
				loadStaticContent(bufferStartId, addlBuffer, dir);	// Populate list view.
			}
			return false;
		},

		redrawListView : function (batchId, howMany) {
			listStart = getListPos(batchId);
			listEnd = listStart + howMany - 1;
			$("#listview > li").hide();
			for (var i = listStart; i <= listEnd; i++){
				$("#listview > li:eq(" + i + ")").show();
			}
		}
	};
}();