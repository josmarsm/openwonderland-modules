/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */


package org.jdesktop.wonderland.modules.isocial.scavenger.common;

/**
 *  Represents current ranking of student. This class contains all information needed to rank
 *  student based on score and time.
 *
 * @author Vladimir Djurovic
 */
public class StudentRank implements Comparable<StudentRank> {
    
    private int score;
    private long time;
    private String username;

    public void setScore(int score) {
        this.score = score;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int compareTo(StudentRank o) {
        int result = 0;
        if(score > o.score){
            result = -1;
        } else if(score < o.score) {
            result = 1;
        } else {
            if(time < o.time){
                result = -1;
            } else if(time > o.time) {
                result = 1;
            }
        }
        
        return result;
    }

}
