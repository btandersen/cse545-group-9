<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>CSE 545 Group 9 | Web Document Management System</title>
        <LINK href="../css/style.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id="container">
            <%@include file="../WEB-INF/jspf/header.jspf" %>
            <div id="content">
                <h1>Admin Page</h1>
                <p><a href="UpdateUserPage">Update or delete a user</a></p>
                <p><a href="AdminLogPage">See file access history</a></p>
                <%@include file="/WEB-INF/jspf/logoutform.jspf" %>
            </div>
            <%@include file="../WEB-INF/jspf/footer.jspf" %>
        </div>
    </body>
</html>
