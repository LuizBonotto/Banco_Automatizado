package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.infra.gateway.bd.ContaGatewayDatabase;
import com.ada.banco.infra.gateway.bd.ContaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class ContaUseCaseTestH2 {
    @Autowired
    private ContaUseCase contaUseCase;
    @Autowired
    private ContaGatewayDatabase contaGateway;
    @Autowired
    private ContaRepository contaRepository;

    private Conta contaTeste;


    @BeforeEach
    public void setUp() {
        contaRepository.deleteAll();
        contaTeste = new Conta(1L,2L, 3L, BigDecimal.valueOf(2.49), "Luiz", "000.000.000-00");
    }

    @Test
    @DisplayName("Criar conta")
    public void deveCriarContaCorretamente() throws Exception {
        contaUseCase.criar(contaTeste);
        Conta contaSalva = contaGateway.buscarPorId(contaTeste.getId());
        Assertions.assertEquals(contaTeste, contaSalva);
    }

    @Test
    public void deveLancarExceptionCasoContaJaExista() {
        contaGateway.salvar(contaTeste);
        Throwable throwable =
                Assertions.assertThrows(ContaJaExisteException.class,
                        () -> contaUseCase.criar(contaTeste));
        Assertions.assertEquals("A conta ID: "+ contaTeste.getId() + " ja existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Listar rodas as contas")
    public void deveListarContas() throws Exception {
        Conta conta = new Conta(3L,2L, 3L, BigDecimal.ZERO, "Luiz", "000.000.000-00");
        contaUseCase.criar(conta);
        conta = new Conta(4L,2L, 3L, BigDecimal.ZERO, "Luiz1", "000.000.000-00");
        contaUseCase.criar(conta);
        conta = new Conta(1L,2L, 3L, BigDecimal.ZERO, "Luiz2", "000.000.000-00");
        contaUseCase.criar(conta);
        List<Conta> contas = contaGateway.listar();
        for (Conta contaShow : contas) {
            System.out.println(contaShow);
        }
    }
}