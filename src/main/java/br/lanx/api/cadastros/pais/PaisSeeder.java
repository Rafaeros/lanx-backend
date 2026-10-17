package br.lanx.api.cadastros.pais;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class PaisSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PaisSeeder.class);
    private static final String DELIMITER = ";";

    private final PaisRepository paisRepository;
    private final ResourceLoader resourceLoader;
    private final String csvPath;

public PaisSeeder(PaisRepository paisRepository,
                  ResourceLoader resourceLoader,
                  @Value("${seeder.paises.path:classpath:db/seeders/paises.csv}") String csvPath) {
    this.paisRepository = paisRepository;
    this.resourceLoader = resourceLoader;
    this.csvPath = csvPath;
}

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (paisRepository.count() > 0) {
            log.info("Seeder de países ignorado: já existem registros.");
            return;
        }

        Resource resource = resourceLoader.getResource(csvPath);

        if (!resource.exists()) {
            log.error("Arquivo de seed de países não encontrado: {}", csvPath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            reader.lines()
                    .skip(1)
                    .map(this::parseLine)
                    .flatMap(Optional::stream)
                    .filter(this::doesNotExist)
                    .map(this::createPais)
                    .forEach(paisRepository::save);
        }

        log.info("Seeder de países concluído. Total: {}", paisRepository.count());
    }

    private Optional<PaisCsvDto> parseLine(String line) {
        String[] parts = line.split(DELIMITER, -1);

        if (parts.length != 3) {
            log.warn("Linha inválida no arquivo CSV de países: {}", line);
            return Optional.empty();
        }

        String nome = parts[0].trim();
        String iso2 = parts[1].trim().toUpperCase();
        String iso3 = parts[2].trim().toUpperCase();

        if (nome.isBlank() || iso2.length() != 2 || iso3.length() != 3) {
            log.warn("Dados incompletos ou inválidos: {}", line);
            return Optional.empty();
        }

        return Optional.of(new PaisCsvDto(nome, iso2, iso3));
    }

    private boolean doesNotExist(PaisCsvDto dto) {
        return !paisRepository.existsByCodigoIso2(dto.iso2());
    }

    private Pais createPais(PaisCsvDto dto) {
        Pais pais = new Pais();
        pais.setNome(dto.nome());
        pais.setCodigoIso2(dto.iso2());
        pais.setCodigoIso3(dto.iso3());
        return pais;
    }

    private record PaisCsvDto(String nome, String iso2, String iso3) {
    }
}