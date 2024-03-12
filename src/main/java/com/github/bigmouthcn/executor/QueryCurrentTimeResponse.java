package com.github.bigmouthcn.executor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class QueryCurrentTimeResponse {

    private int plusDays;
    private String dateTimeAfterPlusDays = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

    public QueryCurrentTimeResponse setTime3(int diffDates) {
        this.dateTimeAfterPlusDays = LocalDateTime
                .now()
                .plusDays(diffDates)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return this;
    }
}
