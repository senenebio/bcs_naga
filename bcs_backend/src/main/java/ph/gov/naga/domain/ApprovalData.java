/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.domain;

import java.util.Date;
import lombok.Data;
import lombok.NonNull;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Data
public class ApprovalData {

    @NonNull
    private TerminalPass terminalPass;

    public ApprovalData() {
        this.terminalPass = new TerminalPass();
    }

    public ApprovalData(@NonNull TerminalPass tp) {
        this.terminalPass = tp;
    }

    public boolean isValid() {
        boolean result = this.terminalPass != null
                && this.terminalPass.getApprovedBy() != null
                && this.terminalPass.getApprovedTime() != null
                && !this.terminalPass.getApprovedBy().isEmpty();
        return result;
    }

}
