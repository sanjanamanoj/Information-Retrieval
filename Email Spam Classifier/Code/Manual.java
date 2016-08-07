package IR.assn7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

// This program creates the test and train maps for a list of spam words given in "OfficialSpamWords.txt". It creates a HashMap for the spam words and dumps it in a file.

public class Manual
{

  // List of spam words with a number assigned to eaxh word. So the test and train files use these numbers to represent the words
	public static LinkedHashMap<String,Integer> spamMap = new LinkedHashMap<String,Integer>();
	public static LinkedHashMap<String,LinkedHashMap<Integer,Integer>> trainFeatureMap = new LinkedHashMap<String,LinkedHashMap<Integer,Integer>>();
	public static LinkedHashMap<String,LinkedHashMap<Integer,Integer>> testFeatureMap = new LinkedHashMap<String,LinkedHashMap<Integer,Integer>>();

  //Used to determine if the given file is spam or not
	public static HashMap<String,Integer> spamDeterminant = new HashMap<String,Integer>();

	public static void main(String[] args) throws IOException
	{
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		createSpamMap("OfficialSpamWords.txt");
		System.out.println(spamMap.size());
		dumpSpamMap();
		query(client);
		dumpFeatureMap("Part1/traindocs.txt",trainFeatureMap);
		dumpFeatureMap("Part1/testdocs.txt",testFeatureMap);


	}

  // Dumps the feature map in the format required by the LIBLINEAR library
	public static void dumpFeatureMap(String filename, LinkedHashMap<String,LinkedHashMap<Integer,Integer>> map) throws FileNotFoundException
	{
		System.out.println("Dumping feature map");
		PrintWriter pw = new PrintWriter(filename);
		for(Entry<String,LinkedHashMap<Integer,Integer>> e: map.entrySet())
		{
			//pw.println(e.getKey());
			pw.println(spamDeterminant.get(e.getKey())+" ");
			for(Entry<Integer,Integer> e1: e.getValue().entrySet())
			{
				pw.print(e1.getKey()+":"+e1.getValue()+" ");
			}
			pw.println();
		}
		pw.close();
	}



	public static void dumpSpamMap() throws FileNotFoundException
	{
		System.out.println("dumping spam map");
		PrintWriter pw = new PrintWriter("Part1/map.txt");
		for(Entry<String,Integer> e: spamMap.entrySet())
		{
			pw.println(e.getKey()+" "+e.getValue());
		}
		pw.close();
	}


	public static void createSpamMap(String filename) throws IOException
	{
		int count =1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			if(!spamMap.containsKey(line.trim()))
			{
				spamMap.put(line.toLowerCase().trim(),count);
				count++;
			}

			line = br.readLine();
		}
		br.close();

	}


	public static void query(Client client)
	{


		String[] a = {"text"};
		for(Entry<String,Integer> e:spamMap.entrySet())
		{
			System.out.println(e.getKey());
		//String ty = "free";
			Map<String, Object> params;
			 params = new HashMap<String, Object>();
		        params.put("term", e.getKey());
		        params.put("field", "text");

			 SearchResponse scrollResp = client.prepareSearch("spams")
		                .setTypes("document")
		                .setScroll(new TimeValue(120 * 60000))
		                .setQuery(QueryBuilders.functionScoreQuery
		                          (QueryBuilders.termQuery("text", e.getKey()),
		                            new ScriptScoreFunctionBuilder(new Script("getTF",
		                                    ScriptType.INDEXED, "groovy", params)))
		                          .boostMode("replace"))
		                .setFetchSource( null,a)
		                .setExplain(true)
		                .setSize(100)
		                .execute()
		                .actionGet();

	 while (true)
	 {
		 for (SearchHit hit : scrollResp.getHits().getHits())
		 {
			 String type = (String)hit.getSource().get("type");
			 String docno =  (String) hit.getSource().get("docno");
			 String label = (String)hit.getSource().get("spam");
			 int tf = (int) hit.getScore();
			 if(label.equals("yes"))
				 spamDeterminant.put(docno,1);
			 else
				 spamDeterminant.put(docno,0);
			 if(type.equals("train"))
			 {
				 LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();

				 if(trainFeatureMap.containsKey(docno))
				 {
					 temp.putAll(trainFeatureMap.get(docno));
					 temp.put(e.getValue(), tf);
					 trainFeatureMap.put(docno, temp);
				 }
				 else
				 {
					 temp.put(e.getValue(), tf);
					 trainFeatureMap.put(docno,temp);
				 }


			 }
			 else
			 {
				 LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();

				 if(testFeatureMap.containsKey(docno))
				 {
					 temp.putAll(testFeatureMap.get(docno));
					 temp.put(e.getValue(), tf);
					 testFeatureMap.put(docno, temp);
				 }
				 else
				 {
					 temp.put(e.getValue(), tf);
					 testFeatureMap.put(docno,temp);
				 }

			 }

		 }

		 try{
		 scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
				 .setScroll(new TimeValue(120 * 60000))
				 .execute()
				 .actionGet();
		 }
		 catch(Exception e1)
		 {
		 	continue;
		 }

		 //Break condition: No hits are returned
		 if (scrollResp.getHits().getHits().length == 0)
		 {
			 break;
		 }

	 }





}
}


}
