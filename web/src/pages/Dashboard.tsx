import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Search, Plus, Filter, BookOpen, Users, BookMarked, Compass } from "lucide-react";
import PaperCard from "@/components/PaperCard";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "sonner";

const Dashboard = () => {
  const { user, signOut } = useAuth();
  const queryClient = useQueryClient();
  const [isImportOpen, setIsImportOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [newPaper, setNewPaper] = useState({
    title: '',
    authors: '',
    journal: '',
    year: new Date().getFullYear(),
    abstract: '',
    doi: '',
  });

  const { data: papers, isLoading } = useQuery({
    queryKey: ['papers', user?.id, searchQuery],
    queryFn: async () => {
      let query = supabase
        .from('papers')
        .select('*')
        .eq('user_id', user?.id)
        .order('created_at', { ascending: false });

      if (searchQuery) {
        query = query.or(`title.ilike.%${searchQuery}%,authors.cs.{${searchQuery}}`);
      }
      
      const { data, error } = await query;
      if (error) throw error;
      return data;
    },
    enabled: !!user,
  });

  const importMutation = useMutation({
    mutationFn: async () => {
      const authorsArray = newPaper.authors.split(',').map(a => a.trim()).filter(a => a);
      
      const { error } = await supabase
        .from('papers')
        .insert({
          user_id: user?.id,
          title: newPaper.title,
          authors: authorsArray,
          journal: newPaper.journal || null,
          year: newPaper.year || null,
          abstract: newPaper.abstract || null,
          doi: newPaper.doi || null,
        });
      
      if (error) throw error;
    },
    onSuccess: () => {
      toast.success('Paper imported successfully');
      queryClient.invalidateQueries({ queryKey: ['papers'] });
      setIsImportOpen(false);
      setNewPaper({
        title: '',
        authors: '',
        journal: '',
        year: new Date().getFullYear(),
        abstract: '',
        doi: '',
      });
    },
    onError: () => {
      toast.error('Failed to import paper');
    },
  });

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            <Link to="/" className="flex items-center gap-2 transition-smooth hover:opacity-80">
              <BookOpen className="h-6 w-6 text-primary" />
              <span className="text-xl font-bold text-gradient">LabVerse</span>
            </Link>

            <nav className="hidden md:flex items-center gap-6">
              <Link to="/dashboard" className="text-sm font-medium text-primary">
                Library
              </Link>
              <Link to="/collections" className="text-sm font-medium hover:text-primary transition-smooth">
                Collections
              </Link>
              <Link to="/reading-lists" className="text-sm font-medium hover:text-primary transition-smooth">
                Reading Lists
              </Link>
              <Link to="/discover" className="text-sm font-medium hover:text-primary transition-smooth">
                Discover
              </Link>
            </nav>

            <div className="flex items-center gap-4">
              <Link to="/profile">
                <Button variant="outline" size="sm">
                  Profile
                </Button>
              </Link>
              <Button variant="ghost" size="sm" onClick={() => signOut()}>
                Sign Out
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Quick Navigation for Mobile */}
      <div className="md:hidden border-b border-border bg-background">
        <div className="container mx-auto px-4 py-3">
          <div className="flex gap-2 overflow-x-auto">
            <Link to="/collections">
              <Button variant="outline" size="sm" className="flex-shrink-0">
                <Users className="h-4 w-4 mr-1" />
                Collections
              </Button>
            </Link>
            <Link to="/reading-lists">
              <Button variant="outline" size="sm" className="flex-shrink-0">
                <BookMarked className="h-4 w-4 mr-1" />
                Lists
              </Button>
            </Link>
            <Link to="/discover">
              <Button variant="outline" size="sm" className="flex-shrink-0">
                <Compass className="h-4 w-4 mr-1" />
                Discover
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-8">
          {/* Page Header */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold mb-2">My Library</h1>
              <p className="text-muted-foreground">
                Manage and organize your research papers
              </p>
            </div>
            
            <Dialog open={isImportOpen} onOpenChange={setIsImportOpen}>
              <DialogTrigger asChild>
                <Button size="lg" className="sm:w-auto w-full">
                  <Plus className="h-5 w-5 mr-2" />
                  Import Paper
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                  <DialogTitle>Import New Paper</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="title">Title *</Label>
                    <Input
                      id="title"
                      value={newPaper.title}
                      onChange={(e) => setNewPaper({ ...newPaper, title: e.target.value })}
                      placeholder="Paper title"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="authors">Authors * (comma separated)</Label>
                    <Input
                      id="authors"
                      value={newPaper.authors}
                      onChange={(e) => setNewPaper({ ...newPaper, authors: e.target.value })}
                      placeholder="John Doe, Jane Smith"
                      required
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="journal">Journal</Label>
                      <Input
                        id="journal"
                        value={newPaper.journal}
                        onChange={(e) => setNewPaper({ ...newPaper, journal: e.target.value })}
                        placeholder="Journal name"
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="year">Year</Label>
                      <Input
                        id="year"
                        type="number"
                        value={newPaper.year}
                        onChange={(e) => setNewPaper({ ...newPaper, year: parseInt(e.target.value) })}
                        placeholder="2024"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="doi">DOI</Label>
                    <Input
                      id="doi"
                      value={newPaper.doi}
                      onChange={(e) => setNewPaper({ ...newPaper, doi: e.target.value })}
                      placeholder="10.1234/example"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="abstract">Abstract</Label>
                    <Textarea
                      id="abstract"
                      value={newPaper.abstract}
                      onChange={(e) => setNewPaper({ ...newPaper, abstract: e.target.value })}
                      placeholder="Paper abstract..."
                      rows={5}
                    />
                  </div>

                  <Button
                    onClick={() => importMutation.mutate()}
                    disabled={!newPaper.title || !newPaper.authors || importMutation.isPending}
                    className="w-full"
                  >
                    {importMutation.isPending ? 'Importing...' : 'Import Paper'}
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          {/* Search and Filters */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-custom-sm">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                <Input
                  placeholder="Search papers by title or author..."
                  className="pl-10"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <Button variant="outline">
                <Filter className="h-5 w-5 mr-2" />
                Filters
              </Button>
            </div>
          </div>

          {/* Papers Grid */}
          {isLoading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
          ) : papers && papers.length > 0 ? (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {papers.map((paper) => (
                <Link key={paper.id} to={`/paper/${paper.id}`}>
                  <PaperCard {...paper} />
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <BookOpen className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
              <h3 className="text-lg font-semibold mb-2">No papers yet</h3>
              <p className="text-muted-foreground mb-4">
                {searchQuery ? 'No papers found matching your search' : 'Import your first paper to get started'}
              </p>
              {!searchQuery && (
                <Button onClick={() => setIsImportOpen(true)}>
                  <Plus className="h-4 w-4 mr-2" />
                  Import Paper
                </Button>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
