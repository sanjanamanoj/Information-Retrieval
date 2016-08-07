package IR.assn4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class HITS
{
	static ArrayList<Double> prevPerplexity1 = new ArrayList<Double>();
	static ArrayList<Double> prevPerplexity2 = new ArrayList<Double>();
	static HashMap<String,Double> authority = new HashMap<String,Double>();
	static HashMap<String,Double> oldAuth = new HashMap<String,Double>();
	static HashMap<String,Double> hub = new HashMap<String,Double>();
	static HashMap<String,Double> oldHub = new HashMap<String,Double>();
	static HashMap<String,HashSet<String>> inlinks = new HashMap<String,HashSet<String>>();
	static HashMap<String,HashSet<String>> outlinks = new HashMap<String,HashSet<String>>();
	static HashSet<String> rootSet = new HashSet<String>();
	static HashSet<String> Base = new HashSet<String>();
	public static void main(String[] args) throws IOException
	{
		//Getting root set
		System.out.println("Creating root set");
		RootSet.getRoot();
		BufferedReader br = new BufferedReader(new FileReader("RootSet.txt"));
		String data = br.readLine();
		while(data!=null)
		{		
			rootSet.add(data.trim());
			data=br.readLine();
		}
		br.close();
		System.out.println("RootSet size:"+rootSet.size());
		
		//Getting base set
		System.out.println("Creating base set");
		BaseSet.getBase();
		BufferedReader br1 = new BufferedReader(new FileReader("baseSet.txt"));
		String line = br1.readLine();
		while(line!=null)
		{		
			Base.add(line.trim());
			line=br1.readLine();
		}
		br1.close();
		System.out.println("BaseSet size:"+Base.size());
		
		//Creating the link graph for baseSet
		System.out.println("Creating link graph for Base set");
		getLinks();
	
		//Initializing and starting HITS algorithm
		System.out.println("Starting HITS algorithm");
		double init = 1;
		for(String s : Base)
		{
			authority.put(s,init);
			hub.put(s,init);
		}
		
		AuthorityUpdate();
		HubUpdate();
		AuthorityNormalize();
		HubNormalize();
		while(!(isConverge(oldAuth,authority) && isConverge(oldHub,hub)))
		{
			oldAuth = new HashMap<String,Double>();
			oldHub = new HashMap<String,Double>();
			
			oldAuth.putAll(authority);
			AuthorityUpdate();
			oldHub.putAll(hub);
			HubUpdate();
			AuthorityNormalize();
			HubNormalize();			
		}
		printresult(authority,"Authority.txt");
		printresult(hub,"Hub.txt");
		
	}
	
	public static void AuthorityUpdate()
	{
		for(String s : Base)
		{
			double score =0;
			HashSet<String> in = new HashSet<String>();
			if(inlinks.containsKey(s))
			{
				in.addAll(inlinks.get(s));
				for(String i : in)
				{
					score+=hub.get(i);
				}
				authority.put(s, score);
			}
			
		}
	}
	
	public static void HubUpdate()
	{
		for(String s : Base)
		{
			double score =0;
			HashSet<String> out = new HashSet<String>();
			if(outlinks.containsKey(s))
			{
				out.addAll(outlinks.get(s));
				for(String i : out)
				{
					score+=authority.get(i);
				}
				hub.put(s, score);
			}
			
		}
	}
	
	public static void AuthorityNormalize()
	{
		double score = 0;
		for(String s:Base)
		{
			score+=(authority.get(s) * authority.get(s)) ;
		}
		double anorm  = Math.sqrt(score);
		for(String s:Base)
		{
			Double temp = authority.get(s)/anorm;
			authority.put(s,temp);
		}
	}
	
	public static void HubNormalize()
	{
		double score = 0;
		for(String s:Base)
		{
			score+=(hub.get(s)* hub.get(s));
		}
		double hnorm  = Math.sqrt(score);
		for(String s:Base)
		{
			Double temp = hub.get(s)/hnorm;
			hub.put(s,temp);
		}
	}

	public static boolean isConverge(HashMap<String,Double> prev,HashMap<String,Double> updated)
	{
		double epsilon = 0.000001;
		for(Entry<String,Double> e: prev.entrySet())
		{
			double diff = Math.abs(e.getValue()-updated.get(e.getKey()));
			if(diff>epsilon)
			{
				return false;
			}
		}
		
		return true;
	}
	public static void getLinks() throws UnknownHostException
	{
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		String[] a = {"docno","out_links"};

		for(String docno: Base)
		{
			GetResponse response = client.prepareGet("four", "document",docno)
					.setFetchSource(a,null)
					.get();
			
			if(response.isExists())
			{

				String out = (String) response.getSource().get("out_links");
				String[] temp2 = out.split("\t");
				HashSet<String> o = new HashSet<String>();
				for(String j : temp2)
				{
					if(Base.contains(j))
						o.add(j);
				}		
				outlinks.put(docno,o);
			}
			
		}
		
		String[] b = {"docno","in_links"};
		for(String docno: Base)
		{
			GetResponse response = client.prepareGet("four", "document",docno)
					.setFetchSource(b,null)
					.get();
			
			if(response.isExists())
			{
				String  in = (String)response.getSource().get("in_links");
				String[] temp1 = in.split("\t");
				HashSet<String> i = new HashSet<String>();
				for(String j : temp1)
				{
					if(Base.contains(j))
						i.add(j);
				}
				inlinks.put(docno,i);
				
			}
		}
		

	
}
	
	
	
	public static void printresult(HashMap<String,Double> pages,String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		int count = 0;
		LinkedHashMap<String, Double> result = new LinkedHashMap<String,Double>();
		result.putAll(sortPages(pages));
		System.out.println("Printing result");
		PrintWriter pw = new PrintWriter(filename,"UTF-8") ;
		for (Entry<String, Double> entry : result.entrySet())
        {
			count++;
			if(count<=500)
				pw.println(entry.getKey()+"\t"+entry.getValue());
			else
				break;
		}
		pw.close();
		System.out.println("Printing completed");
	}
	
	public static LinkedHashMap<String,Double> sortPages(HashMap<String,Double> pages)
	{
		List<HashMap.Entry<String,Double>> list =
	            new LinkedList<HashMap.Entry<String, Double>>(pages.entrySet() );
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
