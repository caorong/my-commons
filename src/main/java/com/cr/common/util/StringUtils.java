package com.cr.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by caorong on 15-3-7 - 下午3:53.
 */
public class StringUtils {

    /**
     * append => ()
     * word => 12, 23
     * 1234 => (123)4
     *
     * @param word
     * @param keys
     * @param headStr
     * @param tailStr
     * @return
     */
    public static String appendSpecificWordAtHeadTail(
            String word, List<String> keys,
            String headStr, String tailStr) {
        TreeSet<Integer> sset = new TreeSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        Map<Integer, Integer> tagsIndex = Maps.newHashMap();
        for (String key : keys) {
            int startindex = word.indexOf(key);
            for (int index = startindex;
                 index >= 0;
                 index = word.indexOf(key, index + 1)) {
//                System.out.println(index);
//                System.out.println(key.length());
                for (int i = 0; i < key.length(); i++) {
                    sset.add(index + i);
//                    tagsIndex.put(startindex,index + i);
                }
            }
        }
        System.out.println(sset);

        // sset to replace with tag
        int start = -1;
        int last = -1;
        for (Integer i : sset) {
            if (start == -1) {
                start = i;
                last = i;
            } else {
                // 连着的
                if (last + 1 == i) {
                    last = i;
                } else {
                    // 断掉, 记录start, end
                    tagsIndex.put(start, last);
                    // 重新记录
                    start = i;
                    last = i;
//                    continue;
                }
                tagsIndex.put(start, last);
            }
        }
        System.out.println(tagsIndex);

        StringBuilder sb = new StringBuilder();

        Integer[] keyarr = tagsIndex.keySet().toArray(new Integer[0]);
        int klength = keyarr.length;
        if (klength > 0) {
            // add head str
            if (keyarr[0] > 0) {
                sb.append(word.substring(0, keyarr[0] + 1));
            }
            for (int i = 0; i < klength; i++) {
                int key = keyarr[i];
                int value = tagsIndex.get(key);
                sb.append(headStr);
                sb.append(word.substring(key, value + 1));
                sb.append(tailStr);
                // add 空白
                if (i + 1 < klength) {
                    sb.append(word.substring(value + 1, keyarr[i + 1]));
                }
            }
        }
        System.out.println(word);
//        System.out.println(sb.toString());
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(appendSpecificWordAtHeadTail("abcdddabccbcdeze", Lists.newArrayList("ab", "bc", "e"), "++", "--"));
    }
}
