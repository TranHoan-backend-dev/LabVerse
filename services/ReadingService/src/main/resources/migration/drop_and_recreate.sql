-- Quick fix: Drop and recreate table (WILL LOSE ALL DATA!)
-- Use this if you don't have important data or want to start fresh

USE LabVerse_Reading_Service;
GO

-- Drop table if exists
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[reading_workflow]') AND type in (N'U'))
BEGIN
    DROP TABLE reading_workflow;
    PRINT 'Table reading_workflow dropped.';
END

-- Hibernate will auto-create the table with correct schema on next startup
-- Make sure spring.jpa.hibernate.ddl-auto=update or create in application.properties

PRINT 'Table dropped. Restart the application to auto-create new table.';
GO

