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
import java.util.GregorianCalendar;
import javax.sql.*;
import javax.naming.*;

/**
 *
 * @author Administrator
 */
public class FileShare extends HttpServlet
{
    private InitialContext ctx;
    private DataSource ds;
    private Connection conn;

    @Override
    public void init()
    {
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

        String permSet = "R,U,L";

        String user = request.getRemoteUser();
        String title = request.getParameter("title");
        String shareUser = request.getParameter("shareuser");
        String perm = request.getParameter("perm");

        if (permSet.contains(perm))
        {
            try
            {
                Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Statement shareUserStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                ResultSet userRs = null;
                ResultSet docRs = null;
                ResultSet shareUserRs = null;

                String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                        + " WHERE " + "uname" + " = '" + user + "'";

                String docQuery = "SELECT * FROM " + "mydb" + "." + "Docs"
                        + " WHERE " + "title" + " = '" + title + "'";

                String shareUserQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                        + " WHERE " + "uname" + " = '" + shareUser + "'";

                userRs = userStmt.executeQuery(userQuery);
                docRs = docStmt.executeQuery(docQuery);
                shareUserRs = shareUserStmt.executeQuery(shareUserQuery);

                if (userRs.next())
                {
                    if (docRs.next())
                    {
                        if ((userRs.getInt("uid") == docRs.getInt("ouid")) && shareUserRs.next())
                        {
                            String suid = String.valueOf(shareUserRs.getInt("uid"));
                            String sdid = String.valueOf(docRs.getInt("did"));

                            Statement shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                            String shareQuery = "INSERT INTO mydb.Shared (sdid,suid,perm) VALUES ('"
                                    + sdid + "','"
                                    + suid + "','"
                                    + perm + "','";

                            shareStmt.executeUpdate(shareQuery);
                            result = true;

                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Share</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>File shared...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;user.jsp");
                        }
                        else
                        {
                            // bad shareUser
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Share</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Invalid user to share with...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;user.jsp");
                        }
                    }
                    else
                    {
                        // bad doc
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Share</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>Invalid document...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;user.jsp");
                    }
                }
                else
                {
                    // bad user
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Share</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Invalid user...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;user.jsp");
                }
            }
            catch (Exception e)
            {
                // SQL error
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Share</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error sharing file...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;user.jsp");
            }
        }
        else
        {
            // bad permission
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Share</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Invalid permission request...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;user.jsp");
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result,time) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "shared','"
                    + String.valueOf(result) + "','" + ((new Date((new GregorianCalendar()).getTimeInMillis())).toString()) + "')";
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
