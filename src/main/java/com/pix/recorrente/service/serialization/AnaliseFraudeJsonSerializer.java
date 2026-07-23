package com.pix.recorrente.service.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.exception.JsonSerializationException;
import org.springframework.stereotype.Service;

@Service
public class AnaliseFraudeJsonSerializer {
    private final ObjectMapper objectMapper;

    public AnaliseFraudeJsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(AnaliseFraude analiseFraude) {
        try {
            return objectMapper.writeValueAsString(analiseFraude);
        } catch (Exception e) {
            throw new JsonSerializationException("Falha ao serializar análise de fraude", e);
        }
    }

    public AnaliseFraude deserialize(String json) {
        try {
            return objectMapper.readValue(json, AnaliseFraude.class);
        } catch (Exception e) {
            throw new JsonSerializationException("Falha ao desserializar análise de fraude", e);
        }
    }
}
