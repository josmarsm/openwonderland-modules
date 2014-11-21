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

import java.awt.Color;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
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
 * Token student view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 * @author Ryan Babiuch
 * @author Kaustubh
 */
@View(value = TokenSheet.class, roles = Role.STUDENT)
public class TokenStudentView
        implements SheetView, ResultListener {

    private static final Logger LOGGER =
            Logger.getLogger(TokenStudentView.class.getName());
    private ISocialManager manager;
    private Sheet sheet;
    private Role role;
    private TokenStudentPanel panel;
    private HUDComponent component;
    private Color color;
    private JLabel tokenLabel;
    private int maxLimit;

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;
        this.panel = new TokenStudentPanel(manager, sheet);
        int maxLessonTokens = ((TokenSheet) sheet.getDetails()).getMaxLessonTokens();
        int maxStudents = ((TokenSheet) sheet.getDetails()).getMaxStudents();
        this.maxLimit = maxStudents * maxLessonTokens;

        manager.addResultListener(sheet.getId(), this);
        try {
            sortAndDisplayTokens((ArrayList<Result>) manager.getResults(sheet.getId()), panel, maxLimit);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading results", ioe);
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
        tokenLabel = new JLabel(imageIcon);
        tokenLabel.setOpaque(false);
        component = hud.createComponent(tokenLabel);
        //component = hud.createImageComponent(imageIcon);
        component.setDecoratable(false);
        component.setPreferredLocation(Layout.NORTHWEST);
        component.setPreferredTransparency(1.0f);
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
                if (tResult.getType() == ResultType.TOKEN_INC) {
                    TokenSoundPlayer.getInstance().playTokenSound();
                }
            }
        }


        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                panel.resetImage();
                try {
                    sortAndDisplayTokens((ArrayList<Result>) manager.getResults(sheet.getId()), panel, maxLimit);
                    tokenLabel.repaint();
                } catch (IOException ex) {
                    Logger.getLogger(TokenStudentView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void resultUpdated(final Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            if (result.getDetails() instanceof TokenResult) {
                TokenResult tResult = (TokenResult) result.getDetails();
                if (tResult.getType() == ResultType.TOKEN_INC) {
                    TokenSoundPlayer.getInstance().playTokenSound();
                }
            }
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                panel.resetImage();
                try {
                    sortAndDisplayTokens((ArrayList<Result>) manager.getResults(sheet.getId()), panel, maxLimit);
                    tokenLabel.repaint();
                } catch (IOException ex) {
                    Logger.getLogger(TokenStudentView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    /**
     * 
     * @param results 
     */
    private void sortAndDisplayTokens(ArrayList<Result> results, TokenStudentPanel panel, int limit) {
        int maxLessonTokens = ((TokenSheet) sheet.getDetails()).getMaxLessonTokens();
        if (results.size() > 0 && maxLessonTokens > 0) {
            limit = results.size() * maxLessonTokens;
        }
        Collections.sort(results, new Comparator<Result>() {

            public int compare(Result r1, Result r2) {
                String creator1 = r1.getCreator().toLowerCase();
                String creator2 = r2.getCreator().toLowerCase();
                return Collator.getInstance().compare(creator1, creator2);
            }
        });
        panel.updateTokens(results, limit, false);
    }
}
