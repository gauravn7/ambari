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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Cluster Service Configuration
 */
@javax.persistence.IdClass(ViewClusterServiceEntityPK.class)
@Table(name = "viewclusterservice")
@Entity
public class ViewClusterServiceEntity {

  /**
   * The cluster name.
   */
  @Id
  @Column(name = "name", nullable = false, insertable = true, updatable = false)
  private String name;

  @Id
  @Column(name = "cluster_name", nullable = false,insertable = false,updatable = false)
  private String clusterName;


  /**
   * The Service properties.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceConfiguration")
  private Collection<ViewClusterConfigurationPropertyEntity> properties = new HashSet<ViewClusterConfigurationPropertyEntity>();

  @ManyToOne
  @JoinColumn(name = "cluster_name", referencedColumnName = "name", nullable = false)
  private ViewClusterConfigurationEntity clusterConfiguration;

  public ViewClusterServiceEntity() {
  }

  /**
   * Add property to service
   * @param key
   * @param value
   */
  public void putProperty(String key,String value) {
    ViewClusterConfigurationPropertyEntity property = new ViewClusterConfigurationPropertyEntity();
    property.setServiceName(name);
    property.setClusterName(clusterName);
    property.setName(key);
    property.setValue(value);
    property.setServiceConfiguration(this);
    properties.add(property);
  }

  /**
   *
   * @return properties for the service
   */
  public Map<String,String> getPropertyMap(){
    Map<String,String> props = new HashMap<String, String>();
    for(ViewClusterConfigurationPropertyEntity property : properties){
      props.put(property.getName(),property.getValue());
    }
    return props;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public ViewClusterConfigurationEntity getClusterConfiguration() {
    return clusterConfiguration;
  }

  public void setClusterConfiguration(ViewClusterConfigurationEntity clusterConfiguration) {
    this.clusterConfiguration = clusterConfiguration;
  }

  public Collection<ViewClusterConfigurationPropertyEntity> getProperties() {
    return properties;
  }

  public void setProperties(Collection<ViewClusterConfigurationPropertyEntity> properties) {
    this.properties = properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ViewClusterServiceEntity that = (ViewClusterServiceEntity) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (clusterName != null ? !clusterName.equals(that.clusterName) : that.clusterName != null) return false;
    return !(properties != null ? !properties.equals(that.properties) : that.properties != null);

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (clusterName != null ? clusterName.hashCode() : 0);
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }
}
