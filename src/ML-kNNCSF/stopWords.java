import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;

public class stopWords 
{
	static Scanner input;
	static HashSet<String> stopword = new HashSet<String>();
	static Stemmer stemmer2 = new Stemmer();
	static int index = 0;
	static void init()
	{
			File file = new File("./stopwords.txt");
			try {
				input = new Scanner(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stopword.clear();
			String temp;
			while(input.hasNext())
			{
				temp = input.nextLine();
				stopword.add(temp);
				index ++;
			}
			input.close();
	}
	static HashSet<String> getStopWordList()
	{
		return stopword;
	}
	static String DropStopWord(String str)
	{
		str = str.replaceAll("(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+])?", " ");
		str = str.replaceAll("is a ", " ");
		str = str.replaceAll("[^a-z]", " ");
		String[] strList = str.split("[ ]+");
		String processed = "";
		String temp;
		for(int i = 0; i < strList.length; i ++)
		{
			if(!stopword.contains(strList[i]))
			{	
				char[] stemch = strList[i].toCharArray();
				stemmer2.add(stemch, stemch.length);
				stemmer2.stem();
				temp = stemmer2.toString();
				//temp = stemmer.stem(strList[i]);
				//if(temp.length() < 3)
				//System.out.println(strList[i] + " -> " + temp);
				strList[i] = temp;
				processed = processed + " " + strList[i];
			}
		}
		
		return processed.trim();
	}
}
