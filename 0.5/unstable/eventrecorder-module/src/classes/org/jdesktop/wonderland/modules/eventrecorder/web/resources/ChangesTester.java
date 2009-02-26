/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventrecorder.web.resources;

import java.io.IOException;
import java.util.Date;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.modules.eventrecorder.server.ChangeDescriptor;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecorderUtils;

/**
 *
 * @author bh37721
 */
public class ChangesTester {

    public static void main(String args[]) throws IOException, JAXBException {
        String tapeName = "Untitled Tape";
        StringBuilder builder = new StringBuilder();
        builder.append("AAAAAAAAAA");
        EventRecorderUtils.createChangesFile(tapeName, new Date().getTime());
        ChangeDescriptor cd = new ChangeDescriptor(tapeName, new Date().getTime(), builder.toString());
        EventRecorderUtils.recordChange(cd);
        EventRecorderUtils.closeChangesFile(tapeName);
    }
}
