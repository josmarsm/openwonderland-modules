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
package org.jdesktop.wonderland.modules.isocial.client.view;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;

/**
 * Base interface for views of a sheet.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface SheetView {

    /**
     * Initialize the view with a sheet and the user's role
     * @param manager the iSocial manager used for requesting data
     * @param sheet the sheet to initialize this view with
     * @param role the role of the user viewing this sheet
     */
    public void initialize(ISocialManager manager, Sheet sheet, Role role);

    /**
     * Get the name of this view to display on menus
     * @return the name to display on menus
     */
    public String getMenuName();

    /**
     * Determine if this sheet should open automatically or only be visible
     * via the menu
     * @return true if the sheet should open automatically, or false if not
     */
    public boolean isAutoOpen();

    /**
     * Get the content of the sheet. The sheet content will be placed in the
     * HUD, using the size of the returned panel.
     * @param HUD the hud to use to create any component
     * @return the panel representing this sheet
     */
    public HUDComponent open(HUD hud);

    /**
     * Notification that this sheet has been closed.
     */
    public void close();
}
