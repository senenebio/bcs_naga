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
public class ArrivalData {

    @NonNull
    private TerminalPass terminalPass;

    public ArrivalData() {
        this.terminalPass = new TerminalPass();
    }

    public ArrivalData(@NonNull TerminalPass tp) {
        this.terminalPass = tp;
    }

    public boolean isValid() {
        //plateNumber, arrivalTime, arrivalRecorder
        boolean result = this.terminalPass != null
                && this.terminalPass.getPlateNumber() != null
                && !this.terminalPass.getPlateNumber().isEmpty()
                && this.terminalPass.getArrivalTime() != null
                && this.terminalPass.getArrivalRecorder() != null
                && !this.terminalPass.getArrivalRecorder().isEmpty();
        return result;
    }

}
