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
            out.println("</head>");
            out.println("<body>");

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

        out.println("<a href=\"admin.jsp\" >Return to Admin Page</a>");
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
