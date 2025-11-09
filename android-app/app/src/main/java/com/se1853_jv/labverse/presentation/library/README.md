# Library Module - Mock Data Guide

## Cấu trúc

```
library/
├── LibraryActivity.java          # Main activity
├── adapter/
│   └── LibraryPaperAdapter.java  # RecyclerView adapter
├── data/
│   └── LibraryMockDataProvider.java  # Mock data provider (Singleton)
└── model/
    └── LibraryPaper.java         # Data model
```

## LibraryMockDataProvider - Quản lý Mock Data

### Singleton Pattern
```java
LibraryMockDataProvider dataProvider = LibraryMockDataProvider.getInstance();
```

### Các phương thức chính:

#### 1. Lấy paper theo ID
```java
LibraryPaper paper = dataProvider.getPaperById("1");
```

#### 2. Lấy tất cả papers
```java
List<LibraryPaper> allPapers = dataProvider.getAllPapers();
```

#### 3. Lấy papers theo tab
```java
// Tab: "recently_added", "recently_read", "favorites"
List<LibraryPaper> papers = dataProvider.getPapersByTab("recently_added");
```

#### 4. Lấy papers theo status
```java
// Status: "Unread", "Reading", "Finished"
List<LibraryPaper> unreadPapers = dataProvider.getPapersByStatus("Unread");
```

#### 5. Lấy papers theo tab và status
```java
List<LibraryPaper> papers = dataProvider.getPapersByTabAndStatus("recently_added", "Unread");
```

#### 6. Toggle favorite
```java
dataProvider.toggleFavorite("1"); // Toggle favorite cho paper có id = "1"
```

#### 7. Cập nhật status
```java
// Tự động cập nhật statusColor và progress
dataProvider.updatePaperStatus("1", "Reading");
```

#### 8. Cập nhật progress
```java
// Tự động cập nhật status dựa trên progress
// 0% -> Unread, 1-99% -> Reading, 100% -> Finished
dataProvider.updateProgress("1", 45);
```

## Mock Data có sẵn

### 7 Papers:

1. **Paper 1** - Deep Learning Approaches for Protein Structure Prediction
   - Status: Unread
   - Added: 2 days ago

2. **Paper 2** - CRISPR-Cas9 Applications in Gene Therapy
   - Status: Reading (45%)
   - Favorite: Yes
   - Last read: 5 hours ago

3. **Paper 3** - Machine Learning in Drug Discovery
   - Status: Finished
   - Favorite: Yes
   - Last read: Yesterday

4. **Paper 4** - Quantum Computing for Molecular Simulations
   - Status: Unread
   - Added: 3 days ago

5. **Paper 5** - Advances in Immunotherapy for Cancer Treatment
   - Status: Reading (30%)
   - Last read: 2 days ago

6. **Paper 6** - Neural Networks for Climate Modeling
   - Status: Unread
   - Added: 1 week ago

7. **Paper 7** - Blockchain Technology in Healthcare Systems
   - Status: Finished
   - Favorite: Yes
   - Last read: 3 days ago

## Cách sử dụng trong Activity

```java
// 1. Khởi tạo data provider
LibraryMockDataProvider dataProvider = LibraryMockDataProvider.getInstance();

// 2. Load papers cho tab
List<LibraryPaper> papers = dataProvider.getPapersByTab("recently_added");

// 3. Load papers với filter
List<LibraryPaper> filteredPapers = dataProvider.getPapersByTabAndStatus("recently_added", "Unread");

// 4. Lấy thông tin 1 paper cụ thể (khi click vào card)
LibraryPaper paper = dataProvider.getPaperById(paperId);

// 5. Toggle favorite
dataProvider.toggleFavorite(paperId);

// 6. Cập nhật progress khi đọc
dataProvider.updateProgress(paperId, 75);
```

## Ví dụ trong Adapter

```java
// Khi click vào bookmark button
bookmarkButton.setOnClickListener(v -> {
    dataProvider.toggleFavorite(paper.getId());
    notifyItemChanged(getAdapterPosition());
});

// Khi click vào card để xem chi tiết
cardView.setOnClickListener(v -> {
    // Lấy thông tin paper từ data provider
    LibraryPaper fullPaper = dataProvider.getPaperById(paper.getId());
    
    Intent intent = new Intent(context, PaperDetailsActivity.class);
    intent.putExtra("paper_id", fullPaper.getId());
    context.startActivity(intent);
});
```

## Lưu ý

- **Singleton Pattern**: Dữ liệu được chia sẻ toàn app, thay đổi ở đâu cũng được cập nhật
- **Auto Update**: Khi update progress hoặc status, các thuộc tính liên quan tự động cập nhật
- **Persistent**: Data tồn tại trong suốt lifecycle của app (cho đến khi app bị kill)
- **Easy to extend**: Dễ dàng thêm papers mới trong `initializeMockData()`

## Mở rộng

### Thêm paper mới:
```java
// Trong initializeMockData() method
LibraryPaper paper8 = LibraryPaper.builder()
    .id("8")
    .title("Your Paper Title")
    .authors("Author Names")
    .journal("Journal Name")
    .year(2024)
    .status("Unread")
    .statusColor("blue")
    .citationCount(100)
    .readCount(1000)
    .isFavorite(false)
    .addedDate("Added today")
    .progress(0)
    .build();
allPapers.put(paper8.getId(), paper8);
```
