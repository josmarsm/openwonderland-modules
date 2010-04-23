/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.server;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.ViewComponentServerState;
import org.jdesktop.wonderland.modules.imageviewer.common.cell.ImageViewerCellServerState;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedModel;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedNews;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.server.childcell.TimelineImageCellMO;
import org.jdesktop.wonderland.modules.timeline.server.childcell.TimelineStickyCellMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ModelCellMO;

/**
 * Map from dated objects to corresponding cells to be added to the timeline.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineChildCellCreatorImpl
        implements TimelineChildCellCreator
{
    private static final Logger logger =
            Logger.getLogger(TimelineChildCellCreatorImpl.class.getName());

    private ManagedReference<TimelineCellMO> timelineCellRef;
    private final Map<DatedObject, CellCleanup> cleanupMap =
            new HashMap<DatedObject, CellCleanup>();

    public void setLive(TimelineCellMO timeline) {
        timelineCellRef = AppContext.getDataManager().createReference(timeline);
    }

    public void createCell(DatedObject obj) {
        logger.warning("Create cell for " + obj +
                       " Class " + obj.getClass().getSimpleName());

        CellCleanup cleanup = null;
        if (obj instanceof DatedNews) {
            logger.warning("Found DatedNews");
            cleanup = createNewsCell((DatedNews) obj);
        } else if (obj instanceof DatedImage) {
            logger.warning("Found DatedImage");
            cleanup = createImageCell((DatedImage) obj);
        } else if (obj instanceof DatedModel) {
            logger.warning("Found DatedModel");
            cleanup = createModelCell((DatedModel) obj);
        }

        if (cleanup == null) {
            logger.warning("Unable to create cell for " + obj);
            return;
        }

        cleanupMap.put(obj, cleanup);
    }

    public void cleanupCell(DatedObject obj) {
        CellCleanup cleanup = cleanupMap.remove(obj);
        if (cleanup != null) {
            cleanup.cleanup();
        }
    }

    protected CellCleanup createNewsCell(DatedNews news) {
        logger.warning("Create news cell for " + news);

        StickyNoteCellServerState state = new StickyNoteCellServerState();
        state.setNoteText(news.getText());
        state.setNoteType(StickyNoteTypes.GENERIC);

        ViewComponentServerState vccs = new ViewComponentServerState(new CellTransform(new Quaternion(), Vector3f.ZERO));
        state.addComponentServerState(vccs);

        if (news.getImageURI() != null) {
            ClickableLinkComponentServerState clss = new ClickableLinkComponentServerState();
            clss.setLinkURL(news.getURI());
            state.addComponentServerState(clss);
        }

        CellMO cell = new TimelineStickyCellMO(news);
        cell.setServerState(state);

        try {
            System.out.println("Add cell " + cell + " to " + timelineCellRef.get());
            timelineCellRef.get().addChild(cell);
            return new RemoveCellCleanup(cell);
        } catch (MultipleParentException mpe) {
            logger.log(Level.WARNING, "Error adding " + cell + " to " +
                       timelineCellRef.get(), mpe);
            return null;
        }
    }

    protected CellCleanup createImageCell(DatedImage image) {
        logger.warning("Create image cell for " + image);

        ImageViewerCellServerState state = new ImageViewerCellServerState();
        state.setImageURI(image.getImageURI());

        PositionComponentServerState pcss = new PositionComponentServerState();
        pcss.setRotation(new Quaternion());
        pcss.setTranslation(Vector3f.ZERO);
        state.addComponentServerState(pcss);

        ClickableLinkComponentServerState clss = new ClickableLinkComponentServerState();
        clss.setLinkURL(image.getImageURI());
        state.addComponentServerState(clss);

        CellMO cell = new TimelineImageCellMO(image);
        cell.setServerState(state);

        try {
            System.out.println("Add cell " + cell + " to " + timelineCellRef.get());
            timelineCellRef.get().addChild(cell);
            return new RemoveCellCleanup(cell);
        } catch (MultipleParentException mpe) {
            logger.log(Level.WARNING, "Error adding " + cell + " to " +
                       timelineCellRef.get(), mpe);
            return null;
        }
    }

    protected CellCleanup createModelCell(DatedModel model) {
        logger.warning("Create model cell for " + model);

        ModelCellServerState state = new ModelCellServerState();

        JmeColladaCellComponentServerState modelState = new JmeColladaCellComponentServerState();
        modelState.model = model.getModelURI();
        modelState.modelTranslation = Vector3f.ZERO;
        modelState.modelRotation = new Quaternion();
        modelState.modelScale = Vector3f.UNIT_XYZ;
        modelState.modelAuthor = "timeline";
        modelState.modelGroup = "group";
        modelState.setModelLoaderClassname("org.jdesktop.wonderland.modules.kmzloader.client.KmzLoader");
        state.addComponentServerState(modelState);

        PositionComponentServerState pcss = new PositionComponentServerState();
        pcss.setRotation(new Quaternion());
        pcss.setTranslation(Vector3f.ZERO);
        state.addComponentServerState(pcss);

        CellMO cell = new ModelCellMO();
        cell.setServerState(state);

        try {
            System.out.println("Add cell " + cell + " to " + timelineCellRef.get());
            timelineCellRef.get().addChild(cell);
            return new RemoveCellCleanup(cell);
        } catch (MultipleParentException mpe) {
            logger.log(Level.WARNING, "Error adding " + cell + " to " +
                       timelineCellRef.get(), mpe);
            return null;
        }
    }

//    protected void blah() {
//     
//        } else if (datedObj instanceof DatedAudio) {
//
//            // We're not going to make cells here, instead
//            // we're going to send messages to the Audio
//            // subsystem about which segment this audio
//            // file is associated with.
//            DatedAudio audio = (DatedAudio)datedObj;
//
//
////            logger.warning("Not handling audio objects properly yet. Need to refactor some stuff for them to work.");
//
//            out = null;
//
//
//    }

    protected interface CellCleanup {
        public void cleanup();
    }

    class RemoveCellCleanup implements CellCleanup, Serializable {
        public ManagedReference<CellMO> cellRef;

        public RemoveCellCleanup(CellMO cell) {
            cellRef = AppContext.getDataManager().createReference(cell);
        }

        public void cleanup() {
            timelineCellRef.get().removeChild(cellRef.get());
        }
    }

}
