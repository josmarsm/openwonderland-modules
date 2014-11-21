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

import javax.ws.rs.core.SecurityContext;
import org.jdesktop.wonderland.modules.isocial.common.model.Cohort;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultMetadata.Visibility;

/**
 * Default implementation of security policy.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class DefaultSecurityPolicyImpl implements ISocialSecurityPolicy {
    public void checkRead(ISocialModelBase obj, SecurityContext context) {
        // admins can always read
        if (isAdmin(context)) {
            return;
        }

        if (obj instanceof Result) {
            // only guides for the relevant cohort can read results
            checkRead((Result) obj, context);
        } else {
            // default is to allow permission
        }
    }

    public void checkWrite(ISocialModelBase obj, SecurityContext context) {
        // admins can always write
        if (isAdmin(context)) {
            return;
        }

        if (obj instanceof Result) {
            // writing is only allowed to the current instance
            checkWrite((Result) obj, context);
        } else if (obj instanceof Cohort) {
            // only admins can make changes to a cohort
            throw new PermissionDeniedException();
        } else if (!isGuide(context)) {
            // default is that guides may write, but not students
            throw new PermissionDeniedException();
        }
    }

    /**
     * Check whether reading is permitted for a result
     */
    protected void checkRead(Result result, SecurityContext context) {
        ISocialDAO dao = ISocialDAOFactory.getReadOnlyInstance();
        
        // get the instance this result is associated with
        Instance instance = dao.getInstance(result.getInstanceId());
        if (instance == null) {
            // shouldn't happen
            throw new PermissionDeniedException("No such instance: " +
                                                result.getInstanceId());
        }

        // get the cohort this result is associated with
        Cohort cohort = dao.getCohort(instance.getCohortId());
        if (cohort == null) {
            // could happen if the cohort was deleted
            throw new PermissionDeniedException("Unable to read cohort " +
                    instance.getCohortId() + " for instance " +
                    instance.getId());
        }

        // guides can read all data for a cohort they are a guide in
        if(isGuideForCohort(cohort, context)) {
            return;
        }

        // find the permission for the result
        Visibility visibility = Visibility.DEFAULT;
        if (result.getMetadata() != null && 
            result.getMetadata().getVisibility() != null)
        {
            visibility = result.getMetadata().getVisibility();
        }
        
        switch (visibility) {
            case HIDDEN:
                // aways fail
                throw new PermissionDeniedException();
            case PUBLIC:
                // anyone is allowed -- always succeed
                return;
        }
        
        // apply default permissions -- users can only read their own results
        if (result.getCreator().equals(context.getUserPrincipal().getName())) {
            return;
        }
        
        // at this point, either a student is trying to read someone else's
        // result, a guide is trying to read a result from a different 
        // cohort, or a student is trying to read a historical result. All
        // of these are forbidden
        throw new PermissionDeniedException();
    }

    protected void checkWrite(Result result, SecurityContext context) {
        // if the result is not part of the current instance, we cannot write
        // it
        if (!isCurrentInstance(result.getInstanceId())) {
            throw new PermissionDeniedException("Result not part of active instance");
        }

        // if the writer is a student, make sure they are using their own
        // user id as the creator (if it is null, it will automatically be
        // populated with the right value)
        if (!isGuide(context) && result.getCreator() != null &&
            !result.getCreator().equals(context.getUserPrincipal().getName()))
        {
            throw new PermissionDeniedException("Only guides can specify an " +
                                                "alternate creator");
        }
    }

    private static boolean isCurrentInstance(String instanceId) {
        // get the current instance
        Instance currentInstance = ISocialDAOFactory.getReadOnlyInstance().getCurrentInstance();

        return (currentInstance != null &&
                instanceId.equals(currentInstance.getId()));
    }

    private static boolean isGuideForCohort(Cohort cohort,
                                            SecurityContext context)
    {
        if (!isGuide(context)) {
            return false;
        }

        // make sure this guide is listed in the list of guides
        for (String guide : cohort.getGuides()) {
            if (guide.equals(context.getUserPrincipal().getName())) {
                // the guide was found
                return true;
            }
        }

        return false;
    }

    private static boolean isAdmin(SecurityContext context) {
        return context.isUserInRole("admin");
    }

    private static boolean isGuide(SecurityContext context) {
        return context.isUserInRole("guide");
    }
}
