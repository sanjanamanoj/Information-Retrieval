package IR.assn7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class test2 
{
	public static void main(String[] args) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("inmail.10"));
		String line = br.readLine();
		StringBuilder str = new StringBuilder();
		while (line!=null)
		{
			str.append(line);
			if(line.endsWith("="))
			{
				str.append("");
			}
			else
				str.append("\n");
			line = br.readLine();
		}
		br.close();
		//System.out.println(str.toString());
		Document doc = Jsoup.parse(str.toString());
		Elements html = doc.getElementsByTag("html");
		//System.out.println(html.text());
		String text = html.text();
		
		String l = text
				 .replace(".", "")
				 .replace(",", "")
				 .replace("_", "")
				 .replace(":", "")
				 .replace("-", " ")
				 .replace("(", "")
				 .replace(")", "")
				 .replace("!", "")
				 .replace("#", "")
				 .replace("%", "")
				 .replace("/", "")
				 .replace("\"", "")
				 .replace("'", "")
				 .replace("<", "")
				 .replace(">", "")
				 .replace("[", "")
				 .replace("]", "")
				 .replace("@", "")
				 .replace("?", "")
				 .replace("=","")
				 .replace(";", "")
				 .replace("+","")
				 .replace("\t","")
				 .replaceAll(" +", " ");
		// l = l.replace("-","");
		System.out.println(l);
		//System.out.println(html.toString());
	}
}
