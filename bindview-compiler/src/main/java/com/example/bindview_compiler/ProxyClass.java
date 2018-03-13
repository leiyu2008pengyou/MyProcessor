package com.example.bindview_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by leiyu on 2018/3/12.
 */

public class ProxyClass {
    private static final String PERMISSIONS_PROXY = "PermissionsProxy";
    private static final ClassName PERMISSIONS_PROXY_INJECTOR = ClassName.get("com.joker.api.wrapper.AnnotationWrapper", "PermissionsProxy");
    private static final ClassName PERMISSIONS_PROXY_PERMISSIONS4M = ClassName.get("com.joker.api", "Permissions4M");
    private static final String CONCAT = "$$";
    private final String packageName;
    private final TypeElement element;
    private final String proxyName;
    // methodName -> requestCodes
    Map<String, int[]> grantedMap = new HashMap<>();
    Map<String, int[]> deniedMap = new HashMap<>();
    Map<String, int[]> rationaleMap = new HashMap<>();
    Map<String, int[]> customRationaleMap = new HashMap<>();
    Map<String, int[]> nonRationaleMap = new HashMap<>();
    // requestCode -> methodName
    Map<Integer, String> singleGrantMap = new HashMap<>();
    Map<Integer, String> singleDeniedMap = new HashMap<>();
    Map<Integer, String> singleRationaleMap = new HashMap<>();
    Map<Integer, String> singleCustomRationaleMap = new HashMap<>();
    Map<Integer, String> singleNonRationaleMap = new HashMap<>();
    // sync request
    Map<int[], String[]> syncPermissions = new HashMap<>(1);
    private int firstRequestCode;
    private String firstRequestPermission;

    ProxyClass(Elements util, TypeElement element) {
        this.element = element;
        packageName = util.getPackageOf(element).getQualifiedName().toString();
        String className = getClassName(element, packageName);
        proxyName = className + CONCAT + PERMISSIONS_PROXY;
    }

    String getProxyName() {
        return proxyName;
    }

    TypeElement getElement() {
        return element;
    }

    public JavaFile generateJavaCode() {

        List<MethodSpec> list = generateMethodCode();
        TypeSpec.Builder finderClass = TypeSpec.classBuilder(proxyName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(PERMISSIONS_PROXY_INJECTOR, TypeName.get(element.asType())));
        for(MethodSpec ms : list){
            finderClass.addMethod(ms);
        }
        return JavaFile.builder(packageName, finderClass.build()).build();
    }

    private List generateMethodCode() {
        List list = new ArrayList<MethodSpec>();
        list.add(generateGrantedMethod());
        list.add(generateDeniedMethod());
        list.add(generateRationaleMethod());
        list.add(generateCustomRationaleMethod());
        list.add(generatePageIntent());
        list.add(generateSyncRequestPermissionsMethod());
        return list;
    }

    private MethodSpec generateSyncRequestPermissionsMethod() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("startSyncRequestPermissionsMethod")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addStatement("$T.requestPermission(object,$S,$L)", PERMISSIONS_PROXY_PERMISSIONS4M, firstRequestPermission, firstRequestCode);
        return injectMethodBuilder.build();
    }

    private MethodSpec generateCustomRationaleMethod() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("customRationale")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(boolean.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addParameter(TypeName.INT, "code");
        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(code)");
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
        injectMethodBuilder.addCode(caseBlock.build());
        return injectMethodBuilder.build();
    }

    private MethodSpec generatePageIntent() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("intent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addParameter(TypeName.INT, "code")
                .addParameter(ClassName.get("android.content", "Intent"), "intent");
        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(code)");
        for (Map.Entry<String, int[]> entry : nonRationaleMap.entrySet()) {
            String methodName = entry.getKey();
            int[] values = entry.getValue();
            for (int value : values) {
                caseBlock.add("case $L:", value).indent();
                if (singleNonRationaleMap.containsKey(value)) {
                    singleNonRationaleMap.remove(value);
                }
            }
            caseBlock.addStatement("object.$N(code, intent)", methodName);
            caseBlock.addStatement("break").unindent();

        }
        for (Map.Entry<Integer, String> entry : singleNonRationaleMap.entrySet()) {
            Integer integer = entry.getKey();
            caseBlock.add("case $L:", integer).indent();
            caseBlock.addStatement("object.$N(intent)", entry.getValue());
            caseBlock.addStatement("break").unindent();
        }
        caseBlock.add("default:\n").indent().addStatement("break").unindent();
        caseBlock.endControlFlow();
        injectMethodBuilder.addCode(caseBlock.build());
        return injectMethodBuilder.build();
    }

    private MethodSpec generateRationaleMethod() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("rationale")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addParameter(TypeName.INT, "code");
        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(code)");
        for (Map.Entry<String, int[]> entry : rationaleMap.entrySet()) {
            String methodName = entry.getKey();
            int[] ints = entry.getValue();
            for (int requestCode : ints) {
                caseBlock.add("case $L:\n", requestCode).indent();
                caseBlock.addStatement("object.$N($L)", methodName, requestCode).addStatement("break").unindent();
                if (singleRationaleMap.containsKey(requestCode)) {
                    singleRationaleMap.remove(requestCode);
                }
            }
        }
        for (Map.Entry<Integer, String> entry : singleRationaleMap.entrySet()) {
            int requestCode = entry.getKey();
            caseBlock.add("case $L:\n", requestCode).indent();
            caseBlock.addStatement("object.$N()", entry.getValue());
            caseBlock.addStatement("break").unindent();
        }
        caseBlock.add("default:\n").indent().addStatement("break").unindent();
        caseBlock.endControlFlow();
        injectMethodBuilder.addCode(caseBlock.build());
        return injectMethodBuilder.build();
    }

    private MethodSpec generateDeniedMethod() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("denied")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addParameter(TypeName.INT, "code");
        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(code)");
        for (Map.Entry<String, int[]> entry : deniedMap.entrySet()) {
            String methodName = entry.getKey();
            int[] ints = entry.getValue();
            for (int requestCode : ints) {
                caseBlock.add("case $L:", requestCode).indent();
                caseBlock.addStatement("object.$N($L)", methodName, requestCode);
                // judge whether need write request permission method
                addSyncRequestPermissionMethod(caseBlock, requestCode);
                caseBlock.addStatement("break").unindent();
                if (singleDeniedMap.containsKey(requestCode)) {
                    singleDeniedMap.remove(requestCode);
                }
            }
        }
        for (Map.Entry<Integer, String> entry : singleDeniedMap.entrySet()) {
            int requestCode = entry.getKey();
            caseBlock.add("case $L:\n", requestCode).indent();
            caseBlock.addStatement("object.$N()", entry.getValue());
            caseBlock.addStatement("break").unindent();
        }
        caseBlock.add("default:\n").indent().addStatement("break").unindent();
        caseBlock.endControlFlow();
        injectMethodBuilder.addCode(caseBlock.build());
        return injectMethodBuilder.build();
    }

    private MethodSpec generateGrantedMethod() {

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("granted")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeName.get(element.asType()), "object", Modifier.FINAL)
                .addParameter(TypeName.INT, "code");
        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch(code)");
        for (Map.Entry<String, int[]> entry : grantedMap.entrySet()) {
            String methodName = entry.getKey();
            int[] ints = entry.getValue();
            for (int requestCode : ints) {
                caseBlock.add("case $L:", requestCode);
                caseBlock.addStatement("object.$N($L)", methodName, requestCode);
                // judge whether need write request permission method
                addSyncRequestPermissionMethod(caseBlock, requestCode);
                caseBlock.addStatement("break");
                if (singleGrantMap.containsKey(requestCode)) {
                    singleGrantMap.remove(requestCode);
                }
            }
        }
        for (Map.Entry<Integer, String> entry : singleGrantMap.entrySet()) {
            int requestCode = entry.getKey();
            caseBlock.add("case $L:", requestCode);
            caseBlock.addStatement("object.$N()", entry.getValue());
            caseBlock.addStatement("break");
        }
        caseBlock.add("default:\n").indent().addStatement("break").unindent();
        caseBlock.endControlFlow();
        injectMethodBuilder.addCode(caseBlock.build());
        return injectMethodBuilder.build();
    }

    private void addSyncRequestPermissionMethod(CodeBlock.Builder caseBlock, int targetRequestCode) {
        // syncPermissions size is 1
        for (Map.Entry<int[], String[]> entry : syncPermissions.entrySet()) {
            int[] requestCodes = entry.getKey();
            String[] permissions = entry.getValue();
            int length = requestCodes.length;
            // when syncRequestPermission size is 1
            if (length == 1) {
                firstRequestCode = requestCodes[0];
                firstRequestPermission = permissions[0];
            } else {
                // when syncRequestPermission size bigger than 1
                for (int i = 0; i < length - 1; i++) {
                    if (i == 0) {
                        firstRequestCode = requestCodes[0];
                        firstRequestPermission = permissions[0];
                    }
                    if (requestCodes[i] == targetRequestCode) {
                        caseBlock.addStatement("$T.requestPermission(object, $S, $L)", PERMISSIONS_PROXY_PERMISSIONS4M, permissions[i + 1], requestCodes[i + 1]);
                    }
                }
            }
        }
    }

    private String getClassName(TypeElement element, String packageName) {
        int packageLen = packageName.length() + 1;
        return element.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }
}
