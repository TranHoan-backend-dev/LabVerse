# Room Database Architecture - LabVerse Android App

## Tổng quan

Ứng dụng Android đang sử dụng **Room Database**, một abstraction layer trên **SQLite** được Google khuyến nghị. Room tự động tạo và quản lý SQLite database file.

## Room Database là gì?

**Room** là một persistence library cung cấp một abstraction layer trên SQLite để cho phép truy cập database một cách dễ dàng hơn, đồng thời tận dụng sức mạnh của SQLite.

### Ưu điểm của Room:
- ✅ Compile-time SQL validation
- ✅ Type-safe queries
- ✅ Giảm boilerplate code
- ✅ Tích hợp với LiveData và RxJava
- ✅ Migration support

## Cấu trúc Database

### 1. Database File Location
```
/data/data/com.se1853_jv.labverse/databases/labverse-db
```

Database được lưu trữ dưới dạng SQLite file (.db) trong thư mục private của app.

### 2. Database Components

#### a) **Entity** (Bảng dữ liệu)
Định nghĩa cấu trúc bảng trong database.

**Ví dụ: Team Entity**
```java
@Entity
public class Team {
    @PrimaryKey
    @NonNull
    String id;
    
    @NonNull
    String name;
    
    String description;
    
    @ColumnInfo(name = "research_field")
    String researchField;
    
    // ... các fields khác
}
```

Room tự động tạo SQL table từ Entity:
```sql
CREATE TABLE Team (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    research_field TEXT,
    ...
)
```

#### b) **DAO (Data Access Object)**
Định nghĩa các phương thức truy cập database.

**Ví dụ: TeamRepository**
```java
@Dao
public interface TeamRepository {
    @Query("SELECT * FROM Team ORDER BY createdDate DESC")
    List<Team> getAll();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Team> teams);
    
    @Query("SELECT * FROM Team WHERE id = :id")
    Team getById(String id);
}
```

Room tự động generate implementation cho các DAO methods.

#### c) **Database Class**
Định nghĩa database và các DAO.

**AppDatabase.java**
```java
@Database(
    entities = {
        Users.class,
        Roles.class,
        Team.class,
        Collections.class,
        // ... các entities khác
    },
    version = 1
)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TeamRepository teamRepository();
    public abstract UserRepository userRepository();
    // ... các repositories khác
}
```

### 3. Database Client (Singleton Pattern)

**DatabaseClient.java** - Quản lý instance của database:

```java
public class DatabaseClient {
    private static volatile DatabaseClient instance;
    private final AppDatabase appDB;
    
    private DatabaseClient(Context context) {
        appDB = Room.databaseBuilder(
            context.getApplicationContext(),
            AppDatabase.class,
            "labverse-db"  // Tên file database
        )
        .fallbackToDestructiveMigration(true) // Xóa data khi migration fail
        .build();
    }
    
    public static DatabaseClient getInstance(Context context) {
        // Double-checked locking pattern
        if (instance == null) {
            synchronized (DatabaseClient.class) {
                if (instance == null) {
                    instance = new DatabaseClient(context);
                }
            }
        }
        return instance;
    }
}
```

## Cách Data được Lưu trữ

### 1. **SQLite File Structure**
Room tạo các file sau:
- `labverse-db` - Main database file
- `labverse-db-shm` - Shared memory file (SQLite WAL mode)
- `labverse-db-wal` - Write-Ahead Log file

### 2. **Data Persistence Flow**

```
API Response (JSON)
    ↓
TeamResponse DTO
    ↓
Convert to Team Entity
    ↓
Room DAO (insertAll)
    ↓
SQLite Database File
```

### 3. **Ví dụ: Lưu Teams vào Database**

```java
// 1. Lấy data từ API
teamApiHandler.getTeams(..., new ApiCallback<TeamsPageResponse>() {
    @Override
    public void onSuccess(TeamsPageResponse response) {
        List<TeamResponse> teamResponses = response.getContent();
        
        // 2. Convert DTO → Entity
        List<Team> teams = convertToTeams(teamResponses);
        
        // 3. Lưu vào database (chạy trên background thread)
        new Thread(() -> {
            teamRepository.insertAll(teams);
        }).start();
    }
});
```

### 4. **Đọc Data từ Database**

```java
// Chạy trên background thread
new Thread(() -> {
    List<Team> teams = teamRepository.getAll();
    
    // Update UI trên main thread
    runOnUiThread(() -> {
        displayTeams(teams);
    });
}).start();
```

## Type Converters

Room không hỗ trợ một số types như `Date`, `List`, `Enum`. Cần dùng `@TypeConverter`:

**Converter.java**
```java
@TypeConverter
public static Long fromDate(Date date) {
    return date == null ? null : date.getTime();
}

@TypeConverter
public static Date toDate(Long millis) {
    return millis == null ? null : new Date(millis);
}
```

## Database Operations

### Thread Safety
- ✅ Room đảm bảo thread-safe
- ✅ Các operations phải chạy trên background thread (trừ `allowMainThreadQueries()`)
- ✅ Sử dụng `new Thread()` hoặc `ExecutorService` cho database operations

### Transaction Support
```java
@Transaction
@Query("SELECT * FROM Team")
List<Team> getAllTeamsWithMembers();
```

## Migration

Khi thay đổi schema (thêm/sửa/xóa column), cần tăng version và định nghĩa migration:

```java
@Database(version = 2) // Tăng version

// Migration từ version 1 → 2
Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE Team ADD COLUMN new_field TEXT");
    }
};

// Thêm vào builder
.addMigrations(MIGRATION_1_2)
```

**Hiện tại:** `fallbackToDestructiveMigration(true)` - Xóa toàn bộ data khi migration fail (chỉ dùng cho development).

## Dependencies

**build.gradle.kts**
```kotlin
implementation(libs.room.runtime)  // Room runtime
annotationProcessor(libs.room.compiler.v250)  // Code generation
```

## Tóm tắt

1. ✅ **Room Database** = Abstraction layer trên **SQLite**
2. ✅ Database file: `/data/data/com.se1853_jv.labverse/databases/labverse-db`
3. ✅ **Entity** → SQL Table
4. ✅ **DAO** → SQL Queries
5. ✅ **Database** → Database instance
6. ✅ Data được persist vào SQLite file
7. ✅ Thread-safe operations
8. ✅ Type-safe queries với compile-time validation

## Lưu ý

- ⚠️ Database operations phải chạy trên background thread
- ⚠️ `fallbackToDestructiveMigration(true)` sẽ xóa data khi migration fail
- ⚠️ Database file chỉ accessible trong app sandbox (private)
- ✅ Room tự động generate SQL code tại compile-time
- ✅ Có thể xem database bằng Android Studio Database Inspector

