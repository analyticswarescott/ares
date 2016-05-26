package com.aw.platform.monitoring.os;

import com.aw.platform.Platform;

import java.time.Instant;

/**
 * Class to manage or OS-spoof interaction with sysstat
 */
public interface SysStatInterface {


	String getRawData(Platform p, Instant t) throws Exception;
	String getRawNetworkData(Platform p, Instant t) throws Exception;

}
