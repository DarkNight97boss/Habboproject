-- Catalog captions fixups: residual EN color suffixes etc.
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE catalog_pages SET caption='USVA - Giallo' WHERE id=164;
UPDATE catalog_pages SET caption='USVA - Verde' WHERE id=165;
UPDATE catalog_pages SET caption='USVA - Rosso' WHERE id=166;
UPDATE catalog_pages SET caption='USVA - Blu' WHERE id=167;
UPDATE catalog_pages SET caption='USVA - Rosa' WHERE id=168;
COMMIT;
