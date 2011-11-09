<%-- 
    Document   : passwordreset
    Created on : Nov 8, 2011, 5:46:11 PM
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Password Reset Page</title>
    </head>
    <body>
        <h1>Reset your password</h1>
        <form action="ResetPassword" method="POST">
            <table border="0">
                <tbody>
                    <tr>
                        <td align="right">Current Password:&nbsp;</td>
                        <td>
                            <input type="password" name="current_pwd" />
                        </td>
                    </tr>
                    <tr>
                        <td align="right">New Password:&nbsp;</td>
                        <td>
                            <input type="password" name="new_pwd" />
                        </td>
                    </tr>
                    <tr>
                        <td align="right">Re-enter New Password:&nbsp;</td>
                        <td>
                            <input type="password" name="new_pwd1" />
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input type="submit" value="Update" /></td>
                    </tr>
                </tbody>
            </table>
        </form>
    </body>
</html>
