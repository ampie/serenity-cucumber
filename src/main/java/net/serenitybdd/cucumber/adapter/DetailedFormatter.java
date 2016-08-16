package net.serenitybdd.cucumber.adapter;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.Date;

public interface DetailedFormatter extends Formatter {
    void childStep(String name);
}
