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
package org.apache.ambari.server.actionmanager;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.Role;
import org.apache.ambari.server.RoleCommand;
import org.apache.ambari.server.agent.ActionQueue;
import org.apache.ambari.server.agent.CommandReport;
import org.apache.ambari.server.controller.HostsMap;
import org.apache.ambari.server.orm.GuiceJpaInitializer;
import org.apache.ambari.server.orm.InMemoryDefaultTestModule;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.svccomphost.ServiceComponentHostStartEvent;
import org.apache.ambari.server.utils.StageUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import static org.junit.Assert.assertNotNull;

public class TestActionManager {

  private long requestId = 23;
  private long stageId = 31;
  private Injector injector;
  private String hostname = "host1";
  private String clusterName = "cluster1";

  private Clusters clusters;
  private UnitOfWork unitOfWork;
  private StageFactory stageFactory;

  @Before
  public void setup() throws AmbariException {
    injector = Guice.createInjector(new InMemoryDefaultTestModule());
    injector.getInstance(GuiceJpaInitializer.class);
    clusters = injector.getInstance(Clusters.class);
    stageFactory = injector.getInstance(StageFactory.class);

    clusters.addHost(hostname);
    clusters.getHost(hostname).persist();
    StackId stackId = new StackId("HDP-0.1");
    clusters.addCluster(clusterName, stackId);
    unitOfWork = injector.getInstance(UnitOfWork.class);
  }

  @After
  public void teardown() throws AmbariException {
    injector.getInstance(PersistService.class).stop();
  }

  @Test
  public void testActionResponse() throws AmbariException {
    ActionDBAccessor db = injector.getInstance(ActionDBAccessorImpl.class);
    ActionManager am = new ActionManager(5000, 1200000, new ActionQueue(),
        clusters, db, new HostsMap((String) null), unitOfWork,
        injector.getInstance(RequestFactory.class), null, null);
    populateActionDB(db, hostname);
    Stage stage = db.getAllStages(requestId).get(0);
    Assert.assertEquals(stageId, stage.getStageId());
    stage.setHostRoleStatus(hostname, "HBASE_MASTER", HostRoleStatus.QUEUED);
    db.hostRoleScheduled(stage, hostname, "HBASE_MASTER");
    List<CommandReport> reports = new ArrayList<CommandReport>();
    CommandReport cr = new CommandReport();
    cr.setTaskId(1);
    cr.setActionId(StageUtils.getActionId(requestId, stageId));
    cr.setRole("HBASE_MASTER");
    cr.setStatus("COMPLETED");
    cr.setStdErr("ERROR");
    cr.setStdOut("OUTPUT");
    cr.setStructuredOut("STRUCTURED_OUTPUT");
    cr.setExitCode(215);
    reports.add(cr);
    am.processTaskResponse(hostname, reports, stage.getOrderedHostRoleCommands());
    assertEquals(215,
        am.getAction(requestId, stageId).getExitCode(hostname, "HBASE_MASTER"));
    assertEquals(HostRoleStatus.COMPLETED, am.getAction(requestId, stageId)
        .getHostRoleStatus(hostname, "HBASE_MASTER"));
    assertEquals(
        "ERROR",
        am.getAction(requestId, stageId)
            .getHostRoleCommand(hostname, "HBASE_MASTER").getStderr());
    assertEquals(
        "OUTPUT",
        am.getAction(requestId, stageId)
            .getHostRoleCommand(hostname, "HBASE_MASTER").getStdout());
    assertEquals(
      "STRUCTURED_OUTPUT",
      am.getAction(requestId, stageId)
        .getHostRoleCommand(hostname, "HBASE_MASTER").getStructuredOut());
    assertNotNull(db.getRequest(requestId));
    assertFalse(db.getRequest(requestId).getEndTime() == -1);
  }

  @Test
  public void testLargeLogs() throws AmbariException {
    ActionDBAccessor db = injector.getInstance(ActionDBAccessorImpl.class);
    ActionManager am = new ActionManager(5000, 1200000, new ActionQueue(),
        clusters, db, new HostsMap((String) null), unitOfWork,
        injector.getInstance(RequestFactory.class), null, null);
    populateActionDB(db, hostname);
    Stage stage = db.getAllStages(requestId).get(0);
    Assert.assertEquals(stageId, stage.getStageId());
    stage.setHostRoleStatus(hostname, "HBASE_MASTER", HostRoleStatus.QUEUED);
    db.hostRoleScheduled(stage, hostname, "HBASE_MASTER");
    List<CommandReport> reports = new ArrayList<CommandReport>();
    CommandReport cr = new CommandReport();
    cr.setTaskId(1);
    cr.setActionId(StageUtils.getActionId(requestId, stageId));
    cr.setRole("HBASE_MASTER");
    cr.setStatus("COMPLETED");
    String errLog = Arrays.toString(new byte[100000]);
    String outLog = Arrays.toString(new byte[110000]);
    cr.setStdErr(errLog);
    cr.setStdOut(outLog);
    cr.setStructuredOut(outLog);
    cr.setExitCode(215);
    reports.add(cr);
    am.processTaskResponse(hostname, reports, stage.getOrderedHostRoleCommands());
    assertEquals(215,
        am.getAction(requestId, stageId).getExitCode(hostname, "HBASE_MASTER"));
    assertEquals(HostRoleStatus.COMPLETED, am.getAction(requestId, stageId)
        .getHostRoleStatus(hostname, "HBASE_MASTER"));
    assertEquals(
        errLog.length(),
        am.getAction(requestId, stageId)
            .getHostRoleCommand(hostname, "HBASE_MASTER").getStderr().length());
    assertEquals(
        outLog.length(),
        am.getAction(requestId, stageId)
            .getHostRoleCommand(hostname, "HBASE_MASTER").getStdout().length());
    assertEquals(
        outLog.length(),
        am.getAction(requestId, stageId)
            .getHostRoleCommand(hostname, "HBASE_MASTER").getStructuredOut().length());
  }

  private void populateActionDB(ActionDBAccessor db, String hostname) throws AmbariException {
    Stage s = stageFactory.createNew(requestId, "/a/b", "cluster1", 1L, "action manager test", "clusterHostInfo", "commandParamsStage", "hostParamsStage");
    s.setStageId(stageId);
    s.addHostRoleExecutionCommand(hostname, Role.HBASE_MASTER,
        RoleCommand.START,
        new ServiceComponentHostStartEvent(Role.HBASE_MASTER.toString(),
            hostname, System.currentTimeMillis()), "cluster1", "HBASE", false, false);
    List<Stage> stages = new ArrayList<Stage>();
    stages.add(s);
    Request request = new Request(stages, clusters);
    db.persistActions(request);
  }

  // Test failing ... tracked by Jira BUG-4966
  @Ignore
  @Test
  public void testCascadeDeleteStages() throws Exception {
    ActionDBAccessor db = injector.getInstance(ActionDBAccessorImpl.class);
    ActionManager am = injector.getInstance(ActionManager.class);
    populateActionDB(db, hostname);
    assertEquals(1, clusters.getClusters().size());

    clusters.getCluster(clusterName);

//    assertEquals(1, am.getRequests(BaseRequest.DEFAULT_PAGE_SIZE).size());

    clusters.deleteCluster(clusterName);

    assertEquals(0, clusters.getClusters().size());
//    assertEquals(0, am.getRequests().size());

  }

  @Test
  public void testGetActions() throws Exception {
    int requestId = 500;
    ActionQueue queue = createNiceMock(ActionQueue.class);
    ActionDBAccessor db = createStrictMock(ActionDBAccessor.class);
    Clusters clusters = createNiceMock(Clusters.class);
    Stage stage1 = createNiceMock(Stage.class);
    Stage stage2 = createNiceMock(Stage.class);
    List<Stage> listStages = new ArrayList<Stage>();
    listStages.add(stage1);
    listStages.add(stage2);

    // mock expectations
    expect(db.getLastPersistedRequestIdWhenInitialized()).andReturn(Long.valueOf(1000));
    expect(db.getAllStages(requestId)).andReturn(listStages);

    replay(queue, db, clusters);

    ActionManager manager = new ActionManager(0, 0, queue, clusters, db, null, unitOfWork,
        injector.getInstance(RequestFactory.class), null, null);
    assertSame(listStages, manager.getActions(requestId));

    verify(queue, db, clusters);
  }
}
