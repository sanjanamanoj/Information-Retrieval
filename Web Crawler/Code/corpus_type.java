package IR.assn3;

import java.util.HashMap;
import java.util.HashSet;

public class corpus_type 
{
	public String docno;
	public String head;
	public String text;
	public String url;
	public String HTTPheader;
	public String source;
	public HashSet<String> outlinks;
	public corpus_type(String id, String header, String body,String link, HashSet<String>out,String src,String http)
	{
		outlinks = new HashSet<String>();
		docno = id;
		head = header;
		text = body;
		url = link;
		outlinks.addAll(out);
		source = src;
		HTTPheader = http;
	}

}
