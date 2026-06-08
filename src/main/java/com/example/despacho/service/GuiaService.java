package com.example.despacho.service;

import com.example.despacho.model.GuiaDespacho;
import java.util.List;

public interface GuiaService {
    // Crea la guía en EFS y retorna la ruta local del archivo
    String crearGuia(GuiaDespacho guia);

    // Sube la guía desde EFS a S3 organizada por fecha/transportista
    String subirGuiaS3(String numeroGuia, String transportista, String fecha);

    // Descarga la guía desde S3
    byte[] descargarGuia(String transportista, String fecha, String numeroGuia);

    // Actualiza el contenido de una guía en S3 (y en EFS)
    String actualizarGuia(String transportista, String fecha, String numeroGuia, GuiaDespacho guiaActualizada);

    // Elimina una guía de S3 (y del EFS si existe)
    void eliminarGuia(String transportista, String fecha, String numeroGuia);

    // Consulta guías por transportista y/o fecha
    List<String> consultarGuias(String transportista, String fecha);
}