package com.aw.unity.jdbc;

import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.*;
import com.aw.platform.Platform;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * A general use elasticsearch client. Uses the platform to determine how to connect. This client is NOT thread safe.
 * Each thread talking to elasticsearch should have its own client. In general this is good practice anyway, as each
 * client will randomly order the elasticsearch nodes to determine which node to connect to for the next operation.
 *
 *
 */
public class UnityJDBCClient implements JSONHandler, SecurityAware {

	private static final Logger logger = Logger.getLogger(UnityJDBCClient.class);


	private DBMgr dbMgr;
	private Platform platform;


	private Connection conn;

	public UnityJDBCClient(DBMgr dbMgr) throws Exception {

		this.dbMgr = dbMgr;


	}


	private HashMap<Field, Integer> ordinals = new HashMap<>();
	private PreparedStatement getInsertForDataType(Connection conn, DataType dataType) throws Exception{

		String sql = "INSERT INTO " + dataType.getName();

		sql = sql + " (";

		int j = 0;
		for (Field f : dataType.getFields()) {
			if (j == 0) {
				sql = sql + f.getName();
			}
			else {
				sql = sql + "," + f.getName();
			}
			ordinals.put(f, j);
			j++;
		}
		sql = sql + ") values (";
		for (int i=0; i< dataType.getFields().length; i++) {
			if (i == 0) {
				sql = sql + "?";
			}
			else {sql = sql + ",?";}
		}
		sql = sql + ")";

		sql = sql + " ON CONFLICT (" + dataType.getIDField().getName() +  ") DO NOTHING ";

		logger.warn(" GENERATED SQL is " + sql);

		return conn.prepareStatement(sql);

	}

	private void processRow(PreparedStatement ps, Data data) throws SQLException {
		for (Field f: data.getType().getFields()) {
			int ordinal = ordinals.get(f);


			String val = data.getValue(f).toString();

			//System.out.println("Field " + f.getName() + " Type is: " + f.getType());

			//add field by data type
			if (f.getType() == FieldType.STRING) {
				ps.setString(ordinal + 1, val);
			}
			else if (f.getType() == FieldType.INT) {
				ps.setInt(ordinal + 1, Integer.parseInt(val));
			}
			else if (f.getType() == FieldType.LONG) {
				ps.setLong(ordinal + 1, Long.parseLong(val));
			}
			else if (f.getType() == FieldType.DOUBLE) {
				ps.setDouble(ordinal + 1, Double.parseDouble(val));
			}
			else if (f.getType() == FieldType.TIMESTAMP) {
				ps.setTimestamp(ordinal + 1, Timestamp.from(Instant.parse(val)));
			}
			else {
				throw new RuntimeException(" unsupported data type :" + f.getType().toString());
			}
		}


	}

	public void bulkInsert(Tenant tenant, String schema, DataType dataType, Iterable<Data> data) throws Exception {

		//simple type based insert generation

		//Connection conn = null;

			conn = dbMgr.getConnection(tenant);
			PreparedStatement ps = getInsertForDataType(conn, dataType);

			for (Data d : data) {
				processRow(ps, d);

				logger.error(" DEBUG: About to execute SQL: " + ps.toString());
				int i = ps.executeUpdate();

				logger.error(" DEBUG: SQL result was " + i);
			}



	}

	/**
	 * TODO: partitioning support
	 *
	 * @param data the data for which index time must be determined
	 * @return the index time for the data
	 */
	Instant getTimePartitionValue(Data data) {
		return time.now(); //current time, i.e. insertion time
	}

	//if errors occur, we'll save the first reason for an exception message
	private String m_error = null;
	private long m_error_ordinal = 0;

	public long getMaxInsertSize() { return this.maxInsertSize; }
	public void setMaxInsertSize(long maxInsertSize) { this.maxInsertSize = maxInsertSize; }
	private long maxInsertSize = 1000; //1000 row

	public void setTimeSource(TimeSource time) { this.time = time; }
	private TimeSource time = TimeSource.SYSTEM_TIME;

	@Override
	public void handleJson(String json, long ordinal, Object context) throws Exception {

		//if we haven't found an error yet, check the next result
		if (m_error == null) {

			JSONObject item = new JSONObject(json);

			//check for error
			if (item.has("index")) {
				JSONObject index = item.getJSONObject("index");
				if (!HttpStatusUtils.isSuccessful(index.getInt("status"))) {
					JSONObject error = index.getJSONObject("error");
					m_error = error.getString("reason");
					m_error_ordinal = ordinal;

					//check for cause
					if (error.has("caused_by")) {
						JSONObject cause = error.getJSONObject("caused_by");
						m_error = m_error + " (" + cause.getString("type") + ": " + cause.getString("reason") + ")";
					}
				}
			}

		}

	}



}
