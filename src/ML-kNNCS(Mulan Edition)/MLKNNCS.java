/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import mulan.classifier.MultiLabelOutput;
import mulan.data.MultiLabelInstances;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.Utils;

/**
 <!-- globalinfo-start -->
 * Class implementing the ML-kNN (Multi-Label k Nearest Neighbours) algorithm.<br>
 * <br>
 * For more information, see<br>
 * <br>
 * Min-Ling Zhang, Zhi-Hua Zhou (2007). ML-KNN: A lazy learning approach to multi-label learning. Pattern Recogn.. 40(7):2038--2048.
 * <br>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Zhang2007,
 *    address = {New York, NY, USA},
 *    author = {Min-Ling Zhang and Zhi-Hua Zhou},
 *    journal = {Pattern Recogn.},
 *    number = {7},
 *    pages = {2038--2048},
 *    publisher = {Elsevier Science Inc.},
 *    title = {ML-KNN: A lazy learning approach to multi-label learning},
 *    volume = {40},
 *    year = {2007},
 *    ISSN = {0031-3203}
 * }
 * </pre>
 * <br>
 <!-- technical-bibtex-end -->
 *
 * @author Eleftherios Spyromitros-Xioufis
 * @version 2012.07.16
 */
@SuppressWarnings("serial")
public class MLKNNCS extends MultiLabelKNN {

    /**
     * Smoothing parameter controlling the strength of uniform prior <br>
     * (Default value is set to 1 which yields the Laplace smoothing).
     */
    protected double smooth;
    /**
     * A table holding the prior probability for an instance to belong in each
     * class
     */
    private double[] PriorProbabilities;
    /**
     * A table holding the prior probability for an instance not to belong in
     * each class
     */
    private double[] PriorNProbabilities;
    /**
     * A table holding the probability for an instance to belong in each
     * class<br> given that i:0..k of its neighbors belong to that class
     */
    private double[][] CondProbabilities;
    /**
     * A table holding the probability for an instance not to belong in each
     * class<br> given that i:0..k of its neighbors belong to that class
     */
    private double[][] CondNProbabilities;
	private double[] Cost;

    /**
     * @param numOfNeighbors : the number of neighbors
     * @param smooth : the smoothing factor
     */
    public MLKNNCS(int numOfNeighbors, double smooth) {
        super(numOfNeighbors);
        this.smooth = smooth;
    }

    /**
     * The default constructor
     */
    public MLKNNCS() {
        super();
        this.smooth = 1.0;
    }

    public String globalInfo() {
        return "Class implementing the ML-kNN (Multi-Label k Nearest Neighbours) algorithm." + "\n\n" + "For more information, see\n\n" + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation result;

        result = new TechnicalInformation(Type.ARTICLE);
        result.setValue(Field.AUTHOR, "Min-Ling Zhang and Zhi-Hua Zhou");
        result.setValue(Field.TITLE, "ML-KNN: A lazy learning approach to multi-label learning");
        result.setValue(Field.JOURNAL, "Pattern Recogn.");
        result.setValue(Field.VOLUME, "40");
        result.setValue(Field.NUMBER, "7");
        result.setValue(Field.YEAR, "2007");
        result.setValue(Field.ISSN, "0031-3203");
        result.setValue(Field.PAGES, "2038--2048");
        result.setValue(Field.PUBLISHER, "Elsevier Science Inc.");
        result.setValue(Field.ADDRESS, "New York, NY, USA");

        return result;
    }

    @Override
    protected void buildInternal(MultiLabelInstances train) throws Exception {
        super.buildInternal(train);
        PriorProbabilities = new double[numLabels];
        PriorNProbabilities = new double[numLabels];
        Cost = new double[numLabels];
        CondProbabilities = new double[numLabels][numOfNeighbors + 1];
        CondNProbabilities = new double[numLabels][numOfNeighbors + 1];
        ComputePrior();
        ComputeCond();

        if (getDebug()) {
            System.out.println("Computed Prior Probabilities");
            for (int i = 0; i < numLabels; i++) {
                System.out.println("Label " + (i + 1) + ": " + PriorProbabilities[i]);
            }
            System.out.println("Computed Posterior Probabilities");
            for (int i = 0; i < numLabels; i++) {
                System.out.println("Label " + (i + 1));
                for (int j = 0; j < numOfNeighbors + 1; j++) {
                    System.out.println(j + " neighbours: " + CondProbabilities[i][j]);
                    System.out.println(j + " neighbours: " + CondNProbabilities[i][j]);
                }
            }
        }
    }

    /**
     * Computing Prior and PriorN Probabilities for each class of the training
     * set
     */
    private void ComputePrior() {
        for (int i = 0; i < numLabels; i++) {
            int temp_Ci = 0;
            for (int j = 0; j < train.numInstances(); j++) {
                double value = Double.parseDouble(train.attribute(labelIndices[i]).value(
                        (int) train.instance(j).value(labelIndices[i])));
                if (Utils.eq(value, 1.0)) {
                    temp_Ci++;
                }
            }
            PriorProbabilities[i] = (smooth + temp_Ci) / (smooth * 2 + train.numInstances());
            PriorNProbabilities[i] = 1 - PriorProbabilities[i];
            Cost[i] = Math.log10((train.numInstances() - temp_Ci - 1) / (temp_Ci + 1)) + 1;
        }
    }

    /**
     * Computing Cond and CondN Probabilities for each class of the training set
     *
     * @throws Exception Potential exception thrown. To be handled in an upper level.
     */
    private void ComputeCond() throws Exception {
        int[][] temp_Ci = new int[numLabels][numOfNeighbors + 1];
        int[][] temp_NCi = new int[numLabels][numOfNeighbors + 1];

        for (int i = 0; i < train.numInstances(); i++) {

            Instances knn = new Instances(lnn.kNearestNeighbours(train.instance(i), numOfNeighbors));

            // now compute values of temp_Ci and temp_NCi for every class label
            for (int j = 0; j < numLabels; j++) {

                int aces = 0; // num of aces in Knn for j
                for (int k = 0; k < numOfNeighbors; k++) {
                    double value = Double.parseDouble(train.attribute(labelIndices[j]).value(
                            (int) knn.instance(k).value(labelIndices[j])));
                    if (Utils.eq(value, 1.0)) {
                        aces++;
                    }
                }
                // raise the counter of temp_Ci[j][aces] and temp_NCi[j][aces] by 1
                if (Utils.eq(Double.parseDouble(train.attribute(labelIndices[j]).value(
                        (int) train.instance(i).value(labelIndices[j]))), 1.0)) {
                    temp_Ci[j][aces]++;
                } else {
                    temp_NCi[j][aces]++;
                }
            }
        }

        // compute CondProbabilities[i][..] for labels based on temp_Ci[]
        for (int i = 0; i < numLabels; i++) {
            int temp1 = 0;
            int temp2 = 0;
            for (int j = 0; j < numOfNeighbors + 1; j++) {
                temp1 += temp_Ci[i][j];
                temp2 += temp_NCi[i][j];
            }
            for (int j = 0; j < numOfNeighbors + 1; j++) {
                CondProbabilities[i][j] = (smooth + temp_Ci[i][j]) / (smooth * (numOfNeighbors + 1) + temp1);
                CondNProbabilities[i][j] = (smooth + temp_NCi[i][j]) / (smooth * (numOfNeighbors + 1) + temp2);
            }
        }
    }

    protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception {
        double[] confidences = new double[numLabels];
        boolean[] predictions = new boolean[numLabels];

        Instances knn = null;
        try {
            knn = new Instances(lnn.kNearestNeighbours(instance, numOfNeighbors));
        } catch (Exception ex) {
            Logger.getLogger(MLKNNCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        int trueCount = 0;
        for (int i = 0; i < numLabels; i++) {
            // compute sum of aces in KNN
            int aces = 0; // num of aces in Knn for i
            for (int k = 0; k < numOfNeighbors; k++)
            {
                double value = Double.parseDouble(train.attribute(labelIndices[i]).value(
                        (int) knn.instance(k).value(labelIndices[i])));
                if (Utils.eq(value, 1.0)) {
                    aces++;
                }
            }
            double Prob_in = PriorProbabilities[i] * CondProbabilities[i][aces];
            double Prob_out = PriorNProbabilities[i] * CondNProbabilities[i][aces];
            
            confidences[i] = Cost[i] * Prob_in/(Cost[i] * Prob_in + Prob_out);
            //confidences[i] = 6*Prob_in/(6*Prob_in + Prob_out);
            
            if (confidences[i] > 0.5)
            {
                predictions[i] = true;
                trueCount ++;
            } 
            else if (confidences[i] < 0.5)
            {
                predictions[i] = false;
            } 
            else 
            {
                Random rnd = new Random();
                predictions[i] = (rnd.nextInt(2) == 1) ? true : false;
            }
            // ranking function
        }
        
        MultiLabelOutput mlo = new MultiLabelOutput(predictions, confidences);
        
        if(trueCount < 3)
        {
        	double[] confidence = mlo.getConfidences();
        	double[] confidenceTop4 = new double[4];
        	int []top4 = new int[4];
        	
        	Arrays.fill(top4, 0);
        	Arrays.fill(confidenceTop4, 0);
        	
        	for(int i = 0; i < confidence.length; i ++)
        	{
        		if(confidence[i] > confidenceTop4[0])
        		{
        			top4[3] = top4[2];
        			confidenceTop4[3] = confidenceTop4[2];
        			top4[2] = top4[1];
        			confidenceTop4[2] = confidenceTop4[1];
        			top4[1] = top4[0];
        			confidenceTop4[1] = confidenceTop4[0];
        			top4[0] = i;
        			confidenceTop4[0] = confidence[i];
        		}
        		else if(confidence[i] > confidenceTop4[1])
        		{
        			top4[3] = top4[2];
        			confidenceTop4[3] = confidenceTop4[2];
        			top4[2] = top4[1];
        			confidenceTop4[2] = confidenceTop4[1];
        			top4[1] = i;
        			confidenceTop4[1] = confidence[i];
        		}
        		else if(confidence[i] > confidenceTop4[2])
        		{
        			top4[3] = top4[2];
        			confidenceTop4[3] = confidenceTop4[2];
        			top4[2] = i;
        			confidenceTop4[2] = confidence[i];
				}
        		else if(confidence[i] > confidenceTop4[3])
        		{
        			top4[3] = i;
        			confidenceTop4[3] = confidence[i];
				}
        		
        	}
        	for(int i = trueCount; i < 4; i ++)
        	{	
        		if((confidence[top4[i]] > 0.25 && i == 3) || confidence[top4[i]] > 0.2 && i < 3)
        		{	
        			predictions[top4[i]] = true;
        			trueCount ++;
        		}
        	}
        	if(trueCount == 0)
        	{
        		predictions[top4[0]] = true;
        	}
        	mlo = new MultiLabelOutput(predictions, confidences);
        }     
        return mlo;
    }
}