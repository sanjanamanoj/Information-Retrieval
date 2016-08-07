package IR.assn7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

public class UnigramFeature
{
	public static HashSet<String> dictionary = new HashSet<String>();
	public static LinkedHashMap<String,Integer> words = new LinkedHashMap<String,Integer>();
	public static LinkedHashMap<String,LinkedHashMap<Integer,Integer>> trainFeatureMap = new LinkedHashMap<String,LinkedHashMap<Integer,Integer>>();
	public static LinkedHashMap<String,LinkedHashMap<Integer,Integer>> testFeatureMap = new LinkedHashMap<String,LinkedHashMap<Integer,Integer>>();
	public static HashMap<String,Integer> spamDeterminant = new HashMap<String,Integer>();
	
	public static void main(String[] args) throws IOException
	{
		loadDictionary("dictionary.txt");
		getUnigramMap();
	//	printMap();
		printFeatureMap(trainFeatureMap,"Part2/UnigramTrainDocs.txt");
		printFeatureMap(testFeatureMap,"Part2/UnigramTestDocs.txt");
		//printUnigramMap();
		
	}
	
	public static void loadDictionary(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			dictionary.add(line.toLowerCase().trim());
			line = br.readLine();
		}
		br.close();
	}
	
	public static void printMap() throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter("Part2/unigramMap.txt");
		for(Entry<String,Integer> e : words.entrySet())
		{
			pw.println(e.getKey()+" "+e.getValue());
		}
		pw.close();
	}
	public static void printFeatureMap(LinkedHashMap<String,LinkedHashMap<Integer,Integer>>map,String filename) throws FileNotFoundException
	{
		System.out.println("printing map");
		PrintWriter pw = new PrintWriter(filename);
		for(Entry<String,LinkedHashMap<Integer,Integer>> e : map.entrySet())
		{
			pw.println(e.getKey());
//			LinkedHashMap<Integer, Integer> hmap = new LinkedHashMap<Integer, Integer>();
//			hmap.putAll(sortMap(e.getValue()));
//			for(Entry<Integer,Integer> e1 : hmap.entrySet())
//			{
//				pw.print(e1.getKey()+":"+e1.getValue()+" ");
//			}
//			pw.println();
		}
		pw.close();
		System.out.println("done printing");
	}
	
	
	public static LinkedHashMap<Integer,Integer> sortMap(LinkedHashMap<Integer,Integer> map)
	{

		List<HashMap.Entry<Integer,Integer>> list =
	            new LinkedList<HashMap.Entry<Integer,Integer>>(map.entrySet() );
	        Collections.sort( list, new Comparator<Map.Entry<Integer,Integer>>()
	        {
	            public int compare( Map.Entry<Integer,Integer> o1, Map.Entry<Integer,Integer> o2 )
	            {
	                return Integer.compare(o1.getKey(), o2.getKey());
	            }
	        } );

	        LinkedHashMap<Integer,Integer> result = new LinkedHashMap<Integer,Integer>();
	        for (Map.Entry<Integer,Integer> entry : list)
	        {
	            result.put( entry.getKey(), entry.getValue() );
	        }
	        
	        
		return result;
	}
	
	
	public static void getUnigramMap() throws UnknownHostException, FileNotFoundException
	{
		int count = 1;
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		String[] a = {"text","docno"};
		
		SearchResponse scrollResp = client.prepareSearch("spams")
				.setScroll(new TimeValue(120 * 60000))
	            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	            //.setFetchSource(a, null)
	            .setSize(1)
				   .execute()
				   .actionGet();
		while (true) 
		 {			 
			 for (SearchHit hit : scrollResp.getHits().getHits()) 
			 {				 
				 String text = (String) hit.getSource().get("text");
				 String docno = (String) hit.getSource().get("docno");
				 String type = (String) hit.getSource().get("type");
				 String label = (String) hit.getSource().get("spam");
				 String[] temp =text.split(" ");
				 
				 if(label.equals("yes"))
					 spamDeterminant.put(docno,1);
				 else
					 spamDeterminant.put(docno,0);
				 
				 for(String s: temp)
				 {
					
						 if(!words.containsKey(s.toLowerCase().trim()))
						 {
							 words.put(s.toLowerCase().trim(),count);
							 count++;
						 }
					 
					
				 }
				 
				 if(type.equals("train"))
				 {
					 trainMap(temp,docno);
				 }
				 else
				 {
					 testMap(temp,docno);
				 }
				
				 System.out.println(trainFeatureMap.size()+" "+testFeatureMap.size());
			
			 }
			// 

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
	}

	 public static void trainMap(String[] text,String docno)
	 {
		if(trainFeatureMap.containsKey(docno))
		{
			LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();
			temp.putAll(trainFeatureMap.get(docno));
			for(String s:text)
			{
				if(words.containsKey(s))
				{
					if(temp.containsKey(words.get(s)))
					{
						temp.put(words.get(s), temp.get(words.get(s))+1);
					}
					else
					{
						temp.put(words.get(s), 1);
					}
				}
				
			}
			trainFeatureMap.put(docno,temp);
		}
		else
		{
			LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();
			
			for(String s:text)
			{
				if(words.containsKey(s))
				{
					if(temp.containsKey(words.get(s)))
					{
						temp.put(words.get(s), temp.get(words.get(s))+1);
					}
					else
					{
						temp.put(words.get(s), 1);
					}
				}
				
			}
			trainFeatureMap.put(docno,temp);
		}
	 }
	
	 
	 public static void testMap(String[] text,String docno)
	 {
		 if(testFeatureMap.containsKey(docno))
			{
				LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();
				temp.putAll(testFeatureMap.get(docno));
				for(String s:text)
				{
					if(words.containsKey(s))
					{
						if(temp.containsKey(words.get(s)))
						{
							temp.put(words.get(s), temp.get(words.get(s))+1);
						}
						else
						{
							temp.put(words.get(s), 1);
						}
					}
					
				}
				testFeatureMap.put(docno,temp);
			}
			else
			{
				LinkedHashMap<Integer,Integer> temp = new LinkedHashMap<Integer,Integer>();
				
				for(String s:text)
				{
					if(words.containsKey(s))
					{
						if(temp.containsKey(words.get(s)))
						{
							temp.put(words.get(s), temp.get(words.get(s))+1);
						}
						else
						{
							temp.put(words.get(s), 1);
						}
					}
					
				}
				testFeatureMap.put(docno,temp);
			}
	 }
//	public static void printUnigramMap() throws FileNotFoundException
//	{
//		PrintWriter pw = new PrintWriter("unigram.txt");
//		for(String s:words)
//		{
//			pw.println(s);
//		}
//		
//		pw.close();
//	}
}
