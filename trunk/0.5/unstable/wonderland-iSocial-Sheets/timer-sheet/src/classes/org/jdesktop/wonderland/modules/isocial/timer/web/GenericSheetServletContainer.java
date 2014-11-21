/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.timer.web;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ryan
 */
public class GenericSheetServletContainer extends ServletContainer
    implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("JUST TESTING INITIALIZED!!!");
    }
    
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("JUST TESTING DESTROYED!!!");
    }

}
