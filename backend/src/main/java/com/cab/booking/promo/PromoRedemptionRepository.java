package com.cab.booking.promo;

import com.cab.booking.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoRedemptionRepository extends JpaRepository<PromoRedemption, Long> {
    long countByPromoCodeAndUser(PromoCode promoCode, User user);
    Page<PromoRedemption> findByPromoCodeOrderByRedeemedAtDesc(PromoCode promoCode, Pageable pageable);
}
