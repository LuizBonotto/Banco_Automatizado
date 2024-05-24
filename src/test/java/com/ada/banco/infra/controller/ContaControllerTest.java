package com.ada.banco.infra.controller;

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
    private ContaUseCase contaUseCase;

    @BeforeEach
    void beforeEach() {
        contaRepository.deleteAll();
    }

    @Test
    void criarConta_ComSucesso_DeveRetornarStatus201() throws Exception {
        // Arrange
        String requestBody = objectMapper.writeValueAsString(new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Conta conta = contaRepository.findByCpf("123456789");
        Assertions.assertNotNull(conta);
    }

    @Test
    void criarConta_JaExistente_DeveRetornarStatusBadRequest() throws Exception {
        // Arrange
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789");
        String requestBody = objectMapper.writeValueAsString(conta);

        contaRepository.save(conta);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    void criarConta_ComSucesso_DeveSalvarAConta() throws Exception {
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789");

        // when
        contaController.criarConta(conta);

        // then
        Conta contaCriada = contaRepository.findByCpf("123456789");
        Assertions.assertEquals("Pedro", contaCriada.getTitular());
    }

    @Test
    @DisplayName("Lista contas com mesmo CPF")
    void deveListarContasComMesmoCPF() throws Exception {
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta1 = new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta2 = new Conta(3L, 2L, 4L, BigDecimal.ZERO, "João", "123.456.789-01");

        // when
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
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta1 = new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        Conta conta2 = new Conta(3L, 2L, 4L, BigDecimal.ZERO, "João", "123.456.789-01");

        // when
        contaController.criarConta(conta);
        contaController.criarConta(conta1);
        contaController.criarConta(conta2);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/contas/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Exception não achou conta")
    void deveLancarExceptionAoNaoLocalizarConta() throws Exception{
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123.456.789-00");
        // when
        contaController.criarConta(conta);
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
        Conta conta = new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro Errado", "123.456.789-00");
        contaUseCase.criar(conta);

        conta.setTitular("Pedro Certo");
        String requestBody = objectMapper.writeValueAsString(conta);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/contas/atualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Conta contaAtualizada = contaRepository.findByIdEquals(1L);
        Assertions.assertEquals("Pedro Certo", contaAtualizada.getTitular());

    }
}
