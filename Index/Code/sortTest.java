package IR.assn2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.EnglishStemmer;

public class sortTest 
{
	public static int count = 0;
	public static int dcid = 0;
	public static void main(String[] args) throws IOException
	{
		HashMap<String,HashMap<Integer , Tf_Pos>> hmap = new HashMap<String,HashMap<Integer , Tf_Pos>>();
		HashMap<Integer , Tf_Pos> jkl1 = new HashMap<Integer , Tf_Pos>();
		ArrayList<Integer> pos1 = new ArrayList<Integer>();
		pos1.add(5);
		pos1.add(10);
		pos1.add(15);
		Tf_Pos tf1 = new Tf_Pos(2,pos1);
		jkl1.put(1,tf1);
		
		ArrayList<Integer> pos2 = new ArrayList<Integer>();
		pos2.add(5);
		Tf_Pos tf2 = new Tf_Pos(3,pos1);
		jkl1.put(2,tf2);
		
		ArrayList<Integer> pos3 = new ArrayList<Integer>();
		pos3.add(5);
		pos3.add(10);
		Tf_Pos tf = new Tf_Pos(4,pos1);
		jkl1.put(3,tf);
		
		hmap.put("algo", jkl1);
		HashMap<Integer , Tf_Pos> jkl2 = new HashMap<Integer , Tf_Pos>();
		
		ArrayList<Integer> pos4 = new ArrayList<Integer>();
		pos4.add(5);
		pos4.add(10);
		pos4.add(15);
		Tf_Pos tf4 = new Tf_Pos(2,pos4);
		jkl2.put(1,tf4);
		
		ArrayList<Integer> pos5 = new ArrayList<Integer>();
		pos5.add(5);
		Tf_Pos tf5 = new Tf_Pos(7,pos5);
		jkl2.put(2,tf5);
		
		ArrayList<Integer> pos6 = new ArrayList<Integer>();
		pos6.add(5);
		pos6.add(10);
		Tf_Pos tf6 = new Tf_Pos(4,pos6);
		jkl2.put(3,tf6);
		hmap.put("test", jkl2);
		compare(hmap);
	}
	
	public static void readFolder() throws IOException
	{
		System.out.println("read folder");
		String target_dir = "ap89_collection2";
        File dir = new File(target_dir);
		File[] files = dir.listFiles();
		readFile(files);
	}
	
	public static void readFile(File[] files) throws IOException
	{
		
		HashMap<String,Integer> docids = new LinkedHashMap<String,Integer>();
		System.out.println("readFile");
		stopWords s = new stopWords();
		ArrayList<String> stopWords = new ArrayList<String>();
		ArrayList<Tf_index> idWithText = new ArrayList<Tf_index>();	
		stopWords = s.stop();
		stopWords.add("(");
		stopWords.add(")");
		stopWords.add(".");
		stopWords.add("?");
		stopWords.add("*");
		stopWords.add("+");
		
		
		String line ="",text ="";
		for(int i=0;i<files.length;i++)
		{
//			idWithText = new ArrayList<Tf_index>();		
			try
			{     
		      String n = "./ap89_collection2/"+files[i].getName().toString();
		      String docno = null;
		      BufferedReader br = new BufferedReader(new FileReader(n));
		      int insideDoc = 0;
		      int insideText = 0;
		      line = br.readLine();
		      while(line != null)
		      {
		        if(line.contains("<DOC>"))
		        {
		        	insideDoc=1;
		        }
		        while(insideDoc==1 && line!=null)
		        {
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
		        		
		        		text+= line;
		        		text +=" ";
		        	}
		        	line=br.readLine();
		        }
		        
		        dcid++;
		      //  System.out.println("dcid=" + dcid +  " docno " + docno);
		        docids.put(docno, dcid);
		        Tf_index temp = new Tf_index(dcid,text);
		        idWithText.add(temp);
		        text="";
		        docno="";
		        if(idWithText.size()==1000)
		        {
		        	//if(i == files.length - 1)
		        	parseText(idWithText, stopWords);
		        	idWithText = new ArrayList<Tf_index>();
		        	//continue;
		        }
		        line = br.readLine();
		       
		      }
		      
		    }
		    catch(IOException e) {}
			 
		}
		
		parseText(idWithText, stopWords);
    	idWithText = new ArrayList<Tf_index>();
	}
	
	
	
	
		public static void parseText(ArrayList<Tf_index> idWithText, ArrayList<String> stopWords) throws IOException 
		{
			if(idWithText.size()>0)
			{
			count++;
//			System.out.println("parsing" + count);
			HashMap<String , Offset> catalog = new HashMap<String, Offset>();
			HashMap<String,HashMap<Integer , Tf_Pos>> hmap = new HashMap<String,HashMap<Integer , Tf_Pos>>();
			
			
			for(Tf_index p : idWithText)
			{

				String text = p.text;
//				
				ArrayList<String> splitText = new ArrayList<String>();
				ArrayList<String> finalText = new ArrayList<String>();
				Pattern pattern = Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
				Matcher matcher = pattern.matcher(text);

				while (matcher.find())
				{
					splitText.add(matcher.group().toLowerCase());
				    //System.out.println("I found the text " + matcher.group());
				}
				HashMap<Integer, Tf_Pos> docFreq = new HashMap<Integer, Tf_Pos>();
				splitText.removeAll(stopWords);
			
				
				EnglishStemmer ps = new EnglishStemmer();
				for(String t : splitText )
				{
				
					ps.setCurrent(t.toLowerCase());
					ps.stem();
					finalText.add(ps.getCurrent());
				}		
//				
				String[] finalString = new String[finalText.size()];
				 finalString = finalText.toArray(finalString);
				for(int i=0;i<finalString.length; i++)
				{
					ArrayList<Integer> pos = new ArrayList<Integer>();
					docFreq = new HashMap<Integer, Tf_Pos>();
					if(!hmap.containsKey(finalString[i]))
					{
						
						pos = new ArrayList<Integer>();
						pos.add(i+1);
						Tf_Pos temp = new Tf_Pos(1 , pos);
						docFreq.put(p.docid, temp);
					}
					else
					{
						
						docFreq= hmap.get(finalString[i]);
						if(!docFreq.containsKey(p.docid))
						{
							pos = new ArrayList<Integer>();
							pos.add(i+1);
							Tf_Pos temp = new Tf_Pos(1 , pos);
							docFreq.put(p.docid, temp);		
						}
						else
						{
							int tf =docFreq.get(p.docid).tf + 1;
							pos = new ArrayList<Integer>();
							pos.addAll(docFreq.get(p.docid).pos);
							pos.add(i+1);
							Tf_Pos temp = new Tf_Pos(tf , pos);
							docFreq.put(p.docid,temp);	
						}
					}
					hmap.put(finalString[i],docFreq);
					
				}
				System.out.println(docFreq.size() );
				System.out.println(hmap.size());
			}
			compare(hmap);
		}
	}
		
		
		public static HashMap<String,HashMap<Integer , Tf_Pos>> compare(HashMap<String,HashMap<Integer , Tf_Pos>> hmap)
		{
			HashMap<String,HashMap<Integer , Tf_Pos>> sortedMap = new HashMap<String,HashMap<Integer , Tf_Pos>>();
			for(Entry<String, HashMap<Integer , Tf_Pos>> e : hmap.entrySet())
			{
				HashMap<Integer,Tf_Pos> h = new HashMap<Integer,Tf_Pos>();
				System.out.println(e.getKey());
				System.out.println(e.getValue().size());
	
					 List<Entry<Integer , Tf_Pos>> sortedEntries = new ArrayList<Entry<Integer , Tf_Pos>>(e.getValue().entrySet());

					    Collections.sort(sortedEntries, 
					            new Comparator<Entry<Integer , Tf_Pos>>() {
					                public int compare(Entry<Integer , Tf_Pos> e1, Entry<Integer , Tf_Pos> e2) {
					                    return Integer.compare((e2.getValue().tf),(e1.getValue().tf));
					                }
					            }
					    );
					    
					    //System.out.println(sortedEntries.size());
				
					    HashMap<Integer , Tf_Pos> result = new LinkedHashMap<Integer , Tf_Pos>();
				        for (Map.Entry<Integer , Tf_Pos> entry : sortedEntries)
				        {
				            result.put( entry.getKey(), entry.getValue() );
				          //  System.out.println(entry.getKey()+" "+entry.getValue().tf);
				        }
				      sortedMap.put(e.getKey(),result);
		    }
//			for(Entry<String, HashMap<Integer , Tf_Pos>> e : sortedMap.entrySet())
//			{
//				System.out.println(e.getKey());
//				HashMap<Integer,Tf_Pos> h = new HashMap<Integer,Tf_Pos>();
//				h = e.getValue();
//				for(Entry<Integer , Tf_Pos> k : h.entrySet())
//				{
//					System.out.println(k.getKey()+" "+k.getValue().tf);
//				}
//				System.out.println();
//				
//			}
			return sortedMap;
		}
}

