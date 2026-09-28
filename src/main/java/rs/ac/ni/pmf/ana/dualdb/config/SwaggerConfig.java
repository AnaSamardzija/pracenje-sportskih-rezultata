package rs.ac.ni.pmf.ana.dualdb.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig
{
	@Bean
	public OpenAPI openAPI()
	{
		final String schemeName = "bearerAuth";

		return new OpenAPI()
				.info(new Info()
							  .title("Sports Results API")
							  .version("v1")
							  .description("REST API za praćenje rezultata amaterskih sportskih mečeva nad MariaDB ili MongoDB bazom. "
									  + "Baza se bira pri prijavi (storageType) i nosi se u tokenu."))
				.tags(List.of(
						new Tag().name("Auth").description("Prijava i registracija"),
						new Tag().name("Nalog i korisnici").description("Moj profil i lozinka, upravljanje korisnicima za SYSTEM_ADMIN-a"),
						new Tag().name("Sportovi").description("Katalog sportova i njihova pravila bodovanja"),
						new Tag().name("Grupe i članstvo").description("Grupe, pridruživanje i upravljanje članovima"),
						new Tag().name("Mečevi").description("Unos i pregled odigranih mečeva"),
						new Tag().name("Rang-liste i statistika").description("Rang-liste i statistika igrača, izvedene iz mečeva")))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components()
									.addSecuritySchemes(schemeName,
														new SecurityScheme()
																.name(schemeName)
																.type(SecurityScheme.Type.HTTP)
																.scheme("bearer")
																.bearerFormat("JWT")));
	}
}
