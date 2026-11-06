package pl.edu.pw.ee.zai.dto;

public class ValidationErrorItem {

    private String field;
    private String message;

    public ValidationErrorItem() {
    }

    public ValidationErrorItem(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
