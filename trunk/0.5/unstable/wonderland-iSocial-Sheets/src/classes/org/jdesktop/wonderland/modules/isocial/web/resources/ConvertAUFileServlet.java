/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.web.resources;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;

/*
* ConvertAUFileServlet used to covert au file to wav file.
* It is used for playing question and answer in browser for standard sheet.
*/
public class ConvertAUFileServlet extends HttpServlet {

    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{
            String fname = request.getParameter("filename");
            String repos = request.getParameter("repository");
            String username = request.getParameter("username");
            URL sourceURL = null;
            String fnameE = URLEncoder.encode(fname);
            String usernameE = URLEncoder.encode(username);
            String qid;
            
            System.out.println("fname : "+fname);
            System.out.println("repos : "+repos);
            System.out.println("username : "+username);
            String sheetId = null;
            WebContentRepositoryRegistry reg = WebContentRepositoryRegistry.getInstance();
            WebContentRepository repo = reg.getRepository(getServletContext());
            ContentResource sourceFile=null;
            if(repos.equals("questionCap")) {
                ContentCollection userdir = repo.getUserRoot(request.getParameter("username"));
                ContentCollection qca = (ContentCollection) userdir.getChild("question-capability-audio");
                ContentCollection temp = (ContentCollection) qca.getChild("temp");
                sourceFile = (ContentResource) temp.getChild("untitled.au");
                sourceURL = sourceFile.getURL();
                System.out.println("source : "+sourceURL);
                URI uri = new URI(sourceURL.getProtocol(), sourceURL.getUserInfo()
                        , sourceURL.getHost(), sourceURL.getPort(), sourceURL.getPath()
                        , sourceURL.getQuery(), sourceURL.getRef());
                sourceURL = uri.toURL();
                System.out.println("source : "+sourceURL);
            } else if(repos.equals("standardSheetAudio")) {
                
            } else if(repos.equals("standardSheetRecording")) {
                sheetId = request.getParameter("sheetId");
                qid = request.getParameter("qid");
                ContentCollection userdir = repo.getUserRoot(request.getParameter("username"));
                ContentCollection audiosheet = (ContentCollection) userdir.getChild("audiosheet");
                ContentCollection temp = (ContentCollection) audiosheet.getChild("temp");
                sourceFile = (ContentResource) temp.getChild("untitled-"+qid+".au");
                sourceURL = sourceFile.getURL();
                System.out.println("source : "+sourceURL);
                URI uri = new URI(sourceURL.getProtocol(), sourceURL.getUserInfo()
                        , sourceURL.getHost(), sourceURL.getPort(), sourceURL.getPath()
                        , sourceURL.getQuery(), sourceURL.getRef());
                sourceURL = uri.toURL();
                System.out.println("source : "+sourceURL);
            } else if(repos.equals("audioCap")) {
                ContentCollection userdir = repo.getUserRoot(request.getParameter("username"));
                ContentCollection qca = (ContentCollection) userdir.getChild("audio");
                ContentCollection temp = (ContentCollection) qca.getChild("temp");
                sourceFile = (ContentResource) temp.getChild("untitled.au");
                sourceURL = sourceFile.getURL();
                System.out.println("source : "+sourceURL);
                URI uri = new URI(sourceURL.getProtocol(), sourceURL.getUserInfo()
                        , sourceURL.getHost(), sourceURL.getPort(), sourceURL.getPath()
                        , sourceURL.getQuery(), sourceURL.getRef());
                sourceURL = uri.toURL();
                System.out.println("source : "+sourceURL);
            }
           
            AudioFileFormat.Type outputType =
                            AudioFileFormat.Type.WAVE;
            
            String path = getServletConfig().getServletContext().getRealPath("../../../");
            
            File destWav = null;
            File destAu = null;
            if(repos.equals("questionCap")) {
                destWav = new File(path+"/content/users/"+request.getParameter("username")+"/question-capability-audio/"+fname+".wav");
                destAu = new File(path+"/content/users/"+request.getParameter("username")+"/question-capability-audio/"+fname+".au");
            } else if(repos.equals("standardSheetAudio")) {
                
            } else if(repos.equals("standardSheetRecording")) {
                destWav = new File(path+"/content/users/"+request.getParameter("username")+"/audiosheet/"+sheetId+"/"+fname+".wav");
                destAu = new File(path+"/content/users/"+request.getParameter("username")+"/audiosheet/"+sheetId+"/"+fname+".au");
            } else if(repos.equals("audioCap")) {
                //destWav = new File(path+"/content/users/"+request.getParameter("username")+"/audio/"+fname+".wav");
                destAu = new File(path+"/content/users/"+request.getParameter("username")+"/audio/"+fname+".au");
            }
            if(destWav!=null) {
                if(!destWav.getParentFile().exists()) {
                    boolean s = destWav.getParentFile().mkdirs();
                }
            }
            if(destAu!=null) {
                if(!destAu.getParentFile().exists()) {
                    boolean s = destAu.getParentFile().mkdirs();
                }
            }
            AudioInputStream audioInputStream = null;
            while(audioInputStream==null){
                System.out.println("audioInputStream : "+audioInputStream);
                try{
                audioInputStream = AudioSystem.getAudioInputStream(sourceURL);
                } catch (Exception e) { }
                //showFileType(inputFileObj);
                int bytesWritten = 0;
                if(audioInputStream!=null) {
                    if(destWav!=null) {
                        bytesWritten = AudioSystem.write(audioInputStream,outputType,destWav);
                    }
                    if(destAu!=null) {
                        FileInputStream fis = (FileInputStream) sourceFile.getInputStream();
                        FileOutputStream fos = new FileOutputStream(destAu);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = fis.read(buf)) > 0) { 
                            fos.write(buf, 0, len);
                        }
                        fis.close();
                        fos.close();
                    }
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("GET");
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("POST");
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
