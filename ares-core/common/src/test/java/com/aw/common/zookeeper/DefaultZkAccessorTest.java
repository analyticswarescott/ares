package com.aw.common.zookeeper;

import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.structure.ZkPurpose;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by scott on 31/03/16.
 */
public class DefaultZkAccessorTest extends AbstractClusterTest {

	private ZkAccessor zk;

	private static String expectedTree = "/aw\n" +
		"/aw/system\n" +
		"/aw/system/cluster\n" +
		"/aw/system/cluster/foo\n" +
		"/aw/system/cluster/foo/child1\n" +
		"/aw/tenant\n" +
		"/aw/tenant/1\n" +
		"/aw/tenant/1/offsets\n" +
		"/aw/tenant/1/offsets/test1\n" +
		"/aw/tenant/2\n" +
		"/aw/tenant/2/offsets\n" +
		"/aw/tenant/2/offsets/test2\n";

	@Test
	public void test() throws Exception{

		DefaultZkAccessor zk = new DefaultZkAccessor(platform, Hive.SYSTEM);

		DefaultZkAccessor zkTenant = new DefaultZkAccessor(platform, Hive.TENANT);
		zkTenant.ensure("/aw/tenant/1");
		zkTenant.ensure("/aw/tenant/2");

		zk.put(ZkPurpose.CLUSTER, "foo", "1");
		zk.put(ZkPurpose.CLUSTER, "foo/child1", "2");

		try {
			SecurityUtil.setThreadSystemAccess();
			Impersonation.impersonateTenant("1");
			zkTenant.put(ZkPurpose.OFFSETS, "test1", "3");

			Impersonation.impersonateTenant("2");
			zkTenant.put(ZkPurpose.OFFSETS, "test2", "4");
		}
		finally {
			Impersonation.unImpersonate();
		}




		StringBuffer buf = zk.listZK( "/", true);

		//System.out.println(buf.toString());

		Assert.assertEquals(expectedTree, buf.toString());

	}


}
