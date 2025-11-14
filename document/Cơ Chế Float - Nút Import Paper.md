# Cơ Chế Float của Nút Import Paper (FAB)

## Tổng Quan

Nút Import Paper sử dụng **FloatingActionButton (FAB)** với tính năng **drag-and-drop** (kéo thả) để user có thể di chuyển nút đến vị trí mong muốn trên màn hình. Vị trí được lưu lại và khôi phục khi mở lại app.

---

## 1. Cơ Chế Float - Drag and Drop

### 1.1. Khái Niệm

**Float** ở đây có nghĩa là:
- ✅ **Floating**: Nút nổi trên màn hình, không bị scroll theo content
- ✅ **Draggable**: User có thể kéo thả nút đến vị trí bất kỳ
- ✅ **Persistent**: Vị trí được lưu lại và khôi phục khi mở lại app

### 1.2. Cách Hoạt Động

```
1. User touch vào FAB
   ↓
2. Di chuyển ngón tay (drag)
   ↓
3. FAB di chuyển theo ngón tay
   ↓
4. Thả ngón tay (drop)
   ↓
5. Lưu vị trí mới vào SharedPreferences
   ↓
6. Lần sau mở app → Load vị trí đã lưu
```

---

## 2. Implementation Chi Tiết

### 2.1. XML Layout

```xml
<!-- layout_common_ui_home.xml -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fabImportPaper"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginEnd="24dp"
    android:layout_marginBottom="80dp"
    android:contentDescription="Import Paper - Drag to move"
    android:src="@drawable/ic_add"
    app:backgroundTint="@color/primary"
    app:tint="@color/white"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

**Vị trí mặc định:**
- **Bottom Right**: Góc dưới bên phải
- **Margin**: 24dp từ cạnh phải, 80dp từ bottom navbar

---

### 2.2. Setup Drag and Drop

#### Bước 1: Remove Constraints

```java
// Remove constraints để cho phép free movement
ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fabImportPaper.getLayoutParams();
params.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
params.rightToRight = ConstraintLayout.LayoutParams.UNSET;
params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
params.topToTop = ConstraintLayout.LayoutParams.UNSET;
fabImportPaper.setLayoutParams(params);
```

**Mục đích**: Bỏ các ràng buộc của ConstraintLayout để có thể di chuyển tự do bằng `setX()` và `setY()`.

#### Bước 2: Set Default Position

```java
// Tính toán vị trí mặc định (bottom right)
float defaultX = screenWidth - fabWidth - dpToPx(24); // 24dp margin
float defaultY = screenHeight - fabHeight - bottomNavHeight - dpToPx(80); // 80dp margin

// Set vị trí
fabImportPaper.setX(defaultX);
fabImportPaper.setY(defaultY);
```

---

### 2.3. Touch Event Handling

#### ACTION_DOWN (Bắt đầu touch)

```java
case MotionEvent.ACTION_DOWN:
    // Lưu vị trí touch ban đầu
    initialTouchX = event.getRawX();
    initialTouchY = event.getRawY();
    
    // Tính offset giữa FAB và touch point
    dX = view.getX() - event.getRawX();
    dY = view.getY() - event.getRawY();
    
    isDragging = false;
    return false; // Không consume để cho phép click hoạt động
```

**Mục đích**: 
- Lưu vị trí touch ban đầu để tính toán khoảng cách di chuyển
- Tính offset để FAB di chuyển chính xác theo ngón tay

#### ACTION_MOVE (Di chuyển)

```java
case MotionEvent.ACTION_MOVE:
    // Tính khoảng cách di chuyển
    float deltaX = Math.abs(event.getRawX() - initialTouchX);
    float deltaY = Math.abs(event.getRawY() - initialTouchY);
    
    // Chỉ bắt đầu drag nếu di chuyển > threshold (10dp)
    if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
        isDragging = true;
        
        // Ngăn parent view intercept touch events
        view.getParent().requestDisallowInterceptTouchEvent(true);
        
        // Cancel click event
        view.setPressed(false);
        view.cancelLongPress();
        
        // Tính vị trí mới
        float newX = event.getRawX() + dX;
        float newY = event.getRawY() + dY;
        
        // Giới hạn trong màn hình
        newX = Math.max(0, Math.min(newX, screenWidth - fabWidth));
        newY = Math.max(statusBarHeight, Math.min(newY, screenHeight - fabHeight - bottomNavHeight));
        
        // Cập nhật vị trí FAB
        view.setX(newX);
        view.setY(newY);
        
        return true; // Consume event
    }
    return false; // Chưa đủ để drag, cho phép click
```

**Điểm quan trọng:**

1. **DRAG_THRESHOLD = 10dp**: 
   - Chỉ bắt đầu drag khi di chuyển > 10dp
   - Tránh nhầm lẫn giữa click và drag

2. **Boundary Constraints**:
   - Giới hạn FAB trong màn hình
   - Không cho phép di chuyển ra ngoài màn hình
   - Tránh che bottom navbar

3. **Click vs Drag**:
   - Nếu di chuyển < threshold → Click event
   - Nếu di chuyển > threshold → Drag event (cancel click)

#### ACTION_UP / ACTION_CANCEL (Kết thúc)

```java
case MotionEvent.ACTION_UP:
case MotionEvent.ACTION_CANCEL:
    // Cho phép parent intercept lại
    view.getParent().requestDisallowInterceptTouchEvent(false);
    
    if (isDragging) {
        // Lưu vị trí sau khi drag
        saveFabPosition(view.getX(), view.getY());
        isDragging = false;
        view.setPressed(false);
        return true; // Consume để prevent click
    }
    
    isDragging = false;
    return false; // Cho phép click event
```

---

### 2.4. Lưu và Load Vị Trí

#### Lưu Vị Trí

```java
private void saveFabPosition(float x, float y) {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putFloat(KEY_FAB_X, x);
    editor.putFloat(KEY_FAB_Y, y);
    editor.apply(); // Async save
}
```

**Lưu vào**: `SharedPreferences` với key `"FeedActivityPrefs"`

#### Load Vị Trí

```java
private void loadFabPosition() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    float savedX = prefs.getFloat(KEY_FAB_X, -1);
    float savedY = prefs.getFloat(KEY_FAB_Y, -1);
    
    if (savedX >= 0 && savedY >= 0) {
        // Validate và apply vị trí đã lưu
        float x = Math.max(0, Math.min(savedX, screenWidth - fabWidth));
        float y = Math.max(statusBarHeight, Math.min(savedY, screenHeight - fabHeight - bottomNavHeight));
        
        fabImportPaper.setX(x);
        fabImportPaper.setY(y);
    } else {
        // Không có vị trí đã lưu → dùng default position
    }
}
```

**Validation**: Đảm bảo vị trí đã lưu vẫn hợp lệ khi mở lại (màn hình có thể đã thay đổi kích thước).

---

## 3. Click Event Handling

### 3.1. Phân Biệt Click vs Drag

```java
fabImportPaper.setOnClickListener(v -> {
    // Chỉ trigger click nếu KHÔNG đang drag
    if (!isDragging) {
        Intent intent = new Intent(FeedActivity.this, ImportPaperManuallyActivity.class);
        startActivityForResult(intent, REQUEST_CODE_IMPORT_PAPER);
    }
});
```

**Logic:**
- Nếu `isDragging = false` → Click event → Mở ImportPaperManuallyActivity
- Nếu `isDragging = true` → Không trigger click → Chỉ drag

### 3.2. Click Event Flow

```
User tap FAB
   ↓
ACTION_DOWN → isDragging = false
   ↓
ACTION_MOVE → Check distance
   ↓
   ├─ Distance < 10dp → Click (isDragging = false)
   └─ Distance > 10dp → Drag (isDragging = true, cancel click)
   ↓
ACTION_UP
   ↓
   ├─ isDragging = true → Save position, prevent click
   └─ isDragging = false → Trigger onClick → Open ImportPaperManuallyActivity
```

---

## 4. Visibility Management

### 4.1. Tab-Based Visibility

```java
// Ẩn FAB khi ở tab Teams (position 2)
tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        FloatingActionButton fab = findViewById(R.id.fabImportPaper);
        if (fab != null) {
            fab.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
        }
    }
});
```

**Logic:**
- **Tab 0 (Discovery)**: Hiển thị FAB
- **Tab 1 (My Papers)**: Hiển thị FAB
- **Tab 2 (Teams)**: Ẩn FAB

**Lý do**: Tab Teams không cần import paper (chỉ quản lý teams).

---

## 5. Boundary Constraints

### 5.1. Screen Bounds

```java
// Giới hạn trong màn hình
int screenWidth = rootLayout.getWidth();
int screenHeight = rootLayout.getHeight();
int statusBarHeight = displayRect.top;
int bottomNavHeight = bottomNav.getHeight();

// Constrain
newX = Math.max(0, Math.min(newX, screenWidth - fabWidth));
newY = Math.max(statusBarHeight, Math.min(newY, screenHeight - fabHeight - bottomNavHeight));
```

**Constraints:**
- **Left**: `x >= 0`
- **Right**: `x <= screenWidth - fabWidth`
- **Top**: `y >= statusBarHeight`
- **Bottom**: `y <= screenHeight - fabHeight - bottomNavHeight`

---

## 6. Luồng Hoạt Động Tổng Thể

### 6.1. Khi App Khởi Động

```
1. onCreate() → setupFabDragAndDrop()
   ↓
2. Wait for layout measurement
   ↓
3. Remove constraints
   ↓
4. Set default position (bottom right)
   ↓
5. Load saved position (nếu có)
   ↓
6. Apply position → FAB hiển thị
```

### 6.2. Khi User Drag FAB

```
1. Touch FAB → ACTION_DOWN
   ↓
2. Move finger → ACTION_MOVE
   ↓
3. Check distance > 10dp?
   ├─ NO → Continue (allow click)
   └─ YES → Start dragging
       ↓
4. Calculate new position
   ↓
5. Constrain within screen bounds
   ↓
6. Update FAB position (setX, setY)
   ↓
7. Release finger → ACTION_UP
   ↓
8. Save position to SharedPreferences
```

### 6.3. Khi User Click FAB

```
1. Touch FAB → ACTION_DOWN
   ↓
2. Move finger < 10dp → ACTION_MOVE (not dragging)
   ↓
3. Release finger → ACTION_UP
   ↓
4. isDragging = false → Trigger onClick
   ↓
5. Open ImportPaperManuallyActivity
```

---

## 7. Technical Details

### 7.1. Coordinate System

- **getRawX() / getRawY()**: Tọa độ tuyệt đối trên màn hình
- **getX() / getY()**: Tọa độ tương đối của view
- **setX() / setY()**: Set vị trí của view (sau khi remove constraints)

### 7.2. Touch Event Consumption

- **return true**: Consume event → Không propagate
- **return false**: Không consume → Cho phép click event

### 7.3. Request Disallow Intercept

```java
view.getParent().requestDisallowInterceptTouchEvent(true);
```

**Mục đích**: Ngăn parent view (ScrollView, RecyclerView) intercept touch events khi đang drag FAB.

---

## 8. Use Cases

### Use Case 1: User Drag FAB
```
Scenario: User muốn di chuyển FAB sang trái để không che content

Steps:
1. Touch và giữ FAB
2. Kéo sang trái
3. Thả tay
4. FAB ở vị trí mới
5. Lần sau mở app → FAB vẫn ở vị trí đó
```

### Use Case 2: User Click FAB
```
Scenario: User muốn import paper mới

Steps:
1. Tap FAB (không kéo)
2. Mở ImportPaperManuallyActivity
3. Upload PDF
4. Quay lại → FAB vẫn ở vị trí cũ
```

### Use Case 3: User Switch Tabs
```
Scenario: User chuyển sang tab Teams

Steps:
1. Click tab Teams
2. FAB tự động ẩn (View.GONE)
3. Click tab My Papers
4. FAB tự động hiện lại (View.VISIBLE)
```

---

## 9. Câu Trả Lời Ngắn Gọn

**"Cơ chế float của nút import paper hoạt động như thế nào?"**

**Trả lời:**

"Nút Import Paper sử dụng FloatingActionButton (FAB) với tính năng drag-and-drop:

1. **Floating**: Nút nổi trên màn hình, không bị scroll theo content

2. **Draggable**: 
   - User có thể kéo thả nút đến vị trí bất kỳ
   - Sử dụng TouchEvent handling (ACTION_DOWN, ACTION_MOVE, ACTION_UP)
   - Có threshold 10dp để phân biệt click và drag

3. **Persistent**: 
   - Vị trí được lưu vào SharedPreferences
   - Khôi phục vị trí khi mở lại app

4. **Boundary Constraints**: 
   - Giới hạn FAB trong màn hình
   - Tránh che status bar và bottom navbar

5. **Click vs Drag**:
   - Di chuyển < 10dp → Click event → Mở ImportPaperManuallyActivity
   - Di chuyển > 10dp → Drag event → Di chuyển FAB

**Implementation**: Remove constraints của ConstraintLayout, sử dụng setX()/setY() để di chuyển tự do."

---

## 10. Code Summary

### Key Components:

1. **FloatingActionButton**: Material Design component
2. **TouchEvent Listener**: Handle drag and drop
3. **SharedPreferences**: Lưu vị trí
4. **ConstraintLayout**: Layout container (constraints được remove để cho phép free movement)
5. **Coordinate Calculation**: Tính toán vị trí dựa trên screen bounds

### Key Methods:

- `setupFabDragAndDrop()`: Setup drag and drop functionality
- `saveFabPosition()`: Lưu vị trí vào SharedPreferences
- `loadFabPosition()`: Load vị trí từ SharedPreferences
- `onTouch()`: Handle touch events
- `updateFabVisibility()`: Show/hide FAB based on tab

---

**Tài liệu này giải thích chi tiết cơ chế float/drag-and-drop của nút Import Paper trong Android app.**

