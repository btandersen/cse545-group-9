<%-- 
    Document   : admin
    Created on : Oct 23, 2011, 8:49:32 PM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Admin Page</title>
    </head>
    <body>
        <h1>Admin Page</h1>
        <p><a href="UpdateUserPage">Update or delete a user</a></p>
        <p><a href="AdminLogPage">See file access history</a></p>
        <%@include file="/WEB-INF/jspf/logoutform.jspf" %>
    </body>
</html>
