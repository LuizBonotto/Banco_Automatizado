package com.ada.banco.infra.controller;

import com.ada.banco.domain.model.Conta;
import com.ada.banco.infra.gateway.bd.ContaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

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
}
