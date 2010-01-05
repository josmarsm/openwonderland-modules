/**
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
package org.jdesktop.wonderland.modules.timeline.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 *
 *  
 */
@CellFactory
public class TimelineCellFactory implements CellFactorySPI {

    private static final Logger logger =
            Logger.getLogger(TimelineCellFactory.class.getName());
    private TimelineCellServerState state;
    private TimelineCreationHUDPanel creationPanel;
    private HUDComponent timelineCreationHUD;
    private final Object configLock = new Object();
    private boolean ready = false;

    public String[] getExtensions() {
        return null;
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        state = new TimelineCellServerState();
        state.setConfig(new TimelineConfiguration());
        createCreationHUD();

        synchronized (configLock) {
            while (!ready) {
                try {
                    configLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }

//        // Setup code for the sample provider. Just used for testing.
//        TimelineProviderServerState providerState = new TimelineProviderServerState();
//        TimelineQuery query = new TimelineQuery("org.jdesktop.wonderland.modules.timelineproviders.provider.SampleProvider");
//        query.getProperties().setProperty("test", "123");
//        providerState.getQueries().add(query);
//
//        state.addComponentServerState(providerState);

        return (T) state;
    }

    private void createCreationHUD() {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        creationPanel = new TimelineCreationHUDPanel();
        creationPanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
                if (pe.getPropertyName().equals("create")) {
                    logger.info("--- create/update timeline");

                    String title = creationPanel.getTitle();
                    String description = creationPanel.getDescription();
                    Date start = creationPanel.getStartDate();
                    Date end = creationPanel.getEndDate();
                    float scale = creationPanel.getScale();
                    TimelineConfiguration.TimelineUnits units = creationPanel.getUnits();

                    logger.info("--- title: " + title);
                    logger.info("--- description: " + description);
                    logger.info("--- start: " + start);
                    logger.info("--- end: " + end);
                    logger.info("--- scale: " + scale);
                    logger.info("--- units: " + units);

                    state.getConfig().setDateRange(new TimelineDate(start, end));
                    state.getConfig().setUnits(units);

                    List<TimelineQuery> queries = creationPanel.getQueries();
                    logger.info("--- queries: " + queries);
                    if (queries != null) {
                        TimelineProviderServerState tpss = new TimelineProviderServerState();

                        ListIterator<TimelineQuery> iter = queries.listIterator();
                        while (iter.hasNext()) {
                            TimelineQuery query = iter.next();
                            query.getProperties().setProperty("startDate", String.valueOf(state.getConfig().getDateRange().getMinimum().getTime()));
                            query.getProperties().setProperty("endDate", String.valueOf(state.getConfig().getDateRange().getMaximum().getTime()));
                            logger.info("--- adding query: " + query.getQueryClass() + ": " + query.getProperties());
                            tpss.getQueries().add(query);
                        }

                        state.addComponentServerState(tpss);
                    }

                    synchronized (configLock) {
                        logger.info("--- notifying timeline configuration ready");
                        ready = true;
                        configLock.notify();
                    }
                } else if (pe.getPropertyName().equals("update")) {
                    // TODO: handle update case
                } else if (pe.getPropertyName().equals("cancel")) {
                    // timeline creation was canceled
                }
                timelineCreationHUD.setVisible(false);
            }
        });
        timelineCreationHUD = mainHUD.createComponent(creationPanel);
        timelineCreationHUD.setPreferredLocation(Layout.CENTER);
        timelineCreationHUD.setName("Create Timeline");
        mainHUD.addComponent(timelineCreationHUD);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                timelineCreationHUD.setVisible(true);
            }
        });
    }

    public String getDisplayName() {
        return "Timeline";
    }

    public Image getPreviewImage() {
        URL url = TimelineCellFactory.class.getResource("resources/timeline_preview128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
