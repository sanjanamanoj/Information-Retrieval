package IR.assn3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class gettingCatalog 
{
	public gettingCatalog() throws IOException
	{
		HashMap<String,HashSet<String>>inlinks = new HashMap<String,HashSet<String>>();
		BufferedReader br = new BufferedReader(new FileReader("inlinks58.txt"));
		try 
		{
			String line = br.readLine();
			while(line != null)
			{
				String[] temp = line.split("\t");
				System.out.println(temp[0]);
				String url = temp[0];
				HashSet<String> temp1 = new HashSet<String>();
				for(int i =1;i<temp.length;i++)
				{
					temp1.add(temp[i]);
				}
				inlinks.put(url,temp1);
				temp1=new HashSet<String>();
				
				line = br.readLine();
			}
			
		} 		
		catch (IOException e) 
		{
			
			e.printStackTrace();
		}
		br.close();
	}
}
