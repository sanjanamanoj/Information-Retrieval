package IR.assn3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.engine.Engine.Get;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.tartarus.snowball.ext.EnglishStemmer;

public class robot {
	 
	 public static void pattern(File file,HashMap<String,HashSet<String>> inlink,Client client) throws IOException, InterruptedException, ExecutionException
	 {
		 System.out.println("in pattern");
	//String str = FileUtils.readFileToString(file);
	// System.out.println(str);
		 String docno="",head="",text="",outlinks="",url="",author="",source="",httpheader="";
		 int depth=Integer.MAX_VALUE;
		 //StringBuilder st = new StringBuilder();
		 String str= readFullFile(file);
		
//		
		Pattern pattern = Pattern.compile("the");
		Matcher matcher = pattern.matcher(str);	
		System.out.println(matcher.find());
		ArrayList<String> docs = new ArrayList<String>();
		//System.out.println("entering while");
		int count=0;
		//System.out.println(matcher.groupCount());
		//System.out.println(matcher.group(1));
		while (matcher.find())
		{
			//System.out.println(count++);
			docs.add(matcher.group(1));
		}
		for(String doc : docs)
		{
			System.out.println("Here");
			//System.out.println(doc);
			Pattern pattern1 = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
			Matcher docno1 = pattern1.matcher(doc);	
			while(docno1.find())
			{
			
				docno = docno1.group(1);
			}
			
		}
	  

 
	 }
	
	 public static HashMap<String,HashSet<String>> gettingCatalog() throws IOException
		{
			HashMap<String,HashSet<String>>inlinks = new HashMap<String,HashSet<String>>();
			BufferedReader br = new BufferedReader(new FileReader("inlinks57.txt"));
			try 
			{
				String line = br.readLine();
				while(line != null)
				{
					String[] temp = line.split("\t");
					//System.out.println(temp[0]);
					String url = temp[0];
					HashSet<String> temp1 = new HashSet<String>();
					for(int i =1;i<temp.length;i++)
					{
						temp1.add(temp[i]);
					}
					inlinks.put(url,temp1);
					temp1=new HashSet<String>();
					
					line = br.readLine();
				}
				
			} 		
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
			br.close();
			return inlinks;
		}
			
	public static String readFullFile(File file) throws IOException
	{
		String n = "./corpus/"+file.getName().toString();
		RandomAccessFile file1 = new RandomAccessFile(n, "rw");
	       
		System.out.println("reading full file");
		StringBuilder str = new StringBuilder();
		//String str = "";
		
		BufferedReader br = new BufferedReader(new FileReader(n));
		
		String line =  file1.readLine();
		
		while(line!=null)
		{
			str.append(line);
			line=br.readLine();
		}
		file1.close();
		System.out.println("returning string");
		System.out.println(str.toString());
		return str.toString();	
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Settings settings = Settings.builder().put("cluster.name","sanjanacluster").build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
	
		String target_dir = "corpus";
        File dir = new File(target_dir);
        //HashMap<String,Offset> inlinkCatalog = readFromCatalog("in_catalog58.txt");
        HashMap<String,HashSet<String>> inlink = new HashMap<String,HashSet<String>>();
        inlink.putAll(gettingCatalog());
        System.out.println("done creating map");
        File[] files = dir.listFiles();
        
		for (File file : files) {
		   //System.out.println(file.getName());
   System.out.println(file.getName());
		pattern(file,inlink,client);
		}
	}
	
}
