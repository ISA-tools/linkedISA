package org.isatools.isa2owl;

import java.net.URL;

import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class for ISA2OWLInstancePopulator
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLInstancePopulatorTest {

	private ISA2OWLInstancePopulator populator = null;
	private String configDir = null;
	private String isatabParentDir = null;
	
	@Before
    public void setUp() throws Exception {
		
		String baseDir = System.getProperty("basedir");
		System.out.println("baseDir="+baseDir);
    	configDir = baseDir + "src/test/resources/configurations/isaconfig-default_v2011-02-18/";
    	System.out.println("configDir="+configDir);
        isatabParentDir = baseDir + "/src/test/resources/BII-I-1-URI";
        System.out.println("isatabParentDir="+isatabParentDir);
		populator = new ISA2OWLInstancePopulator(configDir);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPopulate() { 
    	populator.populateOntology(isatabParentDir);
    }
	
}
