import {Link} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {BookOpen, Sparkles, TrendingUp, Lightbulb} from "lucide-react";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";

const Discover = () => {
    const {signOut} = useAuth();

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
            ],
        },
    ];

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
                <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
                    <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                        <div className="flex h-16 items-center justify-between">
                            <Link to="/dashboard"
                                  className="flex items-center gap-2 transition-smooth hover:opacity-80">
                                <BookOpen className="h-6 w-6 text-primary"/>
                                <span className="text-xl font-bold text-gradient">LabVerse</span>
                            </Link>

                            <div className="flex items-center gap-4">
                                <Link to="/profile">
                                    <Button variant="outline" size="sm">Profile</Button>
                                </Link>
                                <Button variant="ghost" size="sm" onClick={() => signOut()}>
                                    Sign Out
                                </Button>
                            </div>
                        </div>
                    </div>
                </header>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-8">
                        <div>
                            <h1 className="text-3xl font-bold mb-2">Discover</h1>
                            <p className="text-muted-foreground">
                                Explore personalized paper recommendations based on your interests
                            </p>
                        </div>

                        <div className="space-y-8">
                            {recommendations.map((section, idx) => {
                                const Icon = section.icon;
                                return (
                                    <div key={idx}>
                                        <div className="flex items-center gap-2 mb-4">
                                            <Icon className="h-5 w-5 text-primary"/>
                                            <h2 className="text-xl font-semibold">{section.category}</h2>
                                        </div>
                                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                                            {section.papers.map((paper, paperIdx) => (
                                                <Card key={paperIdx}
                                                      className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
                                                    <CardHeader>
                                                        <CardTitle className="text-base line-clamp-2">
                                                            {paper.title}
                                                        </CardTitle>
                                                        <CardDescription className="line-clamp-1">
                                                            {paper.authors.join(', ')}
                                                        </CardDescription>
                                                    </CardHeader>
                                                    <CardContent>
                                                        <div className="text-sm text-muted-foreground mb-4">
                                                            <p>{paper.journal}</p>
                                                            <p>{paper.year}</p>
                                                        </div>
                                                        <Button size="sm" className="w-full">
                                                            Add to Library
                                                        </Button>
                                                    </CardContent>
                                                </Card>
                                            ))}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        <Card className="bg-primary/5 border-primary/20">
                            <CardContent className="py-6">
                                <div className="flex items-start gap-4">
                                    <Sparkles className="h-6 w-6 text-primary flex-shrink-0 mt-1"/>
                                    <div>
                                        <h3 className="font-semibold mb-2">Personalized Recommendations</h3>
                                        <p className="text-sm text-muted-foreground">
                                            Our AI analyzes your library and reading patterns to suggest relevant
                                            papers.
                                            The more you use LabVerse, the better our recommendations become!
                                        </p>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </main>
            </div>
        </>
    );
};

export default Discover;
