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
import org.apache.ambari.server.DuplicateResourceException;
import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationEntity;
import org.apache.ambari.server.orm.entities.ViewClusterServiceEntity;
import org.apache.ambari.server.orm.entities.ViewServiceEntity;
import org.apache.ambari.server.orm.entities.ViewServiceParameterEntity;
import org.apache.ambari.server.view.DefaultMasker;
import org.apache.ambari.server.view.ViewRegistry;
import org.apache.ambari.view.MaskException;
import org.apache.ambari.view.Masker;

import java.security.AccessControlException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *  Resource provider for view cluster instances.
 */
public class ViewClusterInstanceResourceProvider extends AbstractResourceProvider{

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

  private Masker masker = new DefaultMasker();

  public ViewClusterInstanceResourceProvider() {
    super(propertyIds, keyPropertyIds);
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return propertyIds;
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
    Iterator<Map<String,Object>> iterator = request.getProperties().iterator();
    if (iterator.hasNext()) {
      for (Map<String, Object> propertyMap : getPropertyMaps(iterator.next(), predicate)) {
        modifyResources(getUpdateCommand(propertyMap));
      }
    }
    notifyUpdate(Resource.Type.ViewClusterInstance, request, predicate);

    return getRequestStatus(null);
  }

  @Override
  public RequestStatus deleteResources(Predicate predicate) throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  // Create a create command with all properties set.
  private Command<Void> getCreateCommand(final Map<String, Object> properties) {
    return new Command<Void>() {
      @Transactional
      @Override
      public Void invoke() throws AmbariException {
        try{
          ViewRegistry viewRegistry   = ViewRegistry.getInstance();
          ViewClusterConfigurationEntity clusterEntity = toEntity(properties, false);

          if(viewRegistry.getViewClusterConfiguration(clusterEntity.getName()) != null){
            throw new DuplicateResourceException("The instance " + clusterEntity.getName() + " already exists.");
          }

          viewRegistry.addViewClusterConfiguration(clusterEntity);
        }catch (MaskException e) {
          // results in a BAD_REQUEST (400) response for the validation failure.
          throw new IllegalArgumentException(e.getMessage(), e);
        }
        return null;
      }
    };
  }

  private Command<Void> getUpdateCommand(final Map<String, Object> properties) {
    return new Command<Void>() {
      @Transactional
      @Override
      public Void invoke() throws AmbariException {
        try {
          ViewRegistry viewRegistry   = ViewRegistry.getInstance();
          ViewClusterConfigurationEntity clusterEntity = toEntity(properties, true);
          viewRegistry.updateViewClusterConfiguration(clusterEntity);
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
      serviceMap.put(VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID,service.getPropertyMap());
      services.add(serviceMap);
    }
    setResourceProperty(resource, VIEW_CLUSTER_SERVICE_PROPERTY_ID, services, requestedIds);

    return resource;
  }

  private ViewClusterConfigurationEntity toEntity(Map<String, Object> properties,boolean update) throws IllegalArgumentException, MaskException {
    String name = (String) properties.get(VIEW_CLUSTER_NAME_PROPERTY_ID);
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("View Cluster instance name must be provided");
    }

    ViewRegistry viewRegistry = ViewRegistry.getInstance();

    ViewClusterConfigurationEntity cluster = null;

    if(update){
      cluster = viewRegistry.getViewClusterConfiguration(name);
    }

    if(cluster == null){
      cluster = new ViewClusterConfigurationEntity();
      cluster.setName(name);
    }

    cluster.clearServices();

    for(Map<String,Object> serviceMap : (Set<Map<String,Object>>)properties.get(VIEW_CLUSTER_SERVICE_PROPERTY_ID)){
      String serviceName = (String)serviceMap.get(VIEW_CLUSTER_SERVICE_NAME_PROPERTY_ID);

      ViewServiceEntity serviceEntity = viewRegistry.getServiceDefinition(serviceName);

      if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("View Cluster Service name must be provided");
      }

      if(serviceEntity == null) {
        throw new IllegalArgumentException("Invalid view service name");
      }

      Map<String,ViewServiceParameterEntity> parameterMap = new HashMap<String, ViewServiceParameterEntity>();

      for(ViewServiceParameterEntity parameter : serviceEntity.getParameters()){
        parameterMap.put(parameter.getName(),parameter);
      }

      ViewClusterServiceEntity service = new ViewClusterServiceEntity();
      service.setName(serviceName);

      for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
        String propertyName = entry.getKey();
        if (propertyName.startsWith(PROPERTIES_PREFIX)) {
          String key = entry.getKey().substring(PROPERTIES_PREFIX.length());
          String value = (String) entry.getValue();

          ViewServiceParameterEntity parameter = parameterMap.get(key);
          if(parameter != null && parameter.isMasked()){
            value = masker.mask(value);
          }
          service.putProperty(key,value);
        }
      }

      cluster.addService(service);
    }


    return cluster;
  }
}
