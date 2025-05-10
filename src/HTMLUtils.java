import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLUtils {
    public static String extractFirst(String html,String regex) {
        // 定义正则表达式，使用非贪婪匹配并启用多行模式
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL); // DOTALL 模式让 . 匹配包括换行符的所有字符
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            // 返回第一个匹配组的内容，并去除首尾空白（可选）
            return matcher.group(1).trim();
        } else {
            return null; // 未找到匹配项
        }
    }

    public static String getTitle(String html){
        return extractFirst(html,"<title>(.*?)</title>");
    }

    public static String getUserName(String html){
        return extractFirst(html,"<h2 class=\"name\">(.*?)</h2>");
    }

    public static String getUserGroup(String html){
        String userGroup =extractFirst(html,"<li>用户组(.*?)</font></span></li>");
        return userGroup.substring(userGroup.lastIndexOf(">")+1);
    }

    public static String getRegTime(String html){
        return extractFirst(html,"<li>注册时间<span>(.*?)</span></li>");
    }

    public static String getActTime(String html){
        return extractFirst(html,"<li>最后访问<span>(.*?)</span></li>");
    }
}
