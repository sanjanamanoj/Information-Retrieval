//This class is used to obtain a root set of 1000 documents from which we can expand into the base set. The root set can be obtained using ES serach or one of the retrieval models. I use BM25 to retrieve 1000 relevant documents.
package IR.assn4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;


public class RootSet
{
	public static void getRoot() throws IOException
	{

		HashSet<String> root = new HashSet<String>();
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

    // Reading the lengths of documents from the file.
		HashMap<String,Double> len = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader("DocLength.txt"));
		String data = br.readLine();
		while(data!=null)
		{
			String[] temp = data.split("\t");
			String docno = temp[0].trim();
			Double length = Double.parseDouble(temp[1].trim());
			len.put(docno, length);
			data=br.readLine();
		}
		br.close();
		System.out.println("Got doc lengths");

		HashMap<String,ArrayList<Tf_index>> final_hmap = new HashMap<String,ArrayList<Tf_index>>();
		//The query used for the BM25
		String query = "terrorism bomb massacre shoot blast";
		String[] queryTerms = query.split(" ");
		for(String q : queryTerms)

		{
			int count = 0;
			String[] a = {"text","out_links","in_links"};
			Map<String, Object> params;
		 params = new HashMap<String, Object>();
	        params.put("term", q);
	        params.put("field", "text");
		SearchResponse scrollResp = client.prepareSearch("four")
                .setTypes("document")
                .setScroll(new TimeValue(120 * 60000))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource( null,a)
                .setQuery(QueryBuilders.functionScoreQuery
                          (QueryBuilders.termQuery("text", q),
                            new ScriptScoreFunctionBuilder(new Script("getTF",
                                    ScriptType.INDEXED, "groovy", params)))
                          .boostMode("replace"))
                .setExplain(true)
                .setFrom(0)
                .setSize(10)
                .execute()
                .actionGet();


		ArrayList<Tf_index> r = new ArrayList<Tf_index>();
		while (true)
		 {
			 for (SearchHit hit : scrollResp.getHits().getHits())
			 {
				 String docno = (String) hit.getSource().get("docno");
				 String id = (String) hit.getSource().get("id");
				 int tf = (int) hit.getScore();
				 count += tf;
				 Tf_index k = new Tf_index(docno,tf);
				 r.add(k);
			 }


			 scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					 .setScroll(new TimeValue(120 * 60000))
					 .execute()
					 .actionGet();
			 //Break condition: No hits are returned
			 if (scrollResp.getHits().getHits().length == 0)
			 {
				 break;
			 }

		 }

		final_hmap.put(q,r);
		}
		System.out.println("Calling BM25");
		OkapiBM25.matchingScore(len, final_hmap);

	}
}
