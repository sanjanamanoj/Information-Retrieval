package IR.assn6;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class stopWords 
{
	public static ArrayList<String> stop() throws FileNotFoundException
	{
		String content = "";
		ArrayList stopWords = new ArrayList<String>();
		//String n = file.getName().toString();
		BufferedReader br = new BufferedReader(new FileReader("stoplist.txt"));
		try 
		{
			String line = br.readLine();
			while(line != null)
			{
			 stopWords.add(line);
			 line = br.readLine();
			}
			System.out.println(stopWords);
			System.out.println(stopWords.size());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stopWords;
	}
}
