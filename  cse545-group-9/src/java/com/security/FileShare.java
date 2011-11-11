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
import java.util.regex.Pattern;
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

        String permSet = "R,U,L";

        String user = request.getRemoteUser();
        String title = request.getParameter("title");
        String shareUser = request.getParameter("shareuser");
        String perm = request.getParameter("perm");

        boolean cleanInput = false;

        if ((title != null) && (shareUser != null))
        {
            String inputRegex = "[\\w\\s]{1,45}+";
            Pattern inputPattern = Pattern.compile(inputRegex);
            cleanInput = inputPattern.matcher(title).matches();
            inputRegex = "[\\w]{1,45}+";
            inputPattern = Pattern.compile(inputRegex);
            cleanInput = (cleanInput && inputPattern.matcher(shareUser).matches());
        }

        if (cleanInput)
        {
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

                    String shareUserQuery = "SELECT * FROM " + "mydb" + "." + "Users U"
                            + " WHERE " + "U.uname" + " = '" + shareUser + "' AND (NOT EXISTS(SELECT * FROM mydb.Groups G WHERE G.groupid='appadmin' AND G.uname='" + shareUser + "'))";

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
                                int shareUserRole = shareUserRs.getInt("role");

                                if (shareUserRole > Roles.TEMP.ordinal())
                                {
                                    Statement shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                                    String shareQuery = "INSERT INTO mydb.Shared (sdid,suid,perm) VALUES ('"
                                            + sdid + "','"
                                            + suid + "','"
                                            + perm + "')";

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
                                    response.setHeader("Refresh", "5;FileSharePage");
                                }
                                else
                                {
                                    // cannot share with TEMP user
                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>File Share</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>Cannot share with TEMP users...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;FileSharePage");
                                }
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
                                response.setHeader("Refresh", "5;FileSharePage");
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
                            response.setHeader("Refresh", "5;FileSharePage");
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
                        response.setHeader("Refresh", "5;FileSharePage");
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
                    if (e.getMessage().contains("Duplicate"))
                    {
                        out.println("<h1>Already shared with requested user and permission...</h1>");
                    }
                    else
                    {
                        out.println("<h1>Error sharing file...</h1>");
                    }
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;FileSharePage");
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
                response.setHeader("Refresh", "5;FileSharePage");
            }
        }
        else
        {
            // bad input
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Share</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Detected invalid input characters in form data...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;FileSharePage");
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "shared','"
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
