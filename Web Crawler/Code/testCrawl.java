package IR.assn3;





import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URL;



public class testCrawl {
	// Cache of robot disallow lists.
    private static HashMap disallowListCache = new HashMap(); 
	public static int count = 0;
	public static int fileCount = 0;
	
	public static void main(String[] args) throws InterruptedException, IOException 
	{
		long starttime = System.currentTimeMillis();
		HashMap<String, Long> politenessMap = new HashMap<String,Long>();
		HashMap<String, inlink_time> frontier = new LinkedHashMap<String,inlink_time>();
		ArrayList<corpus_type> corpus = new ArrayList<corpus_type>();
		HashSet<String> visitedLinks = new HashSet<String>();
		HashSet<String> writtenToCorpus = new HashSet<String>();
		HashSet<String> outlinks = new HashSet<String>();
		HashMap<String,HashSet<String>> inlinks = new HashMap<String,HashSet<String>>();
		HashMap<String,Integer> depth = new HashMap<String,Integer>();
		
		
		
		inlink_time q1 = new inlink_time("http://en.wikipedia.org/wiki/List_of_terrorist_incidents",0,System.currentTimeMillis());
		String first = canonicalization(q1.url);
		frontier.put(first, q1);
		depth.put(first, 0);
		inlinks.put(first, new HashSet<String>());
		
		inlink_time q2 = new inlink_time("http://en.wikipedia.org/wiki/7_July_2005_London_bombings",0,System.currentTimeMillis());
		String second = canonicalization(q2.url);
		frontier.put(second, q2);
		depth.put(second, 0);
		inlinks.put(second,new HashSet<String>());
		
		inlink_time q3 = new inlink_time("http://en.wikipedia.org/wiki/List_of_terrorist_incidents_in_London",0,System.currentTimeMillis());
		String third = canonicalization(q3.url);
		frontier.put(third, q3);
		depth.put(third, 0);
		inlinks.put(third, new HashSet<String>());
		
		inlink_time q4 = new inlink_time("https://en.wikipedia.org/wiki/Boston_Marathon_bombing",0,System.currentTimeMillis());
		String fourth = canonicalization(q4.url);
		frontier.put(fourth, q4);
		depth.put(fourth, 0);
		inlinks.put(fourth, new HashSet<String>());
		
		int flag = 0 ;
	
		//Iterator<Entry<String,inlink_time>> r = frontier.entrySet().iterator();
		
		while(writtenToCorpus.size()<20000 && frontier.size()!=0)
		{
			
			String canonLink = (String) frontier.keySet().toArray()[0];
			System.out.println(canonLink);
			//continue;
			
			String link = frontier.remove(canonLink).url;
			//String link = null;
			long lastAccess = System.currentTimeMillis();
			URL seed = new URL(link);
					
			String domain = seed.getHost();
			if(politenessMap.containsKey(domain))
			{
				long timeAlreadySlept = lastAccess-politenessMap.get(domain);
				if((timeAlreadySlept)<1000)
					Thread.sleep(1000 - timeAlreadySlept);
				
			}
				
			System.out.println(link);
			Document doc = null;
			try 
			{
				doc = Jsoup.connect(link).header("Accept-Language","en").get();
//				
//				
			} 
			catch (IOException e1) 
			{
				//	frontier.remove(canonLink);
				e1.printStackTrace();
				continue;
			}							
			politenessMap.put(domain,System.currentTimeMillis());
			
			String title = doc.title();
			String text = doc.text();
			String src = doc.html();
			String header = doc.head().toString();
			Elements href = doc.select("a[href]");
				
			//HashSet<String> l = new HashSet<String>();
			if(!writtenToCorpus.contains(canonLink))
			{
				for(Element e1 : href)
				{
					if(!skip(e1.attr("abs:href")))
					{
						String d = canonicalization(e1.attr("abs:href"));
						if(d!=null && !(inlinks.containsKey(d)))
						{
							HashSet<String> t1 = new HashSet<String>();
							t1.add(canonicalization(link));
							inlinks.put(d, t1);
						}
						if(d!=null && (inlinks.containsKey(d)))
						{
							HashSet<String> t1 = new HashSet<String>();
							t1.addAll(inlinks.get(d));
							t1.add(canonicalization(link));
							inlinks.put(d, t1);
						}
							
						outlinks.add(d);
							
					}			
				}	
				
				corpus_type temp = new corpus_type(canonLink,title,text,link,outlinks,src,header);
				corpus.add(temp);
				outlinks = new HashSet<String>();
				writtenToCorpus.add(canonLink);
			}
			visitedLinks.add(canonLink);
			//	out = new ArrayList<String>();
			System.out.println("href size " + href.size());
				
				if(frontier.size()<20000 && flag ==0 )
				{
					if(count%10==0 && count>0)
					{
						System.out.println("SORTING");
					LinkedHashMap<String,inlink_time> temporary = new LinkedHashMap<String,inlink_time>();
					
						temporary.putAll(sortByInlink(frontier));
						frontier = new LinkedHashMap<String,inlink_time>();
						frontier.putAll(temporary);
					}
					for( Element e: href)
					{
						Document d = null;
						URL ur = new URL(e.attr("abs:href"));
						String url = canonicalization(e.attr("abs:href"));
						if(!(depth.containsKey(url)))
						{
							depth.put(url,depth.get(canonLink)+1);
						}
						if(url!=null && frontier.containsKey(url))
						{
							inlink_time obj = frontier.get(url);				
							inlink_time s = new inlink_time(e.attr("abs:href"),obj.inlink+1,obj.time);
							frontier.put(url,s);
						}
						if(url!=null && !(frontier.containsKey(url)) && !(visitedLinks.contains(url)))
						{
							if(!skip(e.attr("abs:href")))
							{
								if(robotSafe(ur))
								{
										String d1 = ur.getHost();
										if(politenessMap.containsKey(d1))
										{
											long timeAlreadySlept = lastAccess-politenessMap.get(d1);
											if((timeAlreadySlept)<1000)
												Thread.sleep(1000 - timeAlreadySlept);
												
										}
										politenessMap.put(d1,lastAccess);
										//System.out.println(href.indexOf(e) +" url: "+ur);
									try
									{
										doc = Jsoup.connect(e.attr("abs:href")).header("Accept-Language","en").get();
//										if(e.attr("abs:href").contains("wikipedia")){
//											//languages
//											doc.getElementById("p-lang").remove();
//											//category links
//											doc.getElementById("catlinks").remove();
//											//main page
//											doc.getElementById("p-logo").remove();
//											doc.getElementById("p-interaction").remove();
//											doc.getElementById("p-tb").remove();
//											doc.getElementById("p-coll-print_export").remove();
//										//	doc.getElementById("p-wikibase-otherprojects").remove();
//											doc.getElementById("toc").remove();
//											doc.getElementById("footer").remove();
//										}
									} 
									catch (IOException e1)
									{
										e1.printStackTrace();
										continue;
										
									}
									
									String t = doc.text();
									if(t.contains("terror")||t.contains("shoot")||t.contains("explosion")||t.contains("bomb")||
											t.contains("assassin")||t.contains("death")||t.contains("massacre")||t.contains("dead")||t.contains("violence"))
									{
				//						
										
//										if(doc.select("html").attr("lang") == "en")
//										{
											//System.out.println(count + " frontier : " + frontier.size() + " timeelapsed: " + ( System.currentTimeMillis()- starttime )/1000);
												
											if( frontier.size()<20000)
											{
												String ti = doc.title();
												String hd = doc.head().toString();
												String sr = doc.html();
												Elements hre = doc.select("a[href]");
												for(Element e1 : hre)
												{
													if(!skip(e1.attr("abs:href")))
													{
														String d2 = canonicalization(e1.attr("abs:href"));
														if(d2!=null && !(inlinks.containsKey(d2)))
														{
															HashSet<String> t1 = new HashSet<String>();
															t1.add(canonicalization(link));
															inlinks.put(d2, t1);
														}
														if(d2!=null && (inlinks.containsKey(d2)))
														{
															HashSet<String> t1 = new HashSet<String>();
															t1.addAll(inlinks.get(d2));
															t1.add(canonicalization(link));
															inlinks.put(d2, t1);
														}
															
														outlinks.add(d2);
															
													}			
												}	
												
												corpus_type temp1 = new corpus_type(url,ti,t,e.attr("abs:href"),outlinks,sr,hd);
												corpus.add(temp1);
												writtenToCorpus.add(url);
												outlinks = new HashSet<String>();
												System.out.println(href.indexOf(e) +" url: "+url);	
													inlink_time s = new inlink_time(e.attr("abs:href"),1,System.currentTimeMillis());
													frontier.put(url,s);
													System.out.println(count + " frontier : " + frontier.size() +" visitedLinks : " + visitedLinks.size()+ " writtenToCorpus : " + writtenToCorpus.size()+" timeelapsed: " + ( System.currentTimeMillis()- starttime )/1000);
													if(corpus.size()==100)
													{
														printCorpus(corpus,depth);
														corpus = new ArrayList<corpus_type>();				
													}
												}
													
									//	}	
										
									}
									
								}
							}
						}
					}
					if(frontier.size()>=20000)
						flag=1;
				}
				
				count++;
			
				
				//System.out.println("REMOVED:"+link);
				//frontier.remove(canonLink);
//				for(Entry<String, inlink_time>  j : frontier.entrySet())
//				{
//					System.out.println(j.getKey());
//				}
				//System.exit(0);
				
				
					
					
				
			
		}
		if(corpus.size()>0)
		{
			printCorpus(corpus,depth);
		}
		printInlinks(inlinks);
		
	}
	
	
	public static boolean skip(String ur)
	{
		String[] ads = {"facebook","books.google","instagram","twitter","shop","wikimedia","ads","foursquare","mediawiki","aenetworks","contact_us",
				"license","plus.google.com","fyi.tv","email","support","emails","wiki/special:","portal:featured_content","portal:current_events",
				"special:random","help:contents","wikipedia:about","wikipedia:community_portal","special:recentchanges","wikipedia:file_upload_wizard",
				"special","wikipedia:general_disclaimer","en.m.","action=edit","help:category","international_standard_book_number",".pdf",
				"file:","youtube","\\.tv","mylifetime","intellectualproperty","integrated_authority","citation"};

		String url = ur.toLowerCase();
		for(int i=0;i<ads.length;i++)
		{
			if(url.contains(ads[i]))
				return true;
		}
			
		return false;
	}
	
	public static HashMap<String,inlink_time> sortByInlink(HashMap<String,inlink_time> frontier)
	{
		
		 List<HashMap.Entry<String,inlink_time>> list =
		            new LinkedList<HashMap.Entry<String, inlink_time>>( frontier.entrySet() );
		        Collections.sort( list, new Comparator<Map.Entry<String,inlink_time>>()
		        {
		            public int compare( Map.Entry<String, inlink_time> o1, Map.Entry<String, inlink_time> o2 )
		            {
		                return Integer.compare(o2.getValue().inlink, o1.getValue().inlink);
		            }
		        } );

		        HashMap<String, inlink_time> result = new LinkedHashMap<String, inlink_time>();
		        for (Map.Entry<String, inlink_time> entry : list)
		        {
		            result.put( entry.getKey(), entry.getValue() );
		        }
//		        
//		        for(Entry<String,inlink_time> e : result.entrySet())
//		        {
//		        	System.out.println(e.getKey());
//		        }
		        return result;
		//return null;
		
	}
	
	
	
	public static void printInlinks(HashMap<String,HashSet<String>> inlinks) throws IOException
	{
		HashMap<String,Offset> catalog = new HashMap<String,Offset>();
		long offset = 0;
		 String fileName = "./inlinks/inlinks"+count+".txt";
		 RandomAccessFile outputFile = new RandomAccessFile(fileName, "rw");
		 for(Entry<String,HashSet<String>> e : inlinks.entrySet())
		 {
			 long start = offset;
			StringBuilder str = new StringBuilder();
			str.append(e.getKey());
			str.append("\t");
			if(e.getValue()!=null)
			{	
				for(String s : e.getValue())
				{
					str.append(s);
					str.append("\t");
				}
			}
			str.append("\r\n");
			
			
			outputFile.seek(start);
			outputFile.writeBytes(str.toString());
			offset = outputFile.getFilePointer();
			long end = offset;
			catalog.put(e.getKey(), new Offset(start,end));
		}
			
			System.out.println("written to INLINKS");
			outputFile.close();	
			String catalogName = "./in_catalog/in_catalog"+count+".txt";
			PrintWriter writer = new PrintWriter(catalogName, "UTF-8");
			for (Entry<String,Offset> e1 : catalog.entrySet())
			{					
				writer.println(e1.getKey()+" "+e1.getValue().start+" "+ e1.getValue().end);					
			}
			writer.close();
		 
	}
	
	
	//REFERENCE: www.java-tips.org
	private static boolean robotSafe(URL urlToCheck) {
	    String host = urlToCheck.getHost().toLowerCase();
	     
	    // Retrieve host's disallow list from cache.
	    ArrayList disallowList =
	            (ArrayList) disallowListCache.get(host);
	     
	    // If list is not in the cache, download and cache it.
	    if (disallowList == null) {
	        disallowList = new ArrayList();
	         
	        try {
	            URL robotsFileUrl =
	                    new URL("http://" + host + "/robots.txt");
	             
	            // Open connection to robot file URL for reading.
	            BufferedReader reader =
	                    new BufferedReader(new InputStreamReader(
	                    robotsFileUrl.openStream()));
	             
	            // Read robot file, creating list of disallowed paths.
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.indexOf("Disallow:") == 0) {
	                    String disallowPath =
	                            line.substring("Disallow:".length());
	                     
	                    // Check disallow path for comments and 
	                    // remove if present.
	                    int commentIndex = disallowPath.indexOf("#");
	                    if (commentIndex != - 1) {
	                        disallowPath =
	                                disallowPath.substring(0, commentIndex);
	                    }
	                     
	                    // Remove leading or trailing spaces from 
	                    // disallow path.
	                    disallowPath = disallowPath.trim();
	                     
	                    // Add disallow path to list.
	                    disallowList.add(disallowPath);
	                }
	            }
	             
	            // Add new disallow list to cache.
	            disallowListCache.put(host, disallowList);
	        } catch (Exception e) {
	    /* Assume robot is allowed since an exception
	       is thrown if the robot file doesn't exist. */
	            return true;
	        }
	    }
	    
	  /* Loop through disallow list to see if the
	     crawling is allowed for the given URL. */
	      String file = urlToCheck.getFile();
	      for (int i = 0; i < disallowList.size(); i++) {
	          String disallow = (String) disallowList.get(i);
	          if (file.startsWith(disallow)) {
	              return false;
	          }
	      }
	       
	      return true;
	  }
	
	
	
	public static void printCorpus(ArrayList<corpus_type> corpus, HashMap<String,Integer> depth) throws IOException
	{
		HashMap<String,Offset> catalog = new HashMap<String,Offset>();
		fileCount++;
		long offset = 0;
		 String fileName = "./corpus/text"+fileCount+".txt";
		 RandomAccessFile outputFile = new RandomAccessFile(fileName, "rw");
	//	 String first = null;	
		for (corpus_type c : corpus)
			{
				long start = offset;
				StringBuilder str = new StringBuilder();	
					str.append("<DOC>");
					str.append("\r\n");
					
					str.append("<DOCNO>");
					str.append(c.docno);
					str.append("</DOCNO>");
					str.append("\r\n");
					
					str.append("<HEAD>");
					str.append(c.head);
					str.append("</HEAD>");
					str.append("\r\n");
					
					str.append("<DEPTH>");
					str.append(depth.get(c.docno));
					str.append("</DEPTH>");
					str.append("\r\n");	
					
					str.append("<AUTHOR>");
					str.append("Sanjana");
					str.append("</AUTHOR>");
					str.append("\r\n");
					
					str.append("<URL>");
					str.append(c.url);
					str.append("</URL>");
					str.append("\r\n");
					
					str.append("<HTTPHEADER>");
					str.append(c.HTTPheader);
					str.append("</HTTPHEADER>");
					str.append("\r\n");
					
					str.append("<SOURCE>");
					str.append(c.source);
					str.append("</SOURCE>");
					str.append("\r\n");
					
					str.append("<OUTLINKS>");
					str.append("\r\n");
					for(String s :c.outlinks)
					{
						str.append(s);
						str.append("\t");
					}
					str.append("\r\n");
					str.append("</OUTLINKS>");
					str.append("\r\n");
					
					str.append("<TEXT>");
					str.append("\r\n"); 
					str.append(c.text);
					str.append("\r\n");
					str.append("</TEXT>");
					str.append("\r\n");
					
					str.append("</DOC>");
					str.append("\r\n");
				outputFile.seek(start);
				outputFile.writeBytes(str.toString());
				offset = outputFile.getFilePointer();
				long end = offset;
				catalog.put(c.docno, new Offset(start,end));
			}
			
			System.out.println("written to output");
			outputFile.close();	
			
	}
	
	
	public static String canonicalization(String ur) throws MalformedURLException
	{
		//System.out.println("canon:"+url);
		if(ur!="")
		{
			URL u = new URL(ur);
			String protocol = u.getProtocol();
			String domain = u.getHost();
			String path = u.getPath();
			String url = protocol+"://"+domain+path;
			if(url.contains("#"))
			{
				url = url.substring(0,url.indexOf("#"));
			}
			if(url.substring(8).contains("//"))
			{
				url = url.substring(8).replace("//","/");
			}
			if((url.contains("https"))&&(url.contains(":443")))
			{
				url = url.substring(0,url.indexOf(":443"));
			}
			if((url.contains("http"))&&(url.contains(":80")))
			{
				url = url.substring(0,url.indexOf(":80"));
			}
			return url;
		}
//		System.out.println("trimmed:"+url);
		else
			return null;
	}
	
	
}
