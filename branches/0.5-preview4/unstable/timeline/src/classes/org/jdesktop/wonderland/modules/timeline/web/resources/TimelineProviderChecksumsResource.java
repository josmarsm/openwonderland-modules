/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.timeline.web.resources;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;
import org.jdesktop.wonderland.common.checksums.Checksum;
import org.jdesktop.wonderland.common.checksums.ChecksumList;
import org.jdesktop.wonderland.runner.RunnerChecksum;
import org.jdesktop.wonderland.runner.RunnerChecksums;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer.DeployedAsset;
import org.jdesktop.wonderland.web.checksums.ChecksumFactory;
import org.jdesktop.wonderland.web.checksums.ChecksumFactory.ChecksumAction;
import org.jdesktop.wonderland.web.checksums.ChecksumManager;
import org.jdesktop.wonderland.web.checksums.modules.ModuleAssetDescriptor;

/**
 * The ModuleChecksumsResource class is a Jersey RESTful service that returns the
 * checksum information about all deployed runner .zips
 * <p>
 * @author kaplanj
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/checksums")
public class TimelineProviderChecksumsResource {
    private static final Logger logger =
            Logger.getLogger(TimelineProviderChecksumsResource.class.getName());

    @Context
    private UriInfo uriInfo;

    /**
     * Returns the checksums information about the deployed runners
     * @return An XML encoding of the module's basic information
     */
    @GET
    @Produces("text/plain")
    public Response getModuleChecksums() {
        List<String> types = new ArrayList<String>();
        types.add("timelineprovider");

        RunnerChecksums checksums = RunnerChecksums.generate(types, uriInfo.getBaseUriBuilder());

        /* Write the XML encoding to a writer and return it */
        StringWriter sw = new StringWriter();
        try {
            checksums.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            /* Log an error and return an error response */
            logger.log(Level.WARNING, "[ASSET] Unable to encode checksums", excp);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }

//    /**
//     * Creates an returns a new instance of the RunnerCheckums object given
//     * a set of asset types to accept.  This will search all deployed modules
//     * for assets of that type
//     * @param types the set of acceptable type of asset
//     * @param builder the UriBuilder for generating URLs
//     */
//    public static RunnerChecksums generate(List<String> types, UriBuilder builder) {
//        Map<String, RunnerChecksum> out = new LinkedHashMap<String, RunnerChecksum>();
//
//        /*
//         * Get a map of all of the Checksum objects for each art asset. We see
//         * if the module name matches each entry and collect its checksum
//         * entries into a single map.
//         */
//        Map<DeployedAsset, File> partMap = AssetDeployer.getFileMap();
//        for (DeployedAsset asset : partMap.keySet()) {
//            if (types.contains(asset.assetType)) {
//                String moduleName = asset.moduleName;
//
//                // go through each checksum, and create a checksum
//                // to add to the output. If there is no factory to load the
//                // checksum for the asset, the go onto the next deployed asset
//                // part
//                ModuleAssetDescriptor mad = new ModuleAssetDescriptor(moduleName, asset.assetType, null);
//                ChecksumManager checksumManager = ChecksumManager.getChecksumManager();
//                ChecksumFactory factory = checksumManager.getChecksumFactory(mad);
//                ChecksumList checksumList = factory.getChecksumList(mad, ChecksumAction.GENERATE);
//                if (checksumList == null) {
//                    continue;
//                }
//
//                for (Map.Entry<String, Checksum> e : checksumList.getChecksumMap().entrySet()) {
//                    String assetName = e.getKey();
//                    Checksum assetChecksum = e.getValue();
//
//                    try {
//                        URL assetURL = getAssetURL(builder, moduleName, assetName);
//                        out.put(assetName,
//                                new RunnerChecksum(assetChecksum,
//                                                     moduleName,
//                                                     assetURL));
//                    } catch (IOException ioe) {
//                        logger.log(Level.WARNING, "Error getting url for " +
//                                   assetName, ioe);
//                    }
//                }
//            }
//        }
//
//        // create the checksums object to write
//        RunnerChecksums ret = new RunnerChecksums();
//        ret.putChecksums(out);
//        return ret;
//    }
//
//    /**
//     * Get the URL for an asset on this server
//     * @return the asset URL
//     */
//    protected static URL getAssetURL(UriBuilder builder, String moduleName,
//                                     String assetPath)
//        throws MalformedURLException
//    {
//        // Should we fetch the context prefix from a property? XXX -jslott
//        String assetPrefix = "webdav/content/modules/installed/";
//        builder.replacePath(assetPrefix + moduleName + "/" + assetPath);
//
//        return builder.build().toURL();
//    }
}
