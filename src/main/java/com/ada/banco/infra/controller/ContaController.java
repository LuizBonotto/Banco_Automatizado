package com.ada.banco.infra.controller;

import com.ada.banco.domain.model.Conta;
import com.ada.banco.domain.usecase.ContaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private ContaUseCase contaUseCase;

    public ContaController(ContaUseCase contaUseCase) {
        this.contaUseCase = contaUseCase;
    }

    @PostMapping
    public ResponseEntity criarConta(@RequestBody Conta conta) throws Exception {
        Conta novaConta;
        try {
            novaConta = contaUseCase.execute(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(novaConta);
    }
}
