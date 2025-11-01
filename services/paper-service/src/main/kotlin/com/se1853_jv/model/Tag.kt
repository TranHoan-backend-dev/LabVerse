package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
//import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "tags")
data class Tag(
    @Id
    val id: String? = null,
    val name: String? = null,
    val paperIds: List<String> = emptyList(),

//    @Field(name = "number_of_paper_connect_with_tag")
    val numberOfPaperConnectingToTag: Int? = paperIds.size,
)
