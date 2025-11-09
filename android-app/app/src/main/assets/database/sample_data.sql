-- LabVerse Android App - Sample Data Script
-- This script inserts sample data for testing purposes
-- Run this after init.sql

-- ============================================
-- SAMPLE ROLES (if not already inserted)
-- ============================================
INSERT OR IGNORE INTO Roles (id, name, description) VALUES 
    ('role_pi', 'Principal Investigator', 'Lab Head / Principal Investigator'),
    ('role_researcher', 'Researcher', 'Postdoc / PhD Researcher'),
    ('role_student', 'Student', 'Student / Intern');

-- ============================================
-- SAMPLE USERS
-- ============================================
-- Note: In production, passwords should be properly hashed
-- These are sample data for testing only
INSERT OR IGNORE INTO Users (id, email, password, full_name, username, createdDate, updatedDate, roleId, avatarUrl) VALUES
    ('user_1', 'admin@labverse.com', 'password123', 'Dr. Admin User', 'admin', 1640995200000, 1640995200000, 'role_pi', 'https://example.com/avatar1.png'),
    ('user_2', 'researcher@labverse.com', 'password123', 'Dr. Researcher', 'researcher', 1640995200000, 1640995200000, 'role_researcher', 'https://example.com/avatar2.png'),
    ('user_3', 'student@labverse.com', 'password123', 'Student User', 'student', 1640995200000, 1640995200000, 'role_student', 'https://example.com/avatar3.png');

-- ============================================
-- SAMPLE COLLECTIONS
-- ============================================
INSERT OR IGNORE INTO Collections (id, name, description, privacy, createdDate, updatedDate, createdBy) VALUES
    ('collection_1', 'AI Research Papers', 'Collection of artificial intelligence research papers', 'PRIVATE', 1640995200000, 1640995200000, 'user_1'),
    ('collection_2', 'Machine Learning', 'Papers on machine learning algorithms', 'PUBLIC', 1640995200000, 1640995200000, 'user_2'),
    ('collection_3', 'Deep Learning', 'Deep learning research papers', 'PRIVATE', 1640995200000, 1640995200000, 'user_1');

-- ============================================
-- SAMPLE TAGS
-- ============================================
INSERT OR IGNORE INTO Tag (id, name, description, color, createdDate) VALUES
    ('tag_1', 'AI', 'Artificial Intelligence', '#FF5722', 1640995200000),
    ('tag_2', 'ML', 'Machine Learning', '#2196F3', 1640995200000),
    ('tag_3', 'DL', 'Deep Learning', '#4CAF50', 1640995200000),
    ('tag_4', 'NLP', 'Natural Language Processing', '#9C27B0', 1640995200000),
    ('tag_5', 'CV', 'Computer Vision', '#FF9800', 1640995200000);

-- ============================================
-- SAMPLE PAPER RESEARCH
-- ============================================
INSERT OR IGNORE INTO PaperResearch (id, title, authors, abstract, doi, url, publishedDate, journal, pdfUrl, createdDate, updatedDate) VALUES
    ('paper_1', 'Attention Is All You Need', 'Vaswani et al.', 'We propose a new simple network architecture, the Transformer, based solely on attention mechanisms...', '10.48550/arXiv.1706.03762', 'https://arxiv.org/abs/1706.03762', '2017-06-12', 'NeurIPS', 'https://arxiv.org/pdf/1706.03762.pdf', 1640995200000, 1640995200000),
    ('paper_2', 'BERT: Pre-training of Deep Bidirectional Transformers', 'Devlin et al.', 'We introduce BERT, a new language representation model...', '10.18653/v1/N19-1423', 'https://arxiv.org/abs/1810.04805', '2018-10-11', 'NAACL', 'https://arxiv.org/pdf/1810.04805.pdf', 1640995200000, 1640995200000),
    ('paper_3', 'ImageNet Classification with Deep Convolutional Neural Networks', 'Krizhevsky et al.', 'We trained a large, deep convolutional neural network...', '10.1145/3065386', 'https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks', '2012-12-01', 'NeurIPS', 'https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf', 1640995200000, 1640995200000);

-- ============================================
-- SAMPLE READING LISTS
-- ============================================
INSERT OR IGNORE INTO ReadingList (id, userId, name, description, createdDate, updatedDate) VALUES
    ('readinglist_1', 'user_1', 'To Read', 'Papers I want to read', 1640995200000, 1640995200000),
    ('readinglist_2', 'user_1', 'Currently Reading', 'Papers I am currently reading', 1640995200000, 1640995200000),
    ('readinglist_3', 'user_2', 'Favorites', 'My favorite papers', 1640995200000, 1640995200000);

-- ============================================
-- SAMPLE READING WORKFLOWS
-- ============================================
INSERT OR IGNORE INTO ReadingWorkflow (id, userId, paperId, status, createdDate, updatedDate) VALUES
    ('workflow_1', 'user_1', 'paper_1', 'TO_READ', 1640995200000, 1640995200000),
    ('workflow_2', 'user_1', 'paper_2', 'READING', 1640995200000, 1640995200000),
    ('workflow_3', 'user_2', 'paper_3', 'READ', 1640995200000, 1640995200000);

-- ============================================
-- SAMPLE TEAMS
-- ============================================
INSERT OR IGNORE INTO Team (id, name, description, researchField, privacy, iconUrl, createdDate, updatedDate, createdBy) VALUES
    ('team_1', 'AI Research Lab', 'Research team focused on artificial intelligence', 'Artificial Intelligence', 'PRIVATE', 'https://example.com/team1.png', 1640995200000, 1640995200000, 'user_1'),
    ('team_2', 'ML Research Group', 'Machine learning research group', 'Machine Learning', 'PUBLIC', 'https://example.com/team2.png', 1640995200000, 1640995200000, 'user_2');

-- ============================================
-- SAMPLE NOTIFICATIONS
-- ============================================
INSERT OR IGNORE INTO Notification (id, userId, title, message, type, isRead, createdDate) VALUES
    ('notif_1', 'user_1', 'New Paper Added', 'A new paper has been added to your collection', 'INFO', 0, 1640995200000),
    ('notif_2', 'user_1', 'Team Invitation', 'You have been invited to join AI Research Lab', 'INVITATION', 0, 1640995200000),
    ('notif_3', 'user_2', 'Collection Shared', 'A collection has been shared with you', 'SHARE', 1, 1640995200000);

-- ============================================
-- SAMPLE INSTITUTIONS
-- ============================================
INSERT OR IGNORE INTO Institution (id, name, address, website, createdDate) VALUES
    ('inst_1', 'MIT', 'Cambridge, MA, USA', 'https://www.mit.edu', 1640995200000),
    ('inst_2', 'Stanford University', 'Stanford, CA, USA', 'https://www.stanford.edu', 1640995200000),
    ('inst_3', 'Carnegie Mellon University', 'Pittsburgh, PA, USA', 'https://www.cmu.edu', 1640995200000);

-- ============================================
-- SAMPLE CITATIONS
-- ============================================
INSERT OR IGNORE INTO Citation (id, paperId, citedPaperId, citationText, createdDate) VALUES
    ('citation_1', 'paper_1', 'paper_2', 'Vaswani et al. (2017) introduced the Transformer architecture...', 1640995200000),
    ('citation_2', 'paper_2', 'paper_1', 'Devlin et al. (2018) built upon the Transformer architecture...', 1640995200000);

-- ============================================
-- SAMPLE HIGHLIGHTS
-- ============================================
INSERT OR IGNORE INTO Highlight (id, userId, paperId, text, startPosition, endPosition, color, createdDate) VALUES
    ('highlight_1', 'user_1', 'paper_1', 'attention mechanisms', 100, 120, '#FFFF00', 1640995200000),
    ('highlight_2', 'user_1', 'paper_2', 'bidirectional transformers', 200, 225, '#FF00FF', 1640995200000);

-- ============================================
-- SAMPLE NOTES
-- ============================================
INSERT OR IGNORE INTO Note (id, userId, paperId, highlightId, content, createdDate, updatedDate) VALUES
    ('note_1', 'user_1', 'paper_1', 'highlight_1', 'This is an important concept about attention mechanisms', 1640995200000, 1640995200000),
    ('note_2', 'user_1', 'paper_2', NULL, 'Need to read more about BERT architecture', 1640995200000, 1640995200000);

