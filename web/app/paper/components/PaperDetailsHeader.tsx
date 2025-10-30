"use client";

import React from "react";
import { Chip } from "@heroui/react";

import { Paper } from "@/types/paper.type";

const PaperDetailsHeader = ({ paper }: { paper: Paper }) => {
  return (
    <header className="mb-8">
      <h1 className="text-3xl font-bold text-gray-800 leading-tight">
        {paper.title}
      </h1>
      <p className="text-gray-600 mt-2">
        {paper.authors} • {paper.publicationYear} •{" "}
        <span className="italic">{paper.journal}</span>
      </p>
      <div className="mt-3 flex gap-2 flex-wrap">
        {paper.tags?.map((kw) => (
          <Chip key={kw} color="primary" size="sm" variant="flat">
            {kw}
          </Chip>
        ))}
      </div>
    </header>
  );
};

export default PaperDetailsHeader;
