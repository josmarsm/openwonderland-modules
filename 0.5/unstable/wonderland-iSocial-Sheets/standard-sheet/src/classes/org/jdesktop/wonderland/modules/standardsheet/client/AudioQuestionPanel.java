/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.swing.*;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;

/**
 * Panel that is used for audio question type has a player to play question & a text area to give answer
 * @author nilang
 */
public class AudioQuestionPanel extends JTextArea implements StandardQuestionPanel {
    
    private static final Logger LOGGER = Logger.getLogger(AudioQuestionPanel.class.getName());

    private StandardQuestion question;
    private TextQuestionPanel label;
    private TextQuestionPanel label1;
    private final JPanel p1;
    private int allowedreplays;
    private String fname;
    
    private String status;
    JLabel play_stop;
    private int lines=1;
    private String instructions;
    private boolean instructionsShown = true;
    
    public AudioQuestionPanel(StandardQuestion question) {
        this.question = question;
        this.label = new TextQuestionPanel(question.getText());
        this.allowedreplays = Integer.parseInt(question.getProperties().get("allowedreplays"));
        if(question.getProperties().get("audiolines")!=null && (!question.getProperties().get("audiolines").equals(""))) {
            this.lines = Integer.parseInt(question.getProperties().get("audiolines"));
        }
        this.instructions = question.getProperties().get("audioinstructions");
        this.fname = question.getProperties().get("fname");
        this.label1 = new TextQuestionPanel("("+allowedreplays+" replays remaining)");
        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setAlignmentX(Component.LEFT_ALIGNMENT);
        p1.setPreferredSize(new Dimension(300, 30));
        status="stopped";
    }
    
    public int getId() {
        return question.getId();
    }

    public Collection<JComponent> getJComponents(int width) {
        ArrayList<JComponent> out = new ArrayList<JComponent>();
        
      try{
        URL url = AudioQuestionPanel.class.getResource("resources/play.png");
        Image image =  Toolkit.getDefaultToolkit().createImage(url);
        Image image1 =  Toolkit.getDefaultToolkit()
                    .createImage(AudioQuestionPanel.class.getResource("resources/stop.png"));
        final ImageIcon ii1 = new ImageIcon(image);
        final ImageIcon ii2 = new ImageIcon(image1); 
        play_stop = new JLabel(ii1);
        this.p1.add(play_stop, BorderLayout.CENTER );
        this.p1.add(label1.getJComponents(100).iterator().next());
        label1.setAlignmentX(Component.LEFT_ALIGNMENT);

        //Playing .au file----Generate Clip of Question
        AudioInputStream inputStream = null;
        final Clip clip;
        inputStream = AudioSystem
            .getAudioInputStream(AssetUtils.getAssetURL("wlcontent://groups/users/audiosheet/"+fname+".au"));
        clip = AudioSystem.getClip();
        clip.open(inputStream);
        final long len = clip.getFrameLength();


        clip.addLineListener(new LineListener() {

            public void update(LineEvent event) {
                int flag = 0;
                if(flag==0){

                if(!clip.isRunning()) {

                    //if(clip.getFramePosition()>=len) {
                        try {
                            if(allowedreplays==0) {/*no of replays over*/
                                label1.setText("<html><body><font style='font-size: 10px;font-family: Tahoma'><b>("+allowedreplays+" replays remaining)</b></font></body></html>");
                                label1.validate();
                                label1.repaint();
                                p1.validate();
                                p1.repaint();
                            }
                            else {
                                //restart clip
                                label1.setText("<html><body><font style='font-size: 10px;font-family: Tahoma'><b>("+allowedreplays+" replays remaining)</b></font></body></html>");
                                label1.validate();
                                label1.repaint();
                                p1.validate();
                                p1.repaint();
                                clip.setFramePosition(0);
                                clip.stop();
                                flag=1;
                            }
                            status="stopped";
                            play_stop.setIcon(ii1);
                            play_stop.validate();
                            play_stop.repaint();
                            p1.validate();
                            p1.repaint();
                        } catch (Exception ex) {
                            Logger.getLogger(AudioQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    //}
                } else {

                }
                } else {
                    flag=0;
                }
            }
        });

        //Handling events for play or stop question
        play_stop.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {

                if(status.equals("stopped")) {
                    //start clip
                    if(allowedreplays!=0) {
                        status="playing";
                        allowedreplays = allowedreplays-1;
                        play_stop.setIcon(ii2);
                        play_stop.validate();
                        play_stop.repaint();
                        p1.validate();
                        p1.repaint();
                        clip.start();
                    }
                } else {
                    status="stopped";
                    if(clip.isRunning()) {
                        //stop clip
                        play_stop.setIcon(ii1);
                        play_stop.validate();
                        play_stop.repaint();
                        p1.validate();
                        p1.repaint();
                        clip.stop();
                    } else {
                        play_stop.setIcon(ii1);
                        play_stop.validate();
                        play_stop.repaint();
                        p1.validate();
                        p1.repaint();
                        clip.setFramePosition(0);
                    }
                }
            }
            public void mousePressed(MouseEvent e){}
            public void mouseReleased(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        });
        
        //Text Area for Answer
        if(lines!=0) {
            setRows(lines);
            setLineWrap(true);
            setWrapStyleWord(true);
            showInstructions();
        } else {
            this.setVisible(false);
        }
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent fe) {
                if (instructionsShown) {
                    hideInstructions();
                }
            }

            public void focusLost(FocusEvent fe) {
                if (getText().length() == 0) {
                    showInstructions();
                }
            }            
        });
        
        //Adding all Components in array
        out.addAll(label.getJComponents(width));
        out.add(p1);
        out.add(this);
        
      } catch(Exception ex) { 
          Logger.getLogger(AudioQuestionPanel.class.getName()).log(Level.SEVERE, null, ex);
      }
        
      return out;
    }

    public StandardAnswer getAnswer() {
        //if (instructionsShown || getText().length() == 0) {
        //    return null;
        //}
        String txt = getText();
        if(instructionsShown) {
            txt = "";
        }
        StandardAnswer sa = new StandardAnswer(question,txt);
        HashMap hm = new HashMap();
        hm.put("host", ISocialManager.INSTANCE.getSession().getServerNameAndPort());
        sa.setProperties(hm);
        return sa;
    }

    public void renderAnswer(StandardAnswer answer) {
        hideInstructions();
        setText(answer.getValueString());
    }
    public void clearAnswer() {
        setText("");
        showInstructions();
    }

    private void showInstructions() {
        setForeground(Color.GRAY);
        setText(instructions);
        instructionsShown = true;
    }
    
    private void hideInstructions() {
        setForeground(Color.BLACK);
        setText("");
        instructionsShown = false;
    }
    
    public int getAllowedreplays() {
        return allowedreplays;
    }
    
}
