/*
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

import javax.persistence.*;

@javax.persistence.IdClass(ViewClusterConfigurationPropertyEntityPK.class)
@Table(name = "viewclusterproperty")
@Entity
public class ViewClusterConfigurationPropertyEntity {

  @Id
  @Column(name = "cluster_name", nullable = false, insertable = false, updatable = false, length = 100)
  private String clusterName;

  @Id
  @Column(name = "service_name", nullable = false, insertable = false, updatable = false)
  private String serviceName;

  /**
   * The property key.
   */
  @Id
  @Column(name = "name", nullable = false, insertable = true, updatable = false)
  private String name;

  /**
   * The property value.
   */
  @Column
  @Basic
  private String value;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "service_name", referencedColumnName = "name", nullable = false),
      @JoinColumn(name = "cluster_name", referencedColumnName = "cluster_name", nullable = false)
  })
  private ViewClusterServiceEntity serviceConfiguration;

  public ViewClusterConfigurationPropertyEntity() {
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public ViewClusterServiceEntity getServiceConfiguration() {
    return serviceConfiguration;
  }

  public void setServiceConfiguration(ViewClusterServiceEntity serviceConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }
}
