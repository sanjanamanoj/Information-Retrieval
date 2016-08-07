package IR.assn5;





import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ValueComparator implements Comparator<String>
{
		Map<String, Double> map;
		 
	    public ValueComparator(HashMap<String, Double> base) {
	        this.map = base;
	    }
	 
	   

		public int compare(String a, String b) {
	        if (map.get(a) >= map.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys 
	    }
	
}
