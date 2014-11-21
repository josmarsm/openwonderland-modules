/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.common;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.ISocialConnectionType;
import org.jdesktop.wonderland.modules.isocial.common.ISocialStateUtils;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVRow;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVTable;

/**
 * Standard result read from a StandardSheet.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name = "standard-result")
public class StandardResult extends ResultDetails {
    private List<StandardAnswer> answers = new ArrayList<StandardAnswer>();

    public StandardResult() {
    }

    @XmlElement
    public List<StandardAnswer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<StandardAnswer> answers) {
        this.answers = answers;
    }
    
    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
        StandardSheet sheet = (StandardSheet) sd;
        
        int questionCount = 0;
        int answeredCount = 0;
        
        for (StandardQuestion question : sheet.getQuestions()) {
            // ignore "text" type, since those are instructions, not questions
            if (question.getType().equals("text")) {
                continue;
            }
            
            questionCount++;
            
            // find if there is an answer corresponding to this question
            for (StandardAnswer answer : getAnswers()) {
                if (answer.getId() == question.getId()) {
                    if(answer.getValueString().length()!=0)
                        answeredCount++;
                    break;
                }
            }
        }
        
        int percent = (int) (((float) answeredCount / (float) questionCount) * 100);
        return Collections.singletonList(answeredCount + " (" + percent + "%)");
    }

    @Override
    public List<CSVTable> getResultTables(Sheet sheet, Result result, boolean filter) {
        CSVTable table = new CSVTable();
        
        CSVRow student = new CSVRow();
        student.getRowData().add(result.getCreator());
        table.getRows().add(student);
        
        CSVRow header = new CSVRow();
        header.setStyleClass("ui-widget-header");
        header.getRowData().add("");
        header.getRowData().add("Question");
        header.getRowData().add("Answer");
        table.getRows().add(header);
        
        StandardSheet details = (StandardSheet) sheet.getDetails();
        for (StandardQuestion question : details.getQuestions()) {
            // ignore "text" type, since those are instructions, not questions
            if (question.getType().equals("text")) {
                continue;
            }
            
            CSVRow row = new CSVRow();
            row.getRowData().add("");
            if(question.getType().equals("audio")){
                String host = null;
                for (StandardAnswer answer : getAnswers()) {
                    if (answer.getId() == question.getId()) {
                        host = answer.getProperties().get("host");
                    }
                }
                //String abc = "<embed height='70px' src='http://"+host+"/webdav/content/groups/users/audiosheet/"+question.getProperties().get("fname")+".wav' autostart='false'></embed>";
                String abc = "<div style='border-bottom: 1px solid;'>"+question.getText()+"</div>"
                        + "<div style='float:left;padding-top: 4px;'><audio controls='controls'>"+
                             "<source src='http://"+host+"/webdav/content/groups/users/audiosheet/"+question.getProperties().get("fname")+".wav' type='audio/wav' />"+
                            "<embed src='http://"+host+"/webdav/content/groups/users/audiosheet/"+question.getProperties().get("fname")+".wav' autostart='false' height='70px' ></embed></audio>"
                            + "</div>";
                
                    row.getRowData().add(abc);
            } else {
                row.getRowData().add(question.getText());
            }
            
            // find if there is an answer corresponding to this question
            for (StandardAnswer answer : getAnswers()) {
                if (answer.getId() == question.getId()) {
                    if(answer.getValueString().length()!=0) {
                        row.getRowData().add(answer.getValueString());
                    }
                    break;
                }
            }
            
            table.getRows().add(row);
        }
        
        return Collections.singletonList(table);
    }
}
