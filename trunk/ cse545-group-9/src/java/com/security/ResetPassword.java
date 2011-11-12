/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.security;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.*;
import javax.naming.*;

/**
 *
 * @author Administrator
 */
public class ResetPassword extends HttpServlet
{
    InitialContext ctx;
    DataSource ds;
    Connection conn;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        ctx = null;
        try
        {
            ctx = new InitialContext();
            //ds = (DataSource) ctx.lookup("java:comp/env/jdbc/MySQLDataSource");
            ds = (DataSource) ctx.lookup("jdbc/MySQLDataSource");
            conn = ds.getConnection();
        }
        catch (Exception e)
        {
            //
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        final String PWD_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\`\\!\\@\\$\\%\\^\\&\\*\\(\\)\\-\\_\\=\\+\\[\\]\\;\\:\\'\"\\,\\<\\.\\>\\/\\?]).{8,20})";

        String uname = request.getRemoteUser();
        String currentPwd = request.getParameter("current_pwd");
        String pwd = request.getParameter("new_pwd");
        String pwd1 = request.getParameter("new_pwd1");

        Pattern pwdPattern = Pattern.compile(PWD_REGEX);
        Matcher pwdMatcher = null;
        pwdMatcher = pwdPattern.matcher(pwd);

        boolean pwdMatch = pwdMatcher.matches();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try
        {
            if (pwdMatch
                    && pwd.equals(pwd1)
                    && !pwd.contains(uname)
                    && (pwd.indexOf("!") != 0)
                    && (pwd.indexOf("?") != 0)
                    && !uname.contains(pwd.substring(0, 3)))
            {
                String updateQuery = "";

                try
                {
                    //open connection to db
                    Statement update = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                    //query = "SELECT U.uname,U.pwd FROM mydb.users WHERE U.uname='" + uname + "' AND U.pwd='md5('" + currentPwd + "')'";
                    updateQuery = "UPDATE mydb.Users U SET U.pwd=md5('" + pwd + "') WHERE uname='" + uname + "' AND U.pwd=md5('" + currentPwd + "')";

                    update.executeUpdate(updateQuery);
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Reset Password</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Password updated</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    update.close(); //close the statement
                    response.setHeader("Refresh", "5;user.jsp");
                }
                catch (SQLException e) //generic SQL error
                {
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Reset Password</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error updating password</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;user.jsp");
                }
            }
            else
            {
                //bad pwd
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Reset Password</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>New password does not meet requirements...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;user.jsp");
            }
        }
        catch (Exception e)
        {
            //general error
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Reset Password</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Error encountered processing your request...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;user.jsp");
        }

        out.close();
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }
}
