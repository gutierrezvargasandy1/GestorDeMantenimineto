package com.utng.chatModule.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.utng.chatModule.model.Mensaje;
import com.utng.config.MongoDBConnection;

import static com.mongodb.client.model.Filters.*;

public class MensajeRepository {

    private final MongoCollection<Document> collection;

    public MensajeRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();

        collection = database.getCollection("mensajes");
    }

    // ==========================================
    // CREAR / GUARDAR MENSAJE
    // ==========================================
    public Mensaje guardar(Mensaje mensaje) {

        if (mensaje.getId() == null) {
            mensaje.setId(new ObjectId());
        }

        if (mensaje.getFechaEnvio() == null) {
            mensaje.setFechaEnvio(LocalDateTime.now());
        }

        Document document = convertirADocument(mensaje);

        collection.insertOne(document);

        return mensaje;
    }

    // ==========================================
    // BUSCAR MENSAJE POR ID
    // ==========================================
    public Mensaje buscarPorId(ObjectId id) {

        Document document = collection
                .find(eq("_id", id))
                .first();

        if (document == null) {
            return null;
        }

        return convertirAMensaje(document);
    }

    // ==========================================
    // OBTENER TODOS LOS MENSAJES
    // ==========================================
    public List<Mensaje> buscarTodos() {

        List<Mensaje> mensajes = new ArrayList<>();

        for (Document document : collection.find()) {
            mensajes.add(convertirAMensaje(document));
        }

        return mensajes;
    }

    // ==========================================
    // OBTENER MENSAJES DE UN CHAT
    // ==========================================
    public List<Mensaje> buscarPorChatId(ObjectId chatId) {

        List<Mensaje> mensajes = new ArrayList<>();

        for (Document document : collection.find(
                eq("chatId", chatId))) {
            mensajes.add(convertirAMensaje(document));
        }

        return mensajes;
    }

    // ==========================================
    // OBTENER MENSAJES ENVIADOS POR UN USUARIO
    // ==========================================
    public List<Mensaje> buscarPorEmisorId(ObjectId emisorId) {

        List<Mensaje> mensajes = new ArrayList<>();

        for (Document document : collection.find(
                eq("emisorId", emisorId))) {
            mensajes.add(convertirAMensaje(document));
        }

        return mensajes;
    }

    // ==========================================
    // ACTUALIZAR MENSAJE
    // ==========================================
    public Mensaje actualizar(Mensaje mensaje) {

        Document document = convertirADocument(mensaje);

        collection.replaceOne(
                eq("_id", mensaje.getId()),
                document);

        return mensaje;
    }

    // ==========================================
    // MARCAR MENSAJE COMO LEÍDO
    // ==========================================
    public boolean marcarComoLeido(ObjectId mensajeId) {

        Document actualizacion = new Document(
                "$set",
                new Document("leido", true));

        return collection.updateOne(
                eq("_id", mensajeId),
                actualizacion).getModifiedCount() > 0;
    }

    // ==========================================
    // MARCAR MENSAJE COMO ENVIADO
    // ==========================================
    public boolean marcarComoEnviado(ObjectId mensajeId) {

        Document actualizacion = new Document(
                "$set",
                new Document("enviado", true));

        return collection.updateOne(
                eq("_id", mensajeId),
                actualizacion).getModifiedCount() > 0;
    }

    // ==========================================
    // ELIMINAR MENSAJE
    // ==========================================
    public boolean eliminar(ObjectId id) {

        return collection.deleteOne(
                eq("_id", id)).getDeletedCount() > 0;
    }

    // ==========================================
    // CONVERTIR MENSAJE -> DOCUMENT
    // ==========================================
    private Document convertirADocument(Mensaje mensaje) {

        return new Document("_id", mensaje.getId())
                .append("chatId", mensaje.getChatId())
                .append("emisorId", mensaje.getEmisorId())
                .append("receptorId", mensaje.getReceptorId())
                .append("mensaje", mensaje.getMensaje())
                .append("tipo", mensaje.getTipo())
                .append("leido", mensaje.isLeido())
                .append("enviado", mensaje.isEnviado())
                .append(
                        "fechaEnvio",
                        mensaje.getFechaEnvio() != null
                                ? mensaje.getFechaEnvio().toString()
                                : null);
    }

    // ==========================================
    // CONVERTIR DOCUMENT -> MENSAJE
    // ==========================================
    private Mensaje convertirAMensaje(Document document) {

        Mensaje mensaje = new Mensaje();

        mensaje.setId(document.getObjectId("_id"));
        mensaje.setChatId(document.getObjectId("chatId"));
        mensaje.setEmisorId(document.getObjectId("emisorId"));
        mensaje.setReceptorId(document.getObjectId("receptorId"));

        mensaje.setMensaje(
                document.getString("mensaje"));

        mensaje.setTipo(
                document.getString("tipo"));

        Boolean leido = document.getBoolean("leido");
        mensaje.setLeido(
                leido != null ? leido : false);

        Boolean enviado = document.getBoolean("enviado");
        mensaje.setEnviado(
                enviado != null ? enviado : false);

        String fechaEnvio = document.getString("fechaEnvio");

        if (fechaEnvio != null) {
            mensaje.setFechaEnvio(
                    LocalDateTime.parse(fechaEnvio));
        }

        return mensaje;
    }

    // ==========================================
    // OBTENER MENSAJES DE UN CHAT, ORDENADOS
    // ==========================================
    public List<Mensaje> buscarPorChatIdOrdenado(ObjectId chatId) {
        List<Mensaje> mensajes = buscarPorChatId(chatId);
        mensajes.sort(Comparator.comparing(Mensaje::getFechaEnvio));
        return mensajes;
    }

    // ==========================================
    // CONTAR MENSAJES NO LEIDOS DE UN CHAT PARA UN RECEPTOR
    // ==========================================
    public long contarNoLeidos(ObjectId chatId, ObjectId receptorId) {
        return collection.countDocuments(
                and(eq("chatId", chatId), eq("receptorId", receptorId), eq("leido", false)));
    }

    // ==========================================
    // MARCAR TODA LA CONVERSACION COMO LEIDA
    // ==========================================
    public long marcarConversacionLeida(ObjectId chatId, ObjectId receptorId) {
        Document actualizacion = new Document("$set", new Document("leido", true));

        return collection.updateMany(
                and(eq("chatId", chatId), eq("receptorId", receptorId), eq("leido", false)),
                actualizacion).getModifiedCount();
    }

    // ==========================================
    // BORRAR TODOS LOS MENSAJES DE UN CHAT
    // ==========================================
    public long eliminarPorChatId(ObjectId chatId) {
        return collection.deleteMany(eq("chatId", chatId)).getDeletedCount();
    }
}