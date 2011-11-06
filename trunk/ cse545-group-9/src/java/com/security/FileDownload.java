/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.security;

import java.io.BufferedInputStream;
import java.io.IOException;


import java.io.InputStream;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.sql.*;
import java.util.GregorianCalendar;
import javax.sql.*;
import javax.naming.*;
import javax.servlet.ServletOutputStream;

/**
 *
 * @author Administrator
 */
public class FileDownload extends HttpServlet
{
    InitialContext ctx;
    DataSource ds;
    Connection conn;
    //Statement stmt;
    //ResultSet rs;

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
                int userRole = userRs.getInt("role");
                String uid = uid = String.valueOf(userRs.getInt("uid")); //userRs.getString("uid");
                String did = did = String.valueOf(docRs.getInt("did"));
                String ouid = docRs.getString("ouid");
                String userDept = userRs.getString("dept");
                String docDept = docRs.getString("dept");

                String ownerQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                        + " WHERE " + "uname" + " = '" + ouid + "'";

                String shareQuery = "SELECT * FROM " + "mydb" + "." + "Shared"
                        + " WHERE " + "sdid" + "=" + did + " AND " + "suid" + "=" + uid + " AND " + "perm" + " = '" + "U" + "'";

                ownerRs = ownerStmt.executeQuery(ownerQuery);
                shareRs = shareStmt.executeQuery(shareQuery);

                if (ownerRs.next())
                {
                    int ownerRole = ownerRs.getInt("role");

                    boolean shared = false;
                    boolean readPerm = false;

                    if (shareRs.next())
                    {
                        shared = true;
                        readPerm = shareRs.getString("perm").equals("R");
                    }

                    boolean userIsOwner = uid.equals(ouid);
                    boolean userIsManager = (userRole > Roles.REG_EMP.ordinal());
                    boolean userMeetsRoleReq = (userRole >= ownerRole);
                    boolean userMeetsDeptReq = (userDept.contains(docDept));

                    if (userRole >= Roles.GUEST.ordinal())
                    {
                        if (userIsOwner || (shared && readPerm) || (userIsManager && userMeetsRoleReq && userMeetsDeptReq))
                        {
                            Blob b = docRs.getBlob("file");

                            if (b != null)
                            {
                                InputStream is = b.getBinaryStream();
                                BufferedInputStream buf = new BufferedInputStream(is);
                                ServletOutputStream fileOut = response.getOutputStream();

                                response.setContentType("application/octet-stream");
                                response.addHeader("Content-Disposition", "attachment; filename=" + title);
                                response.setContentLength((int) b.length());

                                int readBytes = 0;
                                while ((readBytes = buf.read()) != -1)
                                {
                                    fileOut.write(readBytes);
                                }

                                result = true;

                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Download</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>File download (read) successful...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;user.jsp");
                            }
                            else
                            {
                                // file was null
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Download</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>Invalid file (file was empty)...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;user.jsp");
                            }
                        }
                        else
                        {
                            // not proper permission
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Download</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>You do not have proper permission to download...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;user.jsp");
                        }
                    }
                    else
                    {
                        // user is a not at least a guest
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Download</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>You do not have proper permission to download (not at least a guest user)...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;user.jsp");
                    }
                }
                else
                {
                    // document owner not in db, should not happen
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Download</title>");
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
                // user or document not in db
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Download</title>");
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
            out.println("<title>File Download</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Error attempting to download...</h1>");
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
                    + "'read','"
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

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
}
