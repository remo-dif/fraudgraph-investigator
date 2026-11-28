package com.fraudgraph.investigator.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public final class CsvPreviewValidator {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final int MAX_ROWS = 50_000;
    private static final int MAX_COLUMNS = 64;
    private static final int MAX_ERRORS = 100;
    private static final int MAX_PREVIEW = 25;
    private static final Map<String, Integer> CURRENCY_SCALE = Map.of(
            "EUR", 2, "USD", 2, "GBP", 2, "JPY", 0, "KWD", 3);
    private static final Set<Field> REQUIRED = Set.of(
            Field.ACCOUNT_ID, Field.EVENT_ID, Field.EVENT_TYPE, Field.TIMESTAMP);

    public enum Field {
        ACCOUNT_ID, EVENT_ID, EVENT_TYPE, TIMESTAMP, CAMPAIGN_ID, DEVICE_ID, IP_TOKEN,
        BENEFICIARY_ID, REWARD_AMOUNT, PAYOUT_AMOUNT, CURRENCY, STATUS, FRAUD_FLAG
    }

    public enum EventType { ACTIVITY, REWARD, PAYOUT, TRANSACTION }
    public enum Status { COMPLETED, PENDING, REVERSED, UNKNOWN }
    public enum Code {
        BYTE_LIMIT, ROW_LIMIT, INVALID_ENCODING, MALFORMED_CSV, INVALID_HEADER,
        INVALID_MAPPING, NO_DATA, COLUMN_COUNT, REQUIRED_VALUE, INVALID_VALUE, CONFLICTING_EVENT
    }

    public record Event(String accountId, String eventId, EventType type, Instant timestamp,
                        String campaignId, String deviceId, String ipToken, String beneficiaryId,
                        Long rewardMinor, Long payoutMinor, String currency, Status status,
                        Boolean fraudFlag) {}

    public record Row(long recordNumber, Event event) {}
    public record Error(long recordNumber, Field field, Code code) {}

    public record Preview(int scannedRows, int uniqueEvents, int duplicateRows,
                          List<Row> sample, List<Error> errors) {
        public boolean valid() {
            return errors.isEmpty();
        }
    }

    // Mapping keys are canonical fields and values are exact source header names.
    public Preview validate(InputStream input, Map<Field, String> mapping) throws IOException {
        var scan = new Scan();
        byte[] bytes = input.readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) {
            return scan.fail(0, Code.BYTE_LIMIT);
        }
        String csv;
        try {
            csv = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException exception) {
            return scan.fail(0, Code.INVALID_ENCODING);
        }
        if (csv.startsWith("\uFEFF")) {
            csv = csv.substring(1);
        }
        if (mapping.keySet().stream().anyMatch(java.util.Objects::isNull)
                || !mapping.keySet().containsAll(REQUIRED)
                || mapping.values().stream().anyMatch(value -> value == null || value.isBlank())
                || new HashSet<>(mapping.values()).size() != mapping.size()) {
            return scan.fail(1, Code.INVALID_MAPPING);
        }
        boolean headerRead = false;
        try (var parser = CSVFormat.RFC4180.parse(new java.io.StringReader(csv))) {
            var records = parser.iterator();
            if (!records.hasNext()) {
                return scan.fail(1, Code.INVALID_HEADER);
            }
            List<String> header = records.next().toList();
            headerRead = true;
            if (header.size() > MAX_COLUMNS || new HashSet<>(header).size() != header.size()
                    || header.stream().anyMatch(String::isBlank)) {
                return scan.fail(1, Code.INVALID_HEADER);
            }
            var columns = new EnumMap<Field, Integer>(Field.class);
            for (var entry : mapping.entrySet()) {
                int index = header.indexOf(entry.getValue());
                if (index < 0) {
                    return scan.fail(1, Code.INVALID_MAPPING);
                }
                columns.put(entry.getKey(), index);
            }
            while (records.hasNext()) {
                if (scan.rows == MAX_ROWS) {
                    return scan.fail(scan.rows + 2L, Code.ROW_LIMIT);
                }
                scan.accept(records.next(), header.size(), columns);
            }
            return scan.rows == 0 ? scan.fail(2, Code.NO_DATA) : scan.result();
        } catch (UncheckedIOException exception) {
            return scan.fail(headerRead ? scan.rows + 2L : 1L, Code.MALFORMED_CSV);
        }
    }

    private static Event normalize(CSVRecord record, Map<Field, Integer> columns) {
        var values = new EnumMap<Field, String>(Field.class);
        columns.forEach((field, index) -> {
            String raw = record.get(index);
            String value = raw.strip();
            if (raw.length() > 256 || raw.codePoints().anyMatch(Character::isISOControl)) {
                throw new Invalid(field, Code.INVALID_VALUE);
            }
            if (value.isEmpty() && REQUIRED.contains(field)) {
                throw new Invalid(field, Code.REQUIRED_VALUE);
            }
            if (!value.isEmpty()) {
                values.put(field, value);
            }
        });
        Instant timestamp;
        try {
            timestamp = OffsetDateTime.parse(values.get(Field.TIMESTAMP)).toInstant();
        } catch (DateTimeParseException exception) {
            throw new Invalid(Field.TIMESTAMP, Code.INVALID_VALUE);
        }
        String currency = values.containsKey(Field.CURRENCY)
                ? values.get(Field.CURRENCY).toUpperCase(Locale.ROOT) : null;
        if (currency == null && (values.containsKey(Field.REWARD_AMOUNT) || values.containsKey(Field.PAYOUT_AMOUNT))) {
            throw new Invalid(Field.CURRENCY, Code.REQUIRED_VALUE);
        }
        if (currency != null && !CURRENCY_SCALE.containsKey(currency)) {
            throw new Invalid(Field.CURRENCY, Code.INVALID_VALUE);
        }
        return new Event(values.get(Field.ACCOUNT_ID), values.get(Field.EVENT_ID),
                enumValue(values, Field.EVENT_TYPE, EventType.class, null), timestamp,
                values.get(Field.CAMPAIGN_ID), values.get(Field.DEVICE_ID), values.get(Field.IP_TOKEN),
                values.get(Field.BENEFICIARY_ID), amount(values, Field.REWARD_AMOUNT, currency),
                amount(values, Field.PAYOUT_AMOUNT, currency), currency,
                enumValue(values, Field.STATUS, Status.class, Status.UNKNOWN), fraudFlag(values.get(Field.FRAUD_FLAG)));
    }

    private static <E extends Enum<E>> E enumValue(Map<Field, String> values, Field field, Class<E> type, E fallback) {
        String value = values.get(field);
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new Invalid(field, Code.INVALID_VALUE);
        }
    }

    private static Long amount(Map<Field, String> values, Field field, String currency) {
        String value = values.get(field);
        if (value == null) {
            return null;
        }
        int scale = CURRENCY_SCALE.get(currency);
        if (!value.matches("[0-9]+(\\.[0-9]+)?")) {
            throw new Invalid(field, Code.INVALID_VALUE);
        }
        BigDecimal decimal = new BigDecimal(value);
        if (decimal.scale() > scale) {
            throw new Invalid(field, Code.INVALID_VALUE);
        }
        try {
            return decimal.movePointRight(scale).longValueExact();
        } catch (ArithmeticException exception) {
            throw new Invalid(field, Code.INVALID_VALUE);
        }
    }

    private static Boolean fraudFlag(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new Invalid(Field.FRAUD_FLAG, Code.INVALID_VALUE);
        };
    }

    private static final class Scan {
        private final Map<String, Event> events = new HashMap<>();
        private final List<Row> sample = new ArrayList<>();
        private final List<Error> errors = new ArrayList<>();
        private int rows;
        private int duplicates;

        private void accept(CSVRecord record, int width, Map<Field, Integer> columns) {
            rows++;
            long number = record.getRecordNumber();
            if (record.size() != width) {
                error(number, null, Code.COLUMN_COUNT);
                return;
            }
            try {
                Event event = normalize(record, columns);
                Event previous = events.putIfAbsent(event.eventId(), event);
                if (previous == null && sample.size() < MAX_PREVIEW) {
                    sample.add(new Row(number, event));
                } else if (event.equals(previous)) {
                    duplicates++;
                } else if (previous != null) {
                    error(number, Field.EVENT_ID, Code.CONFLICTING_EVENT);
                }
            } catch (Invalid exception) {
                error(number, exception.field, exception.code);
            }
        }

        private void error(long record, Field field, Code code) {
            if (errors.size() < MAX_ERRORS) {
                errors.add(new Error(record, field, code));
            }
        }

        private Preview fail(long record, Code code) {
            error(record, null, code);
            return result();
        }

        private Preview result() {
            return new Preview(rows, events.size(), duplicates,
                    errors.isEmpty() ? List.copyOf(sample) : List.of(), List.copyOf(errors));
        }
    }

    private static final class Invalid extends RuntimeException {
        private final Field field;
        private final Code code;

        private Invalid(Field field, Code code) {
            this.field = field;
            this.code = code;
        }
    }
}
