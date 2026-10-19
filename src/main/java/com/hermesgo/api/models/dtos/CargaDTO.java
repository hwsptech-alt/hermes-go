package com.hermesgo.api.models.dtos;

import com.hermesgo.api.models.Carga;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CargaDTO {

    private String origen;
    private String destino;
    private LocalDateTime fechaHora;
    private String tipoCarga;
    private Double peso;
    private Double volumen;
    private Boolean esPerecedera;
    private Boolean esCompartida;
    private String descripcion;
    private Long clienteId;

    public Carga toEntity(com.hermesgo.api.models.Usuario cliente) {
        Carga carga = new Carga();
        carga.setOrigen(this.origen);
        carga.setDestino(this.destino);
        carga.setFechaHora(this.fechaHora);
        carga.setTipoCarga(this.tipoCarga);
        carga.setPeso(this.peso);
        carga.setVolumen(this.volumen);
        carga.setEsPerecedera(this.esPerecedera);
        carga.setEsCompartida(this.esCompartida);
        carga.setDescripcion(this.descripcion);
        carga.setCliente(cliente);
        return carga;
    }
}