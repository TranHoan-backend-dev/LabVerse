# Use Case Specification - Team and Reading List Management

## Table of Contents
1. [Overview](#overview)
2. [Use Cases - Team Management](#use-cases---team-management)
3. [Use Cases - Reading List Management](#use-cases---reading-list-management)
4. [Screen Specifications](#screen-specifications)
5. [API Endpoints](#api-endpoints)
6. [Data Models](#data-models)
7. [Business Rules](#business-rules)

---

## Overview

### Purpose
This document provides detailed specifications for **Team Management** and **Reading List Management** features in the LabVerse Research Paper Management System.

### Scope
- **Team Management**: Research team creation, member management, role assignment, and team collaboration
- **Reading List Management**: Curated paper collections, member sharing, and list organization

### Technology Stack
- **Backend**: Spring Boot (Java/Kotlin), RESTful API
- **Frontend Web**: React + TypeScript, React Query, shadcn/ui
- **Frontend Mobile**: Android (Java), Room Database, Material Design
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka Server
- **Authentication**: JWT (JSON Web Tokens)

---

## Use Cases - Team Management

### UC-TM-01: Create New Team

**Description**: Authenticated users can create a new research team with basic information.

**Actor**: Authenticated User (PI, Researcher, Student)

**Preconditions**:
- User is logged in
- User has permission to create teams

**Main Flow**:
1. User navigates to Teams page
2. User clicks "Create Team" button
3. System displays create team dialog/form
4. User fills in the form:
   - Team Name (required)
   - Description (optional)
   - Research Field (optional)
   - Privacy: PUBLIC or PRIVATE (required)
5. User clicks "Create Team" button
6. System validates input data
7. System creates team in database
8. System automatically adds creator as OWNER
9. System displays success notification
10. System refreshes team list
11. Dialog closes

**Postconditions**:
- New team is created in database
- Creator becomes OWNER of the team
- Team appears in team list
- User can immediately access team details

**Alternative Flows**:
- **4a**: User does not fill Team Name
  - System displays validation error
  - User returns to step 4
- **6a**: Validation fails
  - System displays specific error message
  - User corrects input and retries
- **7a**: Team creation fails (server error)
  - System displays error notification
  - Form remains open for user to retry

**Business Rules**:
- Team name must be unique within the system
- Default privacy is PUBLIC if not specified
- Creator automatically receives OWNER role
- Team ID is generated as UUID and encoded for API responses

**Exception Handling**:
- Network errors: Display "Failed to create team. Please check your connection."
- Duplicate name: Display "A team with this name already exists."
- Unauthorized: Redirect to login page

---

### UC-TM-02: View Team List

**Description**: Users can browse and search teams with pagination and filtering options.

**Actor**: Authenticated User

**Preconditions**:
- User is logged in

**Main Flow**:
1. User navigates to Teams page
2. System fetches teams from API (default: page 0, size 10-12)
3. System displays team cards in grid layout
4. User can:
   - Scroll to see more teams
   - Use search bar to filter by team name
   - Use privacy filter dropdown (All/Public/Private)
   - Use research field filter (if implemented)
   - Click pagination buttons (Previous/Next)
5. System updates displayed teams based on filters
6. User clicks on a team card to view details

**Postconditions**:
- Team list is displayed with current filters applied
- User can navigate to team details

**Business Rules**:
- Default page size: 10-12 teams per page
- Search is case-insensitive
- PUBLIC teams are visible to all authenticated users
- PRIVATE teams are only visible to members
- Pagination shows current page and total pages

**UI Behavior**:
- Loading state: Show spinner while fetching
- Empty state: Show "No teams found" message with create button
- Error state: Show error message with retry option

---

### UC-TM-03: View Team Details

**Description**: Users can view detailed information about a specific team including members, papers, and settings.

**Actor**: Authenticated User

**Preconditions**:
- User is logged in
- Team exists
- User has permission to view team (member or PUBLIC team)

**Main Flow**:
1. User clicks on a team card from team list
2. System navigates to team detail page (`/teams/:id`)
3. System fetches team details from API
4. System displays:
   - Team name, description, research field
   - Privacy setting (PUBLIC/PRIVATE icon)
   - Member count and list of members with roles
   - Paper count (if implemented)
   - Created date, Updated date
5. If user is OWNER/ADMIN, system displays action buttons:
   - Add Member
   - Edit Team
   - Delete Team
   - Update Member Role (for each member)
6. User can interact with team information

**Postconditions**:
- Team details are displayed
- User can perform actions based on their role

**Business Rules**:
- Only OWNER and ADMIN see action buttons
- MEMBER role can only view information
- Member list shows: avatar, name, email, role, joined date
- Actions are context-sensitive based on user's role

**Note**: Full team detail page is partially implemented in web app (route exists but UI incomplete)

---

### UC-TM-04: Add Team Member

**Description**: Team OWNER or ADMIN can add new members to the team.

**Actor**: Team OWNER, Team ADMIN

**Preconditions**:
- User is logged in
- User is OWNER or ADMIN of the team
- Target user exists in the system

**Main Flow**:
1. User navigates to team detail page
2. User clicks "Add Member" button
3. System displays add member dialog/form
4. User enters email or username of target user
5. User selects role for new member (ADMIN or MEMBER)
6. User clicks "Add" button
7. System searches for user by email/username
8. System validates user exists and is not already a member
9. System adds user to team with selected role
10. System sends notification to new member (if implemented)
11. System displays success notification
12. System updates member list
13. Dialog closes

**Postconditions**:
- New member is added to team
- Member receives appropriate permissions based on role
- Member list is updated
- New member can access team

**Alternative Flows**:
- **4a**: User enters invalid email/username format
  - System displays validation error
  - User corrects input
- **7a**: User not found
  - System displays error "User not found"
  - User can retry with different email/username
- **8a**: User is already a member
  - System displays error "User is already a member of this team"
  - Dialog closes

**Business Rules**:
- OWNER cannot be removed or have role changed
- Each user can only be a member once per team
- Default role is MEMBER if not specified
- OWNER role cannot be assigned via this action (only through transfer)

**API Details**:
- Endpoint: `POST /v1/api/teams/{teamId}/members`
- Request body includes `userId` (email or encoded ID) and `role`

---

### UC-TM-05: Remove Team Member

**Description**: Team OWNER or ADMIN can remove members from the team.

**Actor**: Team OWNER, Team ADMIN

**Preconditions**:
- User is logged in
- User is OWNER or ADMIN of the team
- Target member exists in the team

**Main Flow**:
1. User navigates to team detail page
2. User views member list
3. User clicks "Remove" button next to target member
4. System displays confirmation dialog
5. User confirms removal
6. System removes member from team
7. System revokes member's access to team
8. System displays success notification
9. System updates member list

**Postconditions**:
- Member is removed from team
- Member loses access to team
- Member list is updated
- Member count decreases

**Business Rules**:
- OWNER cannot remove themselves
- OWNER cannot be removed by ADMIN
- When member is removed, their team-related data is handled appropriately
- Removal is immediate and cannot be undone

**Exception Handling**:
- If removing last ADMIN: Warn user that only OWNER will remain
- If removing member fails: Display error and keep member in list

---

### UC-TM-06: Update Member Role

**Description**: Team OWNER can change the role of team members (OWNER, ADMIN, MEMBER).

**Actor**: Team OWNER

**Preconditions**:
- User is logged in
- User is OWNER of the team
- Target member exists in the team

**Main Flow**:
1. User navigates to team detail page
2. User views member list
3. User clicks "Change Role" or role dropdown for target member
4. System displays role selection menu
5. User selects new role (OWNER, ADMIN, MEMBER)
6. System displays confirmation dialog (especially for OWNER transfer)
7. User confirms role change
8. System updates member's role in database
9. System updates member's permissions
10. System displays success notification
11. System updates member list with new role

**Postconditions**:
- Member's role is updated
- Member's permissions change according to new role
- Member list displays updated role
- If OWNER transferred, previous OWNER becomes ADMIN

**Business Rules**:
- OWNER can transfer ownership to another member
- When OWNER transfers, previous OWNER automatically becomes ADMIN
- Only one OWNER exists at a time
- OWNER role cannot be assigned to non-members
- Role changes are immediate

**Special Case - OWNER Transfer**:
- When transferring OWNER role, system must:
  1. Warn current OWNER about transfer
  2. Update new OWNER's role
  3. Change previous OWNER's role to ADMIN
  4. Log the transfer for audit purposes

---

### UC-TM-07: Update Team Information

**Description**: Team OWNER or ADMIN can update team details (name, description, research field, privacy).

**Actor**: Team OWNER, Team ADMIN

**Preconditions**:
- User is logged in
- User is OWNER or ADMIN of the team

**Main Flow**:
1. User navigates to team detail page
2. User clicks "Edit Team" button
3. System displays edit form with current team information
4. User modifies fields:
   - Team Name
   - Description
   - Research Field
   - Privacy (PUBLIC/PRIVATE)
5. User clicks "Save" button
6. System validates updated information
7. System checks team name uniqueness (if changed)
8. System updates team in database
9. System displays success notification
10. System refreshes team information display
11. Form closes

**Postconditions**:
- Team information is updated in database
- UI displays updated information
- Team appears in searches with new name (if changed)

**Alternative Flows**:
- **4a**: User cancels editing
  - Form closes without saving
  - No changes are made
- **6a**: Validation fails
  - System displays validation errors
  - User corrects input
- **7a**: New team name already exists
  - System displays error "Team name already exists"
  - User must choose different name

**Business Rules**:
- Team name must be unique (if changed)
- Privacy can be changed from PUBLIC to PRIVATE and vice versa
- Changing privacy affects team visibility immediately
- Updated timestamp is automatically set

---

### UC-TM-08: Delete Team

**Description**: Team OWNER can permanently delete a team from the system.

**Actor**: Team OWNER

**Preconditions**:
- User is logged in
- User is OWNER of the team

**Main Flow**:
1. User navigates to team detail page
2. User clicks "Delete Team" button
3. System displays confirmation dialog with warning:
   - "Are you sure you want to delete this team?"
   - "This action cannot be undone."
   - "All members will lose access to this team."
4. User confirms deletion
5. System deletes team from database
6. System removes all team-member relationships
7. System handles team-paper relationships (papers are not deleted)
8. System displays success notification
9. System navigates to team list page

**Postconditions**:
- Team is deleted from database
- All members lose access to team
- Papers in team are not deleted (only lose team association)
- Team no longer appears in any lists or searches

**Business Rules**:
- Only OWNER can delete team
- Deletion is permanent and cannot be undone
- Papers are not deleted, only lose team association
- All team data is removed (members, settings, etc.)
- Deletion should be logged for audit purposes

**Exception Handling**:
- If deletion fails: Display error and keep team intact
- If team has active workflows: Warn user about impact

---

## Use Cases - Reading List Management

### UC-RL-01: Create Reading List

**Description**: Users can create a new reading list to organize papers by theme or project.

**Actor**: Authenticated User

**Preconditions**:
- User is logged in

**Main Flow**:
1. User navigates to Reading Lists page
2. User clicks "Create List" button
3. System displays create reading list dialog
4. User fills in the form:
   - List Name (required)
   - Description (optional)
5. User clicks "Create List" button
6. System validates input (name is required)
7. System creates reading list in database
8. System automatically adds creator to userIds list
9. System displays success notification
10. System refreshes reading list display
11. Dialog closes

**Postconditions**:
- New reading list is created in database
- Creator is added to list as a member
- List appears in user's reading lists
- List is empty (no papers initially)

**Alternative Flows**:
- **4a**: User does not fill List Name
  - System displays validation error
  - User must provide name to continue
- **6a**: Creation fails
  - System displays error notification
  - Form remains open for retry

**Business Rules**:
- List name does not need to be unique (users can have multiple lists with same name)
- Creator is automatically added to userIds array
- List starts with empty paperIds array
- Created and updated timestamps are set automatically

---

### UC-RL-02: View Reading Lists

**Description**: Users can view all reading lists they are members of.

**Actor**: Authenticated User

**Preconditions**:
- User is logged in

**Main Flow**:
1. User navigates to Reading Lists page (`/reading-lists`)
2. System fetches reading lists for current user from API
3. System displays reading list cards in grid layout
4. Each card shows:
   - List name
   - Description (if available, truncated to 2 lines)
   - Number of papers
   - Number of members
   - Created date
5. User can click on a card to view details
6. User can delete list via dropdown menu

**Postconditions**:
- Reading lists are displayed
- User can access list details

**Business Rules**:
- Only lists where user is a member are displayed
- Lists are sorted by created date (newest first)
- Empty lists (no papers) are still displayed
- Paper count and member count are shown as 0 if empty

**UI States**:
- **Loading**: Show spinner
- **Empty**: Show "No reading lists yet" message with create button
- **Error**: Show error message with retry option

---

### UC-RL-03: View Reading List Details

**Description**: Users can view detailed information about a reading list including papers and members.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- Reading list exists
- User is a member of the reading list

**Main Flow**:
1. User clicks on a reading list card
2. System navigates to reading list detail page (`/reading-lists/:id`)
3. System fetches reading list details from API
4. System displays:
   - List name and description
   - Paper count
   - Member count
   - List of papers (with title, authors, journal)
   - List of members (with name, email, avatar)
   - Created date, Updated date
5. System displays action buttons:
   - Add Paper
   - Add Member
   - Delete List
6. User can interact with papers and members

**Postconditions**:
- Reading list details are displayed
- User can manage papers and members

**Business Rules**:
- Only members can view list details
- Papers are displayed with basic information
- Members are displayed with user information
- All members have equal permissions (no role hierarchy)

**Note**: Full detail page implementation may vary between web and Android platforms

---

### UC-RL-04: Add Papers to Reading List

**Description**: List members can add papers to a reading list.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- User is a member of the reading list
- Papers to add exist in the system

**Main Flow**:
1. User navigates to reading list detail page
2. User clicks "Add Paper" button
3. System displays paper selection interface:
   - Search bar to find papers
   - List of user's papers from library
   - Or paper picker dialog
4. User selects one or multiple papers
5. User clicks "Add" or "Add Selected" button
6. System validates papers exist
7. System checks papers are not already in list (optional)
8. System adds papers to reading list's paperIds array
9. System updates reading list in database
10. System displays success notification
11. System refreshes paper list display

**Postconditions**:
- Papers are added to reading list
- Paper list is updated
- Paper count increases
- Papers remain in user's library and other lists

**Alternative Flows**:
- **4a**: Paper already exists in list
  - System shows warning "Paper already in list"
  - System skips duplicate paper
  - Other papers are still added
- **6a**: Paper not found
  - System displays error for specific paper
  - Other papers are still added

**Business Rules**:
- A paper can exist in multiple reading lists
- Papers are not duplicated within the same list
- Adding papers does not remove them from user's library
- Paper count is automatically updated

**API Details**:
- Endpoint: `PUT /v1/api/reading-lists/{listId}/papers`
- Request body: `{ "paperIds": ["paper-id-1", "paper-id-2"] }`

---

### UC-RL-05: Remove Papers from Reading List

**Description**: List members can remove papers from a reading list.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- User is a member of the reading list
- Paper exists in the reading list

**Main Flow**:
1. User navigates to reading list detail page
2. User views paper list
3. User clicks "Remove" button on paper card or swipes to delete (mobile)
4. System displays confirmation (optional, for important papers)
5. User confirms removal
6. System removes paper from reading list's paperIds array
7. System updates reading list in database
8. System displays success notification
9. System refreshes paper list display

**Postconditions**:
- Paper is removed from reading list
- Paper count decreases
- Paper remains in system and other lists
- Paper list is updated

**Business Rules**:
- Removing paper from list does not delete paper from system
- Paper remains in user's library
- Paper remains in other reading lists (if added)
- Removal is immediate

**UI Interactions**:
- **Web**: Click delete icon → Confirm → Remove
- **Android**: Swipe to delete or long press → Remove option

---

### UC-RL-06: Add Members to Reading List

**Description**: List members can add other users to a reading list for sharing.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- User is a member of the reading list
- Target user exists in the system

**Main Flow**:
1. User navigates to reading list detail page
2. User clicks "Add Member" button
3. System displays add member dialog/form
4. User enters email or username of target user
5. User clicks "Add" button
6. System searches for user by email/username
7. System validates user exists
8. System checks user is not already a member
9. System adds user to reading list's userIds array
10. System updates reading list in database
11. System displays success notification
12. System refreshes member list display
13. Dialog closes

**Postconditions**:
- New user is added to reading list
- User gains access to reading list
- Member list is updated
- Member count increases

**Alternative Flows**:
- **4a**: Invalid email/username format
  - System displays validation error
  - User corrects input
- **6a**: User not found
  - System displays error "User not found"
  - User can retry with different email/username
- **8a**: User already a member
  - System displays warning "User is already a member"
  - Dialog closes without adding

**Business Rules**:
- Each user can only be a member once per list
- All members have equal permissions (no role hierarchy)
- Adding members allows collaborative list management
- Members can immediately access the list

**API Details**:
- Endpoint: `PUT /v1/api/reading-lists/{listId}/users`
- Request body: `{ "userIds": ["user-id-1", "user-id-2"] }`

---

### UC-RL-07: Remove Members from Reading List

**Description**: List members can remove users from a reading list.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- User is a member of the reading list
- Target user exists in the reading list

**Main Flow**:
1. User navigates to reading list detail page
2. User views member list
3. User clicks "Remove" button next to target user
4. System displays confirmation dialog
5. User confirms removal
6. System removes user from reading list's userIds array
7. System updates reading list in database
8. System displays success notification
9. System refreshes member list display

**Postconditions**:
- User is removed from reading list
- User loses access to reading list
- Member list is updated
- Member count decreases

**Business Rules**:
- Users can remove themselves from a list
- Removing a member does not delete papers
- Papers remain accessible to remaining members
- Removal is immediate

**Special Cases**:
- If removing last member: List becomes empty of members (may be handled by deleting list)
- Self-removal: User can remove themselves to stop receiving updates

---

### UC-RL-08: Delete Reading List

**Description**: List members can permanently delete a reading list.

**Actor**: Authenticated User (Member of reading list)

**Preconditions**:
- User is logged in
- User is a member of the reading list

**Main Flow**:
1. User navigates to Reading Lists page or detail page
2. User clicks "Delete" button (dropdown menu or action button)
3. System displays confirmation dialog:
   - "Are you sure you want to delete this reading list?"
   - "This action cannot be undone."
   - "All members will lose access to this list."
4. User confirms deletion
5. System deletes reading list from database
6. System removes all list-user relationships
7. System handles list-paper relationships (papers are not deleted)
8. System displays success notification
9. System navigates to reading lists page

**Postconditions**:
- Reading list is deleted from database
- All members lose access to list
- Papers are not deleted (only lose list association)
- List no longer appears in any lists

**Business Rules**:
- Any member can delete the list (no OWNER requirement)
- Deletion is permanent and cannot be undone
- Papers in list are not deleted from system
- Papers remain in user libraries and other lists
- All list data is removed

**Exception Handling**:
- If deletion fails: Display error and keep list intact
- Network errors: Display connection error message

---

## Screen Specifications

### 1. Teams Page (Web)

**Route**: `/teams`

**Screen Layout**:
```
┌─────────────────────────────────────────────────────────┐
│ Header Navigation Bar                                    │
│ [Logo] [Library] [Collections] [Reading Lists] [Teams]  │
│                                    [Profile] [Logout]    │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Research Teams                    [+ Create Team]       │
│  Join teams or create your own research group            │
│                                                           │
├─────────────────────────────────────────────────────────┤
│  [🔍 Search teams...]              [▼ All teams]        │
│                                                           │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Team Card 1  │  │ Team Card 2  │  │ Team Card 3  │  │
│  │              │  │              │  │              │  │
│  │ Team Name    │  │ Team Name    │  │ Team Name    │  │
│  │ Description  │  │ Description  │  │ Description  │  │
│  │ 🌐 PUBLIC    │  │ 🔒 PRIVATE  │  │ 🌐 PUBLIC    │  │
│  │ Field: AI    │  │ Field: Bio   │  │ Field: ML    │  │
│  │ Members: 5   │  │ Members: 3   │  │ Members: 8   │  │
│  │ Papers: 12   │  │ Papers: 7   │  │ Papers: 20   │  │
│  │        [⋮]   │  │        [⋮]   │  │        [⋮]   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Team Card 4  │  │ Team Card 5  │  │ Team Card 6  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                           │
├─────────────────────────────────────────────────────────┤
│              [← Previous]  Page 1 of 5  [Next →]        │
└─────────────────────────────────────────────────────────┘
```

**Components**:

1. **Header Navigation**
   - Logo/Brand name
   - Navigation links: Library, Collections, Reading Lists, Teams (active)
   - User profile dropdown
   - Logout button

2. **Page Header Section**
   - Title: "Research Teams" (h1, 3xl, bold)
   - Subtitle: "Join teams or create your own research group" (muted text)
   - Create Team Button: Primary button with Plus icon

3. **Search and Filter Bar**
   - Search Input: 
     - Placeholder: "Search teams..."
     - Search icon on left
     - Real-time search (debounced)
   - Privacy Filter Dropdown:
     - Options: "All teams", "Public", "Private"
     - Width: 180px on desktop, full width on mobile

4. **Team Cards Grid**
   - Layout: Responsive grid
     - Mobile: 1 column
     - Tablet: 2 columns
     - Desktop: 3 columns
   - Gap: 24px (1.5rem)
   - Card styling:
     - Shadow: `shadow-custom-sm`
     - Hover: `shadow-custom-md` (transition)
     - Cursor: pointer
     - Border radius: 8px
     - Padding: CardHeader + CardContent

5. **Team Card Components**:
   - **Card Header**:
     - Title: Team name (line-clamp-2, flex-1)
     - Dropdown menu button (MoreVertical icon) - top right
   - **Card Description** (if available):
     - Description text (line-clamp-2, muted)
   - **Card Content**:
     - Privacy indicator: Globe icon (PUBLIC) or Lock icon (PRIVATE) + text
     - Research Field: "Field: {field}" (if available)
     - Member count: "Members: {count}"
     - Paper count: "Papers: {count}"
   - **Dropdown Menu**:
     - Delete option (Trash2 icon, destructive color)

6. **Pagination**
   - Previous button (disabled on first page)
   - Page indicator: "Page {current} of {total}"
   - Next button (disabled on last page)
   - Centered layout

7. **Empty State** (when no teams found):
   - Icon: Users icon (12x12, muted color)
   - Title: "No teams found" (lg, semibold)
   - Message: Context-aware message
   - Create Team button (if no filters applied)

**Interactions**:
- **Click Team Card**: Navigate to `/teams/:id`
- **Click Create Team**: Open create dialog
- **Type in Search**: Filter teams by name (debounced 300ms)
- **Select Privacy Filter**: Filter by privacy type, reset to page 0
- **Click Delete**: Show confirmation dialog → Delete team → Refresh list
- **Click Pagination**: Navigate to different page

**States**:
- **Loading**: Spinner centered, full height
- **Empty**: Empty state card with message
- **Error**: Error message with retry button
- **Success**: Toast notification for actions

**Responsive Behavior**:
- Mobile (< 640px): 1 column, full-width buttons, stacked filters
- Tablet (640px - 1024px): 2 columns
- Desktop (> 1024px): 3 columns, side-by-side filters

---

### 2. Create Team Dialog (Web)

**Component**: Modal/Dialog Overlay

**Layout**:
```
┌─────────────────────────────────────────────────────┐
│  Create New Team                              [✕]    │
├─────────────────────────────────────────────────────┤
│                                                       │
│  Team Name *                                          │
│  ┌─────────────────────────────────────────────┐    │
│  │ e.g., Machine Learning Research Group      │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  Description                                          │
│  ┌─────────────────────────────────────────────┐    │
│  │ What is this team about?                    │    │
│  │                                               │    │
│  │                                               │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  Research Field                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │ e.g., Artificial Intelligence, Bioinformatics│    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  Privacy *                                            │
│  ┌─────────────────────────────────────────────┐    │
│  │ ▼ Select privacy                            │    │
│  └─────────────────────────────────────────────┘    │
│    • 🌐 Public - Anyone can find and join           │
│    • 🔒 Private - Invite only                        │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │         Create Team                           │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
└─────────────────────────────────────────────────────┘
```

**Form Fields**:

1. **Team Name** (required)
   - Type: Text input
   - Placeholder: "e.g., Machine Learning Research Group"
   - Validation: Required, min 3 characters
   - Error message: "Team name is required" / "Team name must be at least 3 characters"

2. **Description** (optional)
   - Type: Textarea
   - Rows: 4
   - Placeholder: "What is this team about?"
   - Max length: 500 characters (optional)
   - Character counter (optional)

3. **Research Field** (optional)
   - Type: Text input
   - Placeholder: "e.g., Artificial Intelligence, Bioinformatics"
   - Autocomplete suggestions (future enhancement)

4. **Privacy** (required)
   - Type: Select dropdown
   - Default: PUBLIC
   - Options:
     - PUBLIC: Globe icon + "Public - Anyone can find and join"
     - PRIVATE: Lock icon + "Private - Invite only"
   - Visual: Icons and descriptive text

**Actions**:
- **Create Team Button**:
  - Disabled when: Team name is empty OR form is submitting
  - Loading state: "Creating..." text
  - Success: Close dialog, show toast, refresh list
  - Error: Show error toast, keep dialog open

- **Cancel/Close**:
  - Click X or outside dialog: Close without saving
  - Reset form state

**Validation**:
- Real-time validation on blur
- Submit validation before API call
- Error messages displayed below fields

**Accessibility**:
- Labels associated with inputs
- Required fields marked with asterisk
- Keyboard navigation support
- Focus management

---

### 3. Team Detail Page (Web - Planned)

**Route**: `/teams/:id`

**Status**: Route exists but full UI not implemented

**Planned Layout**:
```
┌─────────────────────────────────────────────────────┐
│ Header Navigation                                    │
├─────────────────────────────────────────────────────┤
│  ← Back to Teams                                     │
│                                                       │
│  Machine Learning Research Group                     │
│  Research group focusing on ML algorithms            │
│  🌐 PUBLIC | Field: Artificial Intelligence          │
│                                                       │
│  [Add Member] [Edit Team] [Delete Team]             │
│                                                       │
├─────────────────────────────────────────────────────┤
│  Members (5)                                         │
│  ┌─────────────────────────────────────────────┐    │
│  │ [Avatar] John Doe                           │    │
│  │        john.doe@email.com                   │    │
│  │        OWNER                    [Change] [X]│    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │ [Avatar] Jane Smith                         │    │
│  │        jane.smith@email.com                 │    │
│  │        ADMIN                   [Change] [X]│    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
├─────────────────────────────────────────────────────┤
│  Papers (12)                                         │
│  [Paper cards grid - if implemented]                │
│                                                       │
└─────────────────────────────────────────────────────┘
```

**Components** (Planned):
- Team information header
- Member management section
- Paper list section (future)
- Action buttons based on user role

---

### 4. Teams Fragment (Android)

**Location**: Tab in FeedActivity (ViewPager2, position 2)

**Layout Structure**:
```
┌─────────────────────────────────────────┐
│ Common Header                            │
│ [Back] Title                    [Profile]│
├─────────────────────────────────────────┤
│ Common Search Bar                        │
│ [🔍 Search...]              [Filter]    │
├─────────────────────────────────────────┤
│ Active Teams                            │
│ Monitor progress across your teams       │
│ [+ Create New Team]                     │
├─────────────────────────────────────────┤
│ [Filter Chips] [Sort Button]            │
│ [AI] [Biomedical] [Environment] [Sort]   │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ Team Card 1                         │ │
│ │ [Icon] Machine Learning Group       │ │
│ │       Research group focusing...    │ │
│ │       Members: 5 | Papers: 12       │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ Team Card 2                         │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│ Recommended Teams                        │
│ ┌─────────────────────────────────────┐ │
│ │ Team Card 3                         │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**Components**:

1. **Common Header** (from layout_common_ui_home)
   - Back button (if applicable)
   - Title: "Teams" or dynamic title
   - Profile button

2. **Search Bar** (from layout_common_component_search)
   - Search input field
   - Filter button (opens filter options)

3. **Section Header**
   - Title: "Active Teams" (TextView, 18sp, bold)
   - Subtitle: "Monitor progress across your research teams" (14sp, gray)
   - Create Team Button:
     - MaterialButton
     - Text: "Create New Team"
     - Icon: ic_team drawable
     - Background: blue_500
     - Corner radius: 12dp

4. **Filter Section**
   - ChipGroup with filter chips:
     - AI
     - Biomedical
     - Environment
     - (Other research fields)
   - Sort Button:
     - ImageButton
     - Icon: ic_filter
     - Background: bg_icon_container
     - Tint: blue_500

5. **Team Cards** (MaterialCardView)
   - Layout: layout_team_card
   - Components:
     - Team icon/avatar (ImageView)
     - Team name (TextView, bold)
     - Description (TextView, truncated, 2 lines max)
     - Member count and Paper count (TextView)
   - Click action: Navigate to TeamDetailActivity

6. **Recommended Teams Section**
   - Title: "Recommended Teams" (TextView, 18sp, bold)
   - Team cards (same as Active Teams)

**Interactions**:
- **Click Create Team**: Navigate to TeamCreateActivity
- **Click Team Card**: Navigate to TeamDetailActivity with team data
- **Search**: Filter teams by name (real-time)
- **Filter Chips**: Filter by research field
- **Sort Button**: Open sort options dialog

**Data Loading**:
- Load from API on fragment creation
- Cache in Room database
- Show loading indicator while fetching
- Fallback to database if API fails

**Empty States**:
- No teams: Show message "Create your first team"
- No search results: Show "No teams found" with clear search option

---

### 5. Reading Lists Page (Web)

**Route**: `/reading-lists`

**Screen Layout**:
```
┌─────────────────────────────────────────────────────┐
│ Header Navigation                                    │
├─────────────────────────────────────────────────────┤
│                                                       │
│  Reading Lists                    [+ Create List]    │
│  Organize papers into themed collections             │
│                                                       │
├─────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │ List Card 1  │  │ List Card 2  │  │ List Card 3  ││
│  │              │  │              │  │              ││
│  │ List Name    │  │ List Name    │  │ List Name    ││
│  │ Description  │  │ Description  │  │ Description  ││
│  │ Papers: 5    │  │ Papers: 12   │  │ Papers: 0    ││
│  │ Members: 2   │  │ Members: 3    │  │ Members: 1   ││
│  │ Created: ... │  │ Created: ...  │  │ Created: ... ││
│  │        [⋮]   │  │        [⋮]   │  │        [⋮]   ││
│  └──────────────┘  └──────────────┘  └──────────────┘│
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │ List Card 4  │  │ List Card 5  │  │ List Card 6  ││
│  └──────────────┘  └──────────────┘  └──────────────┘│
│                                                       │
└─────────────────────────────────────────────────────┘
```

**Components**:

1. **Page Header Section**
   - Title: "Reading Lists" (h1, 3xl, bold)
   - Subtitle: "Organize papers into themed collections" (muted text)
   - Create List Button: Primary button with Plus icon

2. **Reading List Cards Grid**
   - Layout: Responsive grid (1-3 columns)
   - Gap: 24px
   - Card styling: Same as team cards

3. **Reading List Card Components**:
   - **Card Header**:
     - Title: List name (line-clamp-2)
     - Dropdown menu (MoreVertical icon)
   - **Card Description** (if available):
     - Description text (line-clamp-2)
   - **Card Content**:
     - Paper count: "Papers: {count}"
     - Member count: "Members: {count}"
     - Created date: "Created {formatted date}"
   - **Dropdown Menu**:
     - Delete option

4. **Empty State**:
   - Icon: BookMarked icon
   - Title: "No reading lists yet"
   - Message: "Create your first reading list..."
   - Create List button

**Interactions**:
- Click Create List: Open create dialog
- Click card: Navigate to detail (future)
- Delete: Show confirmation → Delete → Refresh

---

### 6. Create Reading List Dialog (Web)

**Component**: Modal/Dialog

**Layout**:
```
┌─────────────────────────────────────────────┐
│  Create Reading List                  [✕]   │
├─────────────────────────────────────────────┤
│                                               │
│  List Name *                                  │
│  ┌───────────────────────────────────────┐  │
│  │ e.g., Papers to Review This Week      │  │
│  └───────────────────────────────────────┘  │
│                                               │
│  Description                                  │
│  ┌───────────────────────────────────────┐  │
│  │ What's this list for?                   │  │
│  │                                         │  │
│  └───────────────────────────────────────┘  │
│                                               │
│  ┌───────────────────────────────────────┐  │
│  │         Create List                     │  │
│  └───────────────────────────────────────┘  │
│                                               │
└─────────────────────────────────────────────┘
```

**Form Fields**:
- **List Name** (required): Text input
- **Description** (optional): Textarea, 3 rows

**Validation & Actions**: Similar to Create Team dialog

---

### 7. Reading List Detail Activity (Android)

**Activity**: ReadingListDetailActivity

**Layout**:
```
┌─────────────────────────────────────────┐
│ Header                                   │
│ [←] Reading List Details        [Profile]│
├─────────────────────────────────────────┤
│ Papers to Review This Week              │
│ Papers: 5 | Members: 2                  │
│ [Add Paper] [Add Member]                │
├─────────────────────────────────────────┤
│ Papers:                                 │
│ ┌─────────────────────────────────────┐ │
│ │ Paper Card 1                        │ │
│ │ Title: Machine Learning Advances    │ │
│ │ Authors: John Doe, Jane Smith       │ │
│ │ Journal: AI Research Journal        │ │
│ │                            [Remove] │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ Paper Card 2                        │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ (Empty state if no papers)             │
└─────────────────────────────────────────┘
```

**Components**:
- Header with back button
- List info section
- Action buttons
- Papers RecyclerView
- Empty state

**Interactions**:
- Click Add Paper: Navigate to paper selection
- Click Add Member: Navigate to user selection
- Click Paper: Navigate to PaperDetailsActivity
- Swipe/Delete: Remove paper from list

---

## API Endpoints

### Team Management APIs

**Base URL**: `http://localhost:8080/account-service/v1/api/teams`

**Service**: AccountService (Java Spring Boot)

#### 1. Create Team
- **Method**: `POST`
- **Endpoint**: `/`
- **Authentication**: Required (Bearer Token)
- **Authorization**: `@PreAuthorize("isAuthenticated()")`
- **Request Body**:
```json
{
  "name": "Machine Learning Research Group",
  "description": "Research group focusing on ML algorithms",
  "researchField": "Artificial Intelligence",
  "privacy": "PUBLIC",
  "iconUrl": null
}
```
- **Response**: `200 OK`
```json
{
  "status": 200,
  "message": "Team created successfully",
  "data": {
    "id": "encoded-team-id",
    "name": "Machine Learning Research Group",
    "description": "Research group focusing on ML algorithms",
    "researchField": "Artificial Intelligence",
    "privacy": "PUBLIC",
    "memberCount": 1,
    "paperCount": 0,
    "createdAt": "2025-01-12T10:00:00",
    "updatedAt": "2025-01-12T10:00:00"
  }
}
```
- **Error Responses**:
  - `400 Bad Request`: Validation errors
  - `401 Unauthorized`: Missing or invalid token
  - `500 Internal Server Error`: Server error

#### 2. Get Teams (Paginated with Filters)
- **Method**: `GET`
- **Endpoint**: `/?search={query}&researchField={field}&privacy={PUBLIC|PRIVATE}&page={page}&size={size}`
- **Authentication**: Required
- **Query Parameters**:
  - `search` (optional, String): Search query for team name
  - `researchField` (optional, String): Filter by research field
  - `privacy` (optional, String): "PUBLIC" or "PRIVATE"
  - `page` (default: 0, int): Page number (0-indexed)
  - `size` (default: 10, int): Page size
- **Response**: `200 OK`
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": "encoded-id-1",
        "name": "Team 1",
        "description": "...",
        "researchField": "AI",
        "privacy": "PUBLIC",
        "memberCount": 5,
        "paperCount": 12,
        "createdAt": "2025-01-10T10:00:00",
        "updatedAt": "2025-01-12T10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 0,
    "size": 10
  }
}
```

#### 3. Get Team by ID
- **Method**: `GET`
- **Endpoint**: `/{teamId}`
- **Authentication**: Required
- **Path Parameter**: `teamId` (encoded UUID)
- **Response**: `200 OK` with TeamResponse
- **Error**: `404 Not Found` if team doesn't exist

#### 4. Update Team
- **Method**: `PUT`
- **Endpoint**: `/{teamId}`
- **Authentication**: Required
- **Authorization**: OWNER or ADMIN only
- **Request Body**: UpdateTeamRequest (all fields optional except userId)
- **Response**: `200 OK` with updated TeamResponse

#### 5. Delete Team
- **Method**: `DELETE`
- **Endpoint**: `/{teamId}`
- **Authentication**: Required
- **Authorization**: OWNER only
- **Response**: `200 OK`
```json
{
  "status": 200,
  "message": "Team deleted successfully",
  "data": null
}
```

#### 6. Add Team Member
- **Method**: `POST`
- **Endpoint**: `/{teamId}/members`
- **Authentication**: Required
- **Authorization**: OWNER or ADMIN
- **Request Body**:
```json
{
  "userId": "user-email-or-id",
  "role": "MEMBER"
}
```
- **Response**: `200 OK` with TeamMemberResponse
- **Error**: `400 Bad Request` if user already member or invalid role

#### 7. Remove Team Member
- **Method**: `DELETE`
- **Endpoint**: `/{teamId}/members/{memberId}`
- **Authentication**: Required
- **Authorization**: OWNER or ADMIN
- **Response**: `200 OK`

#### 8. Get Team Members
- **Method**: `GET`
- **Endpoint**: `/{teamId}/members`
- **Authentication**: Required
- **Response**: `200 OK` with List<TeamMemberResponse>

#### 9. Update Member Role
- **Method**: `PUT`
- **Endpoint**: `/{teamId}/members/{memberId}/role`
- **Authentication**: Required
- **Authorization**: OWNER only
- **Request Body**:
```json
{
  "role": "ADMIN"
}
```
- **Response**: `200 OK` with updated TeamMemberResponse

---

### Reading List Management APIs

**Base URL**: `http://localhost:8080/reading-service/v1/api/reading-lists`

**Service**: ReadingService (Java Spring Boot)

#### 1. Create Reading List
- **Method**: `POST`
- **Endpoint**: `/`
- **Authentication**: Required
- **Request Body**:
```json
{
  "name": "Papers to Review This Week",
  "description": "Important papers for weekly review",
  "userId": "encoded-user-id",
  "userIdsList": ["encoded-user-id"],
  "paperIdsList": []
}
```
- **Note**: IDs in userIdsList and paperIdsList should be encoded
- **Response**: `200 OK` with ReadingListResponse

#### 2. Get Reading Lists by User
- **Method**: `GET`
- **Endpoint**: `/user/{userId}`
- **Authentication**: Required
- **Path Parameter**: `userId` (encoded)
- **Response**: `200 OK` with List<ReadingListResponse>

#### 3. Get Reading List by ID
- **Method**: `GET`
- **Endpoint**: `/{listId}`
- **Authentication**: Required
- **Path Parameter**: `listId` (encoded)
- **Response**: `200 OK` with ReadingListResponse

#### 4. Update Papers in Reading List
- **Method**: `PUT`
- **Endpoint**: `/{listId}/papers`
- **Authentication**: Required
- **Request Body**:
```json
{
  "paperIds": ["encoded-paper-id-1", "encoded-paper-id-2"]
}
```
- **Response**: `200 OK` with updated ReadingListResponse

#### 5. Update Users in Reading List
- **Method**: `PUT`
- **Endpoint**: `/{listId}/users`
- **Authentication**: Required
- **Request Body**:
```json
{
  "userIds": ["encoded-user-id-1", "encoded-user-id-2"]
}
```
- **Response**: `200 OK` with updated ReadingListResponse

#### 6. Delete Reading List
- **Method**: `DELETE`
- **Endpoint**: `/{listId}`
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "status": 200,
  "message": "Reading list deleted successfully",
  "data": null
}
```

---

## Data Models

### Team Models

#### TeamResponse
```typescript
interface TeamResponse {
  id: string;                    // Encoded UUID
  name: string;                  // Required, unique
  description?: string;           // Optional
  researchField?: string;         // Optional
  privacy: 'PUBLIC' | 'PRIVATE'; // Required
  iconUrl?: string;              // Optional, future use
  memberCount?: number;           // Computed, number of members
  paperCount?: number;            // Computed, number of papers (future)
  createdAt?: string;             // ISO 8601 datetime
  updatedAt?: string;             // ISO 8601 datetime
}
```

#### TeamMemberResponse
```typescript
interface TeamMemberResponse {
  memberId: string;              // User ID (decoded)
  memberName: string;            // User's display name
  memberEmail: string;           // User's email
  memberAvatarUrl?: string;      // User's avatar URL
  role: 'OWNER' | 'ADMIN' | 'MEMBER'; // Required
  joinedAt?: string;             // ISO 8601 datetime
}
```

#### CreateTeamRequest
```typescript
interface CreateTeamRequest {
  name: string;                  // Required
  description?: string;           // Optional
  researchField?: string;         // Optional
  privacy: 'PUBLIC' | 'PRIVATE'; // Required
  iconUrl?: string;              // Optional
  userId: string;                // Creator's user ID (from auth)
}
```

#### UpdateTeamRequest
```typescript
interface UpdateTeamRequest {
  name?: string;                 // Optional
  description?: string;          // Optional
  researchField?: string;        // Optional
  privacy?: 'PUBLIC' | 'PRIVATE'; // Optional
  iconUrl?: string;              // Optional
  userId: string;                // Required for authorization
}
```

#### AddTeamMemberRequest
```typescript
interface AddTeamMemberRequest {
  userId: string;               // User ID or email
  role?: 'ADMIN' | 'MEMBER';    // Optional, default MEMBER
}
```

#### UpdateMemberRoleRequest
```typescript
interface UpdateMemberRoleRequest {
  role: 'OWNER' | 'ADMIN' | 'MEMBER'; // Required
}
```

### Reading List Models

#### ReadingListResponse
```typescript
interface ReadingListResponse {
  id: string;                    // Encoded UUID
  name: string;                  // Required
  description?: string;           // Optional
  paperIds?: string[];           // Array of encoded paper IDs
  userIds?: string[];           // Array of encoded user IDs (members)
  createdAt?: string;            // ISO 8601 datetime
  updatedAt?: string;            // ISO 8601 datetime
}
```

#### CreateReadingListRequest
```typescript
interface CreateReadingListRequest {
  name: string;                  // Required
  description?: string;           // Optional
  userId: string;                // Creator's user ID
}
```

#### UpdateReadingListPapersRequest
```typescript
interface UpdateReadingListPapersRequest {
  paperIds: string[];            // Array of paper IDs (will replace existing)
}
```

#### UpdateReadingListUsersRequest
```typescript
interface UpdateReadingListUsersRequest {
  userIds: string[];             // Array of user IDs (will replace existing)
}
```

---

## Business Rules

### Team Management Rules

1. **Ownership and Roles**:
   - Only one OWNER exists per team at any time
   - Role hierarchy: OWNER > ADMIN > MEMBER
   - OWNER has full control (create, update, delete, manage members, change roles)
   - ADMIN can add/remove members and update team info (cannot delete team or change OWNER)
   - MEMBER can only view and contribute papers

2. **Privacy Settings**:
   - PUBLIC teams: Visible to all authenticated users, anyone can find and join
   - PRIVATE teams: Only visible to members, requires invitation to join
   - Privacy can be changed by OWNER/ADMIN at any time

3. **Member Management**:
   - Each user can only be a member once per team
   - OWNER cannot remove themselves
   - OWNER cannot be removed by ADMIN
   - When OWNER transfers ownership, previous OWNER becomes ADMIN automatically

4. **Team Deletion**:
   - Only OWNER can delete team
   - Deletion is permanent and irreversible
   - Papers in team are NOT deleted, only lose team association
   - All members lose access immediately upon deletion

5. **Team Name**:
   - Must be unique within the system
   - Minimum 3 characters
   - Case-sensitive uniqueness check

### Reading List Management Rules

1. **Membership**:
   - All members have equal permissions (no role hierarchy)
   - Any member can add/remove papers and members
   - Any member can delete the list
   - Creator is automatically added to userIds array

2. **Paper Management**:
   - A paper can exist in multiple reading lists
   - Papers are not duplicated within the same list
   - Removing paper from list does NOT delete paper from system
   - Papers remain in user's library and other lists

3. **User Management**:
   - A user can be a member of multiple reading lists
   - Each user can only be a member once per list
   - Users can remove themselves from a list

4. **List Deletion**:
   - Any member can delete the list
   - Deletion is permanent and irreversible
   - Papers are NOT deleted, only lose list association
   - All members lose access immediately

5. **List Name**:
   - Does NOT need to be unique (users can have multiple lists with same name)
   - Minimum 1 character (required field)

### ID Encoding Rules

1. **Encoding Format**:
   - All IDs (UUIDs) are encoded using Base64 URL-safe encoding (without padding)
   - Encoding is done on backend before sending to frontend
   - Frontend receives encoded IDs and uses them directly in API calls
   - Backend decodes IDs before processing

2. **Encoding Purpose**:
   - Security: Prevents direct exposure of internal UUIDs
   - URL Safety: Encoded IDs are safe for use in URLs

---

## Implementation Details

### Web Implementation (React + TypeScript)

**Technologies**:
- React 18+ with TypeScript
- React Query (TanStack Query) for data fetching and caching
- React Router v6 for navigation
- shadcn/ui components (Card, Dialog, Button, Input, Select, etc.)
- Sonner for toast notifications
- React Helmet for SEO

**Key Files**:
- `web/src/pages/Teams.tsx`: Teams page component
- `web/src/pages/ReadingLists.tsx`: Reading Lists page component
- `web/src/services/team.service.ts`: Team API service
- `web/src/services/reading-list.service.ts`: Reading List API service

**State Management**:
- React Query for server state (teams, reading lists)
- React useState for local UI state (dialogs, forms)
- React Query mutations for create/update/delete operations

**API Integration**:
- Fetch API with custom service functions
- Bearer token authentication via tokenStorage
- Error handling with try-catch and error messages

### Android Implementation (Java)

**Technologies**:
- Android SDK with Java
- Room Database for local caching
- Retrofit for API calls
- Material Design Components
- ViewPager2 for tab navigation

**Key Files**:
- `android-app/app/src/main/java/.../presentation/feed/fragment/TeamFragment.java`
- `android-app/app/src/main/java/.../presentation/readinglist/ReadingListDetailActivity.java`
- `android-app/app/src/main/java/.../data/api/team/TeamApiHandler.java`
- `android-app/app/src/main/java/.../data/api/readinglist/ReadingListApiHandler.java`

**Architecture**:
- MVVM pattern with Repository pattern
- Room Database for offline storage
- API handlers for network calls
- Local database sync on API success

### Backend Implementation

**Team Service** (AccountService):
- `services/AccountService/src/main/java/.../controller/TeamController.java`
- `services/AccountService/src/main/java/.../service/TeamService.java`
- Spring Security `@PreAuthorize` for authorization
- JWT authentication via UserPrincipal

**Reading List Service** (ReadingService):
- `services/ReadingService/src/main/java/.../controller/ReadingListController.java`
- `services/ReadingService/src/main/java/.../service/ReadingListService.java`
- ID encoding/decoding via IdEncoder utility

**Database**:
- Teams: SQL Server database (AccountService)
- Reading Lists: SQL Server database (ReadingService)
- Relationships managed via foreign keys

---

## Testing Scenarios

### Team Management Test Cases

1. **Create Team**:
   - ✅ Create with all fields
   - ✅ Create with only required fields
   - ✅ Validation: Empty name
   - ✅ Validation: Duplicate name
   - ✅ Creator becomes OWNER

2. **View Teams**:
   - ✅ List all teams (paginated)
   - ✅ Search by name
   - ✅ Filter by privacy
   - ✅ Filter by research field
   - ✅ Pagination navigation

3. **Team Details**:
   - ✅ View team information
   - ✅ View member list
   - ✅ Role-based UI visibility

4. **Member Management**:
   - ✅ Add member (OWNER/ADMIN)
   - ✅ Add duplicate member (should fail)
   - ✅ Remove member (OWNER/ADMIN)
   - ✅ Update member role (OWNER only)
   - ✅ Transfer ownership

5. **Team Updates**:
   - ✅ Update team info (OWNER/ADMIN)
   - ✅ Change privacy setting
   - ✅ Update with duplicate name (should fail)

6. **Team Deletion**:
   - ✅ Delete team (OWNER only)
   - ✅ Verify papers not deleted
   - ✅ Verify members lose access

### Reading List Management Test Cases

1. **Create Reading List**:
   - ✅ Create with name and description
   - ✅ Create with name only
   - ✅ Validation: Empty name
   - ✅ Creator added to members

2. **View Reading Lists**:
   - ✅ List user's reading lists
   - ✅ Display empty state
   - ✅ Show paper and member counts

3. **Reading List Details**:
   - ✅ View list information
   - ✅ View paper list
   - ✅ View member list

4. **Paper Management**:
   - ✅ Add papers to list
   - ✅ Add duplicate paper (handled gracefully)
   - ✅ Remove paper from list
   - ✅ Verify paper not deleted from system

5. **Member Management**:
   - ✅ Add member to list
   - ✅ Add duplicate member (should fail)
   - ✅ Remove member from list
   - ✅ Self-removal

6. **Reading List Deletion**:
   - ✅ Delete list (any member)
   - ✅ Verify papers not deleted
   - ✅ Verify members lose access

---

## Future Enhancements

### Team Management Enhancements

1. **Team Invitations**:
   - Email-based invitations
   - Invitation links with expiration
   - Invitation acceptance workflow

2. **Team Activity Feed**:
   - Activity log (member joins, papers added, etc.)
   - Recent activity timeline
   - Notifications for team activities

3. **Team Statistics Dashboard**:
   - Member activity metrics
   - Paper contribution statistics
   - Reading progress visualization

4. **Advanced Features**:
   - Team templates for quick setup
   - Bulk member operations
   - Team export/import
   - Team archiving (soft delete)

### Reading List Management Enhancements

1. **Sharing Features**:
   - Public reading list links
   - Reading list templates
   - Export reading list to PDF/BibTeX

2. **Collaboration**:
   - Comments/discussions on lists
   - Reading progress tracking per list
   - List activity feed

3. **Organization**:
   - List tags/categories
   - List folders/collections
   - List search and advanced filters

4. **Integration**:
   - Import from external sources (Zotero, Mendeley)
   - Sync with reference managers
   - Citation export in multiple formats

---

## Appendix

### Error Codes

**Team Management**:
- `TEAM_NOT_FOUND`: Team does not exist
- `TEAM_NAME_EXISTS`: Team name already in use
- `UNAUTHORIZED_TEAM_ACCESS`: User lacks permission
- `INVALID_ROLE`: Invalid member role specified
- `OWNER_CANNOT_BE_REMOVED`: Attempt to remove OWNER

**Reading List Management**:
- `LIST_NOT_FOUND`: Reading list does not exist
- `USER_NOT_FOUND`: User does not exist
- `PAPER_NOT_FOUND`: Paper does not exist
- `ALREADY_MEMBER`: User is already a member
- `INVALID_ID_FORMAT`: Encoded ID format is invalid

### Performance Considerations

1. **Pagination**: All list endpoints support pagination to handle large datasets
2. **Caching**: Frontend uses React Query for intelligent caching
3. **Offline Support**: Android app caches data in Room database
4. **Lazy Loading**: Team members and papers loaded on demand

### Security Considerations

1. **Authentication**: All endpoints require JWT authentication
2. **Authorization**: Role-based access control (RBAC) for team operations
3. **ID Encoding**: UUIDs encoded to prevent enumeration attacks
4. **Input Validation**: Server-side validation for all inputs
5. **SQL Injection Prevention**: Parameterized queries via JPA

---

**Document Version**: 1.0  
**Last Updated**: January 12, 2025  
**Author**: LabVerse Development Team  
**Status**: Active Specification
