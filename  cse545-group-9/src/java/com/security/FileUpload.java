package com.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.sql.*;
import java.util.GregorianCalendar;
import javax.sql.*;
import javax.naming.*;

public class FileUpload extends HttpServlet
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
        boolean result = false;
        String user = request.getRemoteUser();
        String title = null;

        // Check that we have a file upload request
        if (ServletFileUpload.isMultipartContent(request))
        {

            Statement stmt = null;
            String uid = null;
            String userDept = null;
            int role = 0;

            String query = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            try
            {
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next())
                {
                    uid = String.valueOf(rs.getInt("uid"));
                    userDept = rs.getString("dept");
                    role = rs.getInt("role");

                    if (role > Roles.GUEST.ordinal())
                    {
                        try
                        {
                            // Create a factory for disk-based file items
                            FileItemFactory factory = new DiskFileItemFactory();
                            // Create a new file upload handler
                            ServletFileUpload upload = new ServletFileUpload(factory);
                            // Parse the request
                            List /* FileItem */ items = upload.parseRequest(request);

                            // Process the uploaded items
                            Iterator iter = items.iterator();
                            String fieldName = null;

                            String auth = null;
                            String dept = null;
                            String filename = null;
                            String contentType = null;
                            boolean isInMemory = false;
                            long sizeInBytes = 0;
                            InputStream uploadedStream = null;

                            while (iter.hasNext())
                            {
                                FileItem item = (FileItem) iter.next();

                                if (item.isFormField())
                                {
                                    String name = item.getFieldName();
                                    String value = item.getString();

                                    if (name.equals("title"))
                                    {
                                        title = value;
                                    }
                                    else if (name.equals("author"))
                                    {
                                        auth = value;
                                    }
                                    else if (name.equals("dept"))
                                    {
                                        dept = value;
                                    }
                                    else
                                    {
                                        // name is wrong
                                    }
                                }
                                else
                                {
                                    fieldName = item.getFieldName();
                                    filename = item.getName();
                                    contentType = item.getContentType();
                                    isInMemory = item.isInMemory();
                                    sizeInBytes = item.getSize();
                                    uploadedStream = item.getInputStream();
                                }
                            }

                            if (userDept.contains(dept))
                            {
                                PreparedStatement psmt = conn.prepareStatement("insert into mydb.docs(title,auth,dept,ouid,created,filename,file)" + "values(?,?,?,?,?,?,?)");
                                psmt.setString(1, title);
                                psmt.setString(2, auth);
                                psmt.setString(3, dept);
                                psmt.setString(4, uid);
                                psmt.setString(5, (new Timestamp((new GregorianCalendar()).getTimeInMillis())).toString());
                                psmt.setString(6, filename);
                                psmt.setBinaryStream(7, uploadedStream, (int) sizeInBytes);

                                int s = psmt.executeUpdate();
                            }
                            else
                            {
                                // wrong dept
                            }

                            uploadedStream.close();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e);
                        }
                    }
                }
                else
                {
                    // user not in db
                }

                stmt.close();
            }
            catch (Exception e)
            {
                System.err.println(e);
            }
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result,time) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "'upload','"
                    + String.valueOf(result) + "','" + ((new Date((new GregorianCalendar()).getTimeInMillis())).toString()) + "'";
            logStmt.executeUpdate(logQuery);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }
}