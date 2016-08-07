package IR.assn3;

public class inlink_time 
{
	public String url;
	public int inlink;
	public long time;
	public inlink_time(String link, int indegree, long waitInQueue)
	{
		url = link;
		inlink = indegree;
		time = waitInQueue;
	}
	
	// override equals and hashcode()
	
	public static void main(String[] args) {
		inlink_time  t = new inlink_time("a", 1, 1);
		inlink_time  t2 = new inlink_time("a", 1, 1);
		
		System.out.println("a " + t.equals(t2));
	}
	
}
