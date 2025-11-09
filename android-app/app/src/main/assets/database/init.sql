-- LabVerse Android App - Room Database Initialization Script
-- This script contains SQL statements for initializing the Room database
-- Database name: labverse-db
-- Version: 1

-- Note: Room uses SQLite under the hood, so these are SQLite-compatible statements

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Users (
    id TEXT NOT NULL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    username TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    avatarUrl TEXT,
    roleId TEXT NOT NULL,
    FOREIGN KEY(roleId) REFERENCES Roles(id)
);

CREATE INDEX IF NOT EXISTS index_Users_roleId ON Users(roleId);
CREATE INDEX IF NOT EXISTS index_Users_email ON Users(email);

-- ============================================
-- ROLES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Roles (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

-- ============================================
-- COLLECTIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Collections (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    privacy TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    createdBy TEXT NOT NULL,
    FOREIGN KEY(createdBy) REFERENCES Users(id)
);

CREATE INDEX IF NOT EXISTS index_Collections_createdBy ON Collections(createdBy);

-- ============================================
-- READING WORKFLOW TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ReadingWorkflow (
    id TEXT NOT NULL PRIMARY KEY,
    userId TEXT NOT NULL,
    paperId TEXT NOT NULL,
    status TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(id)
);

CREATE INDEX IF NOT EXISTS index_ReadingWorkflow_userId ON ReadingWorkflow(userId);

-- ============================================
-- TEAM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Team (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    researchField TEXT,
    privacy TEXT NOT NULL,
    iconUrl TEXT,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    createdBy TEXT NOT NULL,
    FOREIGN KEY(createdBy) REFERENCES Users(id)
);

CREATE INDEX IF NOT EXISTS index_Team_createdBy ON Team(createdBy);

-- ============================================
-- TAG TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Tag (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    color TEXT,
    createdDate INTEGER NOT NULL
);

-- ============================================
-- READING LIST TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ReadingList (
    id TEXT NOT NULL PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(id)
);

CREATE INDEX IF NOT EXISTS index_ReadingList_userId ON ReadingList(userId);

-- ============================================
-- PAPER RESEARCH TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS PaperResearch (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    authors TEXT,
    abstract TEXT,
    doi TEXT,
    url TEXT,
    publishedDate TEXT,
    journal TEXT,
    pdfUrl TEXT,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL
);

-- ============================================
-- NOTIFICATION TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Notification (
    id TEXT NOT NULL PRIMARY KEY,
    userId TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL,
    isRead INTEGER NOT NULL DEFAULT 0,
    createdDate INTEGER NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(id)
);

CREATE INDEX IF NOT EXISTS index_Notification_userId ON Notification(userId);
CREATE INDEX IF NOT EXISTS index_Notification_isRead ON Notification(isRead);

-- ============================================
-- INSTITUTION TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Institution (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT,
    website TEXT,
    createdDate INTEGER NOT NULL
);

-- ============================================
-- CITATION TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Citation (
    id TEXT NOT NULL PRIMARY KEY,
    paperId TEXT NOT NULL,
    citedPaperId TEXT,
    citationText TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    FOREIGN KEY(paperId) REFERENCES PaperResearch(id)
);

CREATE INDEX IF NOT EXISTS index_Citation_paperId ON Citation(paperId);

-- ============================================
-- HIGHLIGHT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Highlight (
    id TEXT NOT NULL PRIMARY KEY,
    userId TEXT NOT NULL,
    paperId TEXT NOT NULL,
    text TEXT NOT NULL,
    startPosition INTEGER NOT NULL,
    endPosition INTEGER NOT NULL,
    color TEXT,
    createdDate INTEGER NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(id),
    FOREIGN KEY(paperId) REFERENCES PaperResearch(id)
);

CREATE INDEX IF NOT EXISTS index_Highlight_userId ON Highlight(userId);
CREATE INDEX IF NOT EXISTS index_Highlight_paperId ON Highlight(paperId);

-- ============================================
-- NOTE TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS Note (
    id TEXT NOT NULL PRIMARY KEY,
    userId TEXT NOT NULL,
    paperId TEXT NOT NULL,
    highlightId TEXT,
    content TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    updatedDate INTEGER NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(id),
    FOREIGN KEY(paperId) REFERENCES PaperResearch(id),
    FOREIGN KEY(highlightId) REFERENCES Highlight(id)
);

CREATE INDEX IF NOT EXISTS index_Note_userId ON Note(userId);
CREATE INDEX IF NOT EXISTS index_Note_paperId ON Note(paperId);
CREATE INDEX IF NOT EXISTS index_Note_highlightId ON Note(highlightId);

-- ============================================
-- INSERT SAMPLE DATA (Optional)
-- ============================================

-- Insert default roles
INSERT OR IGNORE INTO Roles (id, name, description) VALUES 
    ('role_pi', 'Principal Investigator', 'Lab Head / Principal Investigator'),
    ('role_researcher', 'Researcher', 'Postdoc / PhD Researcher'),
    ('role_student', 'Student', 'Student / Intern');

-- Insert sample user (password should be hashed in real app)
-- INSERT OR IGNORE INTO Users (id, email, password, full_name, username, createdDate, updatedDate, roleId) VALUES
--     ('user_1', 'admin@labverse.com', 'hashed_password', 'Admin User', 'admin', 1640995200000, 1640995200000, 'role_pi');

