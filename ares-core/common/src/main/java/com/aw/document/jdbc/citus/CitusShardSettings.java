package com.aw.document.jdbc.citus;

import java.util.ArrayList;

/**
 * collection of sharded table definitions for Citus
 */
public class CitusShardSettings {


	public ArrayList<ShardedTable> getShardedTables() {
		return sharded_tables;
	}

	public void setShardedTables(ArrayList<ShardedTable> citus_shard_settings) {
		this.sharded_tables = citus_shard_settings;
	}

	private ArrayList<ShardedTable> sharded_tables;

	/**
     * Definition of a distributed table
     */
    public static class ShardedTable {


        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getShardColumn() {
            return shardColumn;
        }

        public void setShardColumn(String shardColumn) {
            this.shardColumn = shardColumn;
        }

        public int getShardsPerWorker() {
            return shardsPerWorker;
        }

        public void setShardsPerWorker(int shardsPerWorker) {
            this.shardsPerWorker = shardsPerWorker;
        }

        public String getShardMethod() {
            return shardMethod;
        }

        public void setShardMethod(String shardMethod) {
            this.shardMethod = shardMethod;
        }

        private String tableName;
        private String shardColumn;
        private int shardsPerWorker;
        private String shardMethod;
    }
}
