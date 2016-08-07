package IR.assn3;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.EnglishStemmer;

public class Test {
	 
	 public static void pattern(File file,HashMap<String,HashSet<String>> inlink,Client client) throws IOException, InterruptedException, ExecutionException
	 {
		// Object object = new Object();
		 System.out.println("in pattern");
	
		 String docno="",head="",text="",outlinks="",url="",author="",source="",httpheader="";
		 int depth=Integer.MAX_VALUE;
		 //StringBuilder st = new StringBuilder();
		 String str= readFullFile(file);
		
		 Document doc = Jsoup.parse(str,"UTF-8");
		 Elements docs = doc.getElementsByTag("DOC");
		 for(Element d : docs)
		 {
			  docno = d.getElementsByTag("DOCNO").first().text();
			  System.out.println("docno:"+docno);
			  head = d.getElementsByTag("TITLE").first().text();
			  System.out.println("title:"+head);
			  text = d.getElementsByTag("TEXT").first().text();
			  System.out.println("text:"+text);
			  outlinks = d.getElementsByTag("OUTLINKS").first().text();
			  System.out.println("out:"+outlinks);
			  url = d.getElementsByTag("URL").first().text();
			  System.out.println("url:"+url);
			  author = d.getElementsByTag("AUTHOR").first().text();
			  System.out.println("suthor:"+author);
			  source = d.getElementsByTag("SOURCE").first().text();
			  System.out.println("source:"+source);
			  httpheader = d.getElementsByTag("HTTPHEADER").first().text();
			  System.out.println("httpheader:"+httpheader);

				HashSet<String> inlinks = new HashSet<String>();
				String in="";
				if(inlink.get(docno) != null)
				{
					inlinks.addAll(inlink.get(docno));
				}
				
				for(String s : inlinks)
				{
					in+=s;
					in+="\t";
				}
				HashSet<String> op = new HashSet<String>();
				String[] o = outlinks.split("\t");
				for(int y=0;y<o.length;y++)
				{
					op.add(o[y]);
				}
				
				GetResponse response = client.prepareGet("sanjindex", "document", docno).get();
		        if(response.isExists())
		        {
		        	String in1="",out1="",auth1="",dep="";
		        		
		        		 in1 = (String)response.getSource().get("inlinks");
		        		
		        			out1 = (String)response.getSource().get("outlinks");
		        		
		        			auth1 = (String)response.getSource().get("author");
		        			int wave = (Integer) response.getSource().get("depth");
		        			
		        				
		        				if(wave< depth)
		        					depth = wave;
		        			
					if(in1!=null)
					{
						String[] temp = in1.split("\t");
						for(int h=0;h<temp.length;h++)
						{
							if(!(inlinks.contains(temp[h])))
							{
								in+="\t";
								in+=temp[h];
							}
						}
					}
					if(out1!=null)
					{
						String[] temp = out1.split("\t");
						for(int h=0;h<temp.length;h++)
						{
							if(!(op.contains(temp[h])))
							{
								outlinks+="\t";
								outlinks+=temp[h];
							}
						}
					}
					if(auth1!=null)
					{
						String[] a = auth1.split("\t");
						for(int k =0;k<a.length;k++)
							{
								author+="\t";
								author+=a[k];
							}
								//author+="testing";
					}
		        }
			  IndexRequest indexRequest = new IndexRequest("sanjindex", "document", docno)
	        	        .source(XContentFactory.jsonBuilder()
	        	            .startObject()
	        	            .field("docno", docno)
	        	            .field("HTTPheader",httpheader)
	        	            .field("title",head)
	        	            .field("text", text)
	        	            .field("html_Source",source)
	        	           .field("in_links",in)
	        	           .field("out_links",outlinks)
	        	           .field("author",author)
	        	           .field("depth",depth)
			    	       .field("url",url)
	    	            .endObject());
	         // TODO: change again
			  UpdateRequest updateRequest = new UpdateRequest("sanjindex", "document", docno)
	        	        .doc(XContentFactory.jsonBuilder()
	        	            .startObject()
	        	                .field("outlinks", outlinks)
	        	                .field("inlinks",in)
	        	                .field("author",author)
	        	                .field("depth",depth)
	        	            .endObject())
	        	        .upsert(indexRequest);
	        		client.update(updateRequest).get();
				
	         docno="";head="";text="";outlinks="";url="";depth=Integer.MAX_VALUE;author="";in="";
	         source="";httpheader="";
	       System.exit(0);
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
		System.out.println("reading full file");
		StringBuilder str = new StringBuilder();
		//String str = "";
		String n = "./corpus/"+file.getName().toString();
		BufferedReader br = new BufferedReader(new FileReader(n));
		
		String line = br.readLine();
		
		while(line!=null)
		{
			str.append(line);
			line=br.readLine();
		}
		br.close();
		System.out.println("returning string");
		String st = str.toString();
		st= st.replace("<HEAD>","<TITLE>");
		st= st.replace("</HEAD>","<TITLE>");
		
//		st= st.replace("<TEXT>","<MYDATA>");
//		st= st.replace("</TEXT>","</MYDATA>");
		
		
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
