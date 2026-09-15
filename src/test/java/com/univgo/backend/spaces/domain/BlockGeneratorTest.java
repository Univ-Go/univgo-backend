package com.univgo.backend.spaces.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BlockGeneratorTest {

    private static final Duration BLOCK_DURATION = Duration.ofMinutes(120);
    private static final UUID SPACE_ID = UUID.randomUUID();

    @Test
    void mondayToFridayWindowProducesEightTwoHourBlocks() {
        SpaceSchedule schedule = new SpaceSchedule(
                UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(6, 0), LocalTime.of(22, 0));

        List<TimeBlock> blocks = BlockGenerator.generate(List.of(schedule), BLOCK_DURATION);

        assertThat(blocks).containsExactly(
                new TimeBlock(LocalTime.of(6, 0), LocalTime.of(8, 0)),
                new TimeBlock(LocalTime.of(8, 0), LocalTime.of(10, 0)),
                new TimeBlock(LocalTime.of(10, 0), LocalTime.of(12, 0)),
                new TimeBlock(LocalTime.of(12, 0), LocalTime.of(14, 0)),
                new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0)),
                new TimeBlock(LocalTime.of(16, 0), LocalTime.of(18, 0)),
                new TimeBlock(LocalTime.of(18, 0), LocalTime.of(20, 0)),
                new TimeBlock(LocalTime.of(20, 0), LocalTime.of(22, 0)));
    }

    @Test
    void saturdayWindowProducesFiveTwoHourBlocks() {
        SpaceSchedule schedule = new SpaceSchedule(
                UUID.randomUUID(), SPACE_ID, 6, LocalTime.of(8, 0), LocalTime.of(18, 0));

        List<TimeBlock> blocks = BlockGenerator.generate(List.of(schedule), BLOCK_DURATION);

        assertThat(blocks).hasSize(5);
        assertThat(blocks.get(0)).isEqualTo(new TimeBlock(LocalTime.of(8, 0), LocalTime.of(10, 0)));
        assertThat(blocks.get(4)).isEqualTo(new TimeBlock(LocalTime.of(16, 0), LocalTime.of(18, 0)));
    }

    @Test
    void noScheduleForTheDayProducesNoBlocks() {
        List<TimeBlock> blocks = BlockGenerator.generate(List.of(), BLOCK_DURATION);

        assertThat(blocks).isEmpty();
    }

    @Test
    void trailingPartialWindowIsDropped() {
        SpaceSchedule schedule = new SpaceSchedule(
                UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(6, 0), LocalTime.of(21, 0));

        List<TimeBlock> blocks = BlockGenerator.generate(List.of(schedule), BLOCK_DURATION);

        // 6-8,8-10,10-12,12-14,14-16,16-18,18-20 fit; the trailing 20:00-21:00 (1h) does not.
        assertThat(blocks).hasSize(7);
        assertThat(blocks.getLast()).isEqualTo(new TimeBlock(LocalTime.of(18, 0), LocalTime.of(20, 0)));
    }
}
