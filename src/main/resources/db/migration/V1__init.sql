-- Phase 1 baseline: no JPA entities exist yet. This migration exists purely to give
-- Flyway a starting point (creates flyway_schema_history) so that Phase 2+ migrations
-- (customer/admin, book catalog, cart/purchase/stock, review) have a clean,
-- ordered history from V2 onward instead of Flyway having nothing to run at all.
SELECT 1;
