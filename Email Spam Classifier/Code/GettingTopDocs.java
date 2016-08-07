package IR.assn7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GettingTopDocs 
{
	public static LinkedHashMap<Integer,String> docnos = new LinkedHashMap<Integer,String>();
	public static LinkedHashMap<Integer,Double> score = new LinkedHashMap<Integer,Double>();
	public static LinkedHashMap<String,Double> spamMap = new LinkedHashMap<String,Double>();
	public static LinkedHashMap<Integer,Integer> labelMap = new LinkedHashMap<Integer,Integer>();
	public static  LinkedHashMap<String,Integer> finalLabel = new LinkedHashMap<String,Integer>();
	public static void main(String[] args) throws IOException
	{
//		getDocno("Part1/manualTestDocs.txt");
//		getScores("Part1/manualTestOut");
//		combineMaps();
//		combineLabel();
//		printmap("Part1/TopSpamTest.txt");
		
//		getDocno("Part3/testdocs.txt");
//		getScores("Part3/testOut");
//		combineMaps();
//		combineLabel();
//		printmap("Part3/TopSpamTest.txt");
		
		getDocno("Part3/traindocs.txt");
		getScores("Part3/trainOut");
		combineMaps();
		combineLabel();
		printmap("Part3/TopSpamTrain.txt");
		
	}
	
	public static void getDocno(String filename) throws IOException
	{
		int count =1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int label =Integer.parseInt(temp[1].trim());
			labelMap.put(count,label);
			String docno = temp[0].trim();
			docnos.put(count,docno);
			count++;
			line=br.readLine();
		}
		br.close();
	}
	
	public static void getScores(String filename) throws IOException
	{
		int count =1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		line=br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			
			score.put(count,Double.parseDouble(temp[1].trim()));
			count++;
			line=br.readLine();
		}
		br.close();
		
	}
	
	public static void combineLabel()
	{
		for(int i=1;i<=docnos.size();i++)
		{
			finalLabel.put(docnos.get(i), labelMap.get(i));
		}
		
	}
	public static void combineMaps()
	{
		for(int i=1;i<=docnos.size();i++)
		{
			spamMap.put(docnos.get(i), score.get(i));
		}
	}
	
	public static void printmap(String filename) throws FileNotFoundException
	{
		System.out.println("printing map");
		PrintWriter pw = new PrintWriter(filename);
		LinkedHashMap<String,Double> map = new LinkedHashMap<String,Double>();
		map.putAll(sortMap(spamMap));
		for(Entry<String,Double> e : map.entrySet())
		{
			pw.println(e.getKey()+" "+e.getValue()+ " "+finalLabel.get(e.getKey()));
//			LinkedHashMap<Integer, Integer> hmap = new LinkedHashMap<Integer, Integer>();
//			hmap.putAll(sortMap(e.getValue()));
//			for(Entry<Integer,Integer> e1 : hmap.entrySet())
//			{
//				pw.print(e1.getKey()+":"+e1.getValue()+" ");
//			}
//			pw.println();
		}
		pw.close();
		System.out.println("done printing");
	}
	public static LinkedHashMap<String, Double> sortMap(LinkedHashMap<String, Double> map)
	{

		List<HashMap.Entry<String, Double>> list =
	            new LinkedList<HashMap.Entry<String, Double>>(map.entrySet() );
	        Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
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
