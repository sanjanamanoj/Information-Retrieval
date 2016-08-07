package IR.assn1;


import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;





public class Doc_Length 

{
 public static void len() throws IOException
 {
		
		
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		
		HashMap<String,Double> len = new HashMap<String , Double>();
		ArrayList<String> docnos = new ArrayList<String>();
		BufferedReader br1 = new BufferedReader(new FileReader("doc_length.txt"));
		String data = br1.readLine();
		System.out.println(data);
		String docs = null;
		while(data!=null)
		{
			if(data.contains("docno"))
			{
				String[] r1 = data.split("\t");
				String[] r2 = r1[0].split("docno:");
				String doc = r2[1];
				docnos.add(doc);
				
			}
			data=br1.readLine();
		}
		br1.close();
		System.out.println(docnos.size());
		
		
		for(String docno: docnos)
		{
			
		//System.out.println(doc);
			XContentBuilder builder = XContentFactory.jsonBuilder();
	        builder.startObject()
		    .startObject("query")
		    	.startObject("match")
		    	.field("docno",docno)
		    	.endObject()
		    .endObject()
		    .startObject("aggs")
		    	.startObject("count")
		    		.startObject("stats")
		    			.field("script", "doc['text'].values.size()")
		    		.endObject()
		    	.endObject()
		    .endObject()
		    .endObject();
	        
	        SearchResponse response = client.prepareSearch().setTypes("document")
	        	    .setSource(builder)
	        	    .execute()
	        	    .actionGet();
	        //System.out.println(response.toString());
	        for (Aggregation hit : response.getAggregations()) 
		    {
	        	
	        	double length = Double.parseDouble(hit.getProperty("sum").toString());
	        	len.put(docno, length);
		    }
	        	
//			QueryBuilder qb = matchQuery("id",i);
//			SearchResponse response = client.prepareSearch()				
//					.setQuery(qb)					
//					.setSize(1000)
//					.execute()
//					.actionGet();
//			//System.out.println(response.toString());
//			
//			 for (SearchHit hit : response.getHits().getHits())
//			 {
//				 	System.out.println(counter++);
//	            	String text = (String) hit.getSource().get("text");
//	            	int length = getLength(text,stopWords);
//	            	total_len += length;
//	                String docno = (String) hit.getSource().get("docno");
//	                int docid =Integer.parseInt(hit.getId());
//	                docLengths.put(docno, length);
//	                
//			 }
			// System.out.println(total_len);			
			
		}
		
		PrintWriter writer = new PrintWriter("doc_Newlength.txt", "UTF-8");
		for(Entry<String,Double> e : len.entrySet())
		{
			//System.out.println(e.getKey());
			//writer.println("avg_len:"+total_len);
			writer.println("docno:"+e.getKey()+"\tlength:"+e.getValue());			
		}
		writer.close();
		System.out.println("done writing to file");
//	
 }
	
	
}
