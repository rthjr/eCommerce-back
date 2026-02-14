package com.ecommerce.product.controllers;

import com.ecommerce.product.dtos.FAQAnswerRequest;
import com.ecommerce.product.dtos.SellerFAQResponse;
import com.ecommerce.product.services.SellerFAQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/faqs")
public class SellerFAQController {

    private final SellerFAQService faqService;

    @GetMapping
    public ResponseEntity<List<SellerFAQResponse>> getSellerFaqs(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(faqService.getSellerFaqs(sellerId));
    }

    @GetMapping("/unanswered")
    public ResponseEntity<List<SellerFAQResponse>> getUnansweredFaqs(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(faqService.getUnansweredFaqs(sellerId));
    }

    @GetMapping("/hidden")
    public ResponseEntity<List<SellerFAQResponse>> getHiddenFaqs(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(faqService.getHiddenFaqs(sellerId));
    }

    @PutMapping("/{faqId}/answer")
    public ResponseEntity<SellerFAQResponse> answerFaq(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long faqId,
            @RequestBody FAQAnswerRequest request) {
        return faqService.answerFaq(faqId, sellerId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{faqId}/hide")
    public ResponseEntity<SellerFAQResponse> hideFaq(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long faqId) {
        return faqService.hideFaq(faqId, sellerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{faqId}/publish")
    public ResponseEntity<SellerFAQResponse> publishFaq(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long faqId) {
        return faqService.publishFaq(faqId, sellerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
