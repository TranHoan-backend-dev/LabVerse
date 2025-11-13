import React from 'react';
import {Button} from '@/components/ui/button';

type Props = {
    page: number;
    totalPages: number;
    onPreviousPage: () => void;
    onNextPage: () => void;
};

const PaginationControls: React.FC<Props> = ({
    page,
    totalPages,
    onPreviousPage,
    onNextPage,
}) => {
    if (totalPages <= 1) return null;

    return (
        <div className="flex justify-center gap-2">
            <Button
                variant="outline"
                onClick={onPreviousPage}
                disabled={page === 0}
            >
                Previous
            </Button>
            <span className="flex items-center px-4">
                Page {page + 1} of {totalPages}
            </span>
            <Button
                variant="outline"
                onClick={onNextPage}
                disabled={page >= totalPages - 1}
            >
                Next
            </Button>
        </div>
    );
};

export default PaginationControls;
