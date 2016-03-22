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

import org.apache.ambari.server.view.configuration.ParameterConfig;
import org.apache.ambari.server.view.configuration.ServiceConfig;
import org.apache.ambari.server.view.configuration.ViewConfig;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a service config.
 */
@Table(name = "viewservice")
@Entity
public class ViewServiceEntity {

  /**
   * The service name.
   */
  @Id
  @Column(name = "name", nullable = false, insertable = false, updatable = false)
  private String name;

  /**
   * The list of view parameters.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "viewservice")
  private Collection<ViewServiceParameterEntity> parameters = new HashSet<ViewServiceParameterEntity>();

  @Transient
  private ServiceConfig configuration;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ViewServiceEntity(ServiceConfig sc) {
    this.name = sc.getName();
    this.configuration = sc;
    for (ParameterConfig parameterConfiguration : sc.getParameters()) {
      ViewServiceParameterEntity viewParameterEntity =  new ViewServiceParameterEntity();

      viewParameterEntity.setViewServiceName(name);
      viewParameterEntity.setName(parameterConfiguration.getName());
      viewParameterEntity.setDescription(parameterConfiguration.getDescription());
      viewParameterEntity.setLabel(parameterConfiguration.getLabel());
      viewParameterEntity.setPlaceholder(parameterConfiguration.getPlaceholder());
      viewParameterEntity.setDefaultValue(parameterConfiguration.getDefaultValue());
      viewParameterEntity.setClusterConfig(parameterConfiguration.getClusterConfig());
      viewParameterEntity.setRequired(parameterConfiguration.isRequired());
      viewParameterEntity.setMasked(parameterConfiguration.isMasked());
      viewParameterEntity.setViewServiceEntity(this);
      parameters.add(viewParameterEntity);
    }
  }

  public Collection<ViewServiceParameterEntity> getParameters() {
    return parameters;
  }

  public void setParameters(Collection<ViewServiceParameterEntity> parameters) {
    this.parameters = parameters;
  }

  public ServiceConfig getConfiguration() {
    return configuration;
  }
}
