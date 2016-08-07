package IR.assn3;



import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.annotation.JsonValue;



import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class elastic
{


	static Integer counter=0;
	public static void getBuilders(File file,Client client,HashMap<String,HashSet<String>> inlink) throws IOException{
		//BufferedReader br = null;
		String docno = null,text ="",link="",out="",author="",title="";
		System.out.println("in get builders");
		String content = "", line= "";
	    try
	    {
	     
	      String n = "./corpus1/"+file.getName().toString();
			BufferedReader br = new BufferedReader(new FileReader(n));
	      int insideDoc = 0;
	      int insideText = 0;
	      int insideOutlink = 0;
	      
	      line = br.readLine();
	      while(line != null && line!="\n")
	      {
	    	  System.out.println("outside while");
	        if(line.contains("<DOC>"))
	        {
	        	insideDoc=1;
	        }
	        while(insideDoc==1 && line!=null)
	        {
//	        	if(line.trim()=="")
//	        		continue;
	        	if(line.contains("</DOC>"))
	        	{
	        		insideDoc=0;
	        		break;
	        	}
	        	
	        	if(line.contains("<DOCNO>"))
	        	{
	        		int length = line.length();
	        		docno= line.substring(7, length-8);
	        	}
	        	if(line.contains("<URL>"))
	        	{
	        		int length = line.length();
	        		link= line.substring(5, length-6);
	        	}
	        	if(line.contains("<AUTHOR>"))
	        	{
	        		int length = line.length();
	        		author= line.substring(8, length-9);
	        	}
	        	if(line.contains("<HEAD>"))
	        	{
	        		int length = line.length();
	        		title= line.substring(6, length-7);
	        	}

	        	if(line.contains("<OUTLINKS>"))
	        	{
	        		insideOutlink = 1;
	        	}
	        	while(insideOutlink==1)
	        	{
	        		line= br.readLine();
	        		if(line.contains("</OUTLINKS>"))
	        		{
	        			insideOutlink=0;
	        			break;
	        		}
	        		out+=" ";
	        		out+=line;
	        		
	        	}
	        	if(line.contains("<TEXT>"))
	        	{
	        		insideText=1;
	        	}
	        	while(insideText==1)
	        	{
	        		line = br.readLine();
	        		if(line.contains("</TEXT>"))
	        		{
	        			insideText=0;
	        			break;
	        		}
	        		text +=" ";
	        		text+= line;
	        		
	        	}
	        	line=br.readLine();
	        }
	        
//	        Offset o =inlinkCatalog.get(docno);
//	        String filename = "inlinks58.txt";
//			long start2 = o.start;
//			long end2 = o.end;
//			String in = (new String(readFromFile(filename , (int) start2, (int) (end2-start2))));
//			String[] temp = in.split("\t");
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
//			for(int i=1;i<temp.length;i++)
//			{
//				inlinks.add(temp[i]);
//			}
	        System.out.println("Parsed");
			XContentBuilder builder = null;
		   // data.add(builder);
//	        SearchResponse resp = client.prepareSearch()
//	        		.setTypes("document")
//	        		.setQuery(QueryBuilders.termQuery("docno", docno))
//	        		.execute().actionGet();
//	        System.out.println(resp);

		 
//				 for (SearchHit hit : resp.getHits().getHits()) 
//				 {				 
//					// System.out.println("true");
//					 String docn = (String) hit.getSource().get("docno");
//					String txt = (String)hit.getSource().get("text");
//					String op = (String)hit.getSource().get("outlinks");
//					String auth = (String)hit.getSource().get("author");
//					if(auth!=null)
//					{
//						String[] a = auth.split("\t");
//
//						for(int k =0;k<a.length;k++)
//						{
//							author+="\t";
//							author+=a[k];
//						}
//					}
//					else 
//					{
//						auth = "sanjana";
//					}
//					String ur = (String)hit.getSource().get("url");
//					String ip = (String)hit.getSource().get("inlinks");
//					if(ip!=null)
//					{
//						String[]t = ip.split("\t");
//						for(int j=0;j<t.length;j++)
//						{
//							if(!(inlinks.contains(t[j])))
//							{
//								in+="\t";
//								in+=t[j];
//							}
//						}
//					}
//					
//					
//					
//					
//				 }

			
//			System.out.println("docno:"+docno);
//			System.out.println("docno:"+docno);		
//			System.out.println("docno:"+docno);
			
	        System.out.println("builder");
	         builder = XContentFactory.jsonBuilder()
		    	    .startObject()
		    	        .field("docno", docno)
		    	        .field("text", text)
		    	        .field("head",title)
		    	       // .field("id",counter.toString())
		    	        .field("url",link)
		    	       .field("outlinks",out)
		    	        .field("inlinks",in)
		    	    .endObject();
	        
//	         System.out.println("docno:"+docno);
//				System.out.println("head:"+title);
//				System.out.println("url:"+link);
				//System.out.println("text:"+text);
	         
				System.out.println(builder.string());
				
	        IndexResponse response = client.prepareIndex("crawler", "document",docno)
	                .setSource(builder)
	                .execute()
	                .actionGet();
	        //System.out.println("counter" +counter);
	        
	        text="";
	        docno="";
	        link="";
	        out="";
	        in="";
	        author="";
	        title="";
	        line = br.readLine();
	        System.out.println("line:"+line);
	      }
	      br.close();      
	    }
	    catch(IOException e) {}
	   
		
	}
	    public static byte[] readFromFile(String filename, int position, int size) throws IOException 
		{
		        RandomAccessFile file = new RandomAccessFile(filename, "rw");
		        file.seek(position);
		        byte[] bytes = new byte[size];
		        file.read(bytes);
		        file.close();
		        return bytes;
		 }
	    
	    
	    public static HashMap<String,Offset> readFromCatalog(String filename) throws IOException
		{
			HashMap<String,Offset> catalogMap = new HashMap<String,Offset>();		
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try 
			{
				String line = br.readLine();
				while(line != null)
				{
					String[] temp = line.split(" ");
					System.out.println(temp[0]);
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
//			for (Entry<String,Offset> e1 : catalogMap.entrySet())
//			{
//				
//				System.out.println(e1.getKey());
//				System.out.println(e1.getValue().start);
//				System.out.println(e1.getValue().end);
//			}
			return catalogMap;
		}
	    
	    public static HashMap<String,HashSet<String>> gettingCatalog() throws IOException
		{
			HashMap<String,HashSet<String>>inlinks = new HashMap<String,HashSet<String>>();
			BufferedReader br = new BufferedReader(new FileReader("inlinks58.txt"));
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
	    
	public static void main(String[] args) throws Exception{
		
		List<XContentBuilder> b = new ArrayList<XContentBuilder>();
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		String target_dir = "corpus1";
        File dir = new File(target_dir);
        //HashMap<String,Offset> inlinkCatalog = readFromCatalog("in_catalog58.txt");
        HashMap<String,HashSet<String>> inlink = new HashMap<String,HashSet<String>>();
        inlink.putAll(gettingCatalog());
        System.out.println("done creating map");
        File[] files = dir.listFiles();
        
        int i = 1;
        int id = 1;
		for (File file : files) {
		   try
		    {
				//System.out.println(file.getName());
			   System.out.println(file.getName());
			    getBuilders(file,client,inlink);
			   
			}
		    catch(IOException e) {
		    	
		    	System.out.println("here");
		    	System.out.println(e);
		    }
		}
		
		
		}
	}

