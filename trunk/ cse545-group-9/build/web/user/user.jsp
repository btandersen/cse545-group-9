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
                <h1>User Page</h1>
                <div id="menu">
                    <p><a href="fileupload.jsp">Upload a file</a></p>
                    <p><a href="FileUpdatePage">Update a file</a></p>
                    <p><a href="FileDownloadPage">Download a file</a></p>
                    <p><a href="FileLockPage">Lock a file</a></p>
                    <p><a href="FileUnlockPage">Unlock a file</a></p>
                    <p><a href="FileDeletePage">Delete a file</a></p>
                    <p><a href="FileSharePage">Share a file</a></p>
                    <p><a href="passwordreset.jsp">Reset password</a></p>
                    <%@include file="../WEB-INF/jspf/logoutform.jspf" %>
                </div>
                <%@include file="../WEB-INF/jspf/file_reqs.jspf" %>
            </div>
            <%@include file="../WEB-INF/jspf/footer.jspf" %>
        </div>
    </body>
</html>