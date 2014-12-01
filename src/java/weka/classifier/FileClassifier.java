/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.out;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.Loader;
import weka.experiment.InstanceQuery;
import weka.filters.*;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author WekaWeka
 */
public class FileClassifier {    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/news_aggregator";
    private static final String DB_Username = "root";
    private static final String DB_Password = "";
    
    public FileClassifier(){
        
    }
    
    private Connection getConnection() throws ClassNotFoundException, SQLException, IllegalAccessException{
        Connection connection = null;
        
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = DB_URL;
            String user = DB_Username;
            String password = DB_Password;
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
   
    
    public Instances CSV2Instances(Part inputfile) throws IOException{
        // Baca file input
        InputStream stream = inputfile.getInputStream();
        Loader loader = new CSVLoader() ;
        loader.setSource(stream);
        // Konversi file input menjadi instance
        Instances data = loader.getDataSet();
        data.deleteAttributeAt(1);
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
    
    public void classifyFile(Part inputfile, String className) throws Exception{
        String textLabel;
        String filepath;
        Instances trainingSet = null;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
                
        trainingSet = getData();
        
        Attribute fullText = new Attribute("FULL_TEXT", (ArrayList<String>)null);
        Attribute label = trainingSet.attribute(className);
        attributes.add(fullText);
        attributes.add(label);
        
        Instances unlabeled = CSV2Instances(inputfile);
        unlabeled.insertAttributeAt(label, 1);
        
        Instances labeled = classify(trainingSet,unlabeled,className);
        
        CSVSaver saver = new CSVSaver();
        saver.setInstances(labeled);
        
        File F = new File("/data/output.csv");
        if(F.exists()){
            F.delete();
        }
        saver.setFile(F);
        saver.writeBatch();
        
        downloadFile();
    }
    
    public void downloadFile() throws IOException
    {
        File file = new File("data/output.csv");
        InputStream fis = new FileInputStream(file);
        byte[] buf = new byte[1024];
        int offset = 0;
        int numRead = 0;
        while ((offset < buf.length) && ((numRead = fis.read(buf, offset, buf.length - offset)) >= 0)) 
        {
          offset += numRead;
        }
        fis.close();
        HttpServletResponse response =
           (HttpServletResponse) FacesContext.getCurrentInstance()
               .getExternalContext().getResponse();

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=output.csv");
        response.getOutputStream().write(buf);
        response.getOutputStream().flush();
        response.getOutputStream().close();
        FacesContext.getCurrentInstance().responseComplete();
    }
}
