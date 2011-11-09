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
public class FileUpdatePage extends HttpServlet
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
            out.println("<title>File Update Page</title>");
            out.println("</head>");
            out.println("<body>");

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
                        //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE ((NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)) OR (EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))) AND ((A.ouid=" + uid + ") OR (A.dept='" + userDept + "'))";
                        //shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE ((NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)) OR (EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))) AND B.perm='U' AND B.sdid=A.did AND B.suid=" + uid;
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Users U "
                                + "WHERE ((A.ouid=" + uid + ") OR (A.dept='" + userDept + "')) "
                                + "AND U.uid=A.ouid "
                                + "AND NOT EXISTS (SELECT * FROM Users U WHERE U.role>" + userRole + " AND U.uid=A.ouid) "
                                + "AND (NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid) "
                                + "OR EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))";

                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='U' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid "
                                + "AND (NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)"
                                + "OR EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))";
                        docRs = docStmt.executeQuery(docQuery);
                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else if (userIsRegEmp)
                    {
                        // share, own
                        //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE ((NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)) OR (EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))) AND A.ouid=" + uid;
                        //shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE ((NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)) OR (EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))) AND B.perm='U' AND B.sdid=A.did AND B.suid=" + uid;
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Users U "
                                + "WHERE ((A.ouid=" + uid + ") "
                                + "AND U.uid=A.ouid "
                                + "AND (NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)"
                                + "OR EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))";

                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='U' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid "
                                + "AND (NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)"
                                + "OR EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))";
                        docRs = docStmt.executeQuery(docQuery);
                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else if (userIsGuest)
                    {
                        // share
                        //shareQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE ((NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)) OR (EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))) AND B.perm='U' AND B.sdid=A.did AND B.suid=" + uid;
                        shareQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename, U.uname "
                                + "FROM Docs A, Shared B, Users U "
                                + "WHERE B.perm='U' AND B.sdid=A.did AND B.suid=" + uid + " "
                                + "AND U.uid=A.ouid "
                                + "AND (NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)"
                                + "OR EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid AND L.luid=" + uid + "))";
                        shareRs = shareStmt.executeQuery(shareQuery);
                    }
                    else
                    {
                        // invalid role
                    }

                    if (userIsManager || userIsRegEmp)
                    {
                        out.println("<table><th>Owned</th>");
                        out.println("<tr><th>Title</th><th>Author</th><th>Department</th><th>Owner</th><th>filename</th>");

                        while (docRs.next())
                        {
                            out.println("<tr>");
                            out.println("<td>" + docRs.getString("title") + "</td><td>"
                                    + docRs.getString("auth") + "</td><td>"
                                    + docRs.getString("dept") + "</td><td>"
                                    + docRs.getString("uname") + "</td><td>"
                                    + docRs.getString("filename") + "</td>");
                            out.println("</tr>");
                        }

                        out.println("</table>");
                    }

                    out.println("<table><th>Shared</th>");
                    out.println("<tr><th>Title</th><th>Author</th><th>Department</th><th>Owner</th><th>filename</th>");

                    while (shareRs.next())
                    {
                        out.println("<tr>");
                        out.println("<td>" + shareRs.getString("title") + "</td><td>"
                                + shareRs.getString("auth") + "</td><td>"
                                + shareRs.getString("dept") + "</td><td>"
                                + shareRs.getString("uname") + "</td><td>"
                                + shareRs.getString("filename") + "</td>");
                        out.println("</tr>");
                    }

                    out.println("</table>");

                    out.println("<FORM enctype=\"multipart/form-data\" action=\"FileUpdate\" method=POST>");
                    out.println("<table border=\"0\">");
                    out.println("<tr><td colspan=\"2\">File Update</td></tr>");

                    out.println("<tr><td>Select File to Update:</td><td><select name=\"title\">");

                    if (userIsManager || userIsRegEmp)
                    {
                        docRs.beforeFirst();

                        while (docRs.next())
                        {
                            out.println("<option value=\"" + docRs.getString("title") + "\">" + docRs.getString("title") + "</option>");
                        }
                    }

                    shareRs.beforeFirst();

                    while (shareRs.next())
                    {
                        out.println("<option value=\"" + shareRs.getString("title") + "\">" + shareRs.getString("title") + "</option>");
                    }

                    out.println("</select></td></tr>");

                    //String deptSet = "HR,LS,IT,SP,RD,FN";

                    out.println("<tr><td>Enter New Title:</td><td><input name=\"newtitle\" type=\"text\" /></td></tr>");
                    out.println("<tr><td>Enter New Author:</td><td><input name=\"newauthor\" type=\"text\" /></td></tr>");
                    out.println("<tr><td>Enter New Department:</td><td><select name=\"newdept\" type=\"text\">"
                            + "<option value=\"HR\">HR</option>"
                            + "<option value=\"LS\">LS</option>"
                            + "<option value=\"IT\">IT</option>"
                            + "<option value=\"SP\">SP</option>"
                            + "<option value=\"RD\">RD</option>"
                            + "<option value=\"FN\">FN</option>"
                            + "</select></td></tr>");
                    out.println("<tr><td>Choose the file To Update</td><td><input name=\"file\" type=\"file\" /></td></tr>");
                    out.println("<tr><td colspan=\"2\"><input type=\"submit\" value=\"Submit\" /></td></tr>");
                    out.println("</table>");
                    out.println("</FORM>");
                }
                else
                {
                    // invalid user
                    out.println("<p>Invalid user</p>");
                }
            }
            catch (Exception e)
            {
                // SQL Error
                out.println("<p>SQL Error</p>");
            }

            out.println("<a href=\"user.jsp\" >Return to User Page</a>");
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
