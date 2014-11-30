/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import weka.classifier.NewsClassifier;

/**
 *
 * @author calvin-pc
 */
@ManagedBean
@RequestScoped
public class AnalyzerBean {

    private String text;
    private String result;
    
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
    
}
