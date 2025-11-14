import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { FileText, Calendar, Users, ExternalLink, BookOpen } from "lucide-react";
import { Link } from "react-router-dom";

interface RecentlyReadCardProps {
  id: string;
  title: string;
  authors: string;
  journal?: string | null;
  publicationYear?: number | null;
  last_read_page?: number | null;
  total_pages?: number | null;
  progress?: number | null;
}

const RecentlyReadCard = ({
  id,
  title,
  authors,
  journal,
  publicationYear: year,
  last_read_page,
  total_pages,
  progress
}: RecentlyReadCardProps) => {
  let progressPercentage = 0;
  let displayText = "Chưa bắt đầu";

  if (progress !== null && progress !== undefined && !isNaN(progress) && progress >= 0) {
    progressPercentage = Math.min(100, Math.max(0, progress));
    displayText = `${Math.round(progressPercentage)}%`;
  } else if (total_pages && total_pages > 0 && last_read_page !== null && last_read_page !== undefined && last_read_page > 0) {
    const calculatedProgress = (last_read_page / total_pages) * 100;
    progressPercentage = Math.min(100, Math.max(0, calculatedProgress));
    displayText = `${Math.round(progressPercentage)}%`;
  } else if (last_read_page !== null && last_read_page !== undefined && last_read_page > 0) {
    displayText = `Trang ${last_read_page}`;
    progressPercentage = 10;
  }

  return (
    <Card className="hover:shadow-lg transition-all group h-full flex flex-col border-l-4 border-l-primary">
      <CardContent className="p-6 space-y-4 flex-1 overflow-hidden">
        <div className="space-y-3">
          <h3 className="font-semibold text-lg leading-tight group-hover:text-primary transition-colors line-clamp-2">
            {title}
          </h3>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Users className="h-4 w-4 flex-shrink-0" />
            <span className="line-clamp-1">{authors}</span>
          </div>
          <div className="flex items-center gap-4 text-sm text-muted-foreground">
            {journal && (
              <div className="flex items-center gap-1">
                <FileText className="h-4 w-4" />
                <span className="line-clamp-1">{journal}</span>
              </div>
            )}
            {year && (
              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                <span>{year}</span>
              </div>
            )}
          </div>
        </div>
        <div className="space-y-2 pt-2 border-t">
          <div className="flex items-center justify-between text-sm">
            <span className="font-medium text-foreground flex items-center gap-2">
              <BookOpen className="h-4 w-4" />
              Reading Progress
            </span>
            <span className="font-semibold text-primary">{displayText}</span>
          </div>
          <Progress value={progressPercentage} className="h-3" />
          {last_read_page !== null && last_read_page !== undefined && last_read_page > 0 && (
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span>Page {last_read_page}{total_pages ? ` / ${total_pages}` : ''}</span>
              {progressPercentage > 0 && (
                <span>{Math.round(progressPercentage)}% completed</span>
              )}
            </div>
          )}
        </div>
      </CardContent>
      <CardFooter className="p-6 pt-0">
        <Button size="sm" className="w-full" asChild>
          <Link to={`/paper/${id}`}>
            <ExternalLink className="h-4 w-4 mr-2" />
            Continue Reading
          </Link>
        </Button>
      </CardFooter>
    </Card>
  );
};

export default RecentlyReadCard;

