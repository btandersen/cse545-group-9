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
public class NewUser extends HttpServlet {

    InitialContext ctx;
    DataSource ds;
    Connection conn;

    @Override
    public void init() {

        try {
            ctx = new InitialContext();
            //ds = (DataSource) ctx.lookup("java:comp/env/jdbc/MySQLDataSource");
            ds = (DataSource) ctx.lookup("jdbc/MySQLDataSource");
            conn = ds.getConnection();
        } catch (Exception e) {
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
            throws ServletException, IOException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String query = "";

        try {
            //open connection to db
            Statement stmt = conn.createStatement();
            query = "INSERT INTO mydb.users (`uname`) VALUES ('" + user + "')";
            stmt.executeUpdate(query); //execute (insert the row)
            stmt.close(); //close the statement
        } catch (SQLException e) //generic SQL error
        {
            System.out.println(e); //spit out SQL error
        }

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>New User Request</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Request sent for: " + user + "</h1>");
            out.println("<h1>Password: " + pass + "</h1>");
            out.println("</body>");
            out.println("</html>");
            response.setHeader("Refresh", "5;index.jsp");
        } finally {
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
            throws ServletException, IOException {
        processRequest(request, response);
    }
}