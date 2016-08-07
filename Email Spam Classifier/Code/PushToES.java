package IR.assn7;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

//This program is used to push the dataset to Elastic Search with fields
// text: this is the actual text of the email
// spam: the yes or no represents if it is spam or not
// type: if it will be in the train or test set
// docno: the name of the email

public class PushToES
{
	public static int id =0;
	public static int spamCount = 0;
	public static int hamCount = 0;
	public static HashMap<String,String> spamMap = new HashMap<String,String>();
	public static int trainSize;
	public static int testSize;
	public static int spamInTrain;
	public static int hamInTrain;
	public static void main(String[] args) throws IOException
	{
		Settings settings = Settings.builder().build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		readSpamMap("trec07p/full/index");
		 trainSize = (int) (spamMap.size()* 0.8);
		 testSize = spamMap.size() - trainSize;
		 spamInTrain =  (int) ( trainSize * 0.67);
		 hamInTrain = trainSize - spamInTrain;
		 readAllFiles("trec07p/data",client);
	}

	public static void readSpamMap(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			String[] temp2 = temp[1].split("/");
			//System.out.println(temp2[2].trim()+" "+ temp[0].trim());
			spamMap.put(temp2[2].trim(), temp[0].trim());
			line= br.readLine();
		}
		System.out.println(spamMap.size());
		br.close();

	}


	public static void readAllFiles(String target_dir,Client client) throws IOException
	{
		File dir = new File(target_dir);
        File[] files = dir.listFiles();
       	for (File file : files)
		{
			processFile(file , client);
		}
	}

	public static void  processFile(File file,Client client) throws IOException
	{
		id++;
		String type;
		String spam;
		String n = "./trec07p/data/"+file.getName().toString();
		BufferedReader br = new BufferedReader(new FileReader(n));
		StringBuilder str = new StringBuilder();
		String line = br.readLine();
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
				 .replace("{", "")
				 .replace("}", "")
				 .replace("*", "")
				 .replace("&", "")
				 .replace("^", "")
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

		if(spamMap.get(file.getName()).equals("spam"))
		{

			spam ="yes";
			if(spamCount<spamInTrain)
			{
				type = "train";
				spamCount++;
			}
			else
				type ="test";
		}

		else
		{

			spam="no";
			if(hamCount<hamInTrain)
			{
				type = "train";
				hamCount++;
			}
			else
				type ="test";
		}


	    XContentBuilder builder = XContentFactory.jsonBuilder()
	    	    .startObject()
	    	    	.field("docno",file.getName())
	    	    	.field("id",id)
	    	        .field("spam", spam)
	    	        .field("text", l)
	    	        .field("type",type);
	    IndexResponse response = client.prepareIndex("spams", "document", ""+id)
                .setSource(builder)
                .execute()
                .actionGet();

	}

}
