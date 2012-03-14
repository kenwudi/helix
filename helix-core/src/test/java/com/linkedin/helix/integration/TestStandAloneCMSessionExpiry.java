package com.linkedin.helix.integration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.linkedin.helix.DummyProcessThread;
import com.linkedin.helix.InstanceType;
import com.linkedin.helix.TestHelper;
import com.linkedin.helix.TestHelper.StartCMResult;
import com.linkedin.helix.manager.zk.ZKHelixManager;
import com.linkedin.helix.manager.zk.ZNRecordSerializer;
import com.linkedin.helix.manager.zk.ZkClient;
import com.linkedin.helix.tools.ClusterSetup;
import com.linkedin.helix.tools.ClusterStateVerifier;

public class TestStandAloneCMSessionExpiry extends ZkIntegrationTestBase
{
  private static Logger LOG = Logger.getLogger(TestStandAloneCMSessionExpiry.class);
  protected final String CLUSTER_NAME = "CLUSTER_" + "TestStandAloneCMSessionExpiry";
  protected static final int NODE_NR = 5;
  protected Map<String, StartCMResult> _startCMResultMap = new HashMap<String, StartCMResult>();

  class ZkClusterManagerWithSessionExpiry extends ZKHelixManager
  {
    public ZkClusterManagerWithSessionExpiry(String clusterName, String instanceName,
                                             InstanceType instanceType,
                                             String zkConnectString) throws Exception
    {
      super(clusterName, instanceName, instanceType, zkConnectString);
      // TODO Auto-generated constructor stub
    }

    public void expireSession() throws Exception
    {
      ZkIntegrationTestBase.simulateSessionExpiry(_zkClient);
    }
  }

  @Test()
  public void testStandAloneCMSessionExpiry()
    throws Exception
  {
    System.out.println("RUN testStandAloneCMSessionExpiry() at " + new Date(System.currentTimeMillis()));


    ZkClient zkClient = new ZkClient(ZK_ADDR);
    zkClient.setZkSerializer(new ZNRecordSerializer());
    ClusterSetup setupTool = new ClusterSetup(ZK_ADDR);

    TestHelper.setupCluster(CLUSTER_NAME,
                            ZK_ADDR,
                            12918,
                            PARTICIPANT_PREFIX,
                            "TestDB",
                            1,
                            20,
                            NODE_NR,
                            3,
                            "MasterSlave",
                            true);
    // start dummy participants
    Map<String, ZkClusterManagerWithSessionExpiry> managers = new HashMap<String, ZkClusterManagerWithSessionExpiry>();
    for (int i = 0; i < NODE_NR; i++)
    {
      String instanceName = "localhost_" + (12918 + i);
      ZkClusterManagerWithSessionExpiry manager = new ZkClusterManagerWithSessionExpiry(CLUSTER_NAME,
                                                                                        instanceName,
                                                                                        InstanceType.PARTICIPANT,
                                                                                        ZK_ADDR);
      managers.put(instanceName, manager);
      Thread thread = new Thread(new DummyProcessThread(manager, instanceName));
      thread.start();
    }

    // start controller
    String controllerName = "controller_0";

    ZkClusterManagerWithSessionExpiry manager = new ZkClusterManagerWithSessionExpiry(CLUSTER_NAME,
                                                                                      controllerName,
                                                                                      InstanceType.CONTROLLER,
                                                                                      ZK_ADDR);
    manager.connect();
    managers.put(controllerName, manager);

    boolean result = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewZkVerifier(ZK_ADDR, CLUSTER_NAME));
    Assert.assertTrue(result);

    managers.get("localhost_12918").expireSession();

    setupTool.addResourceToCluster(CLUSTER_NAME, "MyDB", 10, "MasterSlave");
    setupTool.rebalanceStorageCluster(CLUSTER_NAME, "MyDB", 3);

    result = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewZkVerifier(ZK_ADDR, CLUSTER_NAME));
    Assert.assertTrue(result);

    managers.get(controllerName).expireSession();

    setupTool.addResourceToCluster(CLUSTER_NAME, "MyDB2", 8, "MasterSlave");
    setupTool.rebalanceStorageCluster(CLUSTER_NAME, "MyDB2", 3);

    result = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewZkVerifier(ZK_ADDR, CLUSTER_NAME));
    Assert.assertTrue(result);

    System.out.println("STOP testStandAloneCMSessionExpiry() at " + new Date(System.currentTimeMillis()));
  }

}