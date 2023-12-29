package com.lcx.phoneBook;

import java.io.Serializable;

/**
 * @author lcx
 * @version 1.0
 */
public class Contact implements Serializable {
    //姓名，电话，工作单位，住址，群组
    private final String id;
    private String name;
    private String phone;
    private String workUnit;
    private String address;
    private String group;

    public Contact(String id,String name, String phone, String workUnit, String address, String group) {
        this.name = name;
        this.phone = phone;
        this.workUnit = workUnit;
        this.address = address;
        this.group = group;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWorkUnit() {
        return workUnit;
    }

    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    //重写toString方法，以，分隔
    @Override
    public String toString() {
        return id + "，" + name + "，" + phone + "，" + workUnit + "，" + address + "，" + group;
    }
}
