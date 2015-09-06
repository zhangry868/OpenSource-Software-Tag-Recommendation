import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;


public class Tags 
{
	static Scanner input;
	static HashMap<String, Integer> Tags = new HashMap<String, Integer>();
	static int index = 0;
	public static String Header = "";
	public static String labelHeader = "";
	static void init()
	{
			File file = new File("./allTags.txt");
			try {
				input = new Scanner(file,"utf-8");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String temp;
			Tags.clear();
			if(input.hasNext())
			{	
				temp = input.nextLine();
				Tags.put(temp, index);
			}
			while(input.hasNext())
			{
				temp = input.nextLine();
				Header = Header + "@attribute " + temp.replaceAll(" ", "_") +" {0,1}\n";
				labelHeader += "<label name=\""+ temp.replaceAll(" ", "_") + "\"></label>" + "\n";
				Tags.put(temp, index);
				//System.out.println(temp);
				index ++;
			}
			input.close();
	}
	static HashMap<String, Integer> getTags()
	{
		return Tags;
	}
}
