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

public class TopFeatures 
{
	public static LinkedHashMap<Integer,String> words = new LinkedHashMap<Integer,String>();
	public static LinkedHashMap<String,Double> features = new LinkedHashMap<String,Double>();
 	public static void main(String[] args) throws IOException
	{
		loadWords("Part2/unigramMap.txt");
		loadModel("Part2/unigramModel");
		printTopFeatures("Part2/unigramTopFeatures.txt");
	}
 
 	public static void printTopFeatures(String filename) throws FileNotFoundException
 	{
 		LinkedHashMap<String, Double> sortedFeatures = new LinkedHashMap<String, Double>();
 		sortedFeatures.putAll(sortFeatures(features));
 		PrintWriter pw = new PrintWriter(filename);
 		for(Entry<String,Double> e:sortedFeatures.entrySet())
 		{
 			pw.println(e.getKey()+" "+e.getValue());
 		}
 		pw.close();
 		
 	}
 	
 	public static LinkedHashMap<String, Double> sortFeatures (HashMap<String, Double> map) 
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
 	
	public static void loadWords(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			try{
				String word ="";
				int num = Integer.parseInt(temp[temp.length-1].trim());
				for(int i=0;i<temp.length-1;i++)
				{
					word+=temp[i];
					word+=" ";
				}
				words.put(num,word);
			}
			catch(Exception e)
			{
				line = br.readLine();
				continue;
			}
			
			line=br.readLine();
		}
		br.close();
	}
	
	public static void loadModel(String filename) throws IOException
	{
		int count =1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			if(count<=5)
			{
				line=br.readLine();
				count++;
			}
			else
			{
				double score = Double.parseDouble(line.trim());
				features.put(words.get(count), score);
				count++;
				line=br.readLine();
			}
		}
		br.close();
				
	}
}
