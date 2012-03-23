/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package uk.ac.essex.wonderland.modules.postercontrol.web.resources;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterControlConnection;
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterRecord;
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterRecordList;
import uk.ac.essex.wonderland.modules.postercontrol.web.servlet.PosterServletContainer;

/**
 * REST Web Service
 * Root context path is at http://<host:port>/postercontrol/postercontrol/resources/
 * This resource is authenticated, so first login saving cookies to a file:
 * curl --cookie-jar cookies.txt --data "username=<username>&password=<password>" 'http://localhost:8080/security-session-auth/security-session-auth/login?action=login'
 * then use the cookies.txt file to provide the login credentials
 *
 * @author Bernard Horan
 */

@Path("/")
public class PosterResource extends Application {
    private static final Logger logger = Logger.getLogger(PosterResource.class.getName());

    @Context
    private UriInfo context;
    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest request;

    /** Creates a new instance of the PosterResource (not sure if this is necessary */
    public PosterResource() {
        
    }

    /**
     * Retrieves representation of the posters in the virtual world
     * curl --cookie cookies.txt --header "Accept: text/html" http://localhost:8080/postercontrol/postercontrol/resources/posters/
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/html")
    @Path("posters/")
    public String getPostersHTML() {
        PosterControlConnection pcc = getPosterControlConnection();
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body><h1>List of Poster Cells</h1>");
        try {
            Collection<PosterRecord> records = pcc.getPosterRecords();
            //Very poor formatting
            for (PosterRecord posterRecord : records) {
                builder.append(posterRecord.getCellID());
                builder.append("(");
                builder.append(posterRecord.getCellName());
                builder.append(")");
                builder.append("-->");
                builder.append(posterRecord.getPosterContents());
            }
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        builder.append("</body></html>");
        return builder.toString();
    }

    /**
     * Retrieves representation of a specified in the virtual world
     * @param index is the index of the poster in the array of posters, NOT the cell id of the poster
     * curl --cookie cookies.txt --header "Accept: text/html" http://localhost:8080/postercontrol/postercontrol/resources/posters/0
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/html")
    @Path("posters/{index}/")
    public String getPosterHTML(@PathParam("index") int index) {
        PosterControlConnection pcc = getPosterControlConnection();
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body><h1>Details of Poster Cell: ");
        builder.append(index);
        builder.append("</h1>");
        try {
            List<PosterRecord> records = new ArrayList<PosterRecord>(pcc.getPosterRecords());
            if (index >= records.size()) {
                return Response.status(Response.Status.NOT_FOUND).build().toString();
            }
            PosterRecord posterRecord = records.get(index);
            //Very poor formatting
            builder.append(posterRecord.getCellID());
            builder.append("(");
            builder.append(posterRecord.getCellName());
            builder.append(")");
            builder.append("-->");
            builder.append(posterRecord.getPosterContents());
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        builder.append("</body></html>");
        return builder.toString();
    }

    /**
     * Retrieves representation of the posters in the virtual world
     * curl --cookie cookies.txt --header "Accept: application/xml" http://localhost:8080/postercontrol/postercontrol/resources/posters/
     * @return an instance of Response
     */
    @GET
    @Produces("application/xml")
    @Path("posters/")
    public Response getPostersXML() {
        PosterControlConnection pcc = getPosterControlConnection();
        PosterRecordList posterList = null;
        try {
            Collection<PosterRecord> records = pcc.getPosterRecords();
            posterList = new PosterRecordList(records);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return Response.ok(posterList).build();
    }

    /**
     * Retrieves representation of a specified in the virtual world
     * @param index is the index of the poster in the array of posters, NOT the cell id of the poster
     * curl --cookie cookies.txt --header "Accept: application/xml" http://localhost:8080/postercontrol/postercontrol/resources/posters/0
     * @return an instance of Response
     */
    @GET
    @Produces("application/xml")
    @Path("posters/{index}/")
    public Response getPosterXML(@PathParam("index") int index) {
        PosterControlConnection pcc = getPosterControlConnection();
        PosterRecord record = null;
        try {
            List<PosterRecord> records = new ArrayList<PosterRecord>(pcc.getPosterRecords());
            if (index >= records.size()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            record = records.get(index);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return Response.ok(record).build();
    }

    /**
     * PUT method for updating a poster
     * @param index is the index of the poster in the array of posters, NOT the cell id of the poster
     * @param data the new contents of the poster
     * curl -X PUT --cookie cookies.txt --header "Accept: application/xml" --header "Content-type:text/plain" --data "Updated Poster" http://localhost:8080/postercontrol/postercontrol/resources/posters/0
     * @return an instance of Response
     */
    @PUT
    @Produces("application/xml")
    @Path("posters/{index}/")
    @Consumes("text/plain")
    public Response putPosterText(@PathParam("index") int index, byte[] data) {
        PosterControlConnection pcc = getPosterControlConnection();
        PosterRecord record = null;
        try {
            List<PosterRecord> records = new ArrayList<PosterRecord>(pcc.getPosterRecords());
            if (index >= records.size()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            record = records.get(index);
            String newContents = new String(data, "UTF-8");
            pcc.setPosterContents(record.getCellID(), newContents);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Connection interrupted", ex);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "Unsupported character encoding", ex);
        }
        return Response.ok().build();
    }

    /**
     * DELETE method for removing a poster
     * @param index is the index of the poster in the array of posters, NOT the cell id of the poster
     * curl -X DELETE --cookie cookies.txt --header "Accept: application/xml" http://localhost:8080/postercontrol/postercontrol/resources/posters/0
     * @return an instance of Response
     */
    @DELETE
    @Path("posters/{index}/")
    @Produces("application/xml")
    public Response removePoster(@PathParam("index") int index) {
        PosterControlConnection pcc = getPosterControlConnection();
        PosterRecord record = null;
        try {
            List<PosterRecord> records = new ArrayList<PosterRecord>(pcc.getPosterRecords());
            if (index >= records.size()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            record = records.get(index);
            pcc.removePosterCell(record.getCellID());
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return Response.ok().build();
    }

    private PosterControlConnection getPosterControlConnection() {
        return (PosterControlConnection) servletContext.getAttribute(PosterServletContainer.POSTER_RESOURCE_CONN_ATTR);
    }

}
