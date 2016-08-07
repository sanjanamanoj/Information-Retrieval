// This class is used to obtain the document lengths for each document in the ES index.

package IR.assn4;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;





public class Doc_length

{
 public static void main(String[] args) throws IOException
 {

		Settings settings = Settings.builder().put().build();
		Client client;
		 client = TransportClient.builder().settings(settings).build()
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		 HashMap<String,Double> len = new HashMap<String,Double>();
		 HashSet<String> docs = new HashSet<String>();

		 //Gets the list of URL's present in the merged crawl
		 BufferedReader br1 = new BufferedReader(new FileReader("MergedUrlList.txt"));
			String data = br1.readLine();
			while(data!=null)
			{
				docs.add(data.trim());
				data=br1.readLine();
			}
			br1.close();
			System.out.println("Total number of merged documents:"+docs.size());

			for(String s: docs)
			{
				 MetricsAggregationBuilder agg = AggregationBuilders.stats("sta").script(new Script("doc['text'].values.size()"));
			 	String[] a = {"text","out_links","in_links"};
				SearchResponse response = client.prepareSearch("four").setTypes("document")
						.setQuery(QueryBuilders.termQuery("_id", s))
			 	    //.setSource(builder)
			 	    .addAggregation(agg)
			 	    .setFetchSource(null,a)
			 	    .execute()
			 	    .actionGet();
				//System.out.println(response);
				for (Aggregation hit : response.getAggregations())
				{

					double length = Double.parseDouble(hit.getProperty("sum").toString());
					len.put(s,length);
				}

			}
			System.out.println("number of documents in length map:"+ len.size());

		PrintWriter writer = new PrintWriter("DocLength.txt", "UTF-8");
		for(Entry<String,Double> e : len.entrySet())
		{

			writer.println(e.getKey()+"\t"+e.getValue());
		}
		writer.close();
		System.out.println("done writing to file");

 }


}
