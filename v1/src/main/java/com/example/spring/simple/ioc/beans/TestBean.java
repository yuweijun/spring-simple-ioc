package com.example.spring.simple.ioc.beans;

import java.util.List;

public class TestBean {

    private String name;

    private List<?> list;

    private Object objRef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<?> getList() {
        return list;
    }

    public void setList(List<?> list) {
        this.list = list;
    }

    public Object getObjRef() {
        return objRef;
    }

    public void setObjRef(Object object) {
        this.objRef = object;
    }

    @Override
    public String toString() {
        return "TestBean{" +
            "name='" + name + '\'' +
            ", list=" + list +
            ", objRef=" + objRef +
            '}';
    }
}
