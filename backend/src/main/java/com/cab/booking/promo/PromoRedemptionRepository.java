package com.cab.booking.promo;

import com.cab.booking.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoRedemptionRepository extends JpaRepository<PromoRedemption, Long> {
    long countByPromoCodeAndUser(PromoCode promoCode, User user);
}
