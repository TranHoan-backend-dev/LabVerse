# Progress Tracking API Documentation

## Overview
This document describes the backend service implementation for **Requirement 9: Reading Status and Progress Tracking Activity**. The service provides endpoints for tracking reading progress and generating dashboard statistics for PIs (Principal Investigators) to monitor team progress.

## API Endpoints

### 1. Collection Progress Statistics
**Endpoint:** `GET /reading-service/progress/collection/{collectionId}`

**Description:** Get aggregated progress statistics for a collection. This is the main dashboard endpoint for PIs to view team progress.

**Parameters:**
- `collectionId` (path, encoded): Collection ID

**Response:** `CollectionProgressStatisticsResponse`
```json
{
  "collectionId": "encoded_id",
  "totalPapers": 10,
  "totalUsers": 5,
  "unreadCount": 20,
  "readingCount": 15,
  "finishedCount": 15,
  "averageProgress": 45.5,
  "statusDistribution": [
    {
      "status": "unread",
      "count": 20,
      "percentage": 40.0
    },
    {
      "status": "reading",
      "count": 15,
      "percentage": 30.0
    },
    {
      "status": "finished",
      "count": 15,
      "percentage": 30.0
    }
  ],
  "teamMemberProgress": [
    {
      "userId": "encoded_user_id",
      "totalPapers": 8,
      "unreadCount": 3,
      "readingCount": 2,
      "finishedCount": 3,
      "averageProgress": 55.0,
      "totalProgress": 440
    }
  ]
}
```

### 2. Team Member Progress
**Endpoint:** `GET /reading-service/progress/collection/{collectionId}/member/{userId}`

**Description:** Get progress statistics for a specific team member in a collection.

**Parameters:**
- `collectionId` (path, encoded): Collection ID
- `userId` (path, encoded): User ID

**Response:** `TeamMemberProgressResponse`
```json
{
  "userId": "encoded_user_id",
  "totalPapers": 8,
  "unreadCount": 3,
  "readingCount": 2,
  "finishedCount": 3,
  "averageProgress": 55.0,
  "totalProgress": 440
}
```

### 3. Paper Progress in Collection
**Endpoint:** `GET /reading-service/progress/collection/{collectionId}/paper/{paperId}`

**Description:** Get progress statistics for a specific paper in a collection. Shows how all team members are progressing with this paper.

**Parameters:**
- `collectionId` (path, encoded): Collection ID
- `paperId` (path, encoded): Paper ID

**Response:** `PaperProgressResponse`
```json
{
  "paperId": "encoded_paper_id",
  "totalReaders": 5,
  "unreadCount": 2,
  "readingCount": 2,
  "finishedCount": 1,
  "averageProgress": 45.0,
  "userProgressList": [
    {
      "userId": "encoded_user_id",
      "status": "reading",
      "lastPage": 15,
      "progress": 45,
      "lastUpdated": "2025-01-15T10:30:00"
    }
  ]
}
```

### 4. Get Workflows by Collection
**Endpoint:** `GET /reading-service/workflows/collection/{collectionId}`

**Description:** Get all reading workflows for a collection, optionally filtered by status.

**Parameters:**
- `collectionId` (path, encoded): Collection ID
- `status` (query, optional): Filter by status (`unread`, `reading`, or `finished`)

**Response:** `List<ReadingWorkflowResponse>`
```json
[
  {
    "collectionId": "encoded_collection_id",
    "paperId": "encoded_paper_id",
    "usersid": "encoded_user_id",
    "status": "reading",
    "lastPage": 15,
    "progress": 45,
    "createdAt": "2025-01-10T08:00:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
]
```

## Implementation Details

### Repository Layer
Added new repository methods in `ReadingWorkflowRepository`:
- `findById_CollectionId(String collectionId)` - Get all workflows for a collection
- `findByCollectionIdAndStatus(String collectionId, String status)` - Get workflows by collection and status
- `findByCollectionIdAndPaperId(String collectionId, String paperId)` - Get workflows for a specific paper in collection
- `findByCollectionIdAndUsersid(String collectionId, String usersid)` - Get workflows for a user in a collection

### Service Layer
Created `ProgressTrackingService` with the following methods:
- `getCollectionProgressStatistics(String collectionId)` - Main dashboard method
- `getTeamMemberProgress(String collectionId, String userId)` - Team member statistics
- `getPaperProgress(String collectionId, String paperId)` - Paper progress statistics

### DTOs Created
1. `CollectionProgressStatisticsResponse` - Main dashboard response
2. `TeamMemberProgressResponse` - Team member progress
3. `PaperProgressResponse` - Paper progress in collection
4. `UserPaperProgressResponse` - Individual user progress for a paper
5. `StatusCountResponse` - Status distribution statistics

### Controller
Created `ProgressTrackingController` with three main endpoints for progress tracking and dashboard functionality.

## Usage Examples

### Get Collection Dashboard (PI View)
```bash
GET /reading-service/progress/collection/{encodedCollectionId}
```

### Get Team Member Progress
```bash
GET /reading-service/progress/collection/{encodedCollectionId}/member/{encodedUserId}
```

### Get Paper Progress
```bash
GET /reading-service/progress/collection/{encodedCollectionId}/paper/{encodedPaperId}
```

### Get All Workflows in Collection
```bash
GET /reading-service/workflows/collection/{encodedCollectionId}?status=reading
```

## Integration Notes

1. **ID Encoding**: All IDs in requests and responses are Base64 URL-safe encoded using the `IdEncoder` utility.

2. **Gateway Routing**: Endpoints are accessible through the API Gateway at `/reading-service/**` which routes to the Reading Service.

3. **Database**: The service uses the existing `reading_workflow` table with composite key (collectionId, paperId, usersid).

4. **Status Values**: Valid status values are:
   - `unread` - Paper has not been started
   - `reading` - Paper is currently being read
   - `finished` - Paper reading is complete (progress >= 100%)

## Features Implemented

✅ Track reading status (Unread, Reading, Finished)  
✅ Track page number progress  
✅ Track progress percentage (0-100%)  
✅ Aggregated statistics per collection  
✅ Team member progress tracking  
✅ Paper progress tracking across team  
✅ Status distribution statistics  
✅ Average progress calculations  
✅ Dashboard endpoint for PIs  

## Next Steps

For frontend integration:
1. Use `/progress/collection/{collectionId}` for the main dashboard
2. Use `/progress/collection/{collectionId}/member/{userId}` for individual team member views
3. Use `/progress/collection/{collectionId}/paper/{paperId}` for paper-specific progress
4. Use `/workflows/collection/{collectionId}` to get all workflows for display in lists

