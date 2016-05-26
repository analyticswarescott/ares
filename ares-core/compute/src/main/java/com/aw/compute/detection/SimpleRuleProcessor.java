package com.aw.compute.detection;

import org.apache.log4j.Logger;

import com.aw.common.rest.security.TenantAware;
import com.aw.common.spark.StreamDef;
import com.aw.compute.inject.Dependent;
import com.aw.compute.referencedata.ReferenceDataList;
import com.aw.compute.referencedata.ReferenceDataManager.ReferenceDataType;
import com.aw.compute.streams.exceptions.DataProcessingException;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.AbstractDataProcessor;
import com.aw.compute.streams.processor.framework.IterableTupleProcessor;
import com.aw.unity.Data;

/**
 * Processes simple one event rules to look for matches. When a match occurs, actions are then taken.
 *
 *
 */
public class SimpleRuleProcessor extends AbstractDataProcessor implements IterableTupleProcessor, TenantAware, Dependent {

	private static final long serialVersionUID = 1L;

	public static final Logger logger = Logger.getLogger(SimpleRuleProcessor.class);

	@Override
	public void process(String string, Iterable<String> messages) throws StreamProcessingException {

		for (String message : messages) {

			process(message);

		}

	}

	@Override
	public void process(Data data) throws StreamProcessingException {

	    //process rules per tenant
        try {

            for (SimpleRule rule : getRules()) {

            	//process this data with this rule
                rule.process(data);

            }

        }

        //on any exception, bubble it up
        catch(Exception ex) {
            throw new DataProcessingException("while processing data for tenant " + getTenantID(), ex);
        }

	}

	/**
	 * @return The simple rules to run against the current piece of data
	 */
	protected synchronized ReferenceDataList<SimpleRule> getRules() throws StreamProcessingException {

		if (m_rules == null) {

			try {

				//have to do this on-demand because of spark serialization
				m_rules = getDependency(ReferenceDataType.SIMPLE_RULES.getType());

			} catch (Exception e) {
				throw new StreamProcessingException("error getting reference data", e);
			}

		}

		return m_rules;

	}
	private ReferenceDataList<SimpleRule> m_rules;

    @Override
    public void init(StreamDef streamDef) throws ProcessorInitializationException {

    }

}
