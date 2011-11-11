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
public class FileDownloadPage extends HttpServlet
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
            out.println("<title>File Download Page</title>");
            out.println("<LINK href=\"../css/style.css\" rel=\"stylesheet\" type=\"text/css\" />");
            out.println("</head>");
            out.println("<body>");
            out.println("<div id=\"container\">");
            out.println("<div id=\"header\"><h1>Web Document Management System</h1></div>");
            out.println("<div id=\"content\">");

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
                    out.println("<div id=\"currentuser\"><table>");
                    out.println("<tr><th>Current User</th></tr><tr><th>User ID</th><th>User Name</th><th>Role</th><th>Dept</th><th>Group</th></tr>");
                    out.println("<tr>");
                    out.println("<td>" + userRs.getInt("uid") + "</td>"
                            + "<td>" + userRs.getString("uname") + "</td>"
                            + "<td>" + Roles.values()[userRs.getInt("role")] + "</td>"
                            + "<td>" + userRs.getString("dept") + "</td>"
                            + "<td>" + userRs.getString("groupid") + "</td>");
                    out.println("</tr>");
                    out.println("</table><div>");
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

            out.println("<div><a href=\"user.jsp\" >Return to User Page</a></div>");

            String uname = request.getRemoteUser();
            String uid = null;
            int userRole = 0;
            String userDept = null;

            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet userRs = null;
            ResultSet docRs = null;
            ResultSet shareRs = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + uname + "'";
            String docQuery = null;
            String shareQuery = null;

            try
            {
                userRs = userStmt.executeQuery(userQuery);

                if (userRs.next())
                {
                    uid = userRs.getString("uid");
                    userRole = userRs.getInt("role");
                    userDept = userRs.getString("dept");

                    boolean userIsManager = (userRole > Roles.REG_EMP.ordinal());
                    boolean userIsRegEmp = (userRole == Roles.REG_EMP.ordinal());
                    boolean userIsGuest = (userRole == Roles.GUEST.ordinal());

                    if (userIsManager)
                    {
                        // share, own, dept
                        //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE (A.ouid=" + uid + ") OR (A.dept='" + userDept + "')";
                        //shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid;
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Users U "
                                + "WHERE ((A.ouid=" + uid + ") OR (A.dept='" + userDept + "')) "
                                + "AND U.uid=A.ouid "
                                + "AND NOT EXISTS (SELECT * FROM Users U WHERE U.role>" + userRole + " AND U.uid=A.ouid)";

                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid ";

                        docRs = docStmt.executeQuery(docQuery);
                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else if (userIsRegEmp)
                    {
                        // share, own
                        //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE A.ouid=" + uid;
                        //shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid;
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Users U "
                                + "WHERE A.ouid=" + uid + " "
                                + "AND U.uid=A.ouid";

                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid";

                        docRs = docStmt.executeQuery(docQuery);
                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else if (userIsGuest)
                    {
                        // share
                        shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid;
                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='R' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid ";

                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else
                    {
                        // invalid role
                    }

                    out.println("<form action=\"FileDownload\" method=POST>");

                    if (userIsManager || userIsRegEmp)
                    {
                        out.println("<table><th>Owned</th>");
                        out.println("<div id=\"owneddocs\"><tr><th>Title</th><th>Author</th><th>Department</th><th>Owner</th><th>filename</th></tr>");

                        while (docRs.next())
                        {
                            out.println("<tr>");
                            out.println("<td>" + docRs.getString("title") + "</td><td>"
                                    + docRs.getString("auth") + "</td><td>"
                                    + docRs.getString("dept") + "</td><td>"
                                    + String.valueOf(docRs.getInt("ouid")) + "</td><td>"
                                    + docRs.getString("filename") + "</td><td>"
                                    + "<input type=\"radio\" name=\"title\" value=\"" + docRs.getString("title") + "\"></td>");
                            out.println("</tr></div>");
                        }

                        out.println("</table>");
                    }

                    out.println("<table><th>Shared</th>");
                    out.println("<div id=\"shareddocs\"><tr><th>Title</th><th>Author</th><th>Department</th><th>Owner</th><th>filename</th></tr>");

                    while (shareRs.next())
                    {
                        out.println("<tr>");
                        out.println("<td>" + shareRs.getString("title") + "</td><td>"
                                + shareRs.getString("auth") + "</td><td>"
                                + shareRs.getString("dept") + "</td><td>"
                                + String.valueOf(shareRs.getInt("ouid")) + "</td><td>"
                                + shareRs.getString("filename") + "</td><td>"
                                + "<input type=\"radio\" name=\"title\" value=\"" + shareRs.getString("title") + "\"></td>");
                        out.println("</tr></div>");
                    }
                    
                    out.println("<tr><td colspan=\"5\"><hr /></td><tr><tr><td>Enter pass phrase and select YES for decryption:</td><td><input name=\"key\" type=\"password\" /></td><td><input type=\"radio\" name=\"dec\" value=\"yes\"/>YES<br /><input type=\"radio\" name=\"dec\" value=\"no\" Checked />NO</td></tr>");

                    out.println("<tr><td><input type=\"submit\" value=\"Submit\" /></td></tr>");
                    out.println("</table>");
                    out.println("</form>");
                }
                else
                {
                    // invalid user
                }
            }
            catch (Exception e)
            {
                // SQL Error
            }

            out.println("</div>");
            out.println("<div id=\"footer\"><p>CSE 545 | Group 9</p></div>");
            out.println("</body>");
            out.println("</html>");
        }
        catch (Exception e)
        {
            // Output stream error
            response.setHeader("Refresh", "2;user.jsp");
        }
        finally
        {
            out.close();
        }
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
