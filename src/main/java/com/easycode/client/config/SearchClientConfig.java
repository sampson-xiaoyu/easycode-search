package com.easycode.client.config;

import java.util.Properties;

public class SearchClientConfig extends Config{
	
	private String clusterName;
	
	private String serverNodes;
	
	@Override
	public Properties build() {
		// TODO Auto-generated method stub
		Properties pro = new Properties();
		pro.setProperty("search.cluster.name", clusterName);
		pro.setProperty("search.cluster.nodes", serverNodes);
		return pro;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getServerNodes() {
		return serverNodes;
	}

	public void setServerNodes(String serverNodes) {
		this.serverNodes = serverNodes;
	}

}
