package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respostas da API que vêm dentro de um objeto "data"
 * @param <T> Tipo do objeto dentro de "data"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiDataResponse<T> {
    private T data;
}
