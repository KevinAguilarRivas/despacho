package com.example.despacho.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class S3RepositoryImpl implements S3Repository {

    private final S3Client s3Client;

    @Override
    public void subirArchivo(String bucket, String key, byte[] contenido) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(contenido));
    }

    @Override
    public byte[] descargarArchivo(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public void eliminarArchivo(String bucket, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public void moverArchivo(String bucket, String keyOrigen, String keyDestino) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(keyOrigen)
                .destinationBucket(bucket)
                .destinationKey(keyDestino)
                .build();
        s3Client.copyObject(copyRequest);
        eliminarArchivo(bucket, keyOrigen);
    }

    @Override
    public List<String> listarArchivos(String bucket, String prefijo) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefijo)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }
}