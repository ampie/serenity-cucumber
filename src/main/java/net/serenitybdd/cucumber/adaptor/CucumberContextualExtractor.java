package net.serenitybdd.cucumber.adaptor;

public abstract class CucumberContextualExtractor {
    protected String sourceContext;
    public void setSourceContext(String ctx){
        this.sourceContext=ctx;
    }
    protected String contextualizeId(String id) {
        return sourceContext==null?id:id+"-"+sourceContext;
    }
    protected String contextualizeName(String name) {
        return sourceContext==null?name:name+" ("+sourceContext +")";
    }
}
