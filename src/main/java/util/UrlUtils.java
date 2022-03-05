package util;

public class UrlUtils {
    public static String getUrl(String data) {
        return split(data)[1];
    }

    public static String getQueryString(String url) {
        int qsIndex = url.indexOf("?");
        return url.substring(qsIndex + 1);
    }

    public static String[] split(String data) {
        return data.split(" ");
    }
}
