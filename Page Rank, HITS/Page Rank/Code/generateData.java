// This class is used to read the text file consisting of inlinks to generate the set of pages, sinks, inlinks and outlinks graph

package IR.assn4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class generateData
{
	public static reqData generate() throws IOException
	{
		HashMap<String,HashSet<String>> inlinks = new HashMap<String,HashSet<String>>();
		HashMap<String,HashSet<String>> outlinks = new HashMap<String,HashSet<String>>();
		HashSet<String> pages = new HashSet<String>();
		HashSet<String> sink = new HashSet<String>();

		//Specify which file to read from. The wt2g or the crawled results.

		BufferedReader br = new BufferedReader(new FileReader("inlinksForPr.txt"));
		String line =br.readLine();
		while(line!=null)
		{
		// Split by tab for crawled documents and by blank space for the wt2g file.
			String[] temp = line.split("\t");
			HashSet<String> tempIn = new HashSet<String>();
			HashSet<String> tempOut = new HashSet<String>();
			pages.add(temp[0].trim());
			inlinks.put(temp[0].trim(),new HashSet<String>());
			String destination = temp[0].trim();
			if(temp.length>1)
			{
				for(int i=1;i<temp.length;i++)
				{
					if(temp[i].trim().length()==0)
						continue;
					pages.add(temp[i].trim());
					tempIn.add(temp[i].trim());
					if(outlinks.containsKey(temp[i].trim()))
					{
						HashSet<String> tem = new HashSet<String>();
						tem.addAll(outlinks.get(temp[i].trim()));
						tem.add(temp[0].trim());
						outlinks.put(temp[i].trim(),tem);
					}
					else
					{
						HashSet<String> tem = new HashSet<String>();
						tem.add(temp[0].trim());
						outlinks.put(temp[i].trim(),tem);
					}
				}
				inlinks.put(destination,tempIn);
			}
			line=br.readLine();
		}
		br.close();

    //Sink is a page that has no outlinks.
		sink.addAll(pages);
		sink.removeAll(outlinks.keySet());

		System.out.println("pagesSize:"+pages.size());
		System.out.println("sinkSize:"+sink.size());
		System.out.println("inlinkSize:"+inlinks.size());
		System.out.println("outlinkSize:"+outlinks.size());
		reqData r = new reqData(inlinks,outlinks,pages,sink);
		return r;
	}

}
