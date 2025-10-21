package com.se1853_jv.service

import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}
private const val GROBID_API: String = "http://localhost:8070/api/processFulltextDocument" // xu ly toan van ban
private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

@Service
class GrobidService {
    fun parsePdf(file: File): MutableList<MutableMap<String, String>> {
        logger.info { "Parse pdf file" }
        val mediaType = "application/pdf".toMediaType()
        val fileBody = file.asRequestBody(mediaType)

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("input", file.name, fileBody)
            .build()
        val request: Request = Request.Builder()
            .url(GROBID_API)
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val xml = response.body!!.string()
            println(xml)
            return extractMetadata(xml)
        }
    }

    private fun extractMetadata(xml: String): MutableList<MutableMap<String, String>> {
        val doc: Document = Jsoup.parse(xml, "", Parser.xmlParser())
        val citations: Elements = doc.select("listBibl > biblStruct")
        val citationList = mutableListOf<MutableMap<String, String>>()

        for (cite in citations) {
            val citation = mutableMapOf<String, String>()

            val title = cite.select("analytic > title").text()
                .ifEmpty { cite.select("monogr > title[level=j]").text() }
                .ifEmpty { cite.select("monogr > title").text() }
                .ifEmpty { cite.select("title").text() }

            val authors = buildString {
                val personAuthors = cite.select("author > persName")
                if (personAuthors.isNotEmpty()) {
                    append(personAuthors.joinToString(", ") {
                        val forename = it.select("forename").text()
                        val surname = it.select("surname").text()
                        "$forename $surname".trim()
                    })
                } else {
                    val orgAuthors = cite.select("author > orgName")
                    if (orgAuthors.isNotEmpty()) {
                        append(orgAuthors.joinToString(", ") { it.text() })
                    }
                }
            }

            val journal = cite.select("monogr > title[level=j]").text()
                .ifEmpty { cite.select("monogr > title").text() }
                .ifEmpty { cite.select("title[level=j]").text() }

            val year = cite.select("imprint > date[when]").attr("when")
                .ifEmpty { cite.select("imprint > date").text() }
                .ifEmpty { cite.select("date").text() }

            val doi = cite.select("idno[type=DOI]").text()
                .ifEmpty { cite.select("ptr[target^=doi]").attr("target") }

            citation["title"] = title.trim()
            citation["authors"] = authors.trim()
            citation["journal"] = journal.trim()
            citation["year"] = year.trim()
            citation["doi"] = doi.trim()

            citationList.add(citation)
        }

        return citationList
    }


}