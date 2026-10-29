package com.acme.salary.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure band maths, kept out of SQL and Spring so it is trivial to unit test.
 * Bands are relative to one country's own min..max range, because a fixed band such as
 * "50,000-60,000" means something completely different in INR and in USD.
 */
final class SalaryBands {

    private SalaryBands() {
    }

    /**
     * @param countsByBucket headcount per bucket index, as computed by the database
     *                       (index = floor((salary - min) * bands / (max - min)))
     */
    static List<SalaryBand> build(BigDecimal min, BigDecimal max, int bands, Map<Integer, Long> countsByBucket) {
        long total = countsByBucket.values().stream().mapToLong(Long::longValue).sum();
        if (min.compareTo(max) == 0) {
            return List.of(new SalaryBand(min, max, total));
        }

        BigDecimal width = max.subtract(min).divide(BigDecimal.valueOf(bands), 2, RoundingMode.HALF_UP);
        long[] headcounts = new long[bands];
        countsByBucket.forEach((bucket, count) -> headcounts[Math.min(bucket, bands - 1)] += count);

        List<SalaryBand> result = new ArrayList<>(bands);
        for (int i = 0; i < bands; i++) {
            BigDecimal from = min.add(width.multiply(BigDecimal.valueOf(i)));
            BigDecimal to = i == bands - 1 ? max : from.add(width);
            result.add(new SalaryBand(from, to, headcounts[i]));
        }
        return result;
    }
}
