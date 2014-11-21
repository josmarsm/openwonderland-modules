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
package org.jdesktop.wonderland.modules.isocial.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
//import org.jdesktop.wonderland.modules.dock.client.DockManager;
import org.jdesktop.wonderland.modules.isocial.client.ISocialConnection.ISocialConnectionListener;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.ISocialStateUtils;
import org.jdesktop.wonderland.modules.isocial.common.model.Cohort;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortState;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultMetadata;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;

/**
 * Manager for interacting with iSocial objects on web server
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public enum ISocialManager {

    INSTANCE;
    private static final Logger LOGGER =
            Logger.getLogger(ISocialManager.class.getName());
    private final ISocialConnectionListenerImpl isocialListener =
            new ISocialConnectionListenerImpl();
    private final Map<String, Collection<ResultListener>> listeners =
            new LinkedHashMap<String, Collection<ResultListener>>();
    // set during initialize
    private boolean initialized = false;
    private ServerSessionManager session;
    private ISocialConnection connection;
    private JAXBContext context;
    private Instance currentInstance = null;
    private Role currentRole = null;
    private final Map<String, Result> currentResults =
            new LinkedHashMap<String, Result>();
    private SheetManager sheetManager;

    /**
     * Initialize the iSocialManager with the correct session a
     * @param session
     * @param connection 
     */
    public synchronized void initialize(ServerSessionManager session,
            ISocialConnection connection) {
        this.initialized = true;
        this.session = session;
        this.connection = connection;

        // create the JAXB context
        context = ISocialStateUtils.createContext(session.getClassloader());

        // add a listener to the connection
        connection.addListener(isocialListener);

        // setup the initial sheets
        updateSheetManager();
    }

    /**
     * Check if the manager was initialized
     * @return true if the manager was initialized, or false if not
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Clean up this manager
     */
    public synchronized void cleanup() {
        if (connection != null) {
            connection.removeListener(isocialListener);
        }

        if (sheetManager != null) {
            sheetManager.cleanup();
        }

        initialized = false;
    }

    /**
     * Get the current instance from the web service
     * @return the current instance, or null if there is no current instance
     */
    public synchronized Instance getCurrentInstance()
            throws IOException {
        checkInitialized();

        if (currentInstance == null) {
            currentInstance = (Instance) getObject("instances/current");
        }

        return currentInstance;
    }

    /**
     * Get all results for the current instance from the web service
     * @return the current results, or an empty collection if there are
     * no current results
     */
    public synchronized Collection<Result> getCurrentResults()
            throws IOException {
        checkInitialized();

        if (currentResults.isEmpty()) {
            Instance i = getCurrentInstance();
            if (i == null) {
                throw new IllegalStateException("No current instance");
            }

            ISocialModelCollection<Result> results =
                    (ISocialModelCollection<Result>) getObject("results/" + i.getId());
            for (Result result : results.getItems()) {
                currentResults.put(result.getId(), result);
            }
        }

        // return a copy of the data
        return new ArrayList<Result>(currentResults.values());
    }

    /**
     * Get this user's role in the current instance.
     * @return the user's current role
     */
    public synchronized Role getCurrentRole() throws IOException {
        checkInitialized();

        if (currentRole == null) {
            // get the groups this user is eligible for
            Role role = connection.getBaseRole();
            if (role == Role.GUIDE) {
                // make sure we are a guide in the current cohort
                Instance i = getCurrentInstance();
                if (i == null) {
                    throw new IllegalStateException("No current instance");
                }

                Cohort cohort = (Cohort) getObject("cohorts/" + i.getCohortId());
                if (!cohort.getGuides().contains(session.getUsername())) {
                    // we are not a guide in the current cohort
                    role = Role.STUDENT;
                }
            }

            currentRole = role;
        }

        return currentRole;
    }

    /**
     * Convenience method to get the current username
     * @return the current username
     */
    public String getUsername() {
        return getSession().getUsername();
    }

    /**
     * Convenience method to get the current session
     * @return the current session
     */
    public synchronized ServerSessionManager getSession() {
        checkInitialized();
        return session;
    }

    /**
     * Get results from a sheet in the current instance
     * @param sheetId the id of the current sheet
     */
    public Collection<Result> getResults(String sheetId)
            throws IOException {
        Collection<Result> results = getCurrentResults();
        for (Iterator<Result> i = results.iterator(); i.hasNext();) {
            Result r = i.next();
            if (!r.getSheetId().equals(sheetId)) {
                i.remove();
            }
        }

        return results;
    }

    /**
     * Get a single result
     * @param resultId the id of the result to get
     */
    public Result getResult(String resultId) throws IOException {
        Instance i = getCurrentInstance();
        if (i == null) {
            throw new IllegalStateException("No current instance");
        }

        return (Result) getObject("results/" + i.getId() + "/" + resultId);
    }

    /**
     * Get previous instances for this cohort
     * @return a collection of instances for this cohort, sorted by date
     */
    public Collection<Instance> getInstances() throws IOException {
        Instance i = getCurrentInstance();
        if (i == null) {
            throw new IllegalStateException("No current instance");
        }

        ISocialModelCollection<Instance> ic = (ISocialModelCollection<Instance>) getObject("instances?cohortId=" + i.getCohortId());
        return ic.getItems();
    }

    /**
     * Get all the instances belonging to current unit.
     * @return
     * @throws IOException 
     */
    public Collection<Instance> getCurrentUnitInstances() throws IOException {
        Collection<Instance> instances = getInstances();
        String currentUnitId = getCurrentInstance().getUnit().getId();

        for (Iterator<Instance> i = instances.iterator(); i.hasNext();) {
            Instance instance = i.next();
            if (!instance.getUnit().getId().equals(currentUnitId)) {
                i.remove();
            }
        }

        return instances;
    }

    /**
     * Get all the result for current unit from all previous instances.
     * @return
     * @throws IOException 
     */
    public Collection<Result> getCurrentUnitResults(SheetDetails details) throws IOException {
        Collection<Instance> currentUnitInstances = getCurrentUnitInstances();
        ArrayList<Result> out = new ArrayList<Result>();

        for (Iterator<Instance> it = currentUnitInstances.iterator(); it.hasNext();) {
            Instance instance = it.next();
            List<Sheet> sheets = instance.getSheets();
            for (Sheet sheet : sheets) {
                if (sheet.getDetails().getTypeName().equals(details.getTypeName())) {
                    out.addAll(getResultsForInstance(instance.getId(), sheet.getId()));
                }
            }
        }
        return out;
    }

    /**
     * Get results from a previous instance
     * @param instanceId the id of the instance to get results from
     * @return a collection of results from that instance, sorted by date
     */
    public Collection<Result> getResultsForInstance(String instanceId, String sheetId)
            throws IOException {
        checkInitialized();

        ArrayList<Result> out = new ArrayList<Result>();

        ISocialModelCollection<Result> results = (ISocialModelCollection<Result>) getObject("results/" + instanceId);
        for (Result result : results.getItems()) {
            if (result.getSheetId().equals(sheetId)) {
                out.add(result);
            }
        }

        return out;
    }

    /**
     * Submit a new result
     */
    public Result submitResult(String sheetId, ResultDetails details)
            throws IOException 
    {
        return submitResult(sheetId, details, null);
    }
    
    /**
     * Submit a new result
     */
    public Result submitResult(String sheetId, ResultDetails details,
                               ResultMetadata metadata)
            throws IOException
    {   
        Instance i = getCurrentInstance();
        if (i == null) {
            throw new IllegalStateException("No current instance");
        }

        Result result = new Result(i.getId(), sheetId);
        result.setDetails(details);
        
        if (metadata != null) {
            result.setMetadata(metadata); 
        }
    
        // create the new object
        result = (Result) writeObject("results/" + i.getId() + "/new", result);
        return result;
    }

    /**
     * Submit a new result on behalf of a particular student. This is only
     * available to guides and administrators.
     *
     * @param creator the creator to use when creating the result
     * @param sheetId the id of the sheet to submit a result for
     * @param details the result details
     */
    public Result submitResultAs(String creator, String sheetId,
                                 ResultDetails details)
            throws IOException 
    {
        return submitResultAs(creator, sheetId, details, null);
    }
    
    /**
     * Submit a new result on behalf of a particular student. This is only
     * available to guides and administrators.
     *
     * @param creator the creator to use when creating the result
     * @param sheetId the id of the sheet to submit a result for
     * @param details the result details
     * @param metadata the result metadata
     */
    public Result submitResultAs(String creator, String sheetId,
                                 ResultDetails details, ResultMetadata metadata)
            throws IOException 
    {
        Instance i = getCurrentInstance();
        if (i == null) {
            throw new IllegalStateException("No current instance");
        }

        Result result = new Result(i.getId(), sheetId);
        result.setDetails(details);
        result.setCreator(creator);

        if (metadata != null) {
            result.setMetadata(metadata);
        }
        
        // create the new object
        result = (Result) writeObject("results/" + i.getId() + "/new", result);
        return result;
    }

    /**
     * Update a result
     */
    public Result updateResult(String resultId, ResultDetails details)
            throws IOException 
    {
        return updateResult(resultId, details, null);
    }
    
    /*
     * Update a result
     * 
     * @param resultId the id of the result to update
     * @param details the result details or null to leave the details unchanged
     * @param metatdata the result metadata or null to leave the metadata unchanged
     */
    public Result updateResult(String resultId, ResultDetails details,
                               ResultMetadata metadata)
            throws IOException
    {
        Instance i = getCurrentInstance();
        if (i == null) {
            throw new IllegalStateException("No current instance");
        }

        Result result = getResult(resultId);
        if (result == null) {
            throw new IllegalArgumentException("No such result: " + resultId);
        }

        if (details != null) {
            result.setDetails(details);
        }
        
        if (metadata != null) {
            result.setMetadata(metadata);
        }
        
        // update the object
        result = (Result) writeObject("results/" + i.getId() + "/" + resultId, result);
        return result;
    }

    /**
     * Add a listener that will be notified whenever a result is added to the
     * given sheet
     * @param sheetId the sheet to listen for results on
     * @param listener the listener to add
     */
    public synchronized void addResultListener(String sheetId,
            ResultListener listener) {
        Collection<ResultListener> rls = listeners.get(sheetId);
        if (rls == null) {
            rls = new LinkedHashSet<ResultListener>();
            listeners.put(sheetId, rls);
        }

        rls.add(listener);
    }

    /**
     * Remove a sheet listener
     * @param sheetId the sheet to listen for results on
     * @param listener the listener to remove
     */
    public synchronized void removeResultListener(String sheetId,
            ResultListener listener) {
        Collection<ResultListener> rls = listeners.get(sheetId);
        if (rls != null) {
            rls.remove(listener);

            if (rls.isEmpty()) {
                listeners.remove(sheetId);
            }
        }
    }

    /**
     * Get a cohort state value
     * @param key the key to read
     * @return a cohort state value of the given class
     */
    public CohortState getCohortState(String key)
            throws IOException {
        checkInitialized();

        try {
            String uri = new URI(null, null, "cohortState/" + key, null).toASCIIString();
            return (CohortState) getObject(uri);
        } catch (URISyntaxException use) {
            throw new IOException(use);
        }
    }

    /**
     * Set a cohort state value
     * @param key the key to write
     * @param value the value to write, or null to remove any values
     */
    public void setCohortState(String key, CohortState value)
            throws IOException {
        checkInitialized();

        try {
            String uri = new URI(null, null, "cohortState/" + key, null).toASCIIString();
            if (value == null) {
                deleteObject(uri);
            } else {
                writeObject(uri, value);
            }
        } catch (URISyntaxException use) {
            throw new IOException(use);
        }
    }

    /**
     * Get the view for a particular sheet
     * @param sheetId the id of the sheet to get views for
     * @return the views associated with the given sheet
     */
    public Collection<SheetView> getSheetViews(String sheetId) {
        checkInitialized();
        System.out.println("++ in getSheetViews ++");
        SheetManager manager = getSheetManager();
        return manager.getSheetViews(sheetId);
    }

    /**
     * Get the HUDComponent for a particular sheet
     * @param sheetId the id of the sheet to get components for
     * @return the HUDComponents associated with the given sheet
     */
    public Collection<HUDComponent> getHUDComponents(String sheetId) {
        checkInitialized();

        SheetManager manager = getSheetManager();
        return manager.getHUDComponents(sheetId);
    }

    /**
     * Fire an event when a new result is added
     * @param result the result was added
     */
    protected void fireResultAdded(final Result result) {
        ResultListener[] larr = new ResultListener[0];

        synchronized (this) {
            Collection<ResultListener> rls = listeners.get(result.getSheetId());
            if (rls != null) {
                larr = rls.toArray(new ResultListener[rls.size()]);
            }
        }

        final ResultListener[] farr = larr;

        // notify listeners on the AWT event thread
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (ResultListener listener : farr) {
                    listener.resultAdded(result);
                }
            }
        });
    }

    /**
     * Fire an event when a result is updated
     * @param result the result was updated
     */
    protected void fireResultUpdated(final Result result) {
        ResultListener[] larr = new ResultListener[0];

        synchronized (this) {
            Collection<ResultListener> rls = listeners.get(result.getSheetId());
            if (rls != null) {
                larr = rls.toArray(new ResultListener[rls.size()]);
            }
        }

        final ResultListener[] farr = larr;

        // notify listeners on the AWT event thread
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (ResultListener listener : farr) {
                    listener.resultUpdated(result);
                }
            }
        });
    }

    /**
     * Get an object from a URL.
     * @param url the URL (relative to the base of the service)
     * @return the object unmarshalled from the given URL
     */
    protected Object getObject(String url) throws IOException {
        return ISocialStateUtils.getObject(session.getServerURL(), url,
                session.getCredentialManager(),
                context);
    }

    /**
     * Get an object from a URL.
     * @param url the URL (relative to the base of the service)
     * @return the object unmarshalled from the given URL
     */
    protected Object writeObject(String url, Object obj)
            throws IOException {
        return ISocialStateUtils.writeObject(session.getServerURL(), url, obj,
                session.getCredentialManager(),
                context);
    }

    /**
     * Delete an object from a URL.
     * @param url the URL (relative to the base of the service)
     */
    protected void deleteObject(String url) throws IOException {
        ISocialStateUtils.deleteObject(session.getServerURL(), url,
                session.getCredentialManager());
    }

    protected synchronized void instanceChanged(String instanceId) {
        currentInstance = null;
        currentResults.clear();
        currentRole = null;

        updateSheetManager();
    }

    protected void resultAdded(String resultId) {
        try {
            Result result = getResult(resultId);

            synchronized (this) {
                currentResults.put(resultId, result);
            }

            fireResultAdded(result);
        } catch (IOException ioe) {
            // something is out of sync
            LOGGER.log(Level.FINE, "Error adding result " + resultId, ioe);
        }
    }

    protected void resultUpdated(String resultId) {
        try {
            Result result = getResult(resultId);

            synchronized (this) {
                currentResults.put(resultId, result);
            }

            fireResultUpdated(result);
        } catch (IOException ioe) {
            // something is out of sync
            LOGGER.log(Level.FINE, "Error updating result " + resultId, ioe);
        }
    }

    /**
     * Check if this manager is initialized. If it is, return normally. If not,
     * throw an IllegalStateException
     */
    private void checkInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
    }

    /**
     * Update the sheet manager
     */
    private synchronized void updateSheetManager() {
        if (sheetManager != null) {
            sheetManager.cleanup();
            sheetManager = null;
        }

        try {
            Instance i = getCurrentInstance();
            if (i != null) {
                sheetManager = new SheetManager(i.getSheets());
                sheetManager.addMenus();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error creating sheet manager", ioe);
        }
    }

    /**
     * Get the current sheet manager
     * @return the sheet manager, or null if there is no sheet manager
     */
    private synchronized SheetManager getSheetManager() {
        return sheetManager;
    }

    /**
     * Find all views for the given sheet
     * @param sheet the sheet to search for views of
     * @return a collection of views for the given sheet, or an empty collection
     * if no views are found
     */
    private Collection<SheetView> findViews(Sheet sheet) {
        List<SheetView> out = new ArrayList<SheetView>();

        ScannedClassLoader scl = session.getClassloader();

        Iterator<String> viewClasses = scl.getClasses(View.class).iterator();
        while (viewClasses.hasNext()) {
            String className = viewClasses.next();

            try {
                Class clazz = scl.loadClass(className);
                if (viewMatches(clazz, sheet, getCurrentRole())) {
                    SheetView view = (SheetView) clazz.newInstance();
                    view.initialize(this, sheet, getCurrentRole());
                    out.add(view);
                }
            } catch (ClassNotFoundException cnfe) {
                LOGGER.log(Level.WARNING, "Error finding class", cnfe);
            } catch (InstantiationException ie) {
                LOGGER.log(Level.WARNING, "Error instantiating class", ie);
            } catch (IllegalAccessException iae) {
                LOGGER.log(Level.WARNING, "Illegal access", iae);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error getting current role", ioe);
            }
        }

        return out;
    }

    /**
     * Return true if the given classes' view annotation matches the given
     * sheet type and role
     */
    private boolean viewMatches(Class clazz, Sheet sheet, Role role) {
        // find the @View annotation
        View annot = (View) clazz.getAnnotation(View.class);

        // check if the sheet class can be cast to the view type
        if (!annot.value().isAssignableFrom(sheet.getDetails().getClass())) {
            return false;
        }

        // check if there is a role annotation. If not, we are done
        if (annot.roles() == null || annot.roles().length == 0) {
            return true;
        }

        // check if the user's role matches any of the specified roles
        for (Role required : annot.roles()) {
            if (role.equals(required)) {
                return true;
            }
        }

        // if we get here, a role was specified but didn't match our role
        return false;
    }

    class ISocialConnectionListenerImpl implements ISocialConnectionListener {

        public void instanceChanged(String instanceId) {
            ISocialManager.this.instanceChanged(instanceId);
        }

        public void resultAdded(String resultId) {
            ISocialManager.this.resultAdded(resultId);
        }

        public void resultUpdated(String resultId) {
            ISocialManager.this.resultUpdated(resultId);
        }
    }

    class SheetManager {

        private final Collection<Sheet> sheets;
        private final Set<SheetViewMenuItem> menus =
                new LinkedHashSet<SheetViewMenuItem>();

        public SheetManager(Collection<Sheet> sheets) {
            this.sheets = sheets;
        }

        public synchronized Collection<SheetView> getSheetViews(String sheetId) {
            Collection<SheetView> out = new ArrayList<SheetView>();

            for (SheetViewMenuItem item : menus) {
                if (item.getSheetId().equals(sheetId)) {
                    out.add(item.getView());
                }
            }

            return out;
        }

        public synchronized Collection<HUDComponent> getHUDComponents(String sheetId) {
            Collection<HUDComponent> out = new ArrayList<HUDComponent>();

            for (SheetViewMenuItem item : menus) {
                if (item.getSheetId().equals(sheetId)) {
                    out.add(item.getHUDComponent());
                }
            }

            return out;
        }

        public void addMenus() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    for (Sheet sheet : sheets) {
                        for (SheetView view : findViews(sheet)) {
                            initializeView(sheet, view);
                        }
                    }
                }
            });
        }

        public void cleanup() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    for (SheetViewMenuItem item : menus) {
                        cleanupMenu(item);
                    }
                }
            });
        }

        private void initializeView(Sheet sheet, SheetView view) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            HUDComponent component = view.open(mainHUD);

            component.setVisible(false);
            
            // only update location if it is not set by the component
            if (component.getPreferredLocation() == null ||
                component.getPreferredLocation() == Layout.NONE)
            {
                component.setPreferredLocation(Layout.NORTHEAST);
            }
            
            component.setName(view.getMenuName());

            mainHUD.addComponent(component);

            final SheetViewMenuItem item = new SheetViewMenuItem(sheet.getId(), view,
                    component);

            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (item.getHUDComponent().isVisible()) {
                        item.hideHUD();
                    } else {
                        item.showHUD();
                    }
                }
            });

            component.addEventListener(new HUDEventListener() {

                public void HUDObjectChanged(HUDEvent event) {
                    if (event.getEventType() == HUDEventType.CLOSED
                            || event.getEventType() == HUDEventType.DISAPPEARED) {
                        item.hideHUD();
                    }
                }
            });
            

            synchronized (this) {
                menus.add(item);
            }

            if (view.isAutoOpen()) {
                item.showHUD();
            }

            if (view instanceof DockableSheetView) {
                DockableSheetView dockSheetView = (DockableSheetView) view;
                if (dockSheetView.isDockable()) {
//                    DockManager.getInstance().register(component);
                    JmeClientMain.getFrame().addToWindowMenu(item);
                } else {
                }
            } else {
                JmeClientMain.getFrame().addToWindowMenu(item);
            }
        }

        private void cleanupMenu(SheetViewMenuItem item) {
            item.hideHUD();

            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            mainHUD.removeComponent(item.getHUDComponent());

            JmeClientMain.getFrame().removeFromWindowMenu(item);

            item.getView().close();
        }
    }

    class SheetViewMenuItem extends JCheckBoxMenuItem {

        private final String sheetId;
        private final SheetView view;
        private final HUDComponent component;

        public SheetViewMenuItem(String sheetId, SheetView view,
                HUDComponent component) {
            super(view.getMenuName());

            this.sheetId = sheetId;
            this.view = view;
            this.component = component;
        }

        public String getSheetId() {
            return sheetId;
        }

        public SheetView getView() {
            return view;
        }

        public HUDComponent getHUDComponent() {
            return component;
        }

        public void showHUD() {
            component.setVisible(true);
            setSelected(true);
        }

        public void hideHUD() {
            component.setVisible(false);
            setSelected(false);
        }
    }
}
