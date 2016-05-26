package com.aw.util;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Rest;

/**
 * Base class for rest servers in the platform.
 *
 *
 *
 */
public abstract class RestServer implements Runnable {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RestServer.class);

	private static final String HK2_GENERATED = "__HK2_Generated_";

	/**
	 * Construct a rest server rooted at the given base path bound to the given port
	 *
	 * @param basePath The base path
	 * @param port The server port
	 */
	public RestServer(String basePath, NodeRole role, int port) {
		m_basePath = basePath;
		m_role = role;
		m_port = port;
	}

    public RestServer init(Platform platform) throws Exception {

//    	//initialize guice
//    	initGuice();

    	String webappDirLocation;
        if (m_basePath != null) {
            webappDirLocation = new File(m_basePath + File.separatorChar + "WebContent")
                    .getAbsolutePath();
        } else {
            webappDirLocation = new File("")
                    .getAbsolutePath() + File.separatorChar + "WebContent";
        }

        PlatformNode node = null;

        try {
             node = platform.getNode(m_role);
        }
        catch (Exception ex) {
            logger.warn(" Platform information not loaded due to exception " + ex.getMessage());
        }

        int port = this.m_port < 1 ?
                node.getSettingInt(Rest.PORT)
              : this.m_port;

        // start the rest service
        m_server = new Server(port);
        for (Connector connector : m_server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                HttpConnectionFactory connectionFactory = connector
                        .getConnectionFactory(HttpConnectionFactory.class);
                connectionFactory.getHttpConfiguration()
                        .setRequestHeaderSize(EnvironmentSettings.getJettyHeaderSizeKb() * 1024);
            }
        }

        //set up the webapp root
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        m_server.setHandler(root);

        return this;
    }

//    /**
//     * Statically set up guice - jersey's hk2 stuff uses all static singletons so this has to be static
//     */
//    private static void initGuice() throws Exception {
//
//    	if (guiceInitialized) {
//    		return;
//    	}
//
//    	//install guice to work with jersey 2
//    	JerseyGuiceUtils.install(new GuiceLocator());
//
//    	guiceInitialized = true;
//
//    }
//	private static boolean guiceInitialized;

    public RestServer addLifeCycleListener(LifeCycle.Listener listener) {
        m_server.addLifeCycleListener(listener);
        return this;
    }

    public RestServer stop() throws Exception {
        m_server.stop();
        return this;
    }

    /**
     * @return The executor service used to run the server
     */
    protected ExecutorService newExecutorService() { return Executors.newCachedThreadPool(); }

    /**
     * Start the server
     * @return The server
     * @throws Exception If anything goes wrong
     */
    public synchronized RestServer start() throws Exception {

    	if (m_service == null) {
    		m_service = newExecutorService();
    	}

    	m_service.submit(this);

        return this;

    }
    protected ExecutorService m_service = null;

    /**
     * Runs the server
     */
    public void run() {
        try {
            logger.info("starting service " + this.getClass().getTypeName());
            m_server.start();
            logger.info(" running post-start " + this.getClass().getTypeName());
            postStart();
			logger.info(" completed post-start " + this.getClass().getTypeName());
            m_server.join();
			//logger.error(" debug joined");
        }

        catch ( Exception e) {

        	//TODO: handle this in the platform, this is a very severe error
        	logger.error("error starting web server", e);

            throw new InitializationException("error starting web server, base_path=" + m_basePath + " port=" + getPort(), e);

        }
    }

    /**
     * Actions to take on the thread that calls start on the jetty server, immediately after start is called
     */
    protected abstract void postStart() throws Exception;

    public String getBasePath() { return m_basePath; }
	private String m_basePath;

	public int getPort() { return m_port; }
	protected void setPort(int port) { m_port = port; }
	private int m_port;

	public Server getServer() { return m_server; }
	private Server m_server;

	public NodeRole getRole() { return m_role; }
	private NodeRole m_role;

//	private static class GuiceLocator implements ServiceLocatorGenerator {
//
//		private Injector injector;
//
//		public GuiceLocator() {
//
//			//create an injectr for the service cluster
//
//			this.injector = Guice.createInjector(
//				new ServletModule(), //standard servlet injection
//				new ServiceClusterModule()); //dawinjection for service cluster
//
//		}
//
//		@Override
//		public ServiceLocator create(String name, ServiceLocator parent) {
//
//			if (!name.startsWith(HK2_GENERATED)) {
//				return null;
//			}
//
//			//set up child modules
//			List<Module> modules = new ArrayList<>();
//			modules.add(new JerseyGuiceModule(name));
//
//			//return the service locator
//			return injector.createChildInjector(modules).getInstance(ServiceLocator.class);
//
//		}
//
//	}

}
