package com.utng.chatModule.service;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

import com.utng.chatModule.model.Chat;
import com.utng.chatModule.model.Mensaje;
import com.utng.chatModule.repository.ChatRepository;
import com.utng.chatModule.repository.MensajeRepository;

/**
 * Capa de servicio que envuelve ChatRepository + MensajeRepository
 * para que los controladores de JavaFX no hablen directo con Mongo.
 */
public class ChatService {

    private final ChatRepository chatRepository = new ChatRepository();
    private final MensajeRepository mensajeRepository = new MensajeRepository();

    public static ObjectId idDesdeLong(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de usuario no puede ser null");
        }
        return new ObjectId(String.format("%024x", id));
    }

    /** Busca el chat entre dos usuarios, o lo crea si no existe. */
    public Chat obtenerOCrearChat(ObjectId usuario1, ObjectId usuario2) {
        Chat chat = chatRepository.buscarEntreUsuarios(usuario1, usuario2);

        if (chat == null) {
            chat = new Chat();
            chat.setUsuario1(usuario1);
            chat.setUsuario2(usuario2);
            chat = chatRepository.guardar(chat);
        }

        return chat;
    }

    public List<Mensaje> obtenerMensajes(ObjectId chatId) {
        return mensajeRepository.buscarPorChatIdOrdenado(chatId);
    }

    /** Guarda el mensaje y actualiza el "ultimoMensaje" del chat. */
    public Mensaje enviarMensaje(ObjectId chatId, ObjectId emisorId, ObjectId receptorId, String texto) {
        Mensaje mensaje = new Mensaje();
        mensaje.setChatId(chatId);
        mensaje.setEmisorId(emisorId);
        mensaje.setReceptorId(receptorId);
        mensaje.setMensaje(texto);
        mensaje.setTipo("texto");
        mensaje.setEnviado(true);
        mensaje.setLeido(false);
        mensaje.setFechaEnvio(LocalDateTime.now());

        mensaje = mensajeRepository.guardar(mensaje);

        Chat chat = chatRepository.buscarPorId(chatId);
        if (chat != null) {
            chat.setMensajeId(mensaje.getId());
            chat.setUltimoMensaje(texto);
            chat.setFechaUltimoMensaje(mensaje.getFechaEnvio());
            chatRepository.actualizar(chat);
        }

        return mensaje;
    }

    public void marcarConversacionLeida(ObjectId chatId, ObjectId lectorId) {
        mensajeRepository.marcarConversacionLeida(chatId, lectorId);
    }

    public long contarNoLeidos(ObjectId chatId, ObjectId lectorId) {
        return mensajeRepository.contarNoLeidos(chatId, lectorId);
    }

    public void eliminarConversacion(ObjectId chatId) {
        mensajeRepository.eliminarPorChatId(chatId);
    }

    /**
     * TEMPORAL: convierte un id entero (los que ya usan tus pantallas demo)
     * en un ObjectId valido, rellenando con ceros a la izquierda.
     * Reemplaza esto en cuanto tengas ObjectId reales de tu coleccion de usuarios.
     */
    public static ObjectId idDesdeEntero(int id) {
        return new ObjectId(String.format("%024x", id));
    }
}