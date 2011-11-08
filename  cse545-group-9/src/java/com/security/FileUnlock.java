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
import java.util.GregorianCalendar;
import javax.sql.*;
import javax.naming.*;

/**
 *
 * @author Administrator
 */
public class FileUnlock extends HttpServlet
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
            ds = (DataSource) ctx.lookup("jdbc/MySQLDataSource");
            conn = ds.getConnection();
        }
        catch (Exception e)
        {
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        boolean result = false;

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String user = request.getRemoteUser();
        String title = request.getParameter("title");

        try
        {
            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);


            ResultSet userRs = null;
            ResultSet docRs = null;


            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            String docQuery = "SELECT * FROM " + "mydb" + "." + "Docs"
                    + " WHERE " + "title" + " = '" + title + "'";

            userRs = userStmt.executeQuery(userQuery);
            docRs = docStmt.executeQuery(docQuery);

            if (userRs.next() && docRs.next())
            {
                String luid = String.valueOf(userRs.getInt("uid"));
                String ldid = String.valueOf(docRs.getInt("did"));

                try
                {
                    Statement lockStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    String lockQuery = "SELECT * FROM mydb.Locked WHERE "
                            + "ldid=" + ldid + " AND luid=" + luid;

                    ResultSet lockRs = lockStmt.executeQuery(lockQuery);

                    if (lockRs.next())
                    {
                        lockRs.deleteRow();
                        result = true;

                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Unlock</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>File unlocked sucessfully...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;FileUnlockPage");
                    }
                    else
                    {
                        // user did not have it locked
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Unlock</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>File was not locked by current user...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;FileUnlockPage");
                    }
                }
                catch (Exception e)
                {
                    // lockStmt SQL error
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Unlock</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error accesing file status...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;FileUnlockPage");
                }

            }
            else
            {
                // bad user or doc
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Unlock</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Invalid user or document...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;FileUnlockPage");
            }
        }
        catch (Exception e)
        {
            // SQL error
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Unlock</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Error processing request...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;FileUnlockPage");
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "unlocked','"
                    + String.valueOf(result) + "')";
            logStmt.executeUpdate(logQuery);
            logStmt.close();
        }
        catch (Exception e)
        {
            // logging failed
            e.printStackTrace();
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
