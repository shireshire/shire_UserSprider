import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    // 定义格式器（线程安全，可复用）
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    /**
     * 将包含日期和时间的字符串解析为 LocalDate
     * @param dateTimeStr 格式示例："2025-3-20 07:54"
     * @return LocalDate 对象
     * @throws DateTimeParseException 如果字符串格式不匹配
     */
    public static LocalDate parseToLocalDate(String dateTimeStr) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        return dateTime.toLocalDate();
    }
}