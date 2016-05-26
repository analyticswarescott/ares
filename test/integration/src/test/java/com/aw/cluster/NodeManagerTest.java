package com.aw.cluster;

import com.aw.BaseIntegrationTest;

public class NodeManagerTest extends BaseIntegrationTest {

//    public NodeManagerTest() {
//        super();
//
//        String confDirectoryPath = getConfDirectory();
//        File confDirectory = new File(confDirectoryPath);
//        if ( !confDirectory.exists() || !confDirectory.isDirectory() ) {
//            throw new RuntimeException("Missing or invalid CONF_DIRECTORY environment variable. Please supply this to proceed.");
//        }
//
//        Map<String, String> environment = new HashMap<>();
//        environment.put( "MANUAL_SERVICES_START", "false"  );
//        environment.put( "UNITY_ES_HOST", "localhost"  );
//        environment.put( "SYS_SERVER_URL", "localhost:2181"  );
//        environment.put( "ZOO_ADDR", "localhost:2181"  );
//
//
//        environment.put("DISABLE_AUTH_VALIDATION", "false");
//        environment.put( "TEST_MODE", "false"  );  //TEST_MODE true means that the test wrappers are used for node role management
//        environment.put("CLEAR_DOCS_ON_STARTUP", "true");
//
//        File f = new File(".");
//        String path = f.getAbsolutePath();
//
//
//        File ffc = new File(path).getParentFile().getParentFile().getParentFile();
//        String confPath = ffc.getAbsolutePath() + File.separatorChar + "dg2" + File.separatorChar + "conf";
//        environment.put( "CONF_DIRECTORY", confPath);
//
//        //TODO: create a way to specify local core dir for tests, or run this test outside the suite with params
//        environment.put( "DG_HOME", "/Users/scott");
//        environment.put("DG_HOST", "localhost"); //to avoid announcing presence as local host by name
//        environment.put( "DG_SPARK_HOME", "/Users/scott/roles/spark");
//
//        File ff = new File(path).getParentFile().getParentFile().getParentFile();
//        String libPath = ff.getAbsolutePath() + File.separatorChar + "dgcore" + File.separatorChar + "compute" + File.separatorChar + "target" + File.separatorChar + "lib";
//        environment.put("SPARK_LIB_HOME", libPath);
//
//        environment.put("DG_REPORTING_SERVICE_BASE_URL", "http://localhost:9099");
////        setEnv(environment);
//        //testDataDir = this.getClass().getClassLoader().ge
//
//    }
//
//    @Override
//    protected boolean usesSpark() {
//    	return false;
//    }
//    @Override
//    protected boolean startsSpark() {
//    	return false;
//    }
//
//	@Override
//	protected boolean usesElasticsearch() {return false;}
//	@Override
//	protected boolean usesKafka() {return false;}
//
//
//	@Test
//	public void roletest() throws Exception{
//		initAndConfigureNodeRole(NodeRole.HDFS_NAME);
//		initAndConfigureNodeRole(NodeRole.ZOOKEEPER);
//	}
//
//	private RoleManager initAndConfigureNodeRole(NodeRole role) throws Exception {
//		SecurityUtil.setThreadSystemAccess();
//		RoleManager roleManager = role.newRoleManager();
//		Platform p = getPlatform();
//
//		PlatformNode node = p.getNode(role);
//
//		roleManager.init(node);
//
//		roleManager.configure();
//
//		//NodeResource nr = new NodeResource();
//		//nr.updateNode(node)
//
//		return roleManager;
//	}
//
//
//	/**
//     * Create an incident, assign it, and close it
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testSingleNodeStart() throws Exception {
//
//
//            //TODO: only using this test to execute and check config
//            new PlatformController().configurePlatformNodes();
//
//
//
//    }
//
//
//    private int countJavaProcs(String lookFor) throws Exception {
//        List<String> cmd = new ArrayList<String>();
//        cmd.add("ps");
//        cmd.add("-ef");
//        CommandResult cr = SysCommandExecutor.executeSimple(cmd);
//
//        String s = cr.stdOut.toString();
//
//        int cnt = 0;
//        String[] lines = s.split(System.getProperty("line.separator"));
//        for (String l : lines) {
//            if (l.contains(lookFor) && (! l.contains("IntelliJ"))) {
//                System.out.println(l);
//                cnt++;
//            }
//        }
//
//        return cnt;
//    }

}
