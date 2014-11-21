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
package org.jdesktop.wonderland.modules.isocial.tokensheet.client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.ResultType;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenResult;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenSheet;

/**
 *
 * @author Kaustubh
 */
@View(value = TokenSheet.class, roles = Role.STUDENT)
public class PassStrikesStudentView implements SheetView, ResultListener {

    private ISocialManager manager;
    private Sheet sheet;
    private Role role;
    private PassStrikeStudentPanel panel;
    private HUDComponent component;
    private static final Logger LOGGER =
            Logger.getLogger(PassStrikesStudentView.class.getName());
    //private String url = "/org/jdesktop/wonderland/modules/isocial/tokensheet/client/resources/pass_strike.png";
    private String url = "/org/jdesktop/wonderland/modules/isocial/tokensheet/client/resources/NewPassStrike.png";
    private JLabel passStrikeLabel;

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;

        this.panel = new PassStrikeStudentPanel(url);
        manager.addResultListener(sheet.getId(), this);
        try {
            for (Result r : manager.getResults(sheet.getId())) {
                if (r.getCreator().equals(manager.getUsername())) {
                    panel.updateStudentStrikesPasses((TokenResult) r.getDetails());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PassStrikesStudentView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMenuName() {
        return ((TokenSheet) sheet.getDetails()).getName();
    }

    public boolean isAutoOpen() {
        //return ((TokenSheet) sheet.getDetails()).isAutoOpen();
        return true;
    }

    public HUDComponent open(HUD hud) {
        ImageIcon imageIcon = panel.getImageIcon();
        passStrikeLabel = new JLabel(imageIcon);
        passStrikeLabel.setOpaque(false);
        component = hud.createComponent(passStrikeLabel);
        passStrikeLabel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent me) {
            }

            public void mousePressed(MouseEvent me) {
                System.out.println("Clicked on Pass-Strike view");
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }
        });
        //component = hud.createImageComponent(imageIcon);
        component.setDecoratable(false);
        component.setPreferredLocation(Layout.NORTHEAST);
        component.setTransparency(1.0f);
        return component;
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            if (result.getDetails() instanceof TokenResult) {
                TokenResult tResult = (TokenResult) result.getDetails();
                if (tResult.getType() == ResultType.PASS_INC) {
                    TokenSoundPlayer.getInstance().playPassSound();
                } else if (tResult.getType() == ResultType.STRIKE_INC) {
                    TokenSoundPlayer.getInstance().playStrikeSound();
                }
            }
            panel.resetImage();
            panel.updateStudentStrikesPasses((TokenResult) result.getDetails());
            passStrikeLabel.repaint();
        }
    }

    public void resultUpdated(final Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            if (result.getDetails() instanceof TokenResult) {
                TokenResult tResult = (TokenResult) result.getDetails();
                if (tResult.getType() == ResultType.PASS_INC) {
                    TokenSoundPlayer.getInstance().playPassSound();
                } else if (tResult.getType() == ResultType.STRIKE_INC) {
                    TokenSoundPlayer.getInstance().playStrikeSound();
                }
            }

            panel.resetImage();
            panel.updateStudentStrikesPasses((TokenResult) result.getDetails());
            passStrikeLabel.repaint();
        }
    }
}
