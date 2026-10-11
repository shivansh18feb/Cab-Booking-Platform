-- ==============================================================================
-- Flyway Migration V2: Initial Reference and Promotional Data
-- ==============================================================================

-- Seed Promo Codes
INSERT INTO promo_codes (code, discount_type, discount_value, minimum_fare, maximum_discount, valid_from, valid_until, usage_limit, times_used, active)
VALUES 
('WELCOME20', 'PERCENTAGE', 20.00, 10.00, 15.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '90 days', 5000, 0, TRUE),
('FLAT5OFF', 'FLAT', 5.00, 15.00, 5.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '60 days', 2000, 0, TRUE),
('VIPRIDE', 'PERCENTAGE', 30.00, 25.00, 25.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '180 days', 1000, 0, TRUE)
ON CONFLICT (code) DO NOTHING;
