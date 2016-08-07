package IR.assn2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.PorterStemmer;

public class regexTest 
{
	public static void main(String[] srgs)
	{
		
	
	String str = "The celluloid a got_torch h.a.s _ U.s.S. J.J.S. 12.3.7 Services been? ..passed!?";
	Pattern pattern = Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher matcher = pattern.matcher(str);

	EnglishStemmer ps = new EnglishStemmer();
	while (matcher.find()) {
		 System.out.println("term " + matcher.group());
		
			ps.setCurrent(matcher.group().toLowerCase());
			ps.stem();
			System.out.println("stem term " + ps.getCurrent());
	}
	
	String[] split = str.split("[A-Za-z0-9]+.?[A-Za-z0-9]+");
	
	for(String s : split){
		System.out.println("s " + s);
		
	}

	}
}
