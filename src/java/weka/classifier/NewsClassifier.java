/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.classifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.*;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author Sakurai
 */
public class NewsClassifier {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/news_aggregator";
    private static final String DB_Username = "root";
    private static final String DB_Password = "";

    public NewsClassifier(){
        
    }
    
    private Connection getConnection() throws ClassNotFoundException, SQLException, IllegalAccessException{
        Connection connection = null;
        
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = DB_URL;
            String user = DB_Username;
            String password = DB_Password;
            connection =  DriverManager.getConnection(url, user, password);
      //  }catch(ClassNotFoundException | InstantiationException e){
		} catch(Exception e){
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
            query.setUsername(DB_Username);
            query.setPassword(DB_Password);
            query.setQuery("select FULL_TEXT, LABEL from artikel natural join artikel_kategori_verified natural join kategori");
            data = query.retrieveInstances();
            
            connection.close();
        }catch(SQLException e){
            out.println(e.getMessage());
        }
        
        return data;
    }
    
    private NominalToString getNominalToStringFilter(){
        NominalToString nominalToString = new NominalToString();
        nominalToString.setAttributeIndexes("first");
        
        return nominalToString;
    }
    
    private StringToWordVector getStringToWordVectorFilter(){
        StringToWordVector stringToWordVector = new StringToWordVector();
        stringToWordVector.setAttributeIndices("first-last");
        stringToWordVector.setLowerCaseTokens(true);
        stringToWordVector.setWordsToKeep(1000);
        stringToWordVector.setOutputWordCounts(true);
        
        return stringToWordVector;
    }
        
    public Classifier makeClassifier(Instances data) throws Exception{
        NaiveBayesMultinomial usedClassifier = new NaiveBayesMultinomial();
        FilteredClassifier classifier = new FilteredClassifier();
        MultiFilter multipleFilter = new MultiFilter();
        Filter [] filters = new Filter[2];

        filters[0] = getNominalToStringFilter();
        filters[1] = getStringToWordVectorFilter();
        
        multipleFilter.setFilters(filters);
        
        classifier.setFilter(multipleFilter);
        classifier.setClassifier(usedClassifier);
        classifier.buildClassifier(data);
        
        return classifier;
    }
		
    public void evaluateClassifier(Classifier usedClassifier, Instances data) throws Exception{
        Evaluation eval = new Evaluation(data);
        int numberOfFolds = 10;
        Random random = new Random(1); // Ini gw ga tau buat apa
        
        eval.crossValidateModel(usedClassifier, data, numberOfFolds, random);     
        
        System.out.println(eval.toSummaryString("\nResults\n======\n", false));
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
    }
	
    public Instances classify(Instances trainingSet, Instances unlabeledData, String className) throws Exception{
        trainingSet.setClass(trainingSet.attribute(className));
        unlabeledData.setClass(trainingSet.attribute(className));
        Classifier usedClassifier = makeClassifier(trainingSet);

        //create copy
        Instances labeledData = unlabeledData;

        // classify each instances, each instance will be given label name classified by using the model
        for (int i = 0; i < unlabeledData.numInstances(); i++) {
            double instanceLabel = usedClassifier.classifyInstance(unlabeledData.instance(i));
            labeledData.instance(i).setClassValue(instanceLabel);
        }

        return labeledData;
    }
	
    public String classifyText(String text, String className) throws Exception{
        String textLabel;
        Instances trainingSet = null;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
                
        trainingSet = getData();
        
        if (trainingSet == null) return "Training set cannot be loaded";

        Attribute fullText = new Attribute("FULL_TEXT", (ArrayList<String>)null);
        Attribute label = trainingSet.attribute(className);
        attributes.add(fullText);
        attributes.add(label);
        
        // Generate instance from input text with unknown class label
        Instances unlabeledData = new Instances("unlabeled", attributes, 0);
        double [] valuesRel = new double[2];
        valuesRel[0] = unlabeledData.attribute(0).addStringValue(text);
        valuesRel[1] = 0;
        unlabeledData.add(new DenseInstance(1.0, valuesRel));
        
        // Create new instance for storing labeled data
        Instances labeledData = null;

        labeledData = classify(trainingSet, unlabeledData, className);
        
        textLabel = labeledData.classAttribute().value((int) labeledData.instance(0).classValue());

        return textLabel;

    }
	
    public String getHtmlCode(String link) throws Exception{
        URL url = new URL(link);
        StringBuffer response;
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine;
        response = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
                response.append(inputLine);
        }

        return response.toString();
    }

    public String getNewsContentFromURL(String link) throws Exception{
        String content = "";
        String htmlCode = getHtmlCode(link);
        String contentRegex = "";

        if(link.contains("liputan6.com")){
                contentRegex = "(<div\\sclass=\"entry-content\">)(.+?)(</div>)";
        } else if(link.contains("detik.com")){
                contentRegex = "(<div\\sclass=\"text_detail\">)(.+?)(</div>)";
        } else if(link.contains("tempo.co")){
                contentRegex = "(<span\\sstyle=\"color:\\s#666666;\">)(.+?)(</div>)";
        } else if(link.contains("okezone.com")){
                contentRegex = "(<strong>)(.+?)(</div>)";
        } else if(link.contains("bharian.com")){
                contentRegex = "(<div\\sclass=\"node-content\\scontent\">)(.+?)(</article>)";
        }

        // get the news content 
        Pattern p = Pattern.compile(contentRegex);
        Matcher m = p.matcher(htmlCode);
        if(m.find()) {
           content = m.group();
        }

        // remove all html tag
        String htmlTagRegex = "<(.+?)>";
        content = content.replaceAll(htmlTagRegex, "");

        // remove all html ascii code
        String htmlAsciiRegex = "&([a-zA-Z0-9]+;)";
        content = content.replaceAll(htmlAsciiRegex, "");

        return content;
    }
}
