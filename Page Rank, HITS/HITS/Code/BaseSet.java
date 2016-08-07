package IR.assn4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class BaseSet 
{
	public static HashSet<String> BaseSet = new HashSet<String>();
	public static HashSet<String> docs = new HashSet<String>();
	public static void getBase() throws IOException
	{
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		
		HashMap<String,HashSet<String>> inlinks = new HashMap<String,HashSet<String>>();
		HashMap<String,HashSet<String>> outlinks = new HashMap<String,HashSet<String>>();
		getDocList();
		
		
		HashSet<String> rootSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader("RootSet.txt"));
		String data = br.readLine();
		while(data!=null)
		{		
			rootSet.add(data.trim());
			data=br.readLine();
		}
		br.close();
		
		
		BaseSet.addAll(rootSet);
		String[] a = {"docno","out_links"};
		for(String docno: rootSet)
		{
			GetResponse response = client.prepareGet("four", "document",docno )
					.setFetchSource(a,null)
					.get();
			if(response.isExists())
			{

				String out = (String) response.getSource().get("out_links");
				String[] temp2 = out.split("\t");
				HashSet<String> o = new HashSet<String>();
				for(String j : temp2)
				{
					o.add(j);
				}		
				//System.out.println(" "+o.size());
				outlinks.put(docno,o);
			}
			
		}
		
		String[] b = {"docno","in_links"};
		for(String docno: rootSet)
		{
			GetResponse response = client.prepareGet("four", "document",docno )
					.setFetchSource(b,null)
					.get();
			if(response.isExists())
			{
				//System.out.println(docno);
				String  in = (String)response.getSource().get("in_links");
				String[] temp1 = in.split("\t");
				HashSet<String> i = new HashSet<String>();
				for(String j : temp1)
				{
					i.add(j);
				}
				//System.out.print(i.size());
				inlinks.put(docno,i);
							}
			
		}
		
		
		
		
		
		for(Entry<String,HashSet<String>> e : inlinks.entrySet())
		{
			
			processInlinks(e.getValue());
		}
		
		for(Entry<String,HashSet<String>> e : outlinks.entrySet())
		{
			
			processOutlinks(e.getValue());
		}
		
		printBase();
		
	}
	
	
	public static void processInlinks(HashSet<String> in)
	{
		
		int d = 1;
		HashSet<String> temp = new HashSet<String>();
		for(String s: BaseSet)
		{
			temp.add(s.toLowerCase());
		}
		for(String s : in )
		{
			
			if(d<50 && !temp.contains(s.toLowerCase()))
			{
				BaseSet.add(s);
				d++;
			}
		}
	}
	
	
	public static void processOutlinks(HashSet<String> out)
	{
		
		HashSet<String> temp = new HashSet<String>();
		for(String s: BaseSet)
		{
			temp.add(s.toLowerCase());
		}
		for(String s: out)
		{
			
			if(docs.contains(s)&& !temp.contains(s.toLowerCase()))
			{
				BaseSet.add(s);
			}
		}
	}
	
	public static void getDocList() throws IOException
	{
		 BufferedReader br1 = new BufferedReader(new FileReader("MergedUrlList.txt"));
			String data = br1.readLine();
			while(data!=null)
			{
				docs.add(data.trim());
				data=br1.readLine();
			}
			br1.close();
			
	}
	

	public static void printBase() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw = new PrintWriter("baseSet.txt","UTF-8");
		for(String s: BaseSet)
		{
			
			pw.println(s);
			
		}
    	 	
		pw.close();
	}
}
