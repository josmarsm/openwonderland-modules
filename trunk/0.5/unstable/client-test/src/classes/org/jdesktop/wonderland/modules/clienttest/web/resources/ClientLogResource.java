/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.web.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jkaplan
 */
@Path("/clientLog")
public class ClientLogResource {
    @POST
    @Path("new")
    @Consumes("text/x-wonderland-log")
    public Response post(ClientTestLog log) {
        return Response.ok().build();
    }
    
    @GET
    @Path("list")
    @Produces({"application/xml", "application/json"})
    public Response list() {
        try {
            return Response.ok(new ClientTestLogList(LogStorage.INSTANCE.list()))
                           .build();
        } catch (IOException ioe) {
            throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GET
    @Path("get/{id}")
    @Produces({"text/html"})
    public Response get(@PathParam("id") String id) {
        try {
            final ClientTestLog log = LogStorage.INSTANCE.get(id);
            if (log == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No object with id " + id).build();
            }
            
            return Response.ok(new StreamingOutput() {
                public void write(OutputStream out) throws IOException, WebApplicationException {
                    String header = "<html><head></head>"
                            + "<body style=\"margin: 0px; "
                            + "padding: 8px;"
                            + "background-color: white\">"
                            + "<pre style=\"word-wrap: break-word; "
                            + "white-space: pre-wrap;\">";
                    out.write(header.getBytes());
                    
                    byte[] buffer = new byte[32 * 1024];
                    int read = 0;
                    
                    while ((read = log.getContent().read(buffer)) > 0) {
                        out.write(buffer, 0, read);
                    }
                    
                    out.write("</pre></body></html>".getBytes());
                }
            }).build();
            
        } catch (IOException ioe) {
            throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        
    }
    
    @XmlRootElement(name="client-test-log-list")
    public static class ClientTestLogList {
        private final List<ClientTestLog> logs = new ArrayList<ClientTestLog>();
        
        public ClientTestLogList() {
        }
        
        public ClientTestLogList(List<ClientTestLog> logs) {
            this.logs.addAll(logs);
        }
        
        @XmlElement
        public List<ClientTestLog> getLogs() {
            return logs;
        }
    }
    
    @Provider
    @Consumes("text/x-wonderland-log")
    public static class LogFileReader implements MessageBodyReader<ClientTestLog> {
        private static final MediaType WONDERLAND_LOG_TYPE = 
                new MediaType("text", "x-wonderland-log");
        
        public boolean isReadable(Class<?> type, Type genericType, 
                                  Annotation[] annotations, MediaType mediaType) 
        {
            return mediaType.equals(WONDERLAND_LOG_TYPE);
        }

        public ClientTestLog readFrom(Class<ClientTestLog> type, Type genericType, 
                                      Annotation[] annotations, MediaType mediaType, 
                                      MultivaluedMap<String, String> headers, 
                                      InputStream in)
                throws IOException, WebApplicationException 
        {
            String creator = headers.getFirst("Creator");
            if (creator == null) {
                throw new WebApplicationException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("No creator header").build());
            }
            
            return LogStorage.INSTANCE.store(creator, in);
        }
    }
}
