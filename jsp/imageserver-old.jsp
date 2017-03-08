<%@ page language="java"%>

<%
	String isbn=request.getParameter("isbn");
	String size=request.getParameter("size");alert("IN imageserver.jsp");
	out.println("http://www.syndetics.com/index.aspx?isbn=" + isbn + "/" + size + "C.GIF&client=403-220-5953");
%>