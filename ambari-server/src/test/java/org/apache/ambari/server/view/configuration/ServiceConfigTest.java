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

package org.apache.ambari.server.view.configuration;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ServiceConfig tests.
 */
public class ServiceConfigTest {

  private static String xml= "<service-config>>\n" +
      "    <name>MY_SERVICE</name>\n" +
      "    <version>1.0.0</version>\n" +
      "    <min-ambari-version>1.6.1</min-ambari-version>\n" +
      "    <max-ambari-version>2.2.0</max-ambari-version>\n" +
      "    <parameter>\n" +
      "        <name>p1</name>\n" +
      "        <description>Parameter 1.</description>\n" +
      "        <label>Label 1.</label>\n" +
      "        <placeholder>Placeholder 1.</placeholder>\n" +
      "        <cluster-config>fake</cluster-config>\n" +
      "        <required>true</required>\n" +
      "    </parameter>\n" +
      "    <parameter>\n" +
      "        <name>p2</name>\n" +
      "        <description>Parameter 2.</description>\n" +
      "        <default-value>Default value 1.</default-value>\n" +
      "        <cluster-config>hdfs-site/dfs.namenode.http-address</cluster-config>\n" +
      "        <required>false</required>\n" +
      "        <masked>true</masked>" +
      "    </parameter>\n" +
      "</service-config>";


  @Test
  public void testGetName() throws Exception {
    ServiceConfig serviceConfig = getServiceConfig();
    Assert.assertEquals("MY_SERVICE", serviceConfig.getName());
  }

  @Test
  public void testGetServiceName() throws Exception {
    ServiceConfig serviceConfig = getServiceConfig();
    Assert.assertEquals("MY_SERVICE{1.0.0}", serviceConfig.getServiceName());
  }

  @Test
  public void testGetVersion() throws Exception {
    ServiceConfig serviceConfig = getServiceConfig();
    Assert.assertEquals("1.0.0", serviceConfig.getVersion());
  }

  @Test
  public void testGetAmbariVersion() throws Exception {
    ServiceConfig serviceConfig = getServiceConfig();
    Assert.assertEquals("1.6.1", serviceConfig.getMinAmbariVersion());
    Assert.assertEquals("2.2.0", serviceConfig.getMaxAmbariVersion());
  }

  @Test
  public void testGetParameters() throws Exception {
    ServiceConfig serviceConfig = getServiceConfig();
    Assert.assertEquals(2, serviceConfig.getParameters().size());
  }

  public static ServiceConfig getServiceConfig() throws JAXBException {
    return getServiceConfig(xml);
  }

  public static ServiceConfig getServiceConfig(String xml) throws JAXBException {
    InputStream configStream =  new ByteArrayInputStream(xml.getBytes());
    JAXBContext jaxbContext = JAXBContext.newInstance(ServiceConfig.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (ServiceConfig) unmarshaller.unmarshal(configStream);
  }
}
