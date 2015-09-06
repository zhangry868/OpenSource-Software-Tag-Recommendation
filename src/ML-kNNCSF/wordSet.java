import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Scanner;


public class wordSet 
{
	static Scanner input;
	static HashMap<String, Integer> word = new HashMap<String, Integer>();
	static HashMap<String, Integer> idfCount = new HashMap<String, Integer>();
	static HashMap<String, Double> idf = new HashMap<String, Double>();
	static int index = 0;
	public static String Header = "";
	static void init()
	{
			File file = new File("./word.txt");
			try {
				input = new Scanner(file,"utf-8");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String temp;
			word.clear();
			if(input.hasNext())
			{
				temp = input.nextLine();
				word.put(temp, index);
			}
			while(input.hasNext())
			{
				temp = input.nextLine();
				Header = Header + "@attribute " + temp.replaceAll(" ", "_") +" numeric\n";
				word.put(temp, index);
				index ++;
			}
			try {
				idf = DeserializePerson();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			input.close();
	}
	static HashMap<String, Integer> getTags()
	{
		return word;
	}
	
	static void SerializePerson() throws FileNotFoundException,IOException {
        // ObjectOutputStream 对象输出流，将Person对象存储到E盘的Person.txt文件中，完成对Person对象的序列化操作
        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(new File("./idf.txt")));
        oo.writeObject(idf);
        System.out.println("IDF对象序列化成功！");
        oo.close();
    }

    /**
     * MethodName: DeserializePerson 
     * Description: 反序列Perons对象
     * @author Rui-Yi ZHANG
     * @return
     * @throws Exception
     * @throws IOException
     */
    static HashMap<String, Double> DeserializePerson() throws Exception, IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                new File("./idf.txt")));
        HashMap<String, Double> person = (HashMap<String, Double>) ois.readObject();
        System.out.println("IDF对象反序列化成功！");
        return person;
    }
}
