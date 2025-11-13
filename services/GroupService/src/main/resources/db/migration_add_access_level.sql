-- Migration: Add access_level column to collection_user table
-- Date: 2025-01-XX
-- Description: Add access_level column to support READ_ONLY, CONTRIBUTOR, and AUTHOR access levels

-- Add access_level column
ALTER TABLE collection_user 
ADD COLUMN access_level VARCHAR(20);

-- Set default values based on existing isAuthor
-- If isAuthor = true, set to AUTHOR
UPDATE collection_user 
SET access_level = 'AUTHOR' 
WHERE isAuthor = true;

-- If isAuthor = false or NULL, set to CONTRIBUTOR (default for existing members)
UPDATE collection_user 
SET access_level = 'CONTRIBUTOR' 
WHERE isAuthor = false OR isAuthor IS NULL;

-- Optional: Add constraint to ensure valid values
-- ALTER TABLE collection_user 
-- ADD CONSTRAINT chk_access_level 
-- CHECK (access_level IN ('READ_ONLY', 'CONTRIBUTOR', 'AUTHOR'));

-- Note: After migration, new members will have access_level set explicitly
-- The isAuthor field is kept for backward compatibility

