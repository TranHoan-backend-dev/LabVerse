import React from 'react';
import {Input} from "@heroui/input";

const SearchBar = () => {
    return (

        <Input
            placeholder="Search papers, authors, keywords..."
            className="my-4"
            variant="bordered"
        />

    );
};

export default SearchBar;