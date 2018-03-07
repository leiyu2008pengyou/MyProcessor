package com.example.bindview_compiler;

import com.example.BindLayout;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by leiyu on 2018/1/31.
 */
@AutoService(Processor.class)
public class LeiyuProcessor extends AbstractProcessor{
    /**
     * 使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
     */

    private Filer mFiler; //文件相关的辅助类
    private Elements mElementUtils; //元素相关的辅助类
    private Messager mMessager; //日志相关的辅助类
    private Map<String, AnnotationClass> mAnnotationClassMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    /**
     * @return 指定哪些注解应该被注解处理器注册
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
//        types.add(BindView.class.getCanonicalName());
//        types.add(OnClick.class.getCanonicalName());
        types.add(BindLayout.class.getCanonicalName());
        return types;
    }

    /**
     * @return 指定使用的 Java 版本。通常返回 SourceVersion.latestSupported()。
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(mAnnotationClassMap != null){
            mAnnotationClassMap.clear();
        }
        try {
//            processBindView(roundEnv);
//            processOnClick(roundEnv);
            processBindLayout(roundEnv);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            return true; // stop process
        }
        try {
            for (AnnotationClass annotatedClass : mAnnotationClassMap.values()) {
                info("Generating file for %s", annotatedClass.getFullClassName());
                annotatedClass.generateFinder().writeTo(mFiler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            error("Generate file failed, reason: %s", e.getMessage());
        }
        return true;
    }

    private void processBindLayout(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindLayout.class)) {
            AnnotationClass annotatedClass = getAnnotatedClassForBindLayout(element);
            BindLayoutClass bindLayoutClass = new BindLayoutClass(element);
            annotatedClass.setContentLayoutId(bindLayoutClass);
        }
    }

    private AnnotationClass getAnnotatedClassForBindLayout(Element element) {
        //不需要获取父类
        TypeElement enclosingElement = (TypeElement) element;
        String fullClassName = enclosingElement.getQualifiedName().toString();
        AnnotationClass annotatedClass = mAnnotationClassMap.get(fullClassName);
        if (annotatedClass == null) {
            annotatedClass = new AnnotationClass(enclosingElement, mElementUtils);
            mAnnotationClassMap.put(fullClassName, annotatedClass);
        }
        return annotatedClass;
    }

    private void error(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    private void info(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }
}
