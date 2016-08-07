package IR.assn1;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class queries 
{
	public static ArrayList<ArrayList<String>> query() throws FileNotFoundException
	{
	
		ArrayList sw = new ArrayList<String>();
		stopWords s = new stopWords();
		sw = s.stop();
		
		ArrayList queryList = new ArrayList<ArrayList<String>>();
		String content = "";
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader("query_desc.51-100.short.txt"));
			try 
			{
				String line = br.readLine();
				while(line != null)
				{
					content +=line;
					content += " ";
					line=br.readLine();	
				}
				ArrayList t = new ArrayList<String>();
				String[] terms = content.split("[0-9]{1,2}\\.");
				System.out.println("here");
				for(int i = 0;i<terms.length;i++)
				{
					
						String temp2 = terms[i].replace(",", "")
								.replace(".","")
								.replace("(", "")
								.replace(")", "");
						temp2=temp2.trim();
						String[] q = temp2.split(" ");
					
						ArrayList query = new ArrayList<String>();
						for(int k =0;k<q.length;k++)
						{
							String l = q[k].trim();
							query.add(l);
							//System.out.println("Query:"+ query);
							
						}
						query.removeAll(sw);
						queryList.add(query);
						
				}
				System.out.println(queryList);
			}
				
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
		}
		
		catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		
		return queryList;
		
	}
}
