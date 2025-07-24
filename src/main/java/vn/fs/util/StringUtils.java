package vn.fs.util;


public class StringUtils {
    // get first character from string, and to uppercase
    private static String getCharacter(String str) {
        if (str == null || str.isEmpty()) {
            return ""; // Trả về chuỗi rỗng nếu str là null hoặc rỗng
        }
        return str.substring(0, 1).toUpperCase();
    }


    // generate link author avatar follow struct : https://placehold.co/200x200?text=[...]
    public static String generateLinkImage(String name) {
        return "https://placehold.co/200x200?text=" + getCharacter(name);
    }
}
