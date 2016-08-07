package IR.assn3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class dumpURL 
{
	static int count = 0;
	static HashMap<String, headersource> map = new HashMap<String,headersource>();
	static HashMap<String, Offset> catalog = new HashMap<String,Offset>();
	static HashSet<String> urls = new HashSet<String>();
	public static void main(String[] args) throws IOException
	{
		String target_dir = "corpus";
        File dir = new File(target_dir);
      
       
        File[] files = dir.listFiles();
        
		for (File file : files) {
		   //System.out.println(file.getName());
		System.out.println(file.getName());
		getUrl(file);
	
		}
		ToFile();
		//printcatalog();
	}
	public static void ToFile() throws FileNotFoundException, UnsupportedEncodingException
	{
		 String catalogName = "URLlist.txt";
			PrintWriter writer = new PrintWriter(catalogName, "UTF-8");
			for (String s: urls)
			{					
				writer.println(s);					
			}
			writer.close();
	}
 public static void printcatalog() throws FileNotFoundException, UnsupportedEncodingException
 {
	 String catalogName = "headsrc_catalog.txt";
		PrintWriter writer = new PrintWriter(catalogName, "UTF-8");
		for (Entry<String,Offset> e1 : catalog.entrySet())
		{					
			writer.println(e1.getKey()+" "+e1.getValue().start+" "+ e1.getValue().end);					
		}
		writer.close();
 }
	public static void getUrl(File file) throws IOException
	{
		
		String str = readFile(file);
//		Document doc = Jsoup.parse(str, "UTF-8");
//		 Elements docs = doc.getElementsByTag("DOC");
//		 for(Element d : docs)
//		 {
			// System.out.println("here");
//			String url = d.getElementsByTag("URL").first().text();
//			if(url!=null)
//				urls.add(url);
//			System.out.println(urls.size());
			

			Pattern pattern = Pattern.compile("<DOC>(.*?)</DOC>");
			Pattern pattern7 = Pattern.compile("<URL>(.*?)</URL>");
			Matcher matcher = pattern.matcher(str);	
			ArrayList<String> docs = new ArrayList<String>();
			while (matcher.find())
			{
				
				docs.add(matcher.group(1));
			}
			
			for(String doc : docs)
			{
			
			Matcher url1 = pattern7.matcher(doc);	
			while(url1.find())
			{
				String url= url1.group(1);
				urls.add(url);
				System.out.println(urls.size());
			}
		 }
	}
	
	
	public static headersource getHTMLSource(String url) {

		//checkDomainDelay(url);
		System.out.println(count++);
		URL url1;
		String headers = "";
		try {
			url1 = new URL(url);
			URLConnection conn = url1.openConnection();
			if(null == conn)
				return null;
			if(null!= conn.getHeaderFields())
				headers = conn.getHeaderFields().toString();
			else
				headers = "NO HEADER DATA";
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder htmlSource = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				htmlSource.append(inputLine);
			in.close();

			headersource data = new headersource(htmlSource.toString(), headers);
			map.put(url, data);
			return data;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NullPointerException e){
			return null;
		} catch(Exception e){
			return null;
		}

		return null;

	}
	
	
	private static void dumpToFile() {

		long seekOffSet = 0;
		String delimiter = "";
		try {
			RandomAccessFile raf = new RandomAccessFile("headersource.txt", "rw");
			for (Entry<String, headersource> e : map.entrySet()) {
				long startOffSet = seekOffSet;

				raf.seek(seekOffSet);
				StringBuilder sb = new StringBuilder();
				sb.append(delimiter);
				delimiter = System.lineSeparator();
				sb.append(e.getValue().header);
				sb.append("SANJANA_MANOJ_KUMAR");
				sb.append(e.getValue().source);

				raf.writeBytes(sb.toString());
				seekOffSet = raf.getFilePointer();
				long endOffSet = seekOffSet;

				catalog.put(e.getKey(), new Offset(startOffSet, endOffSet));

			}
			raf.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public static String readFile(File file) throws IOException
	{
		System.out.println("reading full file");
		StringBuilder str = new StringBuilder();
		//String str = "";
		String n = "./corpus/"+file.getName().toString();
		BufferedReader br = new BufferedReader(new FileReader(n));
		
		String line = br.readLine();
		
		while(line!=null)
		{
			str.append(line);
			line=br.readLine();
		}
		br.close();
		System.out.println("returning string");
		return str.toString();	
	}
}
