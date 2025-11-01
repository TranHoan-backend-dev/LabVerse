import React from "react";

import Navbar from "@/components/ui/navbar";
import TabNavigation from "@/app/(main)/feed/components/TabNavigation";
import SearchBar from "@/components/ui/SearchBar";
import StatsCard from "@/app/(main)/feed/components/StatsCard";
import ActivityList from "@/app/(main)/feed/components/StatsCard";
import FilterTabs from "@/app/(main)/feed/components/FilterTabs";

const Page = () => {
  return (
    <>
      <Navbar />
      <TabNavigation />
      <SearchBar />
      <div className="grid grid-cols-3 gap-2 my-4">
        <StatsCard color="blue" label="Papers" value={247} />
        <StatsCard color="green" label="Collections" value={18} />
        <StatsCard color="orange" label="Team Projects" value={5} />
      </div>
      <FilterTabs />
      <h2 className="font-bold my-4">Recent Activity</h2>
      <ActivityList />
    </>
  );
};

export default Page;
