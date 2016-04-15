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
import org.apache.ambari.server.api.services.ViewService;
import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.ViewServiceEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * View Data Access Object.
 */
@Singleton
public class ViewServiceDAO {
  /**
   * JPA entity manager
   */
  @Inject
  Provider<EntityManager> entityManagerProvider;

  /**
   * Find a view with a given name.
   *
   * @param viewName name of view to find
   *
   * @return  a matching view or null
   */
  @RequiresSession
  public ViewServiceEntity findByName(String viewName) {
    return entityManagerProvider.get().find(ViewServiceEntity.class, viewName);
  }


  /**
   * Find all views.
   *
   * @return all views or an empty List
   */
  @RequiresSession
  public List<ViewServiceEntity> findAll() {
    TypedQuery<ViewServiceEntity> query = entityManagerProvider.get().
        createNamedQuery("allViewServices", ViewServiceEntity.class);

    return query.getResultList();
  }

  /**
   * Refresh the state of the instance from the database,
   * overwriting changes made to the entity, if any.
   *
   * @param ViewServiceEntity  entity to refresh
   */
  @Transactional
  public void refresh(ViewServiceEntity ViewServiceEntity) {
    entityManagerProvider.get().refresh(ViewServiceEntity);
  }

  /**
   * Make an instance managed and persistent.
   *
   * @param ViewServiceEntity  entity to store
   */
  @Transactional
  public void create(ViewServiceEntity ViewServiceEntity) {
    entityManagerProvider.get().persist(ViewServiceEntity);
  }

  /**
   * Merge the state of the given entity into the current persistence context.
   *
   * @param ViewServiceEntity  entity to merge
   * @return the merged entity
   */
  @Transactional
  public ViewServiceEntity merge(ViewServiceEntity ViewServiceEntity) {
    return entityManagerProvider.get().merge(ViewServiceEntity);
  }

  /**
   * Remove the entity instance.
   *
   * @param ViewServiceEntity  entity to remove
   */
  @Transactional
  public void remove(ViewServiceEntity ViewServiceEntity) {
    entityManagerProvider.get().remove(merge(ViewServiceEntity));
  }
}
