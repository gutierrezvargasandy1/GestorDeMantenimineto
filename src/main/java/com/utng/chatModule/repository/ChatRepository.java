package com.utng.chatModule.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.utng.chatModule.model.Chat;
import com.utng.config.MongoDBConnection;

import static com.mongodb.client.model.Filters.*;

public class ChatRepository {

    private final MongoCollection<Document> collection;

    public ChatRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();

        collection = database.getCollection("chats");
    }

    // ==============================
    // CREAR CHAT
    // ==============================
    public Chat guardar(Chat chat) {

        if (chat.getId() == null) {
            chat.setId(new ObjectId());
        }

        if (chat.getCreadoEn() == null) {
            chat.setCreadoEn(LocalDateTime.now());
        }

        chat.setActualizadoEn(LocalDateTime.now());

        Document document = convertirADocument(chat);

        collection.insertOne(document);

        return chat;
    }

    // ==============================
    // BUSCAR POR ID
    // ==============================
    public Chat buscarPorId(ObjectId id) {

        Document document = collection
                .find(eq("_id", id))
                .first();

        if (document == null) {
            return null;
        }

        return convertirAChat(document);
    }

    // ==============================
    // BUSCAR TODOS
    // ==============================
    public List<Chat> buscarTodos() {

        List<Chat> chats = new ArrayList<>();

        for (Document document : collection.find()) {
            chats.add(convertirAChat(document));
        }

        return chats;
    }

    // ==============================
    // BUSCAR CHAT ENTRE 2 USUARIOS
    // ==============================
    public Chat buscarEntreUsuarios(
            ObjectId usuario1,
            ObjectId usuario2) {

        Document document = collection.find(
                or(
                        and(
                                eq("usuario1", usuario1),
                                eq("usuario2", usuario2)),
                        and(
                                eq("usuario1", usuario2),
                                eq("usuario2", usuario1))))
                .first();

        if (document == null) {
            return null;
        }

        return convertirAChat(document);
    }

    // ==============================
    // ACTUALIZAR CHAT
    // ==============================
    public Chat actualizar(Chat chat) {

        chat.setActualizadoEn(LocalDateTime.now());

        Document document = convertirADocument(chat);

        collection.replaceOne(
                eq("_id", chat.getId()),
                document);

        return chat;
    }

    // ==============================
    // ELIMINAR CHAT
    // ==============================
    public boolean eliminar(ObjectId id) {

        return collection
                .deleteOne(eq("_id", id))
                .getDeletedCount() > 0;
    }

    // ==============================
    // CONVERTIR CHAT -> DOCUMENT
    // ==============================
    private Document convertirADocument(Chat chat) {

        return new Document("_id", chat.getId())
                .append("usuario1", chat.getUsuario1())
                .append("usuario2", chat.getUsuario2())
                .append("mensajeId", chat.getMensajeId())
                .append("ultimoMensaje", chat.getUltimoMensaje())
                .append(
                        "fechaUltimoMensaje",
                        chat.getFechaUltimoMensaje() != null
                                ? chat.getFechaUltimoMensaje().toString()
                                : null)
                .append(
                        "creadoEn",
                        chat.getCreadoEn() != null
                                ? chat.getCreadoEn().toString()
                                : null)
                .append(
                        "actualizadoEn",
                        chat.getActualizadoEn() != null
                                ? chat.getActualizadoEn().toString()
                                : null);
    }

    // ==============================
    // CONVERTIR DOCUMENT -> CHAT
    // ==============================
    private Chat convertirAChat(Document document) {

        Chat chat = new Chat();

        chat.setId(document.getObjectId("_id"));
        chat.setUsuario1(document.getObjectId("usuario1"));
        chat.setUsuario2(document.getObjectId("usuario2"));
        chat.setMensajeId(document.getObjectId("mensajeId"));
        chat.setUltimoMensaje(document.getString("ultimoMensaje"));

        String fechaUltimoMensaje = document.getString("fechaUltimoMensaje");

        if (fechaUltimoMensaje != null) {
            chat.setFechaUltimoMensaje(
                    LocalDateTime.parse(fechaUltimoMensaje));
        }

        String creadoEn = document.getString("creadoEn");

        if (creadoEn != null) {
            chat.setCreadoEn(
                    LocalDateTime.parse(creadoEn));
        }

        String actualizadoEn = document.getString("actualizadoEn");

        if (actualizadoEn != null) {
            chat.setActualizadoEn(
                    LocalDateTime.parse(actualizadoEn));
        }

        return chat;
    }
}