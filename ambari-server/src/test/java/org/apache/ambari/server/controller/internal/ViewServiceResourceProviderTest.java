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
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.orm.entities.ViewServiceEntity;
import org.apache.ambari.server.orm.entities.ViewServiceEntityTest;
import org.apache.ambari.server.view.ViewRegistry;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

/**
 * ViewServiceProvider tests.
 */
public class ViewServiceResourceProviderTest {

  private static final ViewRegistry viewRegistry = createMock(ViewRegistry.class);

  static {
    ViewRegistry.initInstance(viewRegistry);
  }

  @Test
  public void testGetResources() throws Exception {
    ViewServiceResourceProvider provider = new ViewServiceResourceProvider();

    ViewServiceEntity serviceEntity1 = ViewServiceEntityTest.getViewServiceEntity();

    Map<String,ViewServiceEntity> definitions = new HashMap<String,ViewServiceEntity>();
    definitions.put(serviceEntity1.getName(),serviceEntity1);

    expect(viewRegistry.getServiceDefinitions()).andReturn(definitions);
    replay(viewRegistry);

    Request request = PropertyHelper.getReadRequest(
      ViewServiceResourceProvider.SERVICE_NAME_PROPERTY_ID);

    Set<Resource> results = provider.getResources(request, null);

    assertEquals(1, results.size());
    Resource r = results.iterator().next();
    assertEquals(Resource.Type.ViewService , r.getType());
    assertEquals("MY_SERVICE{1.0.0}" , r.getPropertiesMap().get("serviceInfo").get("name"));
  }
}
