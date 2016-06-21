package com.aw;


import org.apache.hadoop.hdfs.MiniDFSCluster;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.File;

public class PostgresqlServiceWrapper {


	private int port = 0;
	private PostgresStarter<PostgresExecutable, PostgresProcess>
		runtime = PostgresStarter.getDefaultInstance();
	PostgresProcess postgresProcess = null;

	private MiniDFSCluster cluster;

	public static final String LOCAL_CACHE_DIR = "/data/hdfs_cache";

	public boolean start() throws Exception {
		final PostgresConfig config = PostgresConfig.defaultWithDbName("configdb");
		PostgresExecutable executable = runtime.prepare(config);
		postgresProcess = executable.start();
		return postgresProcess.isProcessRunning();
	}

	public boolean isRunning() {

		return postgresProcess != null && postgresProcess.isProcessRunning();
	}

	public void stop() {
		if(postgresProcess != null && postgresProcess.isProcessRunning()) {
			postgresProcess.stop();
		}
	}
}
