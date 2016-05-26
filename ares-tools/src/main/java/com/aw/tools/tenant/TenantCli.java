package com.aw.tools.tenant;

import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import org.apache.commons.cli.*;

/**
 * Manage the process of firing a scale test at a platform
 */
public class TenantCli {


	enum Ops {
		PROVISION,
		UNPROVISION;
	}

	static Option OPERATION = OptionBuilder
		.withDescription("Operation")
		.hasArg()
		.withArgName("op")
		.create("op");

	static Option TID = OptionBuilder
		.withDescription("tenant id")
		.hasArg()
		.withArgName("tid")
		.create("tid");

	private static Options OPTIONS = new Options() { {
		addOption(OPERATION);
		addOption(TID);
	} };

	public static void main(String[] args) throws Exception {

		CommandLine cli = new BasicParser().parse(OPTIONS, args);

		for ( int i = 0; i < cli.getArgList().size() ; i++) {
			System.out.println(cli.getArgList().get(i));

		}
		//get the required options

		Ops op = Ops.valueOf(cli.getOptionValue(OPERATION.getOpt()));
		String tid = cli.hasOption(TID.getOpt()) ? cli.getOptionValue(TID.getOpt()) : "ALL";


		PlatformClient client = new PlatformClient(PlatformMgr.getCachedPlatform());
		DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, PlatformMgr.getCachedPlatform());


		if (op == Ops.UNPROVISION) {
			if (tid.equals("ALL")) { //remove all but system tenant
				for (Document doc : docs.getAllTenants()) {
					Tenant t = new Tenant(doc.getBody());
					if (!t.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
						System.out.println("unprovisioning tenant " + t.getTenantID());
						client.unProvision(t.getTenantID());
					}

				}
			}
			else {
				client.unProvision(tid);
			}
		}
		else {
			throw new UnsupportedOperationException(" operation " + op + " not supported");
		}


	}

}
