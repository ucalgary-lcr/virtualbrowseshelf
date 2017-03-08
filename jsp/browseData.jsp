<%@ page contentType="text/html;charset=UTF-8" language="java" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:useBean id="browseData" scope="request" type="edu.ncsu.lib.browse.BrowseDataBean" />

<div class="browseDataContent" id="browseDataContent">
	<ul id="mycarousel" class="mycarousel jcarousel-skin-tango">
		<c:forEach items="${browseData.catKeys}" var="batchIdItem" varStatus="iter">
			<c:set var="catkey" value="${browseData.catKeys[batchIdItem.key]}" />
	
			<c:if test='${matchType eq "closestMatch" && batchIdItem.key eq browseData.batchId && param.browse ne "true"}'>
				<li class="browseItem coverBrowse ${batchIdItem.key}">
					<div class="recordImageBlock noResults selected">
						<div class="coverBlock noResultsBlock">
							<div class="noResultsCover">
								<div class="noResultsContents">
									<table>
										<tr>
											<td align="center" valign="middle">
												<div class="noResultsMsg">
													The call number ${sessionScope.virtualBrowseCallNumber} was not found. Here are the nearest call numbers.<br/>
												</div>
											</td>
										</tr>
									</table>
								</div>
							</div>
						</div>
						<div class="locationBlock">
							&nbsp;
						</div>
					</div>
				</li>
			</c:if> 
		
			<li class="browseItem coverBrowse ${batchIdItem.key}">
				<div class="recordImageBlock ${batchIdItem.key} <c:if test='${browseData.matchType eq "startsWith" && batchIdItem.key eq browseData.batchId}'>selected</c:if>" id="cover${batchIdItem.key}" batchId="${batchIdItem.key}">
					<div class="coverBlock">
						<a href="http://sirsi1.lib.ucalgary.ca/uhtbin/ckey.cgi/${browseData.catKeys[batchIdItem.key]}" style="text-decoration: none;" target="_blank">
							<div class="coverImage fakeImage" isbn="${browseData.isBns[batchIdItem.key]}" oclc="" upc="">
								<div class="coverContents cover${batchIdItem.key%5 + 1}">		
									<div class='coverShadowRight'><div class='coverShadowBottom'><div class='coverShadowTop'><div class='coverShadowLeft'><div class='coverShadowCorner'>						
									<table class="coverTable">
										<tr>
											<td align="center" valign="middle">
												<div class="title">	
														<c:if test='${browseData.shortTitles[batchIdItem.key] ne ""}'>
															${browseData.shortTitles[batchIdItem.key]}	
														</c:if>
														<c:if test='${browseData.shortTitles[batchIdItem.key] == ""}'>
															${browseData.tiTles[batchIdItem.key]}		
														</c:if>				
												</div>
												<div class="tilde">
													<c:if test='${not empty(browseData.auThors[batchIdItem.key])}'>
													~
													</c:if>													
												</div>
												<div class="author">
													<c:if test='${not empty(browseData.auThors[batchIdItem.key])}'>
													${browseData.auThors[batchIdItem.key]}
													</c:if>
												</div>
											</td>
										</tr>
									</table>
									</div></div></div></div></div>
								</div>
							</div>
						</a>
					</div>				
					<div class="locationBlock">
						<table>
							<tr>
								<td>
									<div style="font-family: Arial; font-size: 14px;">
										<c:if test='${browseData.locations[batchIdItem.key]!="CHECKEDOUT"}'>
											<img src="/browse/include/icon-available.png">
										</c:if>
										<c:if test='${browseData.locations[batchIdItem.key]=="CHECKEDOUT"}'>
												<img src="/browse/include/icon-unavail.png">&nbsp;&nbsp;Due:${browseData.dues[batchIdItem.key]}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;		
										</c:if>												
										<c:if test='${browseData.locations[batchIdItem.key]=="Internet accessible"}'>
																<a href="${browseData.urLs[batchIdItem.key]}" target="_blank">${browseData.locations[batchIdItem.key]}</a>	
										</c:if>
										<c:if test='${browseData.locations[batchIdItem.key]!="Internet accessible" && browseData.locations[batchIdItem.key]!="CHECKEDOUT"}'>
										${browseData.locations[batchIdItem.key]}
										</c:if>	
										<c:if test='${browseData.multipleCopies[batchIdItem.key]>1}'>
										<br><a href="http://sirsi1.lib.ucalgary.ca/uhtbin/ckey.cgi/${browseData.catKeys[batchIdItem.key]}" target="_blank">Check other locations</a>
										</c:if>	
										</div>
								</td>
							</tr>
						</table>
					</div>
					<div class="mouseoverBlock">
						<div class="title">
							${browseData.tiTles[batchIdItem.key]}
							<span class="pubYear">${browseData.years[batchIdItem.key]}</span>
						</div>
						<div class="author">
							${browseData.auThors[batchIdItem.key]}
						</div>
						<div class="callNum">
							<c:choose>
								<c:when test='${fn:contains(browseData.callNums[batchIdItem.key]," v.")}'>
									${fn:substringBefore(browseData.callNums[batchIdItem.key], " v.")}
								</c:when>
								<c:otherwise>
									<c:choose>
										<c:when test='${fn:contains(browseData.callNums[batchIdItem.key]," V.")}'>
											${fn:substringBefore(browseData.callNums[batchIdItem.key], " V.")}
										</c:when>
										<c:otherwise>
											${browseData.callNums[batchIdItem.key]}
										</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
						</div>
						<c:if test='${param.debug eq "true" }'>
							<div class="batchID">
											</div>
						</c:if>
					</div>
				</div>
			</li>		
		</c:forEach>
	</ul>
	
	
	<ul class="debug" matchedBatchId="${browseData.batchId}" matchType="${matchType}" <c:if test='${param.debug ne "true" }'>style="display: none;"</c:if>>
		<li>
			Searched call number: ${param.callNumber}<br/>
			Matched batch ID: ${browseData.batchId}<br />
			Match type: ${browseData.matchType}<br />
			Browse: <c:choose><c:when test='${param.browse eq "true" }'>True</c:when><c:otherwise>False</c:otherwise></c:choose>
			
		</li>
	</ul>
	
	<ul id="listview" class="listview">
		<c:forEach items="${browseData.catKeys}" var="batchIdItem" varStatus="iter">
			<c:set var="catkey" value="${browseData.catKeys[batchIdItem.key]}" />			
			<li class="browseItem listBrowse <c:if test="${iter.index lt 12 || iter.index gt 16 }">hidden</c:if> <c:if test='${matchType eq "startsWith" && batchIdItem.key eq browseData.batchId}'>selected</c:if> ${batchIdItem.key}" id="list${batchIdItem.key}" batchId="${batchIdItem.key}">
				<c:if test='${matchType eq "closestMatch" && batchIdItem.key eq browseData.batchId && param.browse ne "true"}'>
					<hr />
					<div class="recordListBlock noResults">
						<div class="title noResultsMsg">
							The call number <em>${sessionScope.virtualBrowseCallNumber}</em> was not found. Here are the nearest call numbers.<br/>
						</div>
					</div>
				</c:if>
			
				<hr />
				<div class="recordListBlock" batchId="${batchIdItem.key}">				
					<div class="title">
						<a href="http://sirsi1.lib.ucalgary.ca/uhtbin/ckey.cgi/${browseData.catKeys[batchIdItem.key]}" style="text-decoration: none;" target="_blank">${browseData.tiTles[batchIdItem.key]}</a>						
					</div>
					<table class="metadata">
						<c:if test='${param.debug eq "true" }'>
							<tr>
								<th class="label">
									Batch ID:
								</th>
								<td class="data">
									${batchIdItem.key}
								</td>
							</tr>
						</c:if>
						<tr>
							<th class="label author">
								Author:
							</th>
							<td class="data author">
								<c:if test='${not empty(browseData.auThors[batchIdItem.key])}'>
										${browseData.auThors[batchIdItem.key]}
								</c:if>
							</td>
						</tr>
						<tr>
							<th class="label published">
								Published:
							</th>
							<td class="data published">
								${browseData.years[batchIdItem.key]}
							</td>
						</tr>
						<tr>
							<th class="label callnum">
								Call number:
							</th>
							<td class="data callnum">
								${browseData.callNums[batchIdItem.key]}
							</td>
						</tr>
						<tr>
							<th class="label locations">
								Locations:
							</th>
							<td class="data locations">
								<table>
									<tr>
										<td>
												<c:if test='${browseData.locations[batchIdItem.key]!="CHECKEDOUT"}'>
											<img src="/browse/include/icon-available.png">
										</c:if>
										<c:if test='${browseData.locations[batchIdItem.key]=="CHECKEDOUT"}'>
													<img src="/browse/include/icon-unavail.png">&nbsp;&nbsp;Due:${browseData.dues[batchIdItem.key]}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;						
										</c:if>		
										<c:if test='${browseData.locations[batchIdItem.key]=="Internet accessible"}'>
																<a href="${browseData.urLs[batchIdItem.key]}" target="_blank">${browseData.locations[batchIdItem.key]}</a>	
										</c:if>
										<c:if test='${browseData.locations[batchIdItem.key]!="Internet accessible" && browseData.locations[batchIdItem.key]!="CHECKEDOUT"}'>
										${browseData.locations[batchIdItem.key]}
										</c:if>							
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</div>
			</li>
		</c:forEach>
	</ul>
</div>