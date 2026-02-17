package com.ecommerce.order.controller;

import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.dtos.ReturnRequestDTO;
import com.ecommerce.order.models.RefundMethod;
import com.ecommerce.order.models.RefundStatus;
import com.ecommerce.order.models.ReturnStatus;
import com.ecommerce.order.services.ReturnRefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReturnRefundController.class)
class ReturnRefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReturnRefundService returnRefundService;

    @Test
    void shouldReturnAdminReturnListWithPaginationAndStatusFilter() throws Exception {
        ReturnRequestDTO dto = new ReturnRequestDTO(
                1L,
                10L,
                "1001",
                "demo-customer-001",
                "Damaged",
                List.of("https://example.com/photo.jpg"),
                ReturnStatus.PENDING,
                new BigDecimal("25.00"),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(returnRefundService.getAllReturnRequests(eq("PENDING"), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/returns/admin")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        verify(returnRefundService).getAllReturnRequests("PENDING", 0, 10);
    }

    @Test
    void shouldReturnAdminStats() throws Exception {
        SellerReturnStatsDTO stats = SellerReturnStatsDTO.builder()
                .totalReturns(12L)
                .pendingReturns(3L)
                .approvedReturns(4L)
                .rejectedReturns(2L)
                .completedReturns(3L)
                .totalRefundAmount(180.0)
                .averageRefundAmount(60.0)
                .build();

        when(returnRefundService.getGlobalReturnStats()).thenReturn(stats);

        mockMvc.perform(get("/api/returns/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReturns").value(12))
                .andExpect(jsonPath("$.completedReturns").value(3))
                .andExpect(jsonPath("$.totalRefundAmount").value(180.0));

        verify(returnRefundService).getGlobalReturnStats();
    }

    @Test
    void shouldRunBackfillAndReturnSummary() throws Exception {
        when(returnRefundService.backfillMissingSellerIds())
                .thenReturn(Map.of("scanned", 5L, "updated", 3L, "skipped", 2L));

        mockMvc.perform(post("/api/returns/admin/backfill-seller-ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanned").value(5))
                .andExpect(jsonPath("$.updated").value(3))
                .andExpect(jsonPath("$.skipped").value(2));

        verify(returnRefundService).backfillMissingSellerIds();
    }

    @Test
    void shouldAllowAdminToScheduleRefundWithDelay() throws Exception {
        RefundDTO refund = new RefundDTO(
                1L,
                10L,
                77L,
                new BigDecimal("50.00"),
                RefundMethod.ORIGINAL,
                RefundStatus.SCHEDULED,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(45),
                null,
                null,
                null
        );

        when(returnRefundService.processRefund(77L, RefundMethod.ORIGINAL, 45, true))
                .thenReturn(java.util.Optional.of(refund));

        mockMvc.perform(post("/api/returns/77/refund")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "method", "ORIGINAL",
                                "delayMinutes", 45
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(returnRefundService).processRefund(77L, RefundMethod.ORIGINAL, 45, true);
    }
}
