package com.example.bindview_compiler;

import com.example.OnClick;
import com.example.PermissionCheck;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

/**
 * Created by leiyu on 2018/3/12.
 */

public class PermissionCheckClass implements MethodInterface{
    private Name mMethodName;
    public String[] ids;


    public PermissionCheckClass(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException(String.format("Only methods can be annotated with @%s", OnClick.class.getSimpleName()));
        }
        ExecutableElement methodElement = (ExecutableElement) element;
        this.mMethodName = methodElement.getSimpleName();
        this.ids = methodElement.getAnnotation(PermissionCheck.class).value();
        if (ids == null) {
            throw new IllegalArgumentException(String.format("Must set valid ids for @%s", OnClick.class.getSimpleName()));
        }

        // method params count must equals 0
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (parameters.size() > 0) {
            throw new IllegalArgumentException(
                    String.format("The method annotated with @%s must have no parameters", OnClick.class.getSimpleName()));
        }
    }

    @Override
    public Name getMethodName() {
        return mMethodName;
    }
}
