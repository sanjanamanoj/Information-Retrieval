package IR.assn5;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;


// The individual manual assessment of 150 documents for each query was done using Calaca as a web interface to ElasticSearch. This program is used to get the manual assessment from ElasticSearch to a file.
public class GetQrel
{
	public static void main(String[] args) throws IOException
	{
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		SearchResponse response = client.prepareSearch("trec")
				.setScroll(new TimeValue(120 * 60000))
	            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)

				   .execute()
				   .actionGet();

		 while (true)
		 {
			 FileWriter fw = new FileWriter("SanjanaQrel.txt",true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
			 for (SearchHit hit : response.getHits().getHits())
			 {
				 String queryid =(String) hit.getSource().get("queryid");
				 String doc = (String) hit.getSource().get("docid");
				 String name=(String)hit.getSource().get("assessor");
				 String grade = (String) hit.getSource().get("grade");
				 pw.println(queryid+" "+name+" "+doc+" "+grade);
			 }
			pw.close();

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
	}

}
