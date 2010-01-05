/* This code was developed with funding from the project "España Virtual"
*
* The contents of this file are subject to the GNU General Public
* License, Version 2 (the "License"); you may not use this file
* except in compliance with the License. A copy of the License is
* available at http://www.opensource.org/licenses/gpl-license.php.
*
* "España Virtual es un proyecto de I+D, subvencionado por el CDTI dentro del
* programa Ingenio 2010, orientado a la definición de la arquitectura,
* protocolos y estándares del futuro Internet 3D, con un foco especial en lo
* relativo a visualización 3D, inmersión en mundos virtuales, interacción
* entre usuarios y a la introducción de aspectos semánticos, sin dejar de lado
* el estudio y maduración de las tecnologías para el procesamiento masivo y
* almacenamiento de datos geográficos.
*
* Con una duración de cuatro años, el proyecto está liderado por DEIMOS Space
* y cuenta con la participación del Centro Nacional de Información Geográfica
* (IGN/CNIG), Grid Systems, Indra Espacio, GeoVirtual, Androme Ibérica,
* GeoSpatiumLab, DNX y una decena de prestigiosos centros de investigación y
* universidades nacionales."
*/


package org.jdesktop.wonderland.modules.wonderOSC.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import  org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.modules.avatarbase.client.imi.ImiAvatarConfigManager;
import org.jdesktop.wonderland.modules.wonderOSC.client.*;

/**
 *
 * @author Sergio Galan sgalan@inv.it.uc3m.es Universidad Carlos III de Madrid
 */
public class wonderOSCPlugin extends BaseClientPlugin implements ViewManagerListener{

    private WeakReference<wonderOSCPanel> wonderOSCPanelRef = null;
    JMenuItem ExtCtrlFrameMI;
    AvatarImiJME avatar;
    wonderOSCPanel ctrlPanel;
    private SessionLifecycleListener lifecycleListener;
    private ServerSessionManager loginManager;



    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
        CellRenderer rend = newViewCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (!(rend instanceof AvatarImiJME)) {
            return;
        }
        // set the current avatar
        avatar = (AvatarImiJME) rend;
        if (wonderOSCPanelRef == null || wonderOSCPanelRef.get() == null) {
            // Do nothing
        } else {
            wonderOSCPanelRef.get().setAvatarCharactar(avatar.getAvatarCharacter());
        }
      //  ImiAvatarConfigManager.getImiAvatarConfigManager().setViewCell(newViewCell);
        
        JmeClientMain.getFrame().addToWindowMenu(ExtCtrlFrameMI, 2);
    }



    public void cleanup(){
        loginManager.removeLifecycleListener(lifecycleListener);
        super.cleanup();
        return;
    }

   @Override
   public void initialize(ServerSessionManager loginManager) {

        this.loginManager = loginManager;
        


        //////////////////////////

      //  primaryViewCellChanged(null, ClientContextJME.getViewManager().getPrimaryViewCell());
        ExtCtrlFrameMI = new JMenuItem("OSC control");
        ExtCtrlFrameMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ctrlPanel==null || wonderOSCPanelRef.get()==null) {
                    ctrlPanel = new wonderOSCPanel();
                    ctrlPanel.setAvatarCharactar(avatar.getAvatarCharacter());
                    JFrame f = new JFrame("OSC control");
                    f.getContentPane().add(ctrlPanel);
                    f.pack();
                    f.setVisible(true);
                    f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    wonderOSCPanelRef = new WeakReference(ctrlPanel);
                } else {
                    SwingUtilities.getRoot(wonderOSCPanelRef.get().getParent()).setVisible(true);
                }
            }//end action Performed
        });
        lifecycleListener = new SessionLifecycleListener() {

            public void sessionCreated(WonderlandSession session) {
            }

            public void primarySession(WonderlandSession session) {
                // We need a primary session, so wait for it
                try{
                ImiAvatarConfigManager.getImiAvatarConfigManager().addServerAndSync(wonderOSCPlugin.this.loginManager);
                }catch(Exception e){}
            }
        };
//        loginManager.addLifecycleListener(lifecycleListener);
//        if (loginManager.getPrimarySession() != null) {
//            setPrimarySession(loginManager.getPrimarySession());
//        }

        // Add the OSC control menu item to the "Window" menu
     //   ClientContextJME.getAvatarRenderManager().registerRenderer(loginManager, AvatarImiJME.class, AvatarControls.class);

        super.initialize(loginManager);

   }

    protected void activate(){

        ClientContextJME.getViewManager().addViewManagerListener(this);
        if (ClientContextJME.getViewManager().getPrimaryViewCell() != null) {
            // fake a view cell changed event
            primaryViewCellChanged(null, ClientContextJME.getViewManager().getPrimaryViewCell());
        }



    }

      protected void deactivate(){

            JmeClientMain.getFrame().removeFromWindowMenu(ExtCtrlFrameMI);
            ClientContextJME.getViewManager().removeViewManagerListener(this);

      }
  
    
}
