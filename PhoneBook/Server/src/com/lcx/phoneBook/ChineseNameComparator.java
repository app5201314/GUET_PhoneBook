package com.lcx.phoneBook;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class ChineseNameComparator implements Comparator<String>, Serializable {
    private transient Collator collator;//transient表示不序列化该字段

    public ChineseNameComparator() {
        collator = Collator.getInstance(Locale.CHINA);
    }

    @Override
    public int compare(String o1, String o2) {
        if (collator == null) {
            collator = Collator.getInstance(Locale.CHINA);
        }
        return collator.compare(o1, o2);
    }
}