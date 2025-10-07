## Project Topic : LabVerse - Research Paper Management System

### 1\. Introduction

Project Title: LabVerse - Research Paper Management System

Project Start Date: 18 September 2025

Context: This document outlines the requirements for an Android application designed for laboratories and research teams. The app will serve as a centralized platform for discovering, organizing, annotating, and collaborating on academic research papers. It aims to streamline the literature review process and foster a collaborative knowledge base, supporting research institutions from Hanoi to Ho Chi Minh City and across the globe.

### 2\. User Roles

**Principal Investigator (PI) / Lab Head**: Has administrative oversight. Can create and manage team collections, invite members, and view team-wide reading progress and discussions.

**Researcher (Postdoc / PhD)**: The primary user. Can manage a personal library, contribute to team collections, annotate papers, and engage in discussions.

**Student / Intern**: Can be granted read-only or contributor access to specific collections. Primarily uses the app to read assigned papers and add personal notes.

Detailed Functional Requirements



##### 1\. Secure User Registration \& Login Activity

**Purpose**: To provide secure, individual access to personal and team libraries.

**UI Components**:

EditText for Email/Google account

EditText for Password.

Button for "Login" and TextView link for "Register."

**Core Logic**: Authentication against a backend server via API calls. Supports standard email/password and potentially Google OAuth2 . An authentication token (JWT) is stored securely on-device using Encrypted SharedPreferences.



##### 2\. Personal Library Dashboard Activity

**Purpose**: To serve as the user's main hub, displaying their collection of research papers.

**UI Components**:

A RecyclerView to display a list of papers, with each item being a CardView showing the paper's title, authors, and journal.

A Floating Action Button (FAB) to trigger the "Import New Paper" function.

Tabs for "Recently Added," "Recently Read," and "Favorites."

**Core Logic**: On launch, the app syncs with the backend to fetch the user's library metadata. The list is populated from a local Room database to ensure fast load times and offline access.



##### 3\. Advanced Search and Filtering Activity

**Purpose**: To allow users to quickly find any paper in their personal or team libraries.

**UI Components**:

A SearchView in the main toolbar.

A filter Button that opens a BottomSheetDialog with options to filter by author, journal, keyword/tag, or publication year.

**Core Logic**: The search function performs a full-text search query on the local Room database for instant results. The backend API supports more complex filtered queries that can be triggered by the filter dialog.



##### 4\. Import New Paper Activity

**Purpose**: To add new research papers to a user's library from various sources.

**UI Components**:

A Dialog with options: "Upload PDF"

**Core Logic**:

Upload PDF: Uses an Intent to open the Android file picker. The PDF is uploaded to a cloud storage (like Firebase Storage or S3).



##### 5\. Integrated PDF Reader Activity

**Purpose**: To provide an in-app reading.

**UI Components**:

A view to render the PDF document, likely using a third-party library like AndroidPdfViewer.

**Core Logic**: Annotations are saved as overlay data linked to the document and page number. This data is stored locally and synced to the backend, allowing annotations to be viewed on other devices and shared with collaborators.



##### 6\. Create and Manage Shared Collections (Projects) Activity

**Purpose**: To allow teams to group papers related to a specific project or topic.

**UI Components**:

A dedicated "Teams" or "Collections" tab.

A RecyclerView listing all shared collections the user is a part of.

A Button for PIs to "Create New Collection" and "Invite Members" via email.

**Core Logic**: Collections are a core backend feature. The app fetches a list of collections the user has access to. Adding a paper to a collection links it to the collection's ID.



##### 7\. Set \& View Paper Status/Priority Activity

**Purpose**: To provide a simple, visual way for teams to manage their reading workflow and for PIs to assign priorities within a shared collection.

**UI Components**:

In a shared collection, each paper in the RecyclerView displays a status tag (e.g., "To Read," "Reading," "Finished").

Tapping the tag opens a BottomSheetDialog allowing authorized users to change the status or set a priority level ("High," "Medium," "Low").

**Core Logic**: This is a very achievable feature. Changing a status is just an API call to update a single field in the paper's record for that specific collection. The UI then fetches this data and displays the appropriate tag and color.



##### 8\. Reference and Citation Management Activity

**Purpose**: To automatically extract citation data and allow for easy export.

**UI Components**:

A "Citation" tab in the paper details view.

Displays the parsed reference information (authors, title, journal, year, DOI).

Buttons to "Copy Citation" in various formats (APA, MLA, BibTeX).

**Core Logic**: Upon importing a paper, the backend uses a library like Grobid to parse the PDF and extract its metadata and references. This structured data is saved in the database and displayed in the app.



##### 9\. Reading Status and Progress Tracking Activity

**Purpose**: To help users manage their reading workflow and allow PIs to see team progress.

**UI Components**:

In the library list, a visual indicator next to each paper (e.g., "Unread," "Reading," "Finished").

A progress bar for papers that are partially read.

**Core Logic**: The app locally saves the last-read page number for each document. This status is periodically synced with the backend. PIs can view an aggregated dashboard on a web interface showing the team's reading status for a given collection.



##### 10\. Personalized Feed and Alerts Activity

**Purpose**: To proactively suggest relevant new papers to the user.

**UI Components**:

A "Discover" tab with a RecyclerView feed of recommended papers.

Push notifications for new papers matching saved keywords.

**Core Logic**: The backend analyzes the user's library (keywords, authors, journals) and their saved search queries. It periodically scans new publications and creates a personalized recommendation feed, sending push notifications via Firebase Cloud Messaging (FCM) for high-priority alerts.



##### 11\. Offline Access Activity

**Purpose**: To ensure users can read and annotate their papers without an active internet connection.

**UI Components**: The app should function seamlessly offline. A subtle UI indicator might show offline status.

**Core Logic**: The app aggressively caches data using a Room database. All PDFs and their annotation data are stored locally. A background service built with WorkManager is responsible for syncing any changes made offline (new annotations, comments) back to the server once connectivity is restored.



##### 12\. User Profile Management Activity

**Purpose**: To allow users to manage their personal information and app settings.

**UI Components**:

A profile screen with options to change name, affiliation, and password.

Switch components to manage notification preferences.

**Core Logic**: Standard API calls to a user management endpoint on the backend to update profile information.



##### 13\. Export \& Import Annotations Activity

**Purpose**: To allow users to share their personal notes and highlights with a collaborator in a simple, file-based manner.

**UI Components**:

An "Export Annotations" button in the PDF reader's menu.

An "Import Annotations" button that opens the Android file picker (Intent).

A Dialog to show the status of the import/export process.

**Core Logic**: When a user exports, the app gathers all annotation data (highlight coordinates, note text, page numbers) for that PDF and saves it into a structured file (like JSON or XML). The user can then share this small file. When another user imports that file for the same PDF, the app parses the data and programmatically renders the annotations onto their screen.



##### 14\. Manual Reference (BibTeX) Import Activity

**Purpose**: To allow users to quickly add multiple papers to their library by importing a standard reference file from tools like Zotero, Mendeley, or Google Scholar.

**UI Components**:

A "Import from BibTeX" option in the "Import New Paper" dialog.

This action opens the Android file picker to select a .bib file.

A RecyclerView to show a preview of the papers found in the file, with CheckBoxes to select which ones to import.

**Core Logic**: The app reads the selected .bib file from local storage. The core task is to parse the plain text BibTeX format to extract the metadata for each entry (author, title, year, etc.). The app then makes simple API calls to create these new paper records in the user's library.



##### 15\. Create Reading Lists \& Journal Clubs Activity

**Purpose**: To empower users to manually curate and share themed collections of papers for specific projects or discussion groups.

**UI Components**:

A dedicated "Reading Lists" tab with a RecyclerView of user-created lists.

A FAB to "Create New List."

A simple interface to add papers from the main library to a list.

A share button to invite other users to view or collaborate on a list.

**Core Logic**: This feature relies on straightforward database relationships (a list has many papers; a user has many lists). All actions translate to simple Create, Read, Update, Delete (CRUD) operations through your API, which is a fundamental skill to practice. It provides high user value without needing any machine learning.

