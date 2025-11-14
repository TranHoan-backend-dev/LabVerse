# Admin Dashboard - Đề Xuất Implementation

## Tổng Quan

Tài liệu này đề xuất cách implement Admin Dashboard cho LabVerse dựa trên codebase hiện tại, bao gồm:
- Thêm role ADMIN
- Các tính năng admin phù hợp
- APIs cần thiết
- Frontend dashboard structure

---

## 1. Phân Tích Codebase Hiện Tại

### 1.1. Entities Chính

**AccountService:**
- ✅ `User` - có `Role`, `isActive`
- ✅ `Role` - có thể mở rộng thêm ADMIN
- ✅ `Team` - quản lý teams
- ✅ `TeamMember` - quản lý members

**PaperService:**
- ✅ `Paper` - research papers
- ✅ `Tag` - tags cho papers

**GroupService:**
- ✅ `Collection` - shared collections
- ✅ `CollectionUser` - collection members

**ReadingService:**
- ✅ `ReadingList` - reading lists

**NotificationService:**
- ✅ `Notification` - user notifications

### 1.2. Authentication & Authorization Hiện Tại

- ✅ JWT Authentication
- ✅ `@PreAuthorize("isAuthenticated()")` 
- ✅ `UserPrincipal` chứa user info
- ⚠️ Chưa có role-based authorization

---

## 2. Đề Xuất Tính Năng Admin Dashboard

### 2.1. User Management

**Mục đích**: Quản lý tất cả users trong hệ thống

**Tính năng:**
1. **View All Users** (Paginated)
   - Danh sách users với filters (role, status, search)
   - Hiển thị: email, name, role, status, created date
   - Sort by: name, email, created date

2. **User Details**
   - View chi tiết user
   - Xem papers của user
   - Xem teams/collections user tham gia
   - Xem activity history

3. **Activate/Deactivate User**
   - Ban/unban users
   - Set `isActive = false` để disable account

4. **Change User Role**
   - Thay đổi role của user (PI, Researcher, Intern)
   - Validation: Không thể change role của chính mình

5. **Delete User** (Soft delete)
   - Set `isActive = false`
   - Hoặc hard delete (cẩn thận với foreign keys)

### 2.2. Paper Management

**Mục đích**: Quản lý và kiểm duyệt papers

**Tính năng:**
1. **View All Papers** (Paginated)
   - Danh sách tất cả papers
   - Filters: author, journal, year, created date
   - Search by title, DOI

2. **Paper Details**
   - View chi tiết paper
   - Xem metadata, tags, keywords
   - Xem user đã upload

3. **Delete Paper**
   - Xóa papers không phù hợp
   - Xóa papers vi phạm bản quyền
   - Warning: Có thể ảnh hưởng đến collections/reading lists

4. **Paper Statistics**
   - Tổng số papers
   - Papers theo tháng/năm
   - Top authors, journals
   - Papers per user

### 2.3. Team Management

**Mục đích**: Quản lý teams và members

**Tính năng:**
1. **View All Teams** (Paginated)
   - Danh sách tất cả teams
   - Filters: privacy, research field
   - Hiển thị: name, privacy, member count, paper count

2. **Team Details**
   - View chi tiết team
   - Xem members và roles
   - Xem papers trong team

3. **Delete Team**
   - Xóa teams không phù hợp
   - Warning: Members sẽ mất access

### 2.4. Collection Management

**Tính năng tương tự Team Management:**
- View all collections
- View collection details
- Delete collections

### 2.5. Reading List Management

**Tính năng:**
- View all reading lists
- View list details
- Delete lists

### 2.6. Statistics Dashboard

**Mục đích**: Tổng quan về hệ thống

**Metrics:**
1. **User Statistics**
   - Total users
   - Active users (isActive = true)
   - Users by role (PI, Researcher, Intern)
   - New users this month/year
   - User growth chart

2. **Paper Statistics**
   - Total papers
   - Papers uploaded this month/year
   - Papers by year (publication year)
   - Top journals
   - Top authors

3. **Team Statistics**
   - Total teams
   - Public vs Private teams
   - Average members per team
   - Teams by research field

4. **Collection Statistics**
   - Total collections
   - Average papers per collection
   - Collections by access level

5. **Reading List Statistics**
   - Total reading lists
   - Average papers per list
   - Average members per list

6. **Activity Statistics**
   - Papers uploaded per day/week/month
   - Teams created per month
   - User registrations per month

### 2.7. System Settings (Optional)

**Tính năng:**
- Configure system-wide settings
- Manage tags (create, edit, delete)
- Manage institutions
- Email templates
- Feature flags

---

## 3. Implementation Plan

### 3.1. Backend - Thêm Role ADMIN

#### Step 1: Update Role Model

**File**: `services/AccountService/src/main/java/com/se1853_jv/model/Role.java`

Không cần thay đổi, chỉ cần thêm role "ADMIN" vào database.

#### Step 2: Create Admin Role in Database

**File**: `services/AccountService/src/main/resources/database/init.sql` hoặc migration script

```sql
-- Insert ADMIN role if not exists
IF NOT EXISTS (SELECT 1 FROM Role WHERE name = 'ADMIN')
BEGIN
    INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
END
```

#### Step 3: Create Admin User (Manual hoặc Script)

```sql
-- Create admin user (password: admin123 - should be hashed)
-- Note: Use BCrypt hash in production
INSERT INTO Users (id, email, username, full_name, password, created_date, updated_date, Roleid, is_active)
SELECT 
    NEWID(),
    'admin@labverse.com',
    'admin',
    'System Administrator',
    '$2a$10$...', -- BCrypt hash of password
    GETDATE(),
    GETDATE(),
    (SELECT id FROM Role WHERE name = 'ADMIN'),
    1;
```

#### Step 4: Create Admin Controller

**File**: `services/AccountService/src/main/java/com/se1853_jv/controller/AdminController.java`

```java
package com.se1853_jv.controller;

import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.service.AdminService;
import com.se1853_jv.util.IdEncoder;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ========== USER MANAGEMENT ==========
    
    @GetMapping("/users")
    public ResponseEntity<WrapperApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getAllUsers(page, size, search, role, isActive)
        ));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<WrapperApiResponse> getUserDetails(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getUserDetails(decodedId)
        ));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<WrapperApiResponse> activateUser(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        adminService.activateUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User activated successfully"));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<WrapperApiResponse> deactivateUser(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        adminService.deactivateUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User deactivated successfully"));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<WrapperApiResponse> changeUserRole(
            @PathVariable String id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        String decodedId = IdEncoder.decode(id);
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.changeUserRole(decodedId, request.getRoleId())
        ));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<WrapperApiResponse> deleteUser(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        adminService.deleteUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User deleted successfully"));
    }

    // ========== PAPER MANAGEMENT ==========
    
    @GetMapping("/papers")
    public ResponseEntity<WrapperApiResponse> getAllPapers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String journal) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getAllPapers(page, size, search, author, journal)
        ));
    }

    @GetMapping("/papers/{id}")
    public ResponseEntity<WrapperApiResponse> getPaperDetails(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getPaperDetails(decodedId)
        ));
    }

    @DeleteMapping("/papers/{id}")
    public ResponseEntity<WrapperApiResponse> deletePaper(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        adminService.deletePaper(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("Paper deleted successfully"));
    }

    // ========== TEAM MANAGEMENT ==========
    
    @GetMapping("/teams")
    public ResponseEntity<WrapperApiResponse> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String privacy) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getAllTeams(page, size, search, privacy)
        ));
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<WrapperApiResponse> deleteTeam(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        adminService.deleteTeam(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("Team deleted successfully"));
    }

    // ========== STATISTICS ==========
    
    @GetMapping("/statistics/overview")
    public ResponseEntity<WrapperApiResponse> getOverviewStatistics() {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getOverviewStatistics()
        ));
    }

    @GetMapping("/statistics/users")
    public ResponseEntity<WrapperApiResponse> getUserStatistics(
            @RequestParam(required = false) String period) { // daily, weekly, monthly
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getUserStatistics(period)
        ));
    }

    @GetMapping("/statistics/papers")
    public ResponseEntity<WrapperApiResponse> getPaperStatistics(
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getPaperStatistics(period)
        ));
    }
}
```

#### Step 5: Create AdminService

**File**: `services/AccountService/src/main/java/com/se1853_jv/service/AdminService.java`

```java
package com.se1853_jv.service;

import com.se1853_jv.dto.response.*;
import org.springframework.data.domain.Page;

public interface AdminService {
    // User Management
    Page<UserResponse> getAllUsers(int page, int size, String search, String role, Boolean isActive);
    AdminUserDetailsResponse getUserDetails(String userId);
    void activateUser(String userId);
    void deactivateUser(String userId);
    UserResponse changeUserRole(String userId, String roleId);
    void deleteUser(String userId);
    
    // Paper Management (cần call PaperService)
    Page<PaperResponse> getAllPapers(int page, int size, String search, String author, String journal);
    PaperResponse getPaperDetails(String paperId);
    void deletePaper(String paperId);
    
    // Team Management
    Page<TeamResponse> getAllTeams(int page, int size, String search, String privacy);
    void deleteTeam(String teamId);
    
    // Statistics
    OverviewStatisticsResponse getOverviewStatistics();
    UserStatisticsResponse getUserStatistics(String period);
    PaperStatisticsResponse getPaperStatistics(String period);
}
```

#### Step 6: Update SecurityConfig

**File**: `services/AccountService/src/main/java/com/se1853_jv/config/SecurityConfig.java`

```java
// Thêm method để check role ADMIN
@Bean
public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy());
    return handler;
}

@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("ADMIN > PI > RESEARCHER > INTERN");
    return hierarchy;
}
```

### 3.2. Frontend - Admin Dashboard

#### Step 1: Create Admin Route Protection

**File**: `web/src/components/AdminRoute.tsx`

```typescript
import { Navigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

interface AdminRouteProps {
    children: React.ReactNode;
}

const AdminRoute = ({ children }: AdminRouteProps) => {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (!user) {
        return <Navigate to="/auth" replace />;
    }

    // Check if user has ADMIN role
    if (user.role?.name !== "ADMIN") {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};

export default AdminRoute;
```

#### Step 2: Create Admin Dashboard Page

**File**: `web/src/pages/admin/Dashboard.tsx`

```typescript
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Users, FileText, UsersRound, BarChart3 } from "lucide-react";
import Header from "@/pages/Header";
import AdminRoute from "@/components/AdminRoute";
import UserManagement from "./components/UserManagement";
import PaperManagement from "./components/PaperManagement";
import TeamManagement from "./components/TeamManagement";
import StatisticsDashboard from "./components/StatisticsDashboard";
import { getAdminStatistics } from "@/services/admin.service";

const AdminDashboard = () => {
    const { data: stats } = useQuery({
        queryKey: ["admin-statistics"],
        queryFn: getAdminStatistics,
    });

    return (
        <AdminRoute>
            <div className="min-h-screen bg-background">
                <Header />
                <main className="container mx-auto px-4 py-8">
                    <div className="mb-8">
                        <h1 className="text-3xl font-bold mb-2">Admin Dashboard</h1>
                        <p className="text-muted-foreground">Manage users, papers, teams, and view system statistics</p>
                    </div>

                    {/* Statistics Cards */}
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Total Users</CardTitle>
                                <Users className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">{stats?.totalUsers || 0}</div>
                                <p className="text-xs text-muted-foreground">
                                    {stats?.activeUsers || 0} active
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Total Papers</CardTitle>
                                <FileText className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">{stats?.totalPapers || 0}</div>
                                <p className="text-xs text-muted-foreground">
                                    {stats?.papersThisMonth || 0} this month
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Total Teams</CardTitle>
                                <UsersRound className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">{stats?.totalTeams || 0}</div>
                                <p className="text-xs text-muted-foreground">
                                    {stats?.publicTeams || 0} public
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Statistics</CardTitle>
                                <BarChart3 className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">View</div>
                                <p className="text-xs text-muted-foreground">
                                    Detailed analytics
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    {/* Management Tabs */}
                    <Tabs defaultValue="users" className="space-y-4">
                        <TabsList>
                            <TabsTrigger value="users">Users</TabsTrigger>
                            <TabsTrigger value="papers">Papers</TabsTrigger>
                            <TabsTrigger value="teams">Teams</TabsTrigger>
                            <TabsTrigger value="statistics">Statistics</TabsTrigger>
                        </TabsList>

                        <TabsContent value="users">
                            <UserManagement />
                        </TabsContent>

                        <TabsContent value="papers">
                            <PaperManagement />
                        </TabsContent>

                        <TabsContent value="teams">
                            <TeamManagement />
                        </TabsContent>

                        <TabsContent value="statistics">
                            <StatisticsDashboard />
                        </TabsContent>
                    </Tabs>
                </main>
            </div>
        </AdminRoute>
    );
};

export default AdminDashboard;
```

#### Step 3: Create Admin Service

**File**: `web/src/services/admin.service.ts`

```typescript
import { BASE_API_URL, METHOD } from "@/type/constant";

const ADMIN_SERVICE_PREDICATE = "/account-service/v1/api/admin";

export interface AdminUser {
    id: string;
    email: string;
    username: string;
    fullName: string;
    role: {
        id: string;
        name: string;
    };
    isActive: boolean;
    createdDate: string;
}

export interface AdminStatistics {
    totalUsers: number;
    activeUsers: number;
    totalPapers: number;
    papersThisMonth: number;
    totalTeams: number;
    publicTeams: number;
    totalCollections: number;
    totalReadingLists: number;
}

export const getAdminUsers = async (
    page: number = 0,
    size: number = 20,
    search?: string,
    role?: string,
    isActive?: boolean
) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });
    if (search) params.append("search", search);
    if (role) params.append("role", role);
    if (isActive !== undefined) params.append("isActive", isActive.toString());

    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/users?${params}`,
        {
            method: METHOD.GET,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const activateUser = async (userId: string) => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/users/${userId}/activate`,
        {
            method: METHOD.PATCH,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const deactivateUser = async (userId: string) => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/users/${userId}/deactivate`,
        {
            method: METHOD.PATCH,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const changeUserRole = async (userId: string, roleId: string) => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/users/${userId}/role`,
        {
            method: METHOD.PATCH,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
            body: JSON.stringify({ roleId }),
        }
    );
    return response.json();
};

export const deleteUser = async (userId: string) => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/users/${userId}`,
        {
            method: METHOD.DELETE,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const getAdminPapers = async (
    page: number = 0,
    size: number = 20,
    search?: string,
    author?: string,
    journal?: string
) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });
    if (search) params.append("search", search);
    if (author) params.append("author", author);
    if (journal) params.append("journal", journal);

    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/papers?${params}`,
        {
            method: METHOD.GET,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const deletePaper = async (paperId: string) => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/papers/${paperId}`,
        {
            method: METHOD.DELETE,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    return response.json();
};

export const getAdminStatistics = async (): Promise<AdminStatistics> => {
    const response = await fetch(
        `${BASE_API_URL}${ADMIN_SERVICE_PREDICATE}/statistics/overview`,
        {
            method: METHOD.GET,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        }
    );
    const data = await response.json();
    return data.data;
};
```

#### Step 4: Add Admin Link to Navigation

**File**: `web/src/components/Navigation.tsx` hoặc `AppNavigation.tsx`

```typescript
// Thêm vào navigation menu
{user?.role?.name === "ADMIN" && (
    <Link to="/admin" className="...">
        Admin Dashboard
    </Link>
)}
```

#### Step 5: Add Route

**File**: `web/src/App.tsx` hoặc router file

```typescript
import AdminDashboard from "./pages/admin/Dashboard";

// Trong routes
<Route path="/admin" element={<AdminDashboard />} />
```

---

## 4. API Endpoints Summary

### User Management
- `GET /v1/api/admin/users` - Get all users (paginated, filtered)
- `GET /v1/api/admin/users/{id}` - Get user details
- `PATCH /v1/api/admin/users/{id}/activate` - Activate user
- `PATCH /v1/api/admin/users/{id}/deactivate` - Deactivate user
- `PATCH /v1/api/admin/users/{id}/role` - Change user role
- `DELETE /v1/api/admin/users/{id}` - Delete user

### Paper Management
- `GET /v1/api/admin/papers` - Get all papers (paginated, filtered)
- `GET /v1/api/admin/papers/{id}` - Get paper details
- `DELETE /v1/api/admin/papers/{id}` - Delete paper

### Team Management
- `GET /v1/api/admin/teams` - Get all teams (paginated, filtered)
- `DELETE /v1/api/admin/teams/{id}` - Delete team

### Statistics
- `GET /v1/api/admin/statistics/overview` - Get overview statistics
- `GET /v1/api/admin/statistics/users` - Get user statistics
- `GET /v1/api/admin/statistics/papers` - Get paper statistics

---

## 5. Database Changes

### 5.1. Add ADMIN Role

```sql
-- Migration script
IF NOT EXISTS (SELECT 1 FROM Role WHERE name = 'ADMIN')
BEGIN
    INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
END
```

### 5.2. Create Admin User (Optional - có thể tạo manual)

```sql
-- Create admin user
-- Password should be hashed with BCrypt
-- Example: password "admin123" -> BCrypt hash
DECLARE @adminRoleId UNIQUEIDENTIFIER = (SELECT id FROM Role WHERE name = 'ADMIN');

IF NOT EXISTS (SELECT 1 FROM Users WHERE email = 'admin@labverse.com')
BEGIN
    INSERT INTO Users (id, email, username, full_name, password, created_date, updated_date, Roleid, is_active)
    VALUES (
        NEWID(),
        'admin@labverse.com',
        'admin',
        'System Administrator',
        '$2a$10$...', -- Replace with actual BCrypt hash
        GETDATE(),
        GETDATE(),
        @adminRoleId,
        1
    );
END
```

---

## 6. Security Considerations

### 6.1. Role-Based Access Control

- ✅ Sử dụng `@PreAuthorize("hasRole('ADMIN')")` trên tất cả admin endpoints
- ✅ Check role trong frontend trước khi hiển thị admin links
- ✅ Validate role trong JWT token

### 6.2. Validation

- ✅ Không cho phép admin delete chính mình
- ✅ Không cho phép admin change role của chính mình
- ✅ Validate foreign keys trước khi delete (users, papers, teams)

### 6.3. Audit Logging (Optional)

- Log tất cả admin actions
- Track: who, what, when, why

---

## 7. Implementation Priority

### Phase 1: Core Features (MVP)
1. ✅ Add ADMIN role
2. ✅ User Management (view, activate/deactivate)
3. ✅ Basic Statistics Dashboard
4. ✅ Admin route protection

### Phase 2: Extended Features
1. ✅ Paper Management
2. ✅ Team Management
3. ✅ Change User Role
4. ✅ Advanced Statistics

### Phase 3: Advanced Features
1. ✅ Collection Management
2. ✅ Reading List Management
3. ✅ System Settings
4. ✅ Audit Logging

---

## 8. Testing Checklist

- [ ] Admin can access admin dashboard
- [ ] Non-admin cannot access admin dashboard
- [ ] Admin can view all users
- [ ] Admin can activate/deactivate users
- [ ] Admin can change user roles
- [ ] Admin can view all papers
- [ ] Admin can delete papers
- [ ] Admin can view all teams
- [ ] Admin can delete teams
- [ ] Statistics display correctly
- [ ] Pagination works
- [ ] Filters work
- [ ] Search works

---

**Tài liệu này cung cấp roadmap chi tiết để implement Admin Dashboard cho LabVerse.**

