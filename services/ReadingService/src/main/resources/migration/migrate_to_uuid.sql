-- Migration script: Convert reading_workflow from composite key to UUID primary key
-- Run this script on SQL Server database: LabVerse_Reading_Service

-- Step 1: Drop existing primary key constraint if exists
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[PK_reading_workflow]') AND type in (N'PK'))
BEGIN
    ALTER TABLE reading_workflow DROP CONSTRAINT PK_reading_workflow;
END

-- Step 2: Drop existing foreign key constraints if any
-- (Add more if you have foreign keys referencing this table)
-- ALTER TABLE other_table DROP CONSTRAINT FK_...;

-- Step 3: Add new workflow_id column (UUID)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow]') AND name = 'workflow_id')
BEGIN
    ALTER TABLE reading_workflow 
    ADD workflow_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID();
END

-- Step 4: Set workflow_id for existing rows if any
UPDATE reading_workflow 
SET workflow_id = NEWID() 
WHERE workflow_id IS NULL;

-- Step 5: Remove default constraint (we want auto-generate in application)
DECLARE @constraint_name NVARCHAR(200);
SELECT @constraint_name = name 
FROM sys.default_constraints 
WHERE parent_object_id = OBJECT_ID('reading_workflow')
AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('reading_workflow'), 'workflow_id', 'ColumnId');

IF @constraint_name IS NOT NULL
BEGIN
    EXEC('ALTER TABLE reading_workflow DROP CONSTRAINT ' + @constraint_name);
END

-- Step 6: Drop old composite key columns (collection_id, paper_id, user_id) if they exist
-- NOTE: Only drop if you're sure no other tables reference them
-- ALTER TABLE reading_workflow DROP COLUMN collection_id, paper_id, user_id;

-- Actually, keep collection_id, paper_id, user_id as regular columns (not part of PK)
-- They are still useful for filtering/querying

-- Step 7: Create new primary key on workflow_id
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[PK_reading_workflow_workflow_id]') AND type in (N'PK'))
BEGIN
    ALTER TABLE reading_workflow 
    ADD CONSTRAINT PK_reading_workflow_workflow_id PRIMARY KEY (workflow_id);
END

-- Step 8: Make workflow_id not allow null and add default (for new inserts)
ALTER TABLE reading_workflow 
ALTER COLUMN workflow_id UNIQUEIDENTIFIER NOT NULL;

-- Verification
SELECT 
    'Migration completed!' AS Status,
    COUNT(*) AS TotalRows
FROM reading_workflow;

