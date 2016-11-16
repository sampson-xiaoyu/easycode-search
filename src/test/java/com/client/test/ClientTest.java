package com.client.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.easycode.client.SearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration(locations={"classpath:/spring/*.xml"}) 
public class ClientTest {
	
	@Resource(name = "searchClient")
	private SearchClient searchClient;
	
	@Test
	public void testUpdateDate(){
		try{
			Map<Long,byte[]> searchData = new HashMap<Long,byte[]>();
			for(Long i=11L ; i< 20;i++){
				Map<String,Object> data = new HashMap<String,Object>();
				if(i % 2 == 0){
					data.put("description", "中国天安门");
					data.put("name", "安门");
				}else{
					data.put("description", "中国天");
					data.put("name", "安门国中");
				}
				ObjectMapper objectMapper = new ObjectMapper();
				byte[] json = objectMapper.writeValueAsBytes(data); 
				searchData.put(i, json);
			}
			System.out.println(searchClient.addSearchRecord("productindex", "product", searchData));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void testAddIndex(){
			searchClient.addIndex("productindex");
	}
	@Test
	public void testQuery(){
		SearchRequestBuilder srb = searchClient.initSearchRequest("productindex", "product", 0, 100);
		srb.addSort("name",SortOrder.ASC);
		srb.setQuery(QueryBuilders.matchQuery("name","国天"));
		System.out.println(srb.toString());
		SearchResponse response = srb.execute().actionGet();
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
        	System.out.println(hit.getSourceAsString());
        }
	}
	@Test
	public void testRemoveIndex(){
			searchClient.removeIndex("productindex");
	}
	@Test
	public void testAddType(){
			searchClient.addType("productindex", "product");
	}
	@Test
	public void testAddMapping(){
		try {
			searchClient.putIndexMapping("productindex", "product");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
