package com.se1853_jv.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.InputStream
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
@ConditionalOnBean(S3Client::class)
class S3Service(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket}") private val bucketName: String,
    @Value("\${aws.s3.region}") private val region: String
) {
    companion object {
        private const val PAPERS_FOLDER = "papers"
    }

    /**
     * Upload PDF file to S3 with public-read ACL
     * @param inputStream Input stream of the PDF file
     * @param contentType Content type (e.g., "application/pdf")
     * @return S3 URL of the uploaded file
     */
    fun uploadPdf(inputStream: InputStream, contentType: String = "application/pdf"): String {
        val fileName = "$PAPERS_FOLDER/${UUID.randomUUID()}.pdf"
        
        logger.info { "Uploading PDF to S3: $fileName, bucket: $bucketName, region: $region with ACL: public-read" }
        
        inputStream.use { stream ->
            try {
                // Read input stream into bytes using ByteArrayOutputStream for efficiency
                val outputStream = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                val fileBytes = outputStream.toByteArray()
                outputStream.close()
                
                logger.info { "File size: ${fileBytes.size} bytes" }
                
                // Build PutObjectRequest with public-read ACL
                val putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ) // Set ACL to public-read
                    .build()
                
                logger.info { "Uploading file to S3 with ACL: PUBLIC_READ" }
                
                // Upload file with public-read ACL
                val requestBody = RequestBody.fromBytes(fileBytes)
                val putObjectResponse = s3Client.putObject(putObjectRequest, requestBody)
                
                logger.info { "File uploaded successfully. ETag: ${putObjectResponse.eTag()}" }
                logger.info { "✅ File uploaded with ACL: public-read. File is publicly accessible." }
                
                // Note: If ACL fails to apply, it may be due to bucket settings:
                // 1. "Block Public ACLs" must be disabled
                // 2. "Object Ownership" should be set to "Bucket owner preferred" or "ACLs enabled"
                // Check bucket permissions in AWS Console if files are not publicly accessible
                
                // Generate S3 URL - use region-specific URL if not us-east-1
                val downloadUrl = if (region == "us-east-1") {
                    "https://$bucketName.s3.amazonaws.com/$fileName"
                } else {
                    "https://$bucketName.s3.$region.amazonaws.com/$fileName"
                }
                
                logger.info { "PDF uploaded successfully to S3: $downloadUrl (ACL: public-read)" }
                
                return downloadUrl
            } catch (e: Exception) {
                logger.error(e) { "Failed to upload PDF to S3: ${e.message}" }
                
                // Check if error is related to ACL
                if (e.message?.contains("AccessControlListNotSupported", ignoreCase = true) == true ||
                    e.message?.contains("InvalidRequest", ignoreCase = true) == true) {
                    logger.error { "ACL error detected. Bucket may have 'Block Public ACLs' or 'Object Ownership' settings that prevent ACL application." }
                    logger.error { "Please check S3 bucket settings: Object Ownership should be 'Bucket owner preferred' or 'ACLs enabled'" }
                    logger.error { "And 'Block Public ACLs' should be disabled if you want public-read access" }
                }
                
                throw RuntimeException("Failed to upload PDF to S3", e)
            }
        }
    }
}

