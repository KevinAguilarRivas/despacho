package com.example.despacho.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaDespacho {
    private String numeroGuia;
    private String transportista;
    private String fecha; // formato: yyyyMMdd ej: 20241215
    private String destinatario;
    private String direccionDestino;
    private String descripcionPedido;
    private double pesoCarga;
    private String estado; // PENDIENTE, EN_TRANSITO, ENTREGADO
}