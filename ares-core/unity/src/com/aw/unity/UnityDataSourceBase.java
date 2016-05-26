package com.aw.unity;

import java.util.HashMap;

import javax.inject.Provider;

import org.apache.log4j.Logger;

import com.aw.common.rest.security.SecurityAware;
import com.aw.platform.Platform;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.query.Query;
import com.aw.unity.security.DataSecurityResolverBase;
import com.aw.unity.security.ISecColumn;
import com.aw.unity.security.ISecRow;
import com.aw.unity.security.defaults.DataSecurityResolverDefault;
import com.aw.unity.security.defaults.SecColumnDefault;
import com.aw.unity.security.defaults.SecRowDefault;

/**
 * A data source within unity.
 *
 * @author shill, jlehmann
 */
public abstract class UnityDataSourceBase implements UnityDataSource, SecurityAware {
    public static final Logger logger = Logger.getLogger(UnityDataSourceBase.class);

    public static final String SEC_ROW_DEFAULT = "com.aw.unity.security.defaults.SecRowDefault";
    public static final String SEC_COL_DEFAULT = "com.aw.unity.security.defaults.SecColumnDefault";

    //one meta object per tenant
    protected HashMap<String, UnityMetadata> _meta = new HashMap<String, UnityMetadata>();

    //one Data Security role resolver cache per tenant
    protected HashMap<String, DataSecurityResolverBase> _roleSecResolvers = new HashMap<String, DataSecurityResolverBase>();

    /**
     * The unity instance we belong to
     */
    protected UnityInstance instance;

    /**
     * Security resolver
     */
    protected DataSecurityResolverBase _DataSecResolver;

    protected Provider<Platform> platform;

    /**
     * @return The name of this unity data source
     */
    public String getName() { return m_name; }
    public void setName(String name) { m_name = name; }
    protected String m_name;

    public UnityDataSourceBase() {
    }

    @Override
    public void initialize(UnityInstance instance, Provider<Platform> platform) {
    	this.instance = instance;

    	try {

        	//set up security
        	m_rowSecurity = m_rowSecurityClass.newInstance();
        	m_columnSecurity = m_columnSecurityClass.newInstance();
        	m_roleResolver = m_roleResolverClass.newInstance();
        	this.platform = platform;

    	} catch (Exception e) {
    		throw new InvalidDataException("Invalid security class detected", e);
    	}

    }

    @Override
    public Query enforceDataSecurity(Query query) throws Exception {

    	Query ret = query;

    	if (m_rowSecurity != null) {
    		query = m_rowSecurity.enforceRowSecurity(query, m_roleResolver);
    	}

    	return ret;

    }

    @Override
    public Query enforceFieldSecurity(Query query) throws Exception {

    	Query ret = query;

    	if (m_columnSecurity != null) {
    		ret = m_columnSecurity.enforceColumnSecurity(query);
    	}

    	return ret;
    }

    public ISecRow getRowSecurity() { return m_rowSecurity; }
	public void setRowSecurity(ISecRow rowSecurity) { m_rowSecurity = rowSecurity; }
	private ISecRow m_rowSecurity;

	public ISecColumn getColumnSecurity() { return m_columnSecurity; }
	public void setColumnSecurity(ISecColumn columnSecurity) { m_columnSecurity = columnSecurity; }
	private ISecColumn m_columnSecurity;

	public DataSecurityResolverBase getRoleResolver() { return m_roleResolver; }
	public void setRoleResolver(DataSecurityResolverBase roleResolver) { m_roleResolver = roleResolver; }
	private DataSecurityResolverBase m_roleResolver;

	public Class<? extends ISecRow> getRowSecurityClass() { return m_rowSecurityClass; }
	public void setRowSecurityClass(Class<? extends ISecRow> rowSecurityClass) { m_rowSecurityClass = rowSecurityClass; }
	private Class<? extends ISecRow> m_rowSecurityClass = SecRowDefault.class;

	public Class<? extends ISecColumn> getColumnSecurityClass() { return m_columnSecurityClass; }
	public void setColumnSecurityClass(Class<? extends ISecColumn> columnSecurityClass) { m_columnSecurityClass = columnSecurityClass; }
	private Class<? extends ISecColumn> m_columnSecurityClass = SecColumnDefault.class;

	public Class<? extends DataSecurityResolverBase> getRoleResolverClass() { return m_roleResolverClass; }
	public void setRoleResolverClass(Class<? extends DataSecurityResolverBase> roleResolverClass) { m_roleResolverClass = roleResolverClass; }
	private Class<? extends DataSecurityResolverBase> m_roleResolverClass = DataSecurityResolverDefault.class;

	public UnityInstance getUnity() { return instance; }

}
