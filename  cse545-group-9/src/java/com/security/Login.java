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
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

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
    private final double MIN_LOCKOUT_TIME = 5.0;

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
            System.err.println("error during init");
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

        String user = request.getParameter("j_username");

        String pass = request.getParameter("j_password");

        boolean cleanInput = false;
        String inputRegex = "[\\w]{1,45}+";
        Pattern inputPattern = Pattern.compile(inputRegex);
        
        if (user != null)
        {
            cleanInput = (inputPattern.matcher(user).matches());
        }
        

        if (cleanInput)
        {
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
                    double lockoutTime = this.getLockOutTime(rs.getTimestamp("time"));

                    if (attempts < MAX_ATTEMPTS || lockoutTime > MIN_LOCKOUT_TIME)
                    {
                        if (lockoutTime > MIN_LOCKOUT_TIME)
                        {
                            rs.updateInt("attempts", 0);
                            rs.updateRow();
                            attempts = 0;
                        }

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
                                request.logout();
                                response.sendRedirect("guest.jsp");
                            }
                        }
                        catch (Exception e)
                        {
                            if (attempts < 3)
                            {
                                rs.updateInt("attempts", attempts + 1);
                            }

                            rs.updateTimestamp("time", (new Timestamp((new GregorianCalendar()).getTimeInMillis())));
                            rs.updateRow();
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>Login</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Invalid user credentials...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;index.jsp");
                        }
                    }
                    else
                    {
                        //locked out
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>Login</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>Account locked due to 3 failed attempts...</h1>");
                        out.println("<h1>Minutes remaining until unlock: " + ((int) Math.ceil(MIN_LOCKOUT_TIME + 1 - lockoutTime)) + "</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;index.jsp");
                    }
                }
                else
                {
                    //user not in db
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Login</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error encountered...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;index.jsp");
                }

                stmt.close();
            }
            catch (Exception e)
            {
                // SQL Error
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Login</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error encountered...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;index.jsp");
            }
        }
        else
        {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Login</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Detected invalid username...</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;index.jsp");
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
