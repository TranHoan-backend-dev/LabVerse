package com.se1853_jv.labverse.presentation.library.data;

import com.se1853_jv.labverse.presentation.library.model.LibraryPaper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryMockDataProvider {
    
    private static LibraryMockDataProvider instance;
    private Map<String, LibraryPaper> allPapers;
    
    private LibraryMockDataProvider() {
        initializeMockData();
    }
    
    public static LibraryMockDataProvider getInstance() {
        if (instance == null) {
            instance = new LibraryMockDataProvider();
        }
        return instance;
    }
    
    private void initializeMockData() {
        allPapers = new HashMap<>();
        
        // Paper 1
        LibraryPaper paper1 = LibraryPaper.builder()
                .id("1")
                .title("Deep Learning Approaches for Protein Structure Prediction")
                .authors("Chen, L., Wang, K., Smith, J.")
                .journal("Nature Biotechnology")
                .year(2024)
                .status("Unread")
                .statusColor("blue")
                .citationCount(127)
                .readCount(2345)
                .isFavorite(false)
                .addedDate("Added 2 days ago")
                .progress(0)
                .build();
        allPapers.put(paper1.getId(), paper1);
        
        // Paper 2
        LibraryPaper paper2 = LibraryPaper.builder()
                .id("2")
                .title("CRISPR-Cas9 Applications in Gene Therapy")
                .authors("Rodriguez, M., Thompson, A., Lee, S.")
                .journal("Cell")
                .year(2024)
                .status("Reading")
                .statusColor("yellow")
                .citationCount(89)
                .readCount(1823)
                .isFavorite(true)
                .addedDate("Tags 12.12.22")
                .lastReadDate("Read 5 hours ago")
                .progress(45)
                .build();
        allPapers.put(paper2.getId(), paper2);
        
        // Paper 3
        LibraryPaper paper3 = LibraryPaper.builder()
                .id("3")
                .title("Machine Learning in Drug Discovery")
                .authors("Johnson, B., Kim, H.")
                .journal("Science")
                .year(2023)
                .status("Finished")
                .statusColor("green")
                .citationCount(234)
                .readCount(3456)
                .isFavorite(true)
                .addedDate("Completed yesterday")
                .lastReadDate("Read yesterday")
                .progress(100)
                .build();
        allPapers.put(paper3.getId(), paper3);
        
        // Paper 4
        LibraryPaper paper4 = LibraryPaper.builder()
                .id("4")
                .title("Quantum Computing for Molecular Simulations")
                .authors("Zhang, Y., Miller, P.")
                .journal("Nature")
                .year(2024)
                .status("Unread")
                .statusColor("blue")
                .citationCount(156)
                .readCount(2100)
                .isFavorite(false)
                .addedDate("Added 3 days ago")
                .progress(0)
                .build();
        allPapers.put(paper4.getId(), paper4);
        
        // Paper 5
        LibraryPaper paper5 = LibraryPaper.builder()
                .id("5")
                .title("Advances in Immunotherapy for Cancer Treatment")
                .authors("Brown, D., Garcia, M.")
                .journal("The Lancet")
                .year(2023)
                .status("Reading")
                .statusColor("yellow")
                .citationCount(198)
                .readCount(2890)
                .isFavorite(false)
                .addedDate("Added 5 days ago")
                .lastReadDate("Read 2 days ago")
                .progress(30)
                .build();
        allPapers.put(paper5.getId(), paper5);
        
        // Paper 6
        LibraryPaper paper6 = LibraryPaper.builder()
                .id("6")
                .title("Neural Networks for Climate Modeling")
                .authors("Anderson, K., White, L.")
                .journal("Nature Climate Change")
                .year(2024)
                .status("Unread")
                .statusColor("blue")
                .citationCount(78)
                .readCount(1456)
                .isFavorite(false)
                .addedDate("Added 1 week ago")
                .progress(0)
                .build();
        allPapers.put(paper6.getId(), paper6);
        
        // Paper 7
        LibraryPaper paper7 = LibraryPaper.builder()
                .id("7")
                .title("Blockchain Technology in Healthcare Systems")
                .authors("Davis, R., Martinez, S.")
                .journal("IEEE Transactions")
                .year(2023)
                .status("Finished")
                .statusColor("green")
                .citationCount(145)
                .readCount(2234)
                .isFavorite(true)
                .addedDate("Completed 3 days ago")
                .lastReadDate("Read 3 days ago")
                .progress(100)
                .build();
        allPapers.put(paper7.getId(), paper7);
    }
    
    /**
     * Get paper by ID
     */
    public LibraryPaper getPaperById(String paperId) {
        return allPapers.get(paperId);
    }
    
    /**
     * Get all papers
     */
    public List<LibraryPaper> getAllPapers() {
        return new ArrayList<>(allPapers.values());
    }
    
    /**
     * Get papers by tab (Recently Added, Recently Read, Favorites)
     */
    public List<LibraryPaper> getPapersByTab(String tab) {
        List<LibraryPaper> papers = new ArrayList<>();
        
        switch (tab) {
            case "recently_added":
                // Return papers sorted by added date (newest first)
                papers.add(allPapers.get("1")); // Added 2 days ago
                papers.add(allPapers.get("4")); // Added 3 days ago
                papers.add(allPapers.get("5")); // Added 5 days ago
                papers.add(allPapers.get("6")); // Added 1 week ago
                papers.add(allPapers.get("2")); // Tags 12.12.22
                papers.add(allPapers.get("3")); // Completed yesterday
                papers.add(allPapers.get("7")); // Completed 3 days ago
                break;
                
            case "recently_read":
                // Return papers that have been read recently
                papers.add(allPapers.get("2")); // Read 5 hours ago
                papers.add(allPapers.get("3")); // Read yesterday
                papers.add(allPapers.get("5")); // Read 2 days ago
                papers.add(allPapers.get("7")); // Read 3 days ago
                break;
                
            case "favorites":
                // Return only favorite papers
                for (LibraryPaper paper : allPapers.values()) {
                    if (paper.isFavorite()) {
                        papers.add(paper);
                    }
                }
                break;
        }
        
        return papers;
    }
    
    /**
     * Get papers by status filter (Unread, Reading, Finished)
     */
    public List<LibraryPaper> getPapersByStatus(String status) {
        List<LibraryPaper> papers = new ArrayList<>();
        
        for (LibraryPaper paper : allPapers.values()) {
            if (paper.getStatus().equalsIgnoreCase(status)) {
                papers.add(paper);
            }
        }
        
        return papers;
    }
    
    /**
     * Get papers by tab and status filter
     */
    public List<LibraryPaper> getPapersByTabAndStatus(String tab, String status) {
        List<LibraryPaper> tabPapers = getPapersByTab(tab);
        List<LibraryPaper> filteredPapers = new ArrayList<>();
        
        for (LibraryPaper paper : tabPapers) {
            if (paper.getStatus().equalsIgnoreCase(status)) {
                filteredPapers.add(paper);
            }
        }
        
        return filteredPapers;
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String paperId) {
        LibraryPaper paper = allPapers.get(paperId);
        if (paper != null) {
            paper.setFavorite(!paper.isFavorite());
        }
    }
    
    /**
     * Update paper status
     */
    public void updatePaperStatus(String paperId, String newStatus) {
        LibraryPaper paper = allPapers.get(paperId);
        if (paper != null) {
            paper.setStatus(newStatus);
            
            // Update status color based on new status
            switch (newStatus) {
                case "Unread":
                    paper.setStatusColor("blue");
                    paper.setProgress(0);
                    break;
                case "Reading":
                    paper.setStatusColor("yellow");
                    break;
                case "Finished":
                    paper.setStatusColor("green");
                    paper.setProgress(100);
                    break;
            }
        }
    }
    
    /**
     * Update reading progress
     */
    public void updateProgress(String paperId, int progress) {
        LibraryPaper paper = allPapers.get(paperId);
        if (paper != null) {
            paper.setProgress(progress);
            
            // Auto update status based on progress
            if (progress == 0) {
                paper.setStatus("Unread");
                paper.setStatusColor("blue");
            } else if (progress == 100) {
                paper.setStatus("Finished");
                paper.setStatusColor("green");
            } else {
                paper.setStatus("Reading");
                paper.setStatusColor("yellow");
            }
        }
    }
}
