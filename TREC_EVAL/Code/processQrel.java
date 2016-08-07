package IR.assn5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;


// This program is written to combine the manual assessments of all four in the team to a single qrel file
public class processRankFile
{
	public static HashMap<Integer,HashMap<String,Float>> sanj = new HashMap<Integer,HashMap<String,Float>>();
	public static HashMap<Integer,HashMap<String,Float>> anvita = new HashMap<Integer,HashMap<String,Float>>();
	public static HashMap<Integer,HashMap<String,Float>> mohsen = new HashMap<Integer,HashMap<String,Float>>();
	public static HashMap<Integer,HashMap<String,Float>> soumya = new HashMap<Integer,HashMap<String,Float>>();

	public static HashMap<Integer,HashMap<String,Float>> qrel = new HashMap<Integer,HashMap<String,Float>>();

	public static void main(String[] args) throws IOException
	{
		sanj.putAll(readRankFile("SanjanaQrel.txt"));
		anvita.putAll(readRankFile("AnvitaQrel.txt"));
		mohsen.putAll(readRankFile("Mohsen_eval.txt"));
		soumya.putAll(readRankFile("SoumyaQrel.txt"));
		getCombinedQrel();
		printQrel();
	}

	//Prints the result into a file --> "finalQrel.txt"
	public static void printQrel() throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter("finalQrel.txt");
		for(Entry<Integer,HashMap<String,Float>> e: qrel.entrySet())
		{
			for(Entry<String,Float> f : e.getValue().entrySet())
			{
				pw.println(e.getKey()+" "+0+" "+f.getKey()+" "+f.getValue());
			}
		}
		pw.close();
	}


	//Computes the average of all the ranks and takes rank 0 if the average is less than 1, else takes 1
	public static void getCombinedQrel()
	{

		for(Entry<Integer,HashMap<String,Float>> e: sanj.entrySet())
		{
			HashMap<String,Float> r = new HashMap<String,Float>();
			HashMap<String,Float> anv = new HashMap<String,Float>();
			anv.putAll(anvita.get(e.getKey()));
			HashMap<String,Float> moh = new HashMap<String,Float>();
			moh.putAll(mohsen.get(e.getKey()));
			HashMap<String,Float> sou = new HashMap<String,Float>();
			sou.putAll(soumya.get(e.getKey()));
			HashMap<String,Float> docs = new HashMap<String,Float>();
			docs.putAll(e.getValue());
			for(Entry<String,Float> e1: docs.entrySet())
			{
				float sum = e1.getValue();
				if(anv.containsKey(e1.getKey()))
				{
					sum+= anv.get(e1.getKey());
				}
				if(moh.containsKey(e1.getKey()))
				{
					sum+= moh.get(e1.getKey());
				}
				if(sou.containsKey(e1.getKey()))
				{
					sum+= sou.get(e1.getKey());
				}
				float avg = sum/4;
				float rank;
				if(avg<0.5)
					rank=0;
				else if(avg < 1.5 && avg>=0.5)
					 rank=1;
				else
					rank=2;

				r.put(e1.getKey(), rank);
 			}
			if(qrel.containsKey(e.getKey()))
			{
				HashMap<String,Float> temp = new HashMap<String,Float>();
				temp.putAll(qrel.get(e.getKey()));
				temp.putAll(r);
				qrel.put(e.getKey(),temp);
			}
			else
				qrel.put(e.getKey(),r);
		}
		System.out.println(qrel.size());
	}


	public static HashMap<Integer,HashMap<String,Float>> readRankFile(String filename) throws IOException
	{
		HashMap<Integer,HashMap<String,Float>> rfile = new HashMap<Integer,HashMap<String,Float>>();
		int count =1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int qid = Integer.parseInt(temp[0].trim());
			String docid = temp[2].trim();
			float rank = Float.parseFloat(temp[3].trim());
			HashMap<String,Float> r = new HashMap<String,Float>();

			if(rfile.containsKey(qid))
			{
				r.putAll(rfile.get(qid));
				r.put(docid,rank);
				rfile.put(qid, r);
			}
			else
			{
				r.put(docid, rank);
				rfile.put(qid,r);
			}
			count++;
			line=br.readLine();
		}
		br.close();
		return rfile;
	}
}
