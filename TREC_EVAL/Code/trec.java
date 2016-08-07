package IR.assn5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


// This program displays the trec-eval output, which includes the average precision, R-precision, ndcg, recall,precision and F1 measures
public class trec
{
	public static HashMap<Integer,HashMap<String,Integer>> qrel = new HashMap<Integer,HashMap<String,Integer>>();
	public static LinkedHashMap<Integer,LinkedHashMap<String,Float>> rankedFile = new LinkedHashMap<Integer,LinkedHashMap<String,Float>>();
	public static LinkedHashMap<Integer,LinkedHashMap<String,Float>> SortedrankedFile = new LinkedHashMap<Integer,LinkedHashMap<String,Float>>();


	public static void main(String[] args) throws IOException
	{
		//processQrels("finalQrel.txt");
		//processRankingFile("okapiBM25_output.txt");
		processQrels("qrels.adhoc.51-100.AP89.txt");
		processRankingFile("Trec-Text-HW5.txt");
		sortRankingFile();
		graphForQuery("forGraph.txt");
		output();
	}

	//This function displays the output of trec eval to the user
	public static void output()
	{
		HashMap<Integer,Data> total = new HashMap<Integer,Data>();
		for(Entry<Integer,LinkedHashMap<String,Float>> e: SortedrankedFile.entrySet())
		{
			//The values in k represnt the ranks at which the recall and precision values are to be displayed in the output
			int[] k ={5,10,20,50,100};
			int qno = e.getKey();
			System.out.println("QueryID: "+qno);
			System.out.println("DOCUMENT STATS:");
			System.out.println("Retrieved: "+e.getValue().size());
			System.out.println("Relavant: "+ totalRelevant(qno));
			System.out.println("Rel_ret: "+ relRet(qno,getTopK_docs(qno,e.getValue().size())));
			total.put(qno,new Data(e.getValue().size(),relRet(qno,getTopK_docs(qno,e.getValue().size())),totalRelevant(qno),averagePrecision(qno),RPrecision(qno) ));
			System.out.println("RECALL VALUES:");
			for(int i=0;i<k.length;i++)
			{
				System.out.println("\t at "+k[i]+"\t"+recall(qno,getTopK_docs(qno,k[i])));
			}
			System.out.println("Average Precision: "+ averagePrecision(qno));
			System.out.println("Precision:");
			for(int i=0;i<k.length;i++)
			{
				System.out.println("\t at "+k[i]+"\t"+precision(qno,getTopK_docs(qno,k[i])));
			}
			System.out.println("R-precision: " + RPrecision(qno));
			System.out.println("F1 measures:");
			for(int i=0;i<k.length;i++)
			{
				System.out.println("\t at "+k[i]+"\t"+F1measure(qno,k[i]));
			}
			System.out.println("NDCG measures:"+ndcg(qno,1000));
			System.out.println();
		}
		 int totalret=0;
		 int totRel=0;
		 int totRelRet = 0;
		 float Avg=0;
		 float R =0;
		for(Entry<Integer,Data> e: total.entrySet())
		{
			totalret+=e.getValue().ret;
			totRel+=e.getValue().totRel;
			totRelRet+=e.getValue().relret;
			Avg+=e.getValue().avgPrec;
			R+=e.getValue().Rprec;
		}
		System.out.println("TOTAL NUMBER OF QUERIES: "+rankedFile.size());
		System.out.println("Total number of documents over all queries:");
		System.out.println("Retrieved: "+totalret);
		System.out.println("Relavant: "+ totRel);
		System.out.println("Rel_ret: "+ totRelRet);
		System.out.println("Average Precision(over all queries): "+ Avg/rankedFile.size());
		System.out.println("R-Precision (over all queries): "+R/rankedFile.size());
	}

	// Calculates the F1 measure for a given query number at rank k
	public static float F1measure(int qno, int k)
	{
		float f1=0;
		HashSet<String> topdocs = new HashSet<String>();
		topdocs.addAll(getTopK_docs(qno,k));
		float r= recall(qno,topdocs);
		float p = precision(qno,topdocs);
		f1 = (float) ((2.0 * r * p)/(r + p));
		return f1;
	}

	public static float dcg(int qno,LinkedHashSet<String> topdocs)
	{
		int count =0;
		float dcg=0;
		HashMap<String,Integer> q= new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));

		int r1 = 0;
		float sum=0;
		for(String s: topdocs)
		{
			count++;
			if(count==1)
			{
				if(q.containsKey(s))
					r1=q.get(s);
			}
			else
			{
				if(q.containsKey(s))
				{
					sum+=(q.get(s)/(Math.log(count)/Math.log(2)));
				}

			}

		}
		dcg = r1+sum;
		return dcg;
	}

	public static float ndcg(int qno,int k)
	{
		float ndcg=0;
		LinkedHashMap<String,Integer> topMap = new LinkedHashMap<String,Integer>();
		LinkedHashSet<String> topdocs = new LinkedHashSet<String>();
		topdocs.addAll(getTopK_docs(qno,k));
		HashMap<String,Integer> q= new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));
		for(String s:topdocs)
		{
			if(q.containsKey(s))
				topMap.put(s,q.get(s));
			else
				topMap.put(s,0);
		}
		LinkedHashSet<String> sortedTopdocs = new LinkedHashSet<String>();

		float numerator = dcg(qno,topdocs);
		sortedTopdocs.addAll(sortForDcg(topMap));
		float denominator = dcg(qno,sortedTopdocs);
		ndcg = numerator/denominator;
		return ndcg;
	}

	public static LinkedHashSet<String> sortForDcg(LinkedHashMap<String,Integer> topMap)
	{
		LinkedHashSet<String> sortedTopdocs = new LinkedHashSet<String>();

		List<HashMap.Entry<String,Integer>> list =
	            new LinkedList<HashMap.Entry<String, Integer>>(topMap.entrySet() );
	        Collections.sort( list, new Comparator<Map.Entry<String,Integer>>()
	        {
	            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
	            {
	                return Integer.compare(o2.getValue(), o1.getValue());
	            }
	        } );

	        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
	        for (Map.Entry<String, Integer> entry : list)
	        {
	            result.put( entry.getKey(), entry.getValue() );
	        }

	        for(Entry<String,Integer> e : result.entrySet())
	        {
	        	sortedTopdocs.add(e.getKey());
	        }
		return sortedTopdocs;
	}


	public static float RPrecision(int qno)
	{
		int totalRel = totalRelevant(qno);
		HashSet<String> topdocs = new HashSet<String>();
		topdocs.addAll(getTopK_docs(qno,totalRel));
		float Rprec = precision(qno,topdocs);
		return Rprec;
	}

	// Writes the data required to draw a recall-precision graph for each of the queries to a file.(The data is then used to plot a graph in MS Excel)
	public static void graphForQuery(String file) throws FileNotFoundException
	{
		int count =0;
		PrintWriter pw = new PrintWriter(file);
		for(Entry<Integer,LinkedHashMap<String,Float>> e: SortedrankedFile.entrySet())
		{
			LinkedHashMap<Integer,Float> recall = new LinkedHashMap<Integer,Float>();
			LinkedHashMap<Integer,Float> precision = new LinkedHashMap<Integer,Float>();
			TreeMap<Integer,Float> interpolated = new TreeMap<Integer,Float>();
			count++;
			for(int i=1;i<e.getValue().size();i++)
			{
				recall.put(i,recall(e.getKey(),getTopK_docs(e.getKey(),i)));
			}

			for(int i=1;i<e.getValue().size();i++)
			{
				precision.put(i, precision(e.getKey(),getTopK_docs(e.getKey(),i)));
			}
			System.out.println(precision.size());
			float baseValue = precision.get(999);
			for(int i=precision.size()-1;i>0;i--)
			{
				System.out.println("I:"+i);
				if(precision.get(i)<baseValue)
				{
					interpolated.put(i,baseValue);
				}
				else
				{
					baseValue=precision.get(i);
					interpolated.put(i,baseValue);
				}
			}
			pw.println("data for "+e.getKey());
			for(int j=1;j<1001;j++)
				pw.println(recall.get(j)+"\t"+precision.get(j)+"\t"+interpolated.get(j));

		}
		pw.close();
	}


	public static float averagePrecision(int qno)
	{
		int count=0;
		float totalRel = totalRelevant(qno);
		HashMap<String,Integer> q= new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));
		LinkedHashMap<String,Float> rFile = new LinkedHashMap<String,Float>();
		rFile.putAll(SortedrankedFile.get(qno));
		float prec =0;
		for(Entry<String,Float> e: rFile.entrySet())
		{
			count++;
			if(q.containsKey(e.getKey()))
			{
				if(q.get(e.getKey()) == 1)
				{
					HashSet<String> topdocs = new HashSet<String>();
					topdocs.addAll(getTopK_docs(qno,count));
					prec += precision(qno,topdocs);
				}
			}



		}
		float avgPrecision = prec/totalRel;
		return avgPrecision;
	}



	public static  LinkedHashSet<String> getTopK_docs(int qno,int r)
	{
		int count = 0;
		LinkedHashMap<String,Float> rFile = new LinkedHashMap<String,Float>();
		LinkedHashSet<String> topKDocs = new LinkedHashSet<String>();
		rFile.putAll(SortedrankedFile.get(qno));
		//System.out.println(rFile.size());
		for(Entry<String,Float> e : rFile.entrySet())
		{
			count++;
			if(count>r)
				break;
			else
				topKDocs.add(e.getKey());
		}

		return topKDocs;
	}

	public static float recall(int qno, HashSet<String> topKDocs)
	{
		float relRet = relRet(qno,topKDocs);

		float totalRel = totalRelevant(qno);
		float recall = relRet/totalRel;
		//System.out.println(recall);
		return recall;
	}

	// Returns the number of relevant documents in the topdocs passed to the function
	public static float relRet(int qno,HashSet<String> topKDocs)
	{
		float relRet=0;
		HashMap<String,Integer> q = new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));
		for(String s : topKDocs)
		{
			if(q.containsKey(s))
			{
				if(q.get(s)!=0)
					relRet+=1;
			}
		}
		return relRet;
	}



	public static float precision(int qno,HashSet<String> topKDocs)
	{
		float relRet=0;
		HashMap<String,Integer> q = new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));
		for(String s : topKDocs)
		{
			if(q.containsKey(s))
			{
				if(q.get(s)!=0)
					relRet+=1;
			}
		}
		float precision = relRet/(float)topKDocs.size();
		//System.out.println(precision);
		return precision;
	}


	//Calculates the total number of relevant documents in the qrel file
	public static int totalRelevant(int qno)
	{
		int rel =0 ;
		HashMap<String,Integer> q = new HashMap<String,Integer>();
		q.putAll(qrel.get(qno));
		for(Entry<String,Integer> e: q.entrySet())
		{
			if(e.getValue()!=0)
				rel+=1;
		}
		return rel;
	}


	//Reads the qrel file into memory
	public static void processQrels(String filename) throws IOException
	{

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int qno = Integer.parseInt(temp[0].trim());
			String docid = temp[2].trim();
			float relevance = Float.parseFloat(temp[3].trim());
			if(qrel.containsKey(qno))
			{
				HashMap<String,Integer> docs = new HashMap<String,Integer>();
				docs.putAll(qrel.get(qno));
				docs.put(docid,(int)relevance);
				qrel.put(qno,docs);
			}
			else
			{
				HashMap<String,Integer> docs = new HashMap<String,Integer>();
				docs.put(docid,(int)relevance);
				qrel.put(qno,docs);
			}
			line = br.readLine();
		}
		br.close();
		System.out.println("done reading the file :"+ qrel.size());
	}

	//Sorts the ranking file by score
	public static void sortRankingFile()
	{

		for(Entry<Integer,LinkedHashMap<String,Float>> e: rankedFile.entrySet())
		{
			SortedrankedFile.put(e.getKey(),sort(e.getValue()));
		}
	}

	public static LinkedHashMap<String,Float> sort(LinkedHashMap<String,Float> m)
	{
		 List<HashMap.Entry<String,Float>> list =
		            new LinkedList<HashMap.Entry<String, Float>>( m.entrySet() );
		        Collections.sort( list, new Comparator<Map.Entry<String,Float>>()
		        {
		            public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 )
		            {
		                return Float.compare(o2.getValue(), o1.getValue());
		            }
		        } );

		        LinkedHashMap<String, Float> result = new LinkedHashMap<String, Float>();
		        for (Map.Entry<String, Float> entry : list)
		        {
		            result.put( entry.getKey(), entry.getValue() );
		            if(result.size()>=1000)
		            	break;
		        }

		        return result;
	}

	public static void processRankingFile(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			System.out.println(line);
			String[] temp = line.split(" ");
			int qno = Integer.parseInt(temp[0].trim());
			System.out.println(qno);
			String docid = temp[2].trim();
			//String docid = temp[3].trim();
			System.out.println(docid);
			float score = Float.parseFloat(temp[4].trim());
			//float score = Float.parseFloat(temp[6].trim());
			if(rankedFile.containsKey(qno))
			{
				LinkedHashMap<String,Float> docs = new LinkedHashMap<String,Float>();
				docs.putAll(rankedFile.get(qno));
				docs.put(docid, score);

				rankedFile.put(qno, docs);
			}
			else
			{
				LinkedHashMap<String,Float> docs = new LinkedHashMap<String,Float>();

				docs.put(docid, score);
				rankedFile.put(qno, docs);
			}
			line = br.readLine();

		}
		br.close();
		System.out.println(rankedFile.size());
	}
}
