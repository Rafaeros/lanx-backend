package br.lanx.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.List;
import java.util.stream.Stream;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;

/** Regras do hexagonal e dos submódulos de auth: se alguém violar, o build quebra. */
@AnalyzeClasses(packages = "br.lanx.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // ---------- hexagonal (vale para todos os módulos) ----------

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

    // ---------- submódulos de auth ----------

    /** permission, role, user, authentication, authorization. */
    private static final List<String> MODULES = List.of("role", "user", "authentication", "authorization");

    @ArchTest
    static final ArchRule semCiclosEntreModulos = slices()
            .matching("br.lanx.api.auth.(*)..")
            .should().beFreeOfCycles();

    /** permission é a base: todos dependem dela, ela não depende de nenhum. */
    @ArchTest
    static final ArchRule permissionNaoDependeDeNinguem = noClasses()
            .that().resideInAPackage("..auth.permission..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    MODULES.stream().map(m -> "..auth." + m + "..").toArray(String[]::new));

    /**
     * Um módulo só enxerga de outro o domínio e as portas de entrada
     * (application.port.in). Services, portas de saída e adapters são privados.
     */
    @ArchTest
    static final ArchTests modulosSoSeFalamPorPortasDeEntrada = ArchTests.in(ModuleBoundaries.class);

    static class ModuleBoundaries {
        @ArchTest
        static final ArchRule role = boundaryOf("role");
        @ArchTest
        static final ArchRule user = boundaryOf("user");
        @ArchTest
        static final ArchRule authentication = boundaryOf("authentication");
        @ArchTest
        static final ArchRule authorization = boundaryOf("authorization");
    }

    private static ArchRule boundaryOf(String module) {
        String[] forbidden = MODULES.stream()
                .filter(other -> !other.equals(module))
                .flatMap(other -> Stream.of(
                        "..auth." + other + ".application.service..",
                        "..auth." + other + ".application.port.out..",
                        "..auth." + other + ".adapter.in.web..",
                        "..auth." + other + ".adapter.in.bootstrap..",
                        "..auth." + other + ".adapter.in.security..",
                        "..auth." + other + ".adapter.out.."))
                .toArray(String[]::new);
        return noClasses()
                .that().resideInAPackage("..auth." + module + "..")
                .should().dependOnClassesThat().resideInAnyPackage(forbidden)
                .as("o módulo " + module + " só usa o domínio e as portas de entrada dos outros módulos");
    }
}
