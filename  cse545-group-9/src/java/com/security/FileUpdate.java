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

public class FileUpdate extends HttpServlet
{
    private InitialContext ctx;
    private DataSource ds;
    private Connection conn;

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

        boolean result = false;
        boolean validFileType = false;
        String user = request.getRemoteUser();
        String title = "";
        String encrypt = null;
        String key = null;

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Check that we have a file upload request
        if (ServletFileUpload.isMultipartContent(request))
        {
            Statement userStmt = null;
            Statement docStmt = null;
            Statement ownerStmt = null;
            Statement shareStmt = null;
            Statement lockStmt = null;

            String uid = "";
            String did = "";
            String ouid = "";
            String userDept = "";
            String docDept = "";

            int role = 0;

            String fieldName = "";
            String newTitle = "";
            String newAuth = "";
            String newDept = "";
            String newFilename = "";
            String contentType = "";
            boolean isInMemory = false;
            long sizeInBytes = 0;
            InputStream uploadedStream = null;

            boolean newFile = false;

            String userQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                    + " WHERE " + "uname" + " = '" + user + "'";

            String docQuery = "";
            String ownerQuery = "";
            String shareQuery = "";

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

                        if (!value.isEmpty())
                        {
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
                            else if (name.equals("key"))
                            {
                                key = value;
                            }
                            else if (name.equals("enc"))
                            {
                                encrypt = value;
                            }
                            else
                            {
                                // name is wrong
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Update</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>Error parsing form data...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;FileUpdatePage");
                            }
                        }
                    }
                    else
                    {
                        fieldName = item.getFieldName();
                        newFilename = item.getName();
                        contentType = item.getContentType();
                        isInMemory = item.isInMemory();
                        sizeInBytes = item.getSize();
                        uploadedStream = item.getInputStream();
                        if (sizeInBytes > 0)
                        {
                            newFile = true;
                        }
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
                    lockStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                    ResultSet userRs = userStmt.executeQuery(userQuery);
                    ResultSet docRs = docStmt.executeQuery(docQuery);
                    ResultSet ownerRs = null;
                    ResultSet shareRs = null;
                    ResultSet lockRs = null;

                    if (userRs.next() && docRs.next())
                    {
                        uid = String.valueOf(userRs.getInt("uid"));
                        userDept = userRs.getString("dept");
                        role = userRs.getInt("role");

                        did = String.valueOf(docRs.getInt("did"));
                        docDept = docRs.getString("dept");
                        ouid = docRs.getString("ouid");

                        if ((newTitle == null) || (newTitle.isEmpty()))
                        {
                            newTitle = title;
                        }

                        if ((newAuth == null) || (newAuth.isEmpty()))
                        {
                            newAuth = docRs.getString("auth");
                        }

                        if ((newDept == null) || (newDept.isEmpty()))
                        {
                            newDept = docRs.getString("dept");
                        }

                        if ((newFilename == null) || (newFilename.isEmpty()))
                        {
                            newFilename = docRs.getString("filename");
                        }

                        String titleRegex = "[\\w\\s]{1,45}+";
                        String authRegex = "[\\w\\s]{1,45}+";
                        String fileNameRegex = "((^[\\w\\_\\-\\.]+[\\w\\_\\-\\.\\s]*)\\.([a-zA-Z]{1,4}+)){1,45}";

                        Pattern titlePattern = Pattern.compile(titleRegex);
                        Pattern authPattern = Pattern.compile(authRegex);
                        Pattern filenamePattern = Pattern.compile(fileNameRegex);

                        Matcher titleMatcher = titlePattern.matcher(newTitle);
                        Matcher authMatcher = authPattern.matcher(newAuth);
                        Matcher filenameMatcher = filenamePattern.matcher(newFilename);

                        if ((key != null) && (encrypt != null) && encrypt.equalsIgnoreCase("yes"))
                        {
                            AESEncrypt enc = new AESEncrypt();
                            uploadedStream = enc.encryptfile(uploadedStream, key);
                        }

                        String testFileName = newFilename;
                        testFileName = testFileName.toLowerCase();

                        validFileType = (testFileName.endsWith(".pdf")
                                || testFileName.endsWith(".doc")
                                || testFileName.endsWith(".docx")
                                || testFileName.endsWith(".xls")
                                || testFileName.endsWith("xlsx")
                                || testFileName.endsWith("ppt")
                                || testFileName.endsWith("pptx")
                                || testFileName.endsWith(".txt")
                                || testFileName.endsWith("jpg")
                                || testFileName.endsWith("jpeg")
                                || testFileName.endsWith("png"));

                        if (titleMatcher.matches())
                        {
                            if (authMatcher.matches())
                            {
                                if (deptSet.contains(newDept))
                                {
                                    if (validFileType && filenameMatcher.matches())
                                    {
                                        ownerQuery = "SELECT * FROM " + "mydb" + "." + "Users"
                                                + " WHERE " + "uid" + " = " + ouid + "";

                                        shareQuery = "SELECT * FROM " + "mydb" + "." + "Shared"
                                                + " WHERE " + "sdid" + "=" + did + " AND " + "suid" + "=" + uid + " AND " + "perm" + " = '" + "U" + "'";

                                        String lockQuery = "SELECT * FROM " + "mydb" + "." + "Locked"
                                                + " WHERE " + "ldid" + " = " + did + "";

                                        ownerRs = ownerStmt.executeQuery(ownerQuery);
                                        shareRs = shareStmt.executeQuery(shareQuery);
                                        lockRs = lockStmt.executeQuery(lockQuery);

                                        int ownerRole = 0;

                                        if (ownerRs.next())
                                        {
                                            ownerRole = ownerRs.getInt("role");
                                        }

                                        boolean shared = false;
                                        boolean updatePerm = false;

                                        if (shareRs.next())
                                        {
                                            shared = true;
                                            updatePerm = shareRs.getString("perm").equals("U");
                                        }

                                        boolean userIsOwner = uid.equals(ouid);
                                        boolean userIsManager = (role > Roles.REG_EMP.ordinal());
                                        boolean userMeetsRoleReq = (role >= ownerRole);
                                        boolean userMeetsDeptReq = (userDept.contains(docDept));
                                        boolean locked = false;
                                        boolean userHasLock = false;

                                        if (lockRs.next())
                                        {
                                            locked = true;
                                            userHasLock = uid.equals(String.valueOf(lockRs.getInt("luid")));
                                        }

                                        if (userIsOwner || (shared && updatePerm) || (userIsManager && userMeetsRoleReq && userMeetsDeptReq))
                                        {
                                            if (!locked || (locked && userHasLock))
                                            {
                                                try
                                                {
                                                    if (userDept.contains(newDept) || shared)
                                                    {
                                                        PreparedStatement psmt = null;

                                                        if (newFile)
                                                        {
                                                            psmt = conn.prepareStatement("UPDATE mydb.docs SET title=?,auth=?,dept=?,lastMod=?,filename=?,file=? WHERE did = " + did);
                                                            psmt.setString(1, newTitle);
                                                            psmt.setString(2, newAuth);
                                                            psmt.setString(3, newDept);
                                                            psmt.setString(4, (new Timestamp((new GregorianCalendar()).getTimeInMillis())).toString());
                                                            psmt.setString(5, newFilename);
                                                            psmt.setBinaryStream(6, uploadedStream, (int) sizeInBytes);
                                                        }
                                                        else
                                                        {
                                                            psmt = conn.prepareStatement("UPDATE mydb.docs SET title=?,auth=?,dept=?,lastMod=?,filename=? WHERE did = " + did);
                                                            psmt.setString(1, newTitle);
                                                            psmt.setString(2, newAuth);
                                                            psmt.setString(3, newDept);
                                                            psmt.setString(4, (new Timestamp((new GregorianCalendar()).getTimeInMillis())).toString());
                                                            psmt.setString(5, newFilename);
                                                        }

                                                        int s = psmt.executeUpdate();
                                                        result = true;

                                                        out.println("<html>");
                                                        out.println("<head>");
                                                        out.println("<title>File Update</title>");
                                                        out.println("</head>");
                                                        out.println("<body>");
                                                        out.println("<h1>File updated successfully...</h1>");
                                                        out.println("</body>");
                                                        out.println("</html>");
                                                        response.setHeader("Refresh", "5;FileUpdatePage");
                                                    }
                                                    else
                                                    {
                                                        // wrong dept
                                                        out.println("<html>");
                                                        out.println("<head>");
                                                        out.println("<title>File Update</title>");
                                                        out.println("</head>");
                                                        out.println("<body>");
                                                        out.println("<h1>You selected a department you do not belong to...</h1>");
                                                        out.println("</body>");
                                                        out.println("</html>");
                                                        response.setHeader("Refresh", "5;FileUpdatePage");
                                                    }

                                                    uploadedStream.close();
                                                }
                                                catch (Exception e)
                                                {
                                                    out.println("<html>");
                                                    out.println("<head>");
                                                    out.println("<title>File Update</title>");
                                                    out.println("</head>");
                                                    out.println("<body>");
                                                    if (e.getMessage().contains("Duplicate"))
                                                    {
                                                        out.println("<h1>File already exists with that title and/or filename...</h1>");
                                                    }
                                                    else
                                                    {
                                                        out.println("<h1>Error updating file...</h1>");
                                                    }
                                                    out.println("</body>");
                                                    out.println("</html>");
                                                    response.setHeader("Refresh", "5;FileUpdatePage");
                                                }
                                            }
                                            else
                                            {
                                                // file locked
                                                out.println("<html>");
                                                out.println("<head>");
                                                out.println("<title>File Update</title>");
                                                out.println("</head>");
                                                out.println("<body>");
                                                out.println("<h1>Someone else has file locked...</h1>");
                                                out.println("</body>");
                                                out.println("</html>");
                                                response.setHeader("Refresh", "5;FileUpdatePage");
                                            }
                                        }
                                        else
                                        {
                                            // wrong permissions
                                            out.println("<html>");
                                            out.println("<head>");
                                            out.println("<title>File Update</title>");
                                            out.println("</head>");
                                            out.println("<body>");
                                            out.println("<h1>Document owner is invalid...</h1>");
                                            out.println("</body>");
                                            out.println("</html>");
                                            response.setHeader("Refresh", "5;FileUpdatePage");
                                        }
                                    }
                                    else
                                    {
                                        // bad filename
                                        out.println("<html>");
                                        out.println("<head>");
                                        out.println("<title>File Update</title>");
                                        out.println("</head>");
                                        out.println("<body>");
                                        out.println("<h1>You entered an invalid filename or filetype...</h1>");
                                        out.println("</body>");
                                        out.println("</html>");
                                        response.setHeader("Refresh", "5;FileUpdatePage");
                                    }
                                }
                                else
                                {
                                    // bad dept
                                    out.println("<html>");
                                    out.println("<head>");
                                    out.println("<title>File Update</title>");
                                    out.println("</head>");
                                    out.println("<body>");
                                    out.println("<h1>You entered an invalid department...</h1>");
                                    out.println("</body>");
                                    out.println("</html>");
                                    response.setHeader("Refresh", "5;FileUpdatePage");
                                }
                            }
                            else
                            {
                                // bad author
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>File Update</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("<h1>You entered an invalid author...</h1>");
                                out.println("</body>");
                                out.println("</html>");
                                response.setHeader("Refresh", "5;FileUpdatePage");
                            }
                        }
                        else
                        {
                            // bad title
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<title>File Update</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<h1>You entered an invalid title...</h1>");
                            out.println("</body>");
                            out.println("</html>");
                            response.setHeader("Refresh", "5;FileUpdatePage");
                        }
                    }
                    else
                    {
                        // user not in db
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>File Update</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>You are not a valid user...</h1>");
                        out.println("</body>");
                        out.println("</html>");
                        response.setHeader("Refresh", "5;FileUpdatePage");
                    }

                    userStmt.close();
                    docStmt.close();
                    shareStmt.close();
                }
                catch (Exception e)
                {
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>File Update</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error accessing document information...</h1>");
                    out.println("</body>");
                    out.println("</html>");
                    response.setHeader("Refresh", "5;FileUpdatePage");
                }
            }
            catch (Exception e)
            {
                // error uploading document or data
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Update</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error uploading document or data...</h1>");
                out.println("</body>");
                out.println("</html>");
                response.setHeader("Refresh", "5;FileUpdatePage");
            }
        }

        // log result
        try
        {
            Statement logStmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String logQuery = "INSERT INTO mydb.Log (uname,title,action,result) VALUES ('"
                    + user + "','"
                    + title + "','"
                    + "update','"
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