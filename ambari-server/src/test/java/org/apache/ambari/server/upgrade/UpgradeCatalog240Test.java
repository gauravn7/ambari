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


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.configuration.Configuration;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.AmbariManagementControllerImpl;
import org.apache.ambari.server.controller.KerberosHelper;
import org.apache.ambari.server.controller.MaintenanceStateHelper;
import org.apache.ambari.server.orm.DBAccessor;
import org.apache.ambari.server.orm.GuiceJpaInitializer;
import org.apache.ambari.server.orm.InMemoryDefaultTestModule;
import org.apache.ambari.server.orm.dao.StackDAO;
import org.apache.ambari.server.state.AlertFirmness;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.Config;
import org.apache.ambari.server.state.Service;
import org.apache.ambari.server.state.stack.OsFamily;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

import junit.framework.Assert;

public class UpgradeCatalog240Test {
  private static Injector injector;
  private Provider<EntityManager> entityManagerProvider = createStrictMock(Provider.class);
  private EntityManager entityManager = createNiceMock(EntityManager.class);


  @BeforeClass
  public static void classSetUp() {
    injector = Guice.createInjector(new InMemoryDefaultTestModule());
    injector.getInstance(GuiceJpaInitializer.class);
  }

  @Before
  public void init() {
    reset(entityManagerProvider);
    expect(entityManagerProvider.get()).andReturn(entityManager).anyTimes();
    replay(entityManagerProvider);

    injector.getInstance(UpgradeCatalogHelper.class);
    // inject AmbariMetaInfo to ensure that stacks get populated in the DB
    injector.getInstance(AmbariMetaInfo.class);
    // load the stack entity
    StackDAO stackDAO = injector.getInstance(StackDAO.class);
    stackDAO.find("HDP", "2.2.0");
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testExecuteDDLUpdates() throws SQLException, AmbariException {
    Capture<DBAccessor.DBColumnInfo> capturedColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedScColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedScDesiredVersionColumnInfo = newCapture();

    final DBAccessor dbAccessor = createStrictMock(DBAccessor.class);
    Configuration configuration = createNiceMock(Configuration.class);
    Connection connection = createNiceMock(Connection.class);
    Statement statement = createNiceMock(Statement.class);
    ResultSet resultSet = createNiceMock(ResultSet.class);
    Capture<List<DBAccessor.DBColumnInfo>> capturedSettingColumns = EasyMock.newCapture();

    dbAccessor.addColumn(eq("adminpermission"), capture(capturedColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.SERVICE_COMPONENT_DESIRED_STATE_TABLE), capture(capturedScColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.SERVICE_COMPONENT_DESIRED_STATE_TABLE),
        capture(capturedScDesiredVersionColumnInfo));

    dbAccessor.createTable(eq("setting"), capture(capturedSettingColumns), eq("id"));
    expect(configuration.getDatabaseUrl()).andReturn(Configuration.JDBC_IN_MEMORY_URL).anyTimes();
    expect(dbAccessor.getConnection()).andReturn(connection);
    expect(connection.createStatement()).andReturn(statement);
    expect(statement.executeQuery(anyObject(String.class))).andReturn(resultSet);

    Capture<DBAccessor.DBColumnInfo> repoVersionRepoTypeColumnCapture = newCapture();
    Capture<DBAccessor.DBColumnInfo> repoVersionUrlColumnCapture = newCapture();
    Capture<DBAccessor.DBColumnInfo> repoVersionXmlColumnCapture = newCapture();
    Capture<DBAccessor.DBColumnInfo> repoVersionXsdColumnCapture = newCapture();
    Capture<DBAccessor.DBColumnInfo> repoVersionParentIdColumnCapture = newCapture();

    dbAccessor.addColumn(eq("repo_version"), capture(repoVersionRepoTypeColumnCapture));
    dbAccessor.addColumn(eq("repo_version"), capture(repoVersionUrlColumnCapture));
    dbAccessor.addColumn(eq("repo_version"), capture(repoVersionXmlColumnCapture));
    dbAccessor.addColumn(eq("repo_version"), capture(repoVersionXsdColumnCapture));
    dbAccessor.addColumn(eq("repo_version"), capture(repoVersionParentIdColumnCapture));

    // skip all of the drama of the servicecomponentdesiredstate table for now
    expect(dbAccessor.tableHasPrimaryKey("servicecomponentdesiredstate", "id")).andReturn(true);

    Capture<List<DBAccessor.DBColumnInfo>> capturedHistoryColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq("servicecomponent_history"), capture(capturedHistoryColumns),
            eq((String[]) null));

    dbAccessor.addPKConstraint("servicecomponent_history", "PK_sc_history", "id");
    dbAccessor.addFKConstraint("servicecomponent_history", "FK_sc_history_component_id",
        "component_id", "servicecomponentdesiredstate", "id", false);

    dbAccessor.addFKConstraint("servicecomponent_history", "FK_sc_history_upgrade_id", "upgrade_id",
        "upgrade", "upgrade_id", false);

    dbAccessor.addFKConstraint("servicecomponent_history", "FK_sc_history_from_stack_id",
            "from_stack_id", "stack", "stack_id", false);

    dbAccessor.addFKConstraint("servicecomponent_history", "FK_sc_history_to_stack_id",
            "to_stack_id", "stack", "stack_id", false);


    expect(dbAccessor.getConnection()).andReturn(connection);
    expect(connection.createStatement()).andReturn(statement);
    expect(statement.executeQuery(anyObject(String.class))).andReturn(resultSet);

    Capture<DBAccessor.DBColumnInfo> capturedClusterUpgradeColumnInfo = newCapture();
    dbAccessor.addColumn(eq(UpgradeCatalog240.CLUSTER_TABLE), capture(capturedClusterUpgradeColumnInfo));
    dbAccessor.addFKConstraint(UpgradeCatalog240.CLUSTER_TABLE, "FK_clusters_upgrade_id",
            UpgradeCatalog240.CLUSTER_UPGRADE_ID_COLUMN, UpgradeCatalog240.UPGRADE_TABLE, "upgrade_id", false);

    Capture<DBAccessor.DBColumnInfo> capturedHelpURLColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedRepeatToleranceColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedRepeatToleranceEnabledColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedOccurrencesColumnInfo = newCapture();
    Capture<DBAccessor.DBColumnInfo> capturedFirmnessColumnInfo = newCapture();

    dbAccessor.addColumn(eq(UpgradeCatalog240.ALERT_DEFINITION_TABLE), capture(capturedHelpURLColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.ALERT_DEFINITION_TABLE), capture(capturedRepeatToleranceColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.ALERT_DEFINITION_TABLE), capture(capturedRepeatToleranceEnabledColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.ALERT_CURRENT_TABLE), capture(capturedOccurrencesColumnInfo));
    dbAccessor.addColumn(eq(UpgradeCatalog240.ALERT_CURRENT_TABLE), capture(capturedFirmnessColumnInfo));

    // Test creation of blueprint_setting table
    Capture<List<DBAccessor.DBColumnInfo>> capturedBlueprintSettingColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.BLUEPRINT_SETTING_TABLE), capture(capturedBlueprintSettingColumns));
    dbAccessor.addPKConstraint(UpgradeCatalog240.BLUEPRINT_SETTING_TABLE, "PK_blueprint_setting", UpgradeCatalog240.ID);
    dbAccessor.addUniqueConstraint(UpgradeCatalog240.BLUEPRINT_SETTING_TABLE, "UQ_blueprint_setting_name",
            UpgradeCatalog240.BLUEPRINT_NAME_COL, UpgradeCatalog240.SETTING_NAME_COL);
    dbAccessor.addFKConstraint(UpgradeCatalog240.BLUEPRINT_SETTING_TABLE, "FK_blueprint_setting_name",
            UpgradeCatalog240.BLUEPRINT_NAME_COL, UpgradeCatalog240.BLUEPRINT_TABLE,
            UpgradeCatalog240.BLUEPRINT_NAME_COL, false);
    expect(dbAccessor.getConnection()).andReturn(connection);
    expect(connection.createStatement()).andReturn(statement);
    expect(statement.executeQuery(anyObject(String.class))).andReturn(resultSet);

    // Test host_role_command adds a column called original_start_time
    Capture<DBAccessor.DBColumnInfo> hostRoleCommandOriginalStartTimeColumnInfo = newCapture();
    dbAccessor.addColumn(eq(UpgradeCatalog240.HOST_ROLE_COMMAND_TABLE), capture(hostRoleCommandOriginalStartTimeColumnInfo));

    Capture<DBAccessor.DBColumnInfo> viewInstanceShortUrlInfo = newCapture();
    dbAccessor.addColumn(eq(UpgradeCatalog240.VIEWINSTANCE_TABLE), capture(viewInstanceShortUrlInfo));

    Capture<DBAccessor.DBColumnInfo> viewInstanceClusterType = newCapture();
    dbAccessor.addColumn(eq(UpgradeCatalog240.VIEWINSTANCE_TABLE), capture(viewInstanceClusterType));

    // Test creation of View cluster service tables
    Capture<List<DBAccessor.DBColumnInfo>> capturedViewClusterColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.VIEW_CLUSTER_TABLE), capture(capturedViewClusterColumns),anyString());

    Capture<List<DBAccessor.DBColumnInfo>> capturedViewClusterServiceColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.VIEW_CLUSTER_SERVICE_TABLE), capture(capturedViewClusterServiceColumns),anyString(),anyString());

    Capture<List<DBAccessor.DBColumnInfo>> capturedViewClusterPropertyColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.VIEW_CLUSTER_PROPERTY_TABLE), capture(capturedViewClusterPropertyColumns),anyString(),anyString(),anyString());

    Capture<List<DBAccessor.DBColumnInfo>> capturedViewServiceColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.VIEW_SERVICE_TABLE), capture(capturedViewServiceColumns),anyString());

    Capture<List<DBAccessor.DBColumnInfo>> capturedViewServiceParameterColumns = EasyMock.newCapture();
    dbAccessor.createTable(eq(UpgradeCatalog240.VIEW_SERVICE_PARAMETER_TABLE), capture(capturedViewServiceParameterColumns),anyString(),anyString());

    replay(dbAccessor, configuration, connection, statement, resultSet);

    Module module = new Module() {
      @Override
      public void configure(Binder binder) {
        binder.bind(DBAccessor.class).toInstance(dbAccessor);
        binder.bind(OsFamily.class).toInstance(createNiceMock(OsFamily.class));
        binder.bind(EntityManager.class).toInstance(entityManager);
      }
      };

    Injector injector = Guice.createInjector(module);
    UpgradeCatalog240 upgradeCatalog240 = injector.getInstance(UpgradeCatalog240.class);
    upgradeCatalog240.executeDDLUpdates();

    DBAccessor.DBColumnInfo columnInfo = capturedColumnInfo.getValue();
    Assert.assertNotNull(columnInfo);
    Assert.assertEquals(UpgradeCatalog240.SORT_ORDER_COL, columnInfo.getName());
    Assert.assertEquals(null, columnInfo.getLength());
    Assert.assertEquals(Short.class, columnInfo.getType());
    Assert.assertEquals(1, columnInfo.getDefaultValue());
    Assert.assertEquals(false, columnInfo.isNullable());

    // Verify if recovery_enabled column was added to servicecomponentdesiredstate table
    DBAccessor.DBColumnInfo columnScInfo = capturedScColumnInfo.getValue();
    Assert.assertNotNull(columnScInfo);
    Assert.assertEquals(UpgradeCatalog240.RECOVERY_ENABLED_COL, columnScInfo.getName());
    Assert.assertEquals(null, columnScInfo.getLength());
    Assert.assertEquals(Short.class, columnScInfo.getType());
    Assert.assertEquals(0, columnScInfo.getDefaultValue());
    Assert.assertEquals(false, columnScInfo.isNullable());

    DBAccessor.DBColumnInfo columnScDesiredVersionInfo = capturedScDesiredVersionColumnInfo.getValue();
    Assert.assertNotNull(columnScDesiredVersionInfo);
    Assert.assertEquals(UpgradeCatalog240.DESIRED_VERSION_COLUMN_NAME, columnScDesiredVersionInfo.getName());
    Assert.assertEquals(Integer.valueOf(255), columnScDesiredVersionInfo.getLength());
    Assert.assertEquals(String.class, columnScDesiredVersionInfo.getType());
    Assert.assertEquals("UNKNOWN", columnScDesiredVersionInfo.getDefaultValue());
    Assert.assertEquals(false, columnScDesiredVersionInfo.isNullable());

    // Verify if upgrade_id column was added to clusters table
    DBAccessor.DBColumnInfo clusterUpgradeColumnInfo = capturedClusterUpgradeColumnInfo.getValue();
    Assert.assertNotNull(clusterUpgradeColumnInfo);
    Assert.assertEquals(UpgradeCatalog240.CLUSTER_UPGRADE_ID_COLUMN, clusterUpgradeColumnInfo.getName());
    Assert.assertEquals(null, clusterUpgradeColumnInfo.getLength());
    Assert.assertEquals(Long.class, clusterUpgradeColumnInfo.getType());
    Assert.assertEquals(null, clusterUpgradeColumnInfo.getDefaultValue());
    Assert.assertEquals(true, clusterUpgradeColumnInfo.isNullable());

    Map<String, Class> expectedCaptures = new HashMap<>();
    expectedCaptures.put("id", Long.class);
    expectedCaptures.put("name", String.class);
    expectedCaptures.put("setting_type", String.class);
    expectedCaptures.put("content", String.class);
    expectedCaptures.put("updated_by", String.class);
    expectedCaptures.put("update_timestamp", Long.class);

    Map<String, Class> actualCaptures = new HashMap<>();
    for(DBAccessor.DBColumnInfo settingColumnInfo : capturedSettingColumns.getValue()) {
      actualCaptures.put(settingColumnInfo.getName(), settingColumnInfo.getType());
    }

    assertEquals(expectedCaptures, actualCaptures);

    expectedCaptures = new HashMap<>();
    expectedCaptures.put("id", Long.class);
    expectedCaptures.put("component_id", Long.class);
    expectedCaptures.put("upgrade_id", Long.class);
    expectedCaptures.put("from_stack_id", Long.class);
    expectedCaptures.put("to_stack_id", Long.class);

    actualCaptures = new HashMap<>();
    for (DBAccessor.DBColumnInfo historyColumnInfo : capturedHistoryColumns.getValue()) {
      actualCaptures.put(historyColumnInfo.getName(), historyColumnInfo.getType());
    }

    DBAccessor.DBColumnInfo columnHelpURLInfo = capturedHelpURLColumnInfo.getValue();
    Assert.assertNotNull(columnHelpURLInfo);
    Assert.assertEquals(UpgradeCatalog240.HELP_URL_COLUMN, columnHelpURLInfo.getName());
    Assert.assertEquals(Integer.valueOf(512), columnHelpURLInfo.getLength());
    Assert.assertEquals(String.class, columnHelpURLInfo.getType());
    Assert.assertEquals(null, columnHelpURLInfo.getDefaultValue());
    Assert.assertEquals(true, columnHelpURLInfo.isNullable());

    DBAccessor.DBColumnInfo columnRepeatToleranceInfo = capturedRepeatToleranceColumnInfo.getValue();
    Assert.assertNotNull(columnRepeatToleranceInfo);
    Assert.assertEquals(UpgradeCatalog240.REPEAT_TOLERANCE_COLUMN, columnRepeatToleranceInfo.getName());
    Assert.assertEquals(Integer.class, columnRepeatToleranceInfo.getType());
    Assert.assertEquals(1, columnRepeatToleranceInfo.getDefaultValue());
    Assert.assertEquals(false, columnRepeatToleranceInfo.isNullable());

    DBAccessor.DBColumnInfo columnRepeatToleranceEnabledInfo = capturedRepeatToleranceEnabledColumnInfo.getValue();
    Assert.assertNotNull(columnRepeatToleranceEnabledInfo);
    Assert.assertEquals(UpgradeCatalog240.REPEAT_TOLERANCE_ENABLED_COLUMN, columnRepeatToleranceEnabledInfo.getName());
    Assert.assertEquals(Short.class, columnRepeatToleranceEnabledInfo.getType());
    Assert.assertEquals(0, columnRepeatToleranceEnabledInfo.getDefaultValue());
    Assert.assertEquals(false, columnRepeatToleranceEnabledInfo.isNullable());

    DBAccessor.DBColumnInfo columnOccurrencesInfo = capturedOccurrencesColumnInfo.getValue();
    Assert.assertNotNull(columnOccurrencesInfo);
    Assert.assertEquals(UpgradeCatalog240.ALERT_CURRENT_OCCURRENCES_COLUMN, columnOccurrencesInfo.getName());
    Assert.assertEquals(Long.class, columnOccurrencesInfo.getType());
    Assert.assertEquals(1, columnOccurrencesInfo.getDefaultValue());
    Assert.assertEquals(false, columnOccurrencesInfo.isNullable());

    DBAccessor.DBColumnInfo columnFirmnessInfo = capturedFirmnessColumnInfo.getValue();
    Assert.assertNotNull(columnFirmnessInfo);
    Assert.assertEquals(UpgradeCatalog240.ALERT_CURRENT_FIRMNESS_COLUMN, columnFirmnessInfo.getName());
    Assert.assertEquals(String.class, columnFirmnessInfo.getType());
    Assert.assertEquals(AlertFirmness.HARD.name(), columnFirmnessInfo.getDefaultValue());
    Assert.assertEquals(false, columnFirmnessInfo.isNullable());

    assertEquals(expectedCaptures, actualCaptures);

    // Verify blueprint_setting columns
    expectedCaptures = new HashMap<>();
    expectedCaptures.put(UpgradeCatalog240.ID, Long.class);
    expectedCaptures.put(UpgradeCatalog240.BLUEPRINT_NAME_COL, String.class);
    expectedCaptures.put(UpgradeCatalog240.SETTING_NAME_COL, String.class);
    expectedCaptures.put(UpgradeCatalog240.SETTING_DATA_COL, char[].class);

    actualCaptures = new HashMap<>();
    for(DBAccessor.DBColumnInfo blueprintSettingsColumnInfo : capturedBlueprintSettingColumns.getValue()) {
      actualCaptures.put(blueprintSettingsColumnInfo.getName(), blueprintSettingsColumnInfo.getType());
    }

    assertEquals(expectedCaptures, actualCaptures);

    // Verify host_role_command column
    DBAccessor.DBColumnInfo originalStartTimeInfo = hostRoleCommandOriginalStartTimeColumnInfo.getValue();
    Assert.assertNotNull(originalStartTimeInfo);
    Assert.assertEquals("original_start_time", originalStartTimeInfo.getName());
    Assert.assertEquals(Long.class, originalStartTimeInfo.getType());
    Assert.assertEquals(-1L, originalStartTimeInfo.getDefaultValue());

    // Verify host_role_command column
    DBAccessor.DBColumnInfo viewInstanceEntityUrlColInfoValue = viewInstanceShortUrlInfo.getValue();
    Assert.assertNotNull(viewInstanceEntityUrlColInfoValue);
    Assert.assertEquals("short_url", viewInstanceEntityUrlColInfoValue.getName());
    Assert.assertEquals(String.class, viewInstanceEntityUrlColInfoValue.getType());

    // Verify cluster_tye column
    DBAccessor.DBColumnInfo viewInstanceEntityClusterTypeValue = viewInstanceClusterType.getValue();
    Assert.assertNotNull(viewInstanceClusterType);
    Assert.assertEquals("cluster_type", viewInstanceEntityClusterTypeValue.getName());
    Assert.assertEquals(String.class, viewInstanceEntityClusterTypeValue.getType());

    //verify view cluster service tables
    Assert.assertEquals(1,capturedViewClusterColumns.getValue().size());
    Assert.assertEquals(UpgradeCatalog240.NAME,capturedViewClusterColumns.getValue().get(0).getName());

    Assert.assertEquals(2,capturedViewClusterServiceColumns.getValue().size());
    Assert.assertEquals(UpgradeCatalog240.NAME,capturedViewClusterServiceColumns.getValue().get(0).getName());
    Assert.assertEquals(UpgradeCatalog240.CLUSTER_NAME,capturedViewClusterServiceColumns.getValue().get(1).getName());

    Assert.assertEquals(4,capturedViewClusterPropertyColumns.getValue().size());
    Assert.assertEquals(UpgradeCatalog240.NAME,capturedViewClusterPropertyColumns.getValue().get(0).getName());
    Assert.assertEquals(UpgradeCatalog240.CLUSTER_NAME,capturedViewClusterPropertyColumns.getValue().get(1).getName());
    Assert.assertEquals(UpgradeCatalog240.SERVICE_NAME,capturedViewClusterPropertyColumns.getValue().get(2).getName());
    Assert.assertEquals("value",capturedViewClusterPropertyColumns.getValue().get(3).getName());

    Assert.assertEquals(capturedViewServiceColumns.getValue().size(),1);
    Assert.assertEquals(UpgradeCatalog240.NAME,capturedViewServiceColumns.getValue().get(0).getName());

    Assert.assertEquals(capturedViewServiceParameterColumns.getValue().size(),9);
    Assert.assertEquals(UpgradeCatalog240.NAME,capturedViewServiceParameterColumns.getValue().get(0).getName());
    Assert.assertEquals("view_service_name",capturedViewServiceParameterColumns.getValue().get(1).getName());
    Assert.assertEquals("description",capturedViewServiceParameterColumns.getValue().get(2).getName());
    Assert.assertEquals("label",capturedViewServiceParameterColumns.getValue().get(3).getName());
    Assert.assertEquals("placeholder",capturedViewServiceParameterColumns.getValue().get(4).getName());
    Assert.assertEquals("default_value",capturedViewServiceParameterColumns.getValue().get(5).getName());
    Assert.assertEquals("cluster_config",capturedViewServiceParameterColumns.getValue().get(6).getName());
    Assert.assertEquals("required",capturedViewServiceParameterColumns.getValue().get(7).getName());
    Assert.assertEquals("masked",capturedViewServiceParameterColumns.getValue().get(8).getName());

    verify(dbAccessor);
  }

  @Test
  public void testExecuteDMLUpdates() throws Exception {
    Method addNewConfigurationsFromXml = AbstractUpgradeCatalog.class.getDeclaredMethod("addNewConfigurationsFromXml");
    Method updateAlerts = UpgradeCatalog240.class.getDeclaredMethod("updateAlerts");
    Method addManageUserPersistedDataPermission = UpgradeCatalog240.class.getDeclaredMethod("addManageUserPersistedDataPermission");
    Method addSettingPermission = UpgradeCatalog240.class.getDeclaredMethod("addSettingPermission");
    Method updateHDFSConfigs = UpgradeCatalog240.class.getDeclaredMethod("updateHDFSConfigs");
    Method updateAmsConfigs = UpgradeCatalog240.class.getDeclaredMethod("updateAMSConfigs");
    Method updateClusterEnv = UpgradeCatalog240.class.getDeclaredMethod("updateClusterEnv");
    Method updateHostRoleCommandTableDML = UpgradeCatalog240.class.getDeclaredMethod("updateHostRoleCommandTableDML");
    Method updateKerberosEnv = UpgradeCatalog240.class.getDeclaredMethod("updateKerberosConfigs");
    Method updateYarnEnv = UpgradeCatalog240.class.getDeclaredMethod("updateYarnEnv");

    Capture<String> capturedStatements = newCapture(CaptureType.ALL);

    DBAccessor dbAccessor = createStrictMock(DBAccessor.class);
    expect(dbAccessor.executeUpdate(capture(capturedStatements))).andReturn(1).times(7);

    UpgradeCatalog240 upgradeCatalog240 = createMockBuilder(UpgradeCatalog240.class)
            .addMockedMethod(addNewConfigurationsFromXml)
            .addMockedMethod(updateAlerts)
            .addMockedMethod(addSettingPermission)
            .addMockedMethod(addManageUserPersistedDataPermission)
            .addMockedMethod(updateHDFSConfigs)
            .addMockedMethod(updateAmsConfigs)
            .addMockedMethod(updateClusterEnv)
            .addMockedMethod(updateHostRoleCommandTableDML)
            .addMockedMethod(updateKerberosEnv)
            .addMockedMethod(updateYarnEnv)
            .createMock();

    Field field = AbstractUpgradeCatalog.class.getDeclaredField("dbAccessor");
    field.set(upgradeCatalog240, dbAccessor);

    upgradeCatalog240.addNewConfigurationsFromXml();
    upgradeCatalog240.updateAlerts();
    upgradeCatalog240.addSettingPermission();
    upgradeCatalog240.addManageUserPersistedDataPermission();
    upgradeCatalog240.updateHDFSConfigs();
    upgradeCatalog240.updateAMSConfigs();
    upgradeCatalog240.updateClusterEnv();
    upgradeCatalog240.updateHostRoleCommandTableDML();
    upgradeCatalog240.updateKerberosConfigs();
    upgradeCatalog240.updateYarnEnv();

    replay(upgradeCatalog240, dbAccessor);

    upgradeCatalog240.executeDMLUpdates();

    verify(upgradeCatalog240, dbAccessor);

    List<String> statements = capturedStatements.getValues();
    Assert.assertNotNull(statements);
    Assert.assertEquals(7, statements.size());
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=1 WHERE permission_name='AMBARI.ADMINISTRATOR'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=2 WHERE permission_name='CLUSTER.ADMINISTRATOR'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=3 WHERE permission_name='CLUSTER.OPERATOR'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=4 WHERE permission_name='SERVICE.ADMINISTRATOR'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=5 WHERE permission_name='SERVICE.OPERATOR'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=6 WHERE permission_name='CLUSTER.USER'"));
    Assert.assertTrue(statements.contains("UPDATE adminpermission SET sort_order=7 WHERE permission_name='VIEW.USER'"));
  }

  @Test
  public void test_addParam_ParamsNotAvailable() {

    UpgradeCatalog240 upgradeCatalog240 = new UpgradeCatalog240(injector);
    String inputSource = "{ \"path\" : \"test_path\", \"type\" : \"SCRIPT\"}";
    List<String> params = Arrays.asList("connection.timeout", "checkpoint.time.warning.threshold", "checkpoint.time.critical.threshold");
    String expectedSource = "{\"path\":\"test_path\",\"type\":\"SCRIPT\",\"parameters\":[{\"name\":\"connection.timeout\",\"display_name\":\"Connection Timeout\",\"value\":5.0,\"type\":\"NUMERIC\",\"description\":\"The maximum time before this alert is considered to be CRITICAL\",\"units\":\"seconds\",\"threshold\":\"CRITICAL\"},{\"name\":\"checkpoint.time.warning.threshold\",\"display_name\":\"Checkpoint Warning\",\"value\":2.0,\"type\":\"PERCENT\",\"description\":\"The percentage of the last checkpoint time greater than the interval in order to trigger a warning alert.\",\"units\":\"%\",\"threshold\":\"WARNING\"},{\"name\":\"checkpoint.time.critical.threshold\",\"display_name\":\"Checkpoint Critical\",\"value\":2.0,\"type\":\"PERCENT\",\"description\":\"The percentage of the last checkpoint time greater than the interval in order to trigger a critical alert.\",\"units\":\"%\",\"threshold\":\"CRITICAL\"}]}";

    String result = upgradeCatalog240.addParam(inputSource, params);
    Assert.assertEquals(result, expectedSource);
  }

  @Test
  public void test_addParam_ParamsAvailableWithOneOFNeededItem() {

    UpgradeCatalog240 upgradeCatalog240 = new UpgradeCatalog240(injector);
    String inputSource = "{\"path\":\"test_path\",\"type\":\"SCRIPT\",\"parameters\":[{\"name\":\"connection.timeout\",\"display_name\":\"Connection Timeout\",\"value\":5.0,\"type\":\"NUMERIC\",\"description\":\"The maximum time before this alert is considered to be CRITICAL\",\"units\":\"seconds\",\"threshold\":\"CRITICAL\"}]}";
    List<String> params = new ArrayList<>(Arrays.asList("connection.timeout", "checkpoint.time.warning.threshold", "checkpoint.time.critical.threshold"));
    String expectedSource = "{\"path\":\"test_path\",\"type\":\"SCRIPT\",\"parameters\":[{\"name\":\"connection.timeout\",\"display_name\":\"Connection Timeout\",\"value\":5.0,\"type\":\"NUMERIC\",\"description\":\"The maximum time before this alert is considered to be CRITICAL\",\"units\":\"seconds\",\"threshold\":\"CRITICAL\"},{\"name\":\"checkpoint.time.warning.threshold\",\"display_name\":\"Checkpoint Warning\",\"value\":2.0,\"type\":\"PERCENT\",\"description\":\"The percentage of the last checkpoint time greater than the interval in order to trigger a warning alert.\",\"units\":\"%\",\"threshold\":\"WARNING\"},{\"name\":\"checkpoint.time.critical.threshold\",\"display_name\":\"Checkpoint Critical\",\"value\":2.0,\"type\":\"PERCENT\",\"description\":\"The percentage of the last checkpoint time greater than the interval in order to trigger a critical alert.\",\"units\":\"%\",\"threshold\":\"CRITICAL\"}]}";

    String result = upgradeCatalog240.addParam(inputSource, params);
    Assert.assertEquals(result, expectedSource);
  }

  /**
   * Test that dfs.internal.nameservices is not affected
   * Also, dfs.client.retry.policy.enabled is reset
   * @throws Exception
   */
  @Test
  public void testHdfsSiteUpdateConfigs2() throws Exception{
    Map<String, String> oldPropertiesHdfsSite = new HashMap<String, String>() {
      {
        put("dfs.client.retry.policy.enabled", "true");
      }
    };
    Map<String, String> newPropertiesHdfsSite = new HashMap<String, String>() {
      {
        put("dfs.client.retry.policy.enabled", "false");
      }
    };
    EasyMockSupport easyMockSupport = new EasyMockSupport();

    Clusters clusters = easyMockSupport.createNiceMock(Clusters.class);
    final Cluster cluster = easyMockSupport.createNiceMock(Cluster.class);
    final Service service = createStrictMock(Service.class);
    final Map<String, Service> services = Collections.singletonMap("HDFS", service);
    Config mockHdfsSite = easyMockSupport.createNiceMock(Config.class);

    expect(clusters.getClusters()).andReturn(new HashMap<String, Cluster>() {{
      put("normal", cluster);
    }}).anyTimes();
    expect(cluster.getDesiredConfigByType("hdfs-site")).andReturn(mockHdfsSite).atLeastOnce();
    expect(mockHdfsSite.getProperties()).andReturn(oldPropertiesHdfsSite).anyTimes();
    expect(cluster.getServices()).andReturn(services).once();

    Injector injector = easyMockSupport.createNiceMock(Injector.class);
    expect(injector.getInstance(Gson.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(KerberosHelper.class)).andReturn(createNiceMock(KerberosHelper.class)).anyTimes();

    replay(injector, clusters, mockHdfsSite, cluster);

    AmbariManagementControllerImpl controller = createMockBuilder(AmbariManagementControllerImpl.class)
        .addMockedMethod("createConfiguration")
        .addMockedMethod("getClusters", new Class[] { })
        .addMockedMethod("createConfig")
        .withConstructor(createNiceMock(ActionManager.class), clusters, injector)
        .createNiceMock();

    Injector injector2 = easyMockSupport.createNiceMock(Injector.class);
    Capture<Map> propertiesCapture = EasyMock.newCapture();

    expect(injector2.getInstance(AmbariManagementController.class)).andReturn(controller).anyTimes();
    expect(controller.getClusters()).andReturn(clusters).anyTimes();
    expect(controller.createConfig(anyObject(Cluster.class), anyString(), capture(propertiesCapture), anyString(),
                                   anyObject(Map.class))).andReturn(createNiceMock(Config.class)).once();

    replay(controller, injector2);
    new UpgradeCatalog240(injector2).updateHDFSConfigs();
    easyMockSupport.verifyAll();

    Map<String, String> updatedProperties = propertiesCapture.getValue();
    assertTrue(Maps.difference(newPropertiesHdfsSite, updatedProperties).areEqual());
  }

  /**
   * Test that dfs.nameservices is copied over to dfs.internal.nameservices
   * @throws Exception
   */
  @Test
  public void testHdfsSiteUpdateConfigs() throws Exception{
    Map<String, String> oldPropertiesHdfsSite = new HashMap<String, String>() {
      {
        put("dfs.nameservices", "nnha");
      }
    };
    Map<String, String> newPropertiesHdfsSite = new HashMap<String, String>() {
      {
        put("dfs.nameservices", "nnha");
        put("dfs.internal.nameservices", "nnha");
      }
    };
    EasyMockSupport easyMockSupport = new EasyMockSupport();

    Clusters clusters = easyMockSupport.createNiceMock(Clusters.class);
    final Cluster cluster = easyMockSupport.createNiceMock(Cluster.class);
    final Service service = createStrictMock(Service.class);
    final Map<String, Service> services = Collections.singletonMap("HDFS", service);
    Config mockHdfsSite = easyMockSupport.createNiceMock(Config.class);

    expect(clusters.getClusters()).andReturn(new HashMap<String, Cluster>() {{
      put("normal", cluster);
    }}).anyTimes();
    expect(cluster.getDesiredConfigByType("hdfs-site")).andReturn(mockHdfsSite).atLeastOnce();
    expect(mockHdfsSite.getProperties()).andReturn(oldPropertiesHdfsSite).anyTimes();
    expect(cluster.getServices()).andReturn(services).once();

    Injector injector = easyMockSupport.createNiceMock(Injector.class);
    expect(injector.getInstance(Gson.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(KerberosHelper.class)).andReturn(createNiceMock(KerberosHelper.class)).anyTimes();

    replay(injector, clusters, mockHdfsSite, cluster);

    AmbariManagementControllerImpl controller = createMockBuilder(AmbariManagementControllerImpl.class)
        .addMockedMethod("createConfiguration")
        .addMockedMethod("getClusters", new Class[] { })
        .addMockedMethod("createConfig")
        .withConstructor(createNiceMock(ActionManager.class), clusters, injector)
        .createNiceMock();

    Injector injector2 = easyMockSupport.createNiceMock(Injector.class);
    Capture<Map> propertiesCapture = EasyMock.newCapture();

    expect(injector2.getInstance(AmbariManagementController.class)).andReturn(controller).anyTimes();
    expect(controller.getClusters()).andReturn(clusters).anyTimes();
    expect(controller.createConfig(anyObject(Cluster.class), anyString(), capture(propertiesCapture), anyString(),
                                   anyObject(Map.class))).andReturn(createNiceMock(Config.class)).once();

    replay(controller, injector2);
    new UpgradeCatalog240(injector2).updateHDFSConfigs();
    easyMockSupport.verifyAll();

    Map<String, String> updatedProperties = propertiesCapture.getValue();
    assertTrue(Maps.difference(newPropertiesHdfsSite, updatedProperties).areEqual());
  }

  @Test
  public void testYarnEnvUpdateConfigs() throws Exception{

    Map<String, String> oldPropertiesYarnEnv = new HashMap<String, String>() {
      {
        put("content", "export YARN_HISTORYSERVER_HEAPSIZE={{apptimelineserver_heapsize}}");
      }
    };
    Map<String, String> newPropertiesYarnEnv = new HashMap<String, String>() {
      {
        put("content", "# export YARN_HISTORYSERVER_HEAPSIZE={{apptimelineserver_heapsize}}" +
                "\n\n      # Specify the max Heapsize for the timeline server using a numerical value\n" +
                "      # in the scale of MB. For example, to specify an jvm option of -Xmx1000m, set\n" +
                "      # the value to 1024.\n" +
                "      # This value will be overridden by an Xmx setting specified in either YARN_OPTS\n" +
                "      # and/or YARN_TIMELINESERVER_OPTS.\n" +
                "      # If not specified, the default value will be picked from either YARN_HEAPMAX\n" +
                "      # or JAVA_HEAP_MAX with YARN_HEAPMAX as the preferred option of the two.\n" +
                "      export YARN_TIMELINESERVER_HEAPSIZE={{apptimelineserver_heapsize}}");
      }
    };
    EasyMockSupport easyMockSupport = new EasyMockSupport();

    Clusters clusters = easyMockSupport.createNiceMock(Clusters.class);
    final Cluster cluster = easyMockSupport.createNiceMock(Cluster.class);
    Config mockYarnEnv = easyMockSupport.createNiceMock(Config.class);

    expect(clusters.getClusters()).andReturn(new HashMap<String, Cluster>() {{
      put("normal", cluster);
    }}).once();
    expect(cluster.getDesiredConfigByType("yarn-env")).andReturn(mockYarnEnv).atLeastOnce();
    expect(mockYarnEnv.getProperties()).andReturn(oldPropertiesYarnEnv).anyTimes();

    Injector injector = easyMockSupport.createNiceMock(Injector.class);
    expect(injector.getInstance(Gson.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(KerberosHelper.class)).andReturn(createNiceMock(KerberosHelper.class)).anyTimes();

    replay(injector, clusters, mockYarnEnv, cluster);

    AmbariManagementControllerImpl controller = createMockBuilder(AmbariManagementControllerImpl.class)
            .addMockedMethod("createConfiguration")
            .addMockedMethod("getClusters", new Class[] { })
            .addMockedMethod("createConfig")
            .withConstructor(createNiceMock(ActionManager.class), clusters, injector)
            .createNiceMock();

    Injector injector2 = easyMockSupport.createNiceMock(Injector.class);
    Capture<Map> propertiesCapture = EasyMock.newCapture();

    expect(injector2.getInstance(AmbariManagementController.class)).andReturn(controller).anyTimes();
    expect(controller.getClusters()).andReturn(clusters).anyTimes();
    expect(controller.createConfig(anyObject(Cluster.class), anyString(), capture(propertiesCapture), anyString(),
            anyObject(Map.class))).andReturn(createNiceMock(Config.class)).once();

    replay(controller, injector2);
    new UpgradeCatalog240(injector2).updateYarnEnv();
    easyMockSupport.verifyAll();

    Map<String, String> updatedProperties = propertiesCapture.getValue();
    assertTrue(Maps.difference(newPropertiesYarnEnv, updatedProperties).areEqual());
  }

  @Test
  public void testAmsHbaseEnvUpdateConfigs() throws Exception{

    Map<String, String> oldPropertiesAmsHbaseEnv = new HashMap<String, String>() {
      {
        put("content", "some_content");
      }
    };
    Map<String, String> newPropertiesAmsHbaseEnv = new HashMap<String, String>() {
      {
        put("content", "some_content"+ "\n # Explicitly Setting HBASE_HOME for AMS HBase so that there is no conflict\n" +
          "export HBASE_HOME={{ams_hbase_home_dir}}\n");
      }
    };
    EasyMockSupport easyMockSupport = new EasyMockSupport();

    Clusters clusters = easyMockSupport.createNiceMock(Clusters.class);
    final Cluster cluster = easyMockSupport.createNiceMock(Cluster.class);
    Config mockAmsHbaseEnv = easyMockSupport.createNiceMock(Config.class);

    expect(clusters.getClusters()).andReturn(new HashMap<String, Cluster>() {{
      put("normal", cluster);
    }}).once();
    expect(cluster.getDesiredConfigByType("ams-hbase-env")).andReturn(mockAmsHbaseEnv).atLeastOnce();
    expect(mockAmsHbaseEnv.getProperties()).andReturn(oldPropertiesAmsHbaseEnv).anyTimes();

    Injector injector = easyMockSupport.createNiceMock(Injector.class);
    expect(injector.getInstance(Gson.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).anyTimes();
    expect(injector.getInstance(KerberosHelper.class)).andReturn(createNiceMock(KerberosHelper.class)).anyTimes();

    replay(injector, clusters, mockAmsHbaseEnv, cluster);

    AmbariManagementControllerImpl controller = createMockBuilder(AmbariManagementControllerImpl.class)
      .addMockedMethod("createConfiguration")
      .addMockedMethod("getClusters", new Class[] { })
      .addMockedMethod("createConfig")
      .withConstructor(createNiceMock(ActionManager.class), clusters, injector)
      .createNiceMock();

    Injector injector2 = easyMockSupport.createNiceMock(Injector.class);
    Capture<Map> propertiesCapture = EasyMock.newCapture();

    expect(injector2.getInstance(AmbariManagementController.class)).andReturn(controller).anyTimes();
    expect(controller.getClusters()).andReturn(clusters).anyTimes();
    expect(controller.createConfig(anyObject(Cluster.class), anyString(), capture(propertiesCapture), anyString(),
      anyObject(Map.class))).andReturn(createNiceMock(Config.class)).once();

    replay(controller, injector2);
    new UpgradeCatalog240(injector2).updateAMSConfigs();
    easyMockSupport.verifyAll();

    Map<String, String> updatedProperties = propertiesCapture.getValue();
    assertTrue(Maps.difference(newPropertiesAmsHbaseEnv, updatedProperties).areEqual());
  }

  @Test
  public void testUpdateKerberosConfiguration() throws Exception {
    final AmbariManagementController controller = createNiceMock(AmbariManagementController.class);
    final DBAccessor dbAccessor = createNiceMock(DBAccessor.class);
    final OsFamily osFamily = createNiceMock(OsFamily.class);

    final Map<String, String> propertiesKerberosEnv = new HashMap<String, String>() {
      {
        put("realm", "EXAMPLE.COM");
        put("encryption_types", "aes des3-cbc-sha1 rc4 des-cbc-md5");
        put("kdc_host", "c6407.ambari.apache.org");
        put("admin_server_host", "c6407.ambari.apache.org");
        put("kdc_type", "mit-kdc");
      }
    };

    final Map<String, String> propertiesKrb5Conf = new HashMap<String, String>() {
      {
        put("content", "\n" +
            "[libdefaults]\n" +
            "  renew_lifetime = 7d\n" +
            "  forwardable = true\n" +
            "  default_realm = {{realm}}\n" +
            "  ticket_lifetime = 24h\n" +
            "  dns_lookup_realm = false\n" +
            "  dns_lookup_kdc = false\n" +
            "  #default_tgs_enctypes = {{encryption_types}}\n" +
            "  #default_tkt_enctypes = {{encryption_types}}\n" +
            "\n" +
            "{% if domains %}\n" +
            "[domain_realm]\n" +
            "{% for domain in domains.split(',') %}\n" +
            "  {{domain|trim}} = {{realm}}\n" +
            "{% endfor %}\n" +
            "{% endif %}\n" +
            "\n" +
            "[logging]\n" +
            "  default = FILE:/var/log/krb5kdc.log\n" +
            "  admin_server = FILE:/var/log/kadmind.log\n" +
            "  kdc = FILE:/var/log/krb5kdc.log\n" +
            "\n" +
            "[realms]\n" +
            "  {{realm}} = {\n" +
            "    admin_server = {{admin_server_host|default(kdc_host, True)}}\n" +
            "    kdc = {{kdc_host}}\n" +
            "  }\n" +
            "\n" +
            "{# Append additional realm declarations below #}");
      }
    };

    final Config configKerberosEnv = createNiceMock(Config.class);
    expect(configKerberosEnv.getProperties()).andReturn(propertiesKerberosEnv).anyTimes();
    expect(configKerberosEnv.getTag()).andReturn("tag1").anyTimes();

    final Config configKrb5Conf = createNiceMock(Config.class);
    expect(configKrb5Conf.getProperties()).andReturn(propertiesKrb5Conf).anyTimes();
    expect(configKrb5Conf.getTag()).andReturn("tag1").anyTimes();

    final Cluster cluster = createNiceMock(Cluster.class);
    expect(cluster.getDesiredConfigByType("kerberos-env")).andReturn(configKerberosEnv).once();
    expect(cluster.getDesiredConfigByType("krb5-conf")).andReturn(configKrb5Conf).once();

    final Clusters clusters = createNiceMock(Clusters.class);
    expect(clusters.getClusters()).andReturn(Collections.singletonMap("c1", cluster));

    expect(controller.getClusters()).andReturn(clusters).once();

    expect(cluster.getConfigsByType("kerberos-env"))
        .andReturn(Collections.singletonMap("tag1", configKerberosEnv))
        .once();
    expect(cluster.getConfigsByType("krb5-conf"))
        .andReturn(Collections.singletonMap("tag1", configKerberosEnv))
        .once();

    expect(cluster.getDesiredConfigByType("kerberos-env"))
        .andReturn(configKerberosEnv)
        .once();
    expect(cluster.getDesiredConfigByType("krb5-conf"))
        .andReturn(configKerberosEnv)
        .once();

    Capture<Cluster> clusterCapture = newCapture(CaptureType.ALL);
    Capture<String> typeCapture = newCapture(CaptureType.ALL);
    Capture<Map> propertiesCapture = newCapture(CaptureType.ALL);
    Capture<String> tagCapture = newCapture(CaptureType.ALL);
    Capture<Map> attributesCapture = newCapture(CaptureType.ALL);


    expect(controller.createConfig(capture(clusterCapture), capture(typeCapture),
        capture(propertiesCapture), capture(tagCapture), capture(attributesCapture) ))
        .andReturn(createNiceMock(Config.class))
        .anyTimes();

    replay(controller, dbAccessor, osFamily, cluster, configKerberosEnv, configKrb5Conf, clusters);

    final Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(AmbariManagementController.class).toInstance(controller);
        bind(DBAccessor.class).toInstance(dbAccessor);
        bind(OsFamily.class).toInstance(osFamily);
        bind(EntityManager.class).toInstance(entityManager);
      }
    });

    injector.getInstance(UpgradeCatalog240.class).updateKerberosConfigs();

    verify(controller, dbAccessor, osFamily, cluster, configKerberosEnv, configKrb5Conf, clusters);

    List<String> typeCaptureValues = typeCapture.getValues();
    Assert.assertEquals(2, typeCaptureValues.size());
    Assert.assertEquals("kerberos-env", typeCaptureValues.get(0));
    Assert.assertEquals("krb5-conf", typeCaptureValues.get(1));

    List<Map> propertiesCaptureValues = propertiesCapture.getValues();
    Assert.assertEquals(2, propertiesCaptureValues.size());

    Map<String, String> capturedCRProperties;

    capturedCRProperties = propertiesCaptureValues.get(0);
    Assert.assertNotNull(capturedCRProperties);
    Assert.assertFalse(capturedCRProperties.containsKey("kdc_host"));
    Assert.assertTrue(capturedCRProperties.containsKey("kdc_hosts"));

    for (String property : propertiesKerberosEnv.keySet()) {
      if ("kdc_host".equals(property)) {
        Assert.assertEquals(property, propertiesKerberosEnv.get(property), capturedCRProperties.get("kdc_hosts"));
      } else {
        Assert.assertEquals(property, propertiesKerberosEnv.get(property), capturedCRProperties.get(property));
      }
    }

    capturedCRProperties = propertiesCaptureValues.get(1);
    Assert.assertNotNull(capturedCRProperties);
    Assert.assertTrue(capturedCRProperties.containsKey("content"));

    for (String property : propertiesKerberosEnv.keySet()) {
      if ("content".equals(property)) {
        Assert.assertEquals(property, "[libdefaults]\n" +
            "  renew_lifetime = 7d\n" +
            "  forwardable = true\n" +
            "  default_realm = {{realm}}\n" +
            "  ticket_lifetime = 24h\n" +
            "  dns_lookup_realm = false\n" +
            "  dns_lookup_kdc = false\n" +
            "  #default_tgs_enctypes = {{encryption_types}}\n" +
            "  #default_tkt_enctypes = {{encryption_types}}\n" +
            "{% if domains %}\n" +
            "[domain_realm]\n" +
            "{%- for domain in domains.split(',') %}\n" +
            "  {{domain|trim()}} = {{realm}}\n" +
            "{%- endfor %}\n" +
            "{% endif %}\n" +
            "[logging]\n" +
            "  default = FILE:/var/log/krb5kdc.log\n" +
            "  admin_server = FILE:/var/log/kadmind.log\n" +
            "  kdc = FILE:/var/log/krb5kdc.log\n" +
            "\n" +
            "[realms]\n" +
            "  {{realm}} = {\n" +
            "{%- if kdc_hosts > 0 -%}\n" +
            "{%- set kdc_host_list = kdc_hosts.split(',')  -%}\n" +
            "{%- if kdc_host_list and kdc_host_list|length > 0 %}\n" +
            "    admin_server = {{admin_server_host|default(kdc_host_list[0]|trim(), True)}}\n" +
            "{%- if kdc_host_list -%}\n" +
            "{% for kdc_host in kdc_host_list %}\n" +
            "    kdc = {{kdc_host|trim()}}\n" +
            "{%- endfor -%}\n" +
            "{% endif %}\n" +
            "{%- endif %}\n" +
            "{%- endif %}\n" +
            "  }\n" +
            "\n" +
            "{# Append additional realm declarations below #}", capturedCRProperties.get("content"));
      } else {
        Assert.assertEquals(property, propertiesKerberosEnv.get(property), capturedCRProperties.get(property));
      }
    }
  }

  @Test
  public void testUpdateKerberosConfigurationWithChangedKrb5ConfContent() throws Exception {
    final AmbariManagementController controller = createNiceMock(AmbariManagementController.class);
    final DBAccessor dbAccessor = createNiceMock(DBAccessor.class);
    final OsFamily osFamily = createNiceMock(OsFamily.class);

    final Map<String, String> propertiesKerberosEnv = new HashMap<String, String>() {
      {
        put("realm", "EXAMPLE.COM");
        put("encryption_types", "aes des3-cbc-sha1 rc4 des-cbc-md5");
        put("kdc_host", "c6407.ambari.apache.org");
        put("admin_server_host", "c6407.ambari.apache.org");
        put("kdc_type", "mit-kdc");
      }
    };

    final Map<String, String> propertiesKrb5Conf = new HashMap<String, String>() {
      {
        put("content", "CHANGED CONTENT");
      }
    };

    final Config configKerberosEnv = createNiceMock(Config.class);
    expect(configKerberosEnv.getProperties()).andReturn(propertiesKerberosEnv).anyTimes();
    expect(configKerberosEnv.getTag()).andReturn("tag1").anyTimes();

    final Config configKrb5Conf = createNiceMock(Config.class);
    expect(configKrb5Conf.getProperties()).andReturn(propertiesKrb5Conf).anyTimes();
    expect(configKrb5Conf.getTag()).andReturn("tag1").anyTimes();

    final Cluster cluster = createNiceMock(Cluster.class);
    expect(cluster.getDesiredConfigByType("kerberos-env")).andReturn(configKerberosEnv).once();
    expect(cluster.getDesiredConfigByType("krb5-conf")).andReturn(configKrb5Conf).once();

    final Clusters clusters = createNiceMock(Clusters.class);
    expect(clusters.getClusters()).andReturn(Collections.singletonMap("c1", cluster));

    expect(controller.getClusters()).andReturn(clusters).once();

    expect(cluster.getConfigsByType("kerberos-env"))
        .andReturn(Collections.singletonMap("tag1", configKerberosEnv))
        .once();

    expect(cluster.getDesiredConfigByType("kerberos-env"))
        .andReturn(configKerberosEnv)
        .once();

    Capture<Cluster> clusterCapture = newCapture(CaptureType.ALL);
    Capture<String> typeCapture = newCapture(CaptureType.ALL);
    Capture<Map> propertiesCapture = newCapture(CaptureType.ALL);
    Capture<String> tagCapture = newCapture(CaptureType.ALL);
    Capture<Map> attributesCapture = newCapture(CaptureType.ALL);


    expect(controller.createConfig(capture(clusterCapture), capture(typeCapture),
        capture(propertiesCapture), capture(tagCapture), capture(attributesCapture)))
        .andReturn(createNiceMock(Config.class))
        .anyTimes();

    replay(controller, dbAccessor, osFamily, cluster, configKerberosEnv, configKrb5Conf, clusters);

    final Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(AmbariManagementController.class).toInstance(controller);
        bind(DBAccessor.class).toInstance(dbAccessor);
        bind(OsFamily.class).toInstance(osFamily);
        bind(EntityManager.class).toInstance(entityManager);
      }
    });

    injector.getInstance(UpgradeCatalog240.class).updateKerberosConfigs();

    verify(controller, dbAccessor, osFamily, cluster, configKerberosEnv, configKrb5Conf, clusters);

    List<String> typeCaptureValues = typeCapture.getValues();
    Assert.assertEquals(1, typeCaptureValues.size());
    Assert.assertEquals("kerberos-env", typeCaptureValues.get(0));

    List<Map> propertiesCaptureValues = propertiesCapture.getValues();
    Assert.assertEquals(1, propertiesCaptureValues.size());

    Map<String, String> capturedCRProperties;

    capturedCRProperties = propertiesCaptureValues.get(0);
    Assert.assertNotNull(capturedCRProperties);
    Assert.assertFalse(capturedCRProperties.containsKey("kdc_host"));
    Assert.assertTrue(capturedCRProperties.containsKey("kdc_hosts"));

    for (String property : propertiesKerberosEnv.keySet()) {
      if ("kdc_host".equals(property)) {
        Assert.assertEquals(property, propertiesKerberosEnv.get(property), capturedCRProperties.get("kdc_hosts"));
      } else {
        Assert.assertEquals(property, propertiesKerberosEnv.get(property), capturedCRProperties.get(property));
      }
    }
  }
}

