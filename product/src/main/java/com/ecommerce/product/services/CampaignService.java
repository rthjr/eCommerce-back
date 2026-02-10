package com.ecommerce.product.services;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.models.Campaign;
import com.ecommerce.product.models.Campaign.CampaignStatus;
import com.ecommerce.product.models.CouponCode;
import com.ecommerce.product.repositories.CampaignRepository;
import com.ecommerce.product.repositories.CouponCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CouponCodeRepository couponCodeRepository;
    
    private static final String COUPON_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public CampaignResponse createCampaign(String sellerId, CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setSellerId(sellerId);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setType(request.getType());
        campaign.setDiscountType(request.getDiscountType());
        campaign.setDiscountValue(request.getDiscountValue());
        campaign.setMinimumPurchase(request.getMinimumPurchase());
        campaign.setMaximumDiscount(request.getMaximumDiscount());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setProductIds(request.getProductIds());
        campaign.setAllProducts(request.getAllProducts());
        campaign.setUsageLimit(request.getUsageLimit());
        campaign.setPerCustomerLimit(request.getPerCustomerLimit());
        campaign.setStatus(CampaignStatus.DRAFT);
        
        Campaign saved = campaignRepository.save(campaign);
        return mapToResponse(saved);
    }

    public Optional<CampaignResponse> getCampaignById(Long id) {
        return campaignRepository.findById(id).map(this::mapToResponse);
    }

    public List<CampaignResponse> getSellerCampaigns(String sellerId) {
        return campaignRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<CampaignResponse> getSellerCampaignsPaged(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return campaignRepository.findBySellerId(sellerId, pageable).map(this::mapToResponse);
    }

    public List<CampaignResponse> getActiveCampaigns(String sellerId) {
        return campaignRepository.findActiveRunningCampaigns(sellerId, LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<CampaignResponse> updateCampaign(Long id, CampaignRequest request) {
        return campaignRepository.findById(id).map(campaign -> {
            campaign.setName(request.getName());
            campaign.setDescription(request.getDescription());
            campaign.setType(request.getType());
            campaign.setDiscountType(request.getDiscountType());
            campaign.setDiscountValue(request.getDiscountValue());
            campaign.setMinimumPurchase(request.getMinimumPurchase());
            campaign.setMaximumDiscount(request.getMaximumDiscount());
            campaign.setStartDate(request.getStartDate());
            campaign.setEndDate(request.getEndDate());
            campaign.setProductIds(request.getProductIds());
            campaign.setAllProducts(request.getAllProducts());
            campaign.setUsageLimit(request.getUsageLimit());
            campaign.setPerCustomerLimit(request.getPerCustomerLimit());
            return mapToResponse(campaignRepository.save(campaign));
        });
    }

    @Transactional
    public Optional<CampaignResponse> activateCampaign(Long id) {
        return campaignRepository.findById(id).map(campaign -> {
            campaign.setStatus(CampaignStatus.ACTIVE);
            campaign.setIsActive(true);
            return mapToResponse(campaignRepository.save(campaign));
        });
    }

    @Transactional
    public Optional<CampaignResponse> pauseCampaign(Long id) {
        return campaignRepository.findById(id).map(campaign -> {
            campaign.setStatus(CampaignStatus.PAUSED);
            campaign.setIsActive(false);
            return mapToResponse(campaignRepository.save(campaign));
        });
    }

    @Transactional
    public Optional<CampaignResponse> scheduleCampaign(Long id) {
        return campaignRepository.findById(id).map(campaign -> {
            campaign.setStatus(CampaignStatus.SCHEDULED);
            return mapToResponse(campaignRepository.save(campaign));
        });
    }

    @Transactional
    public Optional<CampaignResponse> endCampaign(Long id) {
        return campaignRepository.findById(id).map(campaign -> {
            campaign.setStatus(CampaignStatus.ENDED);
            campaign.setIsActive(false);
            return mapToResponse(campaignRepository.save(campaign));
        });
    }

    @Transactional
    public boolean deleteCampaign(Long id) {
        if (campaignRepository.existsById(id)) {
            // Delete associated coupon codes
            couponCodeRepository.findByCampaignId(id).forEach(coupon -> 
                couponCodeRepository.delete(coupon));
            campaignRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Coupon Code management
    @Transactional
    public CouponCodeResponse createCouponCode(String sellerId, CouponCodeRequest request) {
        CouponCode coupon = new CouponCode();
        coupon.setSellerId(sellerId);
        
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            if (couponCodeRepository.existsByCode(request.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists");
            }
            coupon.setCode(request.getCode());
        } else {
            coupon.setCode(generateCouponCode(8));
        }
        
        if (request.getCampaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
            coupon.setCampaign(campaign);
        }
        
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setPerCustomerLimit(request.getPerCustomerLimit());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setIsActive(true);
        
        CouponCode saved = couponCodeRepository.save(coupon);
        return mapToCouponResponse(saved);
    }

    public List<CouponCodeResponse> getSellerCoupons(String sellerId) {
        return couponCodeRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    public Optional<CouponCodeResponse> validateCouponCode(String code) {
        return couponCodeRepository.findByCodeAndIsActiveTrue(code)
                .filter(CouponCode::isValid)
                .map(this::mapToCouponResponse);
    }

    @Transactional
    public Optional<CouponCodeResponse> useCouponCode(String code) {
        return couponCodeRepository.findByCodeAndIsActiveTrue(code)
                .filter(CouponCode::isValid)
                .map(coupon -> {
                    coupon.setUsageCount(coupon.getUsageCount() + 1);
                    if (coupon.getCampaign() != null) {
                        Campaign campaign = coupon.getCampaign();
                        campaign.setUsageCount(campaign.getUsageCount() + 1);
                        campaign.setConversions(campaign.getConversions() + 1);
                        campaignRepository.save(campaign);
                    }
                    return mapToCouponResponse(couponCodeRepository.save(coupon));
                });
    }

    @Transactional
    public boolean deactivateCoupon(Long id) {
        return couponCodeRepository.findById(id).map(coupon -> {
            coupon.setIsActive(false);
            couponCodeRepository.save(coupon);
            return true;
        }).orElse(false);
    }

    // Campaign performance tracking
    @Transactional
    public void trackCampaignView(Long campaignId) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            campaign.setViews(campaign.getViews() + 1);
            campaignRepository.save(campaign);
        });
    }

    @Transactional
    public void trackCampaignClick(Long campaignId) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            campaign.setClicks(campaign.getClicks() + 1);
            campaignRepository.save(campaign);
        });
    }

    @Transactional
    public void trackCampaignConversion(Long campaignId, BigDecimal revenue) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            campaign.setConversions(campaign.getConversions() + 1);
            campaign.setRevenueGenerated(campaign.getRevenueGenerated().add(revenue));
            campaignRepository.save(campaign);
        });
    }

    public CampaignStatsResponse getCampaignStats(String sellerId) {
        List<Campaign> campaigns = campaignRepository.findBySellerId(sellerId);
        
        long total = campaigns.size();
        long active = campaigns.stream().filter(c -> c.getStatus() == CampaignStatus.ACTIVE).count();
        long scheduled = campaigns.stream().filter(c -> c.getStatus() == CampaignStatus.SCHEDULED).count();
        long ended = campaigns.stream().filter(c -> c.getStatus() == CampaignStatus.ENDED).count();
        
        int totalViews = campaigns.stream().mapToInt(Campaign::getViews).sum();
        int totalClicks = campaigns.stream().mapToInt(Campaign::getClicks).sum();
        int totalConversions = campaigns.stream().mapToInt(Campaign::getConversions).sum();
        BigDecimal totalRevenue = campaigns.stream()
                .map(Campaign::getRevenueGenerated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double conversionRate = totalClicks > 0 ? (double) totalConversions / totalClicks * 100 : 0;
        
        return new CampaignStatsResponse(
                total, active, scheduled, ended,
                totalViews, totalClicks, totalConversions, totalRevenue, conversionRate
        );
    }

    private String generateCouponCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(COUPON_CHARACTERS.charAt(random.nextInt(COUPON_CHARACTERS.length())));
        }
        String code = sb.toString();
        if (couponCodeRepository.existsByCode(code)) {
            return generateCouponCode(length);
        }
        return code;
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
        CampaignResponse response = new CampaignResponse();
        response.setId(campaign.getId());
        response.setSellerId(campaign.getSellerId());
        response.setName(campaign.getName());
        response.setDescription(campaign.getDescription());
        response.setType(campaign.getType());
        response.setDiscountType(campaign.getDiscountType());
        response.setDiscountValue(campaign.getDiscountValue());
        response.setMinimumPurchase(campaign.getMinimumPurchase());
        response.setMaximumDiscount(campaign.getMaximumDiscount());
        response.setStartDate(campaign.getStartDate());
        response.setEndDate(campaign.getEndDate());
        response.setStatus(campaign.getStatus());
        response.setIsActive(campaign.getIsActive());
        response.setProductIds(campaign.getProductIds());
        response.setAllProducts(campaign.getAllProducts());
        response.setUsageLimit(campaign.getUsageLimit());
        response.setUsageCount(campaign.getUsageCount());
        response.setPerCustomerLimit(campaign.getPerCustomerLimit());
        response.setViews(campaign.getViews());
        response.setClicks(campaign.getClicks());
        response.setConversions(campaign.getConversions());
        response.setRevenueGenerated(campaign.getRevenueGenerated());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        return response;
    }

    private CouponCodeResponse mapToCouponResponse(CouponCode coupon) {
        CouponCodeResponse response = new CouponCodeResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setSellerId(coupon.getSellerId());
        response.setCampaignId(coupon.getCampaign() != null ? coupon.getCampaign().getId() : null);
        response.setCampaignName(coupon.getCampaign() != null ? coupon.getCampaign().getName() : null);
        response.setUsageLimit(coupon.getUsageLimit());
        response.setUsageCount(coupon.getUsageCount());
        response.setPerCustomerLimit(coupon.getPerCustomerLimit());
        response.setIsActive(coupon.getIsActive());
        response.setExpiresAt(coupon.getExpiresAt());
        response.setCreatedAt(coupon.getCreatedAt());
        response.setIsValid(coupon.isValid());
        return response;
    }
}
