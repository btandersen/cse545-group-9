/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.security;

import java.io.BufferedInputStream;
import java.io.IOException;


import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.sql.*;
import java.util.GregorianCalendar;
import javax.sql.*;
import javax.naming.*;
import javax.servlet.ServletOutputStream;

/**
 *
 * @author Administrator
 */
public class FileDownload extends HttpServlet
{
    InitialContext ctx;
    DataSource ds;
    Connection conn;
    //Statement stmt;
    //ResultSet rs;

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
        boolean result = false;
        
        String user = request.getRemoteUser();
        String title = request.getParameter("filename");

        try
        {
            PreparedStatement psmt = conn.prepareStatement("select * from mydb.table1 where name=?");
            psmt.setString(1, title);
            ResultSet rs = psmt.executeQuery();

            if (rs.next())
            {
                Blob b = rs.getBlob("file");

                if (b != null)
                {
                    InputStream is = b.getBinaryStream();
                    BufferedInputStream buf = new BufferedInputStream(is);
                    ServletOutputStream out = response.getOutputStream();

                    response.setContentType("application/octet-stream");
                    response.addHeader("Content-Disposition", "attachment; filename=" + title);
                    response.setContentLength((int) b.length());

                    int readBytes = 0;
                    while ((readBytes = buf.read()) != -1)
                    {
                        out.write(readBytes);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // rollback
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result,time) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "'read','"
                    + String.valueOf(result) + "','" + ((new Date((new GregorianCalendar()).getTimeInMillis())).toString()) + "'";
            logStmt.executeQuery(logQuery);
        }
        catch (Exception e)
        {
            // logging failed
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

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
}