import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FileText, Calendar, Users, ExternalLink } from "lucide-react";

interface PaperCardProps {
  title: string;
  authors: string[];
  journal?: string | null;
  year?: number | null;
  status?: string | null;
  priority?: string | null;
}

const PaperCard = ({ title, authors, journal, year, status, priority }: PaperCardProps) => {
  const statusColors: Record<string, string> = {
    "To Read": "bg-muted text-muted-foreground",
    "Reading": "bg-accent/10 text-accent",
    "Finished": "bg-primary/10 text-primary",
  };

  const priorityColors: Record<string, string> = {
    "High": "bg-destructive/10 text-destructive",
    "Medium": "bg-accent/10 text-accent",
    "Low": "bg-muted text-muted-foreground",
  };

  return (
    <Card className="hover:shadow-custom-md transition-smooth group">
      <CardContent className="p-6 space-y-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex-1 space-y-3">
            <h3 className="font-semibold text-lg leading-tight group-hover:text-primary transition-smooth">
              {title}
            </h3>
            
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Users className="h-4 w-4" />
              <span className="line-clamp-1">{authors.join(", ")}</span>
            </div>

            <div className="flex items-center gap-4 text-sm text-muted-foreground">
              <div className="flex items-center gap-1">
                <FileText className="h-4 w-4" />
                <span>{journal}</span>
              </div>
              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                <span>{year}</span>
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-2">
            {status && (
              <Badge variant="secondary" className={statusColors[status] || "bg-muted text-muted-foreground"}>
                {status}
              </Badge>
            )}
            {priority && (
              <Badge variant="outline" className={priorityColors[priority] || "bg-muted text-muted-foreground"}>
                {priority}
              </Badge>
            )}
          </div>
        </div>
      </CardContent>

      <CardFooter className="p-6 pt-0 flex gap-2">
        <Button variant="outline" size="sm" className="flex-1">
          View Details
        </Button>
        <Button size="sm" className="flex-1">
          <ExternalLink className="h-4 w-4 mr-2" />
          Open
        </Button>
      </CardFooter>
    </Card>
  );
};

export default PaperCard;
