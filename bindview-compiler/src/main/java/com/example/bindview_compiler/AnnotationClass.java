package com.example.bindview_compiler;

import com.example.PermissionCheck;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AnnotationClass {
    public TypeElement mClassElement;//类名
    public List<BindViewField> mFields;//成员变量
    public List<MethodInterface> mMethods;//方法
    private BindLayoutClass mBindLayoutClass;
    public Elements mElementUtils;

    public AnnotationClass(TypeElement classElement, Elements elementUtils){
        this.mClassElement = classElement;
        this.mFields = new ArrayList<>();
        this.mMethods = new ArrayList<>();
        this.mElementUtils = elementUtils;
    }

    public String getFullClassName() {
        return mClassElement.getQualifiedName().toString();
    }

    public void setContentLayoutId(BindLayoutClass bindLayoutClass) {
        this.mBindLayoutClass = bindLayoutClass;
    }

    public void addField(BindViewField field) {
        mFields.add(field);
    }

    public void addMethod(MethodInterface method) {
        mMethods.add(method);
    }

    public JavaFile generateFinder() {
        // method inject(final T host, Object source, Provider provider)
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("lyInject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mClassElement.asType()), "host", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source")
                .addParameter(TypeUtil.FINDER, "finder");
        MethodSpec.Builder permissionCheckMethodBuilder = null;
        MethodSpec.Builder permissionRequestMethodBuilder = null;
        MethodSpec.Builder permissionGrantedMethodBuilder = null;

        // setContentView
        if (mBindLayoutClass!=null){
            if(mBindLayoutClass.getLayoutId() != 0){
                injectMethodBuilder.addStatement("host.setContentView($L)", mBindLayoutClass.getLayoutId());
            }
        }

        //field
        for (BindViewField field : mFields
                ) {
            injectMethodBuilder.addStatement("host.$N= ($T)(finder.findView(source,$L))", field.getFieldName()
                    , ClassName.get(field.getFieldType()), field.getResId());

        }
        if (mMethods.size() > 0) {
            injectMethodBuilder.addStatement("$T listener", TypeUtil.ANDROID_ON_CLICK_LISTENER);
        }
        boolean hasPermissionAnnotation = false;
        for(int i=0; i < mMethods.size(); i++){
            if(mMethods.get(i) instanceof OnClickMethod){
                TypeSpec listener = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(TypeUtil.ANDROID_ON_CLICK_LISTENER)
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.VOID)
                                .addParameter(TypeUtil.ANDROID_VIEW, "view")
                                .addStatement("host.$N()", mMethods.get(i).getMethodName())
                                .build()).build();
                injectMethodBuilder.addStatement("listener = $L ", listener);

                for (int id : ((OnClickMethod)mMethods.get(i)).ids) {
                    // set listeners
                    injectMethodBuilder.addStatement("finder.findView(source, $L).setOnClickListener(listener)", id);
                }
            }else if(mMethods.get(i) instanceof PermissionCheckClass){
                hasPermissionAnnotation = true;
            }else if(mMethods.get(i) instanceof PermissionGrantedClass){
                /*permissionGrantedMethodBuilder = MethodSpec.methodBuilder("onRequestPermissionsResult")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(void.class)
                        .addParameter(TypeName.INT, "requestCode")
                        .addParameter(String[].class, "permissions")
                        .addParameter(int[].class, "grantResults");
                CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(requestCode)");
                for (Map.Entry<String, int[]> entry : customRationaleMap.entrySet()) {
                    String methodName = entry.getKey();
                    int[] ints = entry.getValue();
                    for (int requestCode : ints) {
                        caseBlock.add("case $L:\n", requestCode).indent();
                        if (singleCustomRationaleMap.containsKey(requestCode)) {
                            singleCustomRationaleMap.remove(requestCode);
                        }
                    }
                    caseBlock.addStatement("object.$N(code)", methodName);
                    caseBlock.addStatement("return true").unindent();
                }
                for (Map.Entry<Integer, String> entry : singleCustomRationaleMap.entrySet()) {
                    int requestCode = entry.getKey();
                    caseBlock.add("case $L:", requestCode).indent();
                    caseBlock.addStatement("object.$N()", entry.getValue());
                    caseBlock.addStatement("return true").unindent();
                }
                caseBlock.add("default:\n").indent().addStatement("return false").unindent();
                caseBlock.endControlFlow();
                injectMethodBuilder.addCode(caseBlock.build());*/
            }
        }
        if(hasPermissionAnnotation){
            permissionCheckMethodBuilder = MethodSpec.methodBuilder("hasPermission")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(boolean.class)
                    .addParameter(TypeName.get(mClassElement.asType()), "host", Modifier.FINAL)
                    .addParameter(String[].class, "permissions")
                    .beginControlFlow("for ( String permission : permissions)")
                    .beginControlFlow("if($T.checkSelfPermission(host, permission) != $T.PERMISSION_GRANTED)", TypeUtil.ANDROID_CONTEXTCOMPAT,
                            TypeUtil.ANDROID_PACKAGEMANAGER)
                    .addStatement("return false")
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("return true");

            permissionRequestMethodBuilder = MethodSpec.methodBuilder("requestPermission")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(void.class)
                    .addParameter(TypeName.get(mClassElement.asType()), "host", Modifier.FINAL)
                    .addParameter(String[].class, "permissions")
                    .addParameter(int.class, "code")
                    .addStatement("$T.requestPermissions(host, permissions, code)", TypeUtil.ANDROID_ACTIVITYCOMPAT);
        }
        String packageName = getPackageName(mClassElement);
        String className = getClassName(mClassElement, packageName);
        ClassName bindingClassName = ClassName.get(packageName, className);

        // generate whole class
        TypeSpec.Builder finderClass = TypeSpec.classBuilder(bindingClassName.simpleName() + "$LeiyuInjector")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.INJECTOR, TypeName.get(mClassElement.asType())))
                .addMethod(injectMethodBuilder.build());
        if(permissionCheckMethodBuilder != null){
            finderClass.addMethod(permissionCheckMethodBuilder.build());
        }
        if(permissionRequestMethodBuilder != null){
            finderClass.addMethod(permissionRequestMethodBuilder.build());
        }
        return JavaFile.builder(packageName, finderClass.build()).build();
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }
}
