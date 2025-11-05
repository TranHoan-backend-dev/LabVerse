-- Create profiles table for user information
CREATE TABLE public.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  full_name TEXT NOT NULL,
  affiliation TEXT,
  avatar_url TEXT,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Policies for profiles
CREATE POLICY "Users can view all profiles"
  ON public.profiles FOR SELECT
  USING (true);

CREATE POLICY "Users can update own profile"
  ON public.profiles FOR UPDATE
  USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
  ON public.profiles FOR INSERT
  WITH CHECK (auth.uid() = id);

-- Create papers table
CREATE TABLE public.papers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  title TEXT NOT NULL,
  authors TEXT[] NOT NULL,
  journal TEXT,
  year INTEGER,
  doi TEXT,
  pdf_url TEXT,
  abstract TEXT,
  keywords TEXT[],
  status TEXT DEFAULT 'To Read' CHECK (status IN ('To Read', 'Reading', 'Finished')),
  priority TEXT DEFAULT 'Medium' CHECK (priority IN ('High', 'Medium', 'Low')),
  is_favorite BOOLEAN DEFAULT false,
  last_read_page INTEGER DEFAULT 0,
  total_pages INTEGER,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

ALTER TABLE public.papers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own papers"
  ON public.papers FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can create own papers"
  ON public.papers FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own papers"
  ON public.papers FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own papers"
  ON public.papers FOR DELETE
  USING (auth.uid() = user_id);

-- Create collections table
CREATE TABLE public.collections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  description TEXT,
  created_by UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

ALTER TABLE public.collections ENABLE ROW LEVEL SECURITY;

-- Create collection_members table
CREATE TABLE public.collection_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  collection_id UUID REFERENCES public.collections(id) ON DELETE CASCADE NOT NULL,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  role TEXT DEFAULT 'member' CHECK (role IN ('owner', 'member')),
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  UNIQUE(collection_id, user_id)
);

ALTER TABLE public.collection_members ENABLE ROW LEVEL SECURITY;

-- Policies for collections
CREATE POLICY "Users can view collections they are members of"
  ON public.collections FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collections.id AND user_id = auth.uid()
    )
  );

CREATE POLICY "Users can create collections"
  ON public.collections FOR INSERT
  WITH CHECK (auth.uid() = created_by);

CREATE POLICY "Collection owners can update"
  ON public.collections FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collections.id 
      AND user_id = auth.uid() 
      AND role = 'owner'
    )
  );

CREATE POLICY "Collection owners can delete"
  ON public.collections FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collections.id 
      AND user_id = auth.uid() 
      AND role = 'owner'
    )
  );

-- Policies for collection_members
CREATE POLICY "Users can view members of their collections"
  ON public.collection_members FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members cm
      WHERE cm.collection_id = collection_members.collection_id 
      AND cm.user_id = auth.uid()
    )
  );

CREATE POLICY "Collection owners can manage members"
  ON public.collection_members FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collection_members.collection_id 
      AND user_id = auth.uid() 
      AND role = 'owner'
    )
  );

-- Create collection_papers junction table
CREATE TABLE public.collection_papers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  collection_id UUID REFERENCES public.collections(id) ON DELETE CASCADE NOT NULL,
  paper_id UUID REFERENCES public.papers(id) ON DELETE CASCADE NOT NULL,
  added_by UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  UNIQUE(collection_id, paper_id)
);

ALTER TABLE public.collection_papers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Members can view collection papers"
  ON public.collection_papers FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collection_papers.collection_id 
      AND user_id = auth.uid()
    )
  );

CREATE POLICY "Members can add papers to collections"
  ON public.collection_papers FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.collection_members
      WHERE collection_id = collection_papers.collection_id 
      AND user_id = auth.uid()
    ) AND auth.uid() = added_by
  );

-- Create reading_lists table
CREATE TABLE public.reading_lists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

ALTER TABLE public.reading_lists ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own reading lists"
  ON public.reading_lists FOR ALL
  USING (auth.uid() = user_id);

-- Create reading_list_papers junction table
CREATE TABLE public.reading_list_papers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  reading_list_id UUID REFERENCES public.reading_lists(id) ON DELETE CASCADE NOT NULL,
  paper_id UUID REFERENCES public.papers(id) ON DELETE CASCADE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
  UNIQUE(reading_list_id, paper_id)
);

ALTER TABLE public.reading_list_papers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own reading list papers"
  ON public.reading_list_papers FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.reading_lists
      WHERE id = reading_list_papers.reading_list_id 
      AND user_id = auth.uid()
    )
  );

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER set_updated_at
  BEFORE UPDATE ON public.profiles
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER set_updated_at
  BEFORE UPDATE ON public.papers
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER set_updated_at
  BEFORE UPDATE ON public.collections
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER set_updated_at
  BEFORE UPDATE ON public.reading_lists
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_updated_at();

-- Create function to handle new user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, affiliation)
  VALUES (
    NEW.id,
    COALESCE(NEW.raw_user_meta_data->>'full_name', ''),
    COALESCE(NEW.raw_user_meta_data->>'affiliation', '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on signup
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();