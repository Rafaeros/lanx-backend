package br.lanx.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import br.lanx.api.auth.authentication.AuthController;
import br.lanx.api.auth.authorization.MeController;

/** Regras de arquitetura: se alguém violar, o build quebra. */
@AnalyzeClasses(packages = "br.lanx.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

        // ---------- hexagonal (cadastros/unidade_medida) ----------

        @ArchTest
        static final ArchRule dominioPuro = noClasses()
                        .that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                        "org.springframework..", "jakarta.persistence..", "io.jsonwebtoken..",
                                        "..adapter..", "..application..");

        @ArchTest
        static final ArchRule applicationNaoConheceAdapter = noClasses()
                        .that().resideInAPackage("..application..")
                        .should().dependOnClassesThat().resideInAPackage("..adapter..");

        @ArchTest
        static final ArchRule adapterInUsaSoPortasDeEntrada = noClasses()
                        .that().resideInAPackage("..adapter.in..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                        "..application.service..", "..application.port.out..", "..adapter.out..");

        @ArchTest
        static final ArchRule adapterOutNaoConheceAdapterIn = noClasses()
                        .that().resideInAPackage("..adapter.out..")
                        .should().dependOnClassesThat().resideInAPackage("..adapter.in..");

        // ---------- auth em camadas: controller -> service -> repository ----------

        @ArchTest
        static final ArchRule controllerNaoAcessaRepository = noClasses()
                        .that().haveSimpleNameEndingWith("Controller")
                        .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                        .as("controller fala com o service, nunca direto com o repository");

        @ArchTest
        static final ArchRule serviceNaoConheceController = noClasses()
                        .that().haveSimpleNameEndingWith("Service")
                        .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

        @ArchTest
        static final ArchRule semCiclosEntreSubmodulosDeAuth = slices()
                        .matching("br.lanx.api.auth.(*)..")
                        .should().beFreeOfCycles();

        @ArchTest
        static final ArchRule sharedNaoDependeDeModulos = noClasses()
                        .that().resideInAPackage("br.lanx.api.shared..")
                        .should().dependOnClassesThat()
                        .resideInAnyPackage("br.lanx.api.auth..", "br.lanx.api.cadastros..", "br.lanx.api.compras..",
                                        "br.lanx.api.producao..", "br.lanx.api.comercial..", "br.lanx.api.qualidade..");

        /**
         * Cadastros são dados mestres usados por todos os módulos: não podem depender de nenhum deles
         * (o contrário é que vale). Checagens de uso (ex.: cliente com OP) entram por interface do cadastro.
         */
        @ArchTest
        static final ArchRule cadastrosNaoDependemDeModulos = noClasses()
                        .that().resideInAPackage("br.lanx.api.cadastros..")
                        .should().dependOnClassesThat()
                        .resideInAnyPackage("br.lanx.api.compras..", "br.lanx.api.producao..",
                                        "br.lanx.api.comercial..", "br.lanx.api.qualidade..");

        // ---------- segurança: nenhuma rota fica aberta por esquecimento ----------

        /**
         * Substitui o antigo SkipPermission: toda rota precisa declarar @PreAuthorize.
         * As únicas exceções são as rotas que exigem apenas autenticação (/auth/me)
         * ou nem isso (/auth/login, liberada no SecurityConfig).
         */
        @ArchTest
        static final ArchRule todaRotaDeclaraPreAuthorize = methods()
                        .that().areAnnotatedWith(GetMapping.class)
                        .or().areAnnotatedWith(PostMapping.class)
                        .or().areAnnotatedWith(PutMapping.class)
                        .or().areAnnotatedWith(PatchMapping.class)
                        .or().areAnnotatedWith(DeleteMapping.class)
                        .and().areNotDeclaredIn(AuthController.class)
                        .and().areNotDeclaredIn(MeController.class)
                        .should().beAnnotatedWith(PreAuthorize.class)
                        .as("toda rota precisa de @PreAuthorize (exceto login e /auth/me)");
}
