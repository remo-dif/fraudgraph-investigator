package com.fraudgraph.investigator.dataset;

import java.io.IOException;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datasets")
class DatasetPreviewController {
    private final CsvPreviewValidator validator = new CsvPreviewValidator();

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    ResponseEntity<Map<String, String>> invalidRequest() {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_PREVIEW_REQUEST"));
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<Map<String, String>> unreadableUpload() {
        return ResponseEntity.badRequest().body(Map.of("code", "UNREADABLE_UPLOAD"));
    }

    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CsvPreviewValidator.Preview> preview(
            @RequestPart("file") MultipartFile file,
            @RequestPart("mapping") Map<CsvPreviewValidator.Field, String> mapping) throws IOException {
        try (var input = file.getInputStream()) {
            var preview = validator.validate(input, mapping);
            return ResponseEntity.status(preview.valid() ? 200 : 422)
                    .cacheControl(CacheControl.noStore())
                    .body(preview);
        }
    }
}
