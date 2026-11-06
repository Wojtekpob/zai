package pl.edu.pw.ee.zai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemResponse {

    private String type = "about:blank";
    private String title;
    private int status;
    private String detail;
    private String instance;
    private List<ValidationErrorItem> errors;

    public ProblemResponse() {
    }

    public ProblemResponse(String title, int status, String detail, String instance) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public ProblemResponse(String title, int status, String detail, String instance, List<ValidationErrorItem> errors) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.errors = errors;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<ValidationErrorItem> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationErrorItem> errors) {
        this.errors = errors;
    }
}
