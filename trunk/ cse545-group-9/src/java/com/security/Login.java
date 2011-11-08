/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.security;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Administrator
 */
public class Login extends HttpServlet
{
    private InitialContext ctx;
    private DataSource ds;
    private Connection conn;
    private final int MAX_ATTEMPTS = 3;
    private final int MIN_LOCKOUT_TIME = 5;

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

        String user = request.getParameter("j_username");

        String pass = request.getParameter("j_password");

        Statement stmt = null;

        String query = "SELECT * FROM " + "mydb" + "." + "Users"
                + " WHERE " + "uname" + " = '" + user + "'";

        try
        {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            ResultSet rs = stmt.executeQuery(query);

            if (rs.next())
            {
                int attempts = rs.getInt("attempts");
                int lockoutTime = this.getLockOutTime(rs.getTimestamp("time"));

                if (attempts < MAX_ATTEMPTS || lockoutTime > MIN_LOCKOUT_TIME)
                {
                    try
                    {
                        request.login(user, pass);
                        rs.updateInt("attempts", 0);
                        rs.updateRow();
                        if (request.isUserInRole("user"))
                        {
                            response.sendRedirect("user/user.jsp");
                        }
                        else if (request.isUserInRole("admin"))
                        {
                            response.sendRedirect("admin/admin.jsp");
                        }
                        else
                        {
                            response.sendRedirect("guest.jsp");
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println(e);
                        rs.updateInt("attempts", attempts + 1);
                        rs.updateTimestamp("time", (new Timestamp((new GregorianCalendar()).getTimeInMillis())));
                        rs.updateRow();
                        response.sendRedirect("loginerror.jsp");
                    }
                }
                else
                {
                    //locked out
                    response.sendRedirect("loginerror.jsp");
                }
            }
            else
            {
                //user not in db
                response.sendRedirect("loginerror.jsp");
            }

            stmt.close();
        }
        catch (Exception e)
        {
            System.err.println(e);
        }

        return;
    }

    /**
     * Get the minutes difference
     */
    private int getLockOutTime(Timestamp lockOutTime)
    {
        return (int) (((new GregorianCalendar()).getTimeInMillis() / (1000 * 60)) - (lockOutTime.getTime() / (1000 * 60)));
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
