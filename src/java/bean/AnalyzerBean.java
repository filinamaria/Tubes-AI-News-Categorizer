/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

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
    }

    public void analyzeText() {
        //TODO add some weka code here
        result = "berhasil";
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
    
}