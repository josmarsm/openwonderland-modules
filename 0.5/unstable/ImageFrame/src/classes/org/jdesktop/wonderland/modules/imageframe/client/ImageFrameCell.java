/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameClientState;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 * cell for image frame
 */
public class ImageFrameCell extends Cell {

    @UsesCellComponent
    public SharedStateComponent sharedState;
    public SharedMapCli propertyMap;
    private ImageFrameCellRenderer renderer = null;

    private int fit = -1;
    private int aspectRatio = -1;
    private int orientation = -1;
    public int frameWidth = -1;
    public int frameHeight = -1;
    public String imageURL = null;

    /**
     * For adding "Remove Image" menu item in context menu of ImageFrame
     */
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    public ImageFrameCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        this.addChildrenChangeListener(new ChildChangeListener(this));
    }

    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        fit = ((ImageFrameClientState) clientState).getFit();
        aspectRatio = ((ImageFrameClientState) clientState).getAspectRatio();
        orientation = ((ImageFrameClientState) clientState).getOrientation();
        frameHeight = ((ImageFrameClientState) clientState).getFrameHeight();
        frameWidth = ((ImageFrameClientState) clientState).getFrameWidth();
        imageURL = ((ImageFrameClientState) clientState).getImageURL();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false) {

        } else if (status == CellStatus.RENDERING && increasing == true) {

            //For adding "Remove Image" menu item in context menu of ImageFrame
            if (menuFactory == null) {
                final RemoveImageMenuItemListener l = new RemoveImageMenuItemListener(this);
                menuFactory = new ContextMenuFactorySPI() {
                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                            new SimpleContextMenuItem("Remove Image", l)
                        };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }

        } else if (status == CellStatus.ACTIVE && increasing) {

        }
    }

    public void InitProps() {
        propertyMap = sharedState.get(ImageFrameConstants.propertyMap);
        propertyMap.addSharedMapListener(new PropertyMapChangeListener(this));

        if (propertyMap.size() == 0) {
            ImageFrameProperties ifp = new ImageFrameProperties();
            ifp.setFit(0);
            ifp.setAspectRatio(0);
            ifp.setOrientation(0);

            if (fit != -1) {
                ifp.setFit(fit);
            }
            if (aspectRatio != -1) {
                ifp.setAspectRatio(aspectRatio);
            }
            if (orientation != -1) {
                ifp.setOrientation(orientation);
            }
            propertyMap.put(ImageFrameConstants.ImageFrameProperty, ifp);
        }

    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        InitProps();
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new ImageFrameCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }
    }

    protected WonderlandSession getSession() {
        return getCellCache().getSession();
    }

}
