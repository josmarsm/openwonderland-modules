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
package org.jdesktop.wonderland.modules.isocial.weblib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.jdesktop.wonderland.modules.isocial.common.model.Cohort;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortState;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Lesson;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.query.ResultQuery;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.Unit;

/**
 * Implementation of ISocialDAO interface that stores data in memory. A listener
 * is also included that stored data in the filesystem.
 *
 * All data is stored as copies of the original. The original value of data
 * in the memory store is never directly read from or written to by user
 * calls. Therefore changes to the underlying values only happen when the
 * update() method is called, and not when the objects are directly modified.
 *
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class DefaultDAOImpl implements ISocialDAO {
    private static final Logger LOGGER =
            Logger.getLogger(DefaultDAOImpl.class.getName());

    private static final Random random = new Random();

    private final DAOFilesystemStore store = new DAOFilesystemStore();

    private final Map<String, CohortHolder> cohorts =
            new LinkedHashMap<String, CohortHolder>();

    private final Map<String, UnitHolder> units =
            new LinkedHashMap<String, UnitHolder>();

    private final Map<String, InstanceHolder> instances =
            new LinkedHashMap<String, InstanceHolder>();

    private final Set<String> ids = new TreeSet<String>();

    private final Set<ISocialDAOListener> listeners =
            new CopyOnWriteArraySet<ISocialDAOListener>();

    private ISocialSecurityPolicy policy;
    private String currentInstanceId;

    public synchronized ISocialSecurityPolicy getSecurityPolicy() {
        return policy;
    }

    public synchronized void setSecurityPolicy(ISocialSecurityPolicy policy) {
        this.policy = policy;
    }

    public void initialize() {
        // setup the storage as a listener for updates
        addDAOListener(store);

        // re-read all existing data
        try {
            // read existing data
            store.readAll(this);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading data", ioe);
        }
    }

    public synchronized List<Cohort> getCohorts() {
        // get all cohorts
        List<Cohort> out = new ArrayList<Cohort>(cohorts.values().size());
        for (CohortHolder holder : cohorts.values()) {
            out.add(copy(holder.getCohort()));
        }

        applyReadPermission(out);
        return out;
    }

    public synchronized Cohort getCohort(String id) {
        CohortHolder h = cohorts.get(id);
        if (h == null) {
            return null;
        }
        
        return copy(checkRead(h.getCohort()));
    }

    public synchronized Cohort addCohort(Cohort cohort) {
        // update the cohort
        cohort = added(cohort);

        checkWrite(cohort);

        // add it to the map and return it
        cohorts.put(cohort.getId(), new CohortHolder(copy(cohort)));
        fireAdded(cohort);
        return cohort;
    }

    public synchronized Cohort updateCohort(Cohort cohort) {
        CohortHolder holder = getCohortHolder(cohort.getId());
        Cohort current = holder.getCohort();
        
        checkWrite(current);

        cohort = updated(cohort, current);
        holder.setCohort(cohort);
        fireUpdated(current, cohort);

        return cohort;
    }

    public synchronized Cohort removeCohort(String id) {
        CohortHolder holder = cohorts.get(id);
        if (holder == null) {
            return null;
        }
        
        checkWrite(holder.getCohort());

        cohorts.remove(id);
        fireRemoved(holder.getCohort());
        return holder.getCohort();
    }

    public synchronized Collection<Unit> getUnits() {
        List<Unit> out = new ArrayList<Unit>(units.size());
        for (UnitHolder h : units.values()) {
            if (canRead(h.getUnit())) {
                out.add(copy(h.getUnit()));
            }
        }

        return out;
    }

    public synchronized Unit getUnit(String id) {
        UnitHolder h = units.get(id);
        if (h == null) {
            return null;
        }

        return copy(checkRead(h.getUnit()));
    }

    public synchronized Unit addUnit(Unit unit) {
        // update the unit
        unit = added(unit);

        checkWrite(unit);

        // add it to the map and return it
        units.put(unit.getId(), new UnitHolder(copy(unit)));
        fireAdded(unit);
        return unit;
    }

    public synchronized Unit updateUnit(Unit unit) {
        UnitHolder holder = getUnitHolder(unit.getId());
        Unit current = holder.getUnit();

        checkWrite(current);

        unit = updated(unit, current);
        holder.setUnit(copy(unit));
        fireUpdated(current, unit);

        return unit;
    }

    public synchronized Unit removeUnit(String id) {
        UnitHolder holder = units.get(id);
        if (holder == null) {
            return null;
        }

        checkWrite(holder.getUnit());

        units.remove(id);
        fireRemoved(holder.getUnit());
        return holder.getUnit();
    }

    public synchronized Collection<Lesson> getLessons(String unitId) {
        UnitHolder unit = getUnitHolder(unitId);

        List<Lesson> out = new ArrayList<Lesson>(unit.getLessons().size());
        for (LessonHolder h : unit.getLessons().values()) {
            if (canRead(h.getLesson())) {
                out.add(copy(h.getLesson()));
            }
        }

        return out;
    }

    public synchronized Lesson getLesson(String unitId, String lessonId) {
        UnitHolder unit = getUnitHolder(unitId);
        LessonHolder h = unit.getLessons().get(lessonId);
        if (h == null) {
            return null;
        }

        return copy(checkRead(h.getLesson()));
    }

    public synchronized Lesson addLesson(Lesson lesson) {
        UnitHolder unit = getUnitHolder(lesson.getUnitId());

        // update the lesson
        lesson = added(lesson);

        checkWrite(lesson);

        // add it to the map and return it
        unit.getLessons().put(lesson.getId(), new LessonHolder(copy(lesson)));
        fireAdded(lesson);
        return lesson;
    }

    public synchronized Lesson updateLesson(Lesson lesson) {
        LessonHolder holder = getLessonHolder(lesson.getUnitId(), lesson.getId());
        Lesson current = holder.getLesson();

        checkWrite(current);

        lesson = updated(lesson, current);
        holder.setLesson(copy(lesson));
        fireUpdated(current, lesson);

        return lesson;
    }

    public synchronized Lesson removeLesson(String unitId, String lessonId) {
        UnitHolder unit = getUnitHolder(unitId);
        LessonHolder holder = unit.getLessons().get(lessonId);
        if (holder == null) {
            return null;
        }

        checkWrite(holder.getLesson());

        unit.getLessons().remove(lessonId);
        fireRemoved(holder.getLesson());
        return holder.getLesson();
    }

    public synchronized Collection<Sheet> getSheets(String unitId, String lessonId) {
        LessonHolder lesson = getLessonHolder(unitId, lessonId);
        
        List<Sheet> out = new ArrayList<Sheet>(lesson.getSheets().size());
        for (Sheet sheet : lesson.getSheets().values()) {
            out.add(copy(sheet));
        }

        applyReadPermission(out);

        return out;
    }

    public synchronized Sheet getSheet(String unitId, String lessonId, String sheetId) {
        LessonHolder lesson = getLessonHolder(unitId, lessonId);
        return copy(checkRead(lesson.getSheets().get(sheetId)));
    }

    public synchronized Sheet addSheet(Sheet sheet) {        
        LessonHolder lesson = getLessonHolder(sheet.getUnitId(), sheet.getLessonId());

        // update the sheet
        sheet = added(sheet);

        checkWrite(sheet);

        // add it to the map and return it
        lesson.getSheets().put(sheet.getId(), copy(sheet));
        fireAdded(sheet);
        return sheet;
    }

    public synchronized Sheet updateSheet(Sheet sheet) {
        LessonHolder lesson = getLessonHolder(sheet.getUnitId(), sheet.getLessonId());

        Sheet current = lesson.getSheets().get(sheet.getId());
        if (current == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.NOT_FOUND).
                    entity("No sheet with id " + sheet.getId()).
                    build());
        }

        checkWrite(current);

        sheet = updated(sheet, current);
        lesson.getSheets().put(sheet.getId(), copy(sheet));
        fireUpdated(current, sheet);

        return sheet;
    }

    public synchronized Sheet removeSheet(String unitId, String lessonId, String sheetId) {
        LessonHolder lesson = getLessonHolder(unitId, lessonId);
        Sheet sheet = lesson.getSheets().get(sheetId);
        if (sheet == null) {
            return null;
        }

        checkWrite(sheet);

        lesson.getSheets().remove(sheetId);
        fireRemoved(sheet);
        return sheet;
    }

    public synchronized Instance createInstance(String cohortId, String unitId, String lessonId) {
        // create copies of the unit and lesson
        Unit unit = copy(getUnit(unitId));
        Lesson lesson = copy(getLesson(unitId, lessonId));

        // copy each sheet
        List<Sheet> sheets = new ArrayList<Sheet>();
        for (Sheet sheet : getSheets(unitId, lessonId)) {
            if (sheet.isPublished()) {
                sheets.add(copy(sheet));
            }
        }

        // create the instance
        Instance instance = new Instance(cohortId, unit, lesson, sheets);
        instance = added(instance);

        // make sure we are allowed to write
        checkWrite(instance);

        // add it to the list
        instances.put(instance.getId(), new InstanceHolder(instance));
        fireAdded(instance);
        return instance;
    }

    public synchronized Collection<Instance> getInstances() {
        List<Instance> out = new ArrayList<Instance>();
        for (InstanceHolder holder : instances.values()) {
            if (canRead(holder.getInstance())) {
                out.add(copy(holder.getInstance()));
            }
        }

        return out;
    }

    public Collection<Instance> getInstancesForCohort(String cohortId) {
        Collection<Instance> out = getInstances();
        for (Iterator<Instance> i = out.iterator(); i.hasNext();) {
            Instance instance = i.next();
            if (!instance.getCohortId().equals(cohortId)) {
                i.remove();
            }
        }

        return out;
    }

    public synchronized Instance getInstance(String instanceId) {
        InstanceHolder holder = instances.get(instanceId);
        if (holder == null) {
            return null;
        }

        return copy(checkRead(holder.getInstance()));
    }

    public synchronized Instance setCurrentInstance(String instanceId) {
        Instance cur = getInstance(instanceId);
        if (cur != null) {
            currentInstanceId = instanceId;
        
            // notify listeners
            fireCurrentInstance(instanceId);
        }

        return cur;
    }

    public synchronized Instance getCurrentInstance() {
        if (currentInstanceId == null) {
            return null;
        }

        return getInstance(currentInstanceId);
    }

    public synchronized Instance removeInstance(String instanceId) {
        InstanceHolder holder = instances.get(instanceId);
        if (holder == null) {
            return null;
        }

        checkWrite(holder.getInstance());

        instances.remove(instanceId);
        fireRemoved(holder.getInstance());
        return holder.getInstance();
    }

    public synchronized Collection<Result> getResults(String instanceId) {
        InstanceHolder instance = getInstanceHolder(instanceId);

        List<Result> out = new ArrayList<Result>(instance.getResults().size());
        for (Result result : instance.getResults().values()) {
            if(!result.getDetails().isEmpty()){
                out.add(copy(result));
            }
        }

        applyReadPermission(out);
        return out;
    }

    public Collection<Result> getResults(ResultQuery query) {
        Collection<Result> out = new LinkedList<Result>();
        
        Collection<Cohort> cs = getCohorts(query);
        for (Cohort c : cs) {
            Collection<Instance> is = getInstances(query, c);
            for (Instance i : is) {
                if (instanceMatches(query, i)) {
                    for (Result r : getResults(i.getId())) {
                        if (resultMatches(query, r)) {
                            if(!r.getDetails().isEmpty()){
                                out.add(r);
                            }
                        }
                    }
                }
            }
        }
        
        return out;
    }
    
    public synchronized Result getResult(String instanceId, String resultId) {
        InstanceHolder instance = getInstanceHolder(instanceId);
        Result r = instance.getResults().get(resultId);
        if (r == null) {
            return null;
        }

        return copy(checkRead(r));
    }

    public synchronized Result addResult(Result result) {
        InstanceHolder instance = getInstanceHolder(result.getInstanceId());

        // update the result
        result = added(result);

        checkWrite(result);

        // add it to the map and return it
        instance.getResults().put(result.getId(), copy(result));
        fireAdded(result);
        return result;
    }

    public synchronized Result updateResult(Result result) {
        InstanceHolder instance = getInstanceHolder(result.getInstanceId());
        Result current = instance.getResults().get(result.getId());
        if (current == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.BAD_REQUEST).
                    entity("No such result: " + result.getId()).
                    build());
        }

        checkWrite(current);

        result = updated(result, current);
        instance.getResults().put(result.getId(), copy(result));
        fireUpdated(current, result);

        return result;
    }

    public synchronized Result removeResult(String instanceId, String resultId) {
        InstanceHolder instance = getInstanceHolder(instanceId);
        Result result = instance.getResults().get(resultId);
        if (result == null) {
            return null;
        }

        checkWrite(result);

        instance.getResults().remove(resultId);
        fireRemoved(result);
        return result;
    }
    
    public CohortState getCohortState(String cohortId, String key) {
        CohortHolder holder = getCohortHolder(cohortId);
        CohortState state = holder.getState().get(key);
        if (state == null) {
            return null;
        }
        
        return copy(checkRead(state));
    }
    
   
    public CohortState setCohortState(CohortState state) {
        CohortHolder holder = getCohortHolder(state.getCohortId());

        // get the current value
        CohortState current = holder.getState().get(state.getKey());
        
        // update the state
        if (current == null) {
            state = added(state);
        } else {
            state = updated(state, current);
        }
        
        checkWrite(state);
        
        // add it to the map and return it
        holder.getState().put(state.getKey(), copy(state));
        
        // is this an add or an update?
        if (current == null) {
            fireAdded(state);
        } else {
            fireUpdated(current, state);
        }
        
        return state;
    }
    
    
    public CohortState removeCohortState(String cohortId, String key) {
        CohortHolder holder = getCohortHolder(cohortId);
        CohortState state = holder.getState().get(key);
        if (state == null) {
            return null;
        }
        
        checkWrite(state);

        holder.getState().remove(key);
        fireRemoved(state);
        return state;
    }

    public synchronized <T extends Object> T runTransaction(Callable<T> transaction)
        throws Exception
    {
        return transaction.call();
    }

    /**
     * Copy an object by serializing it and then deserializing it, resulting
     * in a new object with the same content.
     * @param obj the object to copy
     * @return a copy of the object
     */
    public <T extends ISocialModelBase> T copy(T obj) {
        try {
            // serialize into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();

            // read back from a byte array
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(baos.toByteArray()));
            return (T) ois.readObject();
          } catch (IOException ioe) {
              throw new WebApplicationException(ioe, Status.INTERNAL_SERVER_ERROR);
          } catch (ClassNotFoundException cnfe) {
              throw new WebApplicationException(cnfe, Status.INTERNAL_SERVER_ERROR);
          }
    }

    public void addDAOListener(ISocialDAOListener listener) {
        listeners.add(listener);
    }

    public void removeDAOListener(ISocialDAOListener listener) {
        listeners.remove(listener);
    }

    public synchronized void shutdown() {
        cohorts.clear();
        units.clear();

        store.shutdown();
    }

    protected void fireAdded(ISocialModelBase obj) {
        for (ISocialDAOListener listener : listeners) {
            listener.added(obj);
        }
    }

    protected void fireUpdated(ISocialModelBase oldObj, 
                               ISocialModelBase newObj)
    {
        for (ISocialDAOListener listener : listeners) {
            listener.updated(oldObj, newObj);
        }
    }

    protected void fireRemoved(ISocialModelBase obj) {
        for (ISocialDAOListener listener : listeners) {
            listener.removed(obj);
        }
    }

    protected void fireCurrentInstance(String instanceId) {
        for (ISocialDAOListener listener : listeners) {
            listener.currentInstanceChanged(instanceId);
        }
    }

    /////////////////////////////////////////////////
    // Methods used by the store when populating data

    synchronized void addCohortInternal(Cohort cohort) {
        cohorts.put(cohort.getId(), new CohortHolder(cohort));
    }

    synchronized void addUnitInternal(Unit unit) {
        units.put(unit.getId(), new UnitHolder(unit));
    }

    synchronized void addLessonInternal(Lesson lesson) {
        UnitHolder holder = getUnitHolder(lesson.getUnitId());
        holder.getLessons().put(lesson.getId(), new LessonHolder(lesson));
    }

    synchronized void addSheetInternal(Sheet sheet) {
        LessonHolder holder = getLessonHolder(sheet.getUnitId(), sheet.getLessonId());
        holder.getSheets().put(sheet.getId(), sheet);
    }

    synchronized void addInstanceInternal(Instance instance) {
        instances.put(instance.getId(), new InstanceHolder(instance));
    }

    synchronized void addResultInternal(Result result) {
        InstanceHolder holder = getInstanceHolder(result.getInstanceId());
        holder.getResults().put(result.getId(), result);
    }

    synchronized void addCohortStateInternal(CohortState state) {
        CohortHolder holder = getCohortHolder(state.getCohortId());
        holder.getState().put(state.getKey(), state);
    }
    
    synchronized void setCurrentInstanceInternal(String currentInstanceId) {
        this.currentInstanceId = currentInstanceId;
    }

    private Collection<Cohort> getCohorts(ResultQuery query) {
        return (query.getCohortId() == null) ?
            getCohorts() :
            Collections.singleton(getCohort(query.getCohortId()));
    }

    private Collection<Instance> getInstances(ResultQuery query, Cohort c) {
        return (query.getInstanceId() == null) ?
                getInstancesForCohort(c.getId()) :
                Collections.singleton(getInstance(query.getInstanceId()));
    }

    private boolean instanceMatches(ResultQuery query, Instance i) {
        if (query.getUnitId() != null &&
            !query.getUnitId().equals(i.getUnit().getId()))
        {
            return false;
        }

        if (query.getLessonId() != null &&
            !query.getLessonId().equals(i.getLesson().getId()))
        {
            return false;
        }

        return true;
    }

    private boolean resultMatches(ResultQuery query, Result r) {
        if (query.getStudentId() != null &&
            !query.getStudentId().equals(r.getCreator()))
        {
            return false;
        }

        if (query.getSheetId() != null &&
            !query.getSheetId().equals(r.getSheetId()))
        {
            return false;
        }

        return true;
    }

    /**
     * Get a CohortHolder from the map, and throw an IllegalArgumentException
     * if the cohort doesn't exist
     * @param cohortId the cohortId to get
     * @return the cohort holder
     * @throws IllegalArgumentException if the cohortId doesn't exist
     */
    private CohortHolder getCohortHolder(String cohortId) {
        CohortHolder out = cohorts.get(cohortId);
        if (out == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.NOT_FOUND).
                    entity("No such cohort: " + cohortId).build());
        }

        return out;
    }
    
    /**
     * Get a UnitHolder from the map, and throw an IllegalArgumentException
     * if the unit doesn't exist
     * @param unitId the unitId to get
     * @return the unit holder
     * @throws IllegalArgumentException if the unitId doesn't exist
     */
    private UnitHolder getUnitHolder(String unitId) {
        UnitHolder out = units.get(unitId);
        if (out == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.NOT_FOUND).
                    entity("No such unit: " + unitId).build());
        }

        return out;
    }

    /**
     * Get a LessonHolder from the map, and throw an IllegalArgumentException
     * if the lesson doesn't exist
     * @param unitId the unitId to get
     * @param lessonId the lessonId to get
     * @return the lesson holder
     * @throws IllegalArgumentException if the unitId or lessonId doesn't exist
     */
    private LessonHolder getLessonHolder(String unitId, String lessonId) {
        UnitHolder unit = getUnitHolder(unitId);
        LessonHolder out = unit.getLessons().get(lessonId);
        if (out == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.NOT_FOUND).
                    entity("No such lesson: " + lessonId).build());
        }

        return out;
    }

    /**
     * Get an InstanceHolder from the map, and throw an WebApplicationException
     * if the instance doesn't exist
     * @param instanceId the instanceId to get
     * @return the instance holder
     * @throws WebApplicationException if the instanceId doesn't exist
     */
    private InstanceHolder getInstanceHolder(String instanceId) {
        InstanceHolder out = instances.get(instanceId);
        if (out == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.NOT_FOUND).
                    entity("No such instance: " + instanceId).build());
        }

        return out;
    }

    private void assignNewId(ISocialModelBase obj) {
        // find a unique id
        String id;
        do {
            // create a 6 hex-digit random number
            int r = random.nextInt(0x1000000);

            // format it
            id = String.format("%06x", r);
        } while (ids.contains(id));

        // assign the new ID to the object, and make sure it doesn't
        // get reused
        ids.add(id);
        obj.setId(id);
    }

    /**
     * Add a new object to the system and assign it a unique id
     * @param obj the object to add
     * @return the object with a new, unique ID
     */
    private <T extends ISocialModelBase> T added(T obj) {
        // assign a new ID to this object
        assignNewId(obj);
        
        // update creator and modified times
        SecurityContext context = ISocialWebUtils.getSecurityContext();
        String principal = context.getUserPrincipal().getName();

        // only overwrite the creator if the value is null. For some objects,
        // guides may need to submit them as someone else
        if (obj.getCreator() == null) {
            obj.setCreator(principal);
        }

        obj.setCreated(new Date());

        obj.setUpdater(principal);
        obj.setUpdated(new Date());

        return obj;
    }

    private <T extends ISocialModelBase> T updated(T newObj, T oldObj) {
        // assign a new ID
        newObj.setId(oldObj.getId());

        // update creator and modified times
        SecurityContext context = ISocialWebUtils.getSecurityContext();
        String principal = context.getUserPrincipal().getName();

        newObj.setCreator(oldObj.getCreator());
        newObj.setCreated(oldObj.getCreated());

        newObj.setUpdater(principal);
        newObj.setUpdated(new Date());

        return newObj;
    }

    private void applyReadPermission(List<? extends ISocialModelBase> objs) {
        for (Iterator<? extends ISocialModelBase> i = objs.iterator(); i.hasNext();) {
            if (!canRead(i.next())) {
                i.remove();
            }
        }
    }

    private <T extends ISocialModelBase> T checkRead(T obj) {
        getSecurityPolicy().checkRead(obj, ISocialWebUtils.getSecurityContext());
        return obj;
    }

    private <T extends ISocialModelBase> T checkWrite(T obj) {
        getSecurityPolicy().checkWrite(obj, ISocialWebUtils.getSecurityContext());
        return obj;
    }

    private boolean canRead(ISocialModelBase obj) {
        try {
            checkRead(obj);
            return true;
        } catch (PermissionDeniedException pde) {
            return false;
        }
    }

    private boolean canWrite(ISocialModelBase obj) {
        try {
            checkWrite(obj);
            return true;
        } catch (PermissionDeniedException pde) {
            return false;
        }
    }

    /**
     * Hold a cohort and associated state
     */
    class CohortHolder {
        private Cohort cohort;
        private final Map<String, CohortState> state =
                new LinkedHashMap<String, CohortState>();
        
        CohortHolder(Cohort cohort) {
            this.cohort = cohort;
        }
        
        Cohort getCohort() {
            return cohort;
        }
        
        void setCohort(Cohort cohort) {
            this.cohort = cohort;
        }
        
        Map<String, CohortState> getState() {
            return state;
        }
    }
    
    /**
     * Hold a unit and associated lessons
     */
    class UnitHolder {
        private Unit unit;
        private final Map<String, LessonHolder> lessons =
                new LinkedHashMap<String, LessonHolder>();

        UnitHolder(Unit unit) {
            this.unit = unit;
        }

        Unit getUnit() {
            return unit;
        }

        void setUnit(Unit unit) {
            this.unit = unit;
        }

        Map<String, LessonHolder> getLessons() {
            return lessons;
        }
    }

    /**
     * Hold a lesson and associated sheets
     */
    class LessonHolder {
        private Lesson lesson;
        private final Map<String, Sheet> sheets =
                new LinkedHashMap<String, Sheet>();

        LessonHolder(Lesson lesson) {
            this.lesson = lesson;
        }

        Lesson getLesson() {
            return lesson;
        }

        void setLesson(Lesson lesson) {
            this.lesson = lesson;
        }

        Map<String, Sheet> getSheets() {
            return sheets;
        }
    }

    /**
     * Holds an instance and associated results
     */
    class InstanceHolder {
        private final Instance instance;
        private final Map<String, Result> results =
                new LinkedHashMap<String, Result>();

        InstanceHolder(Instance instance) {
            this.instance = instance;
        }

        public Instance getInstance() {
            return instance;
        }

        public Map<String, Result> getResults() {
            return results;
        }
    }
}
