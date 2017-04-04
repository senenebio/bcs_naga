/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author Drei
 */
@Data
public class CustomError {
    @NonNull
    private String message;
}
