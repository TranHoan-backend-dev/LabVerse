import Navigation from "@/components/Navigation";
import FeatureCard from "@/components/FeatureCard";
import {Button} from "@/components/ui/button";
import {
    BookOpen,
    Users,
    FileText,
    Search,
    MessageSquare,
    Share2,
    Highlighter,
    Database,
    Bell
} from "lucide-react";
import {Link} from "react-router-dom";

const Index = () => {
    const features = [
        {
            icon: BookOpen,
            title: "Personal Library",
            description: "Organize all your research papers in one centralized, searchable library with advanced filtering."
        },
        {
            icon: Users,
            title: "Team Collaboration",
            description: "Share collections with your lab team, assign reading priorities, and track progress together."
        },
        {
            icon: Highlighter,
            title: "Smart Annotations",
            description: "Highlight, annotate, and add notes directly to PDFs. Export and share your insights seamlessly."
        },
        {
            icon: FileText,
            title: "Citation Management",
            description: "Auto-extract citations and export in multiple formats (APA, MLA, BibTeX) for your manuscripts."
        },
        {
            icon: Search,
            title: "Advanced Search",
            description: "Find papers instantly with full-text search across titles, authors, keywords, and annotations."
        },
        {
            icon: Database,
            title: "BibTeX Import",
            description: "Import your existing libraries from Zotero, Mendeley, or any BibTeX source with one click."
        },
        {
            icon: MessageSquare,
            title: "Discussion Threads",
            description: "Start conversations on papers with your team. Create journal clubs and reading lists."
        },
        {
            icon: Bell,
            title: "Smart Alerts",
            description: "Get notified about new publications matching your research interests and saved searches."
        },
        {
            icon: Share2,
            title: "Reading Lists",
            description: "Curate themed collections for projects or courses. Share with collaborators or students."
        }
    ];

    return (
        <div className="min-h-screen bg-background">
            <Navigation/>

            {/* Hero Section */}
            <section className="relative overflow-hidden bg-gradient-to-br from-primary/5 via-background to-accent/5">
                <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-20 lg:py-32">
                    <div className="max-w-4xl mx-auto text-center space-y-8">
                        <div className="space-y-4">
                            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight">
                                Your Research Papers,
                                <br/>
                                <span className="text-gradient">Perfectly Organized</span>
                            </h1>
                            <p className="text-xl text-muted-foreground max-w-2xl mx-auto leading-relaxed">
                                LabVerse is the research paper management system designed for modern labs and research
                                teams.
                                Discover, annotate, collaborate, and never lose track of important literature again.
                            </p>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-4 justify-center">
                            <Link to="/auth">
                                <Button size="lg" className="text-lg px-8 w-full sm:w-auto">
                                    Get Started Free
                                </Button>
                            </Link>
                            <Link to="/dashboard">
                                <Button size="lg" variant="outline" className="text-lg px-8 w-full sm:w-auto">
                                    View Demo
                                </Button>
                            </Link>
                        </div>

                        <div className="pt-8 flex items-center justify-center gap-8 text-sm text-muted-foreground">
                            <div className="flex items-center gap-2">
                                <Users className="h-4 w-4"/>
                                <span>For Research Teams</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <BookOpen className="h-4 w-4"/>
                                <span>Unlimited Papers</span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <section id="features" className="py-20 lg:py-32">
                <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="text-center space-y-4 mb-16">
                        <h2 className="text-3xl sm:text-4xl font-bold">
                            Everything You Need for Research
                        </h2>
                        <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
                            From discovery to publication, LabVerse supports your entire research workflow
                        </p>
                    </div>

                    <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
                        {features.map((feature, index) => (
                            <FeatureCard key={index} {...feature} />
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="py-20 lg:py-32 bg-gradient-to-br from-primary/5 via-background to-accent/5">
                <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="max-w-3xl mx-auto text-center space-y-8">
                        <h2 className="text-3xl sm:text-4xl font-bold">
                            Ready to Transform Your Research Workflow?
                        </h2>
                        <p className="text-xl text-muted-foreground">
                            Join research teams from Hanoi to Ho Chi Minh City and around the world
                        </p>
                        <Link to="/auth">
                            <Button size="lg" className="text-lg px-8">
                                Start Your Free Account
                            </Button>
                        </Link>
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="border-t border-border py-12">
                <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                        <div className="flex items-center gap-2">
                            <BookOpen className="h-5 w-5 text-primary"/>
                            <span className="font-semibold text-gradient">LabVerse</span>
                        </div>
                        <p className="text-sm text-muted-foreground">
                            © 2025 LabVerse. Built for researchers, by researchers.
                        </p>
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default Index;
