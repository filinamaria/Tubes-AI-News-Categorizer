/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import weka.classifier.NewsClassifier;

/**
 *
 * @author calvin-pc
 */
@ManagedBean
@SessionScoped
public class AnalyzerBean {

    private String text;
    private String result;
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Creates a new instance of AnalyzerBean
     */
    public AnalyzerBean() {
        result = "Loading...";
    }

    public void analyzeLink() throws Exception {
        NewsClassifier nc = new NewsClassifier();
        text = nc.getNewsContentFromURL(text);
        setResult(nc.classifyText(text, "LABEL"));
    }
	
	public void analyzeFile() throws Exception{
        FileClassifier fc = new FileClassifier();
        fc.classifyFile(inputfile,"LABEL");
    }
    
    public void analyzeText() throws Exception {
        NewsClassifier nc = new NewsClassifier();
        setResult(nc.classifyText(text, "LABEL"));
    }
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    public void setFeedback(String txt) throws IllegalAccessException, SQLException, Exception{
        String DB_URL = "jdbc:mysql://localhost:3306/news_aggregator";
        String Username = "root";
        String Password = "";
        String queri = "INSERT INTO artikel(`FULL_TEXT`) VALUE ('" +text +"')";
        Integer id_artikel;
        System.out.println("THIS:"+txt);
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = null;
            connection =  DriverManager.getConnection(DB_URL, Username, Password);
            
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            statement.executeUpdate(queri);
            
            queri = "SELECT ID_ARTIKEL FROM artikel WHERE FULL_TEXT='" +txt +"'";
            
            ResultSet hasil = statement.executeQuery(queri);
            hasil.next();
            id_artikel = hasil.getInt("ID_ARTIKEL");
            
            queri = "INSERT INTO artikel_kategori_verified(`ID_ARTIKEL`,`ID_KELAS`) VALUE('" +id_artikel +"', '" +label +"')" ;
            
            statement.executeUpdate(queri);
            
        }catch(ClassNotFoundException | InstantiationException e){
            e.printStackTrace();
        }
    }
    
}
