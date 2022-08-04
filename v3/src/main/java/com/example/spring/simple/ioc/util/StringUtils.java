package com.example.spring.simple.ioc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public abstract class StringUtils {

    private static final String CHANGE_PATH = "/";        // folder sep.

    private static final String WIN_CHANGE_PATH = "\\";    // Windows folder sep.

    private static final String TOP_PATH = "..";            // Top folder

    private static final String CURRENT_PATH = ".";        // Current folder

    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    public static boolean hasText(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static int countOccurrencesOf(String s, String sub) {
        if (s == null || sub == null || "".equals(sub)) {
            return 0;
        }
        int count = 0, pos = 0, idx = 0;
        while ((idx = s.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    public static String replace(String inString, String oldPattern, String newPattern) {
        if (inString == null) {
            return null;
        }
        if (oldPattern == null || newPattern == null) {
            return inString;
        }

        StringBuffer sbuf = new StringBuffer();
        // output StringBuffer we'll build up
        int pos = 0; // Our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sbuf.append(inString, pos, index);
            sbuf.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sbuf.append(inString.substring(pos));

        // remember to append any characters to the right of a match
        return sbuf.toString();
    }

    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    public static String deleteAny(String inString, String chars) {
        if (inString == null || chars == null) {
            return inString;
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (chars.indexOf(c) == -1) {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String[] tokenizeToStringArray(String s, String delimiters,
        boolean trimTokens, boolean ignoreEmptyTokens) {
        StringTokenizer st = new StringTokenizer(s, delimiters);
        List tokens = new ArrayList();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!(ignoreEmptyTokens && token.length() == 0)) {
                tokens.add(token);
            }
        }
        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    public static String[] delimitedListToStringArray(String s, String delim) {
        if (s == null) {
            return new String[0];
        }
        if (delim == null) {
            return new String[]{s};
        }

        List l = new LinkedList();
        int pos = 0;
        int delPos = 0;
        while ((delPos = s.indexOf(delim, pos)) != -1) {
            l.add(s.substring(pos, delPos));
            pos = delPos + delim.length();
        }
        if (pos <= s.length()) {
            // add rest of String
            l.add(s.substring(pos));
        }

        return (String[]) l.toArray(new String[l.size()]);
    }

    public static String[] commaDelimitedListToStringArray(String s) {
        return delimitedListToStringArray(s, ",");
    }

    public static Set commaDelimitedListToSet(String s) {
        Set set = new TreeSet();
        String[] tokens = commaDelimitedListToStringArray(s);
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i]);
        }
        return set;
    }

    public static String arrayToDelimitedString(Object[] arr, String delim) {
        if (arr == null) {
            return "null";
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(delim);
                sb.append(arr[i]);
            }
            return sb.toString();
        }
    }

    public static String collectionToDelimitedString(Collection c, String delim) {
        if (c == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        Iterator it = c.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (i++ > 0) {
                sb.append(delim);
            }
            sb.append(it.next());
        }
        return sb.toString();
    }

    public static String arrayToCommaDelimitedString(Object[] arr) {
        return arrayToDelimitedString(arr, ",");
    }

    public static String collectionToCommaDelimitedString(Collection c) {
        return collectionToDelimitedString(c, ",");
    }

    public static String[] addStringToArray(String[] arr, String s) {
        String[] newArr = new String[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = s;
        return newArr;
    }

    public static String unqualify(String qualifiedName) {
        return unqualify(qualifiedName, '.');
    }

    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    public static String capitalize(String str) {
        return changeFirstCharacterCase(true, str);
    }

    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(false, str);
    }

    private static String changeFirstCharacterCase(boolean capitalize, String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        StringBuffer buf = new StringBuffer(strLen);
        if (capitalize) {
            buf.append(Character.toUpperCase(str.charAt(0)));
        } else {
            buf.append(Character.toLowerCase(str.charAt(0)));
        }
        buf.append(str.substring(1));
        return buf.toString();
    }

    public static String cleanPath(String path) {
        String p = replace(path, WIN_CHANGE_PATH, CHANGE_PATH);
        String[] pArray = delimitedListToStringArray(p, CHANGE_PATH);
        List pList = new LinkedList();
        int tops = 0;
        for (int i = pArray.length - 1; i >= 0; i--) {
            if (CURRENT_PATH.equals(pArray[i])) {
                // Do nothing
            } else if (TOP_PATH.equals(pArray[i])) {
                tops++;
            } else {
                if (tops > 0) {
                    tops--;
                } else {
                    pList.add(0, pArray[i]);
                }
            }
        }
        return collectionToDelimitedString(pList, CHANGE_PATH);
    }

    public static boolean pathEquals(String path1, String path2) {
        return cleanPath(path1).equals(cleanPath(path2));
    }

}
