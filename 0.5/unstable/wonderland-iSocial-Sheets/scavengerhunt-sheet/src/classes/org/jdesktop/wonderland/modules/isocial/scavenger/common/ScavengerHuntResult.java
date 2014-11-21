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
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

/**
 * Result details for Scavenger Hunt component.
 * 
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="scavenger-hunt-result")
public class ScavengerHuntResult extends ResultDetails {
    
    private List<ScavengerHuntItem> items;
    private SimpleDateFormat sdf;
    
    /** Duration of Scavenger hunt. */
    private long duration;
    
    public ScavengerHuntResult(){
        items = new ArrayList<ScavengerHuntItem>();
        sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
        duration = -1;
    }


    @XmlElement
    public List<ScavengerHuntItem> getItems() {
        return items;
    }

    public void setItems(List<ScavengerHuntItem> items) {
        this.items = items;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    /**
     * Returns summary results per students. Summary includes number of found items,
     * total number of hints and number of answered questions with percentage.
     * 
     * @param list
     * @param sd
     * @return 
     */
    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
       int totalItems = items.size();
       int found = 0;
       int hints = 0;
       int answered = 0;
       for(ScavengerHuntItem item : items){
           if(item.isFound()){
               found++;
           }
           hints += item.getHintUsed();
           if(item.getAnswer() != null && !item.getAnswer().isEmpty()){
               answered++;
           }
       }
       // calculate percentages
       
       int foundPct = (int)((float)found/(float)totalItems * 100);
       int answeredPct = (int)((float)answered/(float)totalItems * 100);
       List<String> results = new ArrayList<String>();
       // total and pct of found items
       results.add(found + "(" + foundPct + "%)"); 
       // total hints
       results.add(Integer.toString(hints));
       //total and pct of answers
       results.add(answered + "(" + answeredPct + "%)");
       Date completed = findTimeCompleted();
       if(completed != null){
           results.add(sdf.format(completed));
       } else {
           results.add("Not completed");
       }
       
       return results;
    }

    @Override
    public boolean isEmpty() {
        boolean status = true;
        for(ScavengerHuntItem item : items){
            if(item.isFound()){
                status = false;
                break;
            }
        }
        return status;
    }
    

    /**
     * Generate per-student detailed result table.
     * 
     * @param sheet 
     * @param result
     * @param filters list of 
     * @return CSV table
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
        header.getRowData().add("Item");
        header.getRowData().add("Found");
        header.getRowData().add("Hint");
        header.getRowData().add("Hint Level");
        header.getRowData().add("Question");
        header.getRowData().add("Snapshot");
        table.getRows().add(header);
        
        // iterate through items and set results for each
        int foundCount = 0;
        int totalHintLevel = 0;
        int totalAnswers = 0;
        for(ScavengerHuntItem item : items){
            CSVRow row = new CSVRow();
            row.getRowData().add(""); // empty row for indentation
            row.getRowData().add(item.getName());
            row.getRowData().add(item.isFound() ? "yes" : "no");
            if(item.isFound()){
                foundCount++;
            }
            row.getRowData().add(item.getHintUsed() > 0 ? "yes" : "no");
            row.getRowData().add(Integer.toString(item.getHintUsed()));
            totalHintLevel += item.getHintUsed();
            row.getRowData().add(item.getAnswer() == null ? "" : item.getAnswer());
            if(item.getAnswer() != null){
                totalAnswers++;
            }
            StringBuilder sb = new StringBuilder();
            if(item.getSnapshotImageUrl() != null && !item.getSnapshotImageUrl().isEmpty()){
                if(filter){
                    sb.append(item.getSnapshotImageUrl());
                } else {
                     sb.append("<a href=\"").append(item.getSnapshotImageUrl()).append("\" target=\"_blank\" >");
                    sb.append("<img src=\"").append(item.getSnapshotImageUrl()).append("\" width=100\" height=\"75\" />");
                    sb.append("</a>");
                }
               
            }
            row.getRowData().add(sb.toString());
            table.getRows().add(row);
        }
        int foundPct = (int)((float)foundCount/(float)items.size() * 100);
       int answeredPct = (int)((float)totalAnswers/(float)items.size() * 100);
        // set total row
        CSVRow totalRow = new CSVRow();
        totalRow.setStyleHint("background-color: lightgrey; font-weight: bold");
        totalRow.getRowData().add("");;
        totalRow.getRowData().add("Totals:");
        totalRow.getRowData().add(Integer.toString(foundCount) + "(" + foundPct + "%)");
        totalRow.getRowData().add("");
        totalRow.getRowData().add(Integer.toString(totalHintLevel));
        totalRow.getRowData().add(Integer.toString(totalAnswers) + "(" + answeredPct + "%)");
        totalRow.getRowData().add("");
        table.getRows().add(totalRow);
        
        
        return Collections.singletonList(table);
    }
    
    
    /**
     * Checks if item with specified name is found.
     * 
     * @param name item name
     * @return  <code>true</code> if item is found, <code>false</code> otherwise
     */
    public boolean isItemFound(String name){
        boolean status = false;
        for(ScavengerHuntItem it : items){
            if(it.getName().equals(name)){
                status = it.isFound();
                break;
            }
        }
        return status;
    }
    
    /**
     * Returns number of used hints for this item.
     * 
     * @param name item name
     * @return  number of hints used
     */
    public int getHintCountForItem(String name){
        int count = 0;
        for(ScavengerHuntItem it : items){
            if(it.getName().equals(name)){
                count = it.getHintUsed();
                break;
            }
        }
        return count;
    }
    
    /**
     * Find time stamp when scavenger hunt is completed. This will be the time stamp 
     * of the last item found (maximum time).
     * 
     * @return date of the last found item
     */
    private Date findTimeCompleted(){
        long time = -1;
        for(ScavengerHuntItem item : items){
            if(item.getTimestamp() != null && item.getTimestamp().getTime() > time){
                time = item.getTimestamp().getTime();
            }
        }
        Date result = null;
        if(time > 0){
            result = new Date(time);
        }
        return result;
    }
    
    /**
     * Calculate student score and elapsed time based on result data.
     * 
     * @return student rank
     */
    public StudentRank calculateRank(){
        int score = 0;
        for(ScavengerHuntItem item : items){
           score += item.getScore();
        }
        StudentRank rank = new StudentRank();
        rank.setScore(score);
        // here we set duration of the scavenger hunt
        rank.setTime(duration);
        return rank;
    }
}
