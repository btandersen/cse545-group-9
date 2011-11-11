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

public class FileDelete extends HttpServlet
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

        boolean cleanInput = false;
        String inputRegex = "[\\w\\s]{1,45}+";
        Pattern inputPattern = Pattern.compile(inputRegex);
        
        if (title != null)
        {
            cleanInput = (inputPattern.matcher(title).matches());
        }
        

        if (cleanInput)
        {
            try
            {
                Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Statement ownerStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Statement lockStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                ResultSet userRs = null;
                ResultSet docRs = null;
                ResultSet ownerRs = null;
                ResultSet lockRs = null;

                String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                        + " WHERE " + "uname" + " = '" + user + "'";

                String docQuery = "SELECT * FROM " + "mydb" + "." + "Docs"
                        + " WHERE " + "title" + " = '" + title + "'";

                userRs = userStmt.executeQuery(userQuery);
                docRs = docStmt.executeQuery(docQuery);

                if (userRs.next() && docRs.next())
                {
                    int userRole = userRs.getInt("role");
                    String uid = userRs.getString("uid");
                    String did = docRs.getString("did");
                    String ouid = docRs.getString("ouid");
                    String userDept = userRs.getString("dept");
                    String docDept = docRs.getString("dept");

                    String ownerQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                            + " WHERE " + "uid" + " = " + ouid + "";

                    String lockQuery = "SELECT * FROM " + "mydb" + "." + "Locked"
                            + " WHERE " + "ldid" + " = " + did + "";

                    ownerRs = ownerStmt.executeQuery(ownerQuery);
                    lockRs = lockStmt.executeQuery(lockQuery);

                    if (ownerRs.next())
                    {
                        int ownerRole = ownerRs.getInt("role");

                        boolean userIsOwner = uid.equals(ouid);
                        boolean userIsManager = (userRole > Roles.REG_EMP.ordinal());
                        boolean userMeetsRoleReq = (userRole >= ownerRole);
                        boolean userMeetsDeptReq = (userDept.contains(docDept));
                        boolean locked = false;
                        boolean userHasLock = false;

                        if (lockRs.next())
                        {
                            locked = true;
                            userHasLock = uid.equals(String.valueOf(lockRs.getInt("luid")));
                        }

                        if (userRole > Roles.GUEST.ordinal())
                        {
                            if (userIsOwner || (userIsManager && userMeetsRoleReq && userMeetsDeptReq))
                            {
                                if (!locked || (locked && userHasLock))
                                {
                                    docRs.deleteRow();
                                    result = true;

                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>File Delete</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>File deleted successfully...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;FileDeletePage");
                                }
                                else
                                {
                                    // locked by someone else
                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>File Delete</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>File is locked by another user...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;FileDeletePage");
                                }
                            }
                            else
                            {
                                // not proper permission
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Delete</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>You do not have permission to delete this file...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;FileDeletePage");
                            }
                        }
                        else
                        {
                            // user is a guest
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Delete</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Guest cannot delete files...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;FileDeletePage");
                        }
                    }
                    else
                    {
                        // document owner not in db, should not happen
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Delete</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>Invalid document owner...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;FileDeletePage");
                    }
                }
                else
                {
                    // user or document not in db
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Delete</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Invalid user or document...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;FileDeletePage");
                }
            }
            catch (Exception e)
            {
                // SQL error
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Delete</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error attempting to delete file...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;FileDeletePage");
            }
        }
        else
        {
            // bad input
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Delete</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Detected invalid input characters in title...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;FileDeletePage");
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "delete','"
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }
}