"use client";

import React from 'react';
import {Tab, Tabs} from "@heroui/react";

const FilterTabs = () => {
    return (

        <Tabs
            aria-label="Filter Tabs"
            variant="solid"
            color="primary"
            className="my-4"
        >
            <Tab key="recently-added" title="Recently Added"/>
            <Tab key="recently-read" title="Recently Read"/>
            <Tab key="favorites" title="Favorites"/>
        </Tabs>

    );
};

export default FilterTabs;