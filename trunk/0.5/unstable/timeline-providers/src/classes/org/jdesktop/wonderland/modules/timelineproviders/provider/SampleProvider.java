/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timelineproviders.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProvider;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProviderContext;

/**
 * Sample query provider
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class SampleProvider implements TimelineProvider {
    private TimelineProviderContext context;
    private Thread testThread;


    public void initialize(final TimelineProviderContext context) {
        this.context = context;

        System.out.println("Got query: " + context.getQuery());
        System.out.println("Test property: " + context.getQuery().getProperties().getProperty("test"));
        
        testThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String imageURI = "wlhttp://icanhascheezburger.files.wordpress.com/2007/01/funny-pictures-monorail-cat1.jpg";

                    while (!Thread.interrupted()) {
                        Thread.sleep(10000);
                        DatedImage image = new DatedImage(new TimelineDate(), imageURI);
                        context.addResults(Collections.singleton((DatedObject) image));
                    }
                } catch (InterruptedException ie) {
                }

                System.out.println("Test thread exiting");
            }
        });
        testThread.start();
    }

    public void shutdown() {
        testThread.interrupt();
    }
}
