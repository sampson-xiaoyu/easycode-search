package com.easycode.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import com.easycode.exception.ClientInitException;

@Component("searchClient")
public final class SearchClient {
	
	@Resource(name = "appProps")
	private Properties props;
	
	private TransportClient client = null;
	
	/**
	 * 初始化客户端
	 */
	@PostConstruct
	private void init(){	
		Settings settings = ImmutableSettings.settingsBuilder()
	            .put("cluster.name", props.getProperty("search.cluster.name")).build();
		client = new TransportClient(settings);
		String nodes = props.getProperty("search.cluster.hosts");
		if(StringUtils.isEmpty(nodes)){
			throw new ClientInitException("searchclient init error {} , search.cluster.hosts is null");
		}	
		for(String node : nodes.split(",")){
			
			String[] hostPort = node.split(":");
			
			if(hostPort.length != 2){
				throw new ClientInitException("hostport init error {} , host or port is null ");
			}	
			client.addTransportAddress(
					new InetSocketTransportAddress(hostPort[0], Integer.parseInt(hostPort[1])));	
		}
	}
	
	/**
	* 创建索引
	* @param indices 索引名称
	*/
	public void addIndex(String indices){
		this.client.admin().indices().prepareCreate(indices).execute().actionGet();
	}
	
	
	/**
	* 删除索引
	* @param indices 索引名称
	*/
	public void removeIndex(String indices){
		this.client.admin().indices().prepareDelete(indices).execute().actionGet();
	}
	
	/**
	 * 添加type
	 * @param indices
	 * @param type
	 */
	public void addType(String indices,String type){
		this.client.admin().indices().prepareTypesExists(indices).setTypes(type).execute().actionGet();
	}
	
	/**
	 * 添加对应的mapping
	 * @param index
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public boolean putIndexMapping(String index,String type) throws IOException{
		XContentBuilder mapping = XContentFactory.jsonBuilder()
			       .startObject()
					       .startObject(type)
						       .startObject("properties")  
						           .startObject("description").field("type", "string").field("index", "not_analyzed").endObject()
						           .startObject("name").field("type", "string").field("indexAnalyzer", "ik").field("searchAnalyzer", "ik").endObject()             
						       .endObject()
					       .endObject()
			       .endObject();
		 System.out.println(mapping.string());
		 PutMappingRequest mappingRequest = Requests.putMappingRequest(index).type(type).source(mapping);
		 PutMappingResponse response = this.client.admin().indices().putMapping(mappingRequest).actionGet();
		 return response.isAcknowledged();
	}
	
	
	public SearchRequestBuilder initSearchRequest(String index , String type , int from ,int to) {
		
        SearchRequestBuilder srb = this.client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(from)
                .setSize(to)
                .setExplain(false);       
        return srb;

    }
	
	
	/**
	 * 根据不同的索引、type对搜索引擎内容进行添加
	 * @param index
	 * @param type
	 * @param searchData
	 * @return
	 */
	public List<Long> addSearchRecord(String index,String type,Map<Long, byte[]> searchData){
		List<Long> successIds = new ArrayList<>(0);
		BulkRequestBuilder bulkRequest = this.client.prepareBulk();
		for(Long id : searchData.keySet()) {
            byte[] json = searchData.get(id);
            bulkRequest.add(this.client.prepareIndex(index, type, id.toString()).setSource(json));
            successIds.add(id);
        }
		int numberOfActions = bulkRequest.numberOfActions();
        if (numberOfActions > 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if(bulkResponse.hasFailures()) {
                return new ArrayList<>(0);
            }
        }
        return successIds;
	}
}
