package com.example.despacho.controller;

import com.example.despacho.model.GuiaDespacho;
import com.example.despacho.service.GuiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;

    /**
     * POST /guias/crear
     * Crea una guía de despacho y la guarda en EFS.
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, String>> crearGuia(@RequestBody GuiaDespacho guia) {
        String rutaEfs = guiaService.crearGuia(guia);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guía creada exitosamente en EFS",
                "rutaEfs", rutaEfs,
                "numeroGuia", guia.getNumeroGuia()));
    }

    /**
     * POST /guias/subir
     * Sube la guía desde EFS a S3.
     */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirGuia(
            @RequestParam String numeroGuia,
            @RequestParam String transportista,
            @RequestParam String fecha) {
        String keyS3 = guiaService.subirGuiaS3(numeroGuia, transportista, fecha);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guía subida exitosamente a S3",
                "keyS3", keyS3));
    }

    /**
     * GET /guias/descargar
     * Descarga la guía desde S3.
     */
    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarGuia(
            @RequestParam String transportista,
            @RequestParam String fecha,
            @RequestParam String numeroGuia) {
        byte[] contenido = guiaService.descargarGuia(transportista, fecha, numeroGuia);
        String nombreArchivo = "guia" + numeroGuia + ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    /**
     * PUT /guias/actualizar
     * Actualiza una guía existente en EFS y S3.
     */
    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, String>> actualizarGuia(
            @RequestParam String transportista,
            @RequestParam String fecha,
            @RequestParam String numeroGuia,
            @RequestBody GuiaDespacho guiaActualizada) {
        String keyS3 = guiaService.actualizarGuia(transportista, fecha, numeroGuia, guiaActualizada);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guía actualizada exitosamente",
                "keyS3", keyS3));
    }

    /**
     * DELETE /guias/eliminar
     * Elimina una guía de S3 y EFS.
     */
    @DeleteMapping("/eliminar")
    public ResponseEntity<Map<String, String>> eliminarGuia(
            @RequestParam String transportista,
            @RequestParam String fecha,
            @RequestParam String numeroGuia) {
        guiaService.eliminarGuia(transportista, fecha, numeroGuia);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guía eliminada exitosamente",
                "numeroGuia", numeroGuia));
    }

    /**
     * GET /guias/consultar
     * Consulta historial de guías por transportista y/o fecha.
     */
    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarGuias(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false) String fecha) {
        List<String> guias = guiaService.consultarGuias(transportista, fecha);
        return ResponseEntity.ok(Map.of(
                "total", guias.size(),
                "guias", guias));
    }
}