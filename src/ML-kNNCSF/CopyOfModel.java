import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

//idf:hamming loss:0.015249449391937182 macro_f1:0.41059186173602663 micro_f1:0.1449664429530201
//old hamming loss:0.015608541606817964 macro_f1:0.41795819274191776 micro_f1:0.1260053619302949
//idf+Cosine:hamming loss:0.015464904720865652 macro_f1:0.43085867520852056 micro_f1:0.13404825737265416
public class CopyOfModel implements Serializable
{
	private double Smooth = 0.05;
	private int k;//K-NN
	private int M;//the words num; abuou 20k
	private int N;//the train data num
	int []tempK;
	int Q;// label num in pool
	private HashMap<Integer, Double>[] train_data;//M X N array, each instance stored in one column
	private int[][] train_target;// Q X N array if the ith training instance belongs to the jth class, then train_target(j,i) equals +1, otherwise train_target(j,i) equals 0
	private double[] Prior;//Q X 1
	private double[] PriorN;//Q X 1
	private double[][] Cond;//Q X (k + 1)
	private double[][] CondN;// Q X (k + 1)
	private Set<String> stopword;
	private Map<String, Integer> tagset;
	private Map<String, Integer> wordlist;
	private Map<String, Double> idf;
	private HashMap<Integer, Double> test_data;//test data
	private int[]test_target;// the test target  TODO
	public double Cost[];
	public static void init()
	{
		Tags.init();
		wordSet.init();
		stopWords.init();
	}
	public CopyOfModel(int k, double Smooth){
		Scanner input = null;
		try {
			N = 4043;
			//N = 4453;
			//Read Metric Related
			this.k = k;
			this.Smooth = Smooth;
			wordlist = wordSet.word;
			tagset = Tags.getTags();
			
			M = wordlist.size();
			Q = tagset.size();
			Cost = new double[Q];
			initmatrix();
			idf = wordSet.idf;
			
			//Read Data
			input = new Scanner(new File("./train.data"));
			int index = 0;
			String[] list = null;
			while(input.hasNext())
			{	//read data
				String temp = input.nextLine();
				list = temp.split("\\#\\$\\#");
				String[] label = list[2].split(",");
				list[1] = list[1].toLowerCase();
				list[1] = stopWords.DropStopWord(list[1]);
				String[] vec = list[1].split("[ ]+");
				double max = 0;
				for(String s:vec)
				{
					if(s != null && s.length() > 0)
					{
						try
						{
							Integer.valueOf(s);
						}
						catch(NumberFormatException e)
						{
							Integer idrow =  wordlist.get(s);	
							if(idrow == null)
							{
								//System.out.println("Not Found!!!");
								continue;
							}
							int row = idrow.intValue();
							Double old = train_data[index].get(row);
							//System.out.println(idf.get(s)/vec.length);
							Double valueDouble = idf.get(s);
							if(valueDouble == null)
								continue;
							if(old == null)
								train_data[index].put(row, valueDouble/vec.length);
							else{								
								train_data[index].put(row, old.doubleValue() + valueDouble/vec.length);
							}
						}
					}
				}
				
				for(String one:label)
				{
					Integer tagID = tagset.get(one);
					if(tagID != null)
					{
						int tagid = tagID.intValue();					
						train_target[tagid][index] = 1;
						//System.out.println(tagid);
					}
					else {
						System.out.println(one);
					}
				}
				index ++;
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(input!=null)
				input.close();
		}
	}
	
	public CopyOfModel(String modelfilename)
	{
		try {
			Scanner modelinput = new Scanner(new File(modelfilename));
			wordlist = new HashMap<String, Integer>();
			int len = modelinput.nextInt();
			String wordstr = modelinput.nextLine();
			//System.out.println(wordstr);
			wordstr = modelinput.nextLine();
			//System.out.println(wordstr);
			String[] words = wordstr.split(" ");
			for(int i = 0; i < len * 2; i += 2)
			{
				wordlist.put(words[i+1],Integer.valueOf(words[i]));
			}
			String temp = modelinput.next();
			Smooth = modelinput.nextDouble();
			temp = modelinput.next();//M
			M = modelinput.nextInt();
			temp = modelinput.next();//N
			N = modelinput.nextInt();
			temp = modelinput.next();//k
			k = modelinput.nextInt();
			temp = modelinput.next();//Q
			Q = modelinput.nextInt();
			temp = modelinput.nextLine();
			temp = modelinput.nextLine();//target set:
			temp = modelinput.nextLine();
			String[] targets = temp.split("\\#");
			tagset = new HashMap<String, Integer>();
			for(String s:targets){
				String[] tag = s.split(",");
				tagset.put(tag[0], Integer.valueOf(tag[1]));
			}
			initmatrix();//��ʼ������
			temp = modelinput.nextLine();//train_data
			for(int i = 0; i < N; i++){
				String line = modelinput.nextLine();
				String[] cells = line.split(" ");
				for(String s:cells){
					String[] entry = s.split(",");
					train_data[Integer.valueOf(entry[0])].put(Integer.valueOf(entry[1]), Double.valueOf(entry[2]));
				}
			}
			
			temp = modelinput.nextLine();//train_target
			for(int i = 0; i < N; i++){
				String oneline = modelinput.nextLine();
				String[] tags = oneline.split(" ");
				for(String s:tags){
					this.train_target[Integer.valueOf(s)][i] = 1;
				}
			}
			
			temp = modelinput.nextLine();//Prior
			temp = modelinput.nextLine();
			String[] po = temp.split(" ");
			for(int i = 0;i < Q; i ++){
				Prior[i] = Double.valueOf(po[i]);
			}
			
			temp = modelinput.nextLine();//PriorN
			temp = modelinput.nextLine();
			String[] pon = temp.split(" ");
			for(int i = 0;i < Q; i ++){
				PriorN[i] = Double.valueOf(pon[i]);
			}
			
			temp = modelinput.nextLine();//Cond
			for(int i = 0; i < Q; i++){
				String s = modelinput.nextLine();
				String[] line = s.split(" ");
				int j = 0;
				for(String st:line){
					Cond[i][j++] = Double.valueOf(st);
				}
			}
			temp = modelinput.nextLine();//CondN
			for(int i = 0; i < Q; i++){
				String s = modelinput.nextLine();
				String[] line = s.split(" ");
				int j = 0;
				for(String st:line){
					CondN[i][j++] = Double.valueOf(st);
				}
			}
			
			System.out.println("Model read end M:" + M + " N:" + N + " k:" + k + " Q:" + Q + " Smooth:" + Smooth);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void initmatrix(){
		train_data = new HashMap[N];
		for(int i = 0; i < N; i ++)
		{
			train_data[i] = new HashMap<Integer, Double>();
		}
		train_target = new int[Q][N];
		Prior = new double[Q];
		PriorN = new double[Q];
		Cond = new double[Q][k+1];
		CondN = new double[Q][k+1];
		tempK = new int[Q];
		System.out.println(Q);
		for(int i = 0; i < Q; i++)
		{
			Prior[i] = 0;
			PriorN[i] = 0;
			for(int j = 0; j < N; j++)
			{
				train_target[i][j] = 0;
			}
			for(int j = 0; j <= k; j++)
			{
				Cond[i][j] = 0;
				CondN[i][j] = 0;
			}
		}
	}
	
	void test(String testdatafile, String ouputfile){//prepare T test_data & the space of test_target
		test_data = new HashMap<Integer, Double>();
		test_target = new int[Q];
		String[] tags = new String[Q];
		for(String s:tagset.keySet()){
			tags[tagset.get(s)] = s;
		}
		try {
			Scanner testinput = new Scanner(new File(testdatafile));
			DataOutputStream testoutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ouputfile)));
			int index = 0;
			while(testinput.hasNext()){
				
				test_data.clear();
				for(int i = 0; i < Q; i++){
					test_target[i] = 0;
				}
					
				String line = testinput.nextLine();
				String[] list = line.split("#\\$#");
				list[1] = list[1].toLowerCase();
				list[1] = stopWords.DropStopWord(list[1]);
				String[] vec = list[1].split("[ ]+");
				double max = 0;
				for(String s:vec)
				{
					if(s != null && s.length() > 0)
					{
						try
						{
							Integer.valueOf(s);
						}
						catch(NumberFormatException e)
						{
							Integer idrow =  wordlist.get(s);	
							if(idrow == null)
								continue;
							int row = idrow.intValue();
							Double old = test_data.get(row);
							if(old == null)
								test_data.put(row, 1.0);//idf.get(s)/vec.length);
							else{								
								test_data.put(row, old.doubleValue() + 1.0);//+ idf.get(s)/vec.length);
							}
						}
					}
				}
				
				this.pridictnow();
				//TODO write a entry out
				testoutput.writeBytes(list[0] + "#$#");
				for(int i = 0; i < Q; i++)
				{
					if(test_target[i] == 1)
						testoutput.writeBytes(tags[i] + ",");
				}
				testoutput.writeBytes("\n");
				//testoutput.writeBytes(list[0] + "#$#");
				//testoutput.writeBytes(list[2]);
				//testoutput.writeBytes("\n");
			}
			testoutput.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pridictnow(){
		double[] dist_matrix = new double[N];
		for(int j = 0; j < N; j++)
		{
			double dist = 0;
			for(Integer sum:test_data.keySet()){
				Double d2 = train_data[j].get(sum);
				if(d2 == null)
					dist += test_data.get(sum) * test_data.get(sum);
				else
					dist += (test_data.get(sum) - d2) * (test_data.get(sum) - d2);
			}
			for(Integer sum:train_data[j].keySet()){
				Double d1 = test_data.get(sum);
				if(d1 == null)
					dist += train_data[j].get(sum) * train_data[j].get(sum);
			}
			dist = Math.sqrt(dist);
			dist_matrix[j] = dist;
		}
		double[] Neighbors_dist = new double[k];
		int[] Neighbors_index = new int[k];
		for(int j = 0; j < k; j++){
			Neighbors_dist[j]= 100000;//a large num
		}
		for(int c = 0; c < N; c ++)
		{
			int max = 0;
			for(int t = 0; t < k; t++)
			{
				if(Neighbors_dist[t] > Neighbors_dist[max])
					max = t;
			}
			if(dist_matrix[c] < Neighbors_dist[max]){
				Neighbors_dist[max] = dist_matrix[c];
				Neighbors_index[max] = c;
			}
		}
		
		for(int m = 0; m < k; m ++)
		{
			double mindist = Neighbors_dist[m];
			int min = m;
			for(int l = m; l < k; l ++)
			{
				if(mindist > Neighbors_dist[l])
				{
					min = l;
					mindist = Neighbors_dist[l];
				}
			}
			int tempid = Neighbors_index[m];
			Neighbors_index[m] = Neighbors_index[min];
			Neighbors_index[min] = tempid;
			
			double tempdist = Neighbors_dist[m];
			Neighbors_dist[m] = Neighbors_dist[min];
			Neighbors_dist[min] = tempdist;
		}
		
		//to predict
		double[] outpri = new double[Q];
		int[] temp = new int[Q];
		for(int i = 0; i < Q; i++){
			temp[i] = 0;
			for(int t = 0; t < k; t++)//tempK[i]
			{
				temp[i] += train_target[i][Neighbors_index[t]];
			}
		}
		for(int i = 0; i < Q; i++){
			double Prob_in = Prior[i] * Cond[i][temp[i]];
			double Prob_out = PriorN[i] * CondN[i][temp[i]];
			if((Prob_in + Prob_out) == 0)
				outpri[i] = Prior[i];
			else
			{	
				outpri[i] = Cost[i] * Prob_in/(Cost[i] * Prob_in + Prob_out);
			}
		}
		//
		int coun = 0;
		for(int i = 0; i < Q; i++)
		{
			if(outpri[i] < 0.5)
			{
				test_target[i] = 0;
			}
			else{
				test_target[i] = 1;
				coun ++;
			}
		}
		if(coun < 3)
		{
			int[] max3= new int[3];
			max3[0] = 0;
			max3[1] = 1;
			max3[2] = 2;
			double[] max3probi = new double[3];
			Arrays.fill(max3probi, 0.0);
			max3probi[0] = outpri[0];
			max3probi[1] = outpri[1];
			max3probi[2] = outpri[2];
			int minm = 0;
			
			for(int i = 0; i < Q; i++){
				for(int l = 0; l < 3; l++){
					if(max3probi[minm] > max3probi[l])
					{
						minm = l;
					}
				}
				if(outpri[i] > max3probi[minm])
				{
					max3probi[minm] = outpri[i]; 
					max3[minm] = i;
				}
			}
			//System.out.println(max3[0]+","+max3[1]+","+max3[2]);
			for(int i = 0; i < 3; i ++)
			{
				//if(max3probi[i] > 0.25)
					test_target[max3[i]] = 1;
			}
		}

	//	System.out.println();
	}
	
	
	public void train(){
		double[][] dist_matrix = new double[N][N];
		
		//computing Prior & PriorN
		for(int i = 0; i < Q; i++){
			double temp_Ci = 0;
			for(int j = 0; j < N; j++){
				temp_Ci += train_target[i][j];
			}
			Prior[i] = (Smooth + temp_Ci)/(Smooth * 2 + N);
			PriorN[i] = 1 - Prior[i];
			Cost[i] = Math.log10((Smooth * 2 + N)/(Smooth + temp_Ci));
			//tempK[i] = (int) (k/(Math.log10((Smooth * 2 + N)/(Smooth + temp_Ci))/Math.log10(5))-1);
			//System.out.println(tempK[i]);
		}
		
		
		//computing distance
		for(int i = 0; i < N; i++){
			//if(i % 1000 == 0)
			//	System.out.println("computing distance for instance: " + i);
			for(int j = i + 1; j < N; j++){
				double dist = 0;
				double len1 = 0;
				double len2 = 0;
				for(Integer sum:train_data[i].keySet())
				{
					len1 += Math.pow(train_data[i].get(sum), 2);
					Double d2 = train_data[j].get(sum);
					if(d2 == null)
						dist += 0;//train_data[i].get(sum) * train_data[i].get(sum);
					else
					{	
						dist += (train_data[i].get(sum) * d2); //* (train_data[i].get(sum) - d2);
						len2 += Math.pow(d2.doubleValue(), 2);
					}
				}
				for(Integer sum:train_data[j].keySet())
				{
					Double d1 = train_data[i].get(sum);
					if(d1 == null)
					{	
						//dist += 0;//train_data[j].get(sum) * train_data[i].get(sum);
						//dist += (train_data[j].get(sum) * d1);
						len2 += Math.pow(train_data[j].get(sum), 2);
					}
				}
				dist = dist/(Math.sqrt(len1)*Math.sqrt(len2));
				dist_matrix[i][j] = 1-dist;
				dist_matrix[j][i] = 1-dist;
				/*for(Integer sum:train_data[i].keySet()){
					Double d2 = train_data[j].get(sum);
					if(d2 == null)
						dist += train_data[i].get(sum) * train_data[i].get(sum);
					else
						dist += (train_data[i].get(sum) - d2) * (train_data[i].get(sum) - d2);
				}
				for(Integer sum:train_data[j].keySet()){
					Double d1 = train_data[i].get(sum);
					if(d1 == null)
						dist += train_data[j].get(sum) * train_data[j].get(sum);
				}
				dist = Math.sqrt(dist);
				dist_matrix[i][j] = dist;
				dist_matrix[j][i] = dist;*/
			}
			dist_matrix[i][i] = 10000;//a large num
		}
		
		//computing Cond & condN
		int[][] Neighbors = new int[k][N];//used to store the index of ith instance's neighbors in Negihbors[][i];
		double[][] Neighbors_dist = new double[k][N];//
		//find neighbors
		
		//Find K Nearest Neighbour
		for(int i = 0; i < N; i++)
		{
			for(int j = 0; j < k; j++){
				Neighbors_dist[j][i] = 100000;//a large num
			}			
			for(int c = 0; c < N; c ++)
			{
				int max = 0;
				for(int t = 0; t < k; t++)
				{
					if(Neighbors_dist[t][i] > Neighbors_dist[max][i])
						max = t;
				}
				if(dist_matrix[c][i] < Neighbors_dist[max][i])
				{
					Neighbors_dist[max][i] = dist_matrix[c][i];
					Neighbors[max][i] = c;
				}				
			}
			
			int min = 0;
			double mindist = 1000;
			for(int m = 0; m < k; m ++)
			{
				mindist = Neighbors_dist[m][i];
				min = m;
				for(int l = m; l < k; l ++)
				{
					if(mindist > Neighbors_dist[l][i])
					{
						min = l;
						mindist = Neighbors_dist[l][i];
					}
				}
				int tempid = Neighbors[m][i];
				Neighbors[m][i] = Neighbors[min][i];
				Neighbors[min][i] = tempid;
				
				double tempdist = Neighbors_dist[m][i];
				Neighbors_dist[m][i] = Neighbors_dist[min][i];
				Neighbors_dist[min][i] = tempdist;
			}
			//for(int j = 0; j < k; j ++)
			//	System.out.print(Neighbors[j][i]+" "+Neighbors_dist[j][i]);
			//System.out.println();
		}
		
		//Calculate CondProb
		double[][] temp_Ci = new double[Q][k+1];
		double[][] temp_NCi = new double[Q][k+1];
		for(int i = 0; i < Q; i++){
			for(int j = 0; j <= k; j++){
				temp_Ci[i][j] = 0;
				temp_NCi[i][j] = 0;
			}
		}
		for(int i = 0; i< N; i++){
			//
			int[] temp = new int[Q];			
			for(int j = 0; j < Q; j ++){
				temp[j] = 0;
				for(int t = 0; t < k; t ++)//tempK[j]
				{
					if(train_target[j][Neighbors[t][i]] == 1)
						temp[j] += 1;
				}
			}//一个实例的k个近邻中有temp[j]个标记为j
			
			for(int j = 0; j < Q; j ++){
				if(train_target[j][i] == 1){
					temp_Ci[j][temp[j]] += 1;
				}else{
					temp_NCi[j][temp[j]] += 1;
				}
				//System.out.print(temp[j] + " " + temp_Ci[j][temp[j]] + " " + temp_NCi[j][temp[j]] + "===");
				//第j个标记上，周围含有temp[j]个正类，为正类个数
			}
			//System.out.println();
			
		}
		
		for(int i = 0; i < Q; i++){
			double temp1 = 0, temp2 = 0;
			for(int j = 0; j <= k; j++)//tempK[i]
			{
				temp1 += temp_Ci[i][j];
				temp2 += temp_NCi[i][j];
			}
			for(int j = 0; j <= k; j++)//tempK[i]
			{
				Cond[i][j] = (Smooth+temp_Ci[i][j])/(Smooth*(k+1)+temp1);
				CondN[i][j] = (Smooth+temp_NCi[i][j])/(Smooth*(k+1)+temp2);
			}
		}
	}
	
	private void wirteWordList(){
		try {			
			PrintWriter output = new PrintWriter(new File("./Model.txt"));
			output.printf("%d\n", wordlist.size());//model first line is the word num
			for(String s:wordlist.keySet().toArray(new String[1]))//model the next line is the word list
				output.printf("%d %s ", wordlist.get(s), s);
			output.print("\n");
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void writeModel(){
		try {
			wirteWordList();
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("./Model.txt", true)));
			output.writeBytes("Smooth " + Smooth + "\n");
			output.writeBytes("M " + M + "\n");
			output.writeBytes("N " + N + "\n");
			output.writeBytes("k " + k + "\n");
			output.writeBytes("Q " + Q + "\n");
			output.writeBytes("tagset:\n");
			for(String s:tagset.keySet()){
				output.writeBytes(s + "," + tagset.get(s) + "#");
			}
			output.writeBytes("\n");
			//write train_data
			output.writeBytes("train_data:\n");
		//	for(int j = 0; j < M; j++){
			for(int i = 0; i < N; i++){
				for(Integer nuzero:train_data[i].keySet()){
					output.writeBytes(i + "," + nuzero.intValue() + "," + train_data[i].get(nuzero) + " ");
				}
				output.writeBytes("\n");
					/*if(train_data[j][i] != 0)
						output.writeBytes(i + "," + j + "," + train_data[j][i] + " ");
					*/
			}
				
	//		}
			//write train_target
			output.writeBytes("train_target:\n");
			for(int j = 0; j < N; j++){
				for(int i = 0; i < Q; i++){
					if(train_target[i][j]  == 1)
						output.writeBytes(i + " ");
				}
				output.writeBytes("\n");
			}
			output.writeBytes("Prior:\n");
			for(int i = 0;i < Q; i ++){
				output.writeBytes(Prior[i] + " ");
			}
			output.writeBytes("\n");
			output.writeBytes("PriorN:\n");
			for(int i = 0;i < Q; i ++){
				output.writeBytes(PriorN[i] + " ");
			}
			output.writeBytes("\n");
			output.writeBytes("Cond:\n");
			for(int i = 0; i < Q; i++){
				for(int j = 0; j <= k; j++){
					output.writeBytes(Cond[i][j] + " ");
				}
				output.writeBytes("\n");
			}
			output.writeBytes("CondN:\n");
			for(int i = 0; i < Q; i++){
				for(int j = 0; j <= k; j++){
					output.writeBytes(CondN[i][j] + " ");
				}
				output.writeBytes("\n");
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public double hamming_loss(int[][] a, int[][] b, int len){
		double loss = 0;
		for(int i = 0; i < len; i ++){
			int aloss = 0;
			for(int j = 0; j < Q; j ++){
				if(a[j][i] != b[j][i]){
					aloss ++;
				}
			}
			loss += aloss;
		}
		return loss/(len * Q);
	}
	public class F1{
		public double macroF1;
		public double microF1;
	}
	public F1  mXcroF1(int[][] a, int[][] b, int len){
		F1 f1 = new F1();
		int tp = 0, fp = 0, tn = 0, fn = 0;
		int[] true_positives = new int[Q];
		int[] false_positives = new int[Q];
		int[] true_negatives = new int[Q];
		int[] false_negatives = new int[Q];
		for(int i = 0; i < Q; i ++){
			true_positives[i] = 0;
			false_positives[i] = 0;
			true_negatives[i] = 0;
			false_negatives[i] = 0;
		}
		for(int i = 0; i < len; i ++){
			for(int j = 0; j < Q; j ++){
			//	if(j == 0)
			//		System.out.println(a[j][i] + " " + b[j][i]);
				if(a[j][i] == 1 && b[j][i] == 1){//tp
					tp++;
					true_positives[j] ++;
				}else if(a[j][i] == 1 && b[j][i] != 1){//fn
					fn++;
					false_negatives[j] ++;
				}else if(a[j][i] != 1 && b[j][i] == 1){//fp
					fp++;
					false_positives[j] ++;
				}else{//tn
					tn++;
					true_negatives[j] ++;
				}
			}
		}
		double precision = (tp + 0.0)/(tp+fp);
		double recall = (tp + 0.0)/(tp + fn);
		f1.microF1 = 2*precision * recall / (precision + recall);
		double macrof1 = 0;
		int div = Q;
		for(int i = 0; i < Q; i ++){
			double pr = 0;
			double re = 0;
			if((true_positives[i] + false_positives[i]) != 0)
				pr = (true_positives[i] + 0.0)/(true_positives[i] + false_positives[i]);
			if((true_positives[i] + false_negatives[i]) != 0)
				re = (true_positives[i] + 0.0)/(true_positives[i] + false_negatives[i]);
			double maf1 = 0;
			if((pr + re) == 0){
				maf1 = 0;
				div --;
			}else maf1 = 2 * pr * re / (pr + re);
			macrof1 += maf1;
		//	System.out.println("macroF1:" + macrof1 + "pr:" + pr + "re:" + re);
		}
		f1.macroF1 = macrof1 / div;
		return f1;
	}
	
	public double evaluate(String truetag, String pretag){
		double av = 0;
		try {
			Scanner truetaginput = new Scanner(new File(truetag));
			Scanner pretaginput = new Scanner(new File(pretag));
			int[][] ttag = new int[Q][2000];
			int[][] ptag = new int[Q][2000];
			for(int i = 0; i < Q; i ++){
				for(int j = 0; j < 2000; j++){
					ttag[i][j] = 0;
					ptag[i][j] = 0;
				}
			}
			int len = 0;
			while(truetaginput.hasNext()){
				String tstr = truetaginput.nextLine();
				String pstr = pretaginput.nextLine();
				String[] tt = tstr.split("\\#\\$\\#");
				String[] pt = pstr.split("\\#\\$\\#");
				String[] tts = tt[2].split(",");
				String[] pts = pt[1].split(",");
				
				for(String s:tts){
				//	System.out.println(s);
					Integer id = tagset.get(s);
					if(id != null)
						ttag[id.intValue()][len] = 1;
				}
				for(String s:pts)
				{
					ptag[tagset.get(s)][len] = 1;
				}
				len ++;
			}
			double hloss = this.hamming_loss(ttag, ptag, len);
			F1 f = this.mXcroF1(ttag, ptag, len);
			System.out.println("hamming loss:" + hloss + " macro_f1:" + f.macroF1 + " micro_f1:" + f.microF1);
			av = hloss + 1.0/ f.macroF1 + 10.0 / f.microF1;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return av;
	}
}
