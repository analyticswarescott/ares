package com.aw.common.system;

/**
 * Temporary build stamp that code can echo to verify patch application
 *
 * TODO: scott - can we remove this? we need to chat about doing a real buildstamp that includes a version
 *   TODO: If we remove this, we have no current way to be sure the jars actually got updated by patch.sh,
 *   which occurs while the node is offline so is difficult to track 100% at this point
 *
 */
public class TmpVersion {
	public static String VERSION = "SLC-1127PR.03";
}