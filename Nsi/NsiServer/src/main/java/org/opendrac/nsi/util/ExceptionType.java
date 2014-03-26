/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.util;

/**
 *
 * @author hacksaw
 */
public class ExceptionType {
    private String errorId;
    private String text;

    public ExceptionType(String errorId, String text) {
        this.errorId = errorId;
        this.text = text;
    }

    /**
     * @return the errorId
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * @param errorId the errorId to set
     */
    public void setErrorId(String errorId) {
        this.errorId = errorId;
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

}
