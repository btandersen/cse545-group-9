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
        <title>CSE 545 Group 9 | Web Document Management System</title>
        <LINK href="../css/style.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id="container">
            <%@include file="../WEB-INF/jspf/header.jspf" %>
            <div id="content"><h1>File Upload</h1>
                <%@include file="/WEB-INF/jspf/fileuploadform.jspf" %>
                <a href="user.jsp" >Return to User Page</a>
            </div>
            <%@include file="../WEB-INF/jspf/footer.jspf" %>
        </div>
    </body>
</html>
