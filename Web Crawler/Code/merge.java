package IR.assn3;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
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
import org.tartarus.snowball.ext.EnglishStemmer;



public class merge {
	 static HashMap<String,Offset> catalog = new HashMap<String, Offset>();
	 public static void pattern(File file,HashMap<String,HashSet<String>> inlink,Client client) throws IOException, InterruptedException, ExecutionException 
	 {
		// Object object = new Object();
		 System.out.println("in pattern");
	
		 String docno="",head="",text="",outlinks="",url="",author="",source="",httpheader="";
		 int depth=Integer.MAX_VALUE;
		 //StringBuilder st = new StringBuilder();
		 String str;
		
			str = readFullFile(file);
		
		

		Pattern pattern = Pattern.compile("<DOC>(.*?)</DOC>");
		Matcher matcher = pattern.matcher(str);	
		
		Pattern pattern1 = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
		Pattern pattern2 = Pattern.compile("<HEAD>(.*?)</HEAD>");
		Pattern pattern3 = Pattern.compile("<TEXT>(.*?)</TEXT>");
		Pattern pattern4 = Pattern.compile("<OUTLINKS>(.*?)</OUTLINKS>");
		Pattern pattern5 = Pattern.compile("<DEPTH>(.*?)</DEPTH>");
		Pattern pattern6 = Pattern.compile("<AUTHOR>(.*?)</AUTHOR>");
		Pattern pattern7 = Pattern.compile("<URL>(.*?)</URL>");
//		Pattern pattern8 = Pattern.compile("<HTTPHEADER>(.*?)</HTTPHEADER>");
//		Pattern pattern9 = Pattern.compile("<SOURCE>(.*?)</SOURCE>");
		
		ArrayList<String> docs = new ArrayList<String>();
		//System.out.println("entering while");
		int count=0;
//		
		while (matcher.find())
		{
			//System.out.println("ssss");
			//System.out.println(count++);
			docs.add(matcher.group(1));
		}
		
		for(String doc : docs)
		{
						
			Matcher docno1 = pattern1.matcher(doc);	
			while(docno1.find())
			{
			
				docno = docno1.group(1);
				System.out.println(docno);
			}
			
			Matcher head1 = pattern2.matcher(doc);	
			while(head1.find())
			{
				head = head1.group(1);
				//System.out.println(head);
			}
			
			Matcher text1 = pattern3.matcher(doc);	
			while(text1.find())
			{
				text= text1.group(1);
			}
			
			Matcher out = pattern4.matcher(doc);	
			while(out.find())
			{
				outlinks = out.group(1);
			}
			
			Matcher depth1 = pattern5.matcher(doc);	
			while(depth1.find())
			{
				depth= Integer.parseInt(depth1.group(1).trim());
			}
			
			Matcher author1 = pattern6.matcher(doc);	
			while(author1.find())
			{
				author= author1.group(1);
			}
			
			Matcher url1 = pattern7.matcher(doc);	
			while(url1.find())
			{
				url= url1.group(1);
			}
		
//			Matcher http = pattern8.matcher(doc);	
//			while(http.find())
//			{
//				httpheader= http.group(1);
//			}
//			
//			Matcher src = pattern9.matcher(doc);	
//			while(src.find())
//			{
				//System.out.println(src.);
//				source= src.group(1);
//				//System.out.println(source);
//			}
//			if(catalog.containsKey(url))
//			{
//				long start = catalog.get(url).start;
//				long end = catalog.get(url).end;
//				System.out.println(start);
//				System.out.println( end-start);
//				 source = (new String(readFromFile("SanHTMLSourceF.txt",start,(int)(end-start))));
//				// System.out.println(source);
				 
//			}
			
			//System.out.println(source);
			//Response r = Jsoup.
//			
//			try{
//				Document d = Jsoup.connect(url).get();
//				source =	d.html();
//			}
//			catch(Exception e2)
//			{
//				e2.printStackTrace();
//				continue;
//			}
			
			//System.out.println(source);
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
			
			
				GetResponse response = client.prepareGet("four", "document", docno).get();
				System.out.println("got response");
		        if(response.isExists())
		        {
		        	String in1="",out1="",auth1="";
		        		
		        		 in1 = (String)response.getSource().get("in_links");
		        		
		        			out1 = (String)response.getSource().get("out_links");
		        		
		        			auth1 = (String)response.getSource().get("author");
		        			int wave =  (Integer) response.getSource().get("depth");
		        			
		        				
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
						author="";
						HashSet<String> h = new HashSet<String>();
						String[] a = auth1.split("\t");
						for(int k =0;k<a.length;k++)
							{
								h.add(a[k]);
//								author+="\t";
//								author+=a[k];
							}
						h.add("Sanjana");
						for(String s: h)
						{
							author+="\t";
							author+=s;
						}
								//author+="testing";
					}
		        }
		
	        
//	        try{
//	        	 IndexResponse respons = client.prepareIndex("test2", "document", docno)
//		        		 .setSource(XContentFactory.jsonBuilder()
//			        	            .startObject()
//			        	            .field("docno", docno)
//			        	            .field("HTTPheader",httpheader)
//			        	            .field("title",head)
//			        	            .field("text", text)
//			        	           .field("html_Source",source)
//			        	           .field("in_links",in)
//			        	           .field("out_links",outlinks)
//			        	           .field("author",author)
//			        	           .field("depth",depth)
//					    	       .field("url",url)
//			    	            .endObject())
//		                .execute()
//		                .actionGet();
//	        }
//	        catch(Exception e)
//	        {
//	        	try{
//		        	 IndexResponse respons = client.prepareIndex("test2", "document", docno)
//			        		 .setSource(XContentFactory.jsonBuilder()
//				        	            .startObject()
//				        	            .field("docno", docno)
//				        	            .field("HTTPheader",httpheader)
//				        	            .field("title",head)
//				        	            .field("text", text)
//				        	           .field("html_Source","")
//				        	           .field("in_links",in)
//				        	           .field("out_links",outlinks)
//				        	           .field("author",author)
//				        	           .field("depth",depth)
//						    	       .field("url",url)
//				    	            .endObject())
//			                .execute()
//			                .actionGet();
//		        }
//	        	catch(Exception e1)
//	        	{
//	        		e1.printStackTrace();
//		        	continue;
//	        	}
//	        	e.printStackTrace();
//	        	continue;
//	        }
	       
	       // System.exit(0);
	    
	        	//System.out.println("source:"+source.substring(0,1000));
			  IndexRequest indexRequest = new IndexRequest("four", "document", docno)
	        	        .source(XContentFactory.jsonBuilder()
	        	            .startObject()
	        	            .field("docno", docno)
	        	           // .field("HTTPheader",httpheader)
	        	            .field("title",head)
	        	            .field("text", text)
	        	          // .field("html_Source",new String(source))
	        	           .field("in_links",in)
	        	           .field("out_links",outlinks)
	        	           .field("author",author)
	        	           .field("depth",depth)
			    	       .field("url",url)
	    	            .endObject());
	         // TODO: change again
			  UpdateRequest updateRequest = new UpdateRequest("four", "document", docno)
	        	        .doc(XContentFactory.jsonBuilder()
	        	            .startObject()
	        	                .field("out_links", outlinks)
	        	                .field("in_links",in)
	        	                .field("author",author)
	        	                .field("depth",depth)
	        	            .endObject())
	        	        .upsert(indexRequest);
	        		
						client.update(updateRequest).get();
					
			
	         docno="";head="";text="";outlinks="";url="";depth=Integer.MAX_VALUE;author="";in="";
	         source="";httpheader="";
			//System.exit(0);
		}
	  

 
	 }
	
	 public static HashMap<String,Offset> readFromC(String filename) throws IOException
		{
			HashMap<String,Offset> catalogMap = new HashMap<String,Offset>();		
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try 
			{
				String line = br.readLine();
				while(line != null)
				{
					String[] temp = line.split("\t");
					String term = temp[0];
					long start = Long.parseLong(temp[1]);
					long end = Long.parseLong(temp[2]);
					Offset t = new Offset(start,end);
					catalogMap.put(term, t);
					line = br.readLine();
				}
				
			} 		
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
			br.close();
//			
			return catalogMap;
		}
		
	 public static byte[] readFromFile(String filename, long position, int size) throws IOException 
		{
		        RandomAccessFile file = new RandomAccessFile(filename, "rw");
		        file.seek(position);
		        byte[] bytes = new byte[size];
		        file.read(bytes);
		        file.close();
		        return bytes;
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
		return str.toString();	
	}
	
	public static void main(String[] args) 
	{
		Settings settings = Settings.builder().put("cluster.name","hw4").build();
		Client client;
		try {
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			String target_dir = "corpus";
			//catalog.putAll(readFromC("SanHTMLSouceCatF.txt"));
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
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
	}
	
}
