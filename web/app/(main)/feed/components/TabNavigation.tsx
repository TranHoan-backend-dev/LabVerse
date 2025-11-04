"use client";

import React from "react";
import { Tab, Tabs } from "@heroui/react";

const TabNavigation = () => {
  return (
    <Tabs aria-label="Navigation Tabs" variant="underlined">
      <Tab key="discover" title="Discover" />
      <Tab key="saved" title="Saved" />
      <Tab key="teams" title="Teams" />
    </Tabs>
  );
};

export default TabNavigation;
