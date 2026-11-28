package com.fraudgraph.investigator.dataset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(controllers = DatasetPreviewController.class,
        properties = "fraudgraph.preview.token=synthetic-test-token-at-least-32-bytes")
@Import(PreviewSecurity.class)
class DatasetPreviewControllerTest {
    private static final String TOKEN = "Bearer synthetic-test-token-at-least-32-bytes";
    @Autowired
    private MockMvc mvc;

    @Test
    void requiresCredentialsBeforeProcessingUpload() throws Exception {
        mvc.perform(request("invalid")).andExpect(status().isUnauthorized());
        mvc.perform(request("invalid").header("Authorization", "Bearer wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsAuthorizedPreviewWithNoStore() throws Exception {
        mvc.perform(request("a,e,activity,2026-11-28T10:00:00Z").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.uniqueEvents").value(1));
    }

    @Test
    void returnsRedactedValidationErrors() throws Exception {
        mvc.perform(request("a,e,activity,sensitive-raw-value").header("Authorization", TOKEN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].code").value("INVALID_VALUE"))
                .andExpect(jsonPath("$.sample").isEmpty());
    }

    private MockMultipartHttpServletRequestBuilder request(String row) {
        return multipart("/api/datasets/preview")
                .file(new MockMultipartFile("file", "events.csv", "text/csv",
                        ("account,event,type,time\n" + row).getBytes(StandardCharsets.UTF_8)))
                .file(new MockMultipartFile("mapping", "mapping.json", "application/json", """
                        {"ACCOUNT_ID":"account","EVENT_ID":"event","EVENT_TYPE":"type","TIMESTAMP":"time"}
                        """.getBytes(StandardCharsets.UTF_8)));
    }
}
