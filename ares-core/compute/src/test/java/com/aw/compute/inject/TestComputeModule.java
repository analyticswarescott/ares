package com.aw.compute.inject;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.aw.action.ActionManager;
import com.aw.action.DefaultActionFactory;
import com.aw.action.DefaultActionManager;
import com.aw.action.DefaultRootActionFactory;
import com.aw.alarm.action.AlarmAction;
import com.aw.alarm.action.AlarmActionType;
import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.compute.TestUnityInstance;
import com.aw.compute.referencedata.OperationTypeLookupData;
import com.aw.compute.streams.processor.XMLBundleProcessorTest.TestOTLookupData;
import com.aw.document.DocumentHandler;
import com.aw.incident.action.IncidentAction;
import com.aw.incident.action.IncidentActionType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.unity.UnityInstance;

/**
 * Common test dependencies for unit tests
 *
 *
 *
 */
public class TestComputeModule extends ComputeModule {

	private UnityInstance unity = new TestUnityInstance();
	private PlatformMgr platformMgr = mock(PlatformMgr.class);
	private Platform platform = new TestPlatform();
	private DocumentHandler docs;
	private RestCluster mockCluster = mock(RestCluster.class);

	public TestComputeModule() {
	}

	@Override
	protected void configure() {

		try {
			docs = new com.aw.compute.TestDocumentHandler();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		bind(PlatformMgr.class).toInstance(platformMgr);
		bind(Platform.class).toInstance(platform);
		bind(DocumentHandler.class).toInstance(docs);
		bind(RestCluster.class).toInstance(mockCluster);
		bind(ActionManager.class).toInstance(new DefaultActionManager(
				new TestProvider<>(platform),
				new TestProvider<>(mockCluster),
				new TestProvider<>(platformMgr),
				new TestProvider<>(unity),
				new DefaultRootActionFactory(
						new DefaultActionFactory(IncidentAction.class, IncidentActionType.values()),
						new DefaultActionFactory(AlarmAction.class, AlarmActionType.values()))
					)
				);

		doReturn(platform).when(platformMgr).getPlatform();

		//use test operation type lookup
		bind(OperationTypeLookupData.class).to(TestOTLookupData.class);

		//use test unity
		bind(UnityInstance.class).toInstance(unity);

	}

	public PlatformMgr getMockPlatformMgr() { return platformMgr; }

}
