package IR.assn6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

public class LinearRegression 
{
	public static LinkedHashMap<Integer,LinkedHashMap<String,Features>> featureMap = new LinkedHashMap<Integer,LinkedHashMap<String,Features>>();
	public static LinkedHashMap<Integer,LinkedHashMap<String,Features>> trainingMap = new LinkedHashMap<Integer,LinkedHashMap<String,Features>>();
	public static LinkedHashMap<Integer,LinkedHashMap<String,Features>> testingMap = new LinkedHashMap<Integer,LinkedHashMap<String,Features>>();
	public static HashMap<Integer,HashMap<String,Double>> trainingPrediction = new HashMap<Integer,HashMap<String,Double>>();
	public static HashMap<Integer,HashMap<String,Double>> testingPrediction = new HashMap<Integer,HashMap<String,Double>>();

	
	public static void main(String[] args) throws IOException
	{
		//double[] a = {1,1};
		getFeatureMap("features.txt");
		splitFeatureMap();
		int l = getTrainingExampleSize();
		int testSize = getTestingExampleSize();
		Feature[][] x = getX(l);
		Feature[][] test = getTestX(testSize);
		double[] y = getY(l);
		regression(l,y,x,test);
		printTestingFile();
		printTrainingFile();
	}
	
	public static double[] getY(int l)
	{
		int count = 0;
		double[] y = new double[l];
		for(Entry<Integer,LinkedHashMap<String,Features>> e: trainingMap.entrySet())
		{
			for(Entry<String,Features> e1: e.getValue().entrySet())
			{
				y[count] = e1.getValue().label;
				count++;
			}
		}
		
		return y;
	}
	
	
	public static Feature[][] getX(int l)
	{
		int count = 0;
		Feature[][] x = new Feature[l][];
		for(Entry<Integer,LinkedHashMap<String,Features>> e: trainingMap.entrySet())
		{
			for(Entry<String,Features> e1: e.getValue().entrySet())
			{
				double[] temp = new double[6];
				temp[0] = e1.getValue().okapitf;
				temp[1] = e1.getValue().tfidf;
				temp[2] = e1.getValue().bm25;
				temp[3] = e1.getValue().laplaceSmoothing;
				temp[4] = e1.getValue().jelinek;
				temp[5] = e1.getValue().proximity;
					
				int size = 6;
				for(int i=0;i<6;i++)
				{
					if(temp[i]==0)
						size--;
				}
				x[count] = new Feature[size];
				FeatureNode ok =new FeatureNode(1,e1.getValue().okapitf);
				FeatureNode tf =new FeatureNode(2,e1.getValue().tfidf);
				FeatureNode bm =new FeatureNode(3,e1.getValue().bm25);
				FeatureNode sm =new FeatureNode(4,e1.getValue().laplaceSmoothing);
				FeatureNode jel =new FeatureNode(5,e1.getValue().jelinek);
				FeatureNode prox =new FeatureNode(6,e1.getValue().proximity);
				FeatureNode[] t = new FeatureNode[6];
				t[0] = ok;
				t[1] = tf;
				t[2] = bm;
				t[3] = sm;
				t[4] = jel;
				t[5] = prox;
				
				
				for(int i=0;i<size;i++)
				{
					if(temp[i]!=0)
						x[count][i] = t[i];
				}
				count++;
			}
		}
		return x;
	}
	
	public static void printTrainingFile() throws IOException
	{
		String fileName = "training.txt";
		File file =new File( fileName);
		if(!file.exists())
		{
    	 	file.createNewFile();
    	 	
		}
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		for(Entry<Integer,HashMap<String,Double>> e : trainingPrediction.entrySet())
		{
			int rank=1;
			LinkedHashMap<String,Double> sorted = new LinkedHashMap<String,Double>();
			sorted.putAll(ranking(e.getValue()));
			for(Entry<String,Double> e1 : sorted.entrySet())
			{
				
				pw.println(e.getKey() + " Q0 " + e1.getKey() + " " + rank + " " + e1.getValue() + " " + "Exp");
				rank++;
			}
		}
		
    	pw.close();
		System.out.println("written to output");
	}
	
	
	public static void printTestingFile() throws IOException
	{
		String fileName = "testing.txt";
		File file =new File( fileName);
		if(!file.exists())
		{
    	 	file.createNewFile();
    	 	
		}
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		for(Entry<Integer,HashMap<String,Double>> e : testingPrediction.entrySet())
		{
			int rank=1;
			LinkedHashMap<String,Double> sorted = new LinkedHashMap<String,Double>();
			sorted.putAll(ranking(e.getValue()));
			for(Entry<String,Double> e1 : sorted.entrySet())
			{
				
				pw.println(e.getKey() + " Q0 " + e1.getKey() + " " + rank + " " + e1.getValue() + " " + "Exp");
				rank++;
			}
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
	
	
	public static Feature[][] getTestX(int l)
	{
		int count = 0;
		Feature[][] x = new Feature[l][];
		for(Entry<Integer,LinkedHashMap<String,Features>> e: testingMap.entrySet())
		{
			for(Entry<String,Features> e1: e.getValue().entrySet())
			{
				double[] temp = new double[6];
				temp[0] = e1.getValue().okapitf;
				temp[1] = e1.getValue().tfidf;
				temp[2] = e1.getValue().bm25;
				temp[3] = e1.getValue().laplaceSmoothing;
				temp[4] = e1.getValue().jelinek;
				temp[5] = e1.getValue().proximity;
					
				int size = 6;
				for(int i=0;i<6;i++)
				{
					if(temp[i]==0)
						size--;
				}
				x[count] = new Feature[size];
				FeatureNode ok =new FeatureNode(1,e1.getValue().okapitf);
				FeatureNode tf =new FeatureNode(2,e1.getValue().tfidf);
				FeatureNode bm =new FeatureNode(3,e1.getValue().bm25);
				FeatureNode sm =new FeatureNode(4,e1.getValue().laplaceSmoothing);
				FeatureNode jel =new FeatureNode(5,e1.getValue().jelinek);
				FeatureNode prox =new FeatureNode(6,e1.getValue().proximity);
				FeatureNode[] t = new FeatureNode[6];
				t[0] = ok;
				t[1] = tf;
				t[2] = bm;
				t[3] = sm;
				t[4] = jel;
				t[5] = prox;
				
				
				for(int i=0;i<size;i++)
				{
					if(temp[i]!=0)
						x[count][i] = t[i];
				}
				count++;
			}
		}
		return x;
	}
	
	
	
	public static void regression(int l, double[] y,Feature[][] x,Feature[][] test) throws IOException
	{
		Problem problem = new Problem();
		problem.l = l; // number of training examples
		problem.n = 6; // number of features
		problem.x = x;  // feature nodes
		problem.y = y; // target values
		System.out.println(problem.y.length);
		
		//System.out.println(y);
		SolverType solver = SolverType.L2R_L1LOSS_SVR_DUAL; // -s 0
		double C = 1.0;    // cost of constraints violation
		double eps = 0.01; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		//System.out.println(parameter.getEps());
		Model model = Linear.train(problem, parameter);
		File modelFile = new File("model");
		model.save(modelFile);
		// load model or use it directly
		trainingModel(modelFile,model);
		testingModel(modelFile,model);
		
	}
	
	public static double predict(File modelFile, Model model,Feature[]instance) throws IOException
	{
		model = Model.load(modelFile);

		//Feature[] instance = { new FeatureNode(1, 4), new FeatureNode(2, 2) };
		double prediction = Linear.predict(model, instance);
		//System.out.println("prediction:"+prediction);
		return prediction;
	}
	
	public static void trainingModel(File modelFile, Model model) throws IOException
	{
		for(Entry<Integer,LinkedHashMap<String,Features>> e: trainingMap.entrySet())
		{
			int qid = e.getKey();
			for(Entry<String,Features> e1:e.getValue().entrySet())
			{
				String docid = e1.getKey();
				double[] temp = new double[6];
				temp[0] = e1.getValue().okapitf;
				temp[1] = e1.getValue().tfidf;
				temp[2] = e1.getValue().bm25;
				temp[3] = e1.getValue().laplaceSmoothing;
				temp[4] = e1.getValue().jelinek;
				temp[5] = e1.getValue().proximity;
					
				int size = 6;
				for(int i=0;i<6;i++)
				{
					if(temp[i]==0)
						size--;
				}
				Feature[] instance = new Feature[size];
				FeatureNode ok =new FeatureNode(1,e1.getValue().okapitf);
				FeatureNode tf =new FeatureNode(2,e1.getValue().tfidf);
				FeatureNode bm =new FeatureNode(3,e1.getValue().bm25);
				FeatureNode sm =new FeatureNode(4,e1.getValue().laplaceSmoothing);
				FeatureNode jel =new FeatureNode(5,e1.getValue().jelinek);
				FeatureNode prox =new FeatureNode(6,e1.getValue().proximity);
				FeatureNode[] t = new FeatureNode[6];
				t[0] = ok;
				t[1] = tf;
				t[2] = bm;
				t[3] = sm;
				t[4] = jel;
				t[5] = prox;
				for(int i=0;i<size;i++)
				{
					if(temp[i]!=0)
						instance[i] = t[i];
				}
				double prediction = predict(modelFile,model,instance);
				HashMap<String,Double> t1 = new HashMap<String,Double>();
				t1.put(docid, prediction);
				if(trainingPrediction.containsKey(qid))
				{
					t1.putAll(trainingPrediction.get(qid));
					trainingPrediction.put(qid,t1);
				}
				else
				{
					trainingPrediction.put(qid,t1);
				}
			}
		}
		System.out.println(trainingPrediction.size());
	}
	
	
	public static void testingModel(File modelFile, Model model) throws IOException
	{
		for(Entry<Integer,LinkedHashMap<String,Features>> e: testingMap.entrySet())
		{
			int qid = e.getKey();
			for(Entry<String,Features> e1:e.getValue().entrySet())
			{
				String docid = e1.getKey();
				double[] temp = new double[6];
				temp[0] = e1.getValue().okapitf;
				temp[1] = e1.getValue().tfidf;
				temp[2] = e1.getValue().bm25;
				temp[3] = e1.getValue().laplaceSmoothing;
				temp[4] = e1.getValue().jelinek;
				temp[5] = e1.getValue().proximity;
					
				int size = 6;
				for(int i=0;i<6;i++)
				{
					if(temp[i]==0)
						size--;
				}
				Feature[] instance = new Feature[size];
				FeatureNode ok =new FeatureNode(1,e1.getValue().okapitf);
				FeatureNode tf =new FeatureNode(2,e1.getValue().tfidf);
				FeatureNode bm =new FeatureNode(3,e1.getValue().bm25);
				FeatureNode sm =new FeatureNode(4,e1.getValue().laplaceSmoothing);
				FeatureNode jel =new FeatureNode(5,e1.getValue().jelinek);
				FeatureNode prox =new FeatureNode(6,e1.getValue().proximity);
				FeatureNode[] t = new FeatureNode[6];
				t[0] = ok;
				t[1] = tf;
				t[2] = bm;
				t[3] = sm;
				t[4] = jel;
				t[5] = prox;
				for(int i=0;i<size;i++)
				{
					if(temp[i]!=0)
						instance[i] = t[i];
				}
				double prediction = predict(modelFile,model,instance);
				HashMap<String,Double> t1 = new HashMap<String,Double>();
				t1.put(docid, prediction);
				if(testingPrediction.containsKey(qid))
				{
					t1.putAll(testingPrediction.get(qid));
					testingPrediction.put(qid,t1);
				}
				else
				{
					testingPrediction.put(qid,t1);
				}
			}
		}
		System.out.println(testingPrediction.size());
	}
	
	
	public static int getTrainingExampleSize()
	{
		int sum=0;
		for(Entry<Integer,LinkedHashMap<String,Features>> e: trainingMap.entrySet())
		{
			sum+=e.getValue().size();
		}
		return sum;
	}
	public static int getTestingExampleSize()
	{
		int sum=0;
		for(Entry<Integer,LinkedHashMap<String,Features>> e: testingMap.entrySet())
		{
			sum+=e.getValue().size();
		}
		return sum;
	}
	
	public static void getFeatureMap(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null)
		{
			String[] temp = line.split(" ");
			int qid = Integer.parseInt(temp[0].trim());
			String docid = temp[1].trim();
			double okapi = Double.parseDouble(temp[2].trim());
			double tfidf = Double.parseDouble(temp[3].trim());
			double bm25 = Double.parseDouble(temp[4].trim());
			double smoothing = Double.parseDouble(temp[5].trim());
			double jelinek = Double.parseDouble(temp[6].trim());
			double proximity = Double.parseDouble(temp[7].trim());
			int label = Integer.parseInt(temp[8].trim());

			Features f = new Features(label,okapi,tfidf,bm25,smoothing,jelinek,proximity);
			LinkedHashMap<String,Features> t = new LinkedHashMap<String,Features>();
			
			if(featureMap.containsKey(qid))
			{
				t.putAll(featureMap.get(qid));
				t.put(docid, f);
				featureMap.put(qid,t);
			}
			else
			{
				t.put(docid, f);
				featureMap.put(qid, t);
			}
					
			line=br.readLine();
			
		}
		br.close();
//		System.out.println("Number of documents in featureMap: " + featureMap.size());
//		int sum=0;
//		for(Entry<Integer, HashMap<String,Features>> e : featureMap.entrySet() )
//		{
//			System.out.println(e.getKey()+":"+e.getValue().size());
//			sum+=e.getValue().size();
//		}
//		System.out.println("sum:"+sum);
		
	}
	
	public static void shuffle()
	{
		
	}
	public static void splitFeatureMap()
	{
		int count = 1;
		List keys = new ArrayList(featureMap.keySet());
		Collections.shuffle(keys);
		for (Object o : keys) {
			if(count>20)
			{
				testingMap.put((Integer) o,featureMap.get(o));
			}
				
			else
			{
				trainingMap.put((Integer) o,featureMap.get(o));
			}
			
			count++;
		    // Access keys/values in a random order
		    //map.get(o);
		}
//		for(Entry<Integer,LinkedHashMap<String,Features>> e: featureMap.entrySet())
//		{
//			if(count>20)
//			{
//				testingMap.put(e.getKey(),e.getValue());
//			}
//				
//			else
//			{
//				trainingMap.put(e.getKey(),e.getValue());
//			}
//			
//			count++;
//		}
		//56, 57, 64, 71, 99
//		testingMap.put(56, featureMap.get(56));
//		testingMap.put(57, featureMap.get(57));
//		testingMap.put(64, featureMap.get(64));
//		testingMap.put(71, featureMap.get(71));
//		testingMap.put(99, featureMap.get(99));
		System.out.println(testingMap.size());
		System.out.println(trainingMap.size());
	}

}
