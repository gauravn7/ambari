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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.HashSet;

/**
 * Cluster Configuration
 */
@Table(name = "viewclusterconfiguration")
@Entity
public class ViewClusterConfigurationEntity {

  /**
   * The cluster name.
   */
  @Id
  @Column(name = "name", nullable = false, updatable = false)
  private String name;

//  @OneToMany(cascade = CascadeType.ALL)
//  private Collection<String> services = new HashSet<String>();

  /**
   * The Cluster properties.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "clusterConfiguration")
  private Collection<ViewClusterConfigurationPropertyEntity> properties = new HashSet<ViewClusterConfigurationPropertyEntity>();

  public ViewClusterConfigurationEntity() {
  }

//  public ViewClusterConfigurationEntity(String name) {
//    this.name = name;
//  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

//  public Collection<String> getServices() {
//    return services;
//  }


  public void putProperty(String key,String value) {
    ViewClusterConfigurationPropertyEntity property = new ViewClusterConfigurationPropertyEntity();
    property.setClusterName(name);
    property.setName(key);
    property.setValue(value);
    property.setClusterConfiguration(this);
    properties.add(property);
  }

  public Collection<ViewClusterConfigurationPropertyEntity> getProperties() {
    return properties;
  }

  public void setProperties(Collection<ViewClusterConfigurationPropertyEntity> properties) {
    this.properties = properties;
  }
}
