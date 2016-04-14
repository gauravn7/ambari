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

package org.apache.ambari.server.orm.entities;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ViewServiceEntity tests.
 */
public class ViewClusterConfigurationEntityTest {

  public ViewClusterConfigurationEntity getViewClusterClonfigurationEntity() {
    ViewClusterConfigurationEntity entity = new ViewClusterConfigurationEntity();
    entity.setName("C1");

    ViewClusterServiceEntity service1 = new ViewClusterServiceEntity();
    service1.setName("s1{1}");
    service1.putProperty("par1","val1");
    service1.putProperty("par2","val2");
    entity.addService(service1);

    ViewClusterServiceEntity service2 = new ViewClusterServiceEntity();
    service2.setName("s2{1}");
    service2.putProperty("par1","val1");
    service2.putProperty("par3","val3");
    entity.addService(service2);

    return entity;
  }

  @Test
  public void testGetPropertyMap() {
    ViewClusterConfigurationEntity entity = getViewClusterClonfigurationEntity();

    Map<String,String> propertyMap = entity.getPropertyMap();

    Assert.assertEquals(3 , propertyMap.size());
    Assert.assertEquals("val1" , propertyMap.get("par1"));
    Assert.assertEquals("val2" , propertyMap.get("par2"));
    Assert.assertEquals("val3" , propertyMap.get("par3"));
  }

  @Test
  public void testGetPropertyMapWithService() {
    ViewClusterConfigurationEntity entity = getViewClusterClonfigurationEntity();
    Set<String> services = new HashSet<String>();
    services.add("s2{1}");
    Map<String,String> propertyMap = entity.getPropertyMap(services);

    Assert.assertEquals(2 , propertyMap.size());
    Assert.assertEquals("val1" , propertyMap.get("par1"));
    Assert.assertEquals("val3" , propertyMap.get("par3"));
  }

}
