package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class LocalStackExecCommandTest {

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5.0"));

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        localstack.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", "test-bucket");
    }

    @Test
    void test() {
        S3Client s3 = S3Client
                .builder()
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        assertThat(s3.listBuckets().buckets()).hasSize(1);
        assertThat(s3.listBuckets().buckets().get(0).name()).isEqualTo("test-bucket");
    }

}
