package com.example.despacho.service;

import com.example.despacho.model.GuiaDespacho;
import com.example.despacho.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuiaServiceImpl implements GuiaService {

    private final S3Repository s3Repository;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.efs.path}")
    private String efsPath;

    @Override
    public String crearGuia(GuiaDespacho guia) {
        try {
            String contenido = buildContenidoGuia(guia);
            String carpeta = efsPath + "/" + guia.getFecha() + "/" + guia.getTransportista();
            Path dirPath = Paths.get(carpeta);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String nombreArchivo = "guia" + guia.getNumeroGuia() + ".txt";
            Path archivoPath = dirPath.resolve(nombreArchivo);
            Files.writeString(archivoPath, contenido, StandardCharsets.UTF_8);
            return archivoPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al crear guía en EFS: " + e.getMessage(), e);
        }
    }

    @Override
    public String subirGuiaS3(String numeroGuia, String transportista, String fecha) {
        try {
            String nombreArchivo = "guia" + numeroGuia + ".txt";
            Path archivoPath = Paths.get(efsPath, fecha, transportista, nombreArchivo);
            if (!Files.exists(archivoPath)) {
                throw new RuntimeException("La guía no existe en EFS: " + archivoPath);
            }
            byte[] contenido = Files.readAllBytes(archivoPath);
            String keyS3 = fecha + "/" + transportista + "/" + nombreArchivo;
            s3Repository.subirArchivo(bucket, keyS3, contenido);
            return keyS3;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir guía a S3: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] descargarGuia(String transportista, String fecha, String numeroGuia) {
        String key = fecha + "/" + transportista + "/guia" + numeroGuia + ".txt";
        return s3Repository.descargarArchivo(bucket, key);
    }

    @Override
    public String actualizarGuia(String transportista, String fecha, String numeroGuia, GuiaDespacho guiaActualizada) {
        try {
            String contenido = buildContenidoGuia(guiaActualizada);
            String nombreArchivo = "guia" + numeroGuia + ".txt";
            Path archivoPath = Paths.get(efsPath, fecha, transportista, nombreArchivo);
            if (!Files.exists(archivoPath.getParent())) {
                Files.createDirectories(archivoPath.getParent());
            }
            Files.writeString(archivoPath, contenido, StandardCharsets.UTF_8);
            String keyS3 = fecha + "/" + transportista + "/" + nombreArchivo;
            s3Repository.subirArchivo(bucket, keyS3, contenido.getBytes(StandardCharsets.UTF_8));
            return keyS3;
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar guía: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarGuia(String transportista, String fecha, String numeroGuia) {
        String nombreArchivo = "guia" + numeroGuia + ".txt";
        String keyS3 = fecha + "/" + transportista + "/" + nombreArchivo;
        s3Repository.eliminarArchivo(bucket, keyS3);
        Path archivoPath = Paths.get(efsPath, fecha, transportista, nombreArchivo);
        try {
            Files.deleteIfExists(archivoPath);
        } catch (IOException e) {
            System.err.println("Advertencia: no se pudo borrar del EFS: " + e.getMessage());
        }
    }

    @Override
    public List<String> consultarGuias(String transportista, String fecha) {
        String prefijo = "";
        if (fecha != null && !fecha.isEmpty() && transportista != null && !transportista.isEmpty()) {
            prefijo = fecha + "/" + transportista + "/";
        } else if (fecha != null && !fecha.isEmpty()) {
            prefijo = fecha + "/";
        } else if (transportista != null && !transportista.isEmpty()) {
            List<String> todos = s3Repository.listarArchivos(bucket, "");
            return todos.stream()
                    .filter(k -> k.contains("/" + transportista + "/"))
                    .toList();
        }
        return s3Repository.listarArchivos(bucket, prefijo);
    }

    private String buildContenidoGuia(GuiaDespacho g) {
        return "========================================\n" +
                "       GUÍA DE DESPACHO\n" +
                "========================================\n" +
                "Número de Guía  : " + g.getNumeroGuia() + "\n" +
                "Transportista   : " + g.getTransportista() + "\n" +
                "Fecha           : " + g.getFecha() + "\n" +
                "Destinatario    : " + g.getDestinatario() + "\n" +
                "Dirección       : " + g.getDireccionDestino() + "\n" +
                "Descripción     : " + g.getDescripcionPedido() + "\n" +
                "Peso (kg)       : " + g.getPesoCarga() + "\n" +
                "Estado          : " + g.getEstado() + "\n" +
                "========================================\n";
    }
}