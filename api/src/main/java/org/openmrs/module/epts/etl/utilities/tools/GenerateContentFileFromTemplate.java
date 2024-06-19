package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class GenerateContentFileFromTemplate {
	
	public static final String templateFile = "D:\\JEE\\Workspace\\FGH\\epts\\etl\\conf\\templates\\db_quick_copy_template.json";
	
	public static final String testSitesFilePath = "D:\\JEE\\Workspace\\FGH\\epts\\etl\\conf\\testing\\sites.txt";
	
	public static void main(String[] args) throws IOException {
		//generateIdefierTypes(args);
		
		generateRelationship(args);
	}
	
	public static void generateIdefierTypes(String[] args) throws IOException {
		List<Map<String, String>> data = new ArrayList<>();
		
		data.add(fastCreateMap("CONCEPT_UUID","10a51328-0448-11ee-8229-0242c0a84002","TYPE_CODE","OpenMRS_PATIENT_UUID","TYPE_NAME","OpenMRS Patient UUID"));
		data.add(fastCreateMap("CONCEPT_UUID","10a51f3a-0448-11ee-8229-0242c0a84002","TYPE_CODE","OpenMRS_ID_NUM","TYPE_NAME","OpenMRS ID Num"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52084-0448-11ee-8229-0242c0a84002","TYPE_CODE","NID_TARV","TYPE_NAME","NID TARV"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52142-0448-11ee-8229-0242c0a84002","TYPE_CODE","BI","TYPE_NAME","BILHETE DE IDENTIDADE"));
		data.add(fastCreateMap("CONCEPT_UUID","10a521ec-0448-11ee-8229-0242c0a84002","TYPE_CODE","CDG_ATS","TYPE_NAME","CDG ATS"));
		data.add(fastCreateMap("CONCEPT_UUID","10a5230e-0448-11ee-8229-0242c0a84002","TYPE_CODE","CDG_PTV_PRE_NATAL","TYPE_NAME","CODIGO PTV PRE NATAL"));
		data.add(fastCreateMap("CONCEPT_UUID","10a523cc-0448-11ee-8229-0242c0a84002","TYPE_CODE","CDG_ITS","TYPE_NAME","CDG ITS"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52480-0448-11ee-8229-0242c0a84002","TYPE_CODE","CDG_PTV_MATERNIDADE","TYPE_NAME","CDG PTV MATERNIDADE"));
		data.add(fastCreateMap("CONCEPT_UUID","10a5252a-0448-11ee-8229-0242c0a84002","TYPE_CODE","NID_CCR","TYPE_NAME","NID CCR"));
		data.add(fastCreateMap("CONCEPT_UUID","10a525d4-0448-11ee-8229-0242c0a84002","TYPE_CODE","PCR_NUM_REG","TYPE_NAME","PCR NUM REG"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52688-0448-11ee-8229-0242c0a84002","TYPE_CODE","NIT_TB","TYPE_NAME","NIT TB"));
		data.add(fastCreateMap("CONCEPT_UUID","10a5273c-0448-11ee-8229-0242c0a84002","TYPE_CODE","NUM_CANCRO_CERVICAL","TYPE_NAME","NUM CANCRO CERVICAL"));
		data.add(fastCreateMap("CONCEPT_UUID","10a5280e-0448-11ee-8229-0242c0a84002","TYPE_CODE","NUIC","TYPE_NAME","NUIC"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52930-0448-11ee-8229-0242c0a84002","TYPE_CODE","NID_DISA","TYPE_NAME","NID DISA"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52a0c-0448-11ee-8229-0242c0a84002","TYPE_CODE","CRAM_ID","TYPE_NAME","CRAM ID"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52ab6-0448-11ee-8229-0242c0a84002","TYPE_CODE","NID_PREP","TYPE_NAME","NID PREP"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52b6a-0448-11ee-8229-0242c0a84002","TYPE_CODE","OpenEMPI_ID","TYPE_NAME","OpenEMPI ID"));
		data.add(fastCreateMap("CONCEPT_UUID","10a52c14-0448-11ee-8229-0242c0a84002","TYPE_CODE","OpenMRS_ID","TYPE_NAME","OpenMRS ID"));
		
		Path templateFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\idtypetemplate.txt")
		        .toPath();
		Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\moz-mpi-identification-types.dataset").toPath();
		
		Charset charset = StandardCharsets.UTF_8;
		
		for (Map<String, String> conceptSet : data) {
			String content = new String(Files.readAllBytes(templateFile), charset);
			
			content = content.replaceAll("CONCEPT_UUID", conceptSet.get("CONCEPT_UUID"));
			content = content.replaceAll("TYPE_CODE", conceptSet.get("TYPE_CODE"));
			content = content.replaceAll("TYPE_NAME", conceptSet.get("TYPE_NAME"));
			
			FileUtilities.write(destFile.toString(), content.getBytes(charset));
		}
	}
	
	public static void generateIdefierTypesSQL(String[] args) throws IOException {
		
		List<Map<String, String>> data = new ArrayList<>();
		
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "OpenMRS_ID_NUM", "REF_TERM_UUID",
		    "3fa2f0ec-d878-11ed-b580-0242c0a8b002", "CONCEPT_UUID", "3fa2f18c-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID",
		    "3fa2f218-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NID_TARV", "REF_TERM_UUID", "3fa30a78-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa30b0e-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa30ba4-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "BI", "REF_TERM_UUID", "3fa30e06-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa30e9c-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa30f32-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "CDG_ATS", "REF_TERM_UUID", "3fa31108-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa3119e-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa3122a-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "CDG_PTV_PRE_NATAL", "REF_TERM_UUID",
		    "3fa3140a-d878-11ed-b580-0242c0a8b002", "CONCEPT_UUID", "3fa31496-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID",
		    "3fa3152c-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "CDG_ITS", "REF_TERM_UUID", "3fa316f8-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa31784-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa31810-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "CDG_PTV_MATERNIDADE", "REF_TERM_UUID",
		    "3fa319d2-d878-11ed-b580-0242c0a8b002", "CONCEPT_UUID", "3fa31a68-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID",
		    "3fa31afe-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NID_CCR", "REF_TERM_UUID", "3fa31cca-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa31d60-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa31dec-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "PCR_NUM_REG", "REF_TERM_UUID", "3fa31fb8-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa32044-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa320e4-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NIT_TB", "REF_TERM_UUID", "3fa3229c-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa32404-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa32490-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NUM_CANCRO_CERVICAL", "REF_TERM_UUID",
		    "3fa32652-d878-11ed-b580-0242c0a8b002", "CONCEPT_UUID", "3fa326de-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID",
		    "3fa32774-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NUIC", "REF_TERM_UUID", "3fa3292c-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa329c2-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa32a4e-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NID_DISA", "REF_TERM_UUID", "3fa32c06-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa32c9c-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa32d32-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "CRAM_ID", "REF_TERM_UUID", "3fa32ef4-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa32f8a-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa3300c-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "NID_PREP", "REF_TERM_UUID", "3fa331e2-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa3326e-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa33304-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "OpenEMPI_ID", "REF_TERM_UUID", "3fa334d0-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa33566-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa335f2-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "OpenMRS_ID", "REF_TERM_UUID", "3fa337b4-d878-11ed-b580-0242c0a8b002",
		    "CONCEPT_UUID", "3fa3384a-d878-11ed-b580-0242c0a8b002", "ID_TYPE_UUID", "3fa338e0-d878-11ed-b580-0242c0a8b002"));
		
		Path templateFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\id_types_template.sql")
		        .toPath();
		//Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\id_types.sql").toPath();
		
		Charset charset = StandardCharsets.UTF_8;
		
		for (Map<String, String> conceptSet : data) {
			String content = new String(Files.readAllBytes(templateFile), charset);
			
			content = content.replaceAll("CONCEPT_UUID", conceptSet.get("CONCEPT_UUID"));
			content = content.replaceAll("ID_TYPE_UUID", conceptSet.get("ID_TYPE_UUID"));
			
			//FileUtilities.write(destFile.toString(), content.getBytes(charset));
		}
	}
	
	public static void generateRelationship(String[] args) throws IOException {
		
		List<Map<String, String>> data = new ArrayList<>();
		
		data.add(fastCreateMap("CONCEPT_UUID","e69c3ed0-0451-11ee-8229-0242c0a84002","TYPE_CODE","DTR","REF_TERM_UUID","e69c3ea8-0451-11ee-8229-0242c0a84002","TYPE_NAME","Doctor"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6b62-0451-11ee-8229-0242c0a84002","TYPE_CODE","SPVR","REF_TERM_UUID","e69c6b44-0451-11ee-8229-0242c0a84002","TYPE_NAME","Sponse"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6d38-0451-11ee-8229-0242c0a84002","TYPE_CODE","HDNEIHBHD","REF_TERM_UUID","e69c6d10-0451-11ee-8229-0242c0a84002","TYPE_NAME","Chefe de Bairro"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6dce-0451-11ee-8229-0242c0a84002","TYPE_CODE","WKR","REF_TERM_UUID","e69c6db0-0451-11ee-8229-0242c0a84002","TYPE_NAME","Worker"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6e6e-0451-11ee-8229-0242c0a84002","TYPE_CODE","OTHER","REF_TERM_UUID","e69c6e46-0451-11ee-8229-0242c0a84002","TYPE_NAME","Other"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6f0e-0451-11ee-8229-0242c0a84002","TYPE_CODE","DPOWATT","REF_TERM_UUID","e69c6ee6-0451-11ee-8229-0242c0a84002","TYPE_NAME","Chefe de Familia"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c6fe0-0451-11ee-8229-0242c0a84002","TYPE_CODE","FRND","REF_TERM_UUID","e69c6fc2-0451-11ee-8229-0242c0a84002","TYPE_NAME","Friend"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c7076-0451-11ee-8229-0242c0a84002","TYPE_CODE","EMPLY","REF_TERM_UUID","e69c7058-0451-11ee-8229-0242c0a84002","TYPE_NAME","Employer"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c7120-0451-11ee-8229-0242c0a84002","TYPE_CODE","PTNT","REF_TERM_UUID","e69c70e4-0451-11ee-8229-0242c0a84002","TYPE_NAME","Patient"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c71b6-0451-11ee-8229-0242c0a84002","TYPE_CODE","FAMDEP","REF_TERM_UUID","e69c718e-0451-11ee-8229-0242c0a84002","TYPE_NAME","Dependente"));
		data.add(fastCreateMap("CONCEPT_UUID","e69c7260-0451-11ee-8229-0242c0a84002","TYPE_CODE","RSIDT","REF_TERM_UUID","e69c7238-0451-11ee-8229-0242c0a84002","TYPE_NAME","Residente"));
		
		Path templateFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\relationshiptemplate.txt")
		        .toPath();
		Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\moz-mpi-relationship-types.dataset").toPath();
		
		Charset charset = StandardCharsets.UTF_8;
		
		for (Map<String, String> conceptSet : data) {
			String content = new String(Files.readAllBytes(templateFile), charset);
			
			content = content.replaceAll("CONCEPT_UUID", conceptSet.get("CONCEPT_UUID"));
			content = content.replaceAll("TYPE_CODE", conceptSet.get("TYPE_CODE"));
			content = content.replaceAll("REF_TERM_UUID", conceptSet.get("REF_TERM_UUID"));
			content = content.replaceAll("TYPE_NAME", conceptSet.get("TYPE_NAME"));
			
			FileUtilities.write(destFile.toString(), content.getBytes(charset));
		}
	}
	
	public static Map<String, String> fastCreateMap(String... params) {
		if (params.length % 2 != 0)
			throw new ForbiddenOperationException("The parameters for fastCreatMap must be pars <K1, V1>, <K2, V2>");
		
		Map<String, String> map = new HashMap<>();
		
		int paramsSize = params.length / 2;
		
		for (int set = 1; set <= paramsSize; set++) {
			int pos = set * 2 - 1;
			
			map.put(((String) params[pos - 1]), params[pos]);
		}
		
		return map;
	}
}
