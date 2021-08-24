package api.meli.com.co.infrastructure.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;
import org.junit.jupiter.api.Test;

/**
 * Consistency tests of the defined architecture of the application
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public class ArchLayerTest {

	@Test
	public void shouldComplyWithLayeringRules() {

		final JavaClasses javaClasses = new ClassFileImporter().importPackages("api.meli.com.co");
		final LayeredArchitecture arch = Architectures.layeredArchitecture()
			//  layers
			.layer("infrastructure").definedBy("..infrastructure..")
			.layer("application").definedBy("..application..")
			.layer("domain").definedBy("..domain..")
			// constraints
			.whereLayer("infrastructure").mayNotBeAccessedByAnyLayer()
			.whereLayer("application").mayOnlyBeAccessedByLayers("infrastructure")
			.whereLayer("domain").mayOnlyBeAccessedByLayers("application", "infrastructure");

		arch.check(javaClasses);
	}
}
