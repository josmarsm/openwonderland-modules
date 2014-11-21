/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.sample.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.sample.common.SampleResult;
import org.jdesktop.wonderland.modules.isocial.sample.common.SampleSheet;

/**
 * Sample student view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 */
@View(value=SampleSheet.class, roles=Role.STUDENT)
public class SampleStudentView
        implements SheetView, PropertyChangeListener, ResultListener
{
    private static final Logger LOGGER =
            Logger.getLogger(SampleStudentView.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/isocial/sample/client/Bundle");

    private ISocialManager manager;
    private Sheet sheet;
    private Role role;
    private SampleViewPanel panel;
    private HUDComponent component;

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;

        this.panel = new SampleViewPanel(manager, sheet);
        this.panel.addPropertyChangeListener(this);

        // see if we already have a result
        manager.addResultListener(sheet.getId(), this);
        try {
            for (Result r : manager.getResults(sheet.getId())) {
                if (r.getCreator().equals(manager.getUsername())) {
                    panel.setResult(r);
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading results", ioe);
        }
    }

    public String getMenuName() {
        SampleSheet details = (SampleSheet) sheet.getDetails();
        return details.getName();
    }

    public boolean isAutoOpen() {
        return ((SampleSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        component = hud.createComponent(panel);
        return component;
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("submit")) {
            SampleResult details = panel.getResultDetails();

            try {
                Result r = manager.submitResult(sheet.getId(), details);
                panel.setResult(r);

                component.setVisible(false);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error submitting result", ioe);
            }
        }
    }

    public void resultAdded(final Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            panel.setResult(result);
        }
    }

    public void resultUpdated(final Result result) {
        
    }
}
