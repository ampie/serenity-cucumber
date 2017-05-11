package net.serenitybdd.cucumber.adaptor;

import gherkin.formatter.model.Feature;

public abstract class Contextualizer {
    protected String sourceContext;
    public void setSourceContext(String ctx){
        this.sourceContext=ctx;
    }
    protected String contextualizeId(String id) {
        return sourceContext==null?id:id+"-"+sourceContext;
    }
    protected String contextualizePath(String path) {
        if(path!=null && path.indexOf('.')>0) {
            String s = path.substring(0, path.lastIndexOf('.')) + "(" + sourceContext + ")" + path.substring(path.lastIndexOf('.'));
            return s;
        }else{
            return contextualizeName(path);
        }

    }

    protected String contextualizeName(String name) {
        return sourceContext==null?name:name+" ("+sourceContext +")";
    }
    protected String typeOf(Feature feature) {
        String s = CucumberTagName.REQUIREMENT_TYPE.valueOn(feature);
        return s==null?"feature":s;
    }

}
