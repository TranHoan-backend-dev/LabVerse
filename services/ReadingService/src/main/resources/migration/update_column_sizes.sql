-- Migration script: Update column sizes in reading_workflow table to support encoded IDs
-- Run this script on SQL Server database: LabVerse_Reading_Service
-- Encoded IDs (base64) can be longer than 36 characters, so we need to increase column sizes

-- Step 1: Drop all foreign key constraints that reference reading_workflow columns
-- This is necessary because SQL Server doesn't allow ALTER COLUMN when foreign keys exist

DECLARE @sql NVARCHAR(MAX) = '';
DECLARE @fkName NVARCHAR(128);

-- Drop foreign keys referencing collection_id, paper_id, or usersid in reading_workflow table
DECLARE fk_cursor CURSOR FOR
SELECT 
    fk.name AS ForeignKeyName
FROM 
    sys.foreign_keys fk
    INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
    INNER JOIN sys.columns c ON fkc.parent_column_id = c.column_id AND fkc.parent_object_id = c.object_id
    INNER JOIN sys.tables t ON c.object_id = t.object_id
WHERE 
    t.name = 'reading_workflow'
    AND c.name IN ('collection_id', 'paper_id', 'usersid');

OPEN fk_cursor;
FETCH NEXT FROM fk_cursor INTO @fkName;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = 'ALTER TABLE reading_workflow DROP CONSTRAINT ' + QUOTENAME(@fkName) + ';';
    EXEC sp_executesql @sql;
    PRINT 'Dropped foreign key constraint: ' + @fkName;
    FETCH NEXT FROM fk_cursor INTO @fkName;
END;

CLOSE fk_cursor;
DEALLOCATE fk_cursor;

-- Also drop foreign keys from join tables that reference reading_workflow
-- Drop foreign keys from reading_workflow_note
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reading_workflow_note')
BEGIN
    DECLARE fk_note_cursor CURSOR FOR
    SELECT 
        fk.name AS ForeignKeyName
    FROM 
        sys.foreign_keys fk
        INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
        INNER JOIN sys.columns c ON fkc.parent_column_id = c.column_id AND fkc.parent_object_id = c.object_id
        INNER JOIN sys.tables t ON c.object_id = t.object_id
    WHERE 
        t.name = 'reading_workflow_note'
        AND c.name IN ('reading_workflow_collection_id', 'reading_workflow_paper_id', 'reading_workflow_usersid');

    OPEN fk_note_cursor;
    FETCH NEXT FROM fk_note_cursor INTO @fkName;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @sql = 'ALTER TABLE reading_workflow_note DROP CONSTRAINT ' + QUOTENAME(@fkName) + ';';
        EXEC sp_executesql @sql;
        PRINT 'Dropped foreign key constraint from reading_workflow_note: ' + @fkName;
        FETCH NEXT FROM fk_note_cursor INTO @fkName;
    END;

    CLOSE fk_note_cursor;
    DEALLOCATE fk_note_cursor;
END;

-- Drop foreign keys from reading_workflow_highlight
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reading_workflow_highlight')
BEGIN
    DECLARE fk_highlight_cursor CURSOR FOR
    SELECT 
        fk.name AS ForeignKeyName
    FROM 
        sys.foreign_keys fk
        INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
        INNER JOIN sys.columns c ON fkc.parent_column_id = c.column_id AND fkc.parent_object_id = c.object_id
        INNER JOIN sys.tables t ON c.object_id = t.object_id
    WHERE 
        t.name = 'reading_workflow_highlight'
        AND c.name IN ('reading_workflow_collection_id', 'reading_workflow_paper_id', 'reading_workflow_usersid');

    OPEN fk_highlight_cursor;
    FETCH NEXT FROM fk_highlight_cursor INTO @fkName;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @sql = 'ALTER TABLE reading_workflow_highlight DROP CONSTRAINT ' + QUOTENAME(@fkName) + ';';
        EXEC sp_executesql @sql;
        PRINT 'Dropped foreign key constraint from reading_workflow_highlight: ' + @fkName;
        FETCH NEXT FROM fk_highlight_cursor INTO @fkName;
    END;

    CLOSE fk_highlight_cursor;
    DEALLOCATE fk_highlight_cursor;
END;

PRINT 'All foreign key constraints dropped. Proceeding with column size updates...';

-- Step 2: Update collection_id column size
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow]') AND name = 'collection_id')
BEGIN
    ALTER TABLE reading_workflow 
    ALTER COLUMN collection_id VARCHAR(255) NOT NULL;
    PRINT 'Updated collection_id column size to 255';
END
ELSE
BEGIN
    PRINT 'collection_id column does not exist';
END

-- Step 3: Update paper_id column size
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow]') AND name = 'paper_id')
BEGIN
    ALTER TABLE reading_workflow 
    ALTER COLUMN paper_id VARCHAR(255) NOT NULL;
    PRINT 'Updated paper_id column size to 255';
END
ELSE
BEGIN
    PRINT 'paper_id column does not exist';
END

-- Step 4: Update usersid column size
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow]') AND name = 'usersid')
BEGIN
    ALTER TABLE reading_workflow 
    ALTER COLUMN usersid VARCHAR(255) NOT NULL;
    PRINT 'Updated usersid column size to 255';
END
ELSE
BEGIN
    PRINT 'usersid column does not exist';
END

PRINT 'Note: Foreign key constraints will be automatically recreated by JPA/Hibernate when the application restarts.';

-- Also update join table columns if they exist
-- Update reading_workflow_note join table
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reading_workflow_note')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_note]') AND name = 'reading_workflow_collection_id')
    BEGIN
        ALTER TABLE reading_workflow_note 
        ALTER COLUMN reading_workflow_collection_id VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_note.reading_workflow_collection_id column size to 255';
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_note]') AND name = 'reading_workflow_paper_id')
    BEGIN
        ALTER TABLE reading_workflow_note 
        ALTER COLUMN reading_workflow_paper_id VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_note.reading_workflow_paper_id column size to 255';
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_note]') AND name = 'reading_workflow_usersid')
    BEGIN
        ALTER TABLE reading_workflow_note 
        ALTER COLUMN reading_workflow_usersid VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_note.reading_workflow_usersid column size to 255';
    END
END

-- Update reading_workflow_highlight join table
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reading_workflow_highlight')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_highlight]') AND name = 'reading_workflow_collection_id')
    BEGIN
        ALTER TABLE reading_workflow_highlight 
        ALTER COLUMN reading_workflow_collection_id VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_highlight.reading_workflow_collection_id column size to 255';
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_highlight]') AND name = 'reading_workflow_paper_id')
    BEGIN
        ALTER TABLE reading_workflow_highlight 
        ALTER COLUMN reading_workflow_paper_id VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_highlight.reading_workflow_paper_id column size to 255';
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow_highlight]') AND name = 'reading_workflow_usersid')
    BEGIN
        ALTER TABLE reading_workflow_highlight 
        ALTER COLUMN reading_workflow_usersid VARCHAR(255) NOT NULL;
        PRINT 'Updated reading_workflow_highlight.reading_workflow_usersid column size to 255';
    END
END

PRINT 'Migration completed! All column sizes updated to 255 to support encoded IDs.';

