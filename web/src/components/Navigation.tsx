import { Button } from "@/components/ui/button";
import { BookOpen, Menu } from "lucide-react";
import { Link } from "react-router-dom";
import { useState } from "react";

const Navigation = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <nav className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <Link to="/" className="flex items-center gap-2 transition-smooth hover:opacity-80">
            <BookOpen className="h-6 w-6 text-primary" />
            <span className="text-xl font-bold text-gradient">LabVerse</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-8">
            <Link to="/#features" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth">
              Features
            </Link>
            <Link to="/#about" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth">
              About
            </Link>
            <Link to="/auth" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth">
              Sign In
            </Link>
            <Link to="/auth">
              <Button className="transition-smooth">Get Started</Button>
            </Link>
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden p-2 rounded-lg hover:bg-muted transition-smooth"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            <Menu className="h-6 w-6" />
          </button>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 space-y-3 border-t border-border">
            <Link
              to="/#features"
              className="block py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth"
              onClick={() => setMobileMenuOpen(false)}
            >
              Features
            </Link>
            <Link
              to="/#about"
              className="block py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth"
              onClick={() => setMobileMenuOpen(false)}
            >
              About
            </Link>
            <Link
              to="/auth"
              className="block py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-smooth"
              onClick={() => setMobileMenuOpen(false)}
            >
              Sign In
            </Link>
            <Link to="/auth" onClick={() => setMobileMenuOpen(false)}>
              <Button className="w-full">Get Started</Button>
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navigation;
