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

import java.util.Collection;
import java.util.concurrent.Callable;
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
 * Data access object for storing ISocial data. This is a singleton object
 * which can be used to store and retrieve iSocial objects. It is maintained
 * in the servlet context of the web application.
 * <p>
 * The default implementation class is ISocialDAOFilesystemImpl. Alternate
 * implementations can be specified using the system property
 * "org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAO.provider". The
 * value should be set to the fully qualified class name of an implementation
 * class with a public, no-argument constructor.
 *
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface ISocialDAO {
    /** the system property to read to find the provider class */
    public static final String PROVIDER_PROP =
                "org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAO.provider";

    /** the class name of the default provider */
    public static final String DEFAULT_PROVIDER =
                "org.jdesktop.wonderland.modules.isocial.weblib.DefaultDAOImpl";

    /**
     * Get the current security policy
     * @return the security policy
     */
    public ISocialSecurityPolicy getSecurityPolicy();

    /**
     * Set the current security policy. This must be called before any other
     * methods.
     * @param policy the updated security policy
     */
    public void setSecurityPolicy(ISocialSecurityPolicy policy);

    /**
     * Called to initialize the DAO after the security policy has been set.
     * This should read in all existing data to make it available via
     * the various getXXX() methods.
     */
    public void initialize();

    /**
     * Get all cohorts
     * @return all known cohorts
     */
    public Collection<Cohort> getCohorts();

    /**
     * Get a particular cohort by ID
     * @return the cohort with the given ID, or null if no cohort exists
     * with that id
     */
    public Cohort getCohort(String id);

    /**
     * Create a new cohort. Any id passed in will be replaced.
     * @param cohort the cohort to add
     * @return the new cohort, updated with a valid id.
     */
    public Cohort addCohort(Cohort cohort);

    /**
     * Update a cohort. The given cohort will be replaced with the
     * one that is passed in.
     * @param cohort the cohort to update
     * @return the updated cohort
     */
    public Cohort updateCohort(Cohort cohort);

    /**
     * Remove a cohort
     * @param id the id of the cohort to remove
     * @return the cohort that was removed
     */
    public Cohort removeCohort(String id);

    /**
     * Get all units
     * @return all units
     */
    public Collection<Unit> getUnits();

    /**
     * Get a unit by id
     * @return the unit with the given id
     */
    public Unit getUnit(String id);

    /**
     * Add a new unit. Any id passed in will be replaced
     * @param unit the unit to add
     * @return the new unit, updated with a valid id
     */
    public Unit addUnit(Unit unit);

    /**
     * Update a unit
     * @param unit the updated unit
     * @return the updated unit
     */
    public Unit updateUnit(Unit unit);

    /**
     * Remove a unit
     * @param id the id of the unit to remove
     * @return the unit that was removed
     */
    public Unit removeUnit(String unitId);

    /**
     * Get all lessons in the given unit
     * @param unitId the id of the unit to get lessons for
     * @return all lessons
     */
    public Collection<Lesson> getLessons(String unitId);

    /**
     * Get a lesson by id
     * @param unitId the id of the unit the lesson is a part of
     * @param lessonId the id of the lesson
     * @return the lesson with the given id
     */
    public Lesson getLesson(String unitId, String lessonId);

    /**
     * Add a new lesson. Any id passed in will be replaced
     * @param lesson the lesson to add
     * @return the new lesson, updated with a valid id
     */
    public Lesson addLesson(Lesson lesson);

    /**
     * Update a lesson in a unit
     * @param lesson the updated lesson
     * @return the updated lesson
     */
    public Lesson updateLesson(Lesson lesson);

    /**
     * Remove a lesson
     * @param unitId the unit to remove the lesson from
     * @param lessonId the id of the lesson to remove
     * @return the lesson that was removed
     */
    public Lesson removeLesson(String unitId, String lessonId);

    /**
     * Get all sheets in the given lesson
     * @param unitId the id of the unit to get sheets for
     * @param lessonId the id of the lesson to get sheets for
     * @return all sheets associated with the given lesson
     */
    public Collection<Sheet> getSheets(String unitId, String lessonId);

    /**
     * Get a sheet by id
     * @param unitId the id of the unit the sheet is a part of
     * @param lessonId the id of the lesson the sheet is part of
     * @param sheetId the id of the sheet
     * @return the sheet with the given id
     */
    public Sheet getSheet(String unitId, String lessonId, String sheetId);

    /**
     * Add a new sheet. Any id passed in will be replaced
     * @param sheet the sheet to add
     * @return the new sheet, updated with a valid id
     */
    public Sheet addSheet(Sheet sheet);

    /**
     * Update a sheet in a lesson
     * @param sheet the updated sheet
     * @return the updated sheet
     */
    public Sheet updateSheet(Sheet sheet);

    /**
     * Remove a sheet
     * @param unitId the unit to remove the sheet from
     * @param lessonId the lesson to remove the sheet from
     * @param sheetId the id of the sheet to remove
     * @return the sheet that was removed
     */
    public Sheet removeSheet(String unitId, String lessonId, String sheetId);

    /**
     * Create a new instance for the given cohort, unit and lesson
     * @param cohortId the id of the cohort for the instance
     * @param unitId the id of the unit for the instance
     * @param lesson the id of the lesson for the instance
     * @return a new Instance for the given cohort, unit and lesson
     */
    public Instance createInstance(String cohortId, String unitId, String lessonId);

    /**
     * Get all instances
     * @return a list of all instances
     */
    public Collection<Instance> getInstances();

    /**
     * Get all instances for the given cohort
     * @param cohortId the cohort to search for instances for
     * @return the list of all instances for the given cohort
     */
    public Collection<Instance> getInstancesForCohort(String cohortId);

    /**
     * Get an instance by id
     * @return the instance with the given id, or null if no instance exists
     * with that id
     */
    public Instance getInstance(String instanceId);

    /**
     * Remove an instance
     * @param instanceId the id of the instance to remove
     * @return the instance that was removed
     */
    public Instance removeInstance(String instanceId);

    /**
     * Set the current instance
     * @param instanceId id of the instance to make current
     * @return the instance that is now current
     */
    public Instance setCurrentInstance(String instanceId);

    /**
     * Get the current instance
     * @return the id of the current instance
     */
    public Instance getCurrentInstance();

    /**
     * Get all results in a given instance
     * @param instanceId the id of the instance to get results for
     * @return all results
     */
    public Collection<Result> getResults(String instanceId);

    /**
     * Get all results matching the given query
     * @param query the query that specifies what results should match
     * @return all results matching the given query
     */
    public Collection<Result> getResults(ResultQuery query);

    /**
     * Get a result by id
     * @param instanceId the id of the instance the result is a part of
     * @param resultId the id of the result
     * @return the result with the given id
     */
    public Result getResult(String instanceId, String resultId);

    /**
     * Add a new result. Any id passed in will be replaced
     * @param result the result to add
     * @return the new result, updated with a valid id
     */
    public Result addResult(Result result);

    /**
     * Update a result in an instance
     * @param result the updated result
     * @return the updated result
     */
    public Result updateResult(Result result);

    /**
     * Remove a result
     * @param instanceId the instance to remove the result from
     * @param resultId the id of the result to remove
     * @return the result that was removed
     */
    public Result removeResult(String instanceId, String resultId);

    /**
     * Get cohort-specific state
     * @param cohortId the cohort the state is relevant to
     * @param key the key for the state
     * @return the state object
     */
    public CohortState getCohortState(String cohortId, String key);
    
    /**
     * Set cohort-specific state
     * @param value the value for the state to set
     * @return the cohort state that was set
     */
    public CohortState setCohortState(CohortState value);
    
    /**
     * Remove cohort-specific state
     * @param cohortId the cohort the state is relevant to
     * @param key the key for the state
     * @return the state object that was removed
     */
    public CohortState removeCohortState(String cohortId, String key);
    
    /**
     * Perform the given runnable in the context of a transaction
     * @param runnable the runnable to run
     * @throws Exception if there is an error running the transaction
     */
    public <T extends Object> T runTransaction(Callable<T> transaction)
            throws Exception;

    /**
     * Copy an object, resulting in a new object with the same content.
     * @param obj the object to copy
     * @return a copy of the object
     */
    public <T extends ISocialModelBase> T copy(T obj);

    /**
     * Add a listener that will be notified of changes
     * @param listener the listener to add
     */
    public void addDAOListener(ISocialDAOListener listener);

    /**
     * Remove a listener
     * @param listener the listener to remove
     */
    public void removeDAOListener(ISocialDAOListener listener);

    /**
     * Shut down the DAO
     */
    public void shutdown();
}
