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
package org.jdesktop.wonderland.modules.isocial.web;

import com.sun.enterprise.security.web.integration.WebPrincipal;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.glassfish.security.common.Group;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.front.admin.AdminRegistration.RegistrationFilter;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVRow;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVResultCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVResultSheet;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVTable;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebUtils;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceUtils;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceUtils.StringCollection;

/**
 * Jersey servlet context that registers with the admin registry
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialServletContainer extends ServletContainer
    implements ServletContextListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ISocialServletContainer.class.getName());

    private static final String LESSON_REG_KEY = "__iSocialLessonAdminRegistration";

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        RegistrationFilter adminOrGuideFilter = new RegistrationFilter() {
            public boolean isVisible(HttpServletRequest request,
                                     HttpServletResponse response)
            {
                if (ISocialWebUtils.isAdmin(request)) {
                    return true;
                }

                // check whether this is a guide. We can't use the built-in
                // mechanisms because the servlet that is evaluating this
                // request (web-front/admin) doesn't know about the guide
                // group
                Principal p = request.getUserPrincipal();
                if (p instanceof WebPrincipal) {
                    Subject s = ((WebPrincipal) p).getSecurityContext().getSubject();
                    for (Group group : s.getPrincipals(Group.class)) {
                        if (group.getName().equals("guide")) {
                            return true;
                        }
                    }
                }

                return false;
            }
        };

        // register with the UI
        AdminRegistration lr = new AdminRegistration("iSocial Sheets",
                                                     "/isocial-sheets/isocial-sheets/lessons.jsp");
        lr.setFilter(adminOrGuideFilter);
        AdminRegistration.register(lr, context);
        context.setAttribute(LESSON_REG_KEY, lr);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        AdminRegistration lr = (AdminRegistration) context.getAttribute(LESSON_REG_KEY);
        if (lr != null) {
            AdminRegistration.unregister(lr, context);
        }
    }
    
    @Provider
    public static class MyJSONContextResolver extends ISocialResourceUtils.ISocialJSONContextResolver {
        public MyJSONContextResolver() throws Exception {
            super();
        }
    }
    
    @Provider
    public static class MyXMLContextResolver extends ISocialResourceUtils.ISocialXMLContextResolver {
        public MyXMLContextResolver() throws Exception {
            super();
        }
    }
    
    /**
     * This provider handles serializing String collections to JSON
     */
    @Provider
    public static class CollectionBodyWriter implements MessageBodyWriter<StringCollection> {
        public boolean isWriteable(Class<?> clazz, Type type, Annotation[] antns,
                                   MediaType mt)
        {
            return StringCollection.class.isAssignableFrom(clazz) &&
                    MediaType.APPLICATION_JSON_TYPE.isCompatible(mt);
        }

        public long getSize(StringCollection t, Class<?> clazz, Type type,
                            Annotation[] antns, MediaType mt)
        {
            return -1;
        }

        public void writeTo(StringCollection t, Class<?> clazz, Type type,
                            Annotation[] antns, MediaType mt,
                            MultivaluedMap<String, Object> mm,
                            OutputStream out)
            throws IOException, WebApplicationException
        {
            PrintStream ps = new PrintStream(out);
            ps.append("[");

            boolean first = true;

            for (String s : t) {
                if (first) {
                    first = false;
                } else {
                    ps.append(",");
                }

                ps.append("\"" + s + "\"");
            }
            ps.append("]");
            ps.flush();
        }
    }

    /**
     * This provider handles serializing CSVResultTable objects to csv files
     */
    @Provider
    public static class CSVBodyWriter
            implements MessageBodyWriter<CSVResultCollection>
    {
        public boolean isWriteable(Class<?> clazz, Type type, Annotation[] antns,
                                   MediaType mt)
        {
            MediaType csvType = new MediaType("application", "zip");
            return CSVResultCollection.class.isAssignableFrom(clazz) &&
                    csvType.isCompatible(mt);
        }

        public long getSize(CSVResultCollection t,
                            Class<?> clazz, Type type,
                            Annotation[] antns, MediaType mt)
        {
            return -1;
        }

        public void writeTo(CSVResultCollection t,
                            Class<?> clazz, Type type,
                            Annotation[] antns, MediaType mt,
                            MultivaluedMap<String, Object> mm,
                            OutputStream out)
            throws IOException, WebApplicationException
        {
            ZipOutputStream zos = new ZipOutputStream(out);
            for (CSVResultSheet sheet : t.getSheets()) {
                ZipEntry ze = new ZipEntry(getFileName(sheet));
                zos.putNextEntry(ze);
                writeTo(zos, sheet);
                zos.closeEntry();
            }
            zos.close();
        }

        private void writeTo(OutputStream out, CSVResultSheet s) {
            CSVPrintStream ps = new CSVPrintStream(new PrintStream(out));

            // header
            ps.append("View Results").lf();
            ps.append("Cohort").append(s.getCohort()).lf();
            ps.append("Date").append(s.getInstance()).lf();
            ps.append("Unit").append(s.getUnit()).lf();
            ps.append("Lesson").append(s.getLesson()).lf();
            ps.append("Sheet").append(s.getSheet()).lf();
            ps.lf();
            
            if (s.getTables().isEmpty()) {
                return;
            }
            
            // summary table
            CSVTable summaryTable = s.getTables().get(0);
            ps.append("Summary").lf();
            for (CSVRow row : summaryTable.getRows()) {
                ps.append("");
                for (String value : row.getRowData()) {
                    ps.append(value);
                }
                ps.lf();
            }
            ps.lf();
            
            // other tables
            for (int i = 1; i < s.getTables().size(); i++) {
                CSVTable table = s.getTables().get(i);
                
                for (CSVRow row : table.getRows()) {
                    for (String value : row.getRowData()) {
                        ps.append(value);
                    }
                    ps.lf();
                }
                ps.lf();
            }
            
            ps.flush();
        }

        private String getFileName(CSVResultSheet s) {
            return fileName(s.getCohort()) + "-" + fileName(s.getUnit()) + "-" +
                   fileName(s.getLesson()) + "-" + fileName(s.getSheet()) + "-" +
                   fileName(s.getInstance()) + "-" + s.getSheetId() + "-" +
                   s.getInstanceId() + ".csv";
        }

        private String fileName(String s) {
            return s.replaceAll("\\W", "_");
        }
        
        private static class CSVPrintStream {
            private final PrintStream ps;
            
            public CSVPrintStream(PrintStream ps) {
                this.ps = ps;
            }
            
            public CSVPrintStream append(String value) {
                ps.append(quote(value)).append(",");
                return this;
            }
            
            public CSVPrintStream lf() {
                ps.append("\n");
                return this;
            }
            
            public void flush() {
                ps.flush();
            }
            
            private String quote(String s) {
                s = s.replace("\"", "\"\"");
                return "\"" + s + "\"";
            }
        }
    }
}
