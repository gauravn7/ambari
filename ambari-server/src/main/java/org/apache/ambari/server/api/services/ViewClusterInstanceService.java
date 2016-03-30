/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.api.services;

import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.controller.spi.Resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.HashMap;

/**
 * Service responsible for view resource requests.
 */
@Path("/viewclusters/")
public class ViewClusterInstanceService extends BaseService {

  /**
   * Handles: GET  /views
   * Get all views.
   *
   * @param headers  http headers
   * @param ui       uri info
   *
   * @return view collection resource representation
   */
  @GET
  @Produces("text/plain")
  public Response getViewClusters(String body, @Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, body, ui, Request.Type.GET, createViewClusterResource(null));
  }

  /**
   * Handles: POST /views/{viewID}/instances
   * Create multiple instances.
   *
   * @param body     http body
   * @param headers  http headers
   * @param ui       uri info
   *
   * @return information regarding the created instances
   */
  @POST
  @Produces("text/plain")
  public Response createCluster(String body, @Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, body, ui, Request.Type.POST,
        createViewClusterResource(null));
  }

  @PUT
  @Produces("text/plain")
  public Response updateCluster(String body, @Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, body, ui, Request.Type.PUT,
      createViewClusterResource(null));
  }

  /**
   * Handles: GET /viewcluster/{clusterName}
   * Get a specific view.
   *
   * @param headers    http headers
   * @param ui         uri info
   * @param clusterName   view id
   *
   * @return view instance representation
   */
  @GET
  @Path("{clusterName}")
  @Produces("text/plain")
  public Response getViewService(String body, @Context HttpHeaders headers, @Context UriInfo ui,
                          @PathParam("clusterName") String clusterName) {

    return handleRequest(headers, body, ui, Request.Type.GET, createViewClusterResource(clusterName));
  }

  /**
   * Create a view resource.
   *
   * @param viewName view name
   *
   * @return a view resource instance
   */
  private ResourceInstance createViewClusterResource(String viewName) {
    return createResource(Resource.Type.ViewClusterInstance,
        Collections.singletonMap(Resource.Type.ViewClusterInstance, viewName));
  }
}
