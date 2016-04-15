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

package org.apache.ambari.server.orm.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.ViewClusterConfigurationEntity;
import org.apache.ambari.server.orm.entities.ViewEntity;
import org.apache.ambari.server.orm.entities.ViewInstanceEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class ViewClusterConfigurationDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  @Inject
  DaoUtils daoUtils;

  @Transactional
  public void create(ViewClusterConfigurationEntity ViewClusterConfigEntity) {
    entityManagerProvider.get().persist(ViewClusterConfigEntity);
  }

  @Transactional
  public void merge(ViewClusterConfigurationEntity ViewClusterConfigEntity) {
    entityManagerProvider.get().merge(ViewClusterConfigEntity);
  }

  /**
   * Find all view instances.
   *
   * @return all views or an empty List
   */
  @RequiresSession
  public List<ViewClusterConfigurationEntity> findAll() {
    TypedQuery<ViewClusterConfigurationEntity> query = entityManagerProvider.get().
        createNamedQuery("allViewClusterInstances", ViewClusterConfigurationEntity.class);

    return query.getResultList();
  }

  @RequiresSession
  public ViewClusterConfigurationEntity findByName(String name) {
    TypedQuery<ViewClusterConfigurationEntity> query = entityManagerProvider.get().createQuery(
        "SELECT instance FROM ViewClusterConfigurationEntity instance WHERE instance.name = ?1",
        ViewClusterConfigurationEntity.class);
    return daoUtils.selectSingle(query, name);
  }

}
