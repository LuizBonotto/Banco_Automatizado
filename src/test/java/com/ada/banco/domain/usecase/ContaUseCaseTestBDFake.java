package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.infra.gateway.bd.ContaGatewayDatabaseFake;
import com.ada.banco.infra.gateway.http.EmailGatewayHttpFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class ContaUseCaseTestBDFake {
    private ContaUseCase contaUseCase;
    private ContaGatewayDatabaseFake contaGatewayDatabaseFake;
    private EmailGatewayHttpFake emailGatewayHttpFake;


    @BeforeEach
    public void setUp() {
        contaGatewayDatabaseFake = new ContaGatewayDatabaseFake();
        emailGatewayHttpFake = new EmailGatewayHttpFake();
        contaUseCase = new ContaUseCase(contaGatewayDatabaseFake, emailGatewayHttpFake);
    }

    @Test
    public void deveCriarContaCorretamente() throws Exception {
        Conta conta = new Conta(2L,2L, 3L, BigDecimal.ZERO, "Luiz", "12345678941");

        contaUseCase.criar(conta);

        Conta contaSalva = contaGatewayDatabaseFake.buscarPorId(conta.getId());

        Assertions.assertEquals(conta, contaSalva);
    }

    @Test
    public void deveLancarExceptionCasoContaJaExista() {
        Conta conta = new Conta(1L,2L, 3L, BigDecimal.ZERO, "Luiz", "000.000.000-00");
        contaGatewayDatabaseFake.salvar(conta);
        Throwable throwable = Assertions.assertThrows(ContaJaExisteException.class, () -> contaUseCase.criar(conta));
        Assertions.assertEquals("A conta ID: "+ conta.getId() + " ja existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Listar rodas as contas")
    public void deveListarContas() throws Exception {
        Conta conta = new Conta(3L,2L, 3L, BigDecimal.ZERO, "Luiz", "000.000.000-00");
        contaUseCase.criar(conta);
        conta = new Conta(4L,2L, 3L, BigDecimal.ZERO, "Luiz1", "000.000.000-00");
        contaUseCase.criar(conta);
        conta = new Conta(5L,2L, 3L, BigDecimal.ZERO, "Luiz2", "000.000.000-00");
        contaUseCase.criar(conta);
        List<Conta> contas = contaGatewayDatabaseFake.listar();
        for (Conta contaShow : contas) {
            System.out.println(contaShow);
        }
    }
    @Test
    @DisplayName("Buscar contas com o mesmo CPF")
    public void deveBuscarContasComMesmoCPF() throws Exception {
        Conta conta1 = new Conta(6L,2L, 3L, BigDecimal.valueOf(1.50), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta1);
        Conta conta2 = new Conta(7L,2L, 3L,BigDecimal.valueOf(2.49), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta2);
        Conta conta3 = new Conta(8L,2L, 3L, BigDecimal.valueOf(10245.50), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta3);
        List<Conta> contas = contaGatewayDatabaseFake.listarPorCpf("000.000.000-00");

        Assertions.assertAll("Contem as constas criadas",
                () -> Assertions.assertTrue(contas.contains(conta1)),
                () -> Assertions.assertTrue(contas.contains(conta2)),
                () -> Assertions.assertTrue(contas.contains(conta3))
        );
    }
}
