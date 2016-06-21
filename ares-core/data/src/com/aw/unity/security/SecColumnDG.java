package com.aw.unity.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.system.EnvironmentSettings;
import com.aw.unity.Field;
import com.aw.unity.UnityInstance;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;

public class SecColumnDG implements ISecColumn {

	public static final Logger logger = Logger.getLogger(SecColumnDG.class);

	@Override
	public Query enforceColumnSecurity(Query query)
			throws Exception {

		if (EnvironmentSettings.isAuthDisabled()) {
			return query;
		}

		//get unity for this tenant
		UnityInstance instance = query.getUnity();

		List<QueryAttribute> attributes = new ArrayList<>(query.getAttributes(true));
		AuthenticatedUser user = getSecurityContext().getUser();

		//the obfuscationRules array will contain a reference to the column that should be masked
		//or substituted by another anonimized column value
		if(user!=null && !user.getObfuscationRules().isEmpty()){
			for(String ruleName: user.getObfuscationRules()){
				ObfuscationRule rule = ObfuscationRule.valueOf(ruleName);
				String origCol = rule.getOriginalColumn();
				String repCol = rule.getReplacementColumn();
				String mask = rule.getMask();

				for (int x=0; x<attributes.size(); x++) {
					QueryAttribute attrib = attributes.get(x);

					String name = attrib.getField().getName();
					if(!StringUtils.isEmpty(name) && name.equalsIgnoreCase(origCol)) {

						//check whether to use mask or replaceColumn
						if(repCol!=null){
							//replace the attribute
							Field field = query.getUnity().getMetadata().getFieldRepository().getField(repCol);
							attributes.set(x, attrib.changeField(field));
						}

						//might need to change, but for now if there is no repCol, there should be a mask val
						else {
							attributes.set(x, attrib.constant(mask));
						}
					}
					logger.debug(x + ": orig:  " + name);
					logger.debug(x + ": replace:  " + attrib.getField());
					logger.debug(x + ": mask:  " + attrib.getConstant());
				}
			}
		}
		return query;
	}



}
