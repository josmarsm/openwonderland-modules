/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVRow;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVTable;

/** Result details for Question component.
 *
 * @author Vladimir Djurovic
 */
@ISocialModel
@XmlRootElement(name="question-result")
public class QuestionResultDetails extends ResultDetails{
    
    /**
     * Represents answer map, where each question is mapped to an answer.
     */
    private List<Question> questions;
    
    /** Date format for "Time completed" column */
    private SimpleDateFormat sdf;
    
    public QuestionResultDetails(){
        questions = new ArrayList<Question>();
        sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
    }

    @XmlElement
    public List<Question> getQuestions() {
        return questions;
    }
    

    /**
     * Returns summary result per student. Summary includes total number of answered questions and percentage
     * of answered question.
     * 
     * @param list 
     * @param sd
     * @return 
     */
    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
        int total =  questions.size();
        int answered = 0;
        long timestamp = 0;
        for(Question q : questions){
            if((q.getAnswerText() != null && ((q.isIncludeAnswer() && !q.getAnswerText().isEmpty())) || !q.isIncludeAnswer())){
                // if value is non-null, assume that question is answered
                answered++;
                if(q.getTimestamp() > timestamp){
                    timestamp = q.getTimestamp();
                }
            }
        }
        int percentage = (int)((float)answered/(float)total * 100);
        String result = answered + "/" + total + "(" + percentage + "%)";
        String completedOn = "Unknown";
        if(timestamp != 0){
            completedOn = sdf.format(new Date(timestamp));
        }
        String[] cols = new String[]{result, completedOn};
        return Arrays.asList(cols);
    }

    @Override
    public boolean isEmpty() {
        boolean status = true;
        for(Question q : questions){
            if((q.getAnswerText() != null && ((q.isIncludeAnswer() && !q.getAnswerText().isEmpty())) || !q.isIncludeAnswer())){
                status = false;
                break;
            }
        }
        return status;
    }
    
    

    /**
     * Returns details data per student.
     * 
     * @param sheet
     * @param result
     * @return 
     */
    @Override
    public List<CSVTable> getResultTables(Sheet sheet, Result result, boolean filter) {
         CSVTable table = new CSVTable();
        
        //student info row
        CSVRow student = new CSVRow();
        student.setStyleHint("background-color:darkgrey; color: white; font-weight: bold;");
        student.getRowData().add(result.getCreator());
        student.getRowData().add("");
        table.getRows().add(student);
        
        // header row
        CSVRow header = new CSVRow();
        header.setStyleHint("background-color: lightgrey");
        header.getRowData().add("");
        header.getRowData().add("Question");
        header.getRowData().add("Answer");
        table.getRows().add(header);
        
        int totalQuestions = questions.size();
        int totalAnswers = 0;
        // results per questio
        for(Question q : questions){
            CSVRow row = new CSVRow();
            row.getRowData().add(""); // empty row for indentation
            row.getRowData().add(q.getQuestionText()); // question
            row.getRowData().add(q.getAnswerText() != null ? q.getAnswerText() : ""); // answer
            if(q.getAnswerText() != null && ((q.isIncludeAnswer() && !q.getAnswerText().isEmpty()) || !q.isIncludeAnswer())){
                totalAnswers++;
            }
            table.getRows().add(row);
        }
        // add totals row
        CSVRow totalRow = new CSVRow();
        totalRow.getRowData().add(""); // indentation
        totalRow.getRowData().add(Integer.toString(totalQuestions));
        totalRow.getRowData().add(Integer.toString(totalAnswers));
        table.getRows().add(totalRow);
        
        return Collections.singletonList(table);
    }
    
    public void setAnswer(Question question, String answer){
        int index = -1;
        question.setAnswerText(answer);
        question.setTimestamp(new Date().getTime());
        for(int i = 0;i < questions.size();i++){
            if(questions.get(i).getCellId().equals(question.getCellId())){
                index = i;
                break;
            }
        }
        if(index >= 0){
            questions.set(index, question);
        } else {
            questions.add(question);
        }
    }
    
}
