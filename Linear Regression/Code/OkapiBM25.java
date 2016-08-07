package IR.assn6;


import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;


	
	
public class OkapiBM25 {
	
	public static double BM25(int freq, String docno, int length, int dfw) throws UnknownHostException, FileNotFoundException
	{
		int docLength = length;
		double avgDocLength = 247;
		double dterm = 0;
		double denominator=0;
		double okapi = 0;
		double k1 = 1.2;
		double b = 0.75;
		int k2= 500;
		
		double firstTerm = Math.log10((84678 + 0.5)/(dfw + 0.5));
		double secondTerm = ((freq +(k1 * freq) )/(freq + k1 *((1-b) + (b * (length/avgDocLength)))));
		double thirdTerm = ((1 + (k2* 1))/(1+k2));
		double Bm25 = firstTerm * secondTerm * thirdTerm;

		return Bm25;
	}
	
	public static LinkedHashMap<String,Double> matchingScore(int qno, HashMap<String,Integer> len, HashMap<String,ArrayList<Tf_index>> tf) throws IOException
	{
		HashMap<String, Double> matchingScore = new HashMap<String , Double>();	
		for(Entry<String,ArrayList<Tf_index>> e : tf.entrySet())
		{
			ArrayList<Tf_index> doc_termFreq = e.getValue();
			int dfw = doc_termFreq.size();
			for(Tf_index i : doc_termFreq)
			{
				
				int length = len.get(i.docid);
				double score =BM25((int)i.freq,i.docid , length, dfw);
				if(!matchingScore.containsKey(i.docid))
				{
					matchingScore.put(i.docid,score);
				}
				else
				{
					double temp = matchingScore.get(i.docid);
					temp += score;
					matchingScore.put(i.docid, temp);
				}
			}
		}
		int rank = 1;
				LinkedHashMap<String, Double> output= ranking(matchingScore);
				return output;
//				String fileName = "okapiBM25_output.txt";
//				File file =new File( fileName);
//				if(!file.exists())
//				{
//		    	 	file.createNewFile();
//		    	 	
//				}
//				FileWriter fw = new FileWriter(file,true);
//				BufferedWriter bw = new BufferedWriter(fw);
//				PrintWriter pw = new PrintWriter(bw);
//				for(Entry<String,Double> e : output.entrySet())
//				{
//					
//					pw.println(qno + " Q0 " + e.getKey() + " " + rank + " " + e.getValue() + " " + "Exp");
//					rank++;
//				}
//		    	 	
//				pw.close();
//				System.out.println(qno + "written to output");
		    		
	}
	
	public static LinkedHashMap<String, Double> ranking (HashMap<String, Double> map) 
	{
		List<HashMap.Entry<String,Double>> list =
	            new LinkedList<HashMap.Entry<String, Double>>(map.entrySet() );
	        Collections.sort( list, new Comparator<Map.Entry<String,Double>>()
	        {
	            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
	            {
	                return Double.compare(o2.getValue(), o1.getValue());
	            }
	        } );

	        LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
	        for (Map.Entry<String, Double> entry : list)
	        {
	            result.put( entry.getKey(), entry.getValue() );
	        }
	        
	        
		return result;
	}
	
}
