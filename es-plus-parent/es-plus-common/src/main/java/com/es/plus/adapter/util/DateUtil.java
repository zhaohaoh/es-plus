package com.es.plus.adapter.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期跑龙套
 *
 * @author hzh
 * @date 2023/07/25
 */
public class DateUtil {
    
    public static Object format(Object date, String format,String timeZone) {
        if (date instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            //转换成指定时区
            if (StringUtils.isNotBlank(timeZone)){
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(timeZone)));
            }
            return simpleDateFormat.format(date);
        } else if (date instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) date;
            //localdatetime本身是不带时区的。now获取的是当前的系统时间。所以先要把他转成带时区的datetime。
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
            if (StringUtils.isNotBlank(timeZone)) {
                zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(timeZone));
            }
            //然后再转成指定时区的字符串
            return zonedDateTime.format(DateTimeFormatter.ofPattern(format));
        } else if (date instanceof LocalDate) {
            LocalDate localDate = (LocalDate) date;
            return  localDate.format(DateTimeFormatter.ofPattern(format));
        } else {
            return date;
        }
    }
    
    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("+8")));
        Date date = new Date();
      
     
        String format1 = format.format(date);
        System.out.println(format1);
    
        SimpleDateFormat format111 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format111.setTimeZone(TimeZone.getTimeZone("UTC+8:00"));
        String format2= format111.format(date);
        System.out.println(format2);
    
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime utc = now.atZone(ZoneId.of("+8"));
        String format3 = utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format3);
    
        ZonedDateTime zonedDateTime = utc.withZoneSameInstant(ZoneId.of("+0"));
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        System.out.println(localDateTime);
    
    }
    
}
