package com.ada.banco.infra.controller;

import com.ada.banco.domain.exception.ContaNaoExisteException;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.domain.usecase.ContaUseCase;
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
    public ResponseEntity<?> getByCpf(@PathVariable String cpf) {
        List<Conta> contas;
        try {
            contas = contaUseCase.listarPorCpf(cpf);
            return new ResponseEntity<>(contas, HttpStatus.OK);
        } catch (ContaNaoExisteException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
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

    @PutMapping("/atualizar")
    public ResponseEntity atualizar(@RequestBody Conta conta) throws Exception {
        Long id = conta.getId();
        try {
            Conta contaAtualizada = contaUseCase.atualizar(id, conta);
            return ResponseEntity.ok(contaAtualizada);
        } catch (ContaNaoExisteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
