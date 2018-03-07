package com.example.bindview_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AnnotationClass {
    public TypeElement mClassElement;//类名
    private BindLayoutClass mBindLayoutClass;
    public Elements mElementUtils;

    public AnnotationClass(TypeElement classElement, Elements elementUtils){
        this.mClassElement = classElement;
        this.mElementUtils = elementUtils;
    }

    public String getFullClassName() {
        return mClassElement.getQualifiedName().toString();
    }

    public void setContentLayoutId(BindLayoutClass bindLayoutClass) {
        this.mBindLayoutClass = bindLayoutClass;
    }

    public JavaFile generateFinder() {
        // method inject(final T host, Object source, Provider provider)
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("lyInject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mClassElement.asType()), "host", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source")
                .addParameter(TypeUtil.FINDER, "finder");


        // setContentView
        if (mBindLayoutClass!=null){
            if(mBindLayoutClass.getLayoutId() != 0){
                injectMethodBuilder.addStatement("host.setContentView($L)", mBindLayoutClass.getLayoutId());
            }
        }

        //field
//        for (BindViewField field : mFields
//                ) {
//            injectMethodBuilder.addStatement("host.$N= ($T)(finder.findView(source,$L))", field.getFieldName()
//                    , ClassName.get(field.getFieldType()), field.getResId());
//
//        }
//        if (mMethods.size() > 0) {
//            injectMethodBuilder.addStatement("$T listener", TypeUtil.ANDROID_ON_CLICK_LISTENER);
//        }
//        for (OnClickMethod method : mMethods) {
//            // declare OnClickListener anonymous class
//            TypeSpec listener = TypeSpec.anonymousClassBuilder("")
//                    .addSuperinterface(TypeUtil.ANDROID_ON_CLICK_LISTENER)
//                    .addMethod(MethodSpec.methodBuilder("onClick")
//                            .addAnnotation(Override.class)
//                            .addModifiers(Modifier.PUBLIC)
//                            .returns(TypeName.VOID)
//                            .addParameter(TypeUtil.ANDROID_VIEW, "view")
//                            .addStatement("host.$N()", method.getMethodName())
//                            .build()).build();
//            injectMethodBuilder.addStatement("listener = $L ", listener);
//
//            for (int id : method.ids) {
//                // set listeners
//                injectMethodBuilder.addStatement("finder.findView(source, $L).setOnClickListener(listener)", id);
//            }
//        }

        String packageName = getPackageName(mClassElement);
        String className = getClassName(mClassElement, packageName);
        ClassName bindingClassName = ClassName.get(packageName, className);

        // generate whole class
        TypeSpec finderClass = TypeSpec.classBuilder(bindingClassName.simpleName() + "$LeiyuInjector")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.INJECTOR, TypeName.get(mClassElement.asType())))
                .addMethod(injectMethodBuilder.build())
                .build();

        return JavaFile.builder(packageName, finderClass).build();
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }
}
