package IR.assn6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class test {
public static void main(String[] args)
{
	HashMap<Integer,HashMap<Integer,Double>> okapi = new HashMap<Integer,HashMap<Integer,Double>>();
	HashMap<Integer, Double> map = new HashMap<Integer, Double>();
	map.put (1, 6.6 );
	map.put (2, 7.7);
	okapi.put(5, map);
	ArrayList<Double> list = new ArrayList<Double>(okapi.get(5).values());
	//List<Double> list = new ArrayList<Double>(upper.get(5).values());
	System.out.println(list.get(okapi.get(5).size()-1));
	for (Double s : list) {
	    System.out.println(s);
	}
}
}
