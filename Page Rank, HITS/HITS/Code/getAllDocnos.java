// Gets the list of documents present in the merged crawl

package IR.assn4;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

public class getAllDocnos 
{
	public static int count =1;
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, UnknownHostException
	{
		HashSet<String> docs = new HashSet<String>();
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		String[] a = {"docno"};
		SearchResponse response = client.prepareSearch("four")
				.setScroll(new TimeValue(120 * 60000))
	            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	            .setFetchSource(a, null)
				   .execute()
				   .actionGet();

		 while (true) 
		 {			 
			 for (SearchHit hit : response.getHits().getHits()) 
			 {				 
				 String doc = (String) hit.getSource().get("docno");
				 docs.add(doc);
				 System.out.println(count++);
			 }


			 response = client.prepareSearchScroll(response.getScrollId())
					 .setScroll(new TimeValue(120 * 60000))
					 .execute()
					 .actionGet();
			 //Break condition: No hits are returned
			 if (response.getHits().getHits().length == 0) 
			 {
				 break;
			 }

		 }
		 
		 PrintWriter writer = new PrintWriter("MergedUrlList.txt", "UTF-8");
			for(String s: docs)
			{
				writer.println(s);	
			
			}
			writer.close();
			System.out.println("done writing to file");
	}
	
	

}
