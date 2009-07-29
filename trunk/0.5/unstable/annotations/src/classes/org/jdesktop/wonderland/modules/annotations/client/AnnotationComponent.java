/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.annotations.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager.ContextMenuListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationEntity;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationPane;
import org.jdesktop.wonderland.modules.annotations.client.display.PanelConfig;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.hud.client.HUDComponent2D;
import org.jdesktop.wonderland.modules.metadata.client.MetadataComponent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEvent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEventListener;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
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
  @UsesCellComponent
  private MetadataComponent metaCompo;

  /**
   * Adjust the cell's context menu as necessary to display it for annotations
   */
  @UsesCellComponent ContextMenuComponent contextMenuCompo;
  
  /**
   * look for annotationpanes of this component
   */
  private PanelConfig panelConfig = new PanelConfig();

  /**
   * maps annotations (from associated MetadataComponent) to hud components
   */
  HashMap<MetadataID, HUDComponent> hudComponents = new HashMap<MetadataID, HUDComponent>();

  /**
   * maps annotations (from associated MetadataComponent) to AnnotationNodes displayed in world
   */
  HashMap<MetadataID, AnnotationEntity> nodes = new HashMap<MetadataID, AnnotationEntity>();
  
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
  private static int panelConfigCount = 1;

  // TODO temp test
  //  private AnnotationPane p;
  //  private HUDComponent myHudCompo;

  /**
   * The root of this component's parent cell, used to add AnnotationNodes
   */
  private Node sceneRoot;
  private CtxListener ctxListener;

  /**
   * Creates the edit item for annotations' context menus
   */
  EditAnnotationContextMenuFactory editCMF = new EditAnnotationContextMenuFactory();

  /**
   * Creates the move item for annotations' context menus
   *
   */
  MoveAnnotationContextMenuFactory moveCMF = new MoveAnnotationContextMenuFactory();

  /**
   * Creates the delete item for annotations' context menus
   *
   */
  DeleteAnnotationContextMenuFactory deleteCMF = new DeleteAnnotationContextMenuFactory();


  /**
   *
   * @param cell
   */
  public AnnotationComponent(Cell cell) {
    super(cell);


    


    panelConfig = getPanelConfig();
    // TODO temp test
//    p = new AnnotationPane(panelConfig);

    logger.info("[ANNO COMPO] compo created");

    
    
  }

  public void setPanelConfig(PanelConfig pc){
    panelConfig = pc;
  }

  public void displayAnnotations(){
    logger.info("[ANNO COMPO] display annotations!");
    globalDisplay = true;

    // TODO temp testing
//    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    HUD myHud = HUDManagerFactory.getHUDManager().getHUD(AnnotationPlugin.ANNOTATION_HUD);
//    java.util.Iterator<HUDComponent> itr = mainHUD.getComponents();
    HUDComponent hc;
    // debugging
//    logger.info("[ANNO COMPO] hc's inside main hud:");
//    while(itr.hasNext()){
//      hc = itr.next();
//      logger.info("compo: class name:" + hc.getClass().getName());
//    }
    // debugging
    java.util.Iterator<HUDComponent> itr = myHud.getComponents();
    logger.info("[ANNO COMPO] hc's inside my hud:");
    while(itr.hasNext()){
      hc = itr.next();
      logger.info("compo: class name:" + hc.getClass().getName());
    }

    // tell all annotations to show themselves if local display = true
    if(display){
      for(MetadataID mid:hudComponents.keySet()){
        displayAnnotationInWorld(mid);
      }
    }
    
  }

//  dialog.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.2f));
//                dialog.setVisible(false);
//                dialog.setWorldVisible(true);

  public void hideAnnotations(){
    logger.info("[ANNO COMPO] hide annotations!");
    globalDisplay = false;
    // tell all annotations to hide themselves
    for(MetadataID mid:hudComponents.keySet()){
      hideAnnotation(mid);
    }
//    myHudCompo.setVisible(false);
//    myHudCompo.setWorldVisible(false);
//    AnnotationPlugin.removeHUDComponent(myHudCompo);
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing){
    super.setStatus(status, increasing);
    if(status == CellStatus.RENDERING && increasing){
      globalDisplay = AnnotationPlugin.addDisplayItemListener(this);
      logger.info("[ANNO COMPO] add self as listener! global display val is: " + globalDisplay);
      // build annotations
      rebuildAnnotations();
      // listen to ctx menu events
      ctxListener = new CtxListener();
      ContextMenuManager.getContextMenuManager().addContextMenuListener(ctxListener);

      // get root node of cell
      CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
      RenderComponent cellRC = (RenderComponent)renderer.getEntity().getComponent(RenderComponent.class);
      sceneRoot = cellRC.getSceneRoot();

      // set metadata component reference
      metaCompo = cell.getComponent(MetadataComponent.class);
      if(metaCompo == null){
        logger.severe("[ANNO COMPO] added annotation component to cell without metadata component");
        // TODO in future, add the metadata compo here instead?
      }

      // determine a decent starting position for metadata objects
      BoundingVolume vol = cell.getWorldBounds();
      Vector3f cellCenter = cell.getWorldBounds().getCenter();
      baseAnnoLocation = new Vector3f(cellCenter);
  //    baseAnnoLocation = new Vector3f(2.0f,2.0f,2.0f);
      // TODO obviously this is not the logic we want in the end
      logger.info("cell center is: "+ cellCenter);
      logger.info("cell vol is: "+ vol);

      // TODO this should be working...
      while(vol.contains(baseAnnoLocation)){
        logger.info("[ANNO COMPO] moving base locations");
        baseAnnoLocation = baseAnnoLocation.add(2.0f, 2.0f, 2.0f);
      }

      baseAnnoLocation = baseAnnoLocation.add(1.0f, 1.0f, 1.0f);
      logger.info("base location is: "+ baseAnnoLocation);
  //
  //    myHudCompo = AnnotationPlugin.createHUDComponent(p, cell);
  //    myHudCompo.setWorldLocation(startingAnnoLoc);
  //    AnnotationPlugin.addHUDComponent(myHudCompo);

      // listen to changes in associated metadata compo's metadata cache
      metaCompo.addCacheListener(this);

      // prepare annotations
      rebuildAnnotations();
    }
    else if(status == CellStatus.ACTIVE && !increasing){
      logger.info("[ANNO COMPO] remove self as listener!");
      AnnotationPlugin.removeDisplayItemListener(this);
    }
    else if(status == CellStatus.DISK && !increasing){
      logger.info("[ANNO COMPO] clean up annotations");
      for(AnnotationEntity node:nodes.values()){
        node.dispose();
      }
      nodes.clear();
      for(HUDComponent hc:hudComponents.values()){
        hc.setVisible(false);
        hc.setWorldVisible(false);
        hc = null;
      }
      hudComponents.clear();
      // stop listening to ctx menu events
      ContextMenuManager.getContextMenuManager().removeContextMenuListener(ctxListener);
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
    logger.info("[ANNO COMPO] cache event occurred");
//    if(e.getMetadata() == null){
//      logger.info("[ANNO COMPO] cache event: metadata was null!!!");
//    }
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
            rebuildAnnotations();
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
    logger.info("[ANNO COMPO] adding annotation to cell " + cell.getCellID());
    if(hudComponents.get(a.getID()) != null){
      logger.info("[ANNO COMPO] annotation already registered and has non-null hud compo!");
//      logger.info("[ANNO COMPO] its hud compo is visible in the world:" + hudComponents.get(a.getID()));
      return;
    }

    logger.info("[ANNO COMPO]" + a);

    // create the panel that will represent this annotation
    AnnotationPane p = new AnnotationPane(panelConfig, a, cell);
    p.addSaveButtonListener(new AnnotationSaveListener(a, p));
    p.addViewOnHudButtonListener(new AnnotationViewOnHudListener(a));

    // create hud component, show in world if necessary
    HUDComponent myHudCompo = AnnotationPlugin.createHUDComponent(p, cell);
    myHudCompo.setTransparency(0.3f);
    myHudCompo.setWorldLocation(baseAnnoLocation);
    logger.info("[ANNO COMPO] base location is:" + baseAnnoLocation);
    hudComponents.put(a.getID(), myHudCompo);

    // add new compo to hud BEFORE you set it visible
    AnnotationPlugin.addHUDComponent(myHudCompo);
    // display if global AND this cell are both set to display
//    if((globalDisplay && display)){
//      logger.info("[ANNO COMPO] SETTING WORLD VISIBLE");
//      myHudCompo.setWorldVisible((globalDisplay && display));
//    }
//    else{
//      logger.info("[ANNO COMPO] not WORLD VISIBLE.. global is" + globalDisplay + " " + " and local is " + display);
//    }
    

    // TODO temporary until edit controls are created!
    // move default location
//    float x = baseAnnoLocation.getX();
//    float y = baseAnnoLocation.getY();
//    float z = baseAnnoLocation.getZ();
//    baseAnnoLocation = baseAnnoLocation.add(2.0f, 2.0f, 0.5f);
//    logger.info("[ANNO COMPO] adjusted base location is:" + baseAnnoLocation);
    
    // create the node to display in world
    AnnotationEntity an = new AnnotationEntity(a, panelConfig, sceneRoot, cell);
    an.setLocalTranslation(baseAnnoLocation);
    baseAnnoLocation = baseAnnoLocation.add(0.5f, 0.5f, 0.5f);
    nodes.put(a.getID(), an);

    // display the node if necessary
    if((globalDisplay && display)){
      displayAnnotationInWorld(a.getID());
    }

    logger.info("new entity is:" + an + " name is " + an.getName());
    
    
  }


  /**
   * remove an annotation, also removing its hud component
   * @param a
   */
  private void removeAnnotation(MetadataID annoID) {
    logger.info("[ANNO COMPO] remove annotation with id " + annoID);
    if(!hudComponents.containsKey(annoID)){
      logger.info("[ANNO COMPO] remove annotation note: trying to remove non-registered annotation with id " + annoID);
      return;
    }
    HUDComponent hc = hudComponents.get(annoID);
    if(hc == null){
      logger.info("[ANNO COMPO] remove annotation note: hc was null for annotation with id " + annoID);
      return;
    }
    
    // remove hud component
    AnnotationPlugin.removeHUDComponent(hc);
    hudComponents.remove(annoID);
    hc.setWorldVisible(false);
    hc.setVisible(false);
    hc = null;

    // remove node
    AnnotationEntity an = nodes.get(annoID);
    an.dispose();
    nodes.remove(annoID);
  }

  /**
   * convenience overload
   * remove an annotation, also removing its hud component
   * @param a
   */
  private void removeAnnotation(Annotation a) {
    removeAnnotation(a.getID());
  }

  /**
   * remove all annotations, eliminating their hud components as well
   * @param a
   */
  private void removeAllAnnotations() {
    logger.info("[ANNO COMPO] remove all anotations");
    for(MetadataID a:hudComponents.keySet()){
      removeAnnotation(a);
    }
  }

  /**
   * adjust an annotation. Pass in the modified annotation. Matched to annotation
   * in this component via ID.
   * @param a modified annotation
   */
  private void modifyAnnotation(Annotation a) {
    logger.info("[ANNO COMPO] mod annotation, passed in:" + "\n" + a);
    HUDComponent hc = hudComponents.get(a.getID());
    if(hc == null){
      logger.severe("[ANNO COMPO] error: trying to modify a non existent annotation" +
              " with id" + a.getID());
      return;
    }

    // cast to HC's implementation class to get backing JComponent out
    HUDComponent2D hc2d = (HUDComponent2D) hc;
    // cast component back to an AnnotationPane
    AnnotationPane ap = (AnnotationPane) hc2d.getComponent();
    // now we can reset all of its fields to match passed annotation
    ap.setAuthor(a.getCreator());
    ap.setText(a.getText());
    ap.setDate(a.getModified());

    // adjust in-world entity
    AnnotationEntity n = nodes.get(a.getID());
    nodes.remove(a.getID());
    PanelConfig pc = n.getPanelConfig();
    Vector3f loc = n.getNode().getLocalTranslation();
    n.dispose();
    n = new AnnotationEntity(a, pc, sceneRoot, cell);
    n.setLocalTranslation(loc);
    nodes.put(a.getID(), n);
    if(display && globalDisplay){
      n.setVisible(true);
    }
  }

  // These functions are called after an annotation is updated within the component (e.g., an
  // annotation pane has been edited).
  // Informs associated Metadata component of the change.
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

  /**
   * Clear out all current annotations and hud components, then
   * get all annotations from the associated metadata component and
   * add them here, creating their hud components
   *
   * Creates a new thread to request the annotations.
   *
   * will clear out
   */
  private void rebuildAnnotations() {
    logger.info("[ANNO COMPO] rebuild annotations (new compo, or cache was invalidated)");
    removeAllAnnotations();

    // fetch annotations from metadata component
    // do this in a separate thread: when a component is added via the the properties
    // pane, it can result in the component being constructed in the clientside
    // darkstar message receiver thread.
    //
    // Then, the send and wait here results
    // in the darkstarReceiver thread blocking and waiting, since that is
    // the thread running this constructor. If the receiver itself blocks, no
    // messages can be received and the sendAndWait will hang forever.
    //
    // by spinning a new thread here, that new thread will (potentially) block
    // instead of the message receiver.
    new Thread(new Runnable(){

      public void run() {
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
    }).start();
  }
  
  /**
   * display an annotation on the HUD
   * @param a annotation to show or hide
   * @param b show or hide annotation
   */
  public void setAnnotationHudVisible(Annotation a, boolean b){
    HUDComponent hc = hudComponents.get(a.getID());
    if(hc == null){
      logger.severe("[ANNO COMPO] error: trying to setAnnotationHudVisible a non-exisiting annotation" +
              "component with id " + a.getID());
    }
    hc.setVisible(b);
    if(b == true){
      logger.info("stay visible in world!");
      hc.setWorldVisible(true);
    }
    logger.info("[ANNO COMPO] setting visible hc is now:" + hc);
  }

  /**
   * returns a PanelConfig based on the pased in integer, which should be
   * a count of annotation components. Used to get semi-unique coloration.
   * @param i count of annotation components
   * @return
   */
  private PanelConfig getPanelConfig() {
    logger.info("[ANNO COMPO] get pc... count is" + panelConfigCount);
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
        pc = new PanelConfig(new Color(230, 150, 65), Color.black, Color.lightGray);
        break;
      case 2:
        // blue and black
        logger.info("default pc 2");
        pc = new PanelConfig(new Color(150, 175, 210), Color.black, Color.lightGray);
        break;
      case 3:
        // red and white
        logger.info("default pc 3");
        pc = new PanelConfig(new Color(145, 20, 20), Color.white, Color.lightGray);
        break;
      case 4:
        pc = new PanelConfig(new Color(250, 250, 165), Color.black, Color.lightGray);
        break;
      default:
        panelConfigCount = 1;
        pc = new PanelConfig();
    }   
    panelConfigCount += 1;
    logger.info("panel config count is now " + panelConfigCount);
    return pc;
  }

  /**
   * Show an already-added annotation in the world
   * @param id
   */
  public void displayAnnotationInWorld(MetadataID id) {
    AnnotationEntity an = nodes.get(id);
    an.setVisible(true);
  }

  /**
   * Hide an annotation in the world and on the HUD
   * @param id
   */
  public void hideAnnotation(MetadataID id) {
    AnnotationEntity an = nodes.get(id);
    an.setVisible(false);
    HUDComponent hc = hudComponents.get(id);
    hc.setVisible(false);
    hc.setWorldVisible(false);
  }

  /**
   * Show an already-added annotation on the HUD
   * @param id
   */
  public void displayAnnotationOnHUD(MetadataID id) {
    hudComponents.get(id).setVisible(true);
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
      // changes, then send them back here
      AnnotationComponent.logger.info("[ANNO COMPONENT] save button for anno with ID " + anno.getID() + " clicked");
      AnnotationComponent.logger.info("[ANNO COMPONENT] should change text from " + anno.getText() );
      AnnotationComponent.logger.info("[ANNO COMPONENT] should change text to " + pane.getEditableText() );
      AnnotationComponent.logger.info("[ANNO COMPONENT] should change subject from " + anno.getSubject() );
      AnnotationComponent.logger.info("[ANNO COMPONENT] should change subject to " + pane.getEditableSubject() );
      anno.setText(pane.getEditableText());
      anno.setSubject(pane.getEditableSubject());
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







  class CtxListener implements ContextMenuListener{
    

    public void contextMenuDisplayed(ContextEvent event) {

      // only pay attention if it is for our cell
      if(event.getPrimaryCell().getCellID() != cell.getCellID()){
        // not for us
        return;
      }

      Entity primary = event.getPrimaryEntity();

      if(!(primary instanceof AnnotationEntity)){
        // this should open up our cell's regular context menu
        // TODO could remove annotation CMF's here, but better to do that
        // on a menu close instead of spurious calls here
        return;
      }
      AnnotationEntity ent = (AnnotationEntity) primary;
      logger.info("do context menu for annotation " + ent.getAnnoID());
      contextMenuCompo.setShowStandardMenuItems(false);

      editCMF.setAnnotationID(ent.getAnnoID());
      contextMenuCompo.addContextMenuFactory(editCMF);

      moveCMF.setAnnotationID(ent.getAnnoID());
      contextMenuCompo.addContextMenuFactory(moveCMF);

      deleteCMF.setAnnotationID(ent.getAnnoID());
      contextMenuCompo.addContextMenuFactory(deleteCMF);

    }

  }

  
  /**
   * Context menu factory for the edit annoation menu item
   */
  class EditAnnotationContextMenuFactory implements ContextMenuFactorySPI {
    MetadataID annoID;

    /**
     * When an annotation's context menu is opening, set its ID here
     * @param mid the annotation's id
     */
    public void setAnnotationID(MetadataID mid) {
      annoID = mid;
    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        return new ContextMenuItem[] {
            new SimpleContextMenuItem("Edit Annotation", null, new EditAnnotationContextMenuListener(annoID))
        };
    }
  }

  /**
   * Listener for when the edit item is selected from the annotation's context menu
   */
  class EditAnnotationContextMenuListener implements ContextMenuActionListener {
    MetadataID annoID;
    /**
     *
     * @param t the type of metadata to create
     */
    public EditAnnotationContextMenuListener(MetadataID mid){
      annoID = mid;
    }

    public void actionPerformed(ContextMenuItemEvent event) {
      // create an object
      logger.info("[ANNO COMPO] edit annotation performed for aid " + annoID);
      displayAnnotationOnHUD(annoID);

      // TODO this is not the best place for this, it would be better suited
      // in an event listenr for a 'context menu closed' event, but put it
      // here for now to avoid crippling the parent cell
      contextMenuCompo.setShowStandardMenuItems(true);
    }
  }

  /**
   * Context menu factory for move annotation menu item
   */
  class MoveAnnotationContextMenuFactory implements ContextMenuFactorySPI {
    MetadataID annoID;

    /**
     * When an annotation's context menu is opening, set its ID here
     * @param mid the annotation's id
     */
    public void setAnnotationID(MetadataID mid) {
      annoID = mid;
    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        return new ContextMenuItem[] {
            new SimpleContextMenuItem("Move Annotation", null, new MoveAnnotationContextMenuListener(annoID))
        };
    }
  }

  /**
   * Listener for when the move item is selected from the annotation's context menu
   */
  class MoveAnnotationContextMenuListener implements ContextMenuActionListener {
    MetadataID annoID;
    /**
     *
     * @param t the type of metadata to create
     */
    public MoveAnnotationContextMenuListener(MetadataID mid){
      annoID = mid;
    }

    public void actionPerformed(ContextMenuItemEvent event) {
      // create an object
      logger.info("[ANNO COMPO] move annotation performed for aid " + annoID);
      // TODO this is not the best place for this, it would be better suited
      // in an event listenr for a 'context menu closed' event, but put it
      // here for now to avoid crippling the parent cell
      contextMenuCompo.setShowStandardMenuItems(true);

    }
  }
  
  /**
   * Context menu factory for delete annotation menu item
   */
  class DeleteAnnotationContextMenuFactory implements ContextMenuFactorySPI {
    MetadataID annoID;

    /**
     * When an annotation's context menu is opening, set its ID here
     * @param mid the annotation's id
     */
    public void setAnnotationID(MetadataID mid) {
      annoID = mid;
    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        return new ContextMenuItem[] {
            new SimpleContextMenuItem("Move Annotation", null, new DeleteAnnotationContextMenuListener(annoID))
        };
    }
  }

  /**
   * Listener for when the move item is selected from the annotation's context menu
   */
  class DeleteAnnotationContextMenuListener implements ContextMenuActionListener {
    MetadataID annoID;
    /**
     *
     * @param t the type of metadata to create
     */
    public DeleteAnnotationContextMenuListener(MetadataID mid){
      annoID = mid;
    }

    public void actionPerformed(ContextMenuItemEvent event) {
      // create an object
      logger.info("[ANNO COMPO] delete annotation performed for aid " + annoID);
      removeAnnotation(annoID);
      // TODO this is not the best place for this, it would be better suited
      // in an event listenr for a 'context menu closed' event, but put it
      // here for now to avoid crippling the parent cell
      contextMenuCompo.setShowStandardMenuItems(true);

    }
  }


}
