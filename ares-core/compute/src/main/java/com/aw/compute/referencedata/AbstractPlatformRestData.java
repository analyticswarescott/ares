package com.aw.compute.referencedata;


import org.apache.log4j.Logger;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.RestClient;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Get reference data from a particular rest location known to the platform
 *
 *
 */
public abstract class AbstractPlatformRestData extends AbstractReferenceData implements ReferenceData {

	private static final Logger logger = Logger.getLogger(AbstractPlatformRestData.class);

	/**
	 * Constructs a basic platform rest data instance which can pull data periodically when requested.
	 *
	 * @param role The node type to connect to
	 * @param port The node setting that holds the REST server port to connect to
	 * @throws Exception
	 */
	public AbstractPlatformRestData(NodeRole role, RoleSetting port, Platform platform) throws Exception {
		initialize(role, port, platform);
	}

	private void initialize(NodeRole role, RoleSetting port, Platform platform) throws Exception {

		//create our rest client
		m_client = new RestClient(role, port, platform);

	}

	/**
	 * Reference-data-type-specific logic related to checking if we need to refresh our data - the default
	 * implementation of this will return true if the check payload has changed since the last time we
	 * pulled the payload, or if the last payload is null (i.e. we haven't checked before)
	 *
	 * @param checkPayload The payload of the check response
	 * @return Whether we need to refresh
	 * @throws StreamProcessingException If anything goes wrong
	 */
	protected boolean shouldRefresh(String checkPayload) throws ProcessingException {

		try {

			//if we haven't gotten any data yet, we need to refresh
			if (m_lastPayload == null) {
				return true;
			}

			else {

				//else compare - we should refresh is the payloads have changed
				return !m_lastPayload.equals(checkPayload);

			}

		} finally {

			//the last payload is now this payload
			m_lastPayload = checkPayload;

		}

	}
	String m_lastPayload = null;

	/**
	 * Reference-data-type-specific logic to refresh the reference data.
	 *
	 * @param pullPayload The payload of the response to the pull request
	 * @throws StreamProcessingException
	 */
	protected abstract void refresh(String pullPayload) throws ProcessingException;

	/**
	 * @return The path with which we will check for new reference data
	 */
	protected abstract String getCheckPath();

	/**
	 * @return The path with which we will pull new reference data
	 */
	protected abstract String getPullPath();

	/**
	 * Check our reference data, refresh if needed
	 *
	 * @throws StreamProcessingException
	 */
	public void onTTLExpired() throws StreamProcessingException {

		try {

			String checkPath = getCheckPath();
			String pullPath = getPullPath();

			String checkPayload = getClient().getString(checkPath);
			if (shouldRefresh(checkPayload)) {

				logger.info("pulling reference data " + getClass().getSimpleName() + " from " + getClient().getRole());

				//if check path == pull path, just use that response, otherwise get the pull path response
				String pullPayload = checkPath.equals(pullPath) ? checkPayload : getClient().getString(pullPath);

				//refresh our data
				refresh(pullPayload);

			}

		} catch (Exception e) {
			throw new StreamProcessingException("error checking TTL / pulling reference data", e);
		}

	}

	//our REST server
	protected RestClient getClient() { return m_client; }
	private RestClient m_client;

}
