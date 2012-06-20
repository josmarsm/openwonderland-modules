/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *  
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.countdowntimer.common;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 *
 * @author Bernard Horan, bernard@essex.ac.uk
 */
public class Time implements Serializable {
    private int minutes;
    private int seconds;
    private boolean isValid = true;
    private final static DecimalFormat nFormat = new DecimalFormat("00"); 
    
    public Time(int minutes, int seconds) {
        if (minutes > 59) {
            throw new IllegalArgumentException("Must be 59 minutes or less");
        }
        if (seconds > 59) {
            throw new IllegalArgumentException("Must be 59 seconds or less");
        }
        this.minutes = minutes;
        this.seconds = seconds;
        
    }
    
    public void decrement() {
        seconds--;
        if (seconds < 0) {
            seconds = 59;
            minutes--;
        }
        if (minutes < 0) {
            isValid = false;
            seconds = 0;
            minutes = 0;
        }
    }
    
    @Override
    public String toString() {
        return nFormat.format(minutes) + ":" + nFormat.format(seconds);
    }
    
    public static void main(String args[]) {
        Time t = new Time(10, 0);
        while (t.isValid()) {
            System.out.println(t);
            t.decrement();
        }
    }

    public boolean isValid() {
        return isValid;
    }
    
}
