<%-- 
    Document   : index
    Created on : Oct 23, 2011, 8:11:28 AM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CSE 545 Group 9 | Web Document Management System</title>
    </head>
    <body>
        <h1>Web Document Management System</h1>
        <h2>Login</h2>
        <p>
            Please enter your username and password to access the application...
        </p>
        <%@include file="WEB-INF/jspf/loginform.jspf" %>
        <h2>Create New User Request</h2>
        <p>
            Please enter your username and password to access the application...
        </p>
        <%@include file="WEB-INF/jspf/newuserform.jspf" %>
    </body>
</html>
