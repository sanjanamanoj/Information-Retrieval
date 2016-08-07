
//This is the return type used in generateData to make the pages, sinks, inlinks and outlinks available for PageRank
package IR.assn4;

import java.util.HashMap;
import java.util.HashSet;

public class reqData 
{
	public static HashMap<String,HashSet<String>> inlinks = new HashMap<String,HashSet<String>>();
	public static HashMap<String,HashSet<String>> outlinks = new HashMap<String,HashSet<String>>();
	public static HashSet<String> pages = new HashSet<String>();
	public static HashSet<String> sink = new HashSet<String>();
	public reqData(HashMap<String,HashSet<String>> in,HashMap<String,HashSet<String>> out,HashSet<String> p,HashSet<String> s )
	{
		inlinks.putAll(in);
		outlinks.putAll(out);
		pages.addAll(p);
		sink.addAll(s);
	}
	
}
