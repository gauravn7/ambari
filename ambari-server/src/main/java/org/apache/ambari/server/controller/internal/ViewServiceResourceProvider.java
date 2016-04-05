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

package org.apache.ambari.server.controller.internal;

import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.orm.entities.ViewServiceEntity;
import org.apache.ambari.server.view.ViewRegistry;
import org.apache.ambari.server.view.configuration.ServiceConfig;

import java.util.*;

/**
 * Resource provider for view service.
 */
public class ViewServiceResourceProvider extends AbstractResourceProvider{

  /**
   * View property id constants.
   */
  public static final String SERVICE_NAME_PROPERTY_ID    = "serviceInfo/name";
  public static final String SERVICE_COMMON_NAME_PROPERTY_ID    = "serviceInfo/commonName";
  public static final String SERVICE_PARAMETER_PROPERTY_ID    = "serviceInfo/parameters";


  /**
   * The key property ids for a view resource.
   */
  private static Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();
  static {
    keyPropertyIds.put(Resource.Type.ViewService, SERVICE_NAME_PROPERTY_ID);
  }

  /**
   * The property ids for a view resource.
   */
  private static Set<String> propertyIds = new HashSet<String>();
  static {
    propertyIds.add(SERVICE_NAME_PROPERTY_ID);
    propertyIds.add(SERVICE_PARAMETER_PROPERTY_ID);
    propertyIds.add(SERVICE_COMMON_NAME_PROPERTY_ID);
  }


  /**
   * Construct a view resource provider.
   */
  public ViewServiceResourceProvider() {
    super(propertyIds, keyPropertyIds);
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return new HashSet<String>(keyPropertyIds.values());
  }

  @Override
  public RequestStatus createResources(Request request) throws SystemException, UnsupportedPropertyException, ResourceAlreadyExistsException, NoSuchParentResourceException {
    return null;
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate) throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    Set<Resource> resources    = new HashSet<Resource>();
    Set<String>   requestedIds = getRequestPropertyIds(request, predicate);

    Set<Map<String, Object>> propertyMaps = getPropertyMaps(predicate);

    if (propertyMaps.isEmpty()) {
      propertyMaps.add(Collections.<String, Object>emptyMap());
    }

   // requestedIds.add(SERVICE_PARAMETER_PROPERTY_ID);
    ViewRegistry viewRegistry = ViewRegistry.getInstance();

    for (Map<String, Object> propertyMap : propertyMaps) {
      String viewServiceName    = (String) propertyMap.get(SERVICE_NAME_PROPERTY_ID);
      for(ViewServiceEntity sg : viewRegistry.getServiceDefinitions().values()){
        if(viewServiceName == null || sg.getName().equals(viewServiceName)){
          Resource resource = new ResourceImpl(Resource.Type.ViewService);
          setResourceProperty(resource, SERVICE_NAME_PROPERTY_ID, sg.getName() , requestedIds);
          setResourceProperty(resource, SERVICE_PARAMETER_PROPERTY_ID, sg.getConfiguration().getParameters() , requestedIds);
          setResourceProperty(resource, SERVICE_COMMON_NAME_PROPERTY_ID, sg.getConfiguration().getName() , requestedIds);
          resources.add(resource);
        }
      }
    }

    return resources;
  }

  @Override
  public RequestStatus updateResources(Request request, Predicate predicate) throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    return null;
  }

  @Override
  public RequestStatus deleteResources(Predicate predicate) throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    return null;
  }
}
