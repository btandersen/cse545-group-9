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
        String deptSet = "HR,LS,IT,SP,RD,FN";

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try
        {
            String userToUpdate = request.getParameter("userToUpdate");
            String newRole = request.getParameter("newRole");
            String newDept = request.getParameter("newDept");
            
            boolean changeRole = (!newRole.isEmpty() && 
                    (Integer.parseInt(newRole) > Roles.TEMP.ordinal()) && 
                    (Integer.parseInt(newRole) <= Roles.OFFICER.ordinal()));
            
            boolean changeDept = (!newDept.isEmpty() && deptSet.contains(newDept));

            Statement userToUpdateStmt = null;
            ResultSet userToUpdateRs = null;

            if (request.isUserInRole("admin"))
            {
                if (changeRole)
                {
                    if (changeDept)
                    {
                        try
                        {
                            userToUpdateStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                            String userToUpdateQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                                    + " WHERE " + "uname" + " = '" + userToUpdate + "'";

                            userToUpdateRs = userToUpdateStmt.executeQuery(userToUpdateQuery);
                        }
                        catch (Exception e)
                        {
                            // SQL Error
                        }
                    }
                }
            }
        }
        finally
        {
            out.close();
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