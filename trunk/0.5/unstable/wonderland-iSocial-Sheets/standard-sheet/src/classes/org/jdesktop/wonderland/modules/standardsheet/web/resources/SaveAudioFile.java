/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.web.resources;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.*;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author nilang
 */

/**
 * SaveAudioFile is used to generate .AU file for recorded question in audio question type
 * 
 */
public class SaveAudioFile extends HttpServlet {

    
    AudioInputStream audioInputStream;
    AudioInputStream audioInputStream1;
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        
        try {
            String fname = request.getParameter("fname");
            InputStream fis = request.getInputStream();
            
            byte buf[]=IOUtils.toByteArray(fis);
            byte buf1[]=IOUtils.toByteArray(fis);
            String path = getServletConfig().getServletContext().getRealPath("../../../");
            
            //save au file
            File f=new File(path+"/content/groups/users/audiosheet/"+fname+".au");
            if(!f.getParentFile().exists()) {
                boolean s = f.getParentFile().mkdirs();
            }
          
            InputStream byteArrayInputStream = new ByteArrayInputStream(buf);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream =new AudioInputStream(byteArrayInputStream,audioFormat,
            buf.length/audioFormat.getFrameSize());
            
            if (AudioSystem.isFileTypeSupported(AudioFileFormat.Type.AU,audioInputStream)) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.AU, f);
            }

            //save wav file
            File wavFile = new File(path+"/content/groups/users/audiosheet/"+fname+".wav");
            if(!wavFile.getParentFile().exists()) {
                boolean s = wavFile.getParentFile().mkdirs();
            }
            audioInputStream1 = AudioSystem.getAudioInputStream(f);
            AudioSystem.write(audioInputStream1, AudioFileFormat.Type.WAVE, wavFile);
        } 
        catch (Exception ex) {
            Logger.getLogger(SaveAudioFile.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private AudioFormat getAudioFormat(){
        float sampleRate = 22050.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                        sampleRate,
                        sampleSizeInBits,
                        channels,
                        signed,
                        bigEndian);
    }//end getAudioFormat
    
    private AudioFormat getAudioFormatForWav(){
        float sampleRate = 11025.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 8;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                        sampleRate,
                        sampleSizeInBits,
                        channels,
                        signed,
                        bigEndian);
    }//end getAudioFormat

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
