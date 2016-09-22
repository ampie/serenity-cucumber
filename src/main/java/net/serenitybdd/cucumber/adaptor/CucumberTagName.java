package net.serenitybdd.cucumber.adaptor;

import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CucumberTagName {
    ISSUE("issue","issues"),
    REQUIREMENT("requirement","requirements"),
    REQUIREMENT_TYPE("requirement-type"),
    PARENT_REQUIREMENT("parent-requirement", "parent-requirements"),
    ID("id"),

    VERSION("version","versions");
    private String plural;
    private String singular;
    private CucumberTagName(String singular){
        this.singular=singular;
    }
    private CucumberTagName(String singular,String plural){
        this(singular);
        this.plural=plural;
    }

    public List<String> valuesOn(TagStatement key) {
        List<String> result = new ArrayList<String>();
        for (Tag tag : key.getTags()) {
            if(tag.getName().startsWith("@" + singular + ":")){
                result.add(tag.getName().substring(("@" + singular + ":").length()));
            }else if(tag.getName().startsWith("@" + plural+ ":")){
                String values = tag.getName().substring(("@" + plural + ":").length());
                result.addAll(Arrays.asList(values.split(",")));
            }
        }
        return result;
    }
    public String valueOn(TagStatement key) {
        for (Tag tag : key.getTags()) {
            if(tag.getName().startsWith("@" + singular + ":")){
                return tag.getName().substring(("@" + singular + ":").length());
            }
        }
        return null;
    }

    public static boolean isDefined(String name) {
        for (CucumberTagName cucumberTagName : values()) {
            if(name.equals(cucumberTagName.plural) || name.equals(cucumberTagName.singular)){
                return true;
            }
        }
        return false;
    }
}
