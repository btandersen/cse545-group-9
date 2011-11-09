<%-- 
    Document   : user
    Created on : Oct 23, 2011, 8:49:58 PM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>User Page</h1>
        <p><a href="fileupload.jsp">Upload a file</a></p>
        <p><a href="FileUpdatePage">Update a file</a></p>
        <p><a href="FileDownloadPage">Download a file</a></p>
        <p><a href="FileLockPage">Lock a file</a></p>
        <p><a href="FileUnlockPage">Unlock a file</a></p>
        <p><a href="FileDeletePage">Delete a file</a></p>
        <p><a href="FileSharePage">Share a file</a></p>
        <p><a href="passwordreset.jsp">Reset password</a></p>
        <p><%@include file="/WEB-INF/jspf/logoutform.jspf" %>
    </body>
</html>
