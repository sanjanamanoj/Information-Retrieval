package IR.assn7;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class UnigramSpamDetection
{
	public static HashMap<String,Double> trainingPrediction = new HashMap<String,Double>();
	public static HashMap<String,Double> testingPrediction = new HashMap<String,Double>();

	public static void main(String[] args) throws IOException
	{
		//int n = getN("Part1/map.txt");
		int n = 508;
		int l = getL("Part1/train.txt");
		double[] y = getY(l,"Part1/train.txt");
		Feature[][] x = getX(l,"Part1/train.txt");
		regression(l,n,y,x);
		//printFile("Part2/unigramTrainPrediction.txt",trainingPrediction);
		//printFile("Part2/unigramTestPrediction.txt",testingPrediction);
		
	}

	public static void printFile(String filename,HashMap<String,Double> map) throws IOException
	{
	
		File file =new File(filename);
		if(!file.exists())
		{
    	 	file.createNewFile();
    	 	
		}
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		LinkedHashMap<String,Double> sorted = new LinkedHashMap<String,Double>();
		sorted.putAll(ranking(map));
		int rank=1;
		for(Entry<String,Double> e : sorted.entrySet())
		{
			
			
				
				pw.println(e.getKey() + " Q0 "  + rank + " " + e.getValue() + " " + "Exp");
				rank++;
			
		}
		
    	pw.close();
		System.out.println("written to output");
	}
	public static LinkedHashMap<String, Double> ranking (HashMap<String, Double> map) 
	{
		List<HashMap.Entry<String,Double>> list =
	            new LinkedList<HashMap.Entry<String, Double>>(map.entrySet() );
	        Collections.sort( list, new Comparator<Map.Entry<String,Double>>()
	        {
	            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
	            {
	                return Double.compare(o2.getValue(), o1.getValue());
	            }
	        } );

	        LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
	        for (Map.Entry<String, Double> entry : list)
	        {
	            result.put( entry.getKey(), entry.getValue() );
	        }
	        
	        
		return result;
	}
	public static void regression(int l,int n, double[] y,Feature[][] x) throws IOException
	{
		Problem problem = new Problem();
		problem.l = l; // number of training examples
		problem.n = n; // number of features
		problem.x = x;  // feature nodes
		problem.y = y; // target values
		System.out.println(problem.y.length);
		
		//System.out.println(y);
		SolverType solver = SolverType.L2R_LR; // -s 0
		double C = 1.0;    // cost of constraints violation
		double eps = 0.01; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		//System.out.println(parameter.getEps());
		Model model = Linear.train(problem, parameter);
		File modelFile = new File("model");
		model.save(modelFile);
		// load model or use it directly
		//createPredictionMap(modelFile,model,"Part3/UnigramTrainMap-docno.txt",trainingPrediction);
		//createPredictionMap(modelFile,model,"Part3/UnigramTestMap-docno.txt",testingPrediction);
	
		
	}
	
	
	public static double predict(File modelFile, Model model,Feature[]instance) throws IOException
	{
		model = Model.load(modelFile);

		//Feature[] instance = { new FeatureNode(1, 4), new FeatureNode(2, 2) };
		double prediction = Linear.predict(model, instance);
		//System.out.println("prediction:"+prediction);
		return prediction;
	}
	
	
	
	public static void createPredictionMap(File modelFile, Model model, String filename, HashMap<String,Double>map) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			String docno = temp[0].trim();
			int label = Integer.parseInt(temp[1].trim());
			
			int size = temp.length - 2;
			Feature[] instance = new Feature[size];
			for(int i =2;i<temp.length;i++)
			{
				String[] temp1 = temp[i].split(":");
				FeatureNode ok =new FeatureNode(Integer.parseInt(temp1[0].trim()),Integer.parseInt(temp1[1].trim()));
				instance[i-2] = ok;
			}
			double prediction = predict(modelFile,model,instance);
			map.put(docno, prediction);
			line = br.readLine();
		}
		br.close();
			
	}
	
	
	
	public static int getN(String filename) throws IOException
	{
		System.out.println("getting n");
		int count = 0;
 		BufferedReader br = new BufferedReader(new FileReader(filename));
 		String line = br.readLine();
 		while(line!=null)
 		{
 			count++;
 			line = br.readLine();
 		}
 		br.close();
 		System.out.println("returning n");
 		return count;
 		
	}
	
	public static int getL(String filename) throws IOException
	{
		System.out.println("getting l");
		int count = 0;
 		BufferedReader br = new BufferedReader(new FileReader(filename));
 		String line = br.readLine();
 		while(line!=null)
 		{
 			count++;
 			line = br.readLine();
 		}
 		br.close();
 		System.out.println("returning l");
 		return count;
	}
	
	public static double[] getY(int l,String filename) throws IOException
	{
		System.out.println("getting y");
		double[] y = new double[l];
		int count =0;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");		
			int label = Integer.parseInt(temp[0].trim());			
			y[count] = label;
			count++;
			line = br.readLine();			
		}
		br.close();
		System.out.println("returning y");
		return y;
	
	}
	
	
	public static Feature[][] getX(int l,String filename) throws IOException
	{
		System.out.println("getting x");
		int count = 0;
		Feature[][] x = new Feature[l][];
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");		
			int label = Integer.parseInt(temp[0].trim());			
			int size = temp.length - 1;			
			x[count] =  new Feature[size];
			for(int i =1;i<temp.length;i++)
			{
				String[] temp1 = temp[i].split(":");
				FeatureNode ok =new FeatureNode(Integer.parseInt(temp1[0].trim()),Integer.parseInt(temp1[1].trim()));
				x[count][i-1] = ok;
			}			
			count++;
			line = br.readLine();			
		}
		br.close();
		System.out.println("returning x");
		return x;
	}
}
