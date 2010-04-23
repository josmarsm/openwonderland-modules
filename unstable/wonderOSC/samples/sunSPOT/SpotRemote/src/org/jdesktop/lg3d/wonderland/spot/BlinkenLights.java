/*
* Copyright (c) 2006 Sun Microsystems, Inc.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to 
* deal in the Software without restriction, including without limitation the 
* rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
* sell copies of the Software, and to permit persons to whom the Software is 
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in 
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
 **/

package org.jdesktop.lg3d.wonderland.spot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;

/**
 * simple utility to run some effects on the LEDs of the sensorboard
 * @author arshan
 * @author bob
 */

public class BlinkenLights {
    
    private LightRunner lightrunner = null;
    ITriColorLED[] leds;
    LEDColor myColor = LEDColor.RED;
    ILightSensor light = null;
    private boolean on = true;
    private boolean disabled = false;
    private int speed = 70;
    boolean lowPower = false;
    
    public BlinkenLights(EDemoBoard demo) {
    	leds = demo.getLEDs();
        lightsOn();
        light = demo.getLightSensor();
    }
    
    public void setColor(LEDColor clr ) {
        myColor = clr;
    }
    
    public void disable() {
        stopPsilon();
        disabled = true;
    }
    
    public void enable() {
        disabled = false;
    }
    
    // default to lowpower mode for now, should probably change that
    public void setLowPowerMode(boolean v) {
        lowPower = v;
        for ( int x = 0;  x < 8 ; x++ ) {
            leds[x].setColor(myColor);
        }
    }
    
    public void setLED(int led, int r , int g, int b) {
        if ( lightrunner != null ) {
            stopPsilon();
        }
        leds[led].setRGB(r,g,b);
    }
    
    
    public void setSpeed(int val){
        if ( val >= 100) { val = 99;}
        speed = 200 - (2*val);
    }
    
    public void lightsOff(){
        if ( on ) {
            for ( int x = 0; x < leds.length; x++) {
                leds[x].setOff();
            }
            on = false;
        }
    }
    
    public void lightsOn() {
        if (disabled) return;
        
        if ( ! on) {
            for ( int x = 0; x < leds.length; x++) {
                leds[x].setOn();
            }
            on = true;
        }
    }
    
    public void binaryDisplay(int x) {
        for ( int y = 0; y < 8 ; y++) {
            if ( (x &     0x1) > 0 ) {
                leds[y].setColor(myColor);
            } else {
                leds[y].setRGB(0,0,0);
            }
            x>>=1;
        }
    }
    
    
    public void setRGB(int r, int g , int b ) {
        myColor = new LEDColor(r,g,b);
    }
    
    private class LightRunner extends Thread {
        
        private boolean keepemrunning = true;
        
        public void stopThread() {
            keepemrunning = false;
        }
        
        private int reduce( int input ) {
            if ( input < 10 ) return 0;
            if ( input < 50 ) return input - (input/ 2);
            return input - ( input / 2 ) ;
        }
        
        private void decay() {
            for ( int x = 0 ; x < leds.length ; x++ ) {
                leds[x].setRGB(reduce(leds[x].getRed()),reduce(leds[x].getGreen()),reduce(leds[x].getBlue()));
            }
        }
        
        private void allHigh() {
            allLights(255,255,255);
        }
        
        int light_avg = 0;
        boolean light_state = false;
        
        private void lightThreshold(int val) {
            if ( val > 10  ) {
                if ( lowPower ) {
                    on = true;
                }
                else {
                    lightsOn();  
                }
            } else {
               
                    lightsOff();
                
            }
        }
        
        public void run() {
            int current = 0;
            int trend   = 1;
            
            if (disabled) return;
            
            for ( int x = 0 ; x < 8 ; x++ ) {
                leds[x].setOn();
                leds[x].setRGB(20,20,20);
                try {
                    Thread.sleep(70);
                } catch (Exception e ) {
                    // so we dont run the lights, no sweat
                }
                leds[x].setRGB(0,0,0);
            }
            boolean miserly = false;
            try {
                
                while (keepemrunning) {
                    
                    
                    //lightThreshold(light.getValue());
                    
                    if ( on ) {
                        
                        if (miserly) {
                           leds[0].setOn();
                           leds[0].setColor(myColor);
                           sleep(25);
                           leds[0].setOff();
                        }
                        else {
                            
                        
                        if (lowPower) {
                            leds[current].setOff();
                        }
                        
                        if ( trend == 1 ) current++;
                        else              current--;
                                                    
                        leds[current].setColor(myColor);
                                                    
                        if (lowPower) {
                            leds[current].setOn();
                            
                        } else {

                            decay();
                        }
                        if ( current == 7 ) trend  = 0;
                        if ( current == 0 ) trend  = 1;
                    }
                    } 
                    Thread.sleep(speed);
                }
            } catch (Exception e ) {
                // so we dont run the lights, no sweat
            }
            
            
        }
    };
    
    public void startPsilon()  {
        
        if ( disabled ) return;
        
        if ( lowPower ) {
            for ( int x = 0; x<8; x++) {
                leds[x].setColor(myColor);
            }
        }
        
        if ( lightrunner != null ) {
            lightrunner.stopThread();
            lightrunner = null;
        }
        
        lightrunner = new LightRunner();
        lightrunner.setPriority(Thread.MIN_PRIORITY);
        lightrunner.start();
    }
    
    public void stopPsilon() {
        
        if(lightrunner != null) {
	    	lightrunner.stopThread();
	        lightrunner = null;
        }
    }
    
    
    public void togglePsilon() {
        if ( lightrunner == null) {
            startPsilon();
        } else {
            stopPsilon();
        }
    }
    
    public boolean isPsilonRunning() {
    	return !(lightrunner == null);
    }
    
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {}
    }
    
    public void allLights( int r , int g, int b) {
        for ( int x = 0 ; x < leds.length ; x++ ) {
            leds[x].setRGB(r,g,b);
        }
    }
    
    /*
     * Generate a bar graph ont the LEDs to represent the value of num on a
     * scale from zero to limit in color
     * @param num The number to graph
     * @param limit The high end of the range.  The low end is assumed to be zero
     * @param color the color of the LEDs for the bar graph
     * @author bob
     */
    public void BarGraph( int num, int limit, LEDColor color ) {
        if ( num > limit ) num = limit;
        if ( num < 0 ) num = 0;
        int pos = (num * 8)/limit;
        int frac = ((num - ((pos*limit)/8)) * 2048)/limit;
        LEDColor rc = new LEDColor((color.red()*frac)/256,(color.green()*frac)/256,(color.blue()*frac)/256);
        
        for (int i = 0; i < 8; i++) {
            if (i < pos) {
                leds[i].setColor(color);
                leds[i].setOn();
            } else if (i == pos) {
                leds[i].setColor(rc);
                leds[i].setOn();
            } else {
                leds[i].setOff();
            }
        }
    }
    
    
    public void dblink() {
        blink(300);
        sleep(150);
        blink(300);
    }
    public void blink() {
        this.dblink();
    }
    
    public void blink(int dur) {
        int startem = 0;
        if ( lightrunner != null ) {
            stopPsilon();
            startem=1;
        }
        allLights(0,0,0);
        sleep(dur/2);
        allLights(0,0,100);
        sleep(dur/2);
        allLights(0,0,0);
        
        if ( startem == 1) {
            try {
                startPsilon();
            } catch (Exception e ) {}
        }
    }
    
};

