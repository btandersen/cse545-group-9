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

public class FileUpdate extends HttpServlet
{
    private InitialContext ctx;
    private DataSource ds;
    private Connection conn;

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
        // Check that we have a file upload request
        if (ServletFileUpload.isMultipartContent(request))
        {
            Statement userStmt = null;
            Statement docStmt = null;
            Statement ownerStmt = null;
            Statement shareStmt = null;

            String user = request.getRemoteUser();
            String title = null;

            String uid = null;
            String did = null;
            String ouid = null;
            String userDept = null;
            String docDept = null;

            int role = 0;

            String fieldName = null;
            String newTitle = null;
            String newAuth = null;
            String newDept = null;
            String filename = null;
            String contentType = null;
            boolean isInMemory = false;
            long sizeInBytes = 0;
            InputStream uploadedStream = null;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            String docQuery = null;
            String ownerQuery = null;
            String shareQuery = null;

            try
            {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List /* FileItem */ items = upload.parseRequest(request);
                Iterator iter = items.iterator();

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
                        else if (name.equals("newtitle"))
                        {
                            newTitle = value;
                        }
                        else if (name.equals("newauthor"))
                        {
                            newAuth = value;
                        }
                        else if (name.equals("newdept"))
                        {
                            newDept = value;
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
                try
                {
                    docQuery = "SELECT * FROM " + "mydb" + "." + "Docs"
                            + " WHERE " + "title" + " = '" + title + "'";

                    userStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    docStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    ownerStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    shareStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    ResultSet userRs = userStmt.executeQuery(userQuery);
                    ResultSet docRs = docStmt.executeQuery(docQuery);
                    ResultSet ownerRs = null;
                    ResultSet shareRs = null;

                    if (userRs.next() && docRs.next())
                    {
                        uid = String.valueOf(userRs.getInt("uid"));
                        userDept = userRs.getString("dept");
                        role = userRs.getInt("role");

                        did = String.valueOf(docRs.getInt("did"));
                        docDept = docRs.getString("dept");
                        ouid = docRs.getString("ouid");

                        ownerQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                                + " WHERE " + "uname" + " = '" + ouid + "'";

                        shareQuery = "SELECT * FROM " + "mydb" + "." + "Shared"
                                + " WHERE " + "sdid" + "=" + did + " AND " + "suid" + "=" + uid + " AND " + "perm" + " = '" + "U" + "'";
                        
                        ownerRs = ownerStmt.executeQuery(ownerQuery);
                        shareRs = shareStmt.executeQuery(shareQuery);

                        if (uid.equals(ouid) || shareRs.next() || ((Roles.REG_EMP.ordinal() < role) && (ownerRs.getInt("role") <= role) && userDept.contains(docRs.getString("dept"))))
                        {
                            try
                            {
                                if (userDept.equals(newDept))
                                {
//                                    Date lastMod = new Date((new GregorianCalendar()).getTimeInMillis());
//                                    docRs.updateString("title", newTitle);
//                                    docRs.updateString("auth", newAuth);
//                                    docRs.updateString("dept", newDept);
//                                    docRs.updateDate("lastMod", lastMod);
//                                    docRs.updateString("filename", filename);
//                                    docRs.updateBinaryStream("file", uploadedStream, sizeInBytes);
//                                    docRs.updateRow();

                                    PreparedStatement psmt = conn.prepareStatement("UPDATE mydb.docs SET title=?,auth=?,dept=?,lastMod=?,filename=?,file=? WHERE did = " + did);
                                    psmt.setString(1, newTitle);
                                    psmt.setString(2, newAuth);
                                    psmt.setString(3, newDept);
                                    psmt.setString(4, (new Date((new GregorianCalendar()).getTimeInMillis())).toString());
                                    psmt.setString(5, filename);
                                    psmt.setBinaryStream(6, uploadedStream, (int) sizeInBytes);

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

                    userStmt.close();
                    docStmt.close();
                    shareStmt.close();
                }
                catch (Exception e)
                {
                    System.err.println(e);
                }
            }
            catch (Exception e)
            {
                //
            }
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