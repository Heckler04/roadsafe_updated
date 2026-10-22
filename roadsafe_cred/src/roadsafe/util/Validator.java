package roadsafe.util;



public class Validator {

    private Validator() {
    }

    public static String fullName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Name cannot be empty.";
        }
        if (text.length() < 2) {
            return "Name must be at least 2 characters.";
        }
        if (text.length() > 100) {
            return "Name must be at most 100 characters.";
        }
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean ok = Character.isLetter(ch) || ch == ' '
                    || ch == '.' || ch == '\'' || ch == '-';
            if (!ok) {
                return "Name may contain only letters, spaces, '.', '-' and '''.";
            }
        }
        return null;
    }

    public static String username(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Username cannot be empty.";
        }
        String msg = "Username must be 3-30 characters: letters, digits, '_' or '.'.";
        if (text.length() < 3 || text.length() > 30) {
            return msg;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean ok = isEnglishLetter(ch) || isDigit(ch)
                    || ch == '_' || ch == '.';
            if (!ok) {
                return msg;
            }
        }
        return null;
    }

    public static String password(String text) {
        if (text == null || text.isEmpty()) {
            return "Password cannot be empty.";
        }
        if (text.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (text.length() > 64) {
            return "Password must be at most 64 characters.";
        }
        return null;
    }

    public static String location(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Location cannot be empty.";
        }
        if (text.length() < 3) {
            return "Location must be at least 3 characters.";
        }
        if (text.length() > 255) {
            return "Location must be at most 255 characters.";
        }
        return null;
    }

    public static String description(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Description cannot be empty.";
        }
        if (text.length() < 5) {
            return "Description must be at least 5 characters.";
        }
        if (text.length() > 1000) {
            return "Description must be at most 1000 characters.";
        }
        return null;
    }

    private static boolean isEnglishLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }
}
