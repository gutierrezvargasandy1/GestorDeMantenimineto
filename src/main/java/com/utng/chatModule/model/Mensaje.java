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
public class Mensaje {
    @BsonId
    private ObjectId id;

    private ObjectId chatId;
    private ObjectId emisorId;
    private ObjectId receptorId;

    private String mensaje;
    private String tipo;

    private boolean leido;
    private boolean enviado;

    private LocalDateTime fechaEnvio;

}
