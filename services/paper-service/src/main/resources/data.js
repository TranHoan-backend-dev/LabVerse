use labverse_paper_service

const tags = [
    {_id: "e5507c1e-e431-4f16-b5d4-bd11138e1963", name: "AI", paperIds: []},
    {_id: "53e497b0-9ec7-4ebc-9ade-56f4d3e7be67", name: "Healthcare", paperIds: []},
    {_id: "e6ae0fc9-bd5d-44c2-89a4-20bc82fc0531", name: "Blockchain", paperIds: []},
    {_id: "9ebbecf2-2fdd-4127-893f-2a0808407b22", name: "Cybersecurity", paperIds: []},
    {_id: "39668bee-e3e8-48c7-a8c3-d6006e3a7767", name: "Big Data", paperIds: []},
    {_id: "9d1bf555-d337-4954-9082-95b1be10ab55", name: "Cloud Computing", paperIds: []},
    {_id: "974fb8dc-cb6f-439d-8ecf-1ac73845cafb", name: "NLP", paperIds: []},
    {_id: "656183be-aa82-46d5-bc43-24938a671576", name: "Quantum", paperIds: []},
    {_id: "b968add3-0634-4fef-87ab-78722ffd65bc", name: "Bioinformatics", paperIds: []},
    {_id: "6f427030-28b7-4f8b-a1b0-2db4249af49d", name: "Autonomous Systems", paperIds: []}
]

db.tags.insertMany(tags)

const papers = [
    {
        _id: "b3cde6a5-af2b-4d2c-9caf-7e1867d679b8",
        metadata: {
            title: "Deep Learning for Biomedical Text Mining",
            authors: "Nguyen Van A, Tran Thi B",
            journal: "Journal of AI in Medicine",
            publicationYear: 2022,
            doi: "10.1007/ai-med.2022.001"
        },
        dataUrl: "https://example.com/paper1.pdf",
        keywords: ["AI", "Deep Learning", "Biomedical"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "1bd295a0-7746-494a-8ae9-ccf7c973a902",
        metadata: {
            title: "Quantum Computing in Modern Cryptography",
            authors: "Le Quang Huy",
            journal: "Vietnam Journal of Computer Science",
            publicationYear: 2021,
            doi: "10.1234/vjcs.2021.045"
        },
        dataUrl: "https://example.com/paper2.pdf",
        keywords: ["Quantum", "Cryptography", "Security"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "74c1df61-5d64-4404-8285-99eb8fdba9ac",
        metadata: {
            title: "Blockchain Applications in Healthcare",
            authors: "Pham Hoang Linh",
            journal: "Healthcare IT Today",
            publicationYear: 2023,
            doi: "10.5678/hit.2023.007"
        },
        dataUrl: "https://example.com/paper3.pdf",
        keywords: ["Blockchain", "Healthcare", "Privacy"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "1270ad3e-a299-4d6e-b436-c9bd7c030121",
        metadata: {
            title: "Efficient Algorithms for Big Data Analytics",
            authors: "Vo Thi Thanh",
            journal: "International Journal of Data Science",
            publicationYear: 2020,
            doi: "10.9999/ijds.2020.032"
        },
        dataUrl: "https://example.com/paper4.pdf",
        keywords: ["Big Data", "Algorithms", "Analytics"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "ffac3859-cdcf-4ad3-ac9d-7f7626ecce6c",
        metadata: {
            title: "Natural Language Processing for Vietnamese",
            authors: "Nguyen Tuan Kiet",
            journal: "Computational Linguistics Vietnam",
            publicationYear: 2023,
            doi: "10.7777/clv.2023.010"
        },
        dataUrl: "https://example.com/paper5.pdf",
        keywords: ["NLP", "Vietnamese", "AI"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "e28fc03f-2c7c-4e3c-8f7e-49cde533ef3f",
        metadata: {
            title: "Cloud-native Microservice Architectures",
            authors: "Tran Hoang Nam",
            journal: "Software Engineering Review",
            publicationYear: 2024,
            doi: "10.5555/ser.2024.022"
        },
        dataUrl: "https://example.com/paper6.pdf",
        keywords: ["Microservice", "Spring Boot", "Kubernetes"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "0740eef8-c778-40d1-8c4e-a25aa61761ab",
        metadata: {
            title: "Reinforcement Learning in Autonomous Driving",
            authors: "Doan Minh Tri",
            journal: "AI & Robotics Journal",
            publicationYear: 2022,
            doi: "10.4242/airob.2022.015"
        },
        dataUrl: "https://example.com/paper7.pdf",
        keywords: ["Reinforcement Learning", "Autonomous", "Robotics"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "f69c7933-27e8-40bc-9d1b-720ba033159a",
        metadata: {
            title: "Medical Image Segmentation using U-Net",
            authors: "Pham Quoc Bao",
            journal: "Medical Imaging Today",
            publicationYear: 2021,
            doi: "10.3131/mit.2021.044"
        },
        dataUrl: "https://example.com/paper8.pdf",
        keywords: ["U-Net", "Image Segmentation", "Medical AI"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "5b20e512-c29b-4a46-a465-c2b7c88d1a7a",
        metadata: {
            title: "Data Privacy in IoT Systems",
            authors: "Tran Duc Minh",
            journal: "IoT Security Review",
            publicationYear: 2020,
            doi: "10.4455/iotsec.2020.099"
        },
        dataUrl: "https://example.com/paper9.pdf",
        keywords: ["IoT", "Privacy", "Security"],
        citationIds: [],
        tagIds: []
    },
    {
        _id: "32bb063b-55e3-46a1-8dfb-d555e5bb325a",
        metadata: {
            title: "AI-assisted Drug Discovery",
            authors: "Nguyen Hoang Phuc",
            journal: "Bioinformatics Advances",
            publicationYear: 2024,
            doi: "10.3211/bioadv.2024.011"
        },
        dataUrl: "https://example.com/paper10.pdf",
        keywords: ["Drug Discovery", "AI", "Bioinformatics"],
        citationIds: [],
        tagIds: []
    }
]

db.papers.insertMany(papers)

const citations = [
    {
        _id: "f93297a2-fc8b-41a8-8158-68d5449dcc08",
        metadata: {
            title: "Cited by Deep Learning Methods",
            authors: "Smith et al.",
            journal: "Neural Networks Review",
            publicationYear: 2020,
            doi: "10.1234/cite.2020.001",
        },
        paperIds: []
    },
    {
        _id: "5b0a7ccb-dc0e-4ef8-8b5a-660a18323782",
        metadata: {
            title: "Reference on AI-assisted Healthcare",
            authors: "Johnson et al.",
            journal: "Medical AI Journal",
            publicationYear: 2021,
            doi: "10.5678/cite.2021.002",
        },
        paperIds: []
    },
    {
        _id: "a03cf387-55e6-42e8-993e-8a5995173d31",
        metadata: {
            title: "Blockchain Security Principles",
            authors: "Lee K.",
            journal: "CyberSecurity Review",
            publicationYear: 2022,
            doi: "10.7777/cite.2022.003",
        },
        paperIds: []
    },
    {
        _id: "c9ab5060-14d0-4833-9af3-f3e54ae46fb5",
        metadata: {
            title: "Applications of NLP in Asian Languages",
            authors: "Nguyen P.",
            journal: "Linguistic Computing",
            publicationYear: 2023,
            doi: "10.8888/cite.2023.004",
        },
        paperIds: []
    },
    {
        _id: "9e74cf1d-6aee-475f-b5ab-89b237823613",
        metadata: {
            title: "Trends in AI and Robotics",
            authors: "Tran D.",
            journal: "Automation Review",
            publicationYear: 2024,
            doi: "10.9999/cite.2024.005",
        },
        paperIds: []
    }
]

db.citations.insertMany(citations)

const highlights = [
    {
        _id: "636662e0-ef50-4d36-ae1e-31fcdd8e6584",
        colorCode: "#FFD700",
        coordinationX: 120,
        coordinationY: 540,
        pageNumber: 1
    },
    {
        _id: "650440c3-db59-4054-9631-20b4e1c9682c",
        colorCode: "#FF4500",
        coordinationX: 200,
        coordinationY: 620,
        pageNumber: 2
    },
    {
        _id: "24f3a20b-c312-44d2-ae8a-d33d8f203c6b",
        colorCode: "#32CD32",
        coordinationX: 330,
        coordinationY: 420,
        pageNumber: 3
    },
    {
        _id: "e0feb23e-22a3-476b-8359-2cf0bf1d87e2",
        colorCode: "#1E90FF",
        coordinationX: 410,
        coordinationY: 530,
        pageNumber: 1
    },
    {
        _id: "cd5d9d48-290b-4ef2-8934-6eca8469195b",
        colorCode: "#FF69B4",
        coordinationX: 510,
        coordinationY: 680,
        pageNumber: 4
    }
]

db.highlights.insertMany(highlights)

const notes = [
    {
        _id: "b708bd8c-24b8-440d-bcda-c8f4e01b5bc1",
        content: "Need to review related works in section 2.",
        coordinationX: 150,
        coordinationY: 520,
        pageNumber: 1
    },
    {
        _id: "978f1670-f12b-4179-b4fe-4081c6c7991f",
        content: "Double-check the citation formatting.",
        coordinationX: 240,
        coordinationY: 600,
        pageNumber: 2
    },
    {
        _id: "0e2b86d5-3104-4a71-8d3a-1260e05be4c4",
        content: "Include more discussion on AI bias.",
        coordinationX: 300,
        coordinationY: 440,
        pageNumber: 3
    },
    {
        _id: "7b9333e5-08bc-4e85-a4d8-3e9021408041",
        content: "Add data comparison chart here.",
        coordinationX: 380,
        coordinationY: 510,
        pageNumber: 2
    },
    {
        _id: "210c92e8-e470-4766-b587-c03b13844acc",
        content: "Mention dataset limitations.",
        coordinationX: 480,
        coordinationY: 670,
        pageNumber: 4
    }
]

db.notes.insertMany(notes)

// ==================================================================================================================

// === 1️⃣ Lấy danh sách paper & tag hiện có ===
const papers = db.papers.find().toArray()
const tags = db.tags.find().toArray()
const citations = db.citations.find().toArray()

// === 2️⃣ Gán tags vào papers theo chủ đề (dựa trên keywords) ===
papers.forEach(paper => {
    const matchedTags = tags.filter(tag => {
        return paper.keywords.some(k =>
            k.toLowerCase().includes(tag.name.toLowerCase().split(" ")[0])
        )
    })

    const tagIds = matchedTags.map(t => t._id)

    // update paper
    db.papers.updateOne(
        {_id: paper._id},
        {$set: {tagIds: tagIds}}
    )

    // update tag.papersIds
    matchedTags.forEach(tag => {
        db.tags.updateOne(
            {_id: tag._id},
            {$addToSet: {paperIds: paper._id}}
        )
    })
})

print("✅ Linked tags <-> papers")

// === 3️⃣ Gán citations cho papers (ngẫu nhiên, 1–3 mỗi paper) ===
papers.forEach(paper => {
    const randomCitations = citations
        .sort(() => 0.5 - Math.random())
        .slice(0, Math.floor(Math.random() * 3) + 1)
        .map(c => c._id)

    db.papers.updateOne(
        {_id: paper._id},
        {$set: {citationIds: randomCitations}}
    )

    // đồng thời cập nhật citations.paperIds
    randomCitations.forEach(cid => {
        db.citations.updateOne(
            {_id: cid},
            {$addToSet: {paperIds: paper._id}}
        )
    })
})

print("✅ Linked citations <-> papers")

// === 4️⃣ Kiểm tra kết quả mẫu ===
print("\n📄 Example linked paper:")
printjson(db.papers.findOne({}, {title: 1, tagIds: 1, citationIds: 1}))

print("\n🏷️ Example linked tag:")
printjson(db.tags.findOne({}, {name: 1, paperIds: 1}))

print("\n📚 Example linked citation:")
printjson(db.citations.findOne({}, {title: 1, paperIds: 1}))

