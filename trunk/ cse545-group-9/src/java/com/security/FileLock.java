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
public class FileLock extends HttpServlet
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

        String user = request.getRemoteUser();
        String title = request.getParameter("title");

        try
        {
            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement ownerStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet userRs = null;
            ResultSet docRs = null;
            ResultSet ownerRs = null;
            ResultSet shareRs = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            String docQuery = "SELECT * FROM " + "mydb" + "." + "Docs"
                    + " WHERE " + "title" + " = '" + title + "'";

            userRs = userStmt.executeQuery(userQuery);
            docRs = docStmt.executeQuery(docQuery);

            if (userRs.next() && docRs.next())
            {
                String uid = String.valueOf(userRs.getInt("uid"));
                String userDept = userRs.getString("dept");
                int role = userRs.getInt("role");

                String did = String.valueOf(docRs.getInt("did"));
                String docDept = docRs.getString("dept");
                String ouid = String.valueOf(docRs.getInt("ouid"));

                String ownerQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                        + " WHERE " + "uname" + " = '" + ouid + "'";

                String shareQuery = "SELECT * FROM " + "mydb" + "." + "Shared"
                        + " WHERE " + "sdid" + "=" + did + " AND " + "suid" + "=" + uid + " AND " + "perm" + " = '" + "U" + "'";

                ownerRs = ownerStmt.executeQuery(ownerQuery);
                shareRs = shareStmt.executeQuery(shareQuery);

                int ownerRole = 0;

                if (ownerRs.next())
                {
                    ownerRole = ownerRs.getInt("role");
                }

                boolean shared = false;
                boolean lockPerm = false;

                if (shareRs.next())
                {
                    shared = true;
                    lockPerm = shareRs.getString("perm").equals("L");
                }

                boolean userIsOwner = uid.equals(ouid);
                boolean userIsManager = (role > Roles.REG_EMP.ordinal());
                boolean userMeetsRoleReq = (role >= ownerRole);
                boolean userMeetsDeptReq = (userDept.contains(docDept));

                if (ownerRs.next())
                {
                    if (userIsOwner || (shared && lockPerm) || (userIsManager && userMeetsRoleReq && userMeetsDeptReq))
                    {
                        try
                        {
                            Statement lockStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                            String lockQuery = "INSERT INTO mydb.Locked (ldid,luid) VALUES ('"
                                    + did + "','"
                                    + uid + "','";

                            lockStmt.executeUpdate(lockQuery);
                            result = true;
                        }
                        catch (Exception e)
                        {
                            // already locked
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Lock</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>File is already locked...</h1>");
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
                        out.println("<title>File Lock</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>You do not have permission to lock this file...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;user.jsp");
                    }
                }
                else
                {
                    // bad doc ouid
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Lock</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Invalid document owner...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;user.jsp");
                }

            }
            else
            {
                // bad user or doc
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Lock</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Invalid user or document...</h1>");
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
            out.println("<title>File Lock</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Error attempting to lock file...</h1>");
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
                    + "'locked','"
                    + String.valueOf(result) + "','" + ((new Date((new GregorianCalendar()).getTimeInMillis())).toString()) + "'";
            logStmt.executeUpdate(logQuery);
        }
        catch (Exception e)
        {
            // logging failed
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
