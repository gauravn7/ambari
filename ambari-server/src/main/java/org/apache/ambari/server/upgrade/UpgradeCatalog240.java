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

package org.apache.ambari.server.upgrade;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.orm.DBAccessor.DBColumnInfo;
import org.apache.ambari.server.orm.dao.AlertDefinitionDAO;
import org.apache.ambari.server.orm.dao.PermissionDAO;
import org.apache.ambari.server.orm.dao.ResourceTypeDAO;
import org.apache.ambari.server.orm.dao.RoleAuthorizationDAO;
import org.apache.ambari.server.orm.entities.AlertDefinitionEntity;
import org.apache.ambari.server.orm.entities.PermissionEntity;
import org.apache.ambari.server.orm.entities.RoleAuthorizationEntity;
import org.apache.ambari.server.state.AlertFirmness;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.Config;
import org.apache.ambari.server.state.ConfigHelper;
import org.apache.ambari.server.state.RepositoryType;
import org.apache.ambari.server.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

/**
 * Upgrade catalog for version 2.4.0.
 */
public class UpgradeCatalog240 extends AbstractUpgradeCatalog {

  protected static final String ADMIN_PERMISSION_TABLE = "adminpermission";
  protected static final String ALERT_DEFINITION_TABLE = "alert_definition";
  protected static final String ALERT_CURRENT_TABLE = "alert_current";
  protected static final String ALERT_CURRENT_OCCURRENCES_COLUMN = "occurrences";
  protected static final String ALERT_CURRENT_FIRMNESS_COLUMN = "firmness";
  protected static final String HELP_URL_COLUMN = "help_url";
  protected static final String REPEAT_TOLERANCE_COLUMN = "repeat_tolerance";
  protected static final String REPEAT_TOLERANCE_ENABLED_COLUMN = "repeat_tolerance_enabled";
  protected static final String PERMISSION_ID_COL = "permission_name";
  protected static final String SORT_ORDER_COL = "sort_order";
  protected static final String REPO_VERSION_TABLE = "repo_version";
  protected static final String HOST_ROLE_COMMAND_TABLE = "host_role_command";
  protected static final String SERVICE_COMPONENT_DS_TABLE = "servicecomponentdesiredstate";
  protected static final String HOST_COMPONENT_DS_TABLE = "hostcomponentdesiredstate";
  protected static final String HOST_COMPONENT_STATE_TABLE = "hostcomponentstate";
  protected static final String SERVICE_COMPONENT_HISTORY_TABLE = "servicecomponent_history";
  protected static final String UPGRADE_TABLE = "upgrade";
  protected static final String STACK_TABLE = "stack";
  protected static final String CLUSTER_TABLE = "clusters";
  protected static final String CLUSTER_UPGRADE_ID_COLUMN = "upgrade_id";
  protected static final String YARN_ENV_CONFIG = "yarn-env";
  public static final String DESIRED_VERSION_COLUMN_NAME = "desired_version";
  public static final String BLUEPRINT_SETTING_TABLE = "blueprint_setting";
  public static final String BLUEPRINT_NAME_COL = "blueprint_name";
  public static final String SETTING_NAME_COL = "setting_name";
  public static final String SETTING_DATA_COL = "setting_data";
  public static final String ID = "id";
  public static final String BLUEPRINT_TABLE = "blueprint";
  public static final String VIEWINSTANCE_TABLE = "viewinstance";
  public static final String SHORT_URL_COLUMN = "short_url";
  public static final String CLUSTER_TYPE_COLUMN = "cluster_type";
  public static final String VIEW_CLUSTER_TABLE = "viewclusterconfiguration";
  public static final String VIEW_CLUSTER_SERVICE_TABLE = "viewclusterservice";
  public static final String VIEW_CLUSTER_PROPERTY_TABLE = "viewclusterproperty";
  public static final String VIEW_SERVICE_TABLE = "viewservice";
  public static final String VIEW_SERVICE_PARAMETER_TABLE = "viewserviceparameter";

  public static final String NAME = "name";
  public static final String SERVICE_NAME = "service_name";
  public static final String CLUSTER_NAME = "cluster_name";


  @Inject
  PermissionDAO permissionDAO;

  @Inject
  ResourceTypeDAO resourceTypeDAO;

  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UpgradeCatalog240.class);

  private static final String SETTING_TABLE = "setting";

  protected static final String SERVICE_COMPONENT_DESIRED_STATE_TABLE = "servicecomponentdesiredstate";
  protected static final String RECOVERY_ENABLED_COL = "recovery_enabled";

  // ----- Constructors ------------------------------------------------------

  /**
   * Don't forget to register new UpgradeCatalogs in {@link org.apache.ambari.server.upgrade.SchemaUpgradeHelper.UpgradeHelperModule#configure()}
   *
   * @param injector Guice injector to track dependencies and uses bindings to inject them.
   */
  @Inject
  public UpgradeCatalog240(Injector injector) {
    super(injector);
    injector.injectMembers(this);
  }

  // ----- UpgradeCatalog ----------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTargetVersion() {
    return "2.4.0";
  }

  // ----- AbstractUpgradeCatalog --------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSourceVersion() {
    return "2.3.0";
  }


  @Override
  protected void executeDDLUpdates() throws AmbariException, SQLException {
    updateAdminPermissionTable();
    updateServiceComponentDesiredStateTable();
    createSettingTable();
    updateRepoVersionTableDDL();
    updateServiceComponentDesiredStateTableDDL();
    createServiceComponentHistoryTable();
    updateClusterTableDDL();
    updateAlertDefinitionTable();
    updateAlertCurrentTable();
    createBlueprintSettingTable();
    updateHostRoleCommandTableDDL();
    updateViewInstanceEntityTable();
    createViewClusterServicesTable();
  }

  private void updateViewInstanceEntityTable() throws SQLException {
    dbAccessor.addColumn(VIEWINSTANCE_TABLE,
            new DBColumnInfo(SHORT_URL_COLUMN, String.class, 255, null, true));

    dbAccessor.addColumn(VIEWINSTANCE_TABLE,
      new DBColumnInfo(CLUSTER_TYPE_COLUMN, String.class, 100, "AMBARI", true));
  }

  private void updateClusterTableDDL() throws SQLException {
    dbAccessor.addColumn(CLUSTER_TABLE, new DBColumnInfo(CLUSTER_UPGRADE_ID_COLUMN, Long.class, null, null, true));

    dbAccessor.addFKConstraint(CLUSTER_TABLE, "FK_clusters_upgrade_id",
      CLUSTER_UPGRADE_ID_COLUMN, UPGRADE_TABLE, "upgrade_id", false);
  }

  @Override
  protected void executePreDMLUpdates() throws AmbariException, SQLException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void executeDMLUpdates() throws AmbariException, SQLException {
    addNewConfigurationsFromXml();
    updateAlerts();
    setRoleSortOrder();
    addSettingPermission();
    addManageUserPersistedDataPermission();
    updateHDFSConfigs();
    updateAMSConfigs();
    updateClusterEnv();
    updateHostRoleCommandTableDML();
    updateKerberosConfigs();
    updateYarnEnv();
  }

  private void createSettingTable() throws SQLException {
    List<DBColumnInfo> columns = new ArrayList<>();

    //  Add setting table
    LOG.info("Creating " + SETTING_TABLE + " table");

    columns.add(new DBColumnInfo(ID, Long.class, null, null, false));
    columns.add(new DBColumnInfo("name", String.class, 255, null, false));
    columns.add(new DBColumnInfo("setting_type", String.class, 255, null, false));
    columns.add(new DBColumnInfo("content", String.class, 3000, null, false));
    columns.add(new DBColumnInfo("updated_by", String.class, 255, "_db", false));
    columns.add(new DBColumnInfo("update_timestamp", Long.class, null, null, false));
    dbAccessor.createTable(SETTING_TABLE, columns, ID);
    addSequence("setting_id_seq", 0L, false);
  }

  protected void addSettingPermission() throws SQLException {
    RoleAuthorizationDAO roleAuthorizationDAO = injector.getInstance(RoleAuthorizationDAO.class);

    if (roleAuthorizationDAO.findById("AMBARI.MANAGE_SETTINGS") == null) {
      RoleAuthorizationEntity roleAuthorizationEntity = new RoleAuthorizationEntity();
      roleAuthorizationEntity.setAuthorizationId("AMBARI.MANAGE_SETTINGS");
      roleAuthorizationEntity.setAuthorizationName("Manage settings");
      roleAuthorizationDAO.create(roleAuthorizationEntity);
    }

    String administratorPermissionId = permissionDAO.findPermissionByNameAndType("AMBARI.ADMINISTRATOR",
        resourceTypeDAO.findByName("AMBARI")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
            new String[]{"'" + administratorPermissionId + "'", "'AMBARI.MANAGE_SETTINGS'"}, false);
  }

  /**
   * Add 'MANAGE_USER_PERSISTED_DATA' permissions for CLUSTER.ADMINISTRATOR, SERVICE.OPERATOR, SERVICE.ADMINISTRATOR,
   * CLUSTER.OPERATOR, AMBARI.ADMINISTRATOR.
   *
   */
  protected void addManageUserPersistedDataPermission() throws SQLException {

    RoleAuthorizationDAO roleAuthorizationDAO = injector.getInstance(RoleAuthorizationDAO.class);

    // Add to 'roleauthorization' table
    if (roleAuthorizationDAO.findById("CLUSTER.MANAGE_USER_PERSISTED_DATA") == null) {
      RoleAuthorizationEntity roleAuthorizationEntity = new RoleAuthorizationEntity();
      roleAuthorizationEntity.setAuthorizationId("CLUSTER.MANAGE_USER_PERSISTED_DATA");
      roleAuthorizationEntity.setAuthorizationName("Manage cluster-level user persisted data");
      roleAuthorizationDAO.create(roleAuthorizationEntity);
    }

    // Adds to 'permission_roleauthorization' table
    String permissionId = permissionDAO.findPermissionByNameAndType("CLUSTER.ADMINISTRATOR",
      resourceTypeDAO.findByName("CLUSTER")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
      new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

    permissionId = permissionDAO.findPermissionByNameAndType("SERVICE.OPERATOR",
      resourceTypeDAO.findByName("CLUSTER")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
      new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

    permissionId = permissionDAO.findPermissionByNameAndType("SERVICE.ADMINISTRATOR",
      resourceTypeDAO.findByName("CLUSTER")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
      new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

    permissionId = permissionDAO.findPermissionByNameAndType("CLUSTER.OPERATOR",
      resourceTypeDAO.findByName("CLUSTER")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
            new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

    permissionId = permissionDAO.findPermissionByNameAndType("AMBARI.ADMINISTRATOR",
      resourceTypeDAO.findByName("AMBARI")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
      new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

    permissionId = permissionDAO.findPermissionByNameAndType("CLUSTER.USER",
      resourceTypeDAO.findByName("CLUSTER")).getId().toString();
    dbAccessor.insertRowIfMissing("permission_roleauthorization", new String[]{"permission_id", "authorization_id"},
      new String[]{"'" + permissionId + "'", "'CLUSTER.MANAGE_USER_PERSISTED_DATA'"}, false);

  }

  protected void updateAlerts() {
    // map of alert_name -> property_name -> visibility_value
    final Map<String, String> hdfsVisibilityMap = new HashMap<String, String>(){{
      put("mergeHaMetrics", "HIDDEN");
      put("appId", "HIDDEN");
      put("metricName", "HIDDEN");
    }};
    final Map<String, String> defaultKeytabVisibilityMap = new HashMap<String, String>(){{
      put("default.smoke.principal", "HIDDEN");
      put("default.smoke.keytab", "HIDDEN");
    }};

    final Map<String, String> percentParameterMap = new HashMap<String, String>(){{
      put("units", "%");
      put("type", "PERCENT");
    }};

    Map<String, Map<String, String>> visibilityMap = new HashMap<String, Map<String, String>>(){{
      put("hive_webhcat_server_status", new HashMap<String, String>(){{
        put("default.smoke.user", "HIDDEN");
      }});
      put("hive_metastore_process", defaultKeytabVisibilityMap);
      put("hive_server_process", defaultKeytabVisibilityMap);
      put("namenode_service_rpc_queue_latency_hourly", hdfsVisibilityMap);
      put("namenode_client_rpc_queue_latency_hourly", hdfsVisibilityMap);
      put("namenode_service_rpc_processing_latency_hourly", hdfsVisibilityMap);
      put("namenode_client_rpc_processing_latency_hourly", hdfsVisibilityMap);
      put("increase_nn_heap_usage_daily", hdfsVisibilityMap);
      put("namenode_service_rpc_processing_latency_daily", hdfsVisibilityMap);
      put("namenode_client_rpc_processing_latency_daily", hdfsVisibilityMap);
      put("namenode_service_rpc_queue_latency_daily", hdfsVisibilityMap);
      put("namenode_client_rpc_queue_latency_daily", hdfsVisibilityMap);
      put("namenode_increase_in_storage_capacity_usage_daily", hdfsVisibilityMap);
      put("increase_nn_heap_usage_weekly", hdfsVisibilityMap);
      put("namenode_increase_in_storage_capacity_usage_weekly", hdfsVisibilityMap);
    }};

    Map<String, Map<String, String>> reportingPercentMap = new HashMap<String, Map<String, String>>(){{
      put("hawq_segment_process_percent", percentParameterMap);
      put("mapreduce_history_server_cpu", percentParameterMap);
      put("yarn_nodemanager_webui_percent", percentParameterMap);
      put("yarn_resourcemanager_cpu", percentParameterMap);
      put("datanode_process_percent", percentParameterMap);
      put("datanode_storage_percent", percentParameterMap);
      put("journalnode_process_percent", percentParameterMap);
      put("namenode_cpu", percentParameterMap);
      put("namenode_hdfs_capacity_utilization", percentParameterMap);
      put("datanode_storage", percentParameterMap);
      put("datanode_heap_usage", percentParameterMap);
      put("storm_supervisor_process_percent", percentParameterMap);
      put("hbase_regionserver_process_percent", percentParameterMap);
      put("hbase_master_cpu", percentParameterMap);
      put("zookeeper_server_process_percent", percentParameterMap);
      put("metrics_monitor_process_percent", percentParameterMap);
      put("ams_metrics_collector_hbase_master_cpu", percentParameterMap);
    }};

    Map<String, Map<String, Integer>> reportingMultiplierMap = new HashMap<String, Map<String, Integer>>(){{
      put("hawq_segment_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("yarn_nodemanager_webui_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("datanode_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("datanode_storage_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("journalnode_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("storm_supervisor_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("hbase_regionserver_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("zookeeper_server_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
      put("metrics_monitor_process_percent", new HashMap<String, Integer>() {{
        put("warning", 100);
        put("critical", 100);
      }});
    }};

    Map<String, Map<String, Integer>> scriptAlertMultiplierMap = new HashMap<String, Map<String, Integer>>(){{
      put("ambari_agent_disk_usage", new HashMap<String, Integer>() {{
        put("percent.used.space.warning.threshold", 100);
        put("percent.free.space.critical.threshold", 100);
      }});
      put("namenode_last_checkpoint", new HashMap<String, Integer>() {{
        put("checkpoint.time.warning.threshold", 100);
        put("checkpoint.time.critical.threshold", 100);
      }});
    }};


    // list of alerts that need to get property updates
    Set<String> alertNamesForPropertyUpdates = new HashSet<String>() {{
      add("namenode_service_rpc_queue_latency_hourly");
      add("namenode_client_rpc_queue_latency_hourly");
      add("namenode_service_rpc_processing_latency_hourly");
      add("namenode_client_rpc_processing_latency_hourly");
      add("increase_nn_heap_usage_daily");
      add("namenode_service_rpc_processing_latency_daily");
      add("namenode_client_rpc_processing_latency_daily");
      add("namenode_service_rpc_queue_latency_daily");
      add("namenode_client_rpc_queue_latency_daily");
      add("namenode_increase_in_storage_capacity_usage_daily");
      add("increase_nn_heap_usage_weekly");
      add("namenode_increase_in_storage_capacity_usage_weekly");
      add("hawq_segment_process_percent");
      add("mapreduce_history_server_cpu");
      add("yarn_nodemanager_webui_percent");
      add("yarn_resourcemanager_cpu");
      add("datanode_process_percent");
      add("datanode_storage_percent");
      add("journalnode_process_percent");
      add("namenode_cpu");
      add("namenode_hdfs_capacity_utilization");
      add("datanode_storage");
      add("datanode_heap_usage");
      add("storm_supervisor_process_percent");
      add("hbase_regionserver_process_percent");
      add("hbase_master_cpu");
      add("zookeeper_server_process_percent");
      add("metrics_monitor_process_percent");
      add("ams_metrics_collector_hbase_master_cpu");
      add("ambari_agent_disk_usage");
      add("namenode_last_checkpoint");
    }};

    LOG.info("Updating alert definitions.");
    AmbariManagementController ambariManagementController = injector.getInstance(AmbariManagementController.class);
    AlertDefinitionDAO alertDefinitionDAO = injector.getInstance(AlertDefinitionDAO.class);
    Clusters clusters = ambariManagementController.getClusters();

    Map<String, Cluster> clusterMap = getCheckedClusterMap(clusters);
    for (final Cluster cluster : clusterMap.values()) {
      long clusterID = cluster.getClusterId();

      // here goes alerts that need get new properties
      final AlertDefinitionEntity namenodeLastCheckpointAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "namenode_last_checkpoint");
      final AlertDefinitionEntity namenodeHAHealthAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "namenode_ha_health");
      final AlertDefinitionEntity nodemanagerHealthAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "yarn_nodemanager_health");
      final AlertDefinitionEntity nodemanagerHealthSummaryAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "nodemanager_health_summary");
      final AlertDefinitionEntity hiveMetastoreProcessAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "hive_metastore_process");
      final AlertDefinitionEntity hiveServerProcessAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "hive_server_process");
      final AlertDefinitionEntity hiveWebhcatServerStatusAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "hive_webhcat_server_status");
      final AlertDefinitionEntity flumeAgentStatusAlertDefinitionEntity = alertDefinitionDAO.findByName(
              clusterID, "flume_agent_status");

      Map<AlertDefinitionEntity, List<String>> alertDefinitionParams = new HashMap<>();
      checkedPutToMap(alertDefinitionParams, namenodeLastCheckpointAlertDefinitionEntity,
              Lists.newArrayList("connection.timeout", "checkpoint.time.warning.threshold", "checkpoint.time.critical.threshold"));
      checkedPutToMap(alertDefinitionParams, namenodeHAHealthAlertDefinitionEntity,
              Lists.newArrayList("connection.timeout"));
      checkedPutToMap(alertDefinitionParams, nodemanagerHealthAlertDefinitionEntity,
              Lists.newArrayList("connection.timeout"));
      checkedPutToMap(alertDefinitionParams, nodemanagerHealthSummaryAlertDefinitionEntity,
              Lists.newArrayList("connection.timeout"));
      checkedPutToMap(alertDefinitionParams, hiveMetastoreProcessAlertDefinitionEntity,
              Lists.newArrayList("default.smoke.user", "default.smoke.principal", "default.smoke.keytab"));
      checkedPutToMap(alertDefinitionParams, hiveServerProcessAlertDefinitionEntity,
              Lists.newArrayList("default.smoke.user", "default.smoke.principal", "default.smoke.keytab"));
      checkedPutToMap(alertDefinitionParams, hiveWebhcatServerStatusAlertDefinitionEntity,
              Lists.newArrayList("default.smoke.user", "connection.timeout"));
      checkedPutToMap(alertDefinitionParams, flumeAgentStatusAlertDefinitionEntity,
              Lists.newArrayList("run.directory"));

      List<AlertDefinitionEntity> definitionsForPropertyUpdates = new ArrayList<>();

      // adding new properties
      for(Map.Entry<AlertDefinitionEntity, List<String>> entry : alertDefinitionParams.entrySet()){
        AlertDefinitionEntity alertDefinition = entry.getKey();
        String source = alertDefinition.getSource();
        alertDefinition.setSource(addParam(source, entry.getValue()));
        definitionsForPropertyUpdates.add(alertDefinition);
      }

      // here goes alerts that need update for existing properties
      for(String name : alertNamesForPropertyUpdates) {
        AlertDefinitionEntity alertDefinition = alertDefinitionDAO.findByName(clusterID, name);
        if(alertDefinition != null) {
          definitionsForPropertyUpdates.add(alertDefinition);
        }
      }

      // updating old and new properties, best way to use map like visibilityMap.
      for(AlertDefinitionEntity alertDefinition : definitionsForPropertyUpdates) {
        // here goes property updates
        if(visibilityMap.containsKey(alertDefinition.getDefinitionName())) {
          for(Map.Entry<String, String> entry : visibilityMap.get(alertDefinition.getDefinitionName()).entrySet()){
            String paramName = entry.getKey();
            String visibilityValue = entry.getValue();
            String source = alertDefinition.getSource();
            alertDefinition.setSource(addParamOption(source, paramName, "visibility", visibilityValue));
          }
        }
        // update percent script alerts param values from 0.x to 0.x * 100 values
        if(scriptAlertMultiplierMap.containsKey(alertDefinition.getDefinitionName())) {
          for(Map.Entry<String, Integer> entry : scriptAlertMultiplierMap.get(alertDefinition.getDefinitionName()).entrySet()){
            String paramName = entry.getKey();
            Integer multiplier = entry.getValue();
            String source = alertDefinition.getSource();
            Float oldValue = getParamFloatValue(source, paramName);
            Integer newValue = Math.round(oldValue * multiplier);
            alertDefinition.setSource(setParamIntegerValue(source, paramName, newValue));
          }
        }

        // update reporting alerts(aggregate and metrics) values from 0.x to 0.x * 100 values
        if(reportingMultiplierMap.containsKey(alertDefinition.getDefinitionName())) {
          for(Map.Entry<String, Integer> entry : reportingMultiplierMap.get(alertDefinition.getDefinitionName()).entrySet()){
            String reportingName = entry.getKey();
            Integer multiplier = entry.getValue();
            String source = alertDefinition.getSource();
            Float oldValue = getReportingFloatValue(source, reportingName);
            Integer newValue = Math.round(oldValue * multiplier);
            alertDefinition.setSource(setReportingIntegerValue(source, reportingName, newValue));
          }
        }

        if(reportingPercentMap.containsKey(alertDefinition.getDefinitionName())) {
          for(Map.Entry<String, String> entry : reportingPercentMap.get(alertDefinition.getDefinitionName()).entrySet()){
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            String source = alertDefinition.getSource();
            alertDefinition.setSource(addReportingOption(source, paramName, paramValue));
          }
        }

        // regeneration of hash and writing modified alerts to database, must go after all modifications finished
        alertDefinition.setHash(UUID.randomUUID().toString());
        alertDefinitionDAO.merge(alertDefinition);
      }
    }
  }

  /*
  * Simple put method with check for key is not null
  * */
  private void checkedPutToMap(Map<AlertDefinitionEntity, List<String>> alertDefinitionParams, AlertDefinitionEntity alertDefinitionEntity,
                               List<String> params) {
    if (alertDefinitionEntity != null) {
      alertDefinitionParams.put(alertDefinitionEntity, params);
    }
  }

  /**
   * Add option to script parameter.
   * @param source json string of script source
   * @param paramName parameter name
   * @param optionName option name
   * @param optionValue option value
   * @return modified source
   */
  protected String addParamOption(String source, String paramName, String optionName, String optionValue){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    JsonArray parametersJson = sourceJson.getAsJsonArray("parameters");
    if(parametersJson != null && !parametersJson.isJsonNull()) {
      for(JsonElement param : parametersJson) {
        if(param.isJsonObject()) {
          JsonObject paramObject = param.getAsJsonObject();
          if(paramObject.has("name") && paramObject.get("name").getAsString().equals(paramName)){
            paramObject.add(optionName, new JsonPrimitive(optionValue));
          }
        }
      }
    }
    return sourceJson.toString();
  }

  /**
   * Returns param value as float.
   * @param source source of script alert
   * @param paramName param name
   * @return param value as float
   */
  protected Float getParamFloatValue(String source, String paramName){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    JsonArray parametersJson = sourceJson.getAsJsonArray("parameters");
    if(parametersJson != null && !parametersJson.isJsonNull()) {
      for(JsonElement param : parametersJson) {
        if(param.isJsonObject()) {
          JsonObject paramObject = param.getAsJsonObject();
          if(paramObject.has("name") && paramObject.get("name").getAsString().equals(paramName)){
            if(paramObject.has("value")) {
              return paramObject.get("value").getAsFloat();
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Set integer param value.
   * @param source source of script alert
   * @param paramName param name
   * @param value new param value
   * @return modified source
   */
  protected String setParamIntegerValue(String source, String paramName, Integer value){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    JsonArray parametersJson = sourceJson.getAsJsonArray("parameters");
    if(parametersJson != null && !parametersJson.isJsonNull()) {
      for(JsonElement param : parametersJson) {
        if(param.isJsonObject()) {
          JsonObject paramObject = param.getAsJsonObject();
          if(paramObject.has("name") && paramObject.get("name").getAsString().equals(paramName)){
            paramObject.add("value", new JsonPrimitive(value));
          }
        }
      }
    }
    return sourceJson.toString();
  }

  /**
   * Returns reporting value as float.
   * @param source source of aggregate or metric alert
   * @param reportingName reporting name, must be "warning" or "critical"
   * @return reporting value as float
   */
  protected Float getReportingFloatValue(String source, String reportingName){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    return sourceJson.getAsJsonObject("reporting").getAsJsonObject(reportingName).get("value").getAsFloat();
  }

  /**
   * Set integer value of reporting.
   * @param source source of aggregate or metric alert
   * @param reportingName reporting name, must be "warning" or "critical"
   * @param value new value
   * @return modified source
   */
  protected String setReportingIntegerValue(String source, String reportingName, Integer value){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    sourceJson.getAsJsonObject("reporting").getAsJsonObject(reportingName).add("value", new JsonPrimitive(value));
    return sourceJson.toString();
  }

  /**
   * Add option to reporting
   * @param source source of aggregate or metric alert
   * @param optionName option name
   * @param value option value
   * @return modified source
   */
  protected String addReportingOption(String source, String optionName, String value){
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    sourceJson.getAsJsonObject("reporting").add(optionName, new JsonPrimitive(value));
    return sourceJson.toString();
  }

  protected String addParam(String source, List<String> params) {
    JsonObject sourceJson = new JsonParser().parse(source).getAsJsonObject();
    JsonArray parametersJson = sourceJson.getAsJsonArray("parameters");

    boolean parameterExists = parametersJson != null && !parametersJson.isJsonNull();

    if (parameterExists) {
      Iterator<JsonElement> jsonElementIterator = parametersJson.iterator();
      while(jsonElementIterator.hasNext()) {
        JsonElement element = jsonElementIterator.next();
        JsonElement name = element.getAsJsonObject().get("name");
        if (name != null && !name.isJsonNull() && params.contains(name.getAsString())) {
          params.remove(name.getAsString());
        }
      }
      if (params.size() == 0) {
        return sourceJson.toString();
      }
    }

    List<JsonObject> paramsToAdd = new ArrayList<>();

    if (params.contains("connection.timeout")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("connection.timeout"));
      param.add("display_name", new JsonPrimitive("Connection Timeout"));
      param.add("value", new JsonPrimitive(5.0));
      param.add("type", new JsonPrimitive("NUMERIC"));
      param.add("description", new JsonPrimitive("The maximum time before this alert is considered to be CRITICAL"));
      param.add("units", new JsonPrimitive("seconds"));
      param.add("threshold", new JsonPrimitive("CRITICAL"));

      paramsToAdd.add(param);

    }
    if (params.contains("checkpoint.time.warning.threshold")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("checkpoint.time.warning.threshold"));
      param.add("display_name", new JsonPrimitive("Checkpoint Warning"));
      param.add("value", new JsonPrimitive(2.0));
      param.add("type", new JsonPrimitive("PERCENT"));
      param.add("description", new JsonPrimitive("The percentage of the last checkpoint time greater than the interval in order to trigger a warning alert."));
      param.add("units", new JsonPrimitive("%"));
      param.add("threshold", new JsonPrimitive("WARNING"));

      paramsToAdd.add(param);

    }
    if (params.contains("checkpoint.time.critical.threshold")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("checkpoint.time.critical.threshold"));
      param.add("display_name", new JsonPrimitive("Checkpoint Critical"));
      param.add("value", new JsonPrimitive(2.0));
      param.add("type", new JsonPrimitive("PERCENT"));
      param.add("description", new JsonPrimitive("The percentage of the last checkpoint time greater than the interval in order to trigger a critical alert."));
      param.add("units", new JsonPrimitive("%"));
      param.add("threshold", new JsonPrimitive("CRITICAL"));

      paramsToAdd.add(param);

    }
    if (params.contains("default.smoke.user")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("default.smoke.user"));
      param.add("display_name", new JsonPrimitive("Default Smoke User"));
      param.add("value", new JsonPrimitive("ambari-qa"));
      param.add("type", new JsonPrimitive("STRING"));
      param.add("description", new JsonPrimitive("The user that will run the Hive commands if not specified in cluster-env/smokeuser"));

      paramsToAdd.add(param);

    }
    if (params.contains("default.smoke.principal")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("default.smoke.principal"));
      param.add("display_name", new JsonPrimitive("Default Smoke Principal"));
      param.add("value", new JsonPrimitive("ambari-qa@EXAMPLE.COM"));
      param.add("type", new JsonPrimitive("STRING"));
      param.add("description", new JsonPrimitive("The principal to use when retrieving the kerberos ticket if not specified in cluster-env/smokeuser_principal_name"));

      paramsToAdd.add(param);

    }
    if (params.contains("default.smoke.keytab")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("default.smoke.keytab"));
      param.add("display_name", new JsonPrimitive("Default Smoke Keytab"));
      param.add("value", new JsonPrimitive("/etc/security/keytabs/smokeuser.headless.keytab"));
      param.add("type", new JsonPrimitive("STRING"));
      param.add("description", new JsonPrimitive("The keytab to use when retrieving the kerberos ticket if not specified in cluster-env/smokeuser_keytab"));

      paramsToAdd.add(param);

    }
    if (params.contains("run.directory")) {
      JsonObject param = new JsonObject();
      param.add("name", new JsonPrimitive("run.directory"));
      param.add("display_name", new JsonPrimitive("Run Directory"));
      param.add("value", new JsonPrimitive("/var/run/flume"));
      param.add("type", new JsonPrimitive("STRING"));
      param.add("description", new JsonPrimitive("The directory where flume agent processes will place their PID files."));

      paramsToAdd.add(param);

    }


    if (!parameterExists) {
      parametersJson = new JsonArray();
      for (JsonObject param : paramsToAdd) {
        parametersJson.add(param);
      }
      sourceJson.add("parameters", parametersJson);
    } else {
      for (JsonObject param : paramsToAdd) {
        parametersJson.add(param);
      }
      sourceJson.remove("parameters");
      sourceJson.add("parameters", parametersJson);
    }

    return sourceJson.toString();
  }

  protected void updateAdminPermissionTable() throws SQLException {
    // Add the sort_order column to the adminpermission table
    dbAccessor.addColumn(ADMIN_PERMISSION_TABLE,
        new DBColumnInfo(SORT_ORDER_COL, Short.class, null, 1, false));
  }

  /**
   * Updates the {@value #ALERT_DEFINITION_TABLE} in the following ways:
   * <ul>
   * <li>Craetes the {@value #HELP_URL_COLUMN} column</li>
   * <li>Craetes the {@value #REPEAT_TOLERANCE_COLUMN} column</li>
   * <li>Craetes the {@value #REPEAT_TOLERANCE_ENABLED_COLUMN} column</li>
   * </ul>
   *
   * @throws SQLException
   */
  protected void updateAlertDefinitionTable() throws SQLException {
    dbAccessor.addColumn(ALERT_DEFINITION_TABLE,
        new DBColumnInfo(HELP_URL_COLUMN, String.class, 512, null, true));

    dbAccessor.addColumn(ALERT_DEFINITION_TABLE,
        new DBColumnInfo(REPEAT_TOLERANCE_COLUMN, Integer.class, null, 1, false));

    dbAccessor.addColumn(ALERT_DEFINITION_TABLE,
        new DBColumnInfo(REPEAT_TOLERANCE_ENABLED_COLUMN, Short.class, null, 0, false));
  }

  /**
   * Updates the {@value #ALERT_CURRENT_TABLE} in the following ways:
   * <ul>
   * <li>Creates the {@value #ALERT_CURRENT_OCCURRENCES_COLUMN} column</li>
   * <li>Creates the {@value #ALERT_CURRENT_FIRMNESS_COLUMN} column</li>
   * </ul>
   *
   * @throws SQLException
   */
  protected void updateAlertCurrentTable() throws SQLException {
    dbAccessor.addColumn(ALERT_CURRENT_TABLE,
            new DBColumnInfo(ALERT_CURRENT_OCCURRENCES_COLUMN, Long.class, null, 1, false));

    dbAccessor.addColumn(ALERT_CURRENT_TABLE, new DBColumnInfo(ALERT_CURRENT_FIRMNESS_COLUMN,
            String.class, 255, AlertFirmness.HARD.name(), false));
  }

  protected void setRoleSortOrder() throws SQLException {
    String updateStatement = "UPDATE " + ADMIN_PERMISSION_TABLE + " SET " + SORT_ORDER_COL + "=%d WHERE " + PERMISSION_ID_COL + "='%s'";

    LOG.info("Setting permission labels");
    dbAccessor.executeUpdate(String.format(updateStatement,
        1, PermissionEntity.AMBARI_ADMINISTRATOR_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
        2, PermissionEntity.CLUSTER_ADMINISTRATOR_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
            3, PermissionEntity.CLUSTER_OPERATOR_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
            4, PermissionEntity.SERVICE_ADMINISTRATOR_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
            5, PermissionEntity.SERVICE_OPERATOR_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
            6, PermissionEntity.CLUSTER_USER_PERMISSION_NAME));
    dbAccessor.executeUpdate(String.format(updateStatement,
            7, PermissionEntity.VIEW_USER_PERMISSION_NAME));
  }

  /**
   * Makes the following changes to the {@value #REPO_VERSION_TABLE} table:
   * <ul>
   * <li>repo_type VARCHAR(255) DEFAULT 'STANDARD' NOT NULL</li>
   * <li>version_url VARCHAR(1024)</li>
   * <li>version_xml MEDIUMTEXT</li>
   * <li>version_xsd VARCHAR(512)</li>
   * <li>parent_id BIGINT</li>
   * </ul>
   *
   * @throws SQLException
   */
  private void updateRepoVersionTableDDL() throws SQLException {
    DBColumnInfo repoTypeColumn = new DBColumnInfo("repo_type", String.class, 255, RepositoryType.STANDARD.name(), false);
    DBColumnInfo versionUrlColumn = new DBColumnInfo("version_url", String.class, 1024, null, true);
    DBColumnInfo versionXmlColumn = new DBColumnInfo("version_xml", Clob.class, null, null, true);
    DBColumnInfo versionXsdColumn = new DBColumnInfo("version_xsd", String.class, 512, null, true);
    DBColumnInfo parentIdColumn = new DBColumnInfo("parent_id", Long.class, null, null, true);

    dbAccessor.addColumn(REPO_VERSION_TABLE, repoTypeColumn);
    dbAccessor.addColumn(REPO_VERSION_TABLE, versionUrlColumn);
    dbAccessor.addColumn(REPO_VERSION_TABLE, versionXmlColumn);
    dbAccessor.addColumn(REPO_VERSION_TABLE, versionXsdColumn);
    dbAccessor.addColumn(REPO_VERSION_TABLE, parentIdColumn);
  }

  /**
   * Makes the following changes to the {@value #SERVICE_COMPONENT_DS_TABLE} table,
   * but only if the table doesn't have it's new PK set.
   * <ul>
   * <li>id BIGINT NOT NULL</li>
   * <li>Drops FKs on {@value #HOST_COMPONENT_DS_TABLE} and {@value #HOST_COMPONENT_STATE_TABLE}</li>
   * <li>Populates ID in {@value #SERVICE_COMPONENT_DS_TABLE}</li>
   * <li>Creates {@code UNIQUE} constraint on {@value #HOST_COMPONENT_DS_TABLE}</li>
   * <li>Adds FKs on {@value #HOST_COMPONENT_DS_TABLE} and {@value #HOST_COMPONENT_STATE_TABLE}</li>
   * <li>Adds new sequence value of {@code servicecomponentdesiredstate_id_seq}</li>
   * </ul>
   *
   * @throws SQLException
   */
  @Transactional
  private void updateServiceComponentDesiredStateTableDDL() throws SQLException {
    if (dbAccessor.tableHasPrimaryKey(SERVICE_COMPONENT_DS_TABLE, ID)) {
      LOG.info("Skipping {} table Primary Key modifications since the new {} column already exists",
          SERVICE_COMPONENT_DS_TABLE, ID);

      return;
    }

    // drop FKs to SCDS in both HCDS and HCS tables
    dbAccessor.dropFKConstraint(HOST_COMPONENT_DS_TABLE, "hstcmpnntdesiredstatecmpnntnme");
    dbAccessor.dropFKConstraint(HOST_COMPONENT_STATE_TABLE, "hstcomponentstatecomponentname");

    // remove existing compound PK
    dbAccessor.dropPKConstraint(SERVICE_COMPONENT_DS_TABLE, "servicecomponentdesiredstate_pkey");

    // add new PK column to SCDS, making it nullable for now
    DBColumnInfo idColumn = new DBColumnInfo(ID, Long.class, null, null, true);
    dbAccessor.addColumn(SERVICE_COMPONENT_DS_TABLE, idColumn);

    // populate SCDS id column
    AtomicLong scdsIdCounter = new AtomicLong(1);
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = dbAccessor.getConnection().createStatement();
      if (statement != null) {
        String selectSQL = String.format("SELECT cluster_id, service_name, component_name FROM %s",
            SERVICE_COMPONENT_DS_TABLE);

        resultSet = statement.executeQuery(selectSQL);
        while (null != resultSet && resultSet.next()) {
          final Long clusterId = resultSet.getLong("cluster_id");
          final String serviceName = resultSet.getString("service_name");
          final String componentName = resultSet.getString("component_name");

          String updateSQL = String.format(
              "UPDATE %s SET %s = %d WHERE cluster_id = %d AND service_name = '%s' AND component_name = '%s'",
              SERVICE_COMPONENT_DS_TABLE, ID, scdsIdCounter.getAndIncrement(), clusterId,
              serviceName, componentName);

          dbAccessor.executeQuery(updateSQL);
        }
      }
    } finally {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }

    // make the column NON NULL now
    dbAccessor.alterColumn(SERVICE_COMPONENT_DS_TABLE,
        new DBColumnInfo(ID, Long.class, null, null, false));

    // create a new PK, matching the name of the constraint found in SQL
    dbAccessor.addPKConstraint(SERVICE_COMPONENT_DS_TABLE, "pk_sc_desiredstate", ID);

    // create UNIQUE constraint, ensuring column order matches SQL files
    String[] uniqueColumns = new String[] { "component_name", "service_name", "cluster_id" };
    dbAccessor.addUniqueConstraint(SERVICE_COMPONENT_DS_TABLE, "unq_scdesiredstate_name",
        uniqueColumns);

    // add FKs back to SCDS in both HCDS and HCS tables
    dbAccessor.addFKConstraint(HOST_COMPONENT_DS_TABLE, "hstcmpnntdesiredstatecmpnntnme",
        uniqueColumns, SERVICE_COMPONENT_DS_TABLE, uniqueColumns, false);

    dbAccessor.addFKConstraint(HOST_COMPONENT_STATE_TABLE, "hstcomponentstatecomponentname",
        uniqueColumns, SERVICE_COMPONENT_DS_TABLE, uniqueColumns, false);

    // Add sequence for SCDS id
    addSequence("servicecomponentdesiredstate_id_seq", scdsIdCounter.get(), false);
  }

  /**
   * Makes the following changes to the {@value #SERVICE_COMPONENT_HISTORY_TABLE} table:
   * <ul>
   * <li>id BIGINT NOT NULL</li>
   * <li>component_id BIGINT NOT NULL</li>
   * <li>upgrade_id BIGINT NOT NULL</li>
   * <li>from_stack_id BIGINT NOT NULL</li>
   * <li>to_stack_id BIGINT NOT NULL</li>
   * <li>CONSTRAINT PK_sc_history PRIMARY KEY (id)</li>
   * <li>CONSTRAINT FK_sc_history_component_id FOREIGN KEY (component_id) REFERENCES servicecomponentdesiredstate (id)</li>
   * <li>CONSTRAINT FK_sc_history_upgrade_id FOREIGN KEY (upgrade_id) REFERENCES upgrade (upgrade_id)</li>
   * <li>CONSTRAINT FK_sc_history_from_stack_id FOREIGN KEY (from_stack_id) REFERENCES stack (stack_id)</li>
   * <li>CONSTRAINT FK_sc_history_to_stack_id FOREIGN KEY (to_stack_id) REFERENCES stack (stack_id)</li>
   * <li>Creates the {@code servicecomponent_history_id_seq}</li>
   * </ul>
   *
   * @throws SQLException
   */
  private void createServiceComponentHistoryTable() throws SQLException {
    List<DBColumnInfo> columns = new ArrayList<>();
    columns.add(new DBColumnInfo(ID, Long.class, null, null, false));
    columns.add(new DBColumnInfo("component_id", Long.class, null, null, false));
    columns.add(new DBColumnInfo("upgrade_id", Long.class, null, null, false));
    columns.add(new DBColumnInfo("from_stack_id", Long.class, null, null, false));
    columns.add(new DBColumnInfo("to_stack_id", Long.class, null, null, false));
    dbAccessor.createTable(SERVICE_COMPONENT_HISTORY_TABLE, columns, (String[]) null);

    dbAccessor.addPKConstraint(SERVICE_COMPONENT_HISTORY_TABLE, "PK_sc_history", ID);

    dbAccessor.addFKConstraint(SERVICE_COMPONENT_HISTORY_TABLE, "FK_sc_history_component_id",
        "component_id", SERVICE_COMPONENT_DS_TABLE, "id", false);

    dbAccessor.addFKConstraint(SERVICE_COMPONENT_HISTORY_TABLE, "FK_sc_history_upgrade_id",
        "upgrade_id", UPGRADE_TABLE, "upgrade_id", false);

    dbAccessor.addFKConstraint(SERVICE_COMPONENT_HISTORY_TABLE, "FK_sc_history_from_stack_id",
        "from_stack_id", STACK_TABLE, "stack_id", false);

    dbAccessor.addFKConstraint(SERVICE_COMPONENT_HISTORY_TABLE, "FK_sc_history_to_stack_id",
            "to_stack_id", STACK_TABLE, "stack_id", false);

    addSequence("servicecomponent_history_id_seq", 0L, false);
  }

  /**
   * Alter servicecomponentdesiredstate table to add recovery_enabled column.
   * @throws SQLException
   */
  private void updateServiceComponentDesiredStateTable() throws SQLException {
    // ALTER TABLE servicecomponentdesiredstate ADD COLUMN
    // recovery_enabled SMALLINT DEFAULT 0 NOT NULL
    dbAccessor.addColumn(SERVICE_COMPONENT_DESIRED_STATE_TABLE,
            new DBColumnInfo(RECOVERY_ENABLED_COL, Short.class, null, 0, false));

    dbAccessor.addColumn(SERVICE_COMPONENT_DESIRED_STATE_TABLE,
            new DBColumnInfo(DESIRED_VERSION_COLUMN_NAME, String.class, 255, State.UNKNOWN.toString(), false));
  }

  /**
   * Alter host_role_command table to add original_start_time, which is needed because the start_time column now
   * allows overriding the value in ActionScheduler.java
   * @throws SQLException
   */
  private void updateHostRoleCommandTableDDL() throws SQLException {
    final String columnName = "original_start_time";
    DBColumnInfo originalStartTimeColumn = new DBColumnInfo(columnName, Long.class, null, -1L, true);
    dbAccessor.addColumn(HOST_ROLE_COMMAND_TABLE, originalStartTimeColumn);
  }

  /**
   * Alter host_role_command table to update original_start_time with values and make it non-nullable
   * @throws SQLException
   */
  protected void updateHostRoleCommandTableDML() throws SQLException {
    final String columnName = "original_start_time";
    dbAccessor.executeQuery("UPDATE " + HOST_ROLE_COMMAND_TABLE + " SET original_start_time = start_time", false);
    dbAccessor.executeQuery("UPDATE " + HOST_ROLE_COMMAND_TABLE + " SET original_start_time=-1 WHERE original_start_time IS NULL");
    dbAccessor.setColumnNullable(HOST_ROLE_COMMAND_TABLE, columnName, false);
  }

  /**
   * In hdfs-site, set dfs.client.retry.policy.enabled=false
   * This is needed for Rolling/Express upgrade so that clients don't keep retrying, which exhausts the retries and
   * doesn't allow for a graceful failover, which is expected.
   *
   * Rely on dfs.internal.nameservices after upgrade. Copy the value from dfs.services
   * @throws AmbariException
   */
  protected void updateHDFSConfigs() throws AmbariException {
    AmbariManagementController ambariManagementController = injector.getInstance(AmbariManagementController.class);
    Clusters clusters = ambariManagementController.getClusters();

    if (clusters != null) {
      Map<String, Cluster> clusterMap = clusters.getClusters();

      if (clusterMap != null && !clusterMap.isEmpty()) {
        for (final Cluster cluster : clusterMap.values()) {
          Set<String> installedServices = cluster.getServices().keySet();

          if (installedServices.contains("HDFS")) {
            Config hdfsSite = cluster.getDesiredConfigByType("hdfs-site");
            if (hdfsSite != null) {
              String clientRetryPolicyEnabled = hdfsSite.getProperties().get("dfs.client.retry.policy.enabled");
              if (null != clientRetryPolicyEnabled && Boolean.parseBoolean(clientRetryPolicyEnabled)) {
                updateConfigurationProperties("hdfs-site", Collections.singletonMap("dfs.client.retry.policy.enabled", "false"), true, false);
              }
              String nameservices = hdfsSite.getProperties().get("dfs.nameservices");
              String int_nameservices = hdfsSite.getProperties().get("dfs.internal.nameservices");
              if(int_nameservices == null && nameservices != null) {
                updateConfigurationProperties("hdfs-site", Collections.singletonMap("dfs.internal.nameservices", nameservices), true, false);
              }
            }
          }
        }
      }
    }
  }

  protected void updateAMSConfigs() throws AmbariException {
    AmbariManagementController ambariManagementController = injector.getInstance(AmbariManagementController.class);
    Clusters clusters = ambariManagementController.getClusters();

    if (clusters != null) {
      Map<String, Cluster> clusterMap = clusters.getClusters();

      if (clusterMap != null && !clusterMap.isEmpty()) {
        for (final Cluster cluster : clusterMap.values()) {

          Config amsEnv = cluster.getDesiredConfigByType("ams-env");
          if (amsEnv != null) {
            String content = amsEnv.getProperties().get("content");
            if (content != null && !content.contains("AMS_INSTANCE_NAME")) {
              String newContent = content + "\n # AMS instance name\n" +
                      "export AMS_INSTANCE_NAME={{hostname}}\n";

              updateConfigurationProperties("ams-env", Collections.singletonMap("content", newContent), true, true);
            }
          }

          Config amsHBaseEnv = cluster.getDesiredConfigByType("ams-hbase-env");
          if (amsHBaseEnv != null) {
            String content = amsHBaseEnv.getProperties().get("content");
            Map<String, String> newProperties = new HashMap<>();

            if (content != null && !content.contains("HBASE_HOME=")) {
              String newContent = content + "\n # Explicitly Setting HBASE_HOME for AMS HBase so that there is no conflict\n" +
                "export HBASE_HOME={{ams_hbase_home_dir}}\n";
              newProperties.put("content", newContent);
            }

            updateConfigurationPropertiesForCluster(cluster, "ams-hbase-env", newProperties, true, true);
          }
        }
      }
    }
  }

  /**
   * Create blueprint_setting table for storing the "settings" section
   * in the blueprint. Auto start information is specified in the "settings" section.
   *
   * @throws SQLException
   */
  private void createBlueprintSettingTable() throws SQLException {
    List<DBColumnInfo> columns = new ArrayList<>();

    //  Add blueprint_setting table
    LOG.info("Creating " + BLUEPRINT_SETTING_TABLE + " table");

    columns.add(new DBColumnInfo(ID, Long.class, null, null, false));
    columns.add(new DBColumnInfo(BLUEPRINT_NAME_COL, String.class, 255, null, false));
    columns.add(new DBColumnInfo(SETTING_NAME_COL, String.class, 255, null, false));
    columns.add(new DBColumnInfo(SETTING_DATA_COL, char[].class, null, null, false));
    dbAccessor.createTable(BLUEPRINT_SETTING_TABLE, columns);

    dbAccessor.addPKConstraint(BLUEPRINT_SETTING_TABLE, "PK_blueprint_setting", ID);
    dbAccessor.addUniqueConstraint(BLUEPRINT_SETTING_TABLE, "UQ_blueprint_setting_name", BLUEPRINT_NAME_COL, SETTING_NAME_COL);
    dbAccessor.addFKConstraint(BLUEPRINT_SETTING_TABLE, "FK_blueprint_setting_name",
            BLUEPRINT_NAME_COL, BLUEPRINT_TABLE, BLUEPRINT_NAME_COL, false);

    addSequence("blueprint_setting_id_seq", 0L, false);
  }

  /**
   * Updates {@code cluster-env} in the following ways:
   * <ul>
   * <li>Adds {@link ConfigHelper#CLUSTER_ENV_ALERT_REPEAT_TOLERANCE} = 1</li>
   * </ul>
   *
   * @throws Exception
   */
  protected void updateClusterEnv() throws AmbariException {
    Map<String, String> propertyMap = new HashMap<>();
    propertyMap.put(ConfigHelper.CLUSTER_ENV_ALERT_REPEAT_TOLERANCE, "1");

    AmbariManagementController ambariManagementController = injector.getInstance(
        AmbariManagementController.class);

    Clusters clusters = ambariManagementController.getClusters();

    Map<String, Cluster> clusterMap = getCheckedClusterMap(clusters);
    for (final Cluster cluster : clusterMap.values()) {
      updateConfigurationPropertiesForCluster(cluster, ConfigHelper.CLUSTER_ENV, propertyMap, true,
          true);
    }
  }


  /**
   * Updates {@code yarn-env} in the following ways:
   * <ul>
   * <li>Replays export YARN_HISTORYSERVER_HEAPSIZE={{apptimelineserver_heapsize}} to export
   * YARN_TIMELINESERVER_HEAPSIZE={{apptimelineserver_heapsize}}</li>
   * </ul>
   *
   * @throws Exception
   */
  protected void updateYarnEnv() throws AmbariException {
    AmbariManagementController ambariManagementController = injector.getInstance(
            AmbariManagementController.class);

    Clusters clusters = ambariManagementController.getClusters();

    Map<String, Cluster> clusterMap = getCheckedClusterMap(clusters);
    for (final Cluster cluster : clusterMap.values()) {
      Config yarnEnvConfig = cluster.getDesiredConfigByType(YARN_ENV_CONFIG);
      Map<String, String> yarnEnvProps = new HashMap<String, String>();
      if (yarnEnvConfig != null) {
        String content = yarnEnvConfig.getProperties().get("content");
        // comment old property
        content = content.replaceAll("export YARN_HISTORYSERVER_HEAPSIZE=\\{\\{apptimelineserver_heapsize\\}\\}",
                "# export YARN_HISTORYSERVER_HEAPSIZE=\\{\\{apptimelineserver_heapsize\\}\\}");
        // add new correct property
        content = content + "\n\n      # Specify the max Heapsize for the timeline server using a numerical value\n" +
                "      # in the scale of MB. For example, to specify an jvm option of -Xmx1000m, set\n" +
                "      # the value to 1024.\n" +
                "      # This value will be overridden by an Xmx setting specified in either YARN_OPTS\n" +
                "      # and/or YARN_TIMELINESERVER_OPTS.\n" +
                "      # If not specified, the default value will be picked from either YARN_HEAPMAX\n" +
                "      # or JAVA_HEAP_MAX with YARN_HEAPMAX as the preferred option of the two.\n" +
                "      export YARN_TIMELINESERVER_HEAPSIZE={{apptimelineserver_heapsize}}";

        yarnEnvProps.put("content", content);
        updateConfigurationPropertiesForCluster(cluster, YARN_ENV_CONFIG, yarnEnvProps, true, true);
      }

    }

  }


  /**
   * Updates the Kerberos-related configurations for the clusters managed by this Ambari
   * <p/>
   * Performs the following updates:
   * <ul>
   * <li>Rename <code>kerberos-env/kdc_host</code> to
   * <code>kerberos-env/kdc_hosts</li>
   * <li>If krb5-conf/content was not changed from the original stack default, update it to the new
   * stack default</li>
   * </ul>
   *
   * @throws AmbariException if an error occurs while updating the configurations
   */
  protected void updateKerberosConfigs() throws AmbariException {
    AmbariManagementController ambariManagementController = injector.getInstance(AmbariManagementController.class);
    Clusters clusters = ambariManagementController.getClusters();
    Map<String, Cluster> clusterMap = getCheckedClusterMap(clusters);

    for (final Cluster cluster : clusterMap.values()) {
      Config config;

      config = cluster.getDesiredConfigByType("kerberos-env");
      if (config != null) {
        // Rename kdc_host to kdc_hosts
        String value = config.getProperties().get("kdc_host");
        Map<String, String> updates = Collections.singletonMap("kdc_hosts", value);
        Set<String> removes = Collections.singleton("kdc_host");

        updateConfigurationPropertiesForCluster(cluster, "kerberos-env", updates, removes, true, false);
      }

      config = cluster.getDesiredConfigByType("krb5-conf");
      if (config != null) {
        String value = config.getProperties().get("content");
        String oldDefault = "\n[libdefaults]\n  renew_lifetime \u003d 7d\n  forwardable \u003d true\n  default_realm \u003d {{realm}}\n  ticket_lifetime \u003d 24h\n  dns_lookup_realm \u003d false\n  dns_lookup_kdc \u003d false\n  #default_tgs_enctypes \u003d {{encryption_types}}\n  #default_tkt_enctypes \u003d {{encryption_types}}\n\n{% if domains %}\n[domain_realm]\n{% for domain in domains.split(\u0027,\u0027) %}\n  {{domain|trim}} \u003d {{realm}}\n{% endfor %}\n{% endif %}\n\n[logging]\n  default \u003d FILE:/var/log/krb5kdc.log\n  admin_server \u003d FILE:/var/log/kadmind.log\n  kdc \u003d FILE:/var/log/krb5kdc.log\n\n[realms]\n  {{realm}} \u003d {\n    admin_server \u003d {{admin_server_host|default(kdc_host, True)}}\n    kdc \u003d {{kdc_host}}\n  }\n\n{# Append additional realm declarations below #}";

        // if the content is the same as the old stack default, update to the new stack default;
        // else leave it alone since the user may have changed it for a reason.
        if(oldDefault.equalsIgnoreCase(value)) {
          String newDefault ="[libdefaults]\n  renew_lifetime = 7d\n  forwardable = true\n  default_realm = {{realm}}\n  ticket_lifetime = 24h\n  dns_lookup_realm = false\n  dns_lookup_kdc = false\n  #default_tgs_enctypes = {{encryption_types}}\n  #default_tkt_enctypes = {{encryption_types}}\n{% if domains %}\n[domain_realm]\n{%- for domain in domains.split(',') %}\n  {{domain|trim()}} = {{realm}}\n{%- endfor %}\n{% endif %}\n[logging]\n  default = FILE:/var/log/krb5kdc.log\n  admin_server = FILE:/var/log/kadmind.log\n  kdc = FILE:/var/log/krb5kdc.log\n\n[realms]\n  {{realm}} = {\n{%- if kdc_hosts > 0 -%}\n{%- set kdc_host_list = kdc_hosts.split(',')  -%}\n{%- if kdc_host_list and kdc_host_list|length > 0 %}\n    admin_server = {{admin_server_host|default(kdc_host_list[0]|trim(), True)}}\n{%- if kdc_host_list -%}\n{% for kdc_host in kdc_host_list %}\n    kdc = {{kdc_host|trim()}}\n{%- endfor -%}\n{% endif %}\n{%- endif %}\n{%- endif %}\n  }\n\n{# Append additional realm declarations below #}";
          Map<String, String> updates = Collections.singletonMap("content", newDefault);
          updateConfigurationPropertiesForCluster(cluster, "krb5-conf", updates, null, true, false);
        }
      }
    }
  }

  protected void createViewClusterServicesTable() throws SQLException {
    List<DBColumnInfo> columns = new ArrayList<>();

    LOG.info("Creating " + VIEW_CLUSTER_TABLE + " table");
    columns.add(new DBColumnInfo(NAME, String.class, 255, null, false));
    dbAccessor.createTable(VIEW_CLUSTER_TABLE, columns, NAME);

    List<DBColumnInfo> viewClusterServiceColumns = new ArrayList<>();
    LOG.info("Creating " + VIEW_CLUSTER_SERVICE_TABLE + " table");
    viewClusterServiceColumns.add(new DBColumnInfo(NAME, String.class, 255, null, false));
    viewClusterServiceColumns.add(new DBColumnInfo(CLUSTER_NAME, String.class, 255, null, false));
    dbAccessor.createTable(VIEW_CLUSTER_SERVICE_TABLE, viewClusterServiceColumns, NAME,CLUSTER_NAME);

    List<DBColumnInfo> viewClusterPropertyColumns = new ArrayList<>();
    LOG.info("Creating " + VIEW_CLUSTER_PROPERTY_TABLE + " table");
    viewClusterPropertyColumns.add(new DBColumnInfo(NAME, String.class, 255, null, false));
    viewClusterPropertyColumns.add(new DBColumnInfo(CLUSTER_NAME, String.class, 255, null, false));
    viewClusterPropertyColumns.add(new DBColumnInfo(SERVICE_NAME, String.class, 255, null, false));
    viewClusterPropertyColumns.add(new DBColumnInfo("value", String.class, 2000, null, true));
    dbAccessor.createTable(VIEW_CLUSTER_PROPERTY_TABLE, viewClusterPropertyColumns, NAME, CLUSTER_NAME, SERVICE_NAME);

    List<DBColumnInfo> viewServiceColumns = new ArrayList<>();
    LOG.info("Creating " + VIEW_SERVICE_TABLE + " table");
    viewServiceColumns.add(new DBColumnInfo(NAME, String.class, 255, null, false));
    dbAccessor.createTable(VIEW_SERVICE_TABLE, viewServiceColumns, NAME);

    List<DBColumnInfo> viewServiceParameterColumns = new ArrayList<>();
    LOG.info("Creating " + VIEW_SERVICE_PARAMETER_TABLE + " table");
    viewServiceParameterColumns.add(new DBColumnInfo(NAME, String.class, 255, null, false));
    viewServiceParameterColumns.add(new DBColumnInfo("view_service_name", String.class, 255, null, false));
    viewServiceParameterColumns.add(new DBColumnInfo("description", String.class, 2048, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("label", String.class, 255, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("placeholder", String.class, 255, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("default_value", String.class, 2000, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("cluster_config", String.class, 255, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("required", Character.class, 1, null, true));
    viewServiceParameterColumns.add(new DBColumnInfo("masked", Character.class, 1, null, true));
    dbAccessor.createTable(VIEW_SERVICE_PARAMETER_TABLE, viewServiceParameterColumns, NAME, "view_service_name");
  }

}
