package com.example.despacho.repository;

import java.util.List;

public interface S3Repository {
    void subirArchivo(String bucket, String key, byte[] contenido);

    byte[] descargarArchivo(String bucket, String key);

    void eliminarArchivo(String bucket, String key);

    void moverArchivo(String bucket, String keyOrigen, String keyDestino);

    List<String> listarArchivos(String bucket, String prefijo);
}