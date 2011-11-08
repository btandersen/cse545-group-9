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
public class FileDeletePage extends HttpServlet
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
            out.println("<title>File Delete Page</title>");
            out.println("</head>");
            out.println("<body>");

            String uname = request.getRemoteUser();
            String uid = null;
            int userRole = 0;
            String userDept = null;

            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            Statement docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet userRs = null;
            ResultSet docRs = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + uname + "'";
            
            String docQuery = null;

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

                    if (userIsManager)
                    {
                        // own, dept
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE ((A.ouid=" + uid + ") OR (A.dept='" + userDept + "')) AND NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)";
                      
                    }
                    else if (userIsRegEmp)
                    {
                        //docQuery = "SELECT A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A, Shared B WHERE (B.sdid=A.did AND B.suid=" + uid + ") OR A.ouid=" + uid;
                        docQuery = "SELECT A.did, A.title, A.auth, A.dept, A.ouid, A.filename FROM Docs A WHERE A.ouid=" + uid + " AND NOT EXISTS (SELECT * FROM Locked L WHERE A.did=L.ldid)";
                    }
                    else
                    {
                        // invalid role
                    }

                    docRs = docStmt.executeQuery(docQuery);

                    out.println("<form action=\"FileDelete\" method=POST>");
                    out.println("<table><th>Owned</th>");
                    out.println("<tr><th>Title</th><th>Author</th><th>Department</th><th>Owner</th><th>filename</th></tr>");

                    while (docRs.next())
                    {
                        out.println("<tr>");
                        out.println("<td>" + docRs.getString("title") + "</td><td>"
                                + docRs.getString("auth") + "</td><td>"
                                + docRs.getString("dept") + "</td><td>"
                                + String.valueOf(docRs.getInt("ouid")) + "</td><td>"
                                + docRs.getString("filename") + "</td><td>"
                                + "<input type=\"radio\" name=\"title\" value=\"" + docRs.getString("title") + "\"></td>");
                        out.println("</tr>");
                    }

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
                e.printStackTrace();
            }

            out.println("<a href=\"user.jsp\" >Return to User Page</a>");
            out.println("</body>");
            out.println("</html>");
        }
        catch (Exception e)
        {
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
