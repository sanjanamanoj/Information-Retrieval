package IR.assn1;



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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;

public class okapiTF 
{
	
	
	
	public static double okapiScore(int freq, String docno, int length) throws UnknownHostException, FileNotFoundException
	{
		int docLength = length;
		double avgDocLength = 247;
		double dterm = 0;
		double denominator=0;
		double okapi = 0;
		
//		BufferedReader br = new BufferedReader(new FileReader("doc_length.txt"));
//		try 
//		{
//			String line = br.readLine();
//			while(line != null)
//			{
//				if(line.contains("avg_len"))
//				{
//					String[] temp = line.split("len:");
//					avgDocLength=Double.parseDouble(temp[1]);
//				
//				}
//				if(line.contains(docno))
//				{
//					String[] temp = line.split("length:");
//					docLength = Integer.parseInt(temp[1]);				 
//					break;
//				}
//				else
//					line=br.readLine();
//				}
//			
				dterm = (docLength/avgDocLength);
				denominator = freq + 0.5 + 1.5 + dterm;
				okapi = freq/denominator;
//				System.out.println(okapi);
//				System.out.println(docLength);
//				System.out.println(avgDocLength);	
//				
//		}
//		catch (IOException e) 
//		{
//			
//			e.printStackTrace();
//		}
		return okapi;
	}
	
	public static void matchingScore(int qno, HashMap<String,Integer> len, HashMap<String,ArrayList<Tf_index>> tf) throws IOException
	{
		HashMap<String, Double> matchingScore = new HashMap<String , Double>();	
		for(Entry<String,ArrayList<Tf_index>> e : tf.entrySet())
		{
			ArrayList<Tf_index> doc_termFreq = e.getValue();
			for(Tf_index i : doc_termFreq)
			{
				// TODO: change later
				int length = len.get(i.docid);
				double score =okapiScore((int)i.freq,i.docid , length);
				if(matchingScore.get(i.docid)==null)
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
				TreeMap<String, Double> output= ranking(matchingScore);
				String fileName = "okapi_output.txt";
				File file =new File( fileName);
				if(!file.exists())
				{
		    	 	file.createNewFile();
		    	 	
				}
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				for(Entry<String,Double> e : output.entrySet())
				{
					if(rank>1000)
					{
						break;
					}
					pw.println(qno + " Q0 " + e.getKey() + " " + rank + " " + e.getValue() + " " + " Exp");
					rank++;
				}
		    	 	
				pw.close();
				System.out.println(qno + "written to output");
		    		
	}
	
	public static TreeMap<String, Double> ranking (HashMap<String, Double> map) 
	{
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
}
