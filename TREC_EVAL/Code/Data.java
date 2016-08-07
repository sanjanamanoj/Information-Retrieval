package IR.assn5;

public class Data 
{
	public  int ret;
	public  float relret;
	public  float totRel;
	public float avgPrec;
	public float Rprec;
	public Data(int retrieved, float rel,float totalRel, float avg,float r )
	{
		//System.out.println("relret;"+rel);
		ret=retrieved;
		relret=rel;
		totRel=totalRel;
		avgPrec=avg;
		Rprec = r;
	}

}
