package com.ecommerce.product.services;

import com.ecommerce.product.dtos.FAQAnswerRequest;
import com.ecommerce.product.dtos.SellerFAQResponse;
import com.ecommerce.product.models.ProductFAQ;
import com.ecommerce.product.repositories.ProductFAQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerFAQService {

    private final ProductFAQRepository faqRepository;

    public List<SellerFAQResponse> getSellerFaqs(String sellerId) {
        return faqRepository.findByProductSellerId(sellerId)
                .stream()
                .map(this::mapToSellerFaqResponse)
                .collect(Collectors.toList());
    }

    public List<SellerFAQResponse> getUnansweredFaqs(String sellerId) {
        return faqRepository.findUnansweredBySellerId(sellerId)
                .stream()
                .map(this::mapToSellerFaqResponse)
                .collect(Collectors.toList());
    }

    public List<SellerFAQResponse> getHiddenFaqs(String sellerId) {
        return faqRepository.findHiddenBySellerId(sellerId)
                .stream()
                .map(this::mapToSellerFaqResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<SellerFAQResponse> answerFaq(Long faqId, String sellerId, FAQAnswerRequest request) {
        return faqRepository.findById(faqId)
                .filter(faq -> faq.getProduct().getSellerId().equals(sellerId))
                .map(faq -> {
                    faq.setAnswer(request.getAnswer());
                    return mapToSellerFaqResponse(faqRepository.save(faq));
                });
    }

    @Transactional
    public Optional<SellerFAQResponse> hideFaq(Long faqId, String sellerId) {
        return faqRepository.findById(faqId)
                .filter(faq -> faq.getProduct().getSellerId().equals(sellerId))
                .map(faq -> {
                    faq.setHidden(true);
                    return mapToSellerFaqResponse(faqRepository.save(faq));
                });
    }

    @Transactional
    public Optional<SellerFAQResponse> publishFaq(Long faqId, String sellerId) {
        return faqRepository.findById(faqId)
                .filter(faq -> faq.getProduct().getSellerId().equals(sellerId))
                .map(faq -> {
                    faq.setHidden(false);
                    return mapToSellerFaqResponse(faqRepository.save(faq));
                });
    }

    private SellerFAQResponse mapToSellerFaqResponse(ProductFAQ faq) {
        SellerFAQResponse response = new SellerFAQResponse();
        response.setId(faq.getId());
        response.setProductId(faq.getProduct().getId());
        response.setProductName(faq.getProduct().getName());
        response.setQuestion(faq.getQuestion());
        response.setAnswer(faq.getAnswer());
        response.setHidden(faq.getHidden());
        return response;
    }
}
