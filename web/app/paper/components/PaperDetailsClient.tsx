"use client";

import React, { useState } from "react";
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Chip,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownTrigger,
  Tab,
  Tabs,
} from "@heroui/react";
import {
  HiOutlineBookOpen,
  HiOutlineChatBubbleBottomCenterText,
  HiOutlineLink,
} from "react-icons/hi2";
import { FiDownload, FiShare2 } from "react-icons/fi";

import { Paper } from "@/types/paper.type";
import PaperDetailsHeader from "@/app/paper/components/PaperDetailsHeader";

export default function PaperDetailsClient() {
  const [selectedTab, setSelectedTab] = useState<string>("overview");
  const paper: Paper = {
    title:
      "Machine Learning Approaches for Predictive Analytics in Healthcare Systems",
    authors: "Smith, J., Johnson, M., Brown, K.",
    publicationYear: 2024,
    journal: "Nature Medicine",
    tags: ["Machine Learning", "Healthcare"],
    doi: "10.1038/s41591-024-0234-5",
  };
  const [citationStyle, setCitationStyle] = useState<string>("APA");

  return (
    <div className="pt-20">
      {/* Paper Header */}
      <PaperDetailsHeader paper={paper} />

      {/* Tabs Section */}
      <Tabs
        aria-label="Paper Tabs"
        className="max-w-4xl"
        selectedKey={selectedTab}
        onSelectionChange={(key) => setSelectedTab(String(key))}
      >
        {/* Overview */}
        <Tab
          key="overview"
          title={
            <span className="flex items-center gap-2">
              <HiOutlineBookOpen size={16} /> Overview
            </span>
          }
        >
          <Card className="mt-4 border border-gray-200 shadow-sm">
            <CardHeader>
              <h3 className="text-lg font-semibold">Description</h3>
            </CardHeader>
            <CardBody>
              <p className="text-gray-700 text-sm leading-relaxed">
                {paper.authors} ({paper.publicationYear}). {paper.title}.{" "}
                <i>{paper.journal}</i>. doi:{paper.doi}
              </p>
            </CardBody>
          </Card>
        </Tab>

        {/* Citation */}
        <Tab
          key="citation"
          title={
            <span className="flex items-center gap-2">
              <HiOutlineChatBubbleBottomCenterText size={16} /> Citation
            </span>
          }
        >
          <Card className="mt-4 border border-gray-200 shadow-sm">
            <CardHeader className="flex justify-between items-center flex-wrap gap-3">
              <h3 className="text-lg font-semibold text-gray-800">
                Citation Information
              </h3>

              <Dropdown>
                <DropdownTrigger>
                  <Button size="sm" variant="flat">
                    {citationStyle}
                  </Button>
                </DropdownTrigger>
                <DropdownMenu onAction={(key) => setCitationStyle(String(key))}>
                  <DropdownItem key="APA">APA Style</DropdownItem>
                  <DropdownItem key="MLA">MLA Style</DropdownItem>
                  <DropdownItem key="BibTeX">BibTeX Style</DropdownItem>
                </DropdownMenu>
              </Dropdown>
            </CardHeader>

            <CardBody className="space-y-6">
              <div>
                <h4 className="font-semibold text-gray-800 mb-3">
                  Paper Metadata
                </h4>

                <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-sm text-gray-700 leading-relaxed font-medium">
                  {citationStyle === "APA" && (
                    <p>
                      {paper.authors} ({paper.publicationYear}). {paper.title}.{" "}
                      <i>{paper.journal}</i>. doi:{paper.doi}
                    </p>
                  )}

                  {citationStyle === "MLA" && (
                    <p>
                      {paper.authors}. &#34;{paper.title}.&#34;{" "}
                      <i>{paper.journal}</i>, {paper.publicationYear}. doi:
                      {paper.doi}
                    </p>
                  )}

                  {citationStyle === "BibTeX" && (
                    <pre className="whitespace-pre-wrap text-xs">
                      {`@article{${paper.authors.split(",")[0].toLowerCase()}${paper.publicationYear},
                        title={${paper.title}},
                        author={${paper.authors}},
                        journal={${paper.journal}},
                        year={${paper.publicationYear}},
                        doi={${paper.doi}}
                      }`}
                    </pre>
                  )}
                </div>

                <div className="mt-4 flex gap-2 flex-wrap">
                  <Button
                    size="sm"
                    variant="flat"
                    onPress={() => navigator.clipboard.writeText(paper.doi)}
                  >
                    Copy DOI
                  </Button>
                </div>
              </div>

              <hr className="border-gray-200" />

              {/* Copy & Export Section */}
              <div className="flex flex-col md:flex-row justify-between gap-6">
                <div>
                  <h4 className="font-semibold text-gray-800 mb-2">
                    Quick Copy Formats
                  </h4>
                  <div className="flex gap-3 flex-wrap">
                    <Button
                      color="warning"
                      size="sm"
                      variant="flat"
                      onPress={() => setCitationStyle("APA")}
                    >
                      APA Style
                    </Button>
                    <Button
                      color="success"
                      size="sm"
                      variant="flat"
                      onPress={() => setCitationStyle("MLA")}
                    >
                      MLA Style
                    </Button>
                    <Button
                      color="default"
                      size="sm"
                      variant="flat"
                      onPress={() => setCitationStyle("BibTeX")}
                    >
                      BibTeX Style
                    </Button>
                  </div>
                </div>

                <div>
                  <h4 className="font-semibold text-gray-800 mb-2">
                    Export Options
                  </h4>
                  <div className="flex gap-3 flex-wrap">
                    <Button
                      color="primary"
                      size="sm"
                      startContent={<FiDownload size={16} />}
                    >
                      Export All
                    </Button>
                    <Button
                      size="sm"
                      startContent={<FiShare2 size={16} />}
                      variant="flat"
                    >
                      Share
                    </Button>
                  </div>
                </div>
              </div>
            </CardBody>
          </Card>
        </Tab>

        {/* Reference */}
        <Tab
          key="reference"
          title={
            <span className="flex items-center gap-2">
              <HiOutlineLink size={16} /> Reference
            </span>
          }
        >
          <div className="mt-4 space-y-3">
            {[1, 2].map((i) => (
              <Card key={i} className="border border-gray-200 shadow-sm">
                <CardBody>
                  <h4 className="font-semibold text-gray-800">
                    A Predictive Analysis of Retail Sales Forecasting using
                    Machine Learning Techniques
                  </h4>
                  <Chip
                    className="mt-2"
                    color="success"
                    size="sm"
                    variant="flat"
                  >
                    Article
                  </Chip>
                  <p className="text-sm text-gray-500 mt-1">
                    Institution • Author • Description
                  </p>
                </CardBody>
              </Card>
            ))}
          </div>
        </Tab>
      </Tabs>
    </div>
  );
}
