/**
 *  '$RCSfile: EMLParserServlet.java,v $'
 *    Purpose: A Class that implements the servlet interface to the
               analytical processing engine Monarch
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2002-10-02 22:19:29 $'
 * '$Revision: 1.1 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ecoinformatics.eml;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.zip.*;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;
import javax.servlet.ServletOutputStream;

/**
 * Servlet interface for the EMLParser
 */
public class EMLParserServlet extends HttpServlet
{

  private ServletConfig servletconfig = null;
  private ServletContext context = null;
  private HttpServletRequest request;
  private static HttpServletResponse response;
  private static PrintWriter out = null;
  Hashtable params = new Hashtable();

  /**
   * Initialize the servlet
   */
  public void init(ServletConfig servletconfig) throws ServletException
  {
    try
    {
      super.init(servletconfig);
      this.servletconfig = servletconfig;
      this.context = servletconfig.getServletContext();
      System.out.println("Starting EMLParserServlet");
    }
    catch (ServletException ex)
    {
      throw ex;
    }
  }

  /**
   * Destroy the servlet
   */
  public void destroy()
  {
    System.out.println("Destroying EMLParserServlet");
  }

  /** Handle "GET" method requests from HTTP clients */
  public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    // Process the data and send back the response
    handleGetOrPost(request, response);
  }

  /** Handle "POST" method requests from HTTP clients */
  public void doPost( HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    // Process the data and send back the response
    handleGetOrPost(request, response);
  }

  /**
   * Control servlet response depending on the action parameter specified
   */
  private void handleGetOrPost(HttpServletRequest request,
                               HttpServletResponse response)
                               throws ServletException, IOException
  {
    this.request = request;
    this.response = response;
    StringBuffer html = new StringBuffer();
    out = response.getWriter();
    String ctype = request.getContentType();
    InputStream fileToParse = null;
    File tempfile = null;
    html.append("<html><head>");
    html.append("<link type=\"text/css\" href=\"");
    html.append("http://knb.ecoinformatics.org/default.css\" ");
    html.append("rel=\"stylesheet\">");
    html.append("</link></head><body>");

    HttpSession sess = request.getSession(true);
    String sess_id = "";
    try
    { //get the cookie for the session.
      sess_id = (String)sess.getId();
    }
    catch(IllegalStateException ise)
    {
      System.out.println("error in handleGetOrPost: this shouldn't " +
                         "happen: the session should be valid: " +
                         ise.getMessage());
    }

    if (ctype != null && ctype.startsWith("multipart/form-data"))
    { //deal with multipart encoding of the package zip file
      try
      {
        fileToParse = handleGetFile(request, response);
        int c = fileToParse.read();
        tempfile = new File(".tmpfile." + sess_id);
        FileOutputStream fos = new FileOutputStream(tempfile);
        while(c != -1)
        {
          fos.write(c);
          c = fileToParse.read();
        }
        fos.flush();
        fos.close();
      }
      catch(Exception e)
      {
        out.println("<html><body><h1>Error handling multipart data: " +
                    e.getMessage() + "</h1></body></html>");
        System.out.println("Error handling multipart data: " + e.getMessage());
        e.printStackTrace();
      }
    }

    String action = (String)params.get("action");

    if(action.equals("parse"))
    { //parse action
      try
      {
        if(fileToParse != null)
        {
          EMLParser parser = new EMLParser(tempfile,
                             new File("../webapps/emlparser/lib/config.xml"));
          html.append("<h2>Document valid.</h2><p>There ");
          html.append("were no EML errors found in your document.</p>");
          tempfile.delete();
        }
        else
        {
          html.append("<h2>Error: The file sent to the parser was null.</h2>");
        }
      }
      catch(Exception e)
      {
        html.append("<h2>Errors Found</h2><p>The following errors were found:");
        html.append("</p><p>").append(e.getMessage()).append("</p>");
      }
      html.append("</body></html>");
    }
    else
    {
      html.append("<h2>Error.  Action '").append(action);
      html.append("' not registered");
    }

    response.setContentType("text/html");
    out.println(html.toString());
    out.flush();
  }

   /**
   * This method deals with getting the zip file from the client, unzipping
   * it, then running the process on it.
   */
  private InputStream handleGetFile(HttpServletRequest request,
                               HttpServletResponse response)
                               throws Exception
  {
    Hashtable fileList = new Hashtable();
    try
    {
      MultipartParser mp = new MultipartParser(request, 1024 * 1024 * 8);
      Part part;
      while ((part = mp.readNextPart()) != null)
      {
        String name = part.getName();
        if (part.isParam())
        { // it's a parameter part
          ParamPart paramPart = (ParamPart) part;
          String value = paramPart.getStringValue();
          params.put(name, value);
        }
        else if (part.isFile())
        { // it's a file part
          FilePart filePart = (FilePart) part;
          fileList.put(name, filePart);
          // Stop once the first file part is found, otherwise going onto the
          // next part prevents access to the file contents.  So...for upload
          // to work, the datafile must be the last part
          break;
        }
      }
    }
    catch (Exception ioe)
    {
      throw ioe;
    }

    //now that we have the file, do some checking and get the files we need
    //out of the zip file.
    FilePart fp = (FilePart)fileList.get("filename");
    return fp.getInputStream();
  }
}
