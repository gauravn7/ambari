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

import org.apache.ambari.server.view.configuration.ServiceConfig;
import org.apache.ambari.server.view.configuration.ServiceConfigTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * ViewServiceEntity tests.
 */
public class ViewServiceEntityTest {

  public static ViewServiceEntity getViewServiceEntity() throws Exception {
    return new ViewServiceEntity(ServiceConfigTest.getServiceConfig());
  }

  @Test
  public void testGetName() throws Exception {
    ViewServiceEntity entity = getViewServiceEntity();
    Assert.assertEquals("MY_SERVICE{1.0.0}",entity.getName());
  }

  @Test
  public void testGetConfigurations() throws Exception {
    ViewServiceEntity entity = getViewServiceEntity();
    ServiceConfig config = ServiceConfigTest.getServiceConfig();
    Assert.assertEquals(config.getMaxAmbariVersion(),entity.getConfiguration().getMaxAmbariVersion());
    Assert.assertEquals(config.getMinAmbariVersion(),entity.getConfiguration().getMinAmbariVersion());
    Assert.assertEquals(config.getName(),entity.getConfiguration().getName());
    Assert.assertEquals(config.getServiceName(),entity.getConfiguration().getServiceName());
  }

  @Test
  public void testGetParameters() throws Exception {
    ViewServiceEntity entity = getViewServiceEntity();
    Assert.assertEquals(2,entity.getParameters().size());
  }
}
