package IR.assn1;




public class Tf_index 
{
	public String docid;
	public int freq;
	public Tf_index(String id , int frequency)
	{
		docid = id;
		freq = frequency;
	}
	
}


//else
//	{
//	JSONObject terms =  (JSONObject) text.get("terms");
//
//String[] term = terms.toString().split("[{}],"); 
//ArrayList<String[]> test = new ArrayList();
//ArrayList<String> test1 = new ArrayList();
//ArrayList<String> test2 = new ArrayList();
//ArrayList<String[]> test3 = new ArrayList();
//ArrayList<String> finalterms = new ArrayList();
//ArrayList<String> freq = new ArrayList();
//for(int i=0;i<term.length;i++)
//{
//String[] t = term[i].split(",\"tokens\":");
//test.add(t);	
//}
//for(String[] si : test)
//{
//	test1.add(si[0]);	
//}
//
//for(String sig : test1)
//{
//	if(sig.contains("end_offset"))
//	{
//		continue;
//	}
//	test2.add(sig);
//
//}
//for(String sig : test2)
//{
//	String[]y = sig.split(":.*\"term_freq\":");
//	test3.add(y);
//}
//    
//for(String[] sig : test3)
//{
//	for(int i=0;i<sig.length;i+=2)
//	{
//		String[]x = sig[i].split("\"");
//		finalterms.add(x[1]);
//	}
//	for(int i=1;i<sig.length;i+=2)
//		freq.add(sig[i]);
//	
//	
//}
//
//for(String sig : finalterms)
//{
//	//System.out.println("here");
//	//System.out.println(sig);
//}
//for(String sig : freq)
//{
//	//System.out.println("here");
//	//System.out.println(sig);
//}		
//for(int i =0;i<finalterms.size();i++)
//{
//	String fr = freq.get(i);
//	int f= Integer.parseInt(fr);
//	hmap.put(finalterms.get(i), f);
//}
//}
//} }
//catch (IOException e){}
//final_hmap.put(id,hmap);
//
//}






//package InfoRet.hw1;
//import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map.Entry;
//
//import org.elasticsearch.ElasticsearchException;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.termvectors.TermVectorsResponse;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.common.xcontent.ToXContent;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//
//public class retrieve 
//{
//	public static void main(String[] args) throws UnknownHostException,ElasticsearchException, ParseException
//	{
//		try{
//			HashMap<String,ArrayList<Tf_index>> final_hmap= new HashMap<String,ArrayList<Tf_index>>();
//			ArrayList<ArrayList<String>> query_terms = queries.query();
//			
//			Settings settings = Settings.builder().build();
//			Client client = TransportClient.builder().settings(settings).build()
//					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
//	//	
//			QueryBuilder qb = matchQuery("text","algorithm");
//			SearchResponse response = client.prepareSearch()
//					.setQuery(qb)
//					.execute()
//					.actionGet();
//			
//			ArrayList<String> t = new ArrayList<String>();
//			ArrayList<Integer> docid = new ArrayList<Integer>();
//			String resp1 = response.toString();
//			String[] t1 = resp1.split("_id") ;
//			for(int i=1;i<t1.length;i++)
//			{
//				String[] temp = t1[i].split("_score");
//				String h = temp[0].replace("\"", "").replace(",", "").replace(" ", "").replace(":","").replace("\n","");
//				t.add(h);
//			}
//			for(String a : t)
//			{
//				int temp = Integer.parseInt(a.toString());
//				docid.add(temp);
//			}
//			
//		//ArrayList s = stopWords.stop();
//		
//		
//
//for(Integer id : docid)
//{
//	HashMap<String, Integer> hmap = new HashMap<String, Integer>();
//	String num= Integer.toString(id);
//			TermVectorsResponse resp = client.prepareTermVectors().setIndex("ap_dataset2")
//                  .setType("document").setId(num).execute().actionGet();
//			XContentBuilder builder;
//			//System.out.println(resp.toString());
//			try {
//				JSONParser jsonParser = new JSONParser();
//
//				
//				builder = XContentFactory.jsonBuilder().startObject();
//				resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
//				builder.endObject();
//				System.out.println(builder.prettyPrint().string());
//				
//				JSONObject jsonObject = (JSONObject) jsonParser.parse(builder.string());
//				JSONObject term_vectors=  (JSONObject) jsonObject.get("term_vectors");
//				if(term_vectors.get("text")!=null)
//				{
//				JSONObject text =  (JSONObject) term_vectors.get("text");
//				if(text.get("terms")!=null)
//				{
//				
//				JSONObject terms =  (JSONObject) text.get("terms");
//			
//			String[] term = terms.toString().split("[{}],"); 
//			ArrayList<String[]> test = new ArrayList();
//			ArrayList<String> test1 = new ArrayList();
//			ArrayList<String> test2 = new ArrayList();
//			ArrayList<String[]> test3 = new ArrayList();
//			ArrayList<String> finalterms = new ArrayList();
//			ArrayList<String> freq = new ArrayList();
//			for(int i=0;i<term.length;i++)
//			{
//			String[] t11 = term[i].split(",\"tokens\":");
//			test.add(t11);	
//			}
//			for(String[] si : test)
//			{
//				test1.add(si[0]);	
//			}
//			
//			for(String sig : test1)
//			{
//				if(sig.contains("end_offset"))
//				{
//					continue;
//				}
//				test2.add(sig);
//	
//			}
//			for(String sig : test2)
//			{
//				String[]y = sig.split(":.*\"term_freq\":");
//				test3.add(y);
//			}
//			    
//			//HashMap<String,Integer> fr = new LinkedHashMap<String,Integer>();
//			for(String[] sig : test3)
//			{
//				
//				for(int i=0;i<sig.length;i+=2)
//				{
//					String[]x = sig[i].split("\"");
//					int f = Integer.parseInt(sig[i+1]);
//					Tf_index ind = new Tf_index(num, f);
//					
//					ArrayList<Tf_index> res = new ArrayList<Tf_index>();
//					res.add(ind);
//					
//					if(final_hmap.get(x[1])== null)
//					{
//						final_hmap.put(x[1], res);
//					}
//					else
//					{
//						ArrayList<Tf_index> temp = final_hmap.get(x[1]);
//						
//						temp.addAll(res);
//						final_hmap.put(x[1], temp);
//					}
//			}
//				
//			}
//			
//				for(Entry<String,ArrayList<Tf_index>> e : final_hmap.entrySet())
//				{
//					System.out.println("key:"+ e.getKey());
//					ArrayList<Tf_index> r = e.getValue();
//					for(Tf_index se : r)
//					{
//						System.out.print("docid:" + se.docid + "freq: " + se.freq);
//					}
//					System.out.println("");
//					
//					
//				}
//				PrintWriter writer = new PrintWriter("term_freq.txt", "UTF-8");
//				for(Entry<String,ArrayList<Tf_index>> e : final_hmap.entrySet())
//				{
//					writer.println("term:"+e.getKey());
//					ArrayList<Tf_index> r = e.getValue();
//					for(Tf_index se : r)
//					{
//						writer.println("\tdocid: " + se.docid + "\tfreq: " + se.freq);
//					}
//				}
//				writer.close();
//				}
//			} 
//			}
//
//			catch (IOException e){}
//		}
//		}
//		catch(IOException e) {}
//		catch(ElasticsearchException e) {
//			System.out.println(e);
//		
//}
//	
//}
//}
//
