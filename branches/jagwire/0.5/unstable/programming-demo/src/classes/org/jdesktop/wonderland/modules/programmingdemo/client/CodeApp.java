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

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb.ControlChangeListener;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class CodeApp extends App2D implements ControlChangeListener {
    private static final Logger logger =
            Logger.getLogger(CodeApp.class.getName());

    private CodeWindow codeWindow;

    public CodeApp(String name, Vector2f pixelScale) {
        super (name, new ControlArbMulti(), pixelScale);
        controlArb.setApp(this);
        controlArb.addListener(this);
    }

    public void setWindow(CodeWindow codeWindow) {
        this.codeWindow = codeWindow;
    }

    public CodeWindow getWindow() {
        return codeWindow;
    }

    /**
     * The state of a control arb you are subscribed to may have changed.
     * The state of whether this user has control or the current set of
     * controlling users may have changed.
     *
     * @param controlArb The control arb that changed.
     */
    public void updateControl(ControlArb controlArb) {
        logger.warning("Update control: " + controlArb.hasControl());

        codeWindow.showControls(controlArb.hasControl());
    }
}
