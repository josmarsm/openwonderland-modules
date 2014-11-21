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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenResult;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenSheet;

/**
 * Creates the Unit token view for students and guides. For students, it shows
 * the number of token they have received in each lesson. For guides, it shows 
 * the total number token given to all the students.
 *
 * @author Kaustubh
 */
@View(value = TokenSheet.class, roles = {Role.GUIDE, Role.ADMIN, Role.STUDENT})
public class StudentUnitTokenView extends JPanel implements SheetView, ResultListener,
        DockableSheetView {

    private ISocialManager manager;
    private Sheet sheet;
    private int rows;
    private HUDComponent hudComponent;
    private TokenStudentPanel currentLessonPanel, unitPanel;
    private JLabel currentLabel, unitLabel;
    private int maxLessonLimit, maxUnitLimit;

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        int maxLessonTokens = ((TokenSheet) sheet.getDetails()).getMaxLessonTokens();
        int maxStudents = ((TokenSheet) sheet.getDetails()).getMaxStudents();
        this.maxLessonLimit = maxLessonTokens * maxStudents;
        this.maxUnitLimit = ((TokenSheet) sheet.getDetails()).getMaxUnitTokens();

        String currentLesson = null;
        try {
            currentLesson = manager.getCurrentInstance().getLesson().getId();
        } catch (IOException ex) {
            Logger.getLogger(StudentUnitTokenView.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.addResultListener(sheet.getId(), this);
        try {

            //Add the current lesson results in the view.
            GridLayout gl = (GridLayout) getLayout();
            currentLessonPanel = new TokenStudentPanel(manager, sheet);
            String lessonName = manager.getCurrentInstance().getLesson().getName();
            currentLabel = new JLabel(currentLessonPanel.getImageIcon());
            currentLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            currentLabel.setHorizontalTextPosition(JLabel.CENTER);
            currentLabel.setText(lessonName);

            int lessonNameWidth = currentLabel.getFontMetrics(currentLabel.getFont()).stringWidth(lessonName) + 15;
            if (role != Role.STUDENT) {
                    rows++;
                    gl.setRows(rows);
                this.add(currentLabel);
                this.setPreferredSize(new Dimension(currentLessonPanel.getImageIcon().getIconWidth(),
                        currentLessonPanel.getImageIcon().getIconHeight() * rows));
                //                List<Sheet> sheets = manager.getCurrentInstance().getSheets();
                //                for (Sheet sheet1 : sheets) {
                //                    if (sheet1.getDetails() instanceof TokenSheet) {
                //                        Collection<Result> results = manager.getResults(sheet1.getId());
                //                        Result myResult = null;
                //                        for (Result result : results) {
                //                            if (result.getCreator().equals(manager.getUsername())) {
                //                                myResult = result;
                //                                currentLessonPanel.updateStudentTokens((TokenResult) result.getDetails());
                //                            }
                //                        }
                //                        if (myResult != null) {
                //                            results.remove(myResult);
                //                        }
                //
                //                    }
                //                }
                ArrayList<Result> results = (ArrayList<Result>) manager.getResults(sheet.getId());
                sortAndDisplayTokens(results, currentLessonPanel, maxLessonLimit);
                    }


            //Add the unit tokens view.

            unitPanel = new TokenStudentPanel(manager, sheet);
            rows++;
            gl.setRows(rows);
            String unitName = manager.getCurrentInstance().getUnit().getName();
            unitLabel = new JLabel(unitPanel.getImageIcon());
            unitLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            unitLabel.setHorizontalTextPosition(JLabel.CENTER);
            unitLabel.setText(unitName);

            int unitNameWidth = unitLabel.getFontMetrics(unitLabel.getFont()).stringWidth(unitName) + 15;
            this.add(unitLabel);
            this.setPreferredSize(new Dimension(unitPanel.getImageIcon().getIconWidth(),
                    unitPanel.getImageIcon().getIconHeight() * rows));
            ArrayList<Result> unitTokenResults = (ArrayList<Result>) manager.getCurrentUnitResults(sheet.getDetails());
            sortAndDisplayTokens(unitTokenResults, unitPanel, maxUnitLimit);
        } catch (IOException ex) {
            Logger.getLogger(StudentUnitTokenView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMenuName() {
        return "Unit Tokens";
    }

    public boolean isAutoOpen() {
        return ((TokenSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        hudComponent = hud.createComponent(this);
        hudComponent.setPreferredLocation(Layout.EAST);
        hudComponent.setTransparency(1.0f);
        return hudComponent;
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    /** Creates new form StudentUnitTokenView */
    public StudentUnitTokenView() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridLayout(0, 1));
    }// </editor-fold>//GEN-END:initComponents

    public boolean isDockable() {
        return ((TokenSheet) sheet.getDetails()).isDockable();
    }

    public void resultAdded(final Result result) {
        final TokenResult details = (TokenResult) result.getDetails();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                currentLessonPanel.resetImage();
                unitPanel.resetImage();
                try {
                    sortAndDisplayTokens((ArrayList<Result>) manager.getResults(sheet.getId()), currentLessonPanel, maxLessonLimit);
                    currentLabel.repaint();
                    ArrayList<Result> unitTokenResults = (ArrayList<Result>) manager.getCurrentUnitResults(sheet.getDetails());
                    sortAndDisplayTokens(unitTokenResults, unitPanel, maxUnitLimit);
                    unitLabel.repaint();
                } catch (IOException ex) {
                    Logger.getLogger(StudentUnitTokenView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void resultUpdated(final Result result) {
        final TokenResult details = (TokenResult) result.getDetails();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                currentLessonPanel.resetImage();
                unitPanel.resetImage();
                try {
                    sortAndDisplayTokens((ArrayList<Result>) manager.getResults(sheet.getId()), currentLessonPanel, maxLessonLimit);
                    currentLabel.repaint();
                    ArrayList<Result> unitTokenResults = (ArrayList<Result>) manager.getCurrentUnitResults(sheet.getDetails());
                    sortAndDisplayTokens(unitTokenResults, unitPanel, maxUnitLimit);
                    unitLabel.repaint();
                } catch (IOException ex) {
                    Logger.getLogger(StudentUnitTokenView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * 
     * @param results 
     */
    private void sortAndDisplayTokens(ArrayList<Result> results, TokenStudentPanel panel, int limit) {
        Collections.sort(results, new Comparator<Result>() {

            public int compare(Result r1, Result r2) {
                String creator1 = r1.getCreator().toLowerCase();
                String creator2 = r2.getCreator().toLowerCase();
                return Collator.getInstance().compare(creator1, creator2);
}
        });
        panel.updateTokens(results, limit, true);
    }
}
