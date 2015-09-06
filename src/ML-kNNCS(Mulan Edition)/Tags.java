import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;


public class Tags 
{
	static Scanner input;
	static HashMap<Integer,String> Tags = new HashMap<Integer,String>();
	static int index = 0;
	public static String Header = "";
	static void init()
	{
			File file = new File("allTags.txt");
			try {
				input = new Scanner(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String temp;
			Tags.clear();
			temp = input.nextLine();
			Tags.put(index, temp);
			while(input.hasNext())
			{
				temp = input.nextLine();
				Header = Header + "@attribute " + temp.replaceAll(" ", "_") +" {0,1}\n";
				Tags.put(index, temp);
				//System.out.println(temp);
				index ++;
			}
			input.close();
	}
	static public String getTagAtIndex(int i)
	{
		return Tags.get(i);
	}
	static HashMap<Integer,String> getTags()
	{
		return Tags;
	}
}
