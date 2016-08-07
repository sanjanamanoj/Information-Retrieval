package IR.assn6;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
	import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
	import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
	import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
	import java.util.Map.Entry;
import java.util.TreeMap;

import org.tartarus.snowball.ext.EnglishStemmer;
	import org.tartarus.snowball.ext.PorterStemmer;








	public class ProximitySearch 
	{	

		public static HashMap<Integer,LinkedHashMap<String,Double>> proximity() throws IOException
		{
			HashMap<Integer,LinkedHashMap<String,Double>> prox = new HashMap<Integer,LinkedHashMap<String,Double>>();
			HashMap<String,Offset> catalogMap = new HashMap<String,Offset>();
			HashMap<Integer,String> docnoWithIndex = new HashMap<Integer,String>();
			HashMap<String,ArrayList<Prox_pos>> final_hmap = new HashMap<String,ArrayList<Prox_pos>>();
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
					docnos.add(doc);
					String[] r3 = r1[1].split("length:");
					int length = (int) Double.parseDouble(r3[1]);
					len.put(doc, length);
					
				}
				data=br1.readLine();
			}
			br1.close();
			
			BufferedReader br = new BufferedReader(new FileReader("query.txt"));
			String line = br.readLine();
		
			while(line!=null)
			{
				HashMap<String,Integer> minSp = new HashMap<String,Integer>();
				final_hmap = new HashMap<String,ArrayList<Prox_pos>>();
				String ty;
				//line =line.replace(",", "").replace(".","").replace("(", "").replace(")", "");
				ArrayList<String> t = new ArrayList<String>();
				ArrayList<String> singleQueryTerms = new ArrayList<String>();
				// q_no is the query number
				int q_no = 0;
				String[] term = line.split(" ");
				if (term[0]!="")
				 q_no = Integer.parseInt(term[0]);
//				System.out.println(q_no);
//				for(int j =0;j<term.length;j++)
//				{
//					System.out.println("Here"+term[j]);
//				}
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
//				for(Entry<String,ArrayList<Prox_pos>> e: final_hmap.entrySet())
//				{
//					System.out.println("docid:"+e.getKey());
//					ArrayList<Prox_pos> d = e.getValue();
//					for(Prox_pos s : d)
//					{
//						System.out.println("term:"+s.term);
//						ArrayList<Integer> f = s.pos;
//						for(Integer r : f)
//						{
//							System.out.print("pos:"+r+" ");
//						}
//						System.out.println();
//					}
//					
//					
//					
//				}
				minSp.putAll(minSpan(final_hmap));
				prox.put(q_no,scoringFunction(q_no,minSp,catalogSize,len,final_hmap));
				line = br.readLine();
			}
			System.out.println("returning prox");
			return prox;
			
		}
		public static LinkedHashMap<String,Double> scoringFunction(int qno ,HashMap<String, Integer>minSpanMap, long vocab, HashMap<String,Integer> len,HashMap<String,ArrayList<Prox_pos>>final_hmap) throws IOException
		{
			HashMap<String,Double> score = new HashMap<String,Double>();
			double c = 1500;
			for(Entry<String,Integer> e : minSpanMap.entrySet())
			{
				String docno = e.getKey();
				int minRange = e.getValue();
				double firstTerm = (c - minRange);
				double numOfTerms = final_hmap.get(docno).size();
				double length = len.get(" "+docno+" ");
				double denom = length + vocab;
				double secondTerm = numOfTerms/denom;
				double res = firstTerm * secondTerm;
				if(score.containsKey(docno))
				{
					double temp = score.get(docno);
					temp+=res;
					score.put(docno,temp);
				}
				else
				{
					score.put(docno, res);
				}
				
			}
			int rank = 1;
			LinkedHashMap<String, Double> output= ranking(score);
			return output;
//			String fileName = "proximitySearch_output.txt";
//			File file =new File( fileName);
//			if(!file.exists())
//			{
//	    	 	file.createNewFile();
//	    	 	
//			}
//			FileWriter fw = new FileWriter(file,true);
//			BufferedWriter bw = new BufferedWriter(fw);
//			PrintWriter pw = new PrintWriter(bw);
//			for(Entry<String,Double> e1 : output.entrySet())
//			{
//				
//				pw.println(qno + " Q0 " + e1.getKey() + " " + rank + " " + e1.getValue() + " " + "Exp");
//				rank++;
//			}
//	    	 	
//			pw.close();
//			System.out.println(qno + "written to output");	
			
		}
		public static HashMap<String, Integer> minSpan(HashMap<String,ArrayList<Prox_pos>> final_hmap)
		{
			HashMap<String, Integer> minSpanMap = new HashMap<String,Integer>();
			for(Entry<String,ArrayList<Prox_pos>> e : final_hmap.entrySet())
			{
				String docno = e.getKey();
				if(e.getValue().size()==1)
				{
					minSpanMap.put(docno,1);
					continue;
				}
				int minSpan = Integer.MAX_VALUE;
				int size = 0;
				ArrayList<Prox_pos> t = e.getValue();
				for(Prox_pos comp : t)
				{
					if(comp.pos.size() > size)
						size = comp.pos.size();
				}
				int maxSize = size * t.size();
			//	System.out.println(maxSize);
				
				for(int i=0;i<maxSize;i++)
				{
					//System.out.println(i);
					HashMap<Integer,Integer > temp = new HashMap<Integer,Integer>();
					for(int k=0;k< t.size();k++)
					{
						int pos = t.get(k).pos.get(0);
						temp.put(pos, k);
					}
					Map<Integer, Integer> map = new TreeMap<Integer, Integer>(temp);
					 Map.Entry<Integer,Integer> entry=map.entrySet().iterator().next();
					 int first= entry.getKey();
					// int minList = entry.getValue();
					
						 for(Entry<Integer,Integer> e1 : map.entrySet())
						 {
							 if(t.get(e1.getValue()).pos.size()>1)
							 {
								 t.get(e1.getValue()).pos.remove(0);
								 break;
							 }
								 
						 }
					 
					 System.out.println(first);
					// System.out.println(minList);
					 Entry<Integer, Integer> lastEntry = ((TreeMap<Integer, Integer>) map).lastEntry();
					 int last = lastEntry.getKey();
					 System.out.println(last);
					 if((last-first)< minSpan)
						 minSpan = (last - first);
					 System.out.println("span:"+minSpan);
				}
				minSpanMap.put(docno,minSpan);
			}
			return minSpanMap;
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
			return catalogMap;
		}
		
		public static HashMap<String,ArrayList<Prox_pos>> getTfForQuery(ArrayList<String>singleQueryTerms,HashMap<String,Offset> catalogMap, HashMap<Integer,String> docnoWithIndex) throws IOException
		{
			HashMap<String,ArrayList<Prox_pos>> final_hmap = new HashMap<String,ArrayList<Prox_pos>>();
			for(String term:singleQueryTerms)
			{
				ArrayList<Prox_pos> docWithTf = new ArrayList<Prox_pos>();
				if(catalogMap.containsKey(term))
				{
//					System.out.println("Term:"+term);
					Offset o = catalogMap.get(term);
					long start = o.start;
					long end = o.end;
					String text = (new String(readFromFile("mergeList84.txt" , (int) start, (int) (end-start))));
					String[] temp1 = text.split(",");
					String t = temp1[0];
//					System.out.println("df:"+temp1.length);
					for(int i=1;i<temp1.length;i++)
					{
						docWithTf = new ArrayList<Prox_pos>();
						ArrayList<Integer> positions = new ArrayList<Integer>();
						String[] temp2 = temp1[i].split("/");
						int docid = Integer.parseInt(temp2[0].trim());
						String docno = docnoWithIndex.get(docid);
						String[] temp3 = temp2[1].split("-");
						int tf = temp3.length;
						//String[] pos = temp3[1].split("-");
						for(int j=0;j<temp3.length;j++)
						{
							positions.add(Integer.parseInt(temp3[j].trim()));
						}
						Prox_pos temp = new Prox_pos(term, positions);
						docWithTf.add(temp);
						if(final_hmap.containsKey(docno))
						{
							docWithTf.addAll(final_hmap.get(docno));
							final_hmap.put(docno,docWithTf);
						}
						else
						{
							final_hmap.put(docno, docWithTf);
						}
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
	
		public static LinkedHashMap<String, Double> ranking (HashMap<String, Double> map) 
		{
			List<HashMap.Entry<String,Double>> list =
		            new LinkedList<HashMap.Entry<String, Double>>(map.entrySet() );
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
