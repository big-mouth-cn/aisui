package com.github.bigmouthcn.executor;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
public class QueryCurrentTimeFunction implements Function<QueryCurrentTimeRequest, Object> {
    @Override
    public Object apply(QueryCurrentTimeRequest queryCurrentTimeRequest) {
        int plusDays = Optional.ofNullable(queryCurrentTimeRequest).orElse(new QueryCurrentTimeRequest()).getPlusDays();
        return new QueryCurrentTimeResponse().setPlusDays(plusDays).setTime3(plusDays);
    }
}
