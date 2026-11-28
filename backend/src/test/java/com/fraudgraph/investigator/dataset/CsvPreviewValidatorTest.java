package com.fraudgraph.investigator.dataset;

import static com.fraudgraph.investigator.dataset.CsvPreviewValidator.Field.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CsvPreviewValidatorTest {
    private static final String HEADER = "member,source_event,kind,occurred,device,reward,currency\n";
    private static final String TIME = "2026-11-28T12:30:00+01:00";
    private static final Map<CsvPreviewValidator.Field, String> MAPPING = Map.of(
            ACCOUNT_ID, "member", EVENT_ID, "source_event", EVENT_TYPE, "kind", TIMESTAMP, "occurred",
            DEVICE_ID, "device", REWARD_AMOUNT, "reward", CURRENCY, "currency");
    private final CsvPreviewValidator validator = new CsvPreviewValidator();

    @Test
    void previewsSyntheticEventsAndDeduplicatesCanonicalRecords() throws IOException {
        try (var input = getClass().getResourceAsStream("/dataset/synthetic-events.csv")) {
            var preview = validator.validate(input, MAPPING);
            assertThat(preview.valid()).isTrue();
            assertThat(preview.scannedRows()).isEqualTo(3);
            assertThat(preview.uniqueEvents()).isEqualTo(2);
            assertThat(preview.duplicateRows()).isEqualTo(1);
            assertThat(preview.sample()).extracting(row -> row.event().accountId()).containsExactly("0001", "0002");
            var first = preview.sample().getFirst().event();
            assertThat(first.deviceId()).isEqualTo("device,shared");
            assertThat(first.rewardMinor()).isEqualTo(1230L);
            assertThat(first.timestamp()).isEqualTo(Instant.parse("2026-11-28T11:30:00Z"));
            assertThat(preview.sample().getLast().event().rewardMinor()).isNull();
        }
    }

    @Test
    void rejectsConflictsAndSuppressesPartialPreview() throws IOException {
        var preview = validate(HEADER + row("a", TIME, "") + row("b", TIME, ""));
        assertThat(preview.errors()).containsExactly(new CsvPreviewValidator.Error(
                3, EVENT_ID, CsvPreviewValidator.Code.CONFLICTING_EVENT));
        assertThat(preview.sample()).isEmpty();
    }

    @Test
    void rejectsLocalTimestampsAndExcessCurrencyPrecision() throws IOException {
        var preview = validate(HEADER + row("a", "2026-11-28T12:30:00", "") + row("b", TIME, "1.001"));
        assertThat(preview.errors()).extracting(CsvPreviewValidator.Error::field)
                .containsExactly(TIMESTAMP, REWARD_AMOUNT);
    }

    @Test
    void rejectsMalformedCsvAndUtf8() throws IOException {
        assertThat(validate(HEADER + "\"unterminated").errors()).containsExactly(
                new CsvPreviewValidator.Error(2, null, CsvPreviewValidator.Code.MALFORMED_CSV));
        var invalidUtf8 = new ByteArrayInputStream(new byte[] {(byte) 0xc3, 0x28});
        assertThat(validator.validate(invalidUtf8, MAPPING).errors().getFirst().code())
                .isEqualTo(CsvPreviewValidator.Code.INVALID_ENCODING);
    }

    @Test
    void boundsBytesAndRows() throws IOException {
        var tooLarge = new ByteArrayInputStream(new byte[10 * 1024 * 1024 + 1]);
        assertThat(validator.validate(tooLarge, MAPPING).errors().getFirst().code())
                .isEqualTo(CsvPreviewValidator.Code.BYTE_LIMIT);
        var tooMany = validate(HEADER + row("a", TIME, "").repeat(50_001));
        assertThat(tooMany.errors().getFirst().code()).isEqualTo(CsvPreviewValidator.Code.ROW_LIMIT);
        assertThat(tooMany.scannedRows()).isEqualTo(50_000);
        assertThat(tooMany.sample()).isEmpty();
    }

    private CsvPreviewValidator.Preview validate(String csv) throws IOException {
        return validator.validate(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), MAPPING);
    }

    private String row(String account, String time, String amount) {
        return account + ",event,reward," + time + ",," + amount + ",EUR\n";
    }
}
