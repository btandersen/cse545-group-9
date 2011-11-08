/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.security;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

import javax.servlet.ServletConfig;

/**
 *
 * @author Administrator
 */
public class DeleteUser extends HttpServlet
{
    private InitialContext ctx;
    private DataSource ds;
    private Connection conn;

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try
        {
            if (request.isUserInRole("admin"))
            {
                String uname = request.getParameter("uname");

                try
                {
                    Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                    ResultSet userRs = null;

                    String userQuery = "SELECT * FROM " + "mydb" + "." + "Users WHERE uname='" + uname + "'";

                    userRs = userStmt.executeQuery(userQuery);

                    if (userRs.next())
                    {
                        userRs.deleteRow();

                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>Delete User</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>User deleted sucessfully...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;UpdateUserPage");
                    }
                    else
                    {
                        // bad user to delete
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>Delete User</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>No such user to delete...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;UpdateUserPage");
                    }
                }
                catch (Exception e)
                {
                    // SQL Exception
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Delete User</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error processing request...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;UpdateUserPage");
                }
            }
        }
        finally
        {
            out.close();
        }
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
