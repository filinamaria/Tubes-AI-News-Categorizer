/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.classifier;

import static java.lang.System.out;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.NewsAddress;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.*;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author Sakurai
 */
public class NewsClassifier {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/news_aggregator";
    private static final String Username = "root";
    private static final String Password = "";

    public NewsClassifier(){
        
    }
    
    private Connection getConnection() throws ClassNotFoundException, SQLException, IllegalAccessException{
        Connection connection = null;
        
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = DB_URL;
            String user = Username;
            String password = Password;
            connection =  DriverManager.getConnection(url, user, password);
        }catch(ClassNotFoundException | InstantiationException e){
            e.printStackTrace();
        }
        
        return connection;
    }
    
    private Instances getData() throws Exception{
        Instances data = null;
        
        try{
            Connection connection = getConnection();
            InstanceQuery query = new InstanceQuery();
        
            query.setDatabaseURL(DB_URL);
            query.setUsername(Username);
            query.setPassword(Password);
            query.setQuery("select FULL_TEXT, LABEL from artikel natural join artikel_kategori_verified natural join kategori");
            data = query.retrieveInstances();
            
            connection.close();
        }catch(SQLException e){
            
        }
        
        return data;
    }
    
    private Instances doFilter(Instances data) throws Exception{
        
        NominalToString nominalToString = new NominalToString();
        nominalToString.setAttributeIndexes("first");
        
        nominalToString.setInputFormat(data);
        Instances newData = Filter.useFilter(data, nominalToString);
        
        StringToWordVector stringToWordVector = new StringToWordVector();
        stringToWordVector.setAttributeIndices("first-last");
        stringToWordVector.setLowerCaseTokens(true);
        stringToWordVector.setWordsToKeep(4000);
        
        stringToWordVector.setInputFormat(newData);
        Instances newData2 = Filter.useFilter(newData, stringToWordVector);
        
        return newData2;
    }
    
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
                unlabeled.setClass(trainingSet.attribute(namaKelas));
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
	
	public String classifyText(String text){
                String namaKelas = "LABEL";
		Instances trainingSet;
                try {
                    trainingSet = getData();
                } catch (Exception ex) {
                    return ex.getMessage();
                }
                if (trainingSet == null) return "training set tidak bisa di load";
                
                ArrayList<Attribute> attributes = new ArrayList<Attribute>();
                Instances afterFilter;
                try {
                    afterFilter = (doFilter(trainingSet));
                } catch (Exception ex) {
                    return ex.getMessage();
                }
                
                Enumeration<Attribute> allAttribute = afterFilter.enumerateAttributes(); 
                while (allAttribute.hasMoreElements()) {
                    attributes.add(allAttribute.nextElement());
                }
                
                Instances unlabeled= new Instances("unlabeled", attributes, 0);
                double [] valuesRel = new double[unlabeled.numAttributes()];
                
                for (int i = 0; i < unlabeled.numAttributes(); i++) {
                    if (attributes.get(i).name().equals(namaKelas)) {
                        valuesRel[i] = 0;
                    }
                    else {
                        if (text.contains(attributes.get(i).name())) {
                            valuesRel[i] = 1;
                        }
                        else valuesRel[i] = 0;
                    }
                }
                
                unlabeled.add(new DenseInstance(1.0, valuesRel));
                Instances labeled;
                try {
                    labeled = classify(afterFilter, unlabeled, namaKelas);
                } catch (Exception ex) {
                    return ex.getMessage();
                }
                return labeled.classAttribute().value((int) labeled.instance(0).classValue());
		//double clsLabel = cls.classifyInstance(unlabeled.instance(i));
		//labeled.instance(i).setClassValue(clsLabel);
		//}
		
		//return labeled;
	}
}
