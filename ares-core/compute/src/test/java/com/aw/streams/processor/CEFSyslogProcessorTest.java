package com.aw.streams.processor;
/*
package com.aw.stream.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aw.compute.streams.processor.impl.CEFSyslogProcessor;
import com.aw.compute.streams.processor.impl.SyslogProcessor;

public class CEFSyslogProcessorTest extends SyslogProcessorTest {

	public CEFSyslogProcessorTest(){

	}

	@Override
	protected SyslogProcessor getSyslogProcessor() {
		return new CEFSyslogProcessor();
	}

	@Override
	protected void validateSyslogData(String msg, String hostname) {
		//make sure the syslog message matched
		Pattern pattern = Pattern.compile("\\<134\\>(.*?) " + hostname + "CEF:0\\|Digital\\sGuardian_AndSomeMoreStuffSoThisShouldFail");
		Matcher matcher = pattern.matcher(msg);
//		assertTrue("syslog format didn't match expected", matcher.find());

	}

	protected int getPort() { return 1515; }


}
*/
