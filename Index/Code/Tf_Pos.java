package IR.assn2;

import java.util.ArrayList;

public class Tf_Pos
{
	//public String docid;
	public int tf;
	public ArrayList<Integer> pos = new ArrayList<Integer>();;
	public Tf_Pos(int freq, ArrayList<Integer> position )
	{
	//	docid = id;
		tf= freq;
		pos.addAll(position);
	}
	
//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		StringBuffer buf = new StringBuffer();
//		buf.append("tf : " + tf + " pos : ");
//		for(Integer i  : pos){
//			buf.append(" " + i);
//		}
//		return buf.toString();
//	}
}
