package br.lanx.api.compras.relatorios.iqf.adapter.in.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.compras.relatorios.iqf.application.ConsultarIqfUseCase;

@RestController 
@RequestMapping("/compras/relatorios/iqf")
public class IqfController {
    
    private final ConsultarIqfUseCase consultarIqfUseCase;

    public IqfController(ConsultarIqfUseCase consultarIqfUseCase) {
        this.consultarIqfUseCase = consultarIqfUseCase;
    }


}
