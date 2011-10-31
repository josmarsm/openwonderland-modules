
package org.jdesktop.wonderland.modules.ezscript.client.hud;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class ShowQuestionBoxMethod implements ReturnableScriptMethodSPI {

    private String title = null;
    private String questionText = null;
    private boolean fail = false;
    private String answer = "no-answer";
    public String getFunctionName() {
        return "ShowQuestionBox";
    }

    public void setArguments(Object[] args) {
        //reset value from any previous run.       
        answer = "no-answer";
        
        //acquire arguments as normal
        title =(String)args[0];
        questionText = (String)args[1];

    }

    public String getDescription() {
        return "--usage: var value = ShowQuestionBox('title', 'question?');";
    }

    public String getCategory() {
        return "HUD";
    }

    public void run() {
        if(fail)
            return;
        
//         final QuestionBoxPanel panel = new QuestionBoxPanel();
//        SwingUtilities.invokeLater(new Runnable() { 
//            public void run() {
               
                
                answer = (String)JOptionPane.showInputDialog(ClientContextJME.getClientMain().getFrame().getCanvas3DPanel(),
                                                            questionText,
                                                            title,
                                                            JOptionPane.PLAIN_MESSAGE,
                                                            null, //no icon
                                                            null, //no combo box
                                                            "0"); //pre-existing value

//                HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
//                HUDComponent c = main.createComponent(panel);
//                panel.setHUDComponent(c);
//                panel.setQuestionText(questionText);
//                panel.setTitleText(title);
//                panel.setCallback(callback);
//                
//                c.setPreferredLocation(Layout.CENTER);
//                c.setName(title);
//                c.setDecoratable(true);
//                main.addComponent(c);
//                
//                c.setVisible(true);                
//            }
//        });
    }

    public Object returns() {
        return answer;
    }
    
}
