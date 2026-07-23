package com.pix.recorrente.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(AgendamentoRejeitadoFraudeException.class)
    public ResponseEntity<ErrorResponse> handleAgendamentoRejeitadoFraude(AgendamentoRejeitadoFraudeException ex) {
        AnaliseFraude af = ex.getAnaliseFraude();
        var afResp = new ErrorResponse.AnaliseFraudeResponse(
                af.statusRisco(),
                af.score(),
                af.regrasVioladas(),
                af.dataAnalise()
        );
        var response = new ErrorResponse(
                "AGENDAMENTO_REJEITADO_FRAUDE",
                "A solicitação de agendamento foi negada pelas regras de segurança.",
                afResp
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(AgendamentoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleAgendamentoDuplicado(AgendamentoDuplicadoException ex) {
        ErrorResponse.AnaliseFraudeResponse afResp = null;

        if (ex.getAgendamento() != null && ex.getAgendamento().getAnaliseFraudeJson() != null) {
            try {
                AnaliseFraude af = objectMapper.readValue(ex.getAgendamento().getAnaliseFraudeJson(), AnaliseFraude.class);
                afResp = new ErrorResponse.AnaliseFraudeResponse(
                        af.statusRisco(),
                        af.score(),
                        af.regrasVioladas(),
                        af.dataAnalise()
                );
            } catch (Exception e) {
                // Se falhar parse, deixa null
            }
        }

        var response = new ErrorResponse(
                "AGENDAMENTO_DUPLICADO",
                ex.getMessage(),
                afResp
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AgendamentoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleAgendamentoNaoEncontrado(AgendamentoNaoEncontradoException ex) {
        var response = new ErrorResponse(
                "AGENDAMENTO_NAO_ENCONTRADO",
                "Nenhum agendamento localizado para o ID informado.",
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        String message = String.join(", ", errors);

        var response = new ErrorResponse(
                "VALIDACAO_ERRO",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
