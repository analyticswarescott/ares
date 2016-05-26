package com.aw.user;

import com.aw.common.auth.User;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.platform.Platform;
import com.aw.security.Role;
import com.aw.tenant.TenantMgr;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jhaight
 */
public class DgmcDbUserManager implements UserManager {

	private Provider<TenantMgr> tenantMgrProvider;
	private DBMgr dbManager;

	private static final String GET_ROLES_QUERY = "SELECT body FROM document JOIN application_role ON application_role.";

	@Inject
	public DgmcDbUserManager(Provider<Platform> platformProvider, Provider<TenantMgr> tenantMgrProvider, DocumentJDBCProvider jdbcProvider) {
		this.tenantMgrProvider = tenantMgrProvider;
		dbManager = new DBMgr(platformProvider, jdbcProvider);
	}

	@Override
	public Collection<Role> getRoles(String userId) throws Exception {
		List<Role> roles = new ArrayList<>();
		try (final Connection connection = dbManager.getConnection(getTenant())) {
			try (final PreparedStatement statement = connection.prepareStatement(GET_ROLES_QUERY)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {

					}
				}
			}
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Tenant> getTenants(String userId) throws Exception {
		if (!Tenant.SYSTEM_TENANT_ID.equals(getTenantID())) {
			throw new UnsupportedOperationException();
		}
		Collection<Document> tenantDocuments = tenantMgrProvider.get().getAllTenants();
		List<Tenant> tenants = new ArrayList<>();
		for (Document tenantDocument : tenantDocuments) {
			tenants.add(tenantDocument.getBodyAsObject());
		}
		return tenants;
	}

	@Override
	public Collection<User> getUsers() {
		throw new UnsupportedOperationException();
	}

	private Tenant getTenant() throws Exception {
		return new Tenant(getTenantID());
	}
}
