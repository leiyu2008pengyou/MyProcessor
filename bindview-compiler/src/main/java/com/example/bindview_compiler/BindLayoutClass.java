package com.example.bindview_compiler;


import com.example.BindLayout;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by leiyu on 2018/1/31.
 */

public class BindLayoutClass {
    private int mLayoutId;

    public BindLayoutClass(Element element) throws IllegalArgumentException{
        if(element.getKind() != ElementKind.CLASS){
            throw new IllegalArgumentException(String.format("Only fields can be annotated with @%s", BindLayout.class.getSimpleName()));
        }
        TypeElement typeElement = (TypeElement) element;
        mLayoutId = typeElement.getAnnotation(BindLayout.class).value();
        if(mLayoutId == 0){
            throw new IllegalArgumentException(String.format(
                    "@%s for a Activity must specify one layout ID. Found: %s. (%s.%s)",
                    BindLayout.class.getSimpleName(), mLayoutId, typeElement.getQualifiedName(),
                    element.getSimpleName()
            ));
        }
    }

    public int getLayoutId(){
        return mLayoutId;
    }
}
