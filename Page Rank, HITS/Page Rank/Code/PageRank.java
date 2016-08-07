//Used to compute the Page Rank for a given set of documents. This is the main function and calls the generateData class and the reqData.
package IR.assn4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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



public class PageRank
{
	static int count = 1;
	static HashMap<String,Double> pageRank = new HashMap<String,Double>();
	static ArrayList<Double> prevPerplexity = new ArrayList<Double>();
	static HashMap<String,HashSet<String>>inlinks  = new HashMap<String,HashSet<String>>();
	public static void main(String[] args) throws IOException
	{
		reqData r = generateData.generate();
		HashSet<String> s = new HashSet<String>();
		HashSet<String> p = new HashSet<String>();
		HashMap<String,HashSet<String>> outlinks = new HashMap<String,HashSet<String>>();
		 p = r.pages;
		 s = r.sink;
		inlinks = r.inlinks;
		 outlinks = r.outlinks;
		double N = p.size();
		HashMap<String,Double> newPageRank = new  HashMap<String,Double>();
		double dampingFactor = (double)0.85;


		//Initializing PageRank
		for(String i : p)
		{
			double pr = 1.0/N;
			pageRank.put(i, pr);
		}

		while(!converge())
		{
			count++;
			double sinkPr = 0;
			for(String j : s)
			{
				sinkPr+=(double) pageRank.get(j);
			}
			System.out.println("sinkPr:"+sinkPr);
			for(String i : p)
			{
				double teleportation = (double)(1 - dampingFactor)/N;
				double spread = (double)dampingFactor *(sinkPr/N);
				double temp = teleportation + spread;
				HashSet<String> in = new HashSet<String>();
				if(inlinks.containsKey(i))
				{
					in.addAll(inlinks.get(i));

					for(String h : in)
					{

						if(outlinks.containsKey(h))
						{
							temp+= (dampingFactor * ((double)((pageRank.get(h)))/(outlinks.get(h).size())));
						}

					}
			}
					newPageRank.put(i,temp);

			}
			pageRank.clear();
			pageRank.putAll(newPageRank);
		}
		sumPr();
		printresult();
	}

//Calculates the sum of all the PageRanks. If the sum is nearly equal to 1, then the PageRank works fine.
	public static void sumPr()
	{
		double sum = 0;
		for(Entry<String,Double> e: pageRank.entrySet())
		{
			sum+=e.getValue();
		}
		System.out.println(sum);
	}

// Calculates the shannon entropy and the perplexity values. The perplexity is used to check for convergence.
	public static double shannonEntropy()
	{
		double ent = 0;
		for(Entry<String,Double> x : pageRank.entrySet())
		{
			ent += (x.getValue() * (Math.log(x.getValue())/Math.log(2)));
		}
		double perplexity  = Math.pow(2, -1*(ent));
		System.out.println(count +" "+"perplexity:"+perplexity);
		return perplexity;

	}


	public static boolean converge()
	{
		double currentEntropy = shannonEntropy();
		if(prevPerplexity.size()==4)
		{
			prevPerplexity.remove(0);
			prevPerplexity.add(currentEntropy);
		}
		else
			prevPerplexity.add(currentEntropy);
		return isSame();

	}

	public static boolean isSame()
	{
		if(prevPerplexity.size()<4)
			return false;
		else
		{
			int first =  prevPerplexity.get(0).intValue();
			int second =  prevPerplexity.get(1).intValue();
			int third =  prevPerplexity.get(2).intValue();
			int fourth =  prevPerplexity.get(3).intValue();
			if(first==second && second==third && third==fourth)
				return true;
			else
				return false;
		}
	}

	//Sorts the pages based on PageRank. Called by the printresult() function
	public static LinkedHashMap<String,Double> sortPages()
	{
		List<HashMap.Entry<String,Double>> list =
				new LinkedList<HashMap.Entry<String, Double>>(pageRank.entrySet());
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

	//Prints the top 500 pages to a file
	public static void printresult() throws FileNotFoundException, UnsupportedEncodingException
	{
		int pcount = 0;
		LinkedHashMap<String, Double> result = new LinkedHashMap<String,Double>();
		result.putAll(sortPages());
		PrintWriter pw = new PrintWriter("crawlResult.txt","UTF-8");
		for (Entry<String, Double> entry : result.entrySet())
		{
			pcount++;
			pw.println(entry.getKey()+" "+ entry.getValue()+" "+inlinks.get(entry.getKey()).size());
			if(pcount == 500)
				break;
		}
		pw.close();
		System.out.println("Finished writing to file");
	}



}
