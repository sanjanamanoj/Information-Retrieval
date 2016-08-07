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


	
	
public class LaplaceSmoothing {
	
	public static double laplace(int freq, int length, long vocab) throws UnknownHostException, FileNotFoundException
	{	
		//System.out.println("freq:"+ freq);
		//System.out.println("length:"+length);
		double numerator = freq+1;
		double denominator = length + vocab;
		double laplace = Math.log10((numerator / denominator));
		//System.out.println("laplace:" + laplace);
		return laplace;
	}
	
	public static LinkedHashMap<String,Double> matchingScore(int qno, HashMap<String,Integer> len, HashMap<String,ArrayList<Tf_index>> tf, long vocab, ArrayList<String> docnos) throws IOException
	{
		HashMap<String, Double> matchingScore = new HashMap<String , Double>();	
		for(Entry<String,ArrayList<Tf_index>> e : tf.entrySet())
		{
			ArrayList<String> alldoc = new ArrayList<String>();
			alldoc.addAll(docnos);
			ArrayList<Tf_index> doc_termFreq = e.getValue();
			int dfw = doc_termFreq.size();
			for(Tf_index i : doc_termFreq)
			{
				// TODO: change later
				alldoc.remove(i.docid);
				int length = len.get(i.docid);
				double score =laplace((int)i.freq, length, vocab);
				//double score = Math.log10(laplace);
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
			for(String unrelated : alldoc)
			{
				if(!matchingScore.containsKey(unrelated))
				{
					double length1 = len.get(unrelated);
					double score1 = Math.log10((1/(Double.valueOf(length1)+vocab)));
					matchingScore.put(unrelated,score1);
				}
				else
				{
					double length2 = len.get(unrelated);
					double score2 = Math.log10((1/(length2+vocab)));
					double temp = matchingScore.get(unrelated);
					temp += score2;
					matchingScore.put(unrelated, temp);
				}
			}
			System.out.println(matchingScore.size());
			
		}
			int rank = 1;
			LinkedHashMap<String, Double> output= ranking(matchingScore);
			return output;
//			String fileName = "laplaceSmoothing_output.txt";
//			File file =new File( fileName);
//			if(!file.exists())
//			{
//	    	 	file.createNewFile();
//	    	 	
//			}
//			FileWriter fw = new FileWriter(file,true);
//			BufferedWriter bw = new BufferedWriter(fw);
//			PrintWriter pw = new PrintWriter(bw);
//			for(Entry<String,Double> e1 : output.entrySet())
//			{
//				
//				pw.println(qno + " Q0 " + e1.getKey() + " " + rank + " " + e1.getValue() + " " + "Exp");
//				rank++;
//			}
//	    	 	
//			pw.close();
//			System.out.println(qno + "written to output");	
		    		
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
