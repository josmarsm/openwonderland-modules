/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.web.resources;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author nilang
 */
/*
* ConvertAUFileServlet used to covert au file to wav file.
* It is used for playing question and answer in browser for standard sheet.
*/
public class ConvertAUFileServlet extends HttpServlet {

    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try{
        
            String path = getServletConfig().getServletContext().getRealPath("../../../");
            String f1 = path+request.getParameter("filepath");
            String f2 = f1.substring(0, f1.length()-3)+".wav";
            String outputTypeStr =
                            f2.substring(f2.
                                lastIndexOf(".") + 1);
            
            AudioFileFormat.Type outputType =
                            getTargetType(outputTypeStr);
            if(outputType != null){}
            else{
                getTargetTypesSupported();
                System.exit(0);
            }//end else

            //Note that input file type does not depend
            // on file name or extension.
            File inputFileObj = new File(f1);
            AudioInputStream audioInputStream = null;
            audioInputStream = AudioSystem.getAudioInputStream(inputFileObj);
            showFileType(inputFileObj);
            int bytesWritten = 0;
            bytesWritten = AudioSystem.write(audioInputStream,outputType,new File(f2));
            showFileType(new File(f2));
            out.println("success");
        } catch (Exception e) {
            out.println(e);
           e.printStackTrace();
           
        }
        
        
    }
    
    public  void getTargetTypesSupported(){
        AudioFileFormat.Type[] typesSupported = AudioSystem.getAudioFileTypes();
    }//end getTargetTypesSupported

    public  AudioFileFormat.Type
                    getTargetType(String extension){
        AudioFileFormat.Type[] typesSupported =
                    AudioSystem.getAudioFileTypes();
        for(int i = 0; i < typesSupported.length;
                                                i++){
        if(typesSupported[i].getExtension().
                                equals(extension)){
            return typesSupported[i];
        }//end if
        }//end for loop
        return null;//no match
    }//end getTargetType

    public void showFileType(File file){
        try{
            AudioSystem.getAudioFileFormat(file);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }//end catch
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
