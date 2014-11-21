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
package org.jdesktop.wonderland.modules.isocial.generic.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.ISocialConnectionType;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericAnswer;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericResult;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericSheet;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericQuestion;
import org.jdesktop.wonderland.modules.isocial.generic.common.MultipleChoiceQuestion;

/**
 * This view will need to consist of a JPanel with a card layout.  On initialize,
 * we'll grab the questions from the sheet and populate GenericQuestionPanels
 * which will then be added to the original JPanel via the card layout.
 */
/**
 * Generic student view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 * @author Kaustubh
 */
@View(value = GenericSheet.class, roles = Role.STUDENT)
public class GenericSheetStudentView
        implements SheetView, PropertyChangeListener, ResultListener, DockableSheetView {

    private static final Logger LOGGER =
            Logger.getLogger(GenericSheetStudentView.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/isocial/generic/client/Bundle");
    private ISocialManager manager;
    private Sheet sheet;
    private Role role;
    private GenericSheetViewPanel panel;
    private HUDComponent component;
    private List<GenericQuestion> questions;
    private List<JPanel> allQPanels;
    private boolean submitted = false;
    private ClientConnection connection;

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;
        this.connection = manager.getSession().getPrimarySession().getConnection(ISocialConnectionType.CONNECTION_TYPE);
        //createDummyQuestions();
        this.allQPanels = new ArrayList<JPanel>();

        boolean singleton = ((GenericSheet) sheet.getDetails()).getSingleton();
        Result prevResult = null;
        Sheet prevSheet = null;
        if (singleton) {
            try {
                prevResult = findPreviousValue(((GenericSheet) sheet.getDetails()).getName());
                prevSheet = findPrevSheet(((GenericSheet) sheet.getDetails()).getName());
            } catch (IOException ex) {
                Logger.getLogger(GenericSheetStudentView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (prevSheet != null) {
            this.sheet.setDetails(prevSheet.getDetails());
        }
        this.panel = new GenericSheetViewPanel(manager, this.sheet);
        this.panel.addPropertyChangeListener(this);
        this.questions = ((GenericSheet) this.sheet.getDetails()).getQuestions();
        createQuestionPanels(questions);
        //this.panel.setQCount(questions.size());
        panel.addCards(allQPanels);
        // see if we already have a result
        manager.addResultListener(sheet.getId(), this);
        try {

            if (prevResult != null) {
                panel.setResult(prevResult);
            } else {
                for (Result r : manager.getResults(this.sheet.getId())) {
                    if (r.getCreator().equals(manager.getUsername())) {
                        panel.setResult(r);
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading results", ioe);
        }
    }

    public String getMenuName() {
        GenericSheet details = (GenericSheet) sheet.getDetails();
        return details.getName();
    }

    public boolean isAutoOpen() {
        return ((GenericSheet) sheet.getDetails()).isAutoOpen();
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
            GenericResult details = panel.getResultDetails();
            try {
                Result r = manager.submitResult(sheet.getId(), details);
                panel.setResult(r);
                component.setVisible(false);
                //connection.getSession().send(connection, new ResultMessage(r.getId(), ResultMessage.Type.ADDED));
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

    private void createQuestionPanels(List<GenericQuestion> questionSet) {
        for (Iterator<GenericQuestion> it = questionSet.iterator(); it.hasNext();) {
            GenericQuestion genericQuestion = it.next();
            if (genericQuestion == null) {
                continue;
            }
            if (genericQuestion.getAnswers().size() == 1) {
                GenericAnswer answer = genericQuestion.getAnswers().get(0);
                if (answer.getQuestionTitle() == null || answer.getValue() == null) {
                    //this is open ended type of question
                    MultipleChoiceQuestion question = (MultipleChoiceQuestion) genericQuestion;
                    OEQPanel oeqPanel = new OEQPanel();
                    oeqPanel.setQuestion(question.getValue());
                    allQPanels.add(oeqPanel);
                }

            } else {
                //this is multiple choice type of question
                MultipleChoiceQuestion question = (MultipleChoiceQuestion) genericQuestion;
                boolean inclusive = question.getInclusive();
                if (inclusive) {
                    MCQInclusivePanel mcqiPanel = new MCQInclusivePanel();
                    mcqiPanel.setQuestion(question.getValue());
                    mcqiPanel.setAnswers(question.getAnswers());
                    allQPanels.add(mcqiPanel);
                } else {
                    MCQExclusivePanel mcqePanel = new MCQExclusivePanel();
                    mcqePanel.setQuestion(question.getValue());
                    mcqePanel.setAnswers(question.getAnswers());
                    allQPanels.add(mcqePanel);
                }
            }
        }
        ReviewPanel review = new ReviewPanel(questionSet);
        allQPanels.add(review);
    }

    /**
     * Determine if this sheet is dockable
     * @return
     */
    public boolean isDockable() {
        return ((GenericSheet) sheet.getDetails()).isDockable();
    }

    private Result findPreviousValue(String name) throws IOException {
        List instances = new ArrayList(manager.getInstances());
        Collections.reverse(instances);
        for (Iterator it = instances.iterator(); it.hasNext();) {
            Instance instance = (Instance) it.next();
            if (!(instance.getId().equals(manager.getCurrentInstance().getId()))) {
                if (instance.getUnit().getId().equals(manager.getCurrentInstance().getUnit().getId())) {
                    for (Sheet s : instance.getSheets()) {
                        // check if this sheet is a setter matching this getter
                        if (s.getName().equalsIgnoreCase(name)) {
                            return getSheetValue(instance, s);
                        }
                    }
                }
            }
        }

        // not found
        return null;
    }

    /**
     * Find a result for this user and extract its value
     */
    private Result getSheetValue(Instance i, Sheet s) throws IOException {
        // get all results
        Collection<Result> results = manager.getResultsForInstance(i.getId(), s.getId());

        // find result for this user
        for (Result result : results) {
            if (result.getCreator().equals(manager.getUsername())) {
                return result;
            }
        }

        // not found
        return null;
    }

    private Sheet findPrevSheet(String name) throws IOException {
        List instances = new ArrayList(manager.getInstances());
        Collections.reverse(instances);
        for (Iterator it = instances.iterator(); it.hasNext();) {
            Instance instance = (Instance) it.next();
            if (!(instance.getId().equals(manager.getCurrentInstance().getId()))) {
                if (instance.getUnit().getId().equals(manager.getCurrentInstance().getUnit().getId())) {
                    for (Sheet s : instance.getSheets()) {
                        // check if this sheet is a setter matching this getter
                        if (s.getName().equalsIgnoreCase(name)) {
                            return s;
                        }
                    }
                }
            }

        }
        // not found
        return null;
    }
}
