package IR.assn6;

public class Features 
{
	public double okapitf;
	public double tfidf;
	public double bm25;
	public double laplaceSmoothing;
	public double jelinek;
	public double proximity;
	public int label;
	public Features(int l,double otf,double tf,double bm,double smooth,double jel,double prox)
	{
		label = l;
		okapitf = otf;
		tfidf = tf;
		bm25 = bm;
		laplaceSmoothing = smooth;
		jelinek = jel;
		proximity = prox;
	}
	

}
