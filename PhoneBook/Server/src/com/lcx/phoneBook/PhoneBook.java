package com.lcx.phoneBook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lcx
 * @version 1.0
 */
public class PhoneBook implements Serializable {
    private final Map<String, Contact> contacts;

    public PhoneBook(Map<String, Contact> contacts) {
        this.contacts = contacts;
    }

    //遍历phoneBook，找到没有被占用的id
    public String getNewId() {
        int id = 0;
        for (Contact contact : contacts.values()) {
            if (Integer.parseInt(contact.getId()) > id) {
                id = Integer.parseInt(contact.getId());
            }
        }
        return String.valueOf(id + 1);
    }

    //模糊查询
    public String searchContacts(String query) {
        // 将查询结果转换为字符串，每个联系人之间用逗号分隔
        StringBuilder sb = new StringBuilder();
        for (Contact contact : contacts.values()) {
            if (contact.getName().contains(query) ||
                    contact.getPhone().contains(query) ||
                    contact.getWorkUnit().contains(query) ||
                    contact.getAddress().contains(query) ||
                    contact.getGroup().contains(query)) {
                sb.append(contact).append(",");
            }
        }

        if (sb.length() > 0) {//去掉最后一个逗号
            sb.deleteCharAt(sb.length() - 1);
        }

        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    //根据id查询
    public String searchContactById(String id) {
        //遍历所有联系人，找到id相同的联系人
        for (Contact contact : contacts.values()) {
            if (contact.getId().equals(id)) {
                return contact.toString();
            }
        }
        return null;
    }

    public Map<String, Contact> getContacts() {
        return contacts;
    }

    // 将查询结果按id排序并转换为字符串，每个联系人之间用逗号分隔
    public String getAllContacts() {
        // 将查询结果转换为字符串，每个联系人之间用逗号分隔
        StringBuilder sb = new StringBuilder();
        for (Contact contact : contacts.values()) {
            sb.append(contact).append(",");
        }

        if (sb.length() == 0) {
            return null;
        }

        // 去掉最后一个逗号
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public String searchGroup(String groupName) {
        // 将查询结果转换为字符串，每个联系人之间用逗号分隔
        StringBuilder sb = new StringBuilder();
        for (Contact contact : contacts.values()) {
            if (contact.getGroup().equals(groupName)) {
                sb.append(contact).append(",");
            }
        }

        // 去掉最后一个逗号
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    //检查是否已经存在该联系人，依据是姓名和电话同时一致
    public boolean IsExisted(String name, String phone) {
        for (Contact contact : contacts.values()) {
            if (contact.getName().equals(name) && contact.getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    public boolean deleteContactById(String id) {
        String del = null;
        //遍历所有联系人，找到id相同的联系人
        for (Contact contact : contacts.values()) {
            if (contact.getId().equals(id)) {
                del = contact.getName() + contact.getId();
                break;
            }
        }

        if (del != null) {
            contacts.remove(del);
            return true;
        } else {
            return false;
        }
    }

    public void deleteAllContacts() {
        contacts.clear();
    }

    public void updateContact(String id, String name, String phone, String workUnit, String address, String group) {
        String oldKey = null;
        Contact updateContact = null;
        
        //遍历所有联系人，找到id相同的联系人
        for (Contact contact : contacts.values()) {
            if (contact.getId().equals(id)) {
                oldKey = contact.getName() + contact.getId();
                updateContact = contact;
            }
        }

        if (updateContact == null) {
            return;
        }
        
        if (!name.equals(" ")) {
            updateContact.setName(name);
        }
        if (!phone.equals(" ")) {
            updateContact.setPhone(phone);
        }
        if (!workUnit.equals(" ")) {
            updateContact.setWorkUnit(workUnit);
        }
        if (!address.equals(" ")) {
            updateContact.setAddress(address);
        }
        if (!group.equals(" ")) {
            updateContact.setGroup(group);
        }
        
        contacts.remove(oldKey);
        contacts.put(name + id, updateContact);
    }

    public void deleteGroup(String content) {
        //遍历所有联系人，找到群组相同的联系人
        List<String> toRemove = new ArrayList<>();

        for (Contact contact : contacts.values()) {
            if (contact.getGroup().equals(content)) {
                toRemove.add(contact.getName() + contact.getId());
            }
        }

        for (String key : toRemove) {
            contacts.remove(key);
        }
    }
}