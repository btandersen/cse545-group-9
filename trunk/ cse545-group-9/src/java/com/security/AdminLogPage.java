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
import javax.sql.*;
import javax.naming.*;

/**
 *
 * @author Administrator
 */
public class AdminLogPage extends HttpServlet
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
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Admin Log Page</title>");
            out.println("<link type=\"text/css\" href=\"../css/style.css\" />");
            out.println("</head>");
            out.println("<body>");
            out.println("<a href=\"admin.jsp\" >Return to Admin Page</a>");

            try
            {
                Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                ResultSet userRs = null;
                String userQuery = "";
                String uname = request.getRemoteUser();
                userQuery = "SELECT U.uid, U.uname, U.role, U.dept, G.groupid "
                        + "FROM Users U, Groups G "
                        + "WHERE U.uname='" + uname + "' "
                        + "AND U.uname=G.uname";

                userRs = userStmt.executeQuery(userQuery);

                if (userRs.next())
                {
                    out.println("<table>");
                    out.println("<tr><th>User ID</th><th>User Name</th><th>Role</th><th>Dept</th><th>Group</th></tr>");
                    out.println("<tr>");
                    out.println("<td>" + userRs.getInt("uid") + "</td>"
                            + "<td>" + userRs.getString("uname") + "</td>"
                            + "<td>" + Roles.values()[userRs.getInt("role")] + "</td>"
                            + "<td>" + userRs.getString("dept") + "</td>"
                            + "<td>" + userRs.getString("groupid") + "</td>");
                    out.println("</tr>");
                    out.println("</table>");
                }
                else
                {
                    // user not found in database
                }
            }
            catch (SQLException e)
            {
                // error retrieving current user from db
            }


            if (request.isUserInRole("admin"))
            {
                try
                {
                    Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                    ResultSet logRs = null;

                    String logQuery = "SELECT * FROM mydb.Log ORDER BY idLog DESC";

                    logRs = logStmt.executeQuery(logQuery);

                    if (logRs.next())
                    {
                        out.println("<table>");
                        out.println("<th>Document Access Logs</th>");
                        out.println("<tr><th>Log ID</th><th>Username</th><th>Document</th><th>Action</th><th>Successful</th><th>Time</th>");

                        do
                        {
                            out.println("<tr>");
                            out.println(
                                    "<td>" + logRs.getString("idLog") + "</td>"
                                    + "<td>" + logRs.getString("uname") + "</td>"
                                    + "<td>" + logRs.getString("title") + "</td>"
                                    + "<td>" + logRs.getString("action") + "</td>"
                                    + "<td>" + logRs.getString("result") + "</td>"
                                    + "<td>" + logRs.getString("time") + "</td>");
                            out.println("</tr>");
                        }
                        while (logRs.next());

                        out.println("</table>");
                    }
                    else
                    {
                        // no logs found
                        out.println("<h1>No logs found...</h1>");
                    }
                }
                catch (Exception e)
                {
                    // SQL error
                    out.println("<h1>Error accessing logs...</h1>");
                }
            }
            else
            {
                // not an admin
            }
        }
        catch (Exception e)
        {
            // output stream error
            out.println("<h1>Error reporting results...</h1>");
        }

        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
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
