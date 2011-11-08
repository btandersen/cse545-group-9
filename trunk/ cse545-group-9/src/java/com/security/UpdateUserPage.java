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
public class UpdateUserPage extends HttpServlet
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String user = request.getRemoteUser();

        try
        {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Update User Page</title>");
            out.println("</head>");
            out.println("<body>");

            Statement userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet userRs = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users WHERE NOT (uname='" + user + "')";

            try
            {
                userRs = userStmt.executeQuery(userQuery);
                    out.println("<form action=\"DeleteUser\" method=POST>");
                    out.println("<table><th>Select a User to Delete</th>");
                    out.println("<tr><th>User ID</th><th>Username</th><th>Role</th><th>Department</th>");

                    while (userRs.next())
                    {
                        out.println("<tr>");
                        out.println("<td>" + userRs.getString("uid") + "</td><td>"
                                + userRs.getString("uname") + "</td><td>"
                                + Roles.values()[userRs.getInt("role")] + "</td><td>"
                                + userRs.getString("dept") + "</td>"
                                + "<td><input type=\"radio\" name=\"uname\" value=\"" + userRs.getString("uname") + "\"></td>");
                        out.println("</tr>");
                    }

                    out.println("<tr><td><input type=\"submit\" value=\"Submit\" /></td></tr>");
                    out.println("</table>");
                    out.println("</form>");

                    out.println("<FORM action=\"UpdateUser\" method=POST>");
                    out.println("<table border=\"0\">");
                    out.println("<tr><th colspan=\"2\">Update a User</th></tr>");
                    
                    userRs.beforeFirst();
                    
                    out.println("<tr><td>Select File to Update:</td><td><select name=\"userToUpdate\">");
                    
                    while (userRs.next())
                    {
                        out.println("<option value=\"" + userRs.getString("uname") + "\">" + userRs.getString("uname") + "</option>");
                    }
                    
                    out.println("</select></td></tr>");
                    
                    out.println("<tr><td>Enter New Role:</td><td><select name=\"newrole\" type=\"text\">"
                            + "<option value=\"1\">GUEST</option>"
                            + "<option value=\"2\">REGULAR EMPLOYEE</option>"
                            + "<option value=\"3\">MANAGER</option>"
                            + "<option value=\"4\">OFFICER</option>"
                            + "</select></td></tr>");
                    
                    out.println("<tr><td>Enter New Department:</td><td><select name=\"newdept\" type=\"text\">"
                            + "<option value=\"HR\">HR</option>"
                            + "<option value=\"LS\">LS</option>"
                            + "<option value=\"IT\">IT</option>"
                            + "<option value=\"SP\">SP</option>"
                            + "<option value=\"RD\">RD</option>"
                            + "<option value=\"FN\">FN</option>"
                            + "</select></td></tr>");
                    
                    out.println("<tr><td>Enter Security Group:</td><td><select name=\"group\" type=\"text\">"
                            + "<option value=\"appuser\">USER</option>"
                            + "<option value=\"appadmin\">ADMIN</option>"
                            + "</select></td></tr>");
                    
                    out.println("<tr><td colspan=\"2\"><input type=\"submit\" value=\"Submit\" /></td></tr>");
                    out.println("</table>");
                    out.println("</FORM>");
            }
            catch (Exception e)
            {
                // SQL Error
            }
            
            out.println("<a href=\"admin.jsp\" >Return to Admin Page</a>");
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
