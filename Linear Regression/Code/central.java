package IR.assn6;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.PorterStemmer;






public class central 
{
	public static HashMap<Integer,LinkedHashMap<String,Double>> okapi = new HashMap<Integer,LinkedHashMap<String,Double>>();
	public static HashMap<Integer,LinkedHashMap<String,Double>> bm25 = new HashMap<Integer,LinkedHashMap<String,Double>>();
	public static HashMap<Integer,LinkedHashMap<String,Double>> smoothing = new HashMap<Integer,LinkedHashMap<String,Double>>();
	public static HashMap<Integer,LinkedHashMap<String,Double>> tfidf = new HashMap<Integer,LinkedHashMap<String,Double>>();
	public static HashMap<Integer,LinkedHashMap<String,Double>> proximity = new HashMap<Integer,LinkedHashMap<String,Double>>();
	public static HashMap<Integer,LinkedHashMap<String,Double>> jelinek = new HashMap<Integer,LinkedHashMap<String,Double>>();

	public static HashMap<Integer,HashSet<Rank>> qrel = new HashMap<Integer,HashSet<Rank>>();
	public static HashMap<Integer,HashSet<Rank>> relevantQrel = new HashMap<Integer,HashSet<Rank>>();

	public static HashMap<Integer,HashMap<String,Features>> docSet = new HashMap<Integer,HashMap<String,Features>>();
	public static void main(String[] args) throws IOException
	{
		proximity.putAll(ProximitySearch.proximity());
		getJelinek("jelinek_output.txt");
		getData();

		readDocsFromQrel("qrels.adhoc.51-100.AP89.txt");
		createRelevantQrel();
		createFeatureMatrix();
		writeFeatures();
	}
	public static void getJelinek(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int qid = Integer.parseInt(temp[0].trim());
			String docid = temp[2].trim();
			double score = Double.parseDouble(temp[4].trim());
			if(jelinek.containsKey(qid))
			{
				LinkedHashMap<String,Double> t = new LinkedHashMap<String,Double>();
				t.putAll(jelinek.get(qid));
				t.put(docid,score);
				jelinek.put(qid, t);
			}
			else
			{
				LinkedHashMap<String,Double> t = new LinkedHashMap<String,Double>();
				t.put(docid,score);
				jelinek.put(qid, t);
			}
			line=br.readLine();
			System.out.println(jelinek.size());
		}
		br.close();
	}
	
	public static void getData() throws IOException
	{
		HashMap<String,Offset> catalogMap = new HashMap<String,Offset>();
		HashMap<Integer,String> docnoWithIndex = new HashMap<Integer,String>();
		HashMap<String,ArrayList<Tf_index>> final_hmap = new HashMap<String,ArrayList<Tf_index>>();
		ArrayList<String> stopwords = new ArrayList<String>();
		stopwords = stopWords.stop();
		catalogMap.putAll(createCatalog());		
		docnoWithIndex.putAll(docnoForId());
		long catalogSize = catalogMap.size();
		
		ArrayList<String> docnos = new ArrayList<String>();
		HashMap<String,Integer> len = new LinkedHashMap<String , Integer>();
		BufferedReader br1 = new BufferedReader(new FileReader("doc_Newlength.txt"));
		String data = br1.readLine();
		while(data!=null)
		{
			if(data.contains("docno"))
			{
				String[] r1 = data.split("\t");
				String[] r2 = r1[0].split("docno:");
				String doc = r2[1];
				docnos.add(doc.trim());
				String[] r3 = r1[1].split("length:");
				int length = (int) Double.parseDouble(r3[1].trim());
				len.put(doc.trim(), length);
				
			}
			data=br1.readLine();
		}
		br1.close();
		
		BufferedReader br = new BufferedReader(new FileReader("query_desc.51-100.short.txt"));
		String line = br.readLine();
	
		while(line!=null)
		{
			final_hmap = new HashMap<String,ArrayList<Tf_index>>();
			String ty;
			//line =line.replace(",", "").replace(".","").replace("(", "").replace(")", "");
			ArrayList<String> t = new ArrayList<String>();
			ArrayList<String> singleQueryTerms = new ArrayList<String>();
			// q_no is the query number
			int q_no = 0;
			String[] term = line.split(" ");
			if (term[0]!="")
			 q_no = Integer.parseInt(term[0]);
			System.out.println(q_no);
//			for(int j =0;j<term.length;j++)
//			{
//				System.out.println("Here"+term[j]);
//			}
			// adds all the query terms to the arrayList t, then removes blanks and stopwords
			for(int i =3;i<term.length;i++)
			{
				t.add(term[i].toLowerCase());
			}
			t.remove(" ");
			t.removeAll(stopwords);
			for(String terms : t)		
			{
					EnglishStemmer ps = new EnglishStemmer();
					ps.setCurrent(terms.toLowerCase());
					ps.stem();
					 ty = ps.getCurrent();
				singleQueryTerms.add(ty);	
			}
			final_hmap.putAll(getTfForQuery(singleQueryTerms,catalogMap,docnoWithIndex));
			for(Entry<String,ArrayList<Tf_index>> e: final_hmap.entrySet())
			{
				ArrayList<Tf_index> g = new ArrayList<Tf_index>();
				g = e.getValue();
				
				
			}
			
			okapi.put(q_no,okapiTF.matchingScore(q_no, len, final_hmap));
			tfidf.put(q_no, Tf_idf.tf_idf(q_no, len, final_hmap));
			bm25.put(q_no, OkapiBM25.matchingScore(q_no, len, final_hmap));
			smoothing.put(q_no, LaplaceSmoothing.matchingScore(q_no, len, final_hmap, catalogSize, docnos));
			//System.out.println(smoothing.size());
			//LaplaceJelinek.matchingScore(q_no, len, final_hmap, vocabSize, docnos, sumTf);
			line = br.readLine();
		}
		br.close();
		
	}
	
	public static void createFeatureMatrix()
	{
		for(Entry<Integer,HashSet<Rank>> e: relevantQrel.entrySet())
		{
			int qid = e.getKey();
			for(Rank r : e.getValue())
			{
				ArrayList<Double> list1 = new ArrayList<Double>(okapi.get(e.getKey()).values());
				ArrayList<Double> list2 = new ArrayList<Double>(tfidf.get(e.getKey()).values());
				ArrayList<Double> list3 = new ArrayList<Double>(bm25.get(e.getKey()).values());
				ArrayList<Double> list4 = new ArrayList<Double>(smoothing.get(e.getKey()).values());
				ArrayList<Double> list5 = new ArrayList<Double>(jelinek.get(e.getKey()).values());
				ArrayList<Double> list6 = new ArrayList<Double>(proximity.get(e.getKey()).values());
				
				double ok = list1.get(okapi.get(qid).size()-1);
				double tf = list2.get(tfidf.get(qid).size()-1);
				double bm = list3.get(bm25.get(qid).size()-1);
				double smooth = list4.get(smoothing.get(qid).size()-1);
				double jel = list5.get(jelinek.get(e.getKey()).size()-1);
				double pro = list6.get(proximity.get(e.getKey()).size()-1);
				
				
				if(okapi.get(qid).containsKey(r.docid))
					ok = okapi.get(qid).get(r.docid);
				if(tfidf.get(qid).containsKey(r.docid))
					tf = tfidf.get(qid).get(r.docid);
				if(bm25.get(qid).containsKey(r.docid))
					bm = bm25.get(qid).get(r.docid);
				if(smoothing.get(qid).containsKey(r.docid))
					smooth = smoothing.get(qid).get(r.docid);
				if(jelinek.get(e.getKey()).containsKey(r.docid))
					jel = jelinek.get(e.getKey()).get(r.docid);
				if(proximity.get(e.getKey()).containsKey(r.docid))
					pro = proximity.get(e.getKey()).get(r.docid);
				HashMap<String,Features> temp = new HashMap<String,Features>();
				temp.put(r.docid, new Features(r.label,ok,tf,bm,smooth,jel,pro));
				if(docSet.containsKey(qid))
				{
					temp.putAll(docSet.get(qid));
					docSet.put(qid, temp);
				}
				else
				{
					docSet.put(qid, temp);
				}
			}
		}
	}
	
	public static void createRelevantQrel() throws IOException
	{
		LinkedHashSet<Integer> querynos = new LinkedHashSet<Integer>();
		querynos.addAll(getQueryNumbers("query_desc.51-100.short.txt"));
		for(Entry<Integer,HashSet<Rank>> e: qrel.entrySet())
		{
			if(querynos.contains(e.getKey()))
				relevantQrel.put(e.getKey(),e.getValue());
		}
		System.out.println("Number of documents from Qrel: " + relevantQrel.size());
		int sum=0;
		for(Entry<Integer, HashSet<Rank>> e : relevantQrel.entrySet() )
		{
			System.out.println(e.getKey()+":"+e.getValue().size());
			sum+=e.getValue().size();
		}
		System.out.println("sum:"+sum);
	}
	
	public static LinkedHashSet<Integer> getQueryNumbers(String filename) throws IOException
	{
		LinkedHashSet<Integer> querynos = new LinkedHashSet<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int qno = Integer.parseInt(temp[0].trim());
			querynos.add(qno);
			line=br.readLine();
		}
//		System.out.println(querynos.size());
//		for(int i : querynos)
//		{
//			System.out.println(i);
//		}
		br.close();
		return querynos;
	}
	public static void writeFeatures() throws FileNotFoundException
	{
		System.out.println("writing features to file");
		PrintWriter pw = new PrintWriter("features.txt");
		for(Entry<Integer,HashMap<String,Features>> e : docSet.entrySet())
		{
			for(Entry<String,Features> e1 : e.getValue().entrySet())
			{
				pw.println(e.getKey()+" "+e1.getKey()+" "+e1.getValue().okapitf+" "+e1.getValue().tfidf+" "+e1.getValue().bm25+" "+e1.getValue().laplaceSmoothing+" "+e1.getValue().jelinek+" "+e1.getValue().proximity+" "+e1.getValue().label);
			}
		}
		pw.close();
		System.out.println("done writing to file");
		
	}
	
	public static void readDocsFromQrel(String filename) throws IOException
	{
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			Integer qid = Integer.parseInt(temp[0].trim());
			String docid = temp[2].trim();
			int label = Integer.parseInt(temp[3].trim());
			HashSet<Rank> t = new HashSet<Rank>();
			t.add(new Rank(docid,label));
			if(qrel.containsKey(qid))
			{
				t.addAll(qrel.get(qid));
				qrel.put(qid, t);
			}
			else
			{
				qrel.put(qid,t);
			}
			
			line = br.readLine();
		}
		br.close();
//		System.out.println("Number of documents from Qrel: " + qrel.size());
//		int sum=0;
//		for(Entry<Integer, HashSet<Rank>> e : qrel.entrySet() )
//		{
//			System.out.println(e.getKey()+":"+e.getValue().size());
//			sum+=e.getValue().size();
//		}
//		System.out.println("sum:"+sum);
	}
	
	
	public static HashMap<String,Offset> createCatalog() throws IOException
	{
		HashMap<String,Offset> catalogMap = new HashMap<String,Offset>();
		BufferedReader br = new BufferedReader(new FileReader("mcatalog84.txt"));
		try 
		{
			String line = br.readLine();
			while(line!=null)
			{
				String[] temp = line.split(" ");
				String term = temp[0];
				long start = Long.parseLong(temp[1].trim());
				long end = Long.parseLong(temp[2].trim());
				Offset o = new Offset(start, end);
				catalogMap.put(term, o);
				line=br.readLine();
			}
		} 
		
		catch (IOException e) {}
		br.close();
//		for (Entry<String,Offset> e : catalogMap.entrySet())
//		{
//			
//			Offset p = e.getValue();
//			System.out.println(e.getKey()+" "+p.start+" "+p.end);
//			
//		}
		return catalogMap;
	}
	
	public static HashMap<String,ArrayList<Tf_index>> getTfForQuery(ArrayList<String>singleQueryTerms,HashMap<String,Offset> catalogMap, HashMap<Integer,String> docnoWithIndex) throws IOException
	{
		HashMap<String,ArrayList<Tf_index>> final_hmap = new HashMap<String,ArrayList<Tf_index>>();
		for(String term:singleQueryTerms)
		{
			ArrayList<Tf_index> docWithTf = new ArrayList<Tf_index>();
			if(catalogMap.containsKey(term))
			{
//				System.out.println("Term:"+term);
				Offset o = catalogMap.get(term);
				long start = o.start;
				long end = o.end;
				String text = (new String(readFromFile("mergeList84.txt" , (int) start, (int) (end-start))));
				//System.out.println(text);
				String[] temp1 = text.split(",");
				String t = temp1[0];
//				System.out.println("df:"+temp1.length);
				for(int i=1;i<temp1.length;i++)
				{
					String[] temp2 = temp1[i].split("/");
					int docid = Integer.parseInt(temp2[0].trim());
					String docno = docnoWithIndex.get(docid);
					String[] temp3 = temp2[1].split("-");
					int tf = temp3.length;
					Tf_index temp = new Tf_index(docno, tf);
					docWithTf.add(temp);
					final_hmap.put(term, docWithTf);
	//				System.out.println(docid);
	//				System.out.println(docno);
	//				System.out.println(tf);
				}
			}
		
		}
		return final_hmap;
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
	
	public static HashMap<Integer,String> docnoForId() throws IOException
	{
		HashMap<Integer,String> docnoWithIndex = new HashMap<Integer,String>();
		BufferedReader br = new BufferedReader(new FileReader("docWithIndex.txt"));
		try 
		{
			String line = br.readLine();
			String docno = null;
			int index = 0;
			while(line!=null)
			{
				String[] temp = line.split("  ");
				
					
					
						  docno = temp[0].replace("docno: ","").trim();
					
						 index = Integer.parseInt(temp[1].replace("id:", "").trim());
					//System.out.println(index+" "+docno);
					docnoWithIndex.put(index, docno);
					
				
				line=br.readLine();
			}
		} 
		
		catch (IOException e) {}
		br.close();
		return docnoWithIndex;
		
	}
}
