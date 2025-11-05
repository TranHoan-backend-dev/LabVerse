import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { BookOpen, Plus, Users } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

const Collections = () => {
  const { user, signOut } = useAuth();
  const queryClient = useQueryClient();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [newCollection, setNewCollection] = useState({ name: '', description: '' });

  const { data: collections, isLoading } = useQuery({
    queryKey: ['collections', user?.id],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('collections')
        .select(`
          *,
          collection_members!inner(role),
          collection_papers(count)
        `)
        .order('created_at', { ascending: false });
      
      if (error) throw error;
      return data;
    },
    enabled: !!user,
  });

  const createMutation = useMutation({
    mutationFn: async () => {
      const { data: collection, error: collectionError } = await supabase
        .from('collections')
        .insert({
          name: newCollection.name,
          description: newCollection.description,
          created_by: user?.id,
        })
        .select()
        .single();
      
      if (collectionError) throw collectionError;

      // Add creator as owner
      const { error: memberError } = await supabase
        .from('collection_members')
        .insert({
          collection_id: collection.id,
          user_id: user?.id,
          role: 'owner',
        });
      
      if (memberError) throw memberError;
    },
    onSuccess: () => {
      toast.success('Collection created successfully');
      queryClient.invalidateQueries({ queryKey: ['collections'] });
      setIsCreateOpen(false);
      setNewCollection({ name: '', description: '' });
    },
    onError: () => {
      toast.error('Failed to create collection');
    },
  });

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            <Link to="/dashboard" className="flex items-center gap-2 transition-smooth hover:opacity-80">
              <BookOpen className="h-6 w-6 text-primary" />
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
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold mb-2">Team Collections</h1>
              <p className="text-muted-foreground">
                Collaborate on research with your team
              </p>
            </div>
            
            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
              <DialogTrigger asChild>
                <Button size="lg" className="sm:w-auto w-full">
                  <Plus className="h-5 w-5 mr-2" />
                  Create Collection
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Create New Collection</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Collection Name</Label>
                    <Input
                      id="name"
                      value={newCollection.name}
                      onChange={(e) => setNewCollection({ ...newCollection, name: e.target.value })}
                      placeholder="e.g., Machine Learning Papers"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                      id="description"
                      value={newCollection.description}
                      onChange={(e) => setNewCollection({ ...newCollection, description: e.target.value })}
                      placeholder="What is this collection about?"
                      rows={3}
                    />
                  </div>
                  <Button
                    onClick={() => createMutation.mutate()}
                    disabled={!newCollection.name || createMutation.isPending}
                    className="w-full"
                  >
                    {createMutation.isPending ? 'Creating...' : 'Create Collection'}
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          {isLoading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
          ) : collections && collections.length > 0 ? (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {collections.map((collection: any) => (
                <Card key={collection.id} className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
                  <CardHeader>
                    <CardTitle className="flex items-start justify-between">
                      <span className="line-clamp-2">{collection.name}</span>
                      <Users className="h-5 w-5 text-muted-foreground flex-shrink-0 ml-2" />
                    </CardTitle>
                    {collection.description && (
                      <CardDescription className="line-clamp-2">
                        {collection.description}
                      </CardDescription>
                    )}
                  </CardHeader>
                  <CardContent>
                    <div className="text-sm text-muted-foreground">
                      <p>Role: {collection.collection_members[0]?.role || 'member'}</p>
                      <p>Papers: {collection.collection_papers?.[0]?.count || 0}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <Card className="text-center py-12">
              <CardContent>
                <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                <h3 className="text-lg font-semibold mb-2">No collections yet</h3>
                <p className="text-muted-foreground mb-4">
                  Create your first collection to start collaborating with your team
                </p>
                <Button onClick={() => setIsCreateOpen(true)}>
                  <Plus className="h-4 w-4 mr-2" />
                  Create Collection
                </Button>
              </CardContent>
            </Card>
          )}
        </div>
      </main>
    </div>
  );
};

export default Collections;
