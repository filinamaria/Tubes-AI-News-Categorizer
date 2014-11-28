/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.classifier;

import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instances;

/**
 *
 * @author Sakurai
 */
public class NewsClassifier {
    
	public Classifier makeClassifier(Instances data) throws Exception{
		Classifier cls = (Classifier) new NaiveBayesMultinomial();
		cls.buildClassifier(data);
		return cls;
	}
	
	public void evaluateClassifier(Classifier cls, Instances data) throws Exception{
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(cls, data, 10, new Random(1));     
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
	}
	
	public Instances classify(Instances trainingSet, Instances unlabeled, String namaKelas) throws Exception{
		trainingSet.setClass(trainingSet.attribute(namaKelas));
		Classifier cls = makeClassifier(trainingSet);
		
		//create copy
		Instances labeled = unlabeled;
		
		// label instances
		for (int i = 0; i < unlabeled.numInstances(); i++) {
			double clsLabel = cls.classifyInstance(unlabeled.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}
		
		return labeled;
	}
}
