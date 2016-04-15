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

import junit.framework.Assert;
import org.junit.Test;
import java.util.Collection;
import java.util.List;

/**
 * ViewServiceParameterEntity tests.
 */
public class ViewServiceParameterEntityTest {

  public static List<ViewServiceParameterEntity> getViewServiceParameterEntity() throws Exception {
    return ViewServiceEntityTest.getViewServiceEntity().getParameters();
  }

  @Test
  public void getName() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals("p1",par1.getName());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals("p2",par2.getName());
  }

  @Test
  public void getLabel() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals("Label 1.", par1.getLabel());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals(null, par2.getLabel());
  }

  @Test
  public void getDescription() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals("Parameter 1.", par1.getDescription());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals("Parameter 2.", par2.getDescription());
  }

  @Test
  public void getPlaceholder() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals("Placeholder 1.", par1.getPlaceholder() );
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals(null, par2.getPlaceholder());
  }

  @Test
  public void getDefaultValue() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals(null,par1.getDefaultValue());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals("Default value 1.",par2.getDefaultValue());
  }

  @Test
  public void getClusterConfig() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals("fake",par1.getClusterConfig());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals("hdfs-site/dfs.namenode.http-address",par2.getClusterConfig());
  }

  @Test
  public void getRequired() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals(true,par1.isRequired());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals(false,par2.isRequired());
  }


  @Test
  public void getMask() throws Exception {
    List<ViewServiceParameterEntity> parameters = getViewServiceParameterEntity();
    ViewServiceParameterEntity par1 = parameters.get(0);
    Assert.assertEquals(false,par1.isMasked());
    ViewServiceParameterEntity par2 = parameters.get(1);
    Assert.assertEquals(true,par2.isMasked());
  }


}
