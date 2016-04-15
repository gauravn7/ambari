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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.HashMap;

/**
 * Service responsible for view resource requests.
 */
@Path("/viewservice/")
public class ViewServiceService extends BaseService {

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
  public Response getViewServices(String body, @Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, body, ui, Request.Type.GET, createResource(Resource.Type.ViewService,new HashMap<Resource.Type, String>()));
  }

  /**
   * Handles: GET /views/{viewID}
   * Get a specific view.
   *
   * @param headers    http headers
   * @param ui         uri info
   * @param viewServiceName   view id
   *
   * @return view instance representation
   */
  @GET
  @Path("{viewServiceName}")
  @Produces("text/plain")
  public Response getViewService(String body, @Context HttpHeaders headers, @Context UriInfo ui,
                          @PathParam("viewServiceName") String viewServiceName) {

    return handleRequest(headers, body, ui, Request.Type.GET, createViewResource(viewServiceName));
  }

  /**
   * Create a view resource.
   *
   * @param viewName view name
   *
   * @return a view resource instance
   */
  private ResourceInstance createViewResource(String viewName) {
    return createResource(Resource.Type.ViewService,
        Collections.singletonMap(Resource.Type.ViewService, viewName));
  }
}
