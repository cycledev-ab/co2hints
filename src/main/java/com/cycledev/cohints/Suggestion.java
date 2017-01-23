/*
Copyright (c) 2017 Cycledev AB

Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.cycledev.cohints;

/**
 * Represents a suggestion.
 * 
 * @author Leonard Saers
 */
public class Suggestion {
    
    String suggestion;
    String text;
    int impact;
    int simplicity;

    public Suggestion(String suggestion, String text, int impact, int simplicity) {
        this.suggestion = suggestion;
        this.text = text;
        this.impact = impact;
        this.simplicity = simplicity;
    }

    public Suggestion() {
    }
    
    public String getSimplicityString(){
        if (simplicity < 25) {
            return "easy";
        } else if (simplicity < 75) {
            return "medium";
        } else {
            return "hard";
        }
    }
    
    public String getImpactString(){
        if (impact < 25) {
            return "low";
        } else if(impact < 100) {
            return "medium";
        } else {
            return "high";
        }
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    public int getSimplicity() {
        return simplicity;
    }

    public void setSimplicity(int simplicity) {
        this.simplicity = simplicity;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return "Suggestion: \"" + suggestion + 
                "\", Impact: " + impact + 
                " , Simplicity: " + simplicity;
    }
    
}
