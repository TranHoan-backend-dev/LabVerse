"use client";

import React from "react";

interface ActivityItemProps {
  title: string;
  authors: string;
  tags: string[];
}

const ActivityItem = ({ title, authors, tags }: ActivityItemProps) => {
  return (
    <div className="p-4 border rounded-lg mb-2">
      <h3 className="font-semibold">{title}</h3>
      <p className="text-sm text-gray-500">{authors}</p>
      <div className="flex gap-2 mt-2">
        {tags.map((tag) => (
          <span key={tag} className="px-2 py-1 text-xs bg-blue-100 rounded">
            {tag}
          </span>
        ))}
      </div>
    </div>
  );
};

export default ActivityItem;
