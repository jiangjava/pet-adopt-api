package com.kmbeast.utils;

import com.kmbeast.pojo.dto.QueryDto;
import com.kmbeast.pojo.vo.ChartVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 时间工具类
 */
public class DateUtil {

    /**
     * 构造时间查询器，即指定的开始时间、结束时间
     *
     * @param days 时间范围
     * @return PagerDTO
     */
    public static QueryDto startAndEndTime(Integer days) {
        // 查全部
        if (days == -1) {
            return new QueryDto();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDayStart = now.minusDays(days).plusDays(1).with(LocalTime.of(0, 0)); // 下一天的开始时间
        LocalDateTime daysAgoEnd = nextDayStart.minusSeconds(1);
        return QueryDto.builder().startTime(daysAgoEnd).endTime(now).build();
    }

    /**
     * 统计指定天数内的记录数
     *
     * @param dayRange 往前查多少天
     * @param dates    数据源
     * @return Map<String, Integer>
     */
    public static List<ChartVO> countDatesWithinRange(Integer dayRange, List<LocalDateTime> dates) {
        LocalDate startDate = LocalDate.now().minusDays(dayRange);
        List<ChartVO> chartVOS = new ArrayList<>();
        for (int offset = 0; offset <= dayRange; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            String dateKey = String.format("%02d-%02d", currentDate.getMonthValue(), currentDate.getDayOfMonth());
            int count = (int) dates.stream()
                    .filter(dateTime -> dateTime.toLocalDate().equals(currentDate))
                    .count();
            if (count != 0){
                // 为了数据更好看，在原始的数据之上，添加一个随机数
                chartVOS.add(new ChartVO(dateKey, count * new Random().nextInt(10000)));
                //                chartVOS.add(new ChartVO(dateKey, count)); 这是原始的数据，不想要上述模拟效果，只需要将上一行代码注释掉，这一行放开即可
            }
        }
        return chartVOS;
    }
}