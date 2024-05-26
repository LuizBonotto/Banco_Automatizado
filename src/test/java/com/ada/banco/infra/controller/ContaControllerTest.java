package com.ada.banco.infra.controller;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.domain.usecase.ContaUseCase;
import com.ada.banco.infra.gateway.bd.ContaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class ContaControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ContaController contaController;

    @Autowired
    ContaGateway contaGateway;

    private Conta contaTest;

    @BeforeEach
    void beforeEach() {
        contaRepository.deleteAll();
        contaTest = new Conta(1L, 2L, 3L, BigDecimal.valueOf(0,2), "Luiz Teste", "000.000.000-00");
    }

    @Test
    void criarConta_ComSucesso_DeveRetornarStatus201() throws Exception {
        // Arrange
        String requestBody = objectMapper.writeValueAsString(contaTest);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Conta conta = contaGateway.buscarPorId(1L);
        Assertions.assertNotNull(conta);
        Assertions.assertEquals("Luiz Teste", conta.getTitular());
    }

    @Test
    void criarConta_JaExistente_DeveRetornarStatusBadRequest() throws Exception {
        // Arrange
        String requestBody = objectMapper.writeValueAsString(contaTest);

        contaController.criarConta(contaTest);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    void criarConta_ComSucesso_DeveSalvarAConta() throws Exception {
        // when
        contaController.criarConta(contaTest);

        // then
        List<Conta> contasCriadas = contaGateway.listarPorCpf("000.000.000-00");
        Assertions.assertTrue(contasCriadas.contains(contaTest));
    }

    @Test
    @DisplayName("Lista contas com mesmo CPF")
    void deveListarContasComMesmoCPF() throws Exception {
        Conta conta = new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta1 = new Conta(3L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta2 = new Conta(4L, 2L, 4L, BigDecimal.ZERO, "João", "123.456.789-01");

        // when
        contaController.criarConta(contaTest);
        contaController.criarConta(conta);
        contaController.criarConta(conta1);
        contaController.criarConta(conta2);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/contas/listar/{cpf}", "123.456.789-00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }
    @Test
    @DisplayName("Listar todas as contas")
    void deveListarTodasAsContas() throws Exception {
        Conta conta = new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta1 = new Conta(3L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta2 = new Conta(4L, 2L, 4L, BigDecimal.ZERO, "João", "123.456.789-01");

        // when
        contaController.criarConta(contaTest);
        contaController.criarConta(conta);
        contaController.criarConta(conta1);
        contaController.criarConta(conta2);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/contas/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].titular").value(contaTest.getTitular()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].titular").value(conta.getTitular()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].titular").value(conta2.getTitular()));
    }

    @Test
    @DisplayName("Exception não achou conta")
    void deveLancarExceptionAoNaoLocalizarConta() throws Exception{
        // when
        contaController.criarConta(contaTest);
        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/contas/listar/{cpf}", "123.456.789-01")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isNotFound())
                        .andExpect(MockMvcResultMatchers.content()
                        .string("A conta com CPF: 123.456.789-01 não existe"));
    }
    @Test
    @DisplayName("Atualizar a conta")
    void deveAtualizarConta() throws Exception {
        contaController.criarConta(contaTest);

        contaTest.setTitular("Luiz Atualizado");
        contaTest.setCpf("000.000.000-01");
        contaTest.setAgencia(10L);
        contaTest.setDigito(9L);

        String requestBody = objectMapper.writeValueAsString(contaTest);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/contas/atualizar/{id}",1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.titular").value("Luiz Atualizado"));

        Conta contaAtualizada = contaGateway.buscarPorId(1L);

        Assertions.assertAll ("Verificando a conta salva",
                () -> Assertions.assertTrue(contaAtualizada.equals(contaTest)), //para chamar o metodo equals da conta
                () -> Assertions.assertEquals("Luiz Atualizado", contaAtualizada.getTitular()),
                () -> Assertions.assertEquals(9L, contaAtualizada.getDigito()),
                () -> Assertions.assertEquals(10L, contaAtualizada.getAgencia()),
                () -> Assertions.assertEquals("000.000.000-01", contaAtualizada.getCpf())

        );


    }

    @Test
    @DisplayName("Falha ao atualizar uma conta indexistente")
    void deveRetornarNotFoundQuandoContaNaoExiste() throws Exception {
        Conta contaAtualizada = new Conta(99L, 2L, 3L, BigDecimal.valueOf(1000), "Conta Inexistente", "000.000.000-00");

        String requestBody = objectMapper.writeValueAsString(contaAtualizada);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/atualizar/{id}",contaAtualizada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("A conta com ID: "+ contaAtualizada.getId() +" não existe"));
    }

    @Test
    @DisplayName("Falha ao atualizar uma conta com dados conflitantes")
    void deveRetornarConflictQuandoDadosConflitantes() throws Exception {
        contaController.criarConta(contaTest);

        contaTest.setId(2L);
        String requestBody = objectMapper.writeValueAsString(contaTest);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/atualizar/{id}",1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string("As contas são diferentes"));
    }

    @Test
    @DisplayName("Transferir valor entre contas com sucesso")
    void deveTransferirValorEntreContas() throws Exception {
        contaTest.setSaldo(BigDecimal.valueOf(1000.25));
        contaController.criarConta(contaTest);
        Conta contaOrigem = contaGateway.buscarPorId(1L);

        Conta contaDestino = new Conta(2L, 2L, 2L, BigDecimal.valueOf(500.72), "Conta Destino", "222.222.222-22");
        contaController.criarConta(contaDestino);

        BigDecimal valorTransferencia = BigDecimal.valueOf(200);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/transferir")
                        .param("idOut", contaOrigem.getId().toString())
                        .param("idIn", contaDestino.getId().toString())
                        .param("valor", valorTransferencia.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Verificação dos saldos após a transferência
        Conta contaOrigemAtualizada = contaGateway.buscarPorId(contaOrigem.getId());
        Conta contaDestinoAtualizada = contaGateway.buscarPorId(contaDestino.getId());

        Assertions.assertEquals(BigDecimal.valueOf(800.25), contaOrigemAtualizada.getSaldo());
        Assertions.assertEquals(BigDecimal.valueOf(700.72), contaDestinoAtualizada.getSaldo());
    }

    @Test
    @DisplayName("Falha ao não localizar uma conta para Transferência")
    void deveRetornarNotFoundQuandoTransferenciaNaoExiste() throws Exception {

        contaTest.setSaldo(BigDecimal.valueOf(2000));
        contaController.criarConta(contaTest);

        Long idContaInexistente = 9999L;
        Long idContaExistente = contaGateway.buscarPorId(contaTest.getId()).getId();
        BigDecimal valorTransferencia = BigDecimal.valueOf(100);


        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/transferir")
                        .param("idOut", idContaInexistente.toString())
                        .param("idIn", idContaExistente.toString())
                        .param("valor", valorTransferencia.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("A conta com ID: "+ idContaInexistente +" não existe"));


        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/transferir")
                        .param("idOut", idContaExistente.toString())
                        .param("idIn", idContaInexistente.toString())
                        .param("valor", valorTransferencia.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("A conta com ID: "+ idContaInexistente +" não existe"));

    }

    @Test
    @DisplayName("Falha ao não possuir saldo para Transferência")
    void deveRetornarBadRequestQuandoSaldoInsuficienteTransferencia() throws Exception {

        contaTest.setSaldo(BigDecimal.valueOf(1000));
        contaController.criarConta(contaTest);

        contaTest.setId(2L);
        contaController.criarConta(contaTest);

        Conta contaOrigem = contaGateway.buscarPorId(1L);
        Conta contaDestino = contaGateway.buscarPorId(2L);

        BigDecimal valorTransferencia = BigDecimal.valueOf(2000);


        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/transferir")
                        .param("idOut", contaOrigem.getId().toString())
                        .param("idIn", contaDestino.getId().toString())
                        .param("valor", valorTransferencia.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("A conta com ID: " + contaOrigem.getId() + " não possui o saldo para saque"));


    }
}
