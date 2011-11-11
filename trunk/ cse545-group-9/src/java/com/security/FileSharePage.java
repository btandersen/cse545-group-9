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
public class FileSharePage extends HttpServlet
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

        String user = request.getRemoteUser();

        try
        {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Share Page</title>");
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

            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet userRs = null;
            ResultSet shareRs = null;
            ResultSet docRs = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            String shareQuery = "SELECT * FROM mydb.Users U WHERE (NOT (U.Role=" + Roles.TEMP.ordinal() + ")) AND (NOT (U.uname='" + user + "')) AND (NOT EXISTS(SELECT * FROM mydb.Groups G WHERE G.groupid='appadmin' AND G.uname=U.uname))";

            String docQuery = null;

            try
            {
                userRs = userStmt.executeQuery(userQuery);

                if (userRs.next())
                {
                    String uid = userRs.getString("uid");
                    int userRole = userRs.getInt("role");
                    String userDept = userRs.getString("dept");

                    boolean userIsManager = (userRole > Roles.REG_EMP.ordinal());
                    boolean userIsRegEmp = (userRole == Roles.REG_EMP.ordinal());
                    boolean userIsGuest = (userRole == Roles.GUEST.ordinal());

                    if (!userIsGuest)
                    {
                        if (userIsManager)
                        {
                            // own, dept
                            docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE ((A.ouid=" + uid + ") OR (A.dept='" + userDept + "'))";
                            docRs = docStmt.executeQuery(docQuery);
                        }
                        else if (userIsRegEmp)
                        {
                            //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE (B.sdid=A.did AND B.suid=" + uid + ") OR A.ouid=" + uid;
                            docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE A.ouid=" + uid;
                            docRs = docStmt.executeQuery(docQuery);
                        }
                        else
                        {
                            // invalid role
                        }

                        shareRs = shareStmt.executeQuery(shareQuery);

                        out.println("<FORM action=\"FileShare\" method=POST>");
                        out.println("<div id=\"fileshare\"><table border=\"0\">");
                        out.println("<tr><th colspan=\"2\">Share a document</th></tr>");

                        out.println("<tr><td>Select a document to share:</td><td><select name=\"title\">");

                        while (docRs.next())
                        {
                            out.println("<option value=\"" + docRs.getString("title") + "\">" + docRs.getString("title") + "</option>");
                        }

                        out.println("</select></td></tr>");

                        out.println("<tr><td>Select user to share with:</td><td><select name=\"shareuser\">");

                        while (shareRs.next())
                        {
                            out.println("<option value=\"" + shareRs.getString("uname") + "\">" + shareRs.getString("uname") + "</option>");
                        }

                        out.println("</select></td></tr>");

                        out.println("<tr><td>Select permission:</td><td><select name=\"perm\" type=\"text\">"
                                + "<option value=\"R\">READ</option>"
                                + "<option value=\"U\">UPDATE</option>"
                                + "<option value=\"L\">CHECK IN/OUT</option>"
                                + "</select></td></tr>");

                        out.println("<tr><td colspan=\"2\"><input type=\"submit\" value=\"Submit\" /></td></tr>");
                        out.println("</table></div>");
                        out.println("</FORM>");
                    }
                    else
                    {
                        // user is guest
                        out.println("<h1>Guests are not allowed to share...</h1>");
                    }
                }
                else
                {
                    // invalid user
                    out.println("<h1>You are not a valid user...</h1>");
                }

            }
            catch (Exception e)
            {
                // SQL Error
                out.println("<h1>Error retrieving data...</h1>");
            }

            //out.println("<a href=\"user.jsp\" >Return to User Page</a>");
            //out.println("</body>");
            //out.println("</html>");
        }
        catch (Exception e)
        {
            // Output stream error
            response.setHeader("Refresh", "2;user.jsp");
        }
        finally
        {
            out.println("</div>");
            out.println("<div id=\"footer\"><p>CSE 545 | Group 9</p></div>");
            out.println("</body>");
            out.println("</html>");
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
