package net.serenitybdd.cucumber.adapter;

public abstract class CucumberContextualFormatter {
    private String sourceContext;
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
