package com.utng.chatModule.model;

import java.time.LocalDateTime;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat {

    @BsonId
    private ObjectId id;

    private ObjectId usuario1;
    private ObjectId usuario2;

    private ObjectId mensajeId;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

}
