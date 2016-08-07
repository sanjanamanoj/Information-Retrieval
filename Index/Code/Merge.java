package IR.assn2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class Merge 
{
	public static long count = 0;
	public static void mergeFiles() throws IOException
	{
		HashMap<String,HashMap<Integer,Tf_Pos>> finalMap = new HashMap<String,HashMap<Integer,Tf_Pos>>();
		HashMap<String,Offset>catalog1 = new HashMap<String,Offset>();
		HashMap<String,Offset>catalog2 = new HashMap<String,Offset>();
		
		String target_invLists = "invertedList";
        File dir = new File(target_invLists);
		File[] invLists = dir.listFiles();
		
		String target_catalog = "catalog";
        File dirc = new File(target_catalog);
		File[] catalog = dirc.listFiles();
		for(int i=1;i<invLists.length;i++)
		{	
			System.out.println(count);
			finalMap = new HashMap<String,HashMap<Integer,Tf_Pos>>();
			catalog1 = new HashMap<String,Offset>();
			catalog2 = new HashMap<String,Offset>();
			String catalog01;
			String catalog02;
			String invList1;
			String invList2;
			try
			{    
				if(count == 0)
				{	
					 invList1 = "./invertedList/"+invLists[0].getName().toString();
				     catalog01 = "./catalog/"+catalog[0].getName().toString();
				     invList2 = "./invertedList/"+invLists[1].getName().toString();    	 
				      catalog02 = "./catalog/"+catalog[1].getName().toString();
				}
				else
				{
					 invList1 = "./invertedList/"+invLists[i].getName().toString();
				     catalog01 = "./catalog/"+catalog[i].getName().toString();
				     invList2 = "./mergeList/mergeList"+count+".txt";   	 
				     catalog02 = "./mergeCatalog/mcatalog"+count+".txt";
				}
				
		     
		     
		     
			
		
		catalog1.putAll(readFromCatalog(catalog01));
		
		catalog2.putAll(readFromCatalog(catalog02));
		for (Entry<String,Offset> e : catalog1.entrySet())
		{
			String term = e.getKey();
			Offset o = e.getValue();
		
			long start1 = o.start;
			long end1 = o.end;
//			System.out.println(term);
//			System.out.println("start:"+start1);
//			System.out.println("end:"+end1);
		}
		for (Entry<String,Offset> e : catalog1.entrySet())
		{
			String term = e.getKey();
			Offset o = e.getValue();
			String filename1 = invList1;
			String filename2 = invList2;
			long start1 = o.start;
			long end1 = o.end;
//			System.out.println(term);
//			System.out.println("start:"+start1);
//			System.out.println("end:"+end1);
//			System.out.println(filename1);
//			System.out.println(filename2);
			if( catalog2.containsKey(term))
			{
				
				Offset c2 = catalog2.get(term);
				long start2 = c2.start;
				long end2 = c2.end;
				String text1 = (new String(readFromFile(filename1 , (int) start1, (int) (end1-start1))));
				HashMap<String,HashMap<Integer,Tf_Pos>>parsedText1 = parseString(text1);
				String text2 = (new String(readFromFile(filename2 , (int) start2, (int) (end2-start2))));
				HashMap<String,HashMap<Integer,Tf_Pos>>parsedText2 = parseString(text2);
				HashMap<String,HashMap<Integer,Tf_Pos>> mergedTerm =mergeSort(term,parsedText1.get(term),parsedText2.get(term));
				finalMap.putAll(mergedTerm);
				catalog2.remove(term);
			}
			else
			{
				String text1 = (new String(readFromFile(filename1 , (int) start1, (int) (end1-start1))));
				HashMap<String,HashMap<Integer,Tf_Pos>>parsedText1 = parseString(text1);
				finalMap.putAll(parsedText1);
			}
		}
		
			for (Entry<String,Offset> e : catalog2.entrySet())
			{
				String term = e.getKey();
				Offset o = e.getValue();
				String filename2 = invList2;
				long start2 = o.start;
				long end2 = o.end;
				String text2 = (new String(readFromFile(filename2 , (int) start2, (int) (end2-start2))));
				HashMap<String,HashMap<Integer,Tf_Pos>>parsedText2 = parseString(text2);
				finalMap.putAll(parsedText2);
			}
		
		 long offset = 0;
		 HashMap<String , Offset> mergeCatalog = new HashMap<String, Offset>();
		 count++;
		 String fileName = "./mergeList/mergeList"+count+".txt";
		 RandomAccessFile outputFile = new RandomAccessFile(fileName, "rw");
		 String first = "";
		for (Entry<String, HashMap<Integer,Tf_Pos>> e : finalMap.entrySet())
			 
		{
		

			long start = offset;
			StringBuilder str = new StringBuilder();
			
			
			str.append(e.getKey());	
//				System.out.println("term:"+e.getKey());
			HashMap<Integer, Tf_Pos> temp3 = new HashMap<Integer, Tf_Pos>();
			temp3 = e.getValue();				
			for (Entry<Integer,Tf_Pos> e1 : temp3.entrySet())
			{
				str.append(",");
				str.append(e1.getKey());
				//System.out.println("docid:"+e1.getKey());
				//str.append(":");
				//str.append(e1.getValue().tf);
				//str.append(" ");
				ArrayList<Integer> p = e1.getValue().pos;
				str.append("/");
				for(Integer g : p)
				{
					str.append(g);
					if(p.indexOf(g)!= (p.size()-1))
						str.append("-");
				}
				str.append("/");
				//System.out.println("freq:"+e1.getValue());
				//str.append(")");
			}
			str.append("\r\n");
			outputFile.seek(start);
			outputFile.writeBytes(str.toString());
			offset = outputFile.getFilePointer();
			long end = offset;
			mergeCatalog.put(e.getKey(), new Offset(start,end));
		}
		System.out.println("written to output");
		outputFile.close();	
		String catalogName = "./mergeCatalog/mcatalog"+count+".txt";
		PrintWriter writer = new PrintWriter(catalogName, "UTF-8");
		for (Entry<String,Offset> e1 : mergeCatalog.entrySet())
		{					
			writer.println(e1.getKey()+" "+e1.getValue().start+" "+ e1.getValue().end);					
		}
		writer.close();
		System.out.println("done merging");
		
		
			}
			 catch(IOException e) {}
		
		}
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
//		for (Entry<String,Offset> e1 : catalogMap.entrySet())
//		{
//			
//			System.out.println(e1.getKey());
//			System.out.println(e1.getValue().start);
//			System.out.println(e1.getValue().end);
//		}
		return catalogMap;
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
	
	public static HashMap<String,HashMap<Integer,Tf_Pos>> parseString(String text)
	{
		String[] split = text.split(",");
		//System.out.println(text);
		HashMap<Integer,Tf_Pos> termFreq = new LinkedHashMap<Integer,Tf_Pos>();
		HashMap<String,HashMap<Integer,Tf_Pos>> finalHmap = new LinkedHashMap<String,HashMap<Integer,Tf_Pos>>();
		String term = split[0];
		for(int i = 1; i<split.length; i++)
		{
			String[] temp = split[i].split("/");
			int docid = Integer.parseInt(temp[0].trim());
			//System.out.println(temp.length);
			//System.out.println(temp[0]);
			//System.out.println(temp[1]);
			
			String[] temp2 = temp[1].split("-");
//			for(int k=0;k<temp2.length;k++)
//				System.out.println("here:"+temp2[k]);
			int tf = temp2.length;
			//String[] temp3 = temp2[1].split("-");
			//System.out.println(temp3[0]);
			ArrayList<Integer> pos = new ArrayList<Integer>();
			for(int j = 0;j<temp2.length;j++)
			{
				pos.add(Integer.parseInt(temp2[j].trim()));
			}
			Tf_Pos t = new Tf_Pos(tf, pos);
			
			termFreq.put(docid,t);
		}
		finalHmap.put(term, termFreq);
		return finalHmap;
	}
	
//	public static HashMap<String,HashMap<Integer,Tf_Pos>> mergeForSameTerm(String term, HashMap<Integer,Tf_Pos> map1,HashMap<Integer,Tf_Pos> map2)
//	{
//		
//		map1.putAll(map2);
//		finalTemp.put(term,map1);
//		return finalTemp;
//	}
	
	public static HashMap<String,HashMap<Integer,Tf_Pos>> mergeSort(String term, HashMap<Integer,Tf_Pos> map1,HashMap<Integer,Tf_Pos> map2)
	{
		HashMap<String,HashMap<Integer,Tf_Pos>> finalTemp = new HashMap<String,HashMap<Integer,Tf_Pos>>();
		HashMap<Integer,Tf_Pos> merged = new LinkedHashMap<Integer,Tf_Pos>();
		int size = map1.size()+map2.size();
		 List<Entry<Integer , Tf_Pos>> first = new ArrayList<Entry<Integer , Tf_Pos>>(map1.entrySet());
		 List<Entry<Integer , Tf_Pos>> second = new ArrayList<Entry<Integer , Tf_Pos>>(map2.entrySet());
		for(int i=0;i<size;i++)
		{
			if(first.size()>0 && second.size()>0)
			{
				int tf1 = first.get(0).getValue().tf;
				int tf2 = second.get(0).getValue().tf;
				if(tf1>tf2)
				{
					merged.put(first.get(0).getKey(), first.get(0).getValue());
					first.remove(0);
				}
				else
				{
					merged.put(second.get(0).getKey(), second.get(0).getValue());
					second.remove(0);
				}
			}
			if(first.size()==0)
			{
				for(int j=0;j<second.size();j++)
				{
					merged.put(second.get(0).getKey(), second.get(0).getValue());
					second.remove(0);
				}
			}
			if(second.size()==0)
			{
				for(int j=0;j<first.size();j++)
				{
					merged.put(first.get(0).getKey(), first.get(0).getValue());
					first.remove(0);
				}
			}
		}
		finalTemp.put(term, merged);
		return finalTemp;
	}

}
