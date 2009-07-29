/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.annotations.client;

import java.awt.Canvas;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDFactory;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.hud.client.HUDCompassLayoutManager;
import org.jdesktop.wonderland.modules.hud.client.WonderlandHUDComponentManager;

/**
 * Provides a means of turning annotations on and off, creates an 'annotations'
 * stub in the cell properties pane of each cell.
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

    /* The menu item for turning on/off annotations globally to add to the menu */
    private JCheckBoxMenuItem viewMI;

    private static ArrayList<ItemListener> viewMIListeners = new ArrayList<ItemListener>();

    private static HUD myHud = null;
    public static final String ANNOTATION_HUD = "annotations";


    private static boolean displayState;

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

      // Create the metadata search menu The menu will be added when our
      // server becomes primary.
      // also create
      viewMI = new JCheckBoxMenuItem("Display Annotations", false);
      viewMI.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            hudCheck();
            if(viewMI.getState()){
              HUDManagerFactory.getHUDManager().setVisible(myHud, true);
            }
            else{
              HUDManagerFactory.getHUDManager().setVisible(myHud, false);
            }

            notifyViewMIItemListeners(e);
            // TODO in the future, after HUD code is updated,
            // will not need to tell components to show/hide their annotations
            // can simply show/hide the whole hud here
            // myHud.setVisible(viewMI.getState());
            
//            // TODO testing
//            PanelConfig pc = new PanelConfig();
//
//              // TODO temp test
//            logger.info("[ANNO PLUGIN] test - adding hud compo");
//            AnnotationPane pa = new AnnotationPane(pc);
//            CellCache cache = ClientContext.getCellCache(getSessionManager().getPrimarySession());
//            Cell c = cache.getCell(new CellID(2));
//            if(c == null){
//              logger.info("[ANNO PLUGIN]couldn't add demo hud compo - cell was null");
//            }
//            else{
//              logger.info("[ANNO PLUGIN]adding demo cell compo");
//              HUDComponent myHudCompo = createHUDComponent(pa, c);
//              HUDComponent mainHudCompo = mainHUD.createComponent(pa, c);
//              Vector3f loc = new Vector3f(1.0f, 1.0f, 1.0f);
//              myHudCompo.setWorldLocation(loc);
//              mainHudCompo.setWorldLocation(loc);
//              addHUDComponent(myHudCompo);
//              myHudCompo.setPreferredLocation(Layout.SOUTHEAST);
//              mainHudCompo.setPreferredLocation(Layout.SOUTHEAST);
//              mainHUD.addComponent(mainHudCompo);
//
//              myHudCompo.setVisible(true);
//              myHudCompo.setWorldVisible(true);
//              mainHudCompo.setVisible(true);
//              mainHudCompo.setWorldVisible(true);
//
//              logger.info("[ANNO PLUGIN]demo compo x:" + myHudCompo.getX() + " y:" + myHudCompo.getY());
//              logger.info("[ANNO PLUGIN]demo compo height:" + myHudCompo.getHeight() + " y:" + myHudCompo.getWidth());
//              logger.info("[ANNO PLUGIN]demo world vis:" + myHudCompo.isWorldVisible() + " vis:" + myHudCompo.isVisible());
//              logger.info("====================== and main hud comp=======================");
//              logger.info("[ANNO PLUGIN]demo compo x:" + mainHudCompo.getX() + " y:" + mainHudCompo.getY());
//              logger.info("[ANNO PLUGIN]demo compo height:" + mainHudCompo.getHeight() + " y:" + mainHudCompo.getWidth());
//              logger.info("[ANNO PLUGIN]demo world vis:" + mainHudCompo.isWorldVisible() + " vis:" + mainHudCompo.isVisible());
//              logger.info("[ANNO PLUGIN] main hud vis:" + mainHUD.isShowing() + " my hud: " + myHud.isShowing());
//              mainHUD.show();
//              myHud.show();
//              logger.info("[ANNO PLUGIN] main hud vis now:" + mainHUD.isShowing());
//            }
//            logger.info("[ANNO PLUGIN]added");
//            logger.info("[ANNO PLUGIN]compos in mine:");
//            java.util.Iterator<HUDComponent> itr = myHud.getComponents();
//            HUDComponent hc;
//            while(itr.hasNext()){
//              hc = itr.next();
//              logger.info("compo: class name:" + hc.getClass().getName());
//            }
//            logger.info("[ANNO PLUGIN]compos in main:");
//            itr = mainHUD.getComponents();
//            while(itr.hasNext()){
//              hc = itr.next();
//              logger.info("compo: class name:" + hc.getClass().getName());
//            }



                // create fps Swing control
//                Chart chart = new Chart("main:");
//                chart.setSampleSize(200);
//                chart.setMaxValue(30);
//                chart.setPreferredSize(new Dimension(200, 34));
//
//                // create HUD control panel
//                HUDComponent fpsComponent = mainHUD.createComponent(chart);
//                fpsComponent.setDecoratable(false);
//                fpsComponent.setPreferredLocation(Layout.NORTHWEST);
//                fpsComponent.setVisible(true);
//                // add HUD control panel to HUD
//                mainHUD.addComponent(fpsComponent);
//
//                chart = new Chart("mine:");
//                chart.setSampleSize(200);
//                chart.setMaxValue(30);
//                chart.setPreferredSize(new Dimension(200, 34));
//
//                // create HUD control panel
//                fpsComponent = myHud.createComponent(chart);
//                fpsComponent.setDecoratable(false);
//                fpsComponent.setPreferredLocation(Layout.NORTHEAST);
//                fpsComponent.setVisible(true);
//                // add HUD control panel to HUD
//                myHud.addComponent(fpsComponent);



          }
      });

      // will this be necessary?
      // create a component manager for the HUD components in this HUD
//        HUDComponentManager compManager = new WonderlandHUDComponentManager();

        // define the layout of HUD components in the Wonderland main HUD
//        compManager.setLayoutManager(new HUDCompassLayoutManager(canvas.getWidth(), canvas.getHeight()));

        // manage the components in the main HUD
//        wonderlandHUD.setComponentManager(compManager);

      super.initialize(loginInfo);



                  // TODO testing
//            PanelConfig pc = new PanelConfig();
//
//              // TODO temp test
//            logger.info("[ANNO PLUGIN] test - adding hud compo");
//            AnnotationPane pa = new AnnotationPane(pc);
//            CellCache cache = ClientContext.getCellCache(getSessionManager().getPrimarySession());
//            Cell c = cache.getCell(new CellID(2));
//            if(c == null){
//              logger.info("[ANNO PLUGIN]couldn't add demo hud compo - cell was null");
//            }
//            else{
//              logger.info("[ANNO PLUGIN]adding demo cell compo");
//              HUDComponent myHudCompo = createHUDComponent(pa, c);
//              HUDComponent mainHudCompo = mainHUD.createComponent(pa, c);
//              Vector3f loc = new Vector3f(1.0f, 1.0f, 1.0f);
//              myHudCompo.setWorldLocation(loc);
//              mainHudCompo.setWorldLocation(loc);
//              addHUDComponent(myHudCompo);
//              myHudCompo.setPreferredLocation(Layout.SOUTHEAST);
//              mainHudCompo.setPreferredLocation(Layout.SOUTHEAST);
//              mainHUD.addComponent(mainHudCompo);
//
//              myHudCompo.setVisible(true);
//              myHudCompo.setWorldVisible(true);
//              mainHudCompo.setVisible(true);
//              mainHudCompo.setWorldVisible(true);
//
//              logger.info("[ANNO PLUGIN]demo compo x:" + myHudCompo.getX() + " y:" + myHudCompo.getY());
//              logger.info("[ANNO PLUGIN]demo compo height:" + myHudCompo.getHeight() + " y:" + myHudCompo.getWidth());
//              logger.info("[ANNO PLUGIN]demo world vis:" + myHudCompo.isWorldVisible() + " vis:" + myHudCompo.isVisible());
//              logger.info("====================== and main hud comp=======================");
//              logger.info("[ANNO PLUGIN]demo compo x:" + mainHudCompo.getX() + " y:" + mainHudCompo.getY());
//              logger.info("[ANNO PLUGIN]demo compo height:" + mainHudCompo.getHeight() + " y:" + mainHudCompo.getWidth());
//              logger.info("[ANNO PLUGIN]demo world vis:" + mainHudCompo.isWorldVisible() + " vis:" + mainHudCompo.isVisible());
//              logger.info("[ANNO PLUGIN] main hud vis:" + mainHUD.isShowing() + " my hud: " + myHud.isShowing());
//              mainHUD.show();
//              myHud.show();
//              logger.info("[ANNO PLUGIN] main hud vis now:" + mainHUD.isShowing());
//            }
//            logger.info("[ANNO PLUGIN]added");
//            logger.info("[ANNO PLUGIN]compos in mine:");
//            java.util.Iterator<HUDComponent> itr = myHud.getComponents();
//            HUDComponent hc;
//            while(itr.hasNext()){
//              hc = itr.next();
//              logger.info("compo: class name:" + hc.getClass().getName());
//            }
//            logger.info("[ANNO PLUGIN]compos in main:");
//            itr = mainHUD.getComponents();
//            while(itr.hasNext()){
//              hc = itr.next();
//              logger.info("compo: class name:" + hc.getClass().getName());
//            }
    }

//    public static final ArrayList<Class> getMetadataTypes(){
//        return metaTypes;
//    }

    /**
     * Notification that our server is now the the primary server
     */
    @Override
    protected void activate() {
      logger.info("[ANNO PLUGIN] activated");
      // add menu item
      JmeClientMain.getFrame().addToViewMenu(viewMI, -1);
    }

    @Override
    protected void deactivate() {
      // deactivate
      JmeClientMain.getFrame().removeFromViewMenu(viewMI);
    }

    /**
     * cell component adds listeners here to be notified on changes
     *
     * static so that annotation components can add themselves as necessary
     * @param l
     */
    static boolean addDisplayItemListener(ItemListener l){
      viewMIListeners.add(l);
      return displayState;
    }

    /**
     * cell component adds listeners here to be notified on changes
     *
     * static so that annotation components can remove themselves as necessary
     * @param l
     */
    static void removeDisplayItemListener(ItemListener l){
      viewMIListeners.add(l);
    }

    /**
     * notify all listeners that viewMI has changed
     */
    private void notifyViewMIItemListeners(ItemEvent e){
      displayState = viewMI.getState();
      logger.info("[ANNO PLUGIN] notify listeners");
//      logger.info("[ANNO PLUGIN] main hud vis:" + mainHUD.isShowing() + " my hud: " + myHud.isShowing());
//              mainHUD.show();            
//              logger.info("[ANNO PLUGIN] main hud vis:" + mainHUD.isShowing() + " my hud: " + myHud.isShowing());
      for(ItemListener l: viewMIListeners){
        logger.info("[ANNO PLUGIN] notifying..");
        l.itemStateChanged(e);
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
    HUDComponentManager compManager = new WonderlandHUDComponentManager();

    // define the layout of HUD components
    compManager.setLayoutManager(new HUDCompassLayoutManager(myHud));

    // manage the components in annotations hud, show hud
    myHud.setComponentManager(compManager);

    

    // TODO test
//    mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
//    if(mainHUD == null){
//      logger.severe("MAIN HUD WAS NULL");
//    }
//    else{
//      logger.info("MAIN HUD WAS OK");
//      Rectangle bounds = mainHUD.getBounds();
//      int h = mainHUD.getHeight();
//      int w = mainHUD.getWidth();
//      logger.info("Main hud h/w: " + h + "/" + w);
//      logger.info("Main hud bounds x/y: " + bounds.getX() + "/" + bounds.getY());
//      logger.info("Main hud bounds h/w: " + bounds.getHeight() + "/" + bounds.getWidth());
//    }
//
//    HUD tmp = HUDManagerFactory.getHUDManager().getHUD(ANNOTATION_HUD);
//    if(tmp == null){
//      logger.severe("MY HUD WAS NULL");
//    }
//    else{
//      logger.info("MY HUD WAS OK");
//      Rectangle bounds = myHud.getBounds();
//      int h = myHud.getHeight();
//      int w = myHud.getWidth();
//      logger.info("my hud h/w: " + h + "/" + w);
//      logger.info("my hud bounds x/y: " + bounds.getX() + "/" + bounds.getY());
//      logger.info("my hud bounds h/w: " + bounds.getHeight() + "/" + bounds.getWidth());
//    }

  }


}
