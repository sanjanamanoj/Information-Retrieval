//This class is used to get the inlink map for the crawled documents and write it to the file in the same format as the wt2g file.
package IR.assn4;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;



public class crawlPR
{
	public static int count = 1;
	public static void main(String[] args) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException
	{
		HashMap<String,HashSet<String>> inlink = new HashMap<String,HashSet<String>>();
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		String[] a = {"docno","in_links"};
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
				 String in = (String) hit.getSource().get("in_links");
				 String[] temp = in.split("\t");
				 HashSet<String> i = new HashSet<String>();
				 for(int j=0;j<temp.length;j++)
				 {
					 i.add(temp[j]);
				 }
				 inlink.put(doc, i);
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

		 PrintWriter writer = new PrintWriter("inlinksForPR.txt", "UTF-8");
			for(Entry<String,HashSet<String>> e : inlink.entrySet())
			{
				writer.print(e.getKey());
				HashSet<String> i =new HashSet<String>();
				i.addAll(e.getValue());
				for(String s:i)
				{
					writer.print("\t"+s.trim());
				}
				writer.println();
			}
			writer.close();
			System.out.println("done writing to file");
	}


}
