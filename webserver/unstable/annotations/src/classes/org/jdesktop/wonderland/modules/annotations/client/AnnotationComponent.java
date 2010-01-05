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

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuInvocationSettings;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.HUDComponent;

import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationEntity;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationEntity.DisplayMode;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationPane;
import org.jdesktop.wonderland.modules.annotations.common.AnnotationSettings;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.hud.client.HUDComponent2D;
import org.jdesktop.wonderland.modules.metadata.client.MetadataComponent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEvent;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEventListener;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * 
 * @author mabonner
 */
public class AnnotationComponent extends CellComponent 
        implements CacheEventListener{
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
   * default graphical options for annotations on this cell/component
   */
  private AnnotationSettings defaultSettings;

  /**
   * maps annotations (from associated MetadataComponent) to hud components
   */
  HashMap<MetadataID, HUDComponent> hudComponents = new HashMap<MetadataID, HUDComponent>();

  /**
   * maps annotations (from associated MetadataComponent) to AnnotationNodes displayed in world
   */
  HashMap<MetadataID, AnnotationEntity> entities = new HashMap<MetadataID, AnnotationEntity>();
  
  /**
   * local (component) display mode setting.. will be overwritten by the global
   * every time the global is changed
   */
  DisplayMode localDisplay = DisplayMode.HIDDEN;

  /**
   * local (component) font size setting.. will be overwritten by the global
   * every time the global is changed
   */
  private float localFontModifier;

  /**
   * where to place new annotations initially
   */
  private Vector3f baseAnnoLocation;
  
  /**
   * count of annotation components, used to assign different colors to component's panes
   * @param cell
   */
  private static int panelConfigCount = 0;

  /**
   * The root of this component's parent cell, used to add AnnotationNodes
   */
  private Node sceneRoot;
  private CtxListener ctxListener;

  /**
   * Creates annotation specific items for context menu, set its annotation
   * ID before using for each annotation.
   */
   AnnotationContextMenuFactory annoCMF = new AnnotationContextMenuFactory();

  /**
   * Constructor, takes the parent cell for default cell component constructor
   * @param cell
   */
  public AnnotationComponent(Cell cell) {
    super(cell);
    defaultSettings = getDefaultAnnotationSettings();
    logger.info("[ANNO COMPO] compo created");
    if(defaultSettings == null){
      logger.severe("[ANNO COMPO] settings are null!");
    }
  }

  /**
   * AnnotationSettings that will be applied to new annotations by default, unless
   * they are changed one by one.
   * @param pc
   */
  public void setDefaultAnnotationSettings(AnnotationSettings as){
    defaultSettings = as;
  }

  /**
   * set a new component-wide display policy
   * @param newMode the new display mode setting
   */
  public void setLocalDisplayMode(DisplayMode newMode){
    logger.info("[ANNO COMPO] set display mode: " + newMode);
    localDisplay = newMode;
    for(AnnotationEntity ent:entities.values()){
      setAnnotationDisplayMode(ent, newMode);
    }
  }

  /**
   * set a new component-wide font size modifier
   * @param newMode the new display mode setting
   */
  public void setLocalFontSizeModifier(float mod){
    logger.info("[ANNO COMPO] set font size mod: " + mod);
    localFontModifier = mod;
    for(AnnotationEntity ent:entities.values()){
      ent.setFontSizeModifier(mod);
    }
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing){
    super.setStatus(status, increasing);
    if(status == CellStatus.RENDERING && increasing){
      AnnotationPlugin.addComponentListener(this);
      localDisplay = AnnotationPlugin.getDisplayMode();
      localFontModifier = AnnotationPlugin.getFontSizeModifier();
      logger.info("[ANNO COMPO] add self as listener! local display val is now: " + localDisplay + " and font mod is " + localFontModifier);
      
      // listen to ctx menu events
      ctxListener = new CtxListener();
      ContextMenuManager.getContextMenuManager().addContextMenuListener(ctxListener);

      // get root node of cell
      CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
      RenderComponent cellRC = (RenderComponent)renderer.getEntity().getComponent(RenderComponent.class);
      sceneRoot = cellRC.getSceneRoot();

      // set metadata component reference
      metaCompo = cell.getComponent(MetadataComponent.class);

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

      // listen to changes in associated metadata compo's metadata cache
      metaCompo.addCacheListener(this);

      // prepare annotations
      rebuildAnnotations();
    }
    else if(status == CellStatus.ACTIVE && !increasing){
      logger.info("[ANNO COMPO] remove self as listener!");
      AnnotationPlugin.removeDisplayListener(this);
    }
    else if(status == CellStatus.DISK && !increasing){
      logger.info("[ANNO COMPO] clean up annotations");
      for(AnnotationEntity node:entities.values()){
        node.dispose();
      }
      entities.clear();
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
    // if annotation is modified while being added (position, graphics settings..)
    // alert the metadata system of the changes to make them persistentexport ANT_OPTS="-Xms512m -Xmx1024m -XX:MaxPermSize=900m"
    boolean modified = false;

    if(a.getSettings() == null){
      // annotation doesn't have its own graphical settings, give it this cell's
      // default look
      logger.info("[ANNO COMPO] settings were null, set to defaults " );
      a.setSettings(defaultSettings);
      modified = true;
    }

    if(a.getTranslation() == null){
      // position in front of user's current position
      // get current position
      ViewCell vc = ClientContextJME.getViewManager().getPrimaryViewCell();
      CellTransform trans = vc.getWorldTransform();
      Vector3f pos = new Vector3f(), look = new Vector3f();
      trans.getLookAt(pos, look);
      logger.info("POSITION, LOOK: " + pos + " " + look);
      // x, y, z
      Vector3f loc = pos.add((look.getX() * 2.5f) , 3.0f, (look.getZ() * 2.5f));
      a.setTranslation(loc);
      logger.info("adjusted POSITION: " + loc);
      // annotation doesn't have its own position, give it this cell's
      // default starting position
//      a.setTranslation(baseAnnoLocation);
//      baseAnnoLocation = baseAnnoLocation.add(0.5f, 0.5f, 0.5f);
      modified = true;
    }

    // TODO is this ok?
    logger.info("[ANNO COMPO] adding annotation to cell " + cell.getCellID());
    if(hudComponents.get(a.getID()) != null){
      logger.info("[ANNO COMPO] annotation already registered and has non-null hud compo!");
      return;
    }

    logger.info("[ANNO COMPO]" + a);

    // create the panel that will represent this annotation
    AnnotationPane p = new AnnotationPane(a, cell);
    p.addSaveButtonListener(new AnnotationSaveListener(a, p));

    // create hud component, show in world if necessary
    HUDComponent myHudCompo = AnnotationPlugin.createHUDComponent(p, cell);
    myHudCompo.setTransparency(0.3f);
    myHudCompo.setHeight(p.getMinimumSize().height);
    myHudCompo.setWorldLocation(baseAnnoLocation);
    logger.info("[ANNO COMPO] base location is:" + baseAnnoLocation);
    hudComponents.put(a.getID(), myHudCompo);

    // add new compo to hud BEFORE you set it visible
    AnnotationPlugin.addHUDComponent(myHudCompo);
    
    // create the entity to represent in world
    logger.info("add anno, and local display is:" + localDisplay);
    AnnotationEntity an = new AnnotationEntity(a, cell, localDisplay, localFontModifier);
    entities.put(a.getID(), an);

    logger.info("new entity is:" + an + " name is " + an.getName());

    if(modified){
      // alert metadata system of change to make persistent
      modifyAnnotationInMetadata(a);
    }
    
  }


  /**
   * remove an annotation, also removing its hud component
   * does NOT remove from metadata component, use removeFromMetadata to do this.
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
    hc.setWorldVisible(false);
    hc.setVisible(false);
    hc = null;

    // remove node
    AnnotationEntity an = entities.get(annoID);
    an.dispose();
    entities.remove(annoID);
  }

  /**
   * convenience overload
   * remove an annotation, also removing its hud component
   * does NOT remove from metadata component
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
    // silly rabbit, you can't remove these during iteration over their container
    hudComponents.clear();
  }

  /**
   * adjust an annotation. Pass in the modified annotation. Matched to annotation
   * in this component via ID.
   * @param a modified annotation
   */
  private void modifyAnnotation(Annotation a) {
    logger.info("[ANNO COMPO] mod annotation, passed in:" + a.getID() + "with location" + a.getTranslation());
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
    ap.setSettings(a.getSettings());
//    ap.removeSaveButtonListener();
//    ap.addSaveButtonListener(new AnnotationSaveListener(a, ap));


    // adjust in-world entity
    AnnotationEntity n = entities.get(a.getID());
//    DisplayMode currentDisplay = n.getDisplayMode();
//    entities.remove(a.getID());
//    AnnotationSettings pc = n.getAnnotationSettings();
//    Vector3f loc = n.getNode().getLocalTranslation();
//    n.dispose();
//    n = new AnnotationEntity(a, pc, cell, localDisplay, localFontModifier);
//    n.setLocalTranslation(loc);
//    entities.put(a.getID(), n);
    n.setAnnotation(a);
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
  public void modifyAnnotationInMetadata(Annotation a) {
    logger.info("[ANNO COMPO] modify anno " + a.getID() + " in metadata");
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
//    if(b == true){
//      logger.info("stay visible in world!");
//      hc.setWorldVisible(true);
//    }
    logger.info("[ANNO COMPO] setting visible hc is now:" + hc);
  }

  /**
   * returns a AnnotationSettings based on the pased in integer, which should be
   * a count of annotation components. Used to get semi-unique coloration.
   * @param i count of annotation components
   * @return
   */
  private AnnotationSettings getDefaultAnnotationSettings() {
    logger.info("[ANNO COMPO] get pc... count is" + panelConfigCount);
    AnnotationSettings pc = null;
    switch(panelConfigCount){
      case 0:
        // default - dark gray and white
        logger.info("default pc");
        pc = new AnnotationSettings();
        break;
      case 1:
        // orange and black
        logger.info("default pc 1");
        pc = new AnnotationSettings(new Color(230, 150, 65), Color.black, Color.gray);
        break;
      case 2:
        // blue and black
        logger.info("default pc 2");
        pc = new AnnotationSettings(new Color(150, 175, 210), Color.black, Color.gray);
        break;
      case 3:
        // red and white
        logger.info("default pc 3");
        pc = new AnnotationSettings(new Color(145, 20, 20), Color.white, Color.gray);
        break;
      case 4:
        // white
        logger.info("default pc 3");
        pc = new AnnotationSettings(new Color(255, 255, 255), Color.black, Color.gray);
        break;
      default:
        panelConfigCount = 1;
        pc = new AnnotationSettings();
    }   
    panelConfigCount += 1;
    logger.info("panel config count is now " + panelConfigCount);
    return pc;
  }

  /**
   * Change an annotation's display mode. To hide an annotation set it to
   * DisplayMode.HIDDEN
   * @param id id of annotation to adjust
   * @param mode new display mode for that annotation
   */
  public void setAnnotationDisplayMode(MetadataID id, DisplayMode mode) {
    AnnotationEntity an = entities.get(id);
    if(an == null){
      logger.severe("[ANNO COMPO] trying to set display mode of annotation " +
              id + " which is not attached to cell " + cell.getCellID());
    }
    setAnnotationDisplayMode(an, mode);
    
  }

  /**
   * helper function, some routines can call this directly. This saves looking
   * up an annotation entity multiple times.
   * @param ent
   * @param mode
   */
  private void setAnnotationDisplayMode(AnnotationEntity ent, DisplayMode mode){
    ent.setDisplayMode(mode);
    // hide the corresponding hud component if necessary
    if(mode == DisplayMode.HIDDEN){
      HUDComponent hc = hudComponents.get(ent.getAnnoID());
      hc.setVisible(false);
      hc.setWorldVisible(false);
    }
  }

  /**
   * Show an already-added annotation on the HUD
   * @param id
   */
  public void displayAnnotationOnHUD(MetadataID id) {
    // get annotation
    Annotation a = (Annotation) metaCompo.getMetadata(id);

    // get hud component
    HUDComponent hc = hudComponents.get(id);
    // cast to HC's implementation class to get backing JComponent out
    HUDComponent2D hc2d = (HUDComponent2D) hc;
    // cast component back to an AnnotationPane
    AnnotationPane ap = (AnnotationPane) hc2d.getComponent();

    // now we can reset all of its fields to match the annotation
    AnnotationEntity ae = entities.get(id);
    logger.info("setting ap subject to " + a.getSubject());
    ap.setSubject(a.getSubject());
    ap.setText(a.getText());
    ap.setEditableSubject(a.getSubject());
    ap.setEditableText(a.getText());
    ap.setDate(a.getModified());

    hc.setVisible(true);
  }

  /**
   * listens for save button presses in an AnnotationPane, updates the associated
   * Annotation and then informs component of update.
   */
  class AnnotationSaveListener implements ActionListener{
    WeakReference<MetadataID> annoID;
    WeakReference<AnnotationPane> pane;

    /**
     *
     * @param a the annotation to update
     * @param p the pane to retrive the updated annotation information from
     */
    public AnnotationSaveListener(Annotation a, AnnotationPane p){
      annoID = new WeakReference<MetadataID>(a.getID());
      pane = new WeakReference<AnnotationPane>(p);
    }

    public void actionPerformed(ActionEvent e) {
      // set changes
      AnnotationEntity ent = entities.get(annoID.get());
      Annotation anno = ent.getAnnotation();
      AnnotationPane paneRef = pane.get();
      anno.setText(paneRef.getEditableText());
      anno.setSubject(paneRef.getEditableSubject());
      logger.info("location of anno in save:" + anno.getTranslation() + " id is" + anno.getID());

      // if settings were changed, adjust annotation
      if(paneRef.isSettingsChanged()){
        ent.setAnnotationSettings(paneRef.getSettings());
        paneRef.setSettingsChanged(false);
        anno.setSettings(paneRef.getSettings());
        logger.info("anno settings changed, bg color is: " + paneRef.getSettings().getBackgroundColor());
      }
      // alert metadata system of change
      modifyAnnotationInMetadata(anno);
    }
  }

  class CtxListener implements ContextMenuListener{
    

    public void contextMenuDisplayed(org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent event) {

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

      // if the event isn't a ContextMenuEvent, an alternative implementation
      // of the context menu (rather than SwingContextMenu) has been created,
      // and this needs to be updated
      if(event instanceof ContextMenuEvent){
        ContextMenuEvent evt = (ContextMenuEvent) event;
        logger.info("adjust CME");
        // get/adjust settings
        ContextMenuInvocationSettings settings = evt.getSettings();
        settings.setDisplayStandard(false);
        settings.setDisplayCellStandard(false);
        settings.setMenuName("Annotation " + ent.getAnnoID());

        // set the CMF for this annotation
        annoCMF.setAnnotationID(ent.getAnnoID());
        settings.addTempFactory(annoCMF);
      }

    }

  }

  
  /**
   * Context menu factory for the edit, delete, move, and settings
   */
  class AnnotationContextMenuFactory implements ContextMenuFactorySPI {
    MetadataID annoID;
    ArrayList<SimpleContextMenuItem> items = new ArrayList<SimpleContextMenuItem>();

    public AnnotationContextMenuFactory(){
      // edit annotation text, color, etc
      items.add(new SimpleContextMenuItem("Edit Annotation", null, new EditAnnotationContextMenuListener()));
      // move annotation/edit location
      items.add(new SimpleContextMenuItem("Move Annotation", null, new MoveAnnotationContextMenuListener()));
      // delete annotation
      items.add(new SimpleContextMenuItem("Delete Annotation", null, new DeleteAnnotationContextMenuListener()));
    }

    /**
     * When an annotation's context menu is opening, set its ID here
     * @param mid the annotation's id
     */
    public void setAnnotationID(MetadataID mid) {
      annoID = mid;
    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        return items.toArray(new ContextMenuItem[4]);
    }

    /**
     * Listener for when the edit item is selected from the annotation's context menu
     */
    class EditAnnotationContextMenuListener implements ContextMenuActionListener {
      public void actionPerformed(ContextMenuItemEvent event) {
        logger.info("[ANNO COMPO] edit annotation performed for aid " + annoID);
        displayAnnotationOnHUD(annoID);
      }
    }

    /**
     * Listener for when the move item is selected from the annotation's context menu
     */
    class MoveAnnotationContextMenuListener implements ContextMenuActionListener {
      public void actionPerformed(ContextMenuItemEvent event) {
        logger.info("[ANNO COMPO] move annotation performed for aid " + annoID);
        AnnotationEntity ent = entities.get(annoID);
        ent.toggleMoving();
//        TranslateAffordance affordance = new TranslateAffordance(ent.getNode());
//        affordance.addTranslationListener(new TranslateAffordanceListener());
//        affordance.setVisible(true);
      }
    }

    /**
     * Listener for when the delete item is selected from the annotation's context menu
     */
    class DeleteAnnotationContextMenuListener implements ContextMenuActionListener {
      public void actionPerformed(ContextMenuItemEvent event) {
        logger.info("[ANNO COMPO] delete annotation performed for aid " + annoID);
        removeAnnotation(annoID);
        removeAnnotationFromMetadata((Annotation)metaCompo.getMetadata(annoID));
      }
    }
  }

}
