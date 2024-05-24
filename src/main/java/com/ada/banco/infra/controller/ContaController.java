package com.ada.banco.infra.controller;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.domain.usecase.ContaUseCase;
import com.ada.banco.infra.gateway.bd.ContaGatewayDatabase;
import com.ada.banco.infra.gateway.bd.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private ContaUseCase contaUseCase;

    @Autowired
    public ContaController(ContaUseCase contaUseCase) {
        this.contaUseCase = contaUseCase;
    }


    @GetMapping("/listar")
    public ResponseEntity<List<Conta>> get() {
        return new ResponseEntity<>(contaUseCase.listar(), HttpStatus.OK);
    }

    @GetMapping("/listar/{cpf}")
    public ResponseEntity<List<Conta>> getByCpf(@PathVariable String cpf) {
        List<Conta> contas = contaUseCase.listarPorCpf(cpf);
        return new ResponseEntity<>(contas, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity criarConta(@RequestBody Conta conta) throws Exception {
        Conta novaConta;
        try {
            novaConta = contaUseCase.criar(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(novaConta);
    }
}
