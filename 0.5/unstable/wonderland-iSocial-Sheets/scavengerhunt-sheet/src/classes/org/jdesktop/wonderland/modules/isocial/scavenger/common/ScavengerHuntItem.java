/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents an item that needs to be found in a Scavenger Hunt.
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "scavenger-hunt-item")
public class ScavengerHuntItem implements Serializable {
    
     /** Base score for found item. */
    private static final int ITEM_SCORE = 100;
    
    /** Deduction points for each hint. */
    private static final int HINT_DEDUCTION = 10;
    
    private String cellId;
    private String name;
    private List<String> hints;
    private int hintsUsed;
    private boolean giveUp;
    private String giveUpMessage;
    private String question;
    private boolean found;
    private String answer;
    private String snapshotImageUrl;
    // timestamp when item is found
    private Date timestamp;
    private boolean hintsExausted;
    
    /** Indicates whether to include answer area for this item. */
    private boolean includeAnswer;
    
    /** Indicates whether to include audio recording controls for this item. */
    private boolean includeAudio;
    
    private boolean userGaveUp;
    
    public ScavengerHuntItem(){
        hints = new ArrayList<String>();
        hintsUsed = 0;
        hintsExausted = false;
        includeAnswer = false;
        userGaveUp = false;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return the hints
     */
    @XmlElement
    public List<String> getHints() {
        return hints;
    }

    /**
     * @param hints the hints to set
     */
    public void setHints(List<String> hints) {
        this.hints = hints;
    }

    /**
     * @return the hintUsed
     */
    public int getHintUsed() {
        return hintsUsed;
    }

    /**
     * @param hintUsed the hintUsed to set
     */
    public void setHintUsed(int hintUsed) {
        this.hintsUsed = hintUsed;
    }

    /**
     * @return the giveUp
     */
    public boolean isGiveUp() {
        return giveUp;
    }

    /**
     * @param giveUp the giveUp to set
     */
    public void setGiveUp(boolean giveUp) {
        this.giveUp = giveUp;
    }

    /**
     * @return the giveUpMessage
     */
    public String getGiveUpMessage() {
        return giveUpMessage;
    }

    /**
     * @param giveUpMessage the giveUpMessage to set
     */
    public void setGiveUpMessage(String giveUpMessage) {
        this.giveUpMessage = giveUpMessage;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    @XmlTransient
    public String getCellId() {
        return cellId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public boolean isFound() {
        return found;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setSnapshotImageUrl(String snapshotImageUrl) {
        this.snapshotImageUrl = snapshotImageUrl;
    }

    public String getSnapshotImageUrl() {
        return snapshotImageUrl;
    }
    
    public String getCurrentHint(){
        if(hintsExausted){
            return giveUpMessage;
        }
        String hint = hints.get(hintsUsed);
        if(hint != null && !hint.isEmpty()){
            hintsUsed++;
        } else {
            hintsExausted = true;
        }
        return hint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setIncludeAnswer(boolean includeAnswer) {
        this.includeAnswer = includeAnswer;
    }

    public void setIncludeAudio(boolean includeAudio) {
        this.includeAudio = includeAudio;
    }

    public boolean isIncludeAnswer() {
        return includeAnswer;
    }

    public boolean isIncludeAudio() {
        return includeAudio;
    }

    public boolean isUserGaveUp() {
        return userGaveUp;
    }

    public void setUserGaveUp(boolean userGaveUp) {
        this.userGaveUp = userGaveUp;
    }
    
    /**
     * Update this item with values from supplied item. The following values can be updated:
     * <ul>
     *  <li>item name</li>
     *  <li>hints</li>
     *  <li>give up</li>
     *  <li>give up message</li>
     *  <li>question</li>
     * </ul>
     * 
     *  Question is updated only if item is not yet found, otherwise it stays the same.
     * @param item item with updated values
     * @return <code>true</code> if some of the values were changed, <code>false</code> otherwise
     */
    public boolean update(ScavengerHuntItem item){
        boolean updated = false;
        if(name != null && !name.equals(item.getName())){
            name = item.getName();
            updated = true;
        }
        if(hints != null && !hints.equals(item.getHints())){
            hints = item.getHints();
            updated = true;
        }
        if(giveUp != item.isGiveUp()){
            giveUp = item.isGiveUp();
            updated = true;
        }
        if(giveUpMessage != null && !giveUpMessage.equals(item.getGiveUpMessage())){
            giveUpMessage = item.getGiveUpMessage();
            updated = true;
        }
        if(question != null && !question.equals(item.getQuestion())){
            question = item.getQuestion();
            updated = true;
        }
        return updated;
    }
    
    /**
     * Returns a score achieved for this item. Score is calculated based on base score
     * and deduction for each hint used.
     * 
     * @return item score
     */
    public int getScore(){
        int score = ITEM_SCORE - (hintsUsed * HINT_DEDUCTION);
        if(!found){
            score = 0;
        }
        return score;
    }
    
}
