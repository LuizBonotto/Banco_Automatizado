package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.exception.ContaNaoExisteException;
import com.ada.banco.domain.exception.ContaSaldoInsuficienteException;
import com.ada.banco.domain.exception.ContasDiferentesException;
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
    @DisplayName("Listar todas as contas")
    public void deveListarContas() throws Exception {
        Conta conta1 = new Conta(3L,2L, 3L, BigDecimal.ZERO, "Luiz", "000.000.000-00");
        contaUseCase.criar(conta1);
        Conta conta2 = new Conta(4L,2L, 3L, BigDecimal.ZERO, "Luiz1", "000.000.000-00");
        contaUseCase.criar(conta2);
        Conta conta3 = new Conta(5L,2L, 3L, BigDecimal.ZERO, "Luiz2", "000.000.000-00");
        contaUseCase.criar(conta3);

        List<Conta> contas = contaUseCase.listar();

        Assertions.assertAll("Lista contem as contas criadas nesse teste",
                () -> Assertions.assertTrue(contas.contains(conta1)),
                () -> Assertions.assertTrue(contas.contains(conta2)),
                () -> Assertions.assertTrue(contas.contains(conta3))
                );

    }
    @Test
    @DisplayName("Buscar contas com o mesmo CPF")
    public void deveBuscarContasComMesmoCPF() throws Exception {
        Conta conta1 = new Conta(6L,2L, 3L, BigDecimal.valueOf(1.50), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta1);
        Conta conta2 = new Conta(7L,2L, 3L,BigDecimal.valueOf(2.49), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta2);
        Conta conta3 = new Conta(8L,2L, 3L, BigDecimal.valueOf(10245.50), "Luiz", "000.000.000-01");
        contaUseCase.criar(conta3);

        List<Conta> contas = contaUseCase.listarPorCpf("000.000.000-00");

        Assertions.assertAll("Contem as constas criadas",
                () -> Assertions.assertTrue(contas.contains(conta1)),
                () -> Assertions.assertTrue(contas.contains(conta2)),
                () -> Assertions.assertFalse(contas.contains(conta3))
        );
    }

    @Test
    @DisplayName("Transferencia entra contas")
    public void deveTrasferirCorretamente() throws Exception {
        Conta conta1 = new Conta(9L,2L, 3L, BigDecimal.valueOf(1.50), "Luiz", "000.000.000-00");
        Conta conta2 = new Conta(10L,2L, 3L,BigDecimal.valueOf(2.49), "Luiz", "000.000.000-00");

        contaUseCase.criar(conta1);
        contaUseCase.criar(conta2);

        BigDecimal valorAntigoConta1 = conta1.getSaldo();
        BigDecimal valorAntigoConta2 = conta2.getSaldo();

        BigDecimal valorATransferir = new BigDecimal("1.50");

        Long numeroTrasacao = contaUseCase.transferir(9L,10L, valorATransferir);

        Assertions.assertAll("Verifica o saldo das contas",
                () -> Assertions.assertEquals(valorAntigoConta1.subtract(valorATransferir), conta1.getSaldo()),
                () -> Assertions.assertEquals(valorAntigoConta2.add(valorATransferir), conta2.getSaldo())
        );

        System.out.println("Transacao "+numeroTrasacao+": R$"+ valorATransferir+" transferidos da conta ID" +
                            conta1.getId() + " para a conta ID" +conta2.getId());

    }

    @Test
    @DisplayName("Lança exception caso conta não exista")
    public void deveLancarExceptionCasoContaNaoExista() {
        String cpf = "80800";
        Throwable throwable =
        Assertions.assertThrows(ContaNaoExisteException.class, () -> contaUseCase.listarPorCpf(cpf));
        Assertions.assertEquals("A conta com CPF: " + cpf + " não existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Lança exception caso conta com ID não exista")
    public void deveLancarExceptionCasoContaComIdNaoExista() {
        Long id = 10000L;
        Throwable throwable =
                Assertions.assertThrows(ContaNaoExisteException.class, () -> contaUseCase.transferir(id, 1L, BigDecimal.ZERO));
        Assertions.assertEquals("A conta com ID: " + id + " não existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Lança Exception saldo insuficiente")
    public void deveLancarExceptionCasoContaNaoPossuaSaldoSuficiente() throws Exception {
        Conta conta1 = new Conta(11L, 2L, 3L, BigDecimal.valueOf(1.50), "Luiz", "000.000.000-00");
        Conta conta2 = new Conta(12L, 2L, 3L, BigDecimal.valueOf(1.50), "Luiz", "000.000.000-00");
        BigDecimal valorATransferir = new BigDecimal("1.51");

        contaUseCase.criar(conta1);
        contaUseCase.criar(conta2);

        Throwable throwable = Assertions.assertThrows(ContaSaldoInsuficienteException.class,
                () -> contaUseCase.transferir(conta1.getId(), conta2.getId(), valorATransferir));
        Assertions.assertEquals( "A conta com ID: " + conta1.getId() + " não possui o saldo para saque", throwable.getMessage());
    }

    @Test
    @DisplayName("Atualiza uma conta")
    public void deveAtualizarConta() throws Exception {
        Conta conta = new Conta(13L,2L, 3L, BigDecimal.valueOf(15075.50), "Luiz Errado", "000.000.000-00");
        contaUseCase.criar(conta);

        Conta contaAtualizada = new Conta(13L,2L, 3L, BigDecimal.valueOf(15075.50), "Luiz Certo", "000.000.000-00");

        Conta contaOk = contaUseCase.atualizar(conta.getId(), contaAtualizada);

        Assertions.assertEquals("Luiz Certo", contaOk.getTitular());

    }

    @Test
    @DisplayName("Atualizar conta errada")
    public void deveLancarExcepitonAtualizarContaErrada() throws Exception {
        Conta conta = new Conta(14L,2L, 3L, BigDecimal.valueOf(15075.50), "Luiz Errado", "000.000.000-00");
        contaUseCase.criar(conta);

        Conta contaAtualizada = new Conta(15L,2L, 3L, BigDecimal.valueOf(15075.50), "Luiz Certo", "000.000.000-00");

        Throwable throwable = Assertions.assertThrows(ContasDiferentesException.class,
                () -> contaUseCase.atualizar(conta.getId(), contaAtualizada));

        Assertions.assertEquals( "As contas são diferentes", throwable.getMessage());
    }
}
