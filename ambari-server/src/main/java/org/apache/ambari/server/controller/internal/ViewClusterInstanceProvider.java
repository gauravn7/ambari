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

import com.google.inject.persist.Transactional;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationEntity;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationPropertyEntity;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationPropertyEntityPK;
import org.apache.ambari.server.orm.entities.ViewClusterServiceEntity;
import org.apache.ambari.server.orm.entities.ViewEntity;
import org.apache.ambari.server.orm.entities.ViewInstanceEntity;
import org.apache.ambari.server.view.ViewRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Resource provider for view cluster instances.
 */
public class ViewClusterInstanceProvider extends AbstractResourceProvider{

  /**
   * View instance property id constants.
   */
  public static final String VIEW_CLUSTER_NAME_PROPERTY_ID      = "ViewClusterInstanceInfo/name";
  public static final String VIEW_CLUSTER_SERVICE_PROPERTY_ID      = "ViewClusterInstanceInfo/services";

  public static final String VIEW_CLUSTER_SERVICE_NAME_PROPERTY_ID      = "name";
  public static final String VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID      = "properties";

  /**
   * Property prefix values.
   */
  private static final String PROPERTIES_PREFIX = VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID + "/";

  private static Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();
  static {
    keyPropertyIds.put(Resource.Type.ViewClusterInstance, VIEW_CLUSTER_NAME_PROPERTY_ID);
  }

  /**
   * The property ids for a view instance resource.
   */
  private static Set<String> propertyIds = new HashSet<String>();
  static {
    propertyIds.add(VIEW_CLUSTER_NAME_PROPERTY_ID);
    propertyIds.add(VIEW_CLUSTER_SERVICE_PROPERTY_ID);
  }


  public ViewClusterInstanceProvider() {
    super(propertyIds, keyPropertyIds);
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return null;
  }

  @Override
  public RequestStatus createResources(Request request) throws SystemException, UnsupportedPropertyException, ResourceAlreadyExistsException, NoSuchParentResourceException {
    for (Map<String, Object> properties : request.getProperties()) {
      createResources(getCreateCommand(properties));
    }
    notifyCreate(Resource.Type.ViewClusterInstance, request);

    return getRequestStatus(null);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate) throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    Set<Resource> resources    = new HashSet<Resource>();
    ViewRegistry  viewRegistry = ViewRegistry.getInstance();
    Set<String>   requestedIds = getRequestPropertyIds(request, predicate);

    Set<Map<String, Object>> propertyMaps = getPropertyMaps(predicate);
    if (propertyMaps.isEmpty()) {
      propertyMaps.add(Collections.<String, Object>emptyMap());
    }

    for (Map<String, Object> propertyMap : propertyMaps) {

      String clusterName = (String) propertyMap.get(VIEW_CLUSTER_NAME_PROPERTY_ID);

      for (ViewClusterConfigurationEntity viewCluster : viewRegistry.getAllClusterConfigurations()){
        if (clusterName == null || clusterName.equals(viewCluster.getName())) {
          resources.add(toResource(viewCluster,requestedIds));
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

  // Create a create command with all properties set.
  private Command<Void> getCreateCommand(final Map<String, Object> properties) {
    return new Command<Void>() {
      @Transactional
      @Override
      public Void invoke() throws AmbariException {
        try {
          ViewRegistry viewRegistry   = ViewRegistry.getInstance();
          ViewClusterConfigurationEntity clusterEntity = toEntity(properties, false);
          viewRegistry.addViewClusterConfiguration(clusterEntity);
//          ViewEntity viewEntity = instanceEntity.getViewEntity();
//          String     viewName   = viewEntity.getCommonName();
//          String     version    = viewEntity.getVersion();
//          ViewEntity view       = viewRegistry.getDefinition(viewName, version);
//
//          if ( view == null ) {
//            throw new IllegalStateException("The view " + viewName + " is not registered.");
//          }
//
//          // the view must be in the DEPLOYED state to create an instance
//          if (!view.isDeployed()) {
//            throw new IllegalStateException("The view " + viewName + " is not loaded.");
//          }
//
//          if (viewRegistry.instanceExists(instanceEntity)) {
//            throw new DuplicateResourceException("The instance " + instanceEntity.getName() + " already exists.");
//          }
//          viewRegistry.installViewInstance(instanceEntity);
//        } catch (org.apache.ambari.view.SystemException e) {
//          throw new AmbariException("Caught exception trying to create view instance.", e);
        } catch (Exception e) {
          // results in a BAD_REQUEST (400) response for the validation failure.
          throw new IllegalArgumentException(e.getMessage(), e);
        }
        return null;
      }
    };
  }

  protected Resource toResource(ViewClusterConfigurationEntity viewCluster, Set<String> requestedIds) {
    Resource resource = new ResourceImpl(Resource.Type.ViewClusterInstance);
    setResourceProperty(resource, VIEW_CLUSTER_NAME_PROPERTY_ID, viewCluster.getName() , requestedIds);

    Set<Map<String,Object>> services = new HashSet<Map<String,Object>>();
    for(ViewClusterServiceEntity service : viewCluster.getServices()){
      Map<String,Object> serviceMap =  new HashMap<String,Object>();
      serviceMap.put(VIEW_CLUSTER_SERVICE_NAME_PROPERTY_ID,service.getName());
      serviceMap.put(VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID,service.getPropertiesAsMap());
      services.add(serviceMap);
    }
    setResourceProperty(resource, VIEW_CLUSTER_SERVICE_PROPERTY_ID, services, requestedIds);

    return resource;
  }

  private ViewClusterConfigurationEntity toEntity(Map<String, Object> properties,boolean update) {
    String name = (String) properties.get(VIEW_CLUSTER_NAME_PROPERTY_ID);
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("View Cluster instance name must be provided");
    }

    ViewRegistry viewRegistry = ViewRegistry.getInstance();
    ViewClusterConfigurationEntity cluster = new ViewClusterConfigurationEntity();
    cluster.setName(name);

    Boolean isUserAdmin = viewRegistry.checkAdmin();

    for(Map<String,Object> serviceMap : (Set<Map<String,Object>>)properties.get(VIEW_CLUSTER_SERVICE_PROPERTY_ID)){
      String serviceName = (String)serviceMap.get(VIEW_CLUSTER_SERVICE_NAME_PROPERTY_ID);

      if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("View Cluster Service name must be provided");
      }

      ViewClusterServiceEntity service = new ViewClusterServiceEntity();
      service.setName(serviceName);
      service.setClusterName(name);
      service.setClusterConfiguration(cluster);

      for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
        String propertyName = entry.getKey();
        if (propertyName.startsWith(PROPERTIES_PREFIX)) {
          if (isUserAdmin) {
            service.putProperty(entry.getKey().substring(PROPERTIES_PREFIX.length()), (String) entry.getValue());
          }
        }
      }

      cluster.addService(service);
    }


    return cluster;
  }
}
