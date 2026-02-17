package dev.kayzao.nyc311.web;

import dev.kayzao.nyc311.model.ServiceRequest;
import dev.kayzao.nyc311.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceRequestController.class)
class ServiceRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceRequestService serviceRequestService;

    @Test
    void api311ReturnsOk() throws Exception {
        ServiceRequest row = new ServiceRequest();
        row.setServiceRequestNumber("SR-123");
        row.setCreatedDate(OffsetDateTime.parse("2026-02-17T00:00:00Z"));
        row.setClosedDate(null);
        row.setComplaintType("Noise - Residential");
        row.setDescriptor("Loud Music/Party");
        row.setBorough("MANHATTAN");
        row.setLatitude(40.7128);
        row.setLongitude(-74.0060);

        when(serviceRequestService.searchRequests(
                anyInt(),
                any(),
                anyBoolean(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble()
        )).thenReturn(List.of(row));

        mockMvc.perform(get("/api/311"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].service_request_number").value("SR-123"))
                .andExpect(jsonPath("$.items[0].created_date").value("2026-02-17T00:00:00Z"))
                .andExpect(jsonPath("$.items[0].borough").value("MANHATTAN"))
                .andExpect(jsonPath("$.items[0].latitude").value(40.7128))
                .andExpect(jsonPath("$.items[0].longitude").value(-74.006))
                .andExpect(jsonPath("$.next_token").isEmpty());
    }

    @Test
    void invalidBboxReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/311").param("bbox", "1,2,3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bbox must be 'minLon,minLat,maxLon,maxLat'"));
    }

}
