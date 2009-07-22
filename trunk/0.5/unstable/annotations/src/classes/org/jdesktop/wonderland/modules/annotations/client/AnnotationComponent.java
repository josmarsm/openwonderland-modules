/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.annotations.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationPane;
import org.jdesktop.wonderland.modules.annotations.client.display.PanelConfig;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.metadata.client.MetadataComponent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEvent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEventListener;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;

/**
 *
 * @author mabonner
 */
public class AnnotationComponent extends CellComponent 
        implements ItemListener, CacheEventListener{
  private static Logger logger = Logger.getLogger(AnnotationComponent.class.getName());
  /**
   * reference to parent cell's metadata component
   */
  @UsesCellComponent private MetadataComponent metaCompo;
  /**
   * look for annotationpanes of this component
   */
  private PanelConfig panelConfig = new PanelConfig();

  /**
   * maps annotations (from associated MetadataComponent) to hud components
   * displayed in world
   */
  HashMap<Annotation, HUDComponent> annotations = new HashMap<Annotation, HUDComponent>();

  /**
   * global display annotations setting, from plugin
   */
  boolean globalDisplay = false;

  /**
   * this cell's setting
   */
  boolean display = true;

  /**
   * where to place new annotations initially
   */
  private Vector3f baseAnnoLocation;
  
  /**
   * count of annotation components, used to assign different colors to component's panes
   * @param cell
   */
  private static int panelConfigCount = 0;

  // TODO temp test
  //  private AnnotationPane p;
  //  private HUDComponent myHudCompo;

  public AnnotationComponent(Cell cell) {
    super(cell);
    panelConfig = getPanelConfig();
    // TODO temp test
//    p = new AnnotationPane(panelConfig);

    metaCompo = cell.getComponent(MetadataComponent.class);
    if(metaCompo == null){
      logger.severe("[ANNO COMPO] added annotation component to cell without metadata component");
      // TODO in future, add the metadata compo here instead?
    }
    logger.info("[ANNO COMPO] compo created");
    BoundingVolume vol = cell.getWorldBounds();
    Vector3f cellCenter = cell.getWorldBounds().getCenter();
    baseAnnoLocation = new Vector3f(cellCenter);
    // TODO obviously this is not the logic we want in the end
    logger.info("cell center is: "+ cellCenter);
    while(vol.contains(baseAnnoLocation)){
      baseAnnoLocation = baseAnnoLocation.add(1.0f, 3.0f, 1.0f);
    }
    logger.info("base location is: "+ cellCenter);
//
//    myHudCompo = AnnotationPlugin.createHUDComponent(p, cell);
//    myHudCompo.setWorldLocation(startingAnnoLoc);
//    AnnotationPlugin.addHUDComponent(myHudCompo);

    // listen to changes in associated metadata compo's metadata cache
    metaCompo.addCacheListener(this);


    // fetch annotations
    // do this in a separate thread: when a component is added via the the properties
    // pane, it will result in the component being constructed in the clientside
    // darkstar message receiver thread.
    //
    // getAnnotations could result in a sendAndWait call to the server. That
    // results in the darkstarReceiver thread blocking and waiting, since that is
    // the thread running this constructor. If the receiver itself blocks, no
    // messages can be received and the sendAndWait will hang forever.
    //
    // by spinning a new thread here, that new thread will (potentially) block
    // instead of the message receiver.
    new Thread(new Runnable(){

      public void run() {
        getAnnotations();
      }
    }).start();
    
  }

  public void setPanelConfig(PanelConfig pc){
    panelConfig = pc;
  }

  public void displayAnnotations(){
    logger.info("[ANNO COMPO] display annotations!");
    globalDisplay = true;

    // TODO temp testing
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    HUD myHud = HUDManagerFactory.getHUDManager().getHUD(AnnotationPlugin.ANNOTATION_HUD);
    java.util.Iterator<HUDComponent> itr = mainHUD.getComponents();
    HUDComponent hc;
    logger.info("[ANNO COMPO] hc's inside main hud:");
    while(itr.hasNext()){
      hc = itr.next();
      logger.info("compo: class name:" + hc.getClass().getName());
    }
    itr = myHud.getComponents();
    logger.info("[ANNO COMPO] hc's inside my hud:");
    while(itr.hasNext()){
      hc = itr.next();
      logger.info("compo: class name:" + hc.getClass().getName());
    }

    // tell all annotations to show themselves
    for(HUDComponent h:annotations.values()){
      // depends on local display var
      logger.info("[ANNO COMPO] telling anno to show");
      h.setWorldVisible(display);
    }
    
  }

//  dialog.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.2f));
//                dialog.setVisible(false);
//                dialog.setWorldVisible(true);

  public void hideAnnotations(){
    logger.info("[ANNO COMPO] hide annotations!");
    globalDisplay = false;
    // tell all annotations to hide themselves
    for(HUDComponent h:annotations.values()){
      // doesn't matter how local display is set, hide all
      h.setWorldVisible(false);
      h.setVisible(false);
    }
//    myHudCompo.setVisible(false);
//    myHudCompo.setWorldVisible(false);
//    AnnotationPlugin.removeHUDComponent(myHudCompo);
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing){
    super.setStatus(status, increasing);
    if(status == CellStatus.RENDERING && increasing){
      logger.info("[ANNO COMPO] add self as listener!");
      AnnotationPlugin.addDisplayItemListener(this);
    }
    else if(status == CellStatus.ACTIVE && !increasing){
      logger.info("[ANNO COMPO] remove self as listener!");
      AnnotationPlugin.removeDisplayItemListener(this);
    }

  }

  /**
   * adds self as listener to AnnotationPlugin, notified to display or hide annotations
   * @param e
   */
  public void itemStateChanged(ItemEvent e) {
    if(e.getStateChange() == ItemEvent.SELECTED){
      // TODO in the future, this will also check if it matches the
      // currently set filters
      displayAnnotations();
    }
    else{
      hideAnnotations();
    }
  }

  /**
   * Associated MetadataComponent's metadata cache has changed. Update annotations
   * as necessary.
   * @param e
   * @return
   */
  public void cacheEventOccurred(CacheEvent e) {
    // take action only if the change involves a piece of annotation metadata,
    // or if the entire cache was invalidated (which could affect any type of
    // metadata)
    Annotation a = null;
    if(e.getMetadata() == null){
      logger.severe("[ANNO COMPO] cache event: metadata was null!!!");
    }
    if(e.getMetadata() != null && e.getMetadata() instanceof Annotation){
      a = (Annotation) e.getMetadata();
    }
    else if(e.getAction() != ModifyCacheAction.INVALIDATE){
      return;
    }
    switch(e.getAction()){
      case ADD:
            addAnnotation(a);
            break;
        case REMOVE:
            removeAnnotation(a);
            break;
        case MODIFY:
            modifyAnnotation(a);
            break;
        case INVALIDATE:
            getAnnotations();
            break;
    }
  }

  // called from CacheEventListener function (cacheEventOccurred)
  // thus, called as a result of exterior changes to annotation metadata

  /**
   * register a new annotation, create its representative AnnotationPane and
   * display it if necessary.
   * @param a
   */
  private void addAnnotation(Annotation a) {
    logger.info("[ANNO COMPO] add ");
    // create the panel that will represent this annotation
    AnnotationPane p = new AnnotationPane(panelConfig, a, cell);
    p.addSaveButtonListener(new AnnotationSaveListener(a, p));
    p.addViewOnHudButtonListener(new AnnotationViewOnHudListener(a));
    // create hud component, show in world if necessary
    HUDComponent myHudCompo = AnnotationPlugin.createHUDComponent(p, cell);
    myHudCompo.setWorldLocation(baseAnnoLocation);
    annotations.put(a, myHudCompo);

    // add new compo to hud BEFORE you set it visible
    AnnotationPlugin.addHUDComponent(myHudCompo);
    // display if global AND this cell are both set to display
    myHudCompo.setWorldVisible((globalDisplay && display));

    // TODO temporary until edit controls are created!
    // move default location
    float x = baseAnnoLocation.getX();
    float y = baseAnnoLocation.getY();
    float z = baseAnnoLocation.getZ();
    baseAnnoLocation = baseAnnoLocation.add(3.0f, 3.0f, 3.0f);
    
    

  }


  /**
   * remove an annotation
   * @param a
   */
  private void removeAnnotation(Annotation a) {
    logger.info("[ANNO COMPO] remove ");
    HUDComponent hc = annotations.get(a);
    if(hc == null){
      logger.info("[ANNO COMPO] note: trying to remove a non existent annotation" +
              " with id" + a.getID());
      return;
    }
    
    // remove hud component
    AnnotationPlugin.removeHUDComponent(hc);
    annotations.remove(a);
    hc.setWorldVisible(false);
    hc.setVisible(false);
    hc = null;
  }

  /**
   * remove all annotations
   * @param a
   */
  private void removeAllAnnotations() {
    logger.info("[ANNO COMPO] remove ");
    for(Annotation a:annotations.keySet()){
      removeAnnotation(a);
    }
  }

  /**
   * adjust an annotation. Pass in the modified annotation. Matched to annotation
   * in this component via ID.
   * @param a modified annotation
   */
  private void modifyAnnotation(Annotation a) {
    logger.info("[ANNO COMPO] mod ");
    HUDComponent hc = annotations.get(a);
    if(hc == null){
      logger.severe("[ANNO COMPO] error: trying to modify a non existent annotation" +
              " with id" + a.getID());
      return;
    }
    Vector3f oldLoc = hc.getWorldLocation();
    removeAnnotation(a);
    addAnnotation(a);
    // reset to old location
    hc = annotations.get(a);
    hc.setWorldLocation(oldLoc);
  }

  // these functions are called after an annotation is updated within the component (e.g., an
  // annotation pane has been edited).
  // Keeps Metadata informed of the change
  /**
   * Inform Metadata component of an annotation (e.g., a piece of Metadata) removal
   * @param a
   */
  private void removeAnnotationFromMetadata(Annotation a) {
    logger.info("[ANNO COMPO] anno removed ");
    metaCompo.removeMetadata(a);
  }

  /**
   * Inform Metadata component of an annotation (e.g., a piece of Metadata) modification
   * @param a
   */
  private void modifyAnnotationInMetadata(Annotation a) {
    logger.info("[ANNO COMPO] anno mod'd ");
    metaCompo.modifyMetadata(a);
  }

  private void getAnnotations() {
    logger.info("[ANNO COMPO] get annotations (new compo, or cache was invalidated)");
    ArrayList<Annotation> annos = metaCompo.getMetadataOfType(Annotation.class);
    if(annos == null){
      logger.info("[ANNO COMPO] annos was null!");
      return;
    }
    logger.info("[ANNO COMPO] adding " + annos.size() + " annotations");
    for(Annotation a:annos){
      addAnnotation(a);
    }
  }
  
  /**
   * display an annotation on the HUD
   * @param a annotation to show or hide
   * @param b show or hide annotation
   */
  public void setAnnotationHudVisible(Annotation a, boolean b){
    HUDComponent hc = annotations.get(a);
    if(hc == null){
      logger.severe("[ANNO COMPO] error: trying to setAnnotationHudVisible a non-exisiting annotation" +
              "component with id " + a.getID());
    }
    hc.setVisible(b);
    logger.info("hc is now:" + hc);
  }

  /**
   * returns a PanelConfig based on the pased in integer, which should be
   * a count of annotation components. Used to get semi-unique coloration.
   * @param i count of annotation components
   * @return
   */
  private PanelConfig getPanelConfig() {
    PanelConfig pc = null;
    switch(panelConfigCount){
      case 0:
        // default
        logger.info("default pc");
        pc = new PanelConfig();
        break;
      case 1:
        // orange and black
        logger.info("default pc 1");
        pc = new PanelConfig(new Color(230, 150, 65), Color.black);
        break;
      case 2:
        // blue and black
        logger.info("default pc 2");
        pc = new PanelConfig(new Color(150, 175, 210), Color.black);
        break;
      case 3:
        // red and white
        logger.info("default pc 3");
        pc = new PanelConfig(new Color(145, 20, 20), Color.white);
        break;
      case 4:
        pc = new PanelConfig(new Color(250, 250, 165), Color.black);
        break;
      default:
        panelConfigCount = 1;
        pc = new PanelConfig();
    }

    panelConfigCount += 1;
    return pc;
  }



  /**
   * listens for save button presses in an AnnotationPane, updates the associated
   * Annotation and then informs component of update.
   */
  class AnnotationSaveListener implements ActionListener{
    Annotation anno;
    AnnotationPane pane;

    /**
     *
     * @param a the annotation to update
     * @param p the pane to retrive the updated annotation information from
     */
    public AnnotationSaveListener(Annotation a, AnnotationPane p){
      anno = a;
      pane = p;
    }

    public void actionPerformed(ActionEvent e) {
//      anno.setText(pane.getText());
      // don't make any changes here... the metadata component will register
      // changes, then send them back here
      modifyAnnotationInMetadata(anno);
    }
  }

  /**
   * listens for view on hud button presses in an AnnotationPane, adjusts
   * pane's display appropriately
   */
  class AnnotationViewOnHudListener implements ActionListener{
    Annotation anno;
    boolean toggle = false;

    /**
     *
     * @param a the annotation to update
     * @param p the pane to retrive the updated annotation information from
     */
    public AnnotationViewOnHudListener(Annotation a){
      anno = a;
    }

    public void actionPerformed(ActionEvent e) {
      toggle = !toggle;
      setAnnotationHudVisible(anno, toggle);
    }
  }
}
