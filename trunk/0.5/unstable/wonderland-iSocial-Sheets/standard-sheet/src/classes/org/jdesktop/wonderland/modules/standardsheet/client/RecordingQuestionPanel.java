/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;

/**
 *
 */
public class RecordingQuestionPanel extends javax.swing.JPanel implements StandardQuestionPanel{

    /**
     * Creates new form RecordingPanel
     */
    public AudioRecordingConnection audioConnection;
    private StandardQuestion question;
    private Sheet sheet;
    private String username;
    private String fname=null;
    private String callID;
    private static final String START_MSG = "Click to Record";
    private static final String STOP_MSG = "Click to Stop";
    private String RECORDING = "Recording...";
    private String MAXLENGTH;
    
    private ImageIcon play_pressed;
    private ImageIcon play_disabled;
    private ImageIcon stop_disabled;
    private ImageIcon stop_pressed;
    private ImageIcon start_recording;
    private ImageIcon stop_recording;
    
    private boolean recording_done = false;
    private boolean is_playing = false;
    private String recording_status="stopped";
    private String max_length;
    private Timer globalTimer;
    private ISocialManager manager;
    private String old_filename;
    private ArrayList<JComponent> out;
    private Clip clip = null;
    String userRole;
    private boolean submitAnswer=false;
    private boolean playUntitled;
    private String untitledFileName;
    
    public RecordingQuestionPanel(StandardQuestion question) {
       this.question = question;
       max_length = question.getProperties().get("maxlength");
       MAXLENGTH = "("+max_length+" minute maximum)";
       RECORDING = "Recording...("+max_length+":00)";
       initComponents();
       
       URL url = RecordingQuestionPanel.class.getResource("resources/PlayDisabled-32.png");
       play_disabled =  new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       url = RecordingQuestionPanel.class.getResource("resources/Stop1Disabled-32.png");
       stop_disabled =  new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       url = RecordingQuestionPanel.class.getResource("resources/PlayPressedBlue-32.png");
       play_pressed =  new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       url = RecordingQuestionPanel.class.getResource("resources/Stop1PressedBlue-32.png");
       stop_pressed =  new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       url = RecordingQuestionPanel.class.getResource("resources/RecordPressed.png");
       start_recording = new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       url = RecordingQuestionPanel.class.getResource("resources/StopRed.png");
       stop_recording = new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
       
       recording.setIcon(start_recording);
       play.setIcon(play_disabled);
       stop.setIcon(stop_disabled);
       
       playUntitled=false;
       untitledFileName = "untitled-"+question.getId();
    }
    
    
    
    public int getId() {
        return question.getId();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        questionLabel = new javax.swing.JLabel("<html><body style='width: 220px'>" + question.getText() + "</html>");
        jPanel1 = new javax.swing.JPanel();
        recording = new javax.swing.JLabel();
        recording_msg1 = new javax.swing.JLabel();
        recording_msg2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        play = new javax.swing.JLabel();
        stop = new javax.swing.JLabel();

        questionLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        recording.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/standardsheet/client/resources/StopRed.png"))); // NOI18N
        recording.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                recordingMouseClicked(evt);
            }
        });

        recording_msg1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        recording_msg1.setText(START_MSG);

        recording_msg2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        recording_msg2.setText(MAXLENGTH
        );

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Listen : ");

        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/standardsheet/client/resources/PlayDisabled-32.png"))); // NOI18N
        play.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playMouseClicked(evt);
            }
        });

        stop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/standardsheet/client/resources/Stop1Disabled-32.png"))); // NOI18N
        stop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stopMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(play)
                        .addGap(18, 18, 18)
                        .addComponent(stop))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(recording)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(recording_msg2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(recording_msg1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(recording_msg1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(recording_msg2)
                        .addGap(19, 19, 19))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(recording)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(play)
                    .addComponent(stop, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(questionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(questionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void stopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopMouseClicked
        // TODO add your handling code here:
        if(is_playing) {
            //stop playing answer
            is_playing = false;
            play.setIcon(play_pressed);
            stop.setIcon(stop_disabled);
            clip.stop();
            clip.setFramePosition(0);
        }
    }//GEN-LAST:event_stopMouseClicked

    private void playMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playMouseClicked
        // TODO add your handling code here:
        System.out.println("RecordingQuestionPanel mouseClicked for Playback() -Enter- : "+new Date().getTime());
        AudioInputStream inputStream = null;
        if(recording_done && (!is_playing)) {
            //start playing answer
            is_playing = true;
            play.setIcon(play_disabled);
            stop.setIcon(stop_pressed);
            try {
                System.out.println("RecordingQuestionPanel mouseClicked for Playback() -before downloading file- : "+new Date().getTime());
                ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
                ContentRepository repo = registry.getRepository(manager.getSession());
                ContentCollection node = (ContentCollection) repo.getUserRoot().getParent().getChild(username);
                ContentCollection audiosheetNode = (ContentCollection) node.getChild("audiosheet");
                ContentCollection sheetIdNode = null;
                ContentResource file = null;
                if(playUntitled) {
                    sheetIdNode = (ContentCollection) audiosheetNode.getChild("temp");
                    file = (ContentResource) sheetIdNode.getChild(untitledFileName+".au");
                } else {
                    sheetIdNode = (ContentCollection) audiosheetNode.getChild(sheet.getId());
                    file = (ContentResource) sheetIdNode.getChild(fname.split("/")[2]+".au");
                }
                

                inputStream = AudioSystem
                .getAudioInputStream(file.getURL());
                System.out.println("RecordingQuestionPanel mouseClicked for Playback() -after downloading file- : "+new Date().getTime());
                clip  = AudioSystem.getClip();
                clip.open(inputStream);
                clip.addLineListener(new RecordingClipListener(clip));
                clip.start();
            } catch(Exception e1) {
                e1.printStackTrace();
            }
            System.out.println("RecordingQuestionPanel mouseClicked for Playback() -Exit- : "+new Date().getTime());
        }
    }//GEN-LAST:event_playMouseClicked

    private void recordingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_recordingMouseClicked
        // TODO add your handling code here:
        if(recording_status.equals("stopped")){
            startRecording();
        } else {
            //stop timer
            getGlobalTimer().stop();
            stopRecording();
        }
    }//GEN-LAST:event_recordingMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel play;
    private javax.swing.JLabel questionLabel;
    private javax.swing.JLabel recording;
    private javax.swing.JLabel recording_msg1;
    private javax.swing.JLabel recording_msg2;
    private javax.swing.JLabel stop;
    // End of variables declaration//GEN-END:variables

    public Collection<JComponent> getJComponents(int width) {
        
        out = new ArrayList<JComponent>(1);
        if(!userRole.equals("student")){
           recording.setVisible(false);
           recording_msg1.setVisible(false);
           recording_msg2.setVisible(false);
           resize(260);
       }
        out.add(this);
        return out;
    }

    public StandardAnswer getAnswer() {
        //for playing answer in result page
        /*String abc = "<form id='aform-"+username+"-"+question.getId()+"' runat='server' style='height: 18px;width:100px'>"+
                    "<input type = 'button' name='"+username+"-"+question.getId()+"' onclick ='PlaySound1(this.name)' value = 'Play' />"+
                    "<input type='hidden' value='"+username+"/audiosheet"+fname+"' id='afilename-"+username+"-"+question.getId()+"' /></form>"+   
                    "<div id='adiv-"+username+"-"+question.getId()+"'></div>";
        */
        if (!recording_done) {
            return null;
        }
        
        String abc = "<audio controls='controls'>"+
                             "<source src='http://"+manager.getSession().getServerNameAndPort()+"/"
                + "webdav/content/users/"+username+"/audiosheet/"
                + "/"+fname+".wav' type='audio/wav' />"+
                            "<embed height='70px' src='http://"+manager.getSession().getServerNameAndPort()+"/"
                + "webdav/content/users/"+username+"/audiosheet"
                + "/"+fname+".wav' autostart='false'></embed></audio>";
        StandardAnswer sa =  new StandardAnswer(question, abc);
        
        HashMap<String,String> hm = new HashMap<String, String>();
        hm.put("fname", fname);
        hm.put("username", username);
        sa.setProperties(hm);
        return sa;
    }

    public void renderAnswer(StandardAnswer answer) {
        try {
            if(userRole.equals("guide")) {
                fname = answer.getProperties().get("fname");
                if(fname!=null) {
                    fname = answer.getProperties().get("fname");
                    username = answer.getProperties().get("username");
                    play.setIcon(play_pressed);
                    recording_done = true;
                    playUntitled=true;
                } else {
                    play.setIcon(play_disabled);
                    recording_done = false;
                    playUntitled=false;
                }
            }
            else {
                fname = answer.getProperties().get("fname");
                if(fname!=null) {
                    fname = answer.getProperties().get("fname");
                    username = answer.getProperties().get("username");
                    play.setIcon(play_pressed);
                    recording_done = true;
                    playUntitled=true;
                } else {
                    playUntitled=false;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearAnswer() {
        
    }

    public void setEditable(boolean editable) {
        
    }
    
    private void resize(int width) {
        Font font = getFont();  
        int fontHeight = getFontMetrics(font).getHeight();  
        int stringWidth = getFontMetrics(font).stringWidth(questionLabel.getText());  
        int linesCount = (int) Math.floor(stringWidth / width);  
        linesCount = Math.max(1, linesCount + 1);  
        setPreferredSize(new Dimension(width, 130));
    }
    
    public void startRecording() {
        System.out.println("RecordingQuestionPanel mouseClicked() -Entered- : "+new Date().getTime());
        int flag = 0;
        callID = String.valueOf(Math.random()*100000);
        if(recording_done==true) {
            //confirm dialog for replacing recorded answer
            int answer = JOptionPane.showConfirmDialog(out.get(0).getTopLevelAncestor()
                    , "Replace recording?","Replace Recording",JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION) {
                flag = 1;
            } else {

            }
        } else {
            flag = 1;
        }
        if(flag==1) {
            //start recording
            recording.setIcon(stop_recording);
            recording_msg1.setText(RECORDING);
            recording_msg2.setText(STOP_MSG);

            try {
                //get avatar's current position
                ViewManager vm=ClientContextJME.getViewManager();
                final Vector3f v = vm.getPrimaryViewCell().getWorldTransform().getTranslation(null);

                if(fname==null) {
                    fname = "/"+sheet.getId()+"/"+sheet.getName()+"-"+username+"-"+new Date().getTime();

                }
                //send message to server for starting recorder
                audioConnection = AudioRecordingManager.getAudioRecordingManager().textChatConnection;
                //create directory if not exist
                ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
                ContentRepository repo = registry.getRepository(manager.getSession());
                ContentCollection node = (ContentCollection) repo.getUserRoot().getParent().getChild(manager.getUsername());
                ContentCollection audiosheetNode = (ContentCollection) node.getChild("audiosheet");
                if(audiosheetNode==null) {
                    audiosheetNode = (ContentCollection) node.createChild("audiosheet", ContentNode.Type.COLLECTION);
                }
                ContentCollection temp = (ContentCollection) audiosheetNode.getChild("temp");
                if(temp==null) {
                    temp = (ContentCollection) audiosheetNode.createChild("temp", ContentNode.Type.COLLECTION);
                }
                AudioInputStream inputStream = null;

                System.out.println("RecordingQuestionPanel mouseClicked() -before downloading start tone file- : "+new Date().getTime());
                URL url = AudioQuestionPanel.class.getResource("resources/start_tone.au");
                inputStream = AudioSystem
                        .getAudioInputStream(url);

                System.out.println("RecordingQuestionPanel mouseClicked() -after downloading start tone file- : "+new Date().getTime());
                final Clip clip  = AudioSystem.getClip();
                clip.open(inputStream);
                final long len = clip.getFrameLength();
                clip.addLineListener(new LineListener() {
                    public void update(LineEvent event) {
                        if(!clip.isRunning()) {
                            //if(clip.getFramePosition()>=len){
                            System.out.println("RecordingQuestionPanel mouseClicked() -Before Sending Msg to server- : "+new Date().getTime());
                            audioConnection.sendTextMessage("start",username,"hello",true,false,untitledFileName,v,callID);

                            //start timer
                            Timer timer =null;
                            TimerListener tl = new TimerListener();
                            timer  = new Timer(1000,tl);
                            setGlobalTimer(timer);
                            tl.setTimer(timer);
                            timer.start();
                            System.out.println("RecordingQuestionPanel mouseClicked() -After Sending Msg to server- : "+new Date().getTime());
                            //}
                        }
                    }
                });       
                clip.start();
                recording_status="started";
            } catch (Exception ex) {
                Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("RecordingQuestionPanel mouseClicked() -Exit- : "+new Date().getTime());
    }
    
    public void stopRecording() {
        try {
            //stop recording
            recording.setIcon(start_recording);
            recording_msg1.setText(START_MSG);
            recording_msg2.setText(MAXLENGTH);
            play.setIcon(play_pressed);
            audioConnection.sendTextMessage("stop","abhi","hello",false,false,fname,null,callID);
            recording_done = true;
            
            AudioInputStream inputStream = null;
            AudioInputStream inputS = null;
            URL url = AudioQuestionPanel.class.getResource("resources/start_tone.au");
            inputS = AudioSystem
                 .getAudioInputStream(url);
            final Clip clip  = AudioSystem.getClip();
            clip.open(inputS);
            clip.start();
            submitAnswer = true;
            recording_status="stopped";
            playUntitled=true;
        } catch (LineUnavailableException ex) {
            Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //clip listener for recorded answer 
   class RecordingClipListener implements LineListener {
       Clip clip;
       public RecordingClipListener(Clip clip) {
           this.clip = clip;
       }
       public void update(LineEvent event) {
            if(!clip.isRunning()) {
                is_playing = false;
                    play.setIcon(play_pressed);
                    stop.setIcon(stop_disabled);
                    clip.close();
                    clip.flush();
            }
        }
    }
    
    //Listner for the timer
    class TimerListener extends AbstractAction {
        Timer timer;
        int i=0;
        long time;
        public void setTimer(Timer timer) {
            this.timer = timer;        
            time = Long.parseLong(max_length);
            time = time * 60;    
        }    
        public  Timer getTimer() {
            return timer;    
        }    
        public void actionPerformed(ActionEvent e) {
            i++;
            long diff = time-i;
            if(diff<0){
                try {
                    timer.stop();
                    stopRecording();
                } catch (Exception ex) {
                    Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                long min = diff/60;
                long secs = diff - (60*min);
                String mins = null;
                if(min<10){
                    mins = "0"+String.valueOf(min);
                } else {
                    mins = String.valueOf(min);
                }
                String secss = null;
                if(secs<10){
                    secss = "0"+String.valueOf(secs);
                } else {
                    secss = String.valueOf(secs);
                }
                recording_msg1.setText("Recording...("+mins+" : "+secss+")");
            }
        }
    }
    
    
    public void saveWaveFile() {
        try {
            //save wav file
            if(recording_done) {
                StringBuilder sb = new StringBuilder("http://").append(ISocialManager.INSTANCE.getSession().getServerNameAndPort())
                          .append("/isocial-sheets/isocial-sheets/convertaufile?repository=standardSheetRecording&filename=")
                          .append(URLEncoder.encode(fname.split("/")[2])+"&username="+URLEncoder.encode(username)+"&sheetId="+sheet.getId()+"&qid="+question.getId());
                
                URL url1 = new URL(sb.toString());
                HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                ISocialManager.INSTANCE.getSession().getCredentialManager().secureURLConnection(conn);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            //save wav file
        } catch (Exception ex) {
            Logger.getLogger(RecordingQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void setGlobalTimer(Timer timer) {
        this.globalTimer = timer;
    }
    
    public Timer getGlobalTimer() {
        return globalTimer;
    }
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }
    public Sheet getSheet() {
        return sheet;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public ISocialManager getManager() {
        return manager;
    }

    void setManager(ISocialManager manager) {
        this.manager = manager;
    }
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    public boolean getSubmitAnswer() {
        return submitAnswer;
    }
    public void setSubmitAnswer(boolean ans) {
        this.submitAnswer = ans;
    }
}
