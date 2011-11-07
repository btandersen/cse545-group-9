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

import java.util.regex.*;

/**
 *
 * @author Administrator
 */
public class NewUser extends HttpServlet
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
        final String UNAME_REGEX = "[\\w]{1,45}+";
        final String FNAME_REGEX = "[a-zA-Z]{1,45}+";
        final String LNAME_REGEX = "[a-zA-Z]{1,45}+";
        final String EMAIL_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        final String PWD_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20})";

        Pattern unamePattern = Pattern.compile(UNAME_REGEX);
        Pattern fnamePattern = Pattern.compile(FNAME_REGEX);
        Pattern lnamePattern = Pattern.compile(LNAME_REGEX);
        Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
        Pattern pwdPattern = Pattern.compile(PWD_REGEX);

        Matcher unameMatcher = null;
        Matcher fnameMatcher = null;
        Matcher lnameMatcher = null;
        Matcher emailMatcher = null;
        Matcher pwdMatcher = null;

        String uname = request.getParameter("uname");
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String email = request.getParameter("email");
        String pwd = request.getParameter("pwd");
        String pwd1 = request.getParameter("pwd1");

        unameMatcher = unamePattern.matcher(uname);
        fnameMatcher = fnamePattern.matcher(fname);
        lnameMatcher = lnamePattern.matcher(lname);
        emailMatcher = emailPattern.matcher(email);
        pwdMatcher = pwdPattern.matcher(pwd);

        boolean unameMatch = unameMatcher.matches();
        boolean fnameMatch = fnameMatcher.matches();
        boolean lnameMatch = lnameMatcher.matches();
        boolean emailMatch = emailMatcher.matches();
        boolean pwdMatch = pwdMatcher.matches();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (unameMatch)
        {
            if (fnameMatch && lnameMatch)
            {
                if (emailMatch)
                {
                    if (pwdMatch && pwd.equals(pwd1))
                    {
                        String query = "";

                        try
                        {
                            //open connection to db
                            Statement stmt = conn.createStatement();
                            query = "INSERT INTO mydb.users (uname,fname,lname,email,role,dept,pwd) VALUES ('"
                                    + uname + "','"
                                    + fname + "','"
                                    + lname + "','"
                                    + email + "',0,'TEMP',md5('" + pwd + "'))";
                            stmt.executeUpdate(query); //execute (insert the row)
                            stmt.close(); //close the statement
                        }
                        catch (SQLException e) //generic SQL error
                        {
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>New User Request</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Error attempting to submit request</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;index.jsp");
                        }

                        try
                        {
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>New User Request</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Request sent for: " + uname + "</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;index.jsp");
                        }
                        finally
                        {
                            out.close();
                        }
                    }
                    else
                    {
                        //bad pwd
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>New User Request</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>Bad password</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;index.jsp");
                    }
                }
                else
                {
                    // bad email
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>New User Request</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Bad email</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;index.jsp");
                }
            }
            else
            {
                // bad fname or lname
                out.println("<html>");
                out.println("<head>");
                out.println("<title>New User Request</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Bad firstname or lastname</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;index.jsp");
            }
        }
        else
        {
            // bad uname
            out.println("<html>");
            out.println("<head>");
            out.println("<title>New User Request</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Bad username</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;index.jsp");
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