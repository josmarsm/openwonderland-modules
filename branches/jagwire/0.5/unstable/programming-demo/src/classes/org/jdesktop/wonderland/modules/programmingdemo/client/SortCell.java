/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.programmingdemo.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.programmingdemo.client.jme.cellrenderer.SortCellRenderer;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.programmingdemo.client.jme.cellrenderer.SortCellRenderer.Sortable;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellClientState;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellOrderMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellSwapMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortConstants;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.tooltip.client.TooltipCellComponent;

/**
 * Cell that displays configurable sorted spheres
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SortCell extends Cell implements SharedMapListenerCli {
    private SortCellRenderer renderer = null;

    @UsesCellComponent 
    private ContextMenuComponent contextComp;

    @UsesCellComponent
    private ChannelComponent channel;

    @UsesCellComponent
    private SharedStateComponent state;

    @UsesCellComponent
    private TooltipCellComponent tooltip;

    private ContextMenuFactorySPI menuFactory = null;
    private List<Integer> order;
    private final List<Integer> highlighted = new ArrayList<Integer>();

    private SortSettingsPanel ssp;
    private HUDComponent sspHUD;

    public SortCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        order = ((SortCellClientState) state).getCurrentOrder();
        doReset(order, true);
    }

    public int get(int index) {
        if (index >= getItemCount()) {
            throw new IllegalArgumentException("Index " + index + " out of " +
                    "range: maximum is " + getItemCount());
        }

        return order.get(index);
    }

    public void requestSwap(int index1, int index2) {
        if (index1 >= getItemCount()) {
            throw new IllegalArgumentException("Index1 " + index1 + " out of " +
                    "range: maximum is " + getItemCount());
        }
        
        if (index2 >= getItemCount()) {
            throw new IllegalArgumentException("Index2 " + index2 + " out of " +
                    "range: maximum is " + getItemCount());
        }

        if (index1 == index2) {
            // we can just ignore these
            return;
        }

        ResponseMessage rm;
        try {
            rm = channel.sendAndWait(new SortCellSwapMessage(getCellID(),
                                                             index1, index2));
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, "Swap interrupted", ie);
            return;
        }
        if (rm instanceof OKMessage) {
            // swap was OK
            doSwap(index1, index2);
        } else {
            logger.warning("Unable to swap: " + rm);
        }
    }

    private void doSwap(int index1, int index2) {
        Integer val1 = order.get(index1);
        Integer val2 = order.get(index2);

        order.remove(index1);
        order.add(index1, val2);
        order.remove(index2);
        order.add(index2, val1);

        if (renderer != null) {
            renderer.swap(index1, index2);
        }
    }

    public void requestReset(boolean randomize) {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);

        String action = randomize ? SortConstants.RANDOM : SortConstants.PREVIOUS;
        settings.put(SortConstants.RESET,
                     SharedString.valueOf(String.valueOf(action)));
    }

    private void doReset(List<Integer> order, boolean local) {
        this.order = order;
        this.highlighted.clear();

        if (renderer != null) {
            List<Sortable> objs = new ArrayList<Sortable>(order.size());
            for (Integer i : order) {
                objs.add(new SimpleSortable(i, 0, order.size()));
            }
            renderer.reset(objs);
        }

        if (!local && ssp != null) {
            ssp.setItemCount(order.size());
        }
    }

    public int getItemCount() {
        return order.size();
    }

    public void requestItemCount(int itemCount) {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        settings.put(SortConstants.ITEM_COUNT,
                     SharedInteger.valueOf(itemCount));
    }

    public float getScale() {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        SharedString val = (SharedString) settings.get(SortConstants.SCALE);
        return Float.valueOf(val.getValue());
    }

    public void requestScale(float scale) {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        settings.put(SortConstants.SCALE,
                     SharedString.valueOf(String.valueOf(scale)));
    }

    private void doScale(SharedString scale, boolean local) {
        float val = Float.parseFloat(scale.getValue());
        renderer.setScale(val);

        if (!local && ssp != null) {
            ssp.setScale(val);
        }
    }

    public float getSpacing() {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        SharedString val = (SharedString) settings.get(SortConstants.SPACING);
        return Float.valueOf(val.getValue());
    }

    public void requestSpacing(float spacing) {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        settings.put(SortConstants.SPACING,
                     SharedString.valueOf(String.valueOf(spacing)));
    }

    private void doSpacing(SharedString spacing, boolean local) {
        float val = Float.parseFloat(spacing.getValue());
        renderer.setSpacing(val);

        if (!local && ssp != null) {
            ssp.setSpacing(val);
        }
    }

    public Color getColor() {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        SharedInteger val = (SharedInteger) settings.get(SortConstants.COLOR);
        return new Color(val.getValue());
    }

    public void requestColor(Color color) {
        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        settings.put(SortConstants.COLOR, SharedInteger.valueOf(color.getRGB()));
    }

    private void doColor(SharedInteger color, boolean local) {
        Color val = new Color(color.getValue());
        renderer.setColor(val);

        if (!local && ssp != null) {
            ssp.setColor(val);
        }
    }

    public void requestHighlight(int... indices) {
        StringBuffer str = new StringBuffer();
        for (int i : indices) {
            str.append(i + " ");
        }

        SharedMapCli settings = state.get(SortConstants.SETTINGS);
        settings.put(SortConstants.HIGHLIGHT_ITEMS,
                     SharedString.valueOf(str.toString()));
    }

    private void doHighlightItems(SharedString items) {
        // remove any existing highlights
        for (int index : highlighted) {
            renderer.highlight(index, false);
        }
        highlighted.clear();

        if (items == null) {
            return;
        }

        for (String indexStr : items.getValue().split(" ")) {
            if (indexStr.trim().length() == 0) {
                continue;
            }

            int index = Integer.parseInt(indexStr);
            highlighted.add(index);
            renderer.highlight(index, true);
        }
    }

    public void setTooltipText(String text) {
        ((CodeTooltipComponent) tooltip).setText(text);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false) {
            channel.removeMessageReceiver(SortCellSwapMessage.class);

            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }

            if (sspHUD != null) {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                mainHUD.removeComponent(sspHUD);
            }
        } else if (status == CellStatus.ACTIVE && increasing == true) {
            doReset(order, true);

            SortCellMessageReceiver recv = new SortCellMessageReceiver();
            channel.addMessageReceiver(SortCellSwapMessage.class, recv);
            channel.addMessageReceiver(SortCellOrderMessage.class, recv);
            
            SharedMapCli settings = state.get(SortConstants.SETTINGS);
            settings.addSharedMapListener(this);
            doScale(settings.get(SortConstants.SCALE, SharedString.class), true);
            doSpacing(settings.get(SortConstants.SPACING, SharedString.class), true);
            doHighlightItems(settings.get(SortConstants.HIGHLIGHT_ITEMS, SharedString.class));
            doColor(settings.get(SortConstants.COLOR, SharedInteger.class), true);

            if (menuFactory == null) {
                final MenuItemListener l = new MenuItemListener();
                menuFactory = new ContextMenuFactorySPI() {
                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                            new SimpleContextMenuItem("Sort Settings", l)
                        };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new SortCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    public void propertyChanged(SharedMapEventCli smec) {
        boolean local = getCellCache().getSession().getID().equals(smec.getSenderID());

        if (smec.getPropertyName().equals(SortConstants.SCALE)) {
            doScale((SharedString) smec.getNewValue(), local);
        } else if (smec.getPropertyName().equals(SortConstants.SPACING)) {
            doSpacing((SharedString) smec.getNewValue(), local);
        } else if (smec.getPropertyName().equals(SortConstants.COLOR)) {
            doColor((SharedInteger) smec.getNewValue(), local);
        } else if (smec.getPropertyName().equals(SortConstants.HIGHLIGHT_ITEMS)) {
            doHighlightItems((SharedString) smec.getNewValue());
        }
    }

    public void showSettingsHUD() {
        if (sspHUD != null) {
            return;
        }

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        ssp = new SortSettingsPanel(this);
        sspHUD = mainHUD.createComponent(ssp, this);
        sspHUD.setPreferredLocation(Layout.NORTHEAST);
        sspHUD.setName("Sort Cell Settings");
        sspHUD.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
                if (event.getEventType() == HUDEvent.HUDEventType.DISAPPEARED) {
                    ssp = null;
                    sspHUD = null;
                }
            }
        });
        mainHUD.addComponent(sspHUD);
        sspHUD.setVisible(true);
    }

    class MenuItemListener implements ContextMenuActionListener {
        public void actionPerformed(ContextMenuItemEvent event) {
            if (sspHUD == null) {
                showSettingsHUD();
            }
        }
    }

    class SortCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            boolean local = getCellCache().getSession().getID().equals(message.getSenderID());

            if (message instanceof SortCellSwapMessage) {
                if (local) {
                    // ignore messages we sent
                    return;
                }

                SortCellSwapMessage scsm = (SortCellSwapMessage) message;
                doSwap(scsm.getIndex1(), scsm.getIndex2());
            } else if (message instanceof SortCellOrderMessage) {
                doReset(((SortCellOrderMessage) message).getOrder(), local);
            }
        }
    }

    class SimpleSortable implements Sortable {
        private int value;
        private float percent;

        public SimpleSortable(int value, int min, int max) {
            this.value = value;

            percent = (float) value / ((max - min) - 1);
        }

        public String getValue() {
            return String.valueOf(value);
        }

        public float getColorPercentage() {
            return percent;
        }
    }
}
