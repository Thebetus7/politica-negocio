package com.example.politica_negocio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${storage.s3.endpoint}") String endpoint,
            @Value("${storage.s3.region}") String region,
            @Value("${storage.s3.access-key}") String accessKey,
            @Value("${storage.s3.secret-key}") String secretKey) {

        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        // Configurar endpoint override y path style únicamente si es desarrollo local (MinIO)
        if (endpoint.contains("localhost") || endpoint.contains("127.0.0.1") || endpoint.contains("minio")) {
            builder.endpointOverride(URI.create(endpoint))
                   .serviceConfiguration(S3Configuration.builder()
                           .pathStyleAccessEnabled(true)
                           .build());
        }

        return builder.build();
    }

    @Bean
    public CommandLineRunner initBucket(
            S3Client s3Client,
            @Value("${storage.s3.bucket}") String bucketName) {
        return args -> {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
                System.out.println("El bucket de MinIO/S3 '" + bucketName + "' ya existe.");
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    System.out.println("Bucket de MinIO/S3 creado exitosamente: " + bucketName);
                } else {
                    System.err.println("Error verificando bucket de S3: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("No se pudo inicializar o conectar a MinIO al arrancar: " + e.getMessage());
            }
        };
    }
}
