package com.solvd.carinaideaplugin.templates;

import com.intellij.psi.PsiPrimitiveType;

public class GenerationTemplate {
    private String name;
    private String message;
    private String methodName;
    private PsiPrimitiveType returnType;


    public GenerationTemplate() {
    }

    public PsiPrimitiveType getReturnType() {
        return returnType;
    }

    public String getMethodBody() {
        return methodBody;
    }

    private String methodBody;

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getMethodName() {
        return methodName;
    }

    public static TemplateBuilder builder(){
        return new TemplateBuilder(new GenerationTemplate());
    }
    public static class TemplateBuilder {
        private final GenerationTemplate template;


        public TemplateBuilder(GenerationTemplate template) {
            this.template = template;
        }

        public TemplateBuilder name(String name){
            this.template.name = name;
            return this;
        }

        public TemplateBuilder message(String message){
            this.template.message = message;
            return this;
        }

        public TemplateBuilder methodName(String methodName){
            this.template.methodName = methodName;
            return this;
        }

        public TemplateBuilder methodBody(String methodBody){
            this.template.methodBody = methodBody;
            return this;
        }

        public TemplateBuilder returnType(PsiPrimitiveType returnType){
            this.template.returnType = returnType;
            return this;
        }

        public GenerationTemplate build(){
            return template;
        }
    }
}
