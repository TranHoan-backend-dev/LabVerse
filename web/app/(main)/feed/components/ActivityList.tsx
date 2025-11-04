"use client";

import React from 'react';

const ActivityList = () => {

    const activities = [
        {
            title: "Deep Learning Approaches for Protein Structure Prediction",
            authors: "Smith A., Johnson B.",
            tags: ["Continue", "In Progress"]
        },
        {
            title: "CRISPR-Cas9 Gene Editing: Recent Advances",
            authors: "Chen L., Wang M.",
            tags: ["Read Again"]
        }
    ];

    return (
        <div>
            {activities.map((item, idx) => (
                <ActivityList key={idx} {...item} />
            ))}
        </div>
    );
};

export default ActivityList;