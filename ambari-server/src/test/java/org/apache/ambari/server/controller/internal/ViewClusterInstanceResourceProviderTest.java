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

import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationEntity;
import org.apache.ambari.server.orm.entities.ViewClusterServiceEntity;
import org.apache.ambari.server.orm.entities.ViewServiceEntity;
import org.apache.ambari.server.view.ViewRegistry;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ViewClusterInstanceResourceProviderTest {

  private static final ViewRegistry singleton = createMock(ViewRegistry.class);

  static {
    ViewRegistry.initInstance(singleton);
  }

  @Before
  public void before() {
    reset(singleton);
  }

  @Test
  public void testToResource() throws Exception {
    ViewClusterInstanceResourceProvider provider = new ViewClusterInstanceResourceProvider();
    Set<String> propertyIds = new HashSet<String>();
    propertyIds.add(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_NAME_PROPERTY_ID);
    propertyIds.add(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_PROPERTY_ID);
    ViewClusterConfigurationEntity clusterConfigurationEntity = createNiceMock(ViewClusterConfigurationEntity.class);

    expect(clusterConfigurationEntity.getServices()).andReturn(getClusterServices());
    expect(clusterConfigurationEntity.getName()).andReturn("c1");

    replay(singleton, clusterConfigurationEntity);

    Resource resource = provider.toResource(clusterConfigurationEntity, propertyIds);
    Map<String, Map<String, Object>> properties = resource.getPropertiesMap();
    assertEquals(1, properties.size());
    Map<String, Object> props = properties.get("ViewClusterInstanceInfo");
    assertNotNull(props);
    assertEquals(2, props.size());
    assertEquals("c1", props.get("name"));

    Set<Map<String,Object>> services = (Set<Map<String,Object>>)props.get("services");
    assertNotNull(services);
    assertEquals(4, services.size());

    assertEquals(services,getClusterServiceMap(false));

    verify(singleton, clusterConfigurationEntity);
  }

  public Collection<ViewClusterServiceEntity> getClusterServices(){
    Set<ViewClusterServiceEntity> clusterServices = new HashSet<ViewClusterServiceEntity>();
    for(int i=0; i < 4 ; i++){
      ViewClusterServiceEntity clusterService = createMock(ViewClusterServiceEntity.class);
      expect(clusterService.getName()).andReturn("Service-"+i).once();
      Map<String,String> propertMap = new HashMap<String,String>();
      propertMap.put("var1"+i , "value1");
      propertMap.put("var2"+i , "value2");
      expect(clusterService.getPropertyMap()).andReturn(propertMap).once();
      replay(clusterService);
      clusterServices.add(clusterService);
    }
    return clusterServices;
  }

  public Set<Map<String,Object>> getClusterServiceMap(boolean concatProperties){
    Set<Map<String,Object>> services = new HashSet<Map<String,Object>>();
    for(int i=0; i < 4 ; i++){
      Map<String,Object> serviceMap = new HashMap<String,Object>();
      serviceMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_NAME_PROPERTY_ID,"Service-"+i);
      Map<String,String> properties = new HashMap<String,String>();
      if(concatProperties) {
        serviceMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID + "/var1"+i,"value1");
        serviceMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID + "/var2"+i,"value2");
      }else {
        properties.put("var1"+i,"value1");
        properties.put("var2"+i,"value2");
        serviceMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_DATA_PROPERTY_ID,properties);
      }

      services.add(serviceMap);
    }
    return services;
  }

  @Test
  public void testCreateResources() throws Exception {
    ViewClusterInstanceResourceProvider provider = new ViewClusterInstanceResourceProvider();

    Set<Map<String, Object>> properties = new HashSet<Map<String, Object>>();

    Map<String, Object> propertyMap = new HashMap<String, Object>();

    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_NAME_PROPERTY_ID, "C1");
    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_PROPERTY_ID, getClusterServiceMap(true));

    properties.add(propertyMap);

    expect(singleton.getViewClusterConfiguration("C1")).andReturn(null).anyTimes();
    expect(singleton.getServiceDefinition(anyString())).andReturn(new ViewServiceEntity()).anyTimes();

    Capture<ViewClusterConfigurationEntity> clusterEntityCapture = new Capture<ViewClusterConfigurationEntity>();
    singleton.addViewClusterConfiguration(capture(clusterEntityCapture));

    expectLastCall().anyTimes();

    replay(singleton);

    provider.createResources(PropertyHelper.getCreateRequest(properties, null));
    assertEquals("C1", clusterEntityCapture.getValue().getName());
    assertEquals(getClusterServices().size(),clusterEntityCapture.getValue().getServices().size());

    for(ViewClusterServiceEntity service : clusterEntityCapture.getValue().getServices()){
      assertEquals(service.getPropertyMap().size(),2);
    }

    verify(singleton);
  }

  @Test
  public void testCreateResources_existingCluster() throws Exception {
    ViewClusterInstanceResourceProvider provider = new ViewClusterInstanceResourceProvider();

    Set<Map<String, Object>> properties = new HashSet<Map<String, Object>>();

    Map<String, Object> propertyMap = new HashMap<String, Object>();

    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_NAME_PROPERTY_ID, "C1");
    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_PROPERTY_ID, getClusterServiceMap(true));

    properties.add(propertyMap);

    expect(singleton.getViewClusterConfiguration("C1")).andReturn(createMock(ViewClusterConfigurationEntity.class));
    expect(singleton.getServiceDefinition(anyString())).andReturn(new ViewServiceEntity()).anyTimes();

    Capture<ViewClusterConfigurationEntity> clusterEntityCapture = new Capture<ViewClusterConfigurationEntity>();
    singleton.addViewClusterConfiguration(capture(clusterEntityCapture));
    expectLastCall().anyTimes();

    replay(singleton);

    try {
      provider.createResources(PropertyHelper.getCreateRequest(properties, null));
      fail("Expected ResourceAlreadyExistsException.");
    } catch (ResourceAlreadyExistsException e) {
      // expected
    }

    verify(singleton);
  }


  @Test
  public void testUpdateResources() throws Exception{
    ViewClusterInstanceResourceProvider provider = new ViewClusterInstanceResourceProvider();

    Set<Map<String, Object>> properties = new HashSet<Map<String, Object>>();

    Map<String, Object> propertyMap = new HashMap<String, Object>();

    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_NAME_PROPERTY_ID, "C1");
    propertyMap.put(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_SERVICE_PROPERTY_ID, getClusterServiceMap(true));

    properties.add(propertyMap);

    ViewClusterConfigurationEntity clusterConfigurationEntity = new ViewClusterConfigurationEntity();
    clusterConfigurationEntity.setName("C1");

    expect(singleton.getAllClusterConfigurations()).andReturn(Collections.singleton(clusterConfigurationEntity)).anyTimes();
    expect(singleton.getViewClusterConfiguration(eq("C1"))).andReturn(clusterConfigurationEntity).anyTimes();
    expect(singleton.getServiceDefinition(anyString())).andReturn(new ViewServiceEntity()).anyTimes();

    Capture<ViewClusterConfigurationEntity> clusterEntityCapture = new Capture<ViewClusterConfigurationEntity>();
    singleton.updateViewClusterConfiguration(capture(clusterEntityCapture));
    expectLastCall().anyTimes();

    replay(singleton);

    PredicateBuilder predicateBuilder = new PredicateBuilder();
    Predicate predicate =
      predicateBuilder.property(ViewClusterInstanceResourceProvider.VIEW_CLUSTER_NAME_PROPERTY_ID).equals("C1").toPredicate();

    provider.updateResources(PropertyHelper.getCreateRequest(properties, null),predicate);
    assertEquals("C1", clusterEntityCapture.getValue().getName());
    assertEquals(getClusterServices().size(),clusterEntityCapture.getValue().getServices().size());

    for(ViewClusterServiceEntity service : clusterEntityCapture.getValue().getServices()){
      assertEquals(service.getPropertyMap().size(),2);
    }

    verify(singleton);
  }
}