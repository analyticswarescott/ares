package com.aw.compute.streams.processor.framework;

import com.aw.common.zookeeper.ZkAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.Initializable;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;

/**
 * Base class for all stream handlers in the system
 */
public abstract class AbstractStreamHandler implements StreamHandler, Initializable<StreamDef> {

	public static final Logger logger = LoggerFactory.getLogger(AbstractStreamHandler.class);

	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	public ZkAccessor getZk() {return zk;}
	public void setZk(ZkAccessor zk) {this.zk = zk;}
	private ZkAccessor zk;

    /**
     * @return The name of this stream handler
     */
    public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

}
