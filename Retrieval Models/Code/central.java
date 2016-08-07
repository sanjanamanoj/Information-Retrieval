package IR.assn1;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.json.simple.parser.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;

// This class parses through all the queries and sends a hashmap of the query terms containing an array list of the 
// documents it is present in along with the frequencies(<term <docid tf>>)
public class central 
{

	public static void main(String[] args) throws IOException, ParseException
	{
		//Doc_Length.len();
		HashMap<String,Long> sumTf;
		ArrayList<String> docnos = new ArrayList<String>();
		
		// This file has a list of all the documents with their respective lengths. "len" is  Hashmap of the docid 
		// and the length of the document(the while loop does this)
		HashMap<String,Integer> len = new HashMap<String , Integer>();
		BufferedReader br1 = new BufferedReader(new FileReader("doc_Newlength.txt"));
		String data = br1.readLine();
		while(data!=null)
		{
			if(data.contains("docno"))
			{
				String[] r1 = data.split("\t");
				String[] r2 = r1[0].split("docno:");
				String doc = r2[1];
				docnos.add(doc);
				String[] r3 = r1[1].split("length:");
				int length = (int) Double.parseDouble(r3[1]);
				len.put(doc, length);
				
			}
			data=br1.readLine();
		}
		br1.close();
		
		
		// stopwords is an ArrayList of all the stopwords. We get this from the stop function in the stopwords class
		ArrayList<String> stopwords = new ArrayList<String>();
		stopwords = stopWords.stop();
		
		// Setting up the client to communicate with elastic search
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		
		// Receives the vocabulary size from elastic search
		long vocabSize;
		MetricsAggregationBuilder aggregation = AggregationBuilders
                .cardinality("agg")
                
                .field("stats");
        SearchResponse sr = client.prepareSearch("ap_dataset2")
        		.setTypes("document")
        		.addAggregation(aggregation)
        		.execute()
        		.actionGet();
        Cardinality agg = sr.getAggregations().get("agg");
        long value = agg.getValue();
        vocabSize = value;
        System.out.println(vocabSize);
        
        
        // Reads the queries from the file.
		BufferedReader br = new BufferedReader(new FileReader("query_desc.51-100.short.txt"));
		String line = br.readLine();
		
		// loop for each query
		// final_hmap is the hashmap containing all the terms with docid and frequency. We pass final_hmap
		// to the retrieval models for each query and not all together
		while(line!="")
		{
			String ty;
			
			// sumTf is a Hashmap of all the terms and the total number of occurences in all documents
			// i.e TF of that terms for all documents
			sumTf = new HashMap<String,Long>();
			HashMap<String,ArrayList<Tf_index>> final_hmap= new HashMap<String,ArrayList<Tf_index>>();
			line =line.replace(",", "").replace(".","").replace("(", "").replace(")", "");
			ArrayList<String> t = new ArrayList<String>();
			
			// q_no is the query number
			int q_no = 0;
			String[] term = line.split(" ");
			if (term[0]!="")
			 q_no = Integer.parseInt(term[0]);
			System.out.println(q_no);
			
			// adds all the query terms to the arrayList t, then removes blanks and stopwords
			for(int i =3;i<term.length;i++)
			{
				t.add(term[i]);
			}
			t.remove(" ");
			t.removeAll(stopwords);
			
			// loop for each query term
			for(String terms : t)
			
			{
				
				int count = 0;
				//handles words with '-' in them
				if (terms.contains("-")) 
				{
					String[] spl = terms.split("-");
					PorterStemmer ps = new PorterStemmer();
					ps.setCurrent(spl[0].toLowerCase());
					ps.stem();
					ps.setCurrent(spl[1].toLowerCase());
					ps.stem();
					 ty = ps.getCurrent();
				} 
				// stems the words, changes them to lower case and sends it to search response
				else 
				
				{
					PorterStemmer ps = new PorterStemmer();
					ps.setCurrent(terms.toLowerCase());
					ps.stem();
					 ty = ps.getCurrent();
				}
				System.out.println(ty);
				
				// Tf_index is a user defined class that takes docid, TF
				ArrayList<Tf_index> r = new ArrayList<Tf_index>();
				Map<String, Object> params; 
			
				r = new ArrayList<Tf_index>();
			
				 params = new HashMap<String, Object>();
			        params.put("term", ty);
			        params.put("field", "text");
			        
			 SearchResponse scrollResp = client.prepareSearch("ap_dataset2")
			                .setTypes("document")
			                .setScroll(new TimeValue(120 * 60000))
			                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			                .setQuery(QueryBuilders.functionScoreQuery
			                          (QueryBuilders.termQuery("text", ty), 
			                            new ScriptScoreFunctionBuilder(new Script("getTF", 
			                                    ScriptType.INDEXED, "groovy", params)))
			                          .boostMode("replace"))	
			                .setExplain(true)
			                .setFrom(0)
			                .setSize(10)
			                .execute()
			                .actionGet();
		
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
			 if(count>0)
			 {
				 sumTf.put(ty,(long) count);
				 final_hmap.put(ty,r);	
			 }
			}			
			okapiTF.matchingScore(q_no, len, final_hmap);
			//Tf_idf.tf_idf(q_no, len, final_hmap);
			 //OkapiBM25.matchingScore(q_no, len, final_hmap);
			//LaplaceSmoothing.matchingScore(q_no, len, final_hmap, vocabSize, docnos);
			//LaplaceJelinek.matchingScore(q_no, len, final_hmap, vocabSize, docnos, sumTf);
			line = br.readLine();
		}
	}
	
	
}
