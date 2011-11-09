package com.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.sql.*;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.*;
import javax.naming.*;

public class FileUpload extends HttpServlet
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
        String deptSet = "HR,LS,IT,SP,RD,FN";

        boolean result = false;
        boolean validFileType = false;
        String user = request.getRemoteUser();
        String title = null;

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

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

                            validFileType = (filename.endsWith(".pdf")
                                    || filename.endsWith(".doc")
                                    || filename.endsWith(".docx")
                                    || filename.endsWith(".xls")
                                    || filename.endsWith("xlsx")
                                    || filename.endsWith("ppt")
                                    || filename.endsWith("pptx")
                                    || filename.endsWith(".txt")
                                    || filename.endsWith("jpg")
                                    || filename.endsWith("jpeg")
                                    || filename.endsWith("png"));

                            String titleRegex = "[\\w]{1,45}+";
                            String authRegex = "[\\w]{1,45}+";
                            String fileNameRegex = "([\\w\\_-]+\\.([a-zA-Z]{1,4}+)){1,45}";

                            Pattern titlePattern = Pattern.compile(titleRegex);
                            Pattern authPattern = Pattern.compile(authRegex);
                            Pattern filenamePattern = Pattern.compile(fileNameRegex);

                            Matcher titleMatcher = titlePattern.matcher(title);
                            Matcher authMatcher = authPattern.matcher(auth);
                            Matcher filenameMatcher = filenamePattern.matcher(filename);

                            if ((title != null) && !(title.isEmpty()) && titleMatcher.matches())
                            {
                                if ((auth != null) && !(auth.isEmpty()) && authMatcher.matches())
                                {
                                    if (validFileType && (filename != null) && !(filename.isEmpty()) && filenameMatcher.matches())
                                    {
                                        if ((dept != null) && !(dept.isEmpty()) && deptSet.contains(dept))
                                        {
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
                                                result = true;

                                                out.println("<html>");
                                                out.println("<head>");
                                                out.println("<title>File Upload</title>");
                                                out.println("</head>");
                                                out.println("<body>");
                                                out.println("<h1>File uploaded...</h1>");
                                                out.println("</body>");
                                                out.println("</html>");
                                                response.setHeader("Refresh", "5;fileupload.jsp");
                                            }
                                            else
                                            {
                                                // wrong dept
                                                out.println("<html>");
                                                out.println("<head>");
                                                out.println("<title>File Upload</title>");
                                                out.println("</head>");
                                                out.println("<body>");
                                                out.println("<h1>You selected a wrong department...</h1>");
                                                out.println("</body>");
                                                out.println("</html>");
                                                response.setHeader("Refresh", "5;fileupload.jsp");
                                            }

                                            uploadedStream.close();
                                        }
                                        else
                                        {
                                            // bad dept
                                            out.println("<html>");
                                            out.println("<head>");
                                            out.println("<title>File Upload</title>");
                                            out.println("</head>");
                                            out.println("<body>");
                                            out.println("<h1>You selected an invalid department...</h1>");
                                            out.println("</body>");
                                            out.println("</html>");
                                            response.setHeader("Refresh", "5;fileupload.jsp");
                                        }
                                    }
                                    else
                                    {
                                        // bad filename
                                        out.println("<html>");
                                        out.println("<head>");
                                        out.println("<title>File Upload</title>");
                                        out.println("</head>");
                                        out.println("<body>");
                                        out.println("<h1>You selected an invalid filename or filetype...</h1>");
                                        out.println("</body>");
                                        out.println("</html>");
                                        response.setHeader("Refresh", "5;fileupload.jsp");
                                    }
                                }
                                else
                                {
                                    // bad author
                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>File Upload</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>You selected an invalid autor name...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;fileupload.jsp");
                                }
                            }
                            else
                            {
                                // bad title
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Upload</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>You selected an invalid title...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;fileupload.jsp");
                            }
                        }
                        catch (Exception e)
                        {
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Upload</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>Error encountered uploading file...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;fileupload.jsp");
                        }
                    }
                }
                else
                {
                    // user not in db
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Upload</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>You are not a valid user...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;fileupload.jsp");
                }

                stmt.close();
            }
            catch (Exception e)
            {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Upload</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error encountered uploading file...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;fileupload.jsp");
            }
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "upload','"
                    + String.valueOf(result) + "')";
            logStmt.executeUpdate(logQuery);
            logStmt.close();
        }
        catch (Exception e)
        {
            // logging failed
            e.printStackTrace();
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