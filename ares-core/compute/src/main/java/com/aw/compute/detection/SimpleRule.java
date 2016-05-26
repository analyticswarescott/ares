package com.aw.compute.detection;

import static com.aw.common.util.JSONUtils.updateFromString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.action.Action;
import com.aw.action.ActionContext;
import com.aw.action.ActionManager;
import com.aw.action.DefaultActionContext;
import com.aw.action.Util;
import com.aw.alarm.action.AlarmAction;
import com.aw.common.Tag;
import com.aw.compute.alarm.SimpleAlarm;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.DataProcessor;
import com.aw.document.DocumentHandler;
import com.aw.document.body.IBodyInitializable;
import com.aw.incident.action.CreateIncidentAction;
import com.aw.incident.action.ErrorAssociationAction;
import com.aw.incident.action.EventAssociationAction;
import com.aw.platform.PlatformError;
import com.aw.platform.PlatformMgr;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.unity.query.Filter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Run filters against incoming events to search for matches.
 *
 * This class registers a "rule" variable available to all actions that are taken as a result of
 * it firing.
 *
 * Created by scott on 09-OCT-2015
 * Modified by jlehmann
 */
public class SimpleRule extends AbstractDetectionRule implements IBodyInitializable, DataProcessor, Dependent {

	public static final String TYPE = "simple_rule";

	//a variable holding a rule TODO: move to a more generic location
	public static final String VAR_RULE = "rule";

	//complex properties for which we need custom deserialization
	private static final String FILTER = "filter";
	private static final String EXCLUSIONS = "exclusions";
	private static final String ACTIONS = "actions";

    public SimpleRule() throws Exception {
    }

    @Override
    public String getType() {
    	return TYPE;
    }

    @Override
    public void initialize(Object data, DocumentHandler docs) throws Exception {

    	ActionManager actionManager = getDependency(ActionManager.class);

    	//initialize from raw data
    	String strJson = data.toString();
    	JSONObject object = new JSONObject(strJson);

    	//update basic properties
    	updateFromString(strJson, this);

    	m_filter = getDependency(UnityInstance.class).newFilter(object.get(FILTER));
    	m_exclusions = toFilters(object.getJSONArray(EXCLUSIONS));

    	//build the action array
    	JSONArray array = object.getJSONArray(ACTIONS);
    	m_actions = new ArrayList<Action>(array.length());
    	for (int x=0; x<array.length(); x++) {
    		m_actions.add(actionManager.newAction(array.getJSONObject(x)));
    	}

    }

    @Override
    public void process(Data data) throws StreamProcessingException {

    	try {

    		//if we match
        	if (isMatch(data)) {

        		executeActions(data);

        	}

    	} catch (StreamProcessingException e) {
    		throw e;
    	} catch (Exception e) {
    		throw new StreamProcessingException("error while processing simple rule", e);
    	}

    }

    protected void executeActions(Data data) throws Exception {

    	//get the action manager
    	ActionManager actionManager = getDependency(ActionManager.class);

    	//register us as an available variable in an action context
    	ActionContext ctx = new DefaultActionContext(data.getUnity(), getProviderDependency(PlatformMgr.class));

    	//register some variables

    	//the rule that generated the alarm
    	ctx.registerVariable(VAR_RULE, this);

    	//the event that caused the alarm
    	ctx.registerVariable(EventAssociationAction.VAR_EVENT, data);

    	//the alarm itself
		ctx.registerVariable(AlarmAction.VAR_ALARM, new SimpleAlarm(data, this));

    	//copy the actions, these are new actions being taken based on the template actions
    	List<Action> actions = Util.copyAll(getActions());

    	//take the actions we're configured to take using our context object - use copies of the original action configurations
		actionManager.takeActions(actions, ctx);

		//if an incident was created, associate the incident with the event that matched
		if (ctx.getVariableObject(CreateIncidentAction.VAR_INCIDENT) != null) {

			//create an error association if applicable
			if (data.getType().getName().equals(PlatformError.UNITY_TYPE)) {
				actionManager.takeAction(new ErrorAssociationAction(), ctx);
			}

			//else assume event for now
			else {
				actionManager.takeAction(new EventAssociationAction(), ctx);
			}

		}

    }

    private List<Filter> toFilters(JSONArray filterJson) throws Exception {

    	List<Filter> ret = new ArrayList<Filter>();

    	//get our unity instance
    	UnityInstance instance = getDependency(UnityInstance.class);

    	for (int x=0; x<filterJson.length(); x++) {
    		Filter filter = instance.newFilter(filterJson.get(x));
    		ret.add(filter);
    	}

    	return ret;

    }

    /**
     * Returns whether it's a match
     *
     * @param data
     * @return
     */
    public boolean isMatch(Data data) {

    	//TODO: never match data that was generated by this rule - i.e. avoid circular firings
    	return m_filter.match(data) && !isExclusion(data);

    }


    /**
     * If this data is excluded from matches, return true
     *
     * @param data The data
     * @return Whether the data is excluded from matching
     */
    public  boolean isExclusion(Data data) {
        for (Filter filter : m_exclusions ) {

            if (filter.match(data)) {
                return true;
            }

        }
        return false;
    }

    @JsonIgnore
	public Filter getFilter() { return m_filter; }
    @JsonIgnore
	public void setFilter(Filter filter) { m_filter = filter; }
	private Filter m_filter;

	@JsonIgnore
	public List<Filter> getExclusions() { return m_exclusions; }
    @JsonIgnore
	public void setExclusions(List<Filter> exclusions) { m_exclusions = exclusions; }
	private List<Filter> m_exclusions;

    /**
     * The actions to take on a match
     */
	@JsonIgnore
	public List<Action> getActions() { return m_actions; }
	private List<Action> m_actions;

	/**
	 * @return the severity of this simple rule
	 */
	public int getSeverity() { return this.severity;  }
	public void setSeverity(int severity) { this.severity = severity; }
	private int severity;

	/**
	 * tags for the rule - if an alarm is created, these will be added to the alarm
	 */
	@JsonProperty("tags")
	public Set<Tag> getTags() { return super.getTags(); }
	@JsonProperty("tags")
	protected void setTags(Set<Tag> tags) { super.setTags(tags); }

}
