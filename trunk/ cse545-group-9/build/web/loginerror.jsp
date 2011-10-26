<%-- 
    Document   : loginerror
    Created on : Oct 23, 2011, 8:26:42 PM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Error</title>
    </head>
    <body>
        <h1>Login Page</h1>
        <p>
            There was an error logging you in.
        </p>
        <p>
            Please enter your username and password to access the application...
        </p>
        <%@include file="WEB-INF/jspf/loginform.jspf" %>
    </body>
</html>
