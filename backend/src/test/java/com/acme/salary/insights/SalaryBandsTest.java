package com.acme.salary.insights;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryBandsTest {

    @Test
    void splitsTheCountryRangeIntoEqualWidthBands() {
        List<SalaryBand> bands = SalaryBands.build(bd("100"), bd("600"), 5, Map.of(0, 2L, 4, 1L));

        assertThat(bands).extracting(SalaryBand::from)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(bd("100"), bd("200"), bd("300"), bd("400"), bd("500"));
        assertThat(bands.get(4).to()).isEqualByComparingTo("600");
    }

    @Test
    void bandsWithNoEmployeesAreShownWithZeroSoTheChartHasNoGaps() {
        List<SalaryBand> bands = SalaryBands.build(bd("100"), bd("600"), 5, Map.of(0, 2L, 4, 1L));

        assertThat(bands).extracting(SalaryBand::headcount).containsExactly(2L, 0L, 0L, 0L, 1L);
    }

    @Test
    void highestSalaryFallsIntoTheLastBandNotAnExtraOne() {
        // bucket index == bands happens only for salary == max; it belongs to the last band
        List<SalaryBand> bands = SalaryBands.build(bd("100"), bd("600"), 5, Map.of(5, 1L, 4, 2L));

        assertThat(bands).hasSize(5);
        assertThat(bands.get(4).headcount()).isEqualTo(3L);
    }

    @Test
    void whenEveryoneEarnsTheSameThereIsASingleBand() {
        List<SalaryBand> bands = SalaryBands.build(bd("500"), bd("500"), 5, Map.of(0, 4L));

        assertThat(bands).singleElement().satisfies(band -> {
            assertThat(band.from()).isEqualByComparingTo("500");
            assertThat(band.to()).isEqualByComparingTo("500");
            assertThat(band.headcount()).isEqualTo(4L);
        });
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
