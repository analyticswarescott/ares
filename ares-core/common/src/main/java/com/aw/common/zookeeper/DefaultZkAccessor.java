package com.aw.common.zookeeper;

import com.aw.common.cluster.ClusterException;
import com.aw.common.system.structure.Hive;
import com.aw.common.system.structure.PathResolverTenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.Zookeeper;
import com.aw.util.Statics;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Class for accessing Zookeeper -- intialized with a Hive
 */
public class DefaultZkAccessor implements ZkAccessor {

	public static final Logger LOGGER = LoggerFactory.getLogger(DefaultZkAccessor.class);

	public static List<String> KNOWN_3P_ROOTS = new ArrayList<>(Arrays.asList(new String[]{"zookeeper", "config",
		"spark", "brokers", "admin", "consumers", "controller", "controller_epoch"}));


	protected Hive hive;
	protected String root;


	public DefaultZkAccessor(Platform platform, Hive hive) {
	   this.hive = hive;
		this.m_platform = platform;
		root = platform.getSettings(NodeRole.ZOOKEEPER).getSetting(Zookeeper.DG_ROOT_NODE);
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		cleanup();
	}

	public DefaultZkAccessor(Platform platform, CuratorFramework client, Hive hive) {
		this.hive = hive;
		m_client = client;
		root = platform.getSettings(NodeRole.ZOOKEEPER).getSetting(Zookeeper.DG_ROOT_NODE);
	}


	public void deleteTenantRoot(String tenantID) throws Exception {
       Path tenantRoot = hive.getPathResolver().getTenantRoot(new Path(root));
		delete(tenantRoot.toString());

	}
	public void createTenantRoot(String tenantID) throws Exception {
		Path tenantRoot = hive.getPathResolver().getTenantRoot(new Path(root));
		ensure(tenantRoot.toString());
	}


	public void init() throws Exception {

		if (initialized) {
			return;
		}

		//TODO: don't initialize cluster until platform is ready

		//start the zk client
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 5); //TODO: configurable
		m_client = CuratorFrameworkFactory.newClient(PlatformUtils.getZkConnectString(m_platform), retryPolicy);
		m_client.start();

		while (m_client.getState() != CuratorFrameworkState.STARTED) {
			Thread.sleep(500);
		}

		LOGGER.debug("  CLIENT started for ZkAccessor: " + hive);
		initialized = true;

	}


	@Override
	public boolean exists(ZkPurpose purpose, Object key) throws Exception {
		String strKey = String.valueOf(key);
		String path = getAbsoluteZkPath(purpose, strKey);
		LOGGER.debug("checking PURPOSE exists for path: " + path);
		return exists(path);

	}

	@Override
	public boolean exists(String path) throws Exception {
		LOGGER.debug("checking RAW exists for path: " + path);
		return m_client.checkExists().forPath(path) != null;
	}



	private String toKey(Object key) {
		String strKey = String.valueOf(key);
		return strKey;
	}

	/**
	 * Get the data from zk
	 */
	@Override
	public <T> T get(ZkPurpose purpose, Object key, Class<T> type) throws Exception {

		T ret = null;

		try {

			String strKey = toKey(key);
			String path = getAbsoluteZkPath(purpose, strKey);

			LOGGER.debug( " getfor path "  + path);
			//convert the json at the node back into a java object
			byte[] data = null;
			if (m_client.checkExists().forPath(path) != null) {
				data = m_client.getData().forPath(path);
			}

			if (data != null) {
				ret = get(data, type);
			}

		} catch (Exception e) {
			throw new Exception("error getting data Purpose = " + purpose + ", key=" + key, e);
		}

		return ret;

	}


	@Override
	public <T> T get(String path, Class<T> type) throws Exception {
		return get(m_client.getData().forPath(path), type);
	}


	/**
	 * deserialize all java objects under the given base key, checking recursively for objects
	 *
	 * @param baseKey the base key under which the objects can be found
	 * @param type the type of object stored in the leaf nodes. all leaf nodes must contain the same object type
	 * @return the list of objects contain in the leaf node data
	 * @throws Exception if anything goes wrong
	 */
	public <T> List<T> getAll(ZkPurpose purpose, String baseKey, Class<T> type) throws Exception {

		//get a list of all data form leaf nodes under this key
		List<T> ret = new ArrayList<T>();
		String path = getAbsoluteZkPath(purpose, baseKey);

		//make sure the path is there
		if (exists(path)) {
			allLeaves(path, ret, m_client.getChildren().forPath(path), type);
		}

		return ret;

	}

	/**
	 * deserialize all java objects under the given base key, checking recursively for objects
	 *
	 * @param baseKey the base key under which the objects can be found
	 * @param type the type of object stored in the leaf nodes. all leaf nodes must contain the same object type
	 * @return the list of objects contain in the leaf node data
	 * @throws Exception if anything goes wrong
	 */
	public <T> List<T> getAll(String path, Class<T> type) throws Exception {

		//get a list of all data form leaf nodes under this key
		List<T> ret = new ArrayList<T>();

		//make sure the path is there
		if (exists(path)) {
			allLeaves(path, ret, m_client.getChildren().forPath(path), type);
		}

		return ret;

	}


	public List<String> getChildren(String path) throws Exception{
		return m_client.getChildren().forPath(path);
	}

	<T> void allLeaves(String parent, List<T> dest, List<String> paths, Class<T> type) throws Exception {

		for (String path : paths) {

			//build the absolute path
			if (parent.equals("/")) {
				path = parent + path;
			}
			else {
				path = parent + "/" + path;
			}

			byte[] data = m_client.getData().forPath(path);

			//if we have data, build an object
			if (data != null && data.length > 0) {
				dest.add(get(data, type));
			}

			//recurse to get sub-path data
			allLeaves(path, dest, m_client.getChildren().forPath(path), type);

		}

	}

	/**
	 *
	 *
	 * @param rawData
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public <T> T get(byte[] rawData, Class<T> type) throws Exception {

		//return value will be null if no data is found
		T ret = null;

		//if there's data, build the return object
		if (rawData != null) {
			String strData = new String(rawData, Statics.CHARSET);

			ret = JSONUtils.objectFromString(strData, type);
		}

		return ret;

	}

	/**
	 * ensure path in ZK
	 */
	@Override
	public void ensure(String path) throws ClusterException {

		try {

			EnsurePath ep = new EnsurePath(path);
			ep.ensure(m_client.getZookeeperClient());

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ClusterException("error ensuring path , path=" + path, e);
		}

	}



	/**
	 * Put the data in zk
	 */
	@Override
	public void put(ZkPurpose purpose, Object key, Object value) throws ClusterException {

		try {

			//get the values we need to set the json data
			String strKey = toKey(key);

			String strValue = JSONUtils.objectToString(value);

			//for tenant writes, fail if tenant root has not been provisioned
			if (hive.getPathResolver() instanceof PathResolverTenant) {
				Path tenantRoot = hive.getPathResolver().getTenantRoot(new Path(root));
				if (!exists(tenantRoot.toString())) {
					throw new ClusterException(" tenant root does not exist :" + tenantRoot.toString());
				}
			}

			String path = getAbsoluteZkPath(purpose, strKey);

			EnsurePath ep = new EnsurePath(path);
			ep.ensure(m_client.getZookeeperClient());

			//set the data on the zk node
			m_client.setData().forPath(path, strValue.getBytes(Statics.CHARSET));

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ClusterException("error putting data, purpose=" + purpose + " key=" + key, e);
		}

	}

	public void delete(ZkPurpose purpose, Object key) throws ClusterException {

		try {

			//get the values we need to set the json data
			String strKey = toKey(key);
			String path = getAbsoluteZkPath(purpose, strKey);

			m_client.delete().deletingChildrenIfNeeded().forPath(path);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ClusterException("error deleting data, purpose=" + purpose + " key=" + key, e);
		}

	}

	public void delete( String path) throws ClusterException {

		try {
			m_client.delete().deletingChildrenIfNeeded().forPath(path);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ClusterException("error deleting data, path=" + path , e);
		}

	}

	@Override
	public void cleanup() {
		m_client.close();
	}

	public String getAbsoluteZkPath(ZkPurpose purpose, String key) {

		String p = hive.getPathResolver().getPurposeRoot(new Path(root), purpose).toString();

		if (key.length() == 0 || key == null) {
		}
		else {
			p = p + "/" +  key;
		}
		return  p;

	}

	//the zk curator client
	private CuratorFramework m_client = null;

	//the platform in which this cluster is running
	private Platform m_platform;

	//whether this cluster is initialized
	protected boolean isInitialized() { return initialized; }
	private boolean initialized;





	@Override
	public StringBuffer listZK(String path, boolean excludeKnown3pRoots) throws Exception {
		StringBuffer ret = new StringBuffer();
		listKids( path, ret, excludeKnown3pRoots);
		return ret;
	}

	private  void listKids(String path, StringBuffer record, boolean excludeKnown3pRoots) throws  Exception{
		LOGGER.debug("listZK: path is: " + path);
		for (String s : getChildren(path)) {

			if (path.equals("/") && KNOWN_3P_ROOTS.contains(s) && excludeKnown3pRoots) {
				LOGGER.debug(" excluding known 3p root:  " + s);
				continue;
			}

			if (path.equals("/")) {
				record.append(path + s + "\n");
				listKids(path + s, record, excludeKnown3pRoots);
			}
			else {
				record.append(path + "/" + s + "\n");
				listKids(path + "/" + s, record, excludeKnown3pRoots);
			}

		}


	}
}
