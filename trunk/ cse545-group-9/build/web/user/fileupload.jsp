<%-- 
    Document   : fileupload
    Created on : Oct 24, 2011, 6:59:58 AM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>File Upload Page</title>
    </head>
    <body>
        <h1>File Upload</h1>
        <%@include file="/WEB-INF/jspf/fileuploadform.jspf" %>
        <a href="user.jsp" >Return to User Page</a>
        <%@include file="/WEB-INF/jspf/logoutform.jspf" %>
    </body>
</html>
