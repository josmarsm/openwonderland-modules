/*
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.annotations.client;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDFactory;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationEntity.DisplayMode;
import org.jdesktop.wonderland.modules.annotations.client.display.FontSizePane;
import org.jdesktop.wonderland.modules.hud.client.HUDCompassLayoutManager;
import org.jdesktop.wonderland.modules.hud.client.WonderlandHUDComponentManager;

/**
 * Provides a menu to globally change annotation display mode, creates an
 * 'annotations' stub in the cell properties pane of each cell.
 *
 * Controls annotation HUD.
 *
 * In the future, could provide a way to filter annotations
 * @author mabonner
 */
@Plugin
public class AnnotationPlugin extends BaseClientPlugin
{
  private static Logger logger = Logger.getLogger(AnnotationPlugin.class.getName());
  //  private static HUD mainHUD;



//    private static ArrayList<Class> metaTypes = new ArrayList<Class>();

    // global annotation display mode menu items
    /** menu item to hide all annotations */
    JMenu displayMenu;

    /** global display setting of annotations */
    private static DisplayMode globalDisplayMode = DisplayMode.HIDDEN;

    /** global tool for adjusting display size of fonts in Annotations in world */
    JMenuItem fontSize;

    /** the font size adjusting tool */
    static FontSizePane fontPane;

    /** hud compo to hold the font panel */
    HUDComponent fontHudCompo;

    /** global setting for font size */
    static float globalFontSizeModifier;

    private static ArrayList<AnnotationComponent> componentListeners = new ArrayList<AnnotationComponent>();

    private static HUD myHud = null;
    public static final String ANNOTATION_HUD = "annotations";



    /**
     * Sets up plugin. Adds plugin as a listener to its own 'view annotations'
     * menu item, so it can in turn notify annotation components. This indirection
     * is necessary to create a static 'addDisplayItemListener' method, reachable from
     * components.
     *
     * Also sets up a HUD for viewing the components.
     * @param loginInfo
     */
    @Override
    public void initialize(final ServerSessionManager loginInfo) {
      logger.info("[ANNO PLUGIN] initialize");

      // create new 'font size' controller
      fontSize = new JMenuItem("Annotation Size");
      fontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              hudCheck();
              // set the hud visible
              HUDManagerFactory.getHUDManager().setVisible(myHud, true);

              // display the component
              logger.info("[ANNO PLUGIN] display new font hud compo");
              fontHudCompo.setVisible(true);
            }
        });

      // Create the 'global set annotation view mode' menu
      displayMenu = new JMenu("Annotations");

      JRadioButtonMenuItem button;    
      // add menu items for every possible display mode
      ButtonGroup group = new ButtonGroup();
      for(DisplayMode dm:DisplayMode.values()){
        // build a button
        button = new JRadioButtonMenuItem(dm.toString(), false);
        button.setName(dm.toString());
        // if this is the "hidden" button, set it to selected
        if(button.getName().equals(DisplayMode.HIDDEN.toString())){
          button.setSelected(true);
        }
        group.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();
              hudCheck();
              // set the hud hidden if this is the hidden button
              if(button.getName().equals(DisplayMode.HIDDEN.toString())){
                HUDManagerFactory.getHUDManager().setVisible(myHud, false);
              }
              else{
                HUDManagerFactory.getHUDManager().setVisible(myHud, true);
              }

              // get the corresponding DisplayMode for this button
              globalDisplayMode = DisplayMode.stringToRawValue(button.getName());
              logger.info("[anno plug] b name is: " + button.getName() + " dm is:" + globalDisplayMode);
              notifyComponentsDisplayChanged(globalDisplayMode);
            }
        });

        // add the button to the display menu
        displayMenu.add(button);
      }

      


      super.initialize(loginInfo);
    }


    /**
     * Notification that our server is now the the primary server
     */
    @Override
    protected void activate() {
      logger.info("[ANNO PLUGIN] activated");
      // add the display menu item
      JmeClientMain.getFrame().addToWindowMenu(fontSize, -1);
      JmeClientMain.getFrame().addToViewMenu(displayMenu, -1);
      // create hud component for font-size elt
      hudCheck();
      fontPane = new FontSizePane();
      globalFontSizeModifier = fontPane.getFontSize();
      fontPane.addFontSliderListener(new fontPanelListener());
      fontHudCompo = myHud.createComponent(fontPane);
      fontHudCompo.setName("Annotation Font Size");
      myHud.addComponent(fontHudCompo);
      fontHudCompo.setPreferredLocation(Layout.SOUTHEAST);
      fontHudCompo.setTransparency(0.3f);
      fontHudCompo.setVisible(false);
      fontHudCompo.setWorldVisible(false);
      
    }

    @Override
    protected void deactivate() {
      // deactivate
      // remove the display menu item
      JmeClientMain.getFrame().removeFromViewMenu(displayMenu);
    }

    /**
     * AnnotationComponents listen for new global display modes and font sizes.
     *
     * static so that annotation components can add themselves as necessary
     * @param l
     */
    static void addComponentListener(AnnotationComponent ac){
      componentListeners.add(ac);
    }

    static DisplayMode getDisplayMode(){
      return globalDisplayMode;
    }

    static float getFontSizeModifier(){
      return globalFontSizeModifier;
    }

    /**
     * Unregister an AnnotationComponent from being notified of new global
     * display mode changes
     *
     * static so that annotation components can remove themselves as necessary
     * @param l
     */
    static void removeDisplayListener(AnnotationComponent ac){
      componentListeners.remove(ac);
    }

    /**
     * notify all components of the new global display policy
     * @param newMode the new display mode setting
     */
    private void notifyComponentsDisplayChanged(DisplayMode newMode){
      logger.info("[ANNO PLUGIN] notify listeners of display change, new state is: " + newMode );
      for(AnnotationComponent ac: componentListeners){
        logger.info("[ANNO PLUGIN] notifying..");
        ac.setLocalDisplayMode(newMode);
      }
    }

    /**
     * creates a hud component out of the passed in swing component
     * using the plugin's hud
     * @param component the component to create a HUDComponent out of
     * @param cell the cell to associate this component with
     * @return the created HUDComponent
     */
    static HUDComponent createHUDComponent(JComponent compo, Cell cell) {
      hudCheck();
      logger.info("[ANNO PLUGIN] create hud compo for cell " + cell.getCellID());
      return myHud.createComponent(compo, cell);
    }

    /**
     * remove a hud component from the plugin's hud
     * @param component the HUDComponent to remove
     */
    static void removeHUDComponent(HUDComponent c) {
      hudCheck();
      logger.info("[ANNO PLUGIN] remove hud compo..");
      myHud.removeComponent(c);
//      mainHUD.removeComponent(c);
    }

    /**
     * add a hud component to the plugin's hud
     * @param component the HUDComponent to add
     */
    static void addHUDComponent(HUDComponent c) {
      hudCheck();
      logger.info("[ANNO PLUGIN] add hud compo..");
      myHud.addComponent(c);
//      mainHUD.addComponent(c);
    }


  // TODO temporary
  /**
   * Check that the annotation HUD is prepared, create it if not
   */
  private static void hudCheck() {
    if(myHud != null){
      return;
    }
    Canvas canvas = JmeClientMain.getFrame().getCanvas();
    logger.fine("[ANNO PLUGIN] creating Anno HUD: " + canvas.getWidth() + "x" + canvas.getHeight() +
              " at " + canvas.getX() + ", " + canvas.getY());

    // create hud, set name
    myHud = HUDFactory.createHUD(canvas.getSize());
    myHud.setName(ANNOTATION_HUD);

    // add to main manager
    HUDManagerFactory.getHUDManager().addHUD(myHud);

    // create a component manager for the HUD components in this HUD
    HUDComponentManager compManager = new WonderlandHUDComponentManager(myHud);

    // define the layout of HUD components
    compManager.setLayoutManager(new HUDCompassLayoutManager(myHud));

    // manage the components in annotations hud, show hud
    myHud.addEventListener(compManager);
  }

  private class fontPanelListener implements ChangeListener{

    public void stateChanged(ChangeEvent e) {
      // font changed, inform all component listeners to resize themselves
      for(AnnotationComponent ac: componentListeners){
        logger.info("[ANNO PLUGIN] notifying..");
        ac.setLocalFontSizeModifier(fontPane.getFontSize());
      }
    }

  }

}
