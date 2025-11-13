package com.se1853_jv.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

private val logger = KotlinLogging.logger {}

@Configuration
@ConditionalOnProperty(prefix = "aws.s3", name = ["access-key"])
class S3Config(
    @Value("\${aws.s3.access-key}") private val accessKey: String,
    @Value("\${aws.s3.secret-key}") private val secretKey: String,
    @Value("\${aws.s3.region}") private val region: String,
    @Value("\${aws.s3.bucket}") private val bucketName: String
) {
    @Bean
    fun s3Client(): S3Client {
        logger.info { "Initializing S3Client with region: $region, bucket: $bucketName" }
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }

    @Bean
    fun s3BucketName(): String = bucketName
}

