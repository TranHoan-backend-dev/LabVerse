"use client";

import React from "react";

interface StatsCardProps {
  label: string;
  value: number;
  color: string;
}

const StatsCard = ({ label, value, color }: StatsCardProps) => {
  return (
    <div
      className={`flex flex-col items-center p-4 rounded-lg bg-${color}-100`}
    >
      <span className="text-xl font-bold">{value}</span>
      <span className="text-sm">{label}</span>
    </div>
  );
};

export default StatsCard;
