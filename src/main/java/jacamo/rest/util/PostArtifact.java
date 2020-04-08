package jacamo.rest.util;

public class PostArtifact {
    private String template;
    private Object[] values;

    public PostArtifact(){};
    
    public PostArtifact(String template, Object[] values) {
        this.template = template;
        this.values = values;
    }
    
    public String getTemplate() {
        return template;
    }
    public void setTemplate(String template) {
        this.template = template;
    }
    public Object[] getValues() {
        return values;
    }
    public void setValues(Object[] values) {
        this.values = values;
    }
}
