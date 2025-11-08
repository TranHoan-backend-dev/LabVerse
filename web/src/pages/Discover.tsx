import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {BookOpen, Sparkles, TrendingUp, Lightbulb} from "lucide-react";
import {Helmet} from "react-helmet-async";
import {useState} from "react";
import Header from "@/pages/Header.tsx";

const pageSize = 2; // số phần tử mỗi trang cho từng section

const recommendations = [
    {
        category: "Trending in Your Field",
        icon: TrendingUp,
        papers: [
            {
                title: "Advanced Neural Architecture Search Methods",
                authors: ["Zhang, L.", "Wang, M."],
                journal: "Nature Machine Intelligence",
                year: 2024,
            },
            {
                title: "Quantum Computing for Drug Discovery",
                authors: ["Smith, J.", "Brown, K."],
                journal: "Science",
                year: 2024,
            },
            {
                title: "Meta Reinforcement Learning in Robotics",
                authors: ["Lee, J.", "Khan, A."],
                journal: "Robotics Journal",
                year: 2023,
            },
        ],
    },
    {
        category: "Recommended for You",
        icon: Sparkles,
        papers: [
            {
                title: "Climate Modeling with Machine Learning",
                authors: ["Garcia, M.", "Lee, S."],
                journal: "Nature Climate Change",
                year: 2024,
            },
            {
                title: "Deep Learning for Genomics",
                authors: ["Kim, H.", "Tran, P."],
                journal: "Genome Research",
                year: 2023,
            }
        ],
    },
    {
        category: "Popular This Week",
        icon: Lightbulb,
        papers: [
            {
                title: "Breakthrough in Cancer Immunotherapy",
                authors: ["Wilson, A.", "Taylor, R."],
                journal: "Cell",
                year: 2024,
            },
            {
                title: "AI-Assisted Vaccine Design",
                authors: ["Nguyen, T.", "Yamada, S."],
                journal: "Medical AI Journal",
                year: 2024,
            },
            {
                title: "Neural Signal Decoding for Prosthetics",
                authors: ["Chen, X.", "Oliver, P."],
                journal: "BioTech Advances",
                year: 2023,
            },
        ],
    },
];

const Discover = () => {

    const [pages, setPages] = useState({0: 1, 1: 1, 2: 1});

    const handleNext = (idx, total) => {
        const maxPages = Math.max(1, Math.ceil(total / pageSize));
        setPages(prev => ({...prev, [idx]: Math.min(prev[idx] + 1, maxPages)}));
    };

    const handlePrev = (idx) => {
        setPages(prev => ({...prev, [idx]: Math.max(prev[idx] - 1, 1)}));
    };

    return (
        <>
            <Helmet>
                <title>Explore — LabVerse</title>
                <meta
                    name="description"
                    content="Discover trending research papers, personalized recommendations, and the latest insights in your field on LabVerse."
                />
                <meta property="og:title" content="Explore — LabVerse"/>
                <meta
                    property="og:description"
                    content="Explore trending and personalized scientific papers curated just for you on LabVerse."
                />
                <meta property="og:image" content="/og-discover.png"/>
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://labverse.app/discover"/>
                <meta property="og:site_name" content="LabVerse"/>
                <link rel="canonical" href="https://labverse.app/discover"/>
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header/>
                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-12">
                    <div>
                        <h1 className="text-3xl font-bold mb-2">Discover</h1>
                        <p className="text-muted-foreground">
                            Explore personalized paper recommendations based on your interests
                        </p>
                    </div>
                    {recommendations.map((section, idx) => {
                        const Icon = section.icon;
                        const start = (pages[idx] - 1) * pageSize;
                        const paginated = section.papers.slice(start, start + pageSize);
                        const totalPages = Math.max(1, Math.ceil(section.papers.length / pageSize));

                        return (
                            <div key={idx} className="space-y-4">
                                <div className="flex items-center gap-2 mb-2">
                                    <Icon className="h-5 w-5 text-primary"/>
                                    <h2 className="text-xl font-semibold">{section.category}</h2>
                                </div>

                                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                                    {paginated.map((paper, i) => (
                                        <Card key={i}
                                              className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
                                            <CardHeader>
                                                <CardTitle className="text-base line-clamp-2">{paper.title}</CardTitle>
                                                <CardDescription
                                                    className="line-clamp-1">{paper.authors.join(', ')}</CardDescription>
                                            </CardHeader>
                                            <CardContent>
                                                <div className="text-sm text-muted-foreground mb-4">
                                                    <p>{paper.journal}</p>
                                                    <p>{paper.year}</p>
                                                </div>
                                                <Button size="sm" className="w-full">Add to Library</Button>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </div>

                                <div className="flex items-center justify-center gap-2">
                                    <Button variant="outline" size="sm" disabled={pages[idx] === 1}
                                            onClick={() => handlePrev(idx)}>Previous</Button>
                                    <span className="text-sm">{pages[idx]} / {totalPages}</span>
                                    <Button variant="outline" size="sm" disabled={pages[idx] === totalPages}
                                            onClick={() => handleNext(idx, section.papers.length)}>Next</Button>
                                </div>
                            </div>
                        );
                    })}
                </main>
            </div>
        </>
    );
};

export default Discover;