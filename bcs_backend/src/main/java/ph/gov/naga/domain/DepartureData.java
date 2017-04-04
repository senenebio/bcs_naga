/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.domain;

import lombok.Data;
import lombok.NonNull;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Data
public class DepartureData {

    @NonNull
    private TerminalPass terminalPass;

    public DepartureData() {
        this.terminalPass = new TerminalPass();
    }

    public DepartureData(@NonNull TerminalPass tp) {
        this.terminalPass = tp;

    }

    public boolean isValid() {
        boolean result = this.getTerminalPass() != null
                && this.getTerminalPass().getPlateNumber() != null
                && !this.getTerminalPass().getPlateNumber().isEmpty()
                && this.getTerminalPass().getDepartureTime() != null
                && this.getTerminalPass().getDepartureRecorder() != null
                && !this.getTerminalPass().getDepartureRecorder().isEmpty();
        return result;
    }
    
    
}
