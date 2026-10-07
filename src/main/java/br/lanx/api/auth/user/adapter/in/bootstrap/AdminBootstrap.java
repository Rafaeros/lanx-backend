package br.lanx.api.auth.user.adapter.in.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.lanx.api.auth.user.application.port.in.BootstrapAdminUseCase;

/** Quem aciona o núcleo na subida da aplicação, como um controller aciona por HTTP. */
@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final BootstrapAdminUseCase bootstrap;
    private final AdminProperties props;

    AdminBootstrap(BootstrapAdminUseCase bootstrap, AdminProperties props) {
        this.bootstrap = bootstrap;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (props.password() == null || props.password().isBlank()) {
            log.info("ADMIN_PASSWORD vazio: bootstrap do administrador ignorado");
            return;
        }
        bootstrap.ensureAdmin(props.username(), props.password());
        log.info("Administrador '{}' garantido", props.username());
    }
}
