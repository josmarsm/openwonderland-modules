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
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.Unit;
import org.jdesktop.wonderland.modules.isocial.common.model.query.ResultQuery;

/**
 * A wrapper around ISocialDAO for a read-only instance
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ReadOnlyDAOWrapper implements ISocialDAO {
    private final ISocialDAO wrapped;

    public ReadOnlyDAOWrapper(ISocialDAO wrapped) {
        this.wrapped = wrapped;
    }

    public ISocialSecurityPolicy getSecurityPolicy() {
        return wrapped.getSecurityPolicy();
    }

    public void setSecurityPolicy(ISocialSecurityPolicy policy) {
        throw new PermissionDeniedException("Read only");
    }

    public void initialize() {
        throw new PermissionDeniedException("Read only");
    }

    public Collection<Cohort> getCohorts() {
        return wrapped.getCohorts();
    }

    public Cohort getCohort(String id) {
        return wrapped.getCohort(id);
    }

    public Cohort addCohort(Cohort cohort) {
        throw new PermissionDeniedException("Read only");
    }

    public Cohort updateCohort(Cohort cohort) {
        throw new PermissionDeniedException("Read only");
    }

    public Cohort removeCohort(String id) {
        throw new PermissionDeniedException("Read only");
    }

    public Collection<Unit> getUnits() {
        return wrapped.getUnits();
    }

    public Unit getUnit(String id) {
        return wrapped.getUnit(id);
    }

    public Unit addUnit(Unit unit) {
        throw new PermissionDeniedException("Read only");
    }

    public Unit updateUnit(Unit unit) {
        throw new PermissionDeniedException("Read only");
    }

    public Unit removeUnit(String unitId) {
        throw new PermissionDeniedException("Read only");
    }

    public Collection<Lesson> getLessons(String unitId) {
        return wrapped.getLessons(unitId);
    }

    public Lesson getLesson(String unitId, String lessonId) {
        return wrapped.getLesson(unitId, lessonId);
    }

    public Lesson addLesson(Lesson lesson) {
        throw new PermissionDeniedException("Read only");
    }

    public Lesson updateLesson(Lesson lesson) {
        throw new PermissionDeniedException("Read only");
    }

    public Lesson removeLesson(String unitId, String lessonId) {
        throw new PermissionDeniedException("Read only");
    }

    public Collection<Sheet> getSheets(String unitId, String lessonId) {
        return wrapped.getSheets(unitId, lessonId);
    }

    public Sheet getSheet(String unitId, String lessonId, String sheetId) {
            return wrapped.getSheet(unitId, lessonId, sheetId);
    }

    public Sheet addSheet(Sheet sheet) {
        throw new PermissionDeniedException("Read only");
    }

    public Sheet updateSheet(Sheet sheet) {
        throw new PermissionDeniedException("Read only");
    }

    public Sheet removeSheet(String unitId, String lessonId, String sheetId) {
        throw new PermissionDeniedException("Read only");
    }

    public Instance createInstance(String cohortId, String unitId, String lessonId) {
        throw new PermissionDeniedException("Read only");
    }

    public Collection<Instance> getInstances() {
        return wrapped.getInstances();
    }

    public Collection<Instance> getInstancesForCohort(String cohortId) {
        return wrapped.getInstancesForCohort(cohortId);
    }

    public Instance getInstance(String instanceId) {
        return wrapped.getInstance(instanceId);
    }

    public Instance removeInstance(String instanceId) {
        throw new PermissionDeniedException("Read only");
    }

    public Instance setCurrentInstance(String instanceId) {
        throw new PermissionDeniedException("Read only");
    }

    public Instance getCurrentInstance() {
        return wrapped.getCurrentInstance();
    }

    public Collection<Result> getResults(String instanceId) {
        return wrapped.getResults(instanceId);
    }

    public Collection<Result> getResults(ResultQuery query) {
        return wrapped.getResults(query);
    }

    public Result getResult(String instanceId, String resultId) {
        return wrapped.getResult(instanceId, resultId);
    }

    public Result addResult(Result result) {
        throw new PermissionDeniedException("Read only");
    }

    public Result updateResult(Result result) {
        throw new PermissionDeniedException("Read only");
    }

    public Result removeResult(String instanceId, String resultId) {
        throw new PermissionDeniedException("Read only");
    }

    public CohortState getCohortState(String cohortId, String key) {
        return wrapped.getCohortState(cohortId, key);
    }

    public CohortState setCohortState(CohortState value) {
        throw new PermissionDeniedException("Read only");
    }
    
    public CohortState removeCohortState(String cohortId, String key) {
        throw new PermissionDeniedException("Read only");
    }
    
    public <T> T runTransaction(Callable<T> transaction) throws Exception {
        return wrapped.runTransaction(transaction);
    }

    public <T extends ISocialModelBase> T copy(T obj) {
        throw new PermissionDeniedException("Read only");
    }

    public void addDAOListener(ISocialDAOListener listener) {
        throw new PermissionDeniedException("Read only");
    }

    public void removeDAOListener(ISocialDAOListener listener) {
        throw new PermissionDeniedException("Read only");
    }

    public void shutdown() {
        throw new PermissionDeniedException("Read only");
    }
}
