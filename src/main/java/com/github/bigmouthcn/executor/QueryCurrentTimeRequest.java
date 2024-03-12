package com.github.bigmouthcn.executor;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
public class QueryCurrentTimeRequest {

    @JsonPropertyDescription("与今天相差天数，0表示今天，1表示明天，-1表示昨天，以此类推")
    private int plusDays = 0;
}
