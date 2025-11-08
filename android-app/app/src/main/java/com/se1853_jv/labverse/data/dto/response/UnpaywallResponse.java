package com.se1853_jv.labverse.data.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;

public class UnpaywallResponse {
    @JsonAlias("best_oa_location")
    public BestOALocation bestOALocation;

    public static class BestOALocation {
        @JsonAlias("url_for_pdf")
        public String urlForPdf;
    }
}
