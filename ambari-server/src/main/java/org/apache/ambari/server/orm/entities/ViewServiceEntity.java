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
import org.apache.ambari.server.view.configuration.ServiceParameterConfig;
import org.apache.ambari.server.view.configuration.ViewConfig;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a service config.
 */
@Table(name = "viewservice")
@NamedQuery(name = "allViewServices",
  query = "SELECT viewService FROM ViewServiceEntity viewService")
@Entity
public class ViewServiceEntity {

  /**
   * The service name.
   */
  @Id
  @Column(name = "name", nullable = false, insertable = true, updatable = false)
  private String name;

  /**
   * The list of view parameters.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "viewservice")
  private List<ViewServiceParameterEntity> parameters = new ArrayList<ViewServiceParameterEntity>();

  @Transient
  private ServiceConfig configuration;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ViewServiceEntity() {
  }

  public ViewServiceEntity(ServiceConfig configuration) {
    this.name =  configuration.getName() + "{" + configuration.getVersion() + "}";
    this.configuration = configuration;

    for (ServiceParameterConfig parameterConfiguration : configuration.getParameters()) {

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

  public List<ViewServiceParameterEntity> getParameters() {
    return parameters;
  }

  public void setParameters(List<ViewServiceParameterEntity> parameters) {
    this.parameters = parameters;
  }

  public ServiceConfig getConfiguration() {
    return configuration;
  }
}