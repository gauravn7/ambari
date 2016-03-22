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
import org.apache.ambari.server.orm.dao.ClusterConfigurationDao;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationEntity;
import org.apache.ambari.server.orm.entities.ViewEntity;
import org.apache.ambari.server.orm.entities.ViewInstanceEntity;
import org.apache.ambari.server.view.ViewRegistry;
import org.apache.ambari.server.view.validation.ValidationException;

import javax.inject.Inject;
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
  public static final String VIEW_CLUSTER_NAME_PROPERTY_ID      = "ViewClusterInstanceInfo/cluster_name";
  public static final String VIEW_CLUSTER_INSTANCE_DATA_PROPERTY_ID      = "ViewClusterInstanceInfo/properties";

  /**
   * Property prefix values.
   */
  private static final String PROPERTIES_PREFIX = VIEW_CLUSTER_INSTANCE_DATA_PROPERTY_ID + "/";

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
    propertyIds.add(VIEW_CLUSTER_INSTANCE_DATA_PROPERTY_ID);
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
    return null;
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

  private ViewClusterConfigurationEntity toEntity(Map<String, Object> properties,boolean update) {
    String name = (String) properties.get(VIEW_CLUSTER_NAME_PROPERTY_ID);
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("View Cluster instance name must be provided");
    }

    ViewRegistry viewRegistry = ViewRegistry.getInstance();
    ViewClusterConfigurationEntity cluster = new ViewClusterConfigurationEntity();
    cluster.setName(name);

    Boolean isUserAdmin = viewRegistry.checkAdmin();

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String propertyName = entry.getKey();

      if (propertyName.startsWith(PROPERTIES_PREFIX)) {
        if (isUserAdmin) {
          cluster.putProperty(entry.getKey().substring(PROPERTIES_PREFIX.length()), (String) entry.getValue());
        }
      }
    }

    return cluster;
  }
}
