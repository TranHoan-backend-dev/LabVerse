# Team API Documentation

## Overview
This document describes the REST APIs for managing teams in the LabVerse application. Teams allow users to collaborate on research papers and projects.

## Base URL
```
http://localhost:8081/api/teams
```

## Authentication
All endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Create Team
**POST** `/api/teams`

Creates a new team. The creator automatically becomes a PI (Principal Investigator) member.

**Request Body:**
```json
{
  "name": "AI Research Lab",
  "description": "Research team focused on artificial intelligence",
  "researchField": "Artificial Intelligence",
  "privacy": "PRIVATE",
  "iconUrl": "https://example.com/icon.png"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Team created successfully",
  "data": {
    "id": "encoded-team-id",
    "name": "AI Research Lab",
    "description": "Research team focused on artificial intelligence",
    "researchField": "Artificial Intelligence",
    "privacy": "PRIVATE",
    "iconUrl": "https://example.com/icon.png",
    "createdDate": "2025-01-15",
    "updatedDate": "2025-01-15",
    "createdById": "encoded-user-id",
    "createdByName": "John Doe",
    "createdByEmail": "john@example.com",
    "memberCount": 1,
    "isMember": true,
    "currentUserRole": {
      "id": "encoded-member-id",
      "userId": "encoded-user-id",
      "userName": "johndoe",
      "userFullName": "John Doe",
      "userEmail": "john@example.com",
      "userAvatarUrl": "https://example.com/avatar.png",
      "role": "PI",
      "joinedDate": "2025-01-15"
    }
  },
  "timestamp": "2025-01-15T10:30:00"
}
```

**Fields:**
- `name` (required): Team name (max 255 characters)
- `description` (optional): Team description (max 2000 characters)
- `researchField` (optional): Research field (max 255 characters)
- `privacy` (optional): `PUBLIC` or `PRIVATE` (default: `PRIVATE`)
- `iconUrl` (optional): URL to team icon/avatar

---

### 2. Get Team by ID
**GET** `/api/teams/{id}`

Retrieves team details by ID. User must be creator, member, or team must be public.

**Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "encoded-team-id",
    "name": "AI Research Lab",
    "description": "Research team focused on artificial intelligence",
    "researchField": "Artificial Intelligence",
    "privacy": "PRIVATE",
    "iconUrl": "https://example.com/icon.png",
    "createdDate": "2025-01-15",
    "updatedDate": "2025-01-15",
    "createdById": "encoded-user-id",
    "createdByName": "John Doe",
    "createdByEmail": "john@example.com",
    "memberCount": 5,
    "isMember": true,
    "currentUserRole": {
      "role": "PI",
      "joinedDate": "2025-01-15"
    }
  },
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 3. List Teams
**GET** `/api/teams`

Retrieves a paginated list of teams accessible to the current user (public teams or teams where user is creator/member).

**Query Parameters:**
- `search` (optional): Search by team name or description
- `researchField` (optional): Filter by research field
- `privacy` (optional): Filter by privacy type (`PUBLIC` or `PRIVATE`)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Example:**
```
GET /api/teams?search=AI&researchField=Artificial Intelligence&privacy=PUBLIC&page=0&size=10
```

**Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "encoded-team-id-1",
        "name": "AI Research Lab",
        "description": "...",
        "researchField": "Artificial Intelligence",
        "privacy": "PUBLIC",
        "memberCount": 5,
        "isMember": true
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 4. Update Team
**PUT** `/api/teams/{id}`

Updates team information. Only the team creator can update.

**Request Body:**
```json
{
  "name": "Updated Team Name",
  "description": "Updated description",
  "researchField": "Machine Learning",
  "privacy": "PUBLIC",
  "iconUrl": "https://example.com/new-icon.png"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Team updated successfully",
  "data": {
    "id": "encoded-team-id",
    "name": "Updated Team Name",
    ...
  },
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 5. Delete Team
**DELETE** `/api/teams/{id}`

Deletes a team. Only the team creator can delete. All team members are automatically removed.

**Response:**
```json
{
  "status": 200,
  "message": "Team deleted successfully",
  "data": null,
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 6. Add Team Member
**POST** `/api/teams/{id}/members`

Adds a user to the team. Only team creator or PI members can add members.

**Request Body:**
```json
{
  "userId": "encoded-user-id",
  "role": "RESEARCHER"
}
```

**Roles:**
- `PI`: Principal Investigator / Lab Head
- `RESEARCHER`: Postdoc / PhD
- `STUDENT`: Student / Intern

**Response:**
```json
{
  "status": 200,
  "message": "Member added successfully",
  "data": {
    "id": "encoded-member-id",
    "userId": "encoded-user-id",
    "userName": "janedoe",
    "userFullName": "Jane Doe",
    "userEmail": "jane@example.com",
    "userAvatarUrl": "https://example.com/avatar.png",
    "role": "RESEARCHER",
    "joinedDate": "2025-01-15"
  },
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 7. Remove Team Member
**DELETE** `/api/teams/{id}/members/{memberId}`

Removes a member from the team. Only team creator or PI members can remove members. Cannot remove the team creator.

**Response:**
```json
{
  "status": 200,
  "message": "Member removed successfully",
  "data": null,
  "timestamp": "2025-01-15T10:30:00"
}
```

---

### 8. Get Team Members
**GET** `/api/teams/{id}/members`

Retrieves all members of a team. User must have access to the team.

**Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": "encoded-member-id-1",
      "userId": "encoded-user-id-1",
      "userName": "johndoe",
      "userFullName": "John Doe",
      "userEmail": "john@example.com",
      "userAvatarUrl": "https://example.com/avatar.png",
      "role": "PI",
      "joinedDate": "2025-01-15"
    },
    {
      "id": "encoded-member-id-2",
      "userId": "encoded-user-id-2",
      "userName": "janedoe",
      "userFullName": "Jane Doe",
      "userEmail": "jane@example.com",
      "userAvatarUrl": "https://example.com/avatar2.png",
      "role": "RESEARCHER",
      "joinedDate": "2025-01-16"
    }
  ],
  "timestamp": "2025-01-15T10:30:00"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "message": "Only team creator or PI members can add members",
  "data": null,
  "timestamp": "2025-01-15T10:30:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Team not found with id: abc123",
  "data": null,
  "timestamp": "2025-01-15T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2025-01-15T10:30:00"
}
```

---

## Integration with Android Activities

### Create Team Activity (`activity_create_team.xml`)
- Use **POST** `/api/teams` to create team
- Map form fields:
  - `etTeamName` → `name`
  - `etDescription` → `description`
  - `spinnerResearchField` → `researchField`
  - `rgPrivacy` (rbPublic/rbPrivate) → `privacy`

### Team List Activity (`activity_team_list.xml`)
- Use **GET** `/api/teams` to list teams
- Support filters:
  - Search bar → `search` parameter
  - Filter chips → `researchField` parameter
  - Privacy filter → `privacy` parameter

### Team Detail Activity (`activity_team_detail.xml`)
- Use **GET** `/api/teams/{id}` to get team details
- Use **GET** `/api/teams/{id}/members` to get team members
- Use **POST** `/api/teams/{id}/members` when clicking "+ Add" button
- Use **DELETE** `/api/teams/{id}/members/{memberId}` to remove members

---

## Database Schema

### Teams Table
- `id` (VARCHAR(36), PK)
- `name` (VARCHAR(255), NOT NULL)
- `description` (TEXT)
- `research_field` (VARCHAR(255))
- `privacy` (VARCHAR(20), NOT NULL)
- `icon_url` (VARCHAR)
- `created_date` (DATE, NOT NULL)
- `updated_date` (DATE, NOT NULL)
- `created_by` (VARCHAR(36), FK to Users.id)

### TeamMembers Table
- `id` (VARCHAR(36), PK)
- `team_id` (VARCHAR(36), FK to Teams.id)
- `user_id` (VARCHAR(36), FK to Users.id)
- `role` (VARCHAR(50), NOT NULL)
- `joined_date` (DATE, NOT NULL)

---

## Notes

1. **ID Encoding**: All IDs in responses are encoded using `IdEncoder`. Decode them before using in subsequent requests.

2. **Privacy**: 
   - `PUBLIC`: Anyone can find and view the team
   - `PRIVATE`: Only members and creator can access

3. **Permissions**:
   - Team Creator: Can update, delete team, and manage members
   - PI Members: Can add/remove members
   - Researchers/Students: Can view team and members

4. **Automatic Member Addition**: When creating a team, the creator is automatically added as a PI member.

5. **Team Creator Protection**: The team creator cannot be removed from the team.

