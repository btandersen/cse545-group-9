<%-- 
    Document   : login
    Created on : Oct 23, 2011, 8:26:24 PM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CSE 545 Group 9 | Web Document Management System</title>
        <LINK href="/WebDocManager/css/style.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id="container">
            <%@include file="WEB-INF/jspf/header.jspf" %>
            <div id="content">
                <%@include file="WEB-INF/jspf/loginform.jspf" %>
            </div>
            <%@include file="WEB-INF/jspf/footer.jspf" %>
        </div>
    </body>
</html>