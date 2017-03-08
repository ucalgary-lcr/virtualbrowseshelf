<%@ page language="java"%>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %> 
<%
	String isbn=request.getParameter("isbn");
	String size=request.getParameter("size");

//	out.println("http://www.syndetics.com/index.aspx?isbn=" + isbn + "/" + size + "C.GIF&client=403-220-5953");

	 URL u;
     InputStream is = null;
     DataInputStream dis;
     String s;
      try {
//		u = new URL("http://www.syndetics.com/index.aspx?isbn=9780415961523/MC.GIF&client=403-220-5953"); //image
//		u = new URL("http://www.syndetics.com/index.aspx?isbn=9781572739239/MC.GIF&client=403-220-5953"); //no image
        u = new URL("http://www.syndetics.com/index.aspx?isbn=" + isbn + "/" + size + "C.GIF&client=403-220-5953");
		 
         is = u.openStream();         // throws an IOException
         dis = new DataInputStream(new BufferedInputStream(is));
		 int len=0;
		 while ((s = dis.readLine()) != null) {
             //   out.println("yo" + s.length()); 
				len+=s.length();
       }
		if (len<100){ //Not Found
		out.println(""); }
		if (len>100){ //Found Image cover
		out.println(u); }
		
	     } catch (MalformedURLException mue) {
         out.println("Ouch - a MalformedURLException happened.");
         mue.printStackTrace();
      } catch (IOException ioe) {
         out.println("Oops- an IOException happened.");
         ioe.printStackTrace();
      } finally {

        try {
            is.close();
         } catch (IOException ioe) {
            // just going to ignore this one
         }
      } // end of 'finally' clause
%>
