import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mulan.classifier.MultiLabelOutput;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.HOMER;
import mulan.classifier.meta.MultiLabelMetaLearner;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.ClassifierChain;
import mulan.classifier.transformation.EnsembleOfClassifierChains;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.MacroFMeasure;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MicroFMeasure;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Utils;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;

public class Mulan implements Serializable
{
	MLKNNCS learner1 = new MLKNNCS(11, 4.0);
	BinaryRelevance learner5 = new BinaryRelevance(new J48());
	HOMER learner3 = new HOMER();
	ClassifierChain learner4 = new ClassifierChain(new J48());
	MLkNN learner2 = new MLkNN(11, 4.0);
	BinaryRelevance learner6 = new BinaryRelevance(new NaiveBayes());//new RAkEL(new LabelPowerset(new J48()),10,3);
	
	String arffFilename, xmlFilename;
    public void trian(String Filename) throws Exception {
    	//Tags.init();
    	String[] option = new String[2];
    	option[0] = "-arff";
    	//option[1] = "emotions.arff";
    	option[1] = Filename + ".arff";
        arffFilename = Utils.getOption("arff", option); // e.g. -arff emotions.arff
        
        option[0] = "-xml";
        //option[1] = "emotions.xml";
    	option[1] = Filename + ".xml";        
        xmlFilename = Utils.getOption("xml", option); // e.g. -xml emotions.xml

    	MultiLabelInstances dataset = new MultiLabelInstances(arffFilename, xmlFilename);
        
        //int numFolds = 10;
        learner3.build(dataset);
		
		// results = eval.crossValidate(learner1, dataset, numFolds);
		// System.out.println(results);
        //results = eval.crossValidate(learner3, dataset, numFolds);
		// System.out.println(results);
    }
    public void prediction(String Filename) throws IOException
    {    	
    	MultiLabelInstances dataset = null;
    	//Tags.init();
    	try {
    		String[] option = new String[2];
    		option[0] = "-arff";
        	//option[1] = "emotions.arff";
        	option[1] = Filename + "test.arff";
        	
        	//option[1] = "Software.arff";
            arffFilename = Utils.getOption("arff", option); // e.g. -arff emotions.arff
            
            option[0] = "-xml";
            //option[1] = "emotions.xml";
            option[1] = Filename + "test.xml";
            
			xmlFilename = Utils.getOption("xml", option);
		
			dataset = new MultiLabelInstances(arffFilename, xmlFilename);
    	} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // e.g. -xml emotions.xml
    	
    	MultiLabelOutput output = null;
    	BufferedWriter bf3 = null;
		File file3=new File("result.txt");
		try 
		{
			bf3 = new BufferedWriter(new PrintWriter(file3));
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    	for(int j = 0; j < dataset.getNumInstances();j ++)
		{
    		bf3.append("\n");
    		try {
    			output = learner3.makePrediction(dataset.getDataSet().get(j));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
			/*int[] ranking = output.getRanking();
			boolean[] tag = output.getBipartition();
			double[] confidence = output.getConfidences();
			for(int i = 0; i <  ranking.length; i ++)
			{
				//+ Tags.getTagAtIndex(i)
				if(ranking[i] <= 4)
					System.out.println("Sapmle-" + (j+1) +" "   + " rank: "+ ranking[i] +": " + tag[i] + " " + confidence[i]);
			}
			System.out.println("=====================");*/
    		
    		bf3.append("Truth: ");
    		boolean[] tag = output.getBipartition();
    		int[] ranking = output.getRanking();
    		for(int i = 0; i <  tag.length; i ++)
			{
    			double num = dataset.getDataSet().get(j).value(dataset.getDataSet().attribute(1499+i));
    			if(Utils.eq(num, 1.0))	
    				bf3.append(Tags.getTagAtIndex(i)+" " +ranking[i] +",");
			}
    		bf3.append("\n");
    		bf3.append("Predict: ");
    		for(int i = 0; i <  tag.length; i ++)
			{
				if(tag[i])
					bf3.append(Tags.getTagAtIndex(i)+",");
			}
    	}
    	bf3.close();
    }
    
    public void evaluate(String FileName)
    {
    	MultiLabelInstances dataset = null;
    	//Tags.init();
    	String[] option = new String[2];
    	option[0] = "-arff";
    	//option[1] = "emotions.arff";
    	option[1] = FileName + "test.arff";
    	
    	try {
    	//option[1] = "Software.arff";
        arffFilename = Utils.getOption("arff", option); // e.g. -arff emotions.arff
        
        option[0] = "-xml";
        //option[1] = "emotions.xml";
        option[1] = FileName + "test.xml";
    	//option[1] = "Software.xml";        
        
		xmlFilename = Utils.getOption("xml", option);
		
        dataset = new MultiLabelInstances(arffFilename, xmlFilename);
    	} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // e.g. -xml emotions.xml
    	
    	Evaluator eval = new Evaluator();
        //MultipleEvaluation results;
        List<Measure> measureList = new ArrayList<Measure>();
    	
    	MacroFMeasure marcof1 = new MacroFMeasure(dataset.getNumLabels()); 
    	MicroFMeasure misrof1 = new MicroFMeasure(dataset.getNumLabels()); 
    	HammingLoss hammingloss = new HammingLoss(); 
    	measureList.add(marcof1);
    	measureList.add(misrof1);
    	measureList.add(hammingloss);
    	
		try {
			Evaluation results = eval.evaluate(learner3, dataset, measureList);
			System.out.println(results);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(results);
        //results = eval.crossValidate(learner3, dataset, numFolds);
		// System.out.println(results);
    }
    
    public void crossEvaluate()
    {
    	//Tags.init();
    	String[] option = new String[2];
    	option[0] = "-arff";
    	option[1] = "hierarchical.arff";
    	//option[1] = "Software500idf.arff";
        try {
			arffFilename = Utils.getOption("arff", option);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // e.g. -arff emotions.arff
        
        option[0] = "-xml";
        option[1] = "hierarchical.xml";
    	//option[1] = "Software500idf.xml";        
        try {
			xmlFilename = Utils.getOption("xml", option);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // e.g. -xml emotions.xml

    	MultiLabelInstances dataset = null;
		try {
			dataset = new MultiLabelInstances(arffFilename, xmlFilename);
		} catch (InvalidDataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //RAkEL learner1 = new RAkEL(new LabelPowerset(new J48()));
        //EnsembleOfClassifierChains learner2 = new EnsembleOfClassifierChains(new Logistic(), 500, true, true);
        //ClassifierChain learner4 = new ClassifierChain();
        
        Evaluator eval = new Evaluator();
        //MultipleEvaluation results;
        
        int numFolds = 5;
       	
		// results = eval.crossValidate(learner1, dataset, numFolds);
		// System.out.println(results);
        
       /* for(int i = 5; i < 50; i += 5)
        {
        	System.out.println("N:" + i);
        	learner1 = new MLKNNCS(i,1);
        	learner2 = new MLkNN(i, 1);
        	Evaluator eval = new Evaluator();
        	MultipleEvaluation results = eval.crossValidate(learner1, dataset, numFolds);
        	System.out.println(results);
        	MultipleEvaluation results2 = eval.crossValidate(learner2, dataset, numFolds);
        	System.out.println(results2);
        }*/
		/*
        for(int j = 1; j <= 10; j += 1)
        {
        	System.out.println("S:" + j*0.5);
        	learner1 = new MLKNNCS(30,j*0.5);
        	learner2 = new MLkNN(30, j*0.5);
        	Evaluator eval = new Evaluator();
        	MultipleEvaluation results = eval.crossValidate(learner1, dataset, numFolds);
        	System.out.println(results);
        	MultipleEvaluation results2 = eval.crossValidate(learner2, dataset, numFolds);
        	System.out.println(results2);
        }*/
        //MultipleEvaluation results = eval.crossValidate(learner1, dataset, numFolds);
    	//System.out.println(results);
    	MultipleEvaluation results2 = eval.crossValidate(learner2, dataset, numFolds);
    	System.out.println(results2);
		//MultipleEvaluation results3 = eval.crossValidate(learner3, dataset, numFolds);
		//System.out.println(results3);
		//MultipleEvaluation results4 = eval.crossValidate(learner4, dataset, numFolds);
		//System.out.println(results4);
		//MultipleEvaluation results5 = eval.crossValidate(learner5, dataset, numFolds);
		//System.out.println(results5);
		//MultipleEvaluation results3 = eval.crossValidate(learner3, dataset, numFolds);
		//System.out.println(results3);
    }
}
