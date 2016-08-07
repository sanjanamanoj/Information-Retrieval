package IR.assn1;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;

public class LaplaceJelinek {

	private static Properties lengthProperties = new Properties();

	public static double jelinek(int freq, int length, long vocab, long sum_num)
			throws UnknownHostException, FileNotFoundException {
		double firstTerm = 0;
		double lambda = 0.7;
		if (freq != 0) {
			firstTerm = (lambda * (Double.valueOf(freq) / length));
		}
		sum_num -= freq;
		double sum_den = (247 * 84678) - length;
		double secondTerm = ((1 - lambda) * (sum_num / sum_den));
		// System.out.println(secondTerm);
		double jelinek = Math.log10(firstTerm + secondTerm);
		return jelinek;
	}

	public static void matchingScore(int qno, HashMap<String, Integer> len, HashMap<String, ArrayList<Tf_index>> tf,
			long vocab, ArrayList<String> docnos, HashMap<String, Long> sumTf) throws IOException {
		lengthProperties.load(new FileInputStream("docLength.properties"));

		HashMap<String, Double> matchingScore = new HashMap<String, Double>();
		for (Entry<String, ArrayList<Tf_index>> e : tf.entrySet()) {
			ArrayList<String> alldoc = new ArrayList<String>();
			alldoc.addAll(docnos);
			System.out.println("All size: " + alldoc.size());
			ArrayList<Tf_index> doc_termFreq = e.getValue();
			long sum_num = sumTf.get(e.getKey());
			System.out.println(sum_num);
			int dfw = doc_termFreq.size();
			for (Tf_index i : doc_termFreq) {
				alldoc.remove(i.docid);

				// int length = len.get(i.docid);
				int length = 0;
				try {
					length = Integer.valueOf(lengthProperties.getProperty(i.docid.trim()));
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println(i.docid);
					System.out.println(length);
					length = 0;
				}
				double score = jelinek((int) i.freq, length, vocab, sum_num);
				// double score = Math.log10(jelinek);
				if (!matchingScore.containsKey(i.docid)) {
					matchingScore.put(i.docid, score);
				} else {
					double temp = matchingScore.get(i.docid);
					temp += score;
					matchingScore.put(i.docid, temp);
				}
			}
			for (String unrelated : alldoc) {
				int length = 0;
				try {
					length = Integer.valueOf(lengthProperties.getProperty(unrelated.trim()));
				} catch (Exception e1) {
					// e1.printStackTrace();
					System.out.println(unrelated);
					System.out.println(length);
					// continue;
					length = 0;
				}
				if (!matchingScore.containsKey(unrelated)) {
					// int length = len.get(unrelated);
					double score = jelinek(0, length, vocab, sum_num);
					// double score = Math.log10(a);
					matchingScore.put(unrelated, score);
				} else {
					// int length = len.get(unrelated);

					double score = jelinek(0, length, vocab, sum_num);
					double temp = matchingScore.get(unrelated);
					temp += score;
					matchingScore.put(unrelated, temp);
				}
			}
			System.out.println("size:" + matchingScore.size());
		}

		int rank = 1;
		TreeMap<String, Double> output = ranking(matchingScore);
		String fileName = "jelinek_output.txt";
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();

		}
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		for (Entry<String, Double> e : output.entrySet()) {
			if (rank > 1000) {
				break;
			}
			pw.println(qno + " Q0 " + e.getKey() + " " + rank + " " + e.getValue() + " " + " Exp");
			rank++;
		}

		pw.close();
		System.out.println(qno + "written to output");

	}

	public static TreeMap<String, Double> ranking(HashMap<String, Double> map) {
		ValueComparator vc = new ValueComparator(map);
		TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}

}
