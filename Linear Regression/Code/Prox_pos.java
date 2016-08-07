package IR.assn6;



import java.util.ArrayList;

public class Prox_pos 
{
	public String term;
	public ArrayList<Integer> pos = new ArrayList<Integer>();
	public Prox_pos(String text, ArrayList<Integer> positions)
	{
		term = text;
		pos = new ArrayList<Integer>();
		pos.addAll(positions);
	}
}
