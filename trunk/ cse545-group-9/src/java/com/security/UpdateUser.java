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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.*;
import javax.naming.*;

/**
 *
 * @author Administrator
 */
public class UpdateUser extends HttpServlet
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
        String deptSet = "HR,LS,IT,SP,RD,FN";

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try
        {
            String userToUpdate = request.getParameter("userToUpdate");
            String newRole = request.getParameter("newrole");
            String newDept = request.getParameter("newdept");
            String group = request.getParameter("group");

            boolean cleanInput = false;
            String inputRegex = "[\\w]{1,45}+";
            Pattern inputPattern = Pattern.compile(inputRegex);
            cleanInput = (inputPattern.matcher(userToUpdate).matches()
                    && inputPattern.matcher(newRole).matches()
                    && inputPattern.matcher(newDept).matches()
                    && inputPattern.matcher(group).matches());

            if (cleanInput)
            {
                boolean changeRole = (!newRole.isEmpty()
                        && (Integer.parseInt(newRole) > Roles.TEMP.ordinal())
                        && (Integer.parseInt(newRole) <= Roles.OFFICER.ordinal()));

                boolean changeDept = (!newDept.isEmpty() && deptSet.contains(newDept));

                Statement userToUpdateStmt = null;
                Statement groupStmt = null;

                ResultSet userToUpdateRs = null;
                ResultSet groupRs = null;

                if (request.isUserInRole("admin"))
                {
                    if (changeRole)
                    {
                        if (changeDept)
                        {
                            try
                            {
                                userToUpdateStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                                groupStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                                String userToUpdateQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                                        + " WHERE " + "uname" + " = '" + userToUpdate + "'";

                                String groupQuery = "SELECT * FROM mydb.Groups WHERE uname='" + userToUpdate + "'";

                                userToUpdateRs = userToUpdateStmt.executeQuery(userToUpdateQuery);
                                groupRs = groupStmt.executeQuery(groupQuery);

                                if (newRole.equals(String.valueOf(Roles.GUEST.ordinal())))
                                {
                                    newDept = "GUEST";
                                    group = "appuser";
                                }

                                if (userToUpdateRs.next())
                                {
                                    userToUpdateRs.updateInt("role", Integer.parseInt(newRole));
                                    userToUpdateRs.updateString("dept", newDept);
                                    userToUpdateRs.updateRow();

                                    if (groupRs.next())
                                    {
                                        groupRs.updateString("groupid", group);
                                    }
                                    else
                                    {
                                        groupRs.moveToInsertRow();
                                        groupRs.updateString("groupid", group);
                                        groupRs.updateString("uname", userToUpdate);
                                        groupRs.insertRow();
                                    }

                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>Update User</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>User updated sucessfully...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;UpdateUserPage");
                                }
                                else
                                {
                                    // Invalid user to update
                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>Update User</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>Invalid user to update...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;UpdateUserPage");
                                }
                            }
                            catch (Exception e)
                            {
                                // SQL Error
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>Update User</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>User updated failed...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;UpdateUserPage");
                            }
                        }
                    }
                }
            }
            else
            {
                // dirty input
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Update User</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Improper input detected...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;UpdateUserPage");
            }
        }
        catch (Exception e)
        {
            // SQL Error
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Update User</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>User updated failed...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;UpdateUserPage");
        }

        out.close();
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
