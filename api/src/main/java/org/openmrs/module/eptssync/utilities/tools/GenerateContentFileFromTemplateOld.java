package org.openmrs.module.eptssync.utilities.tools;

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

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;

public class GenerateContentFileFromTemplateOld {
	
	public static final String templateFile = "D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\templates\\db_quick_copy_template.json";
	
	public static final String testSitesFilePath = "D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\testing\\sites.txt";
	
	public static void main(String[] args) throws IOException {
		generateIdefierTypes(args);
		
		//generateRelationship(args);
		
		//enerateIdefierTypesSQL(args);
	}
	
	public static void generateIdefierTypes(String[] args) throws IOException {
		
		String template = "";
		template += "<update insertIfNotExists=\"true\">";
		template += "		<ReferenceTerm xmlns=\"http://santedb.org/model\">";
		template += "			<id>REF_TERM_UUID</id>";
		template += "			<mnemonic>REF_TERM_MNEMONIC</mnemonic>";
		template += "			<codeSystem>7565c346-d862-11ed-b580-0242c0a8b002</codeSystem>";
		template += "		</ReferenceTerm>";
		template += "</update>";
		
		template += "	<update insertIfNotExists=\"true\">";
		template += "		<Concept xmlns=\"http://santedb.org/model\">";
		template += "			<id>CONCEPT_UUID</id>";
		template += "		</Concept>";
		template += "	</update>";
		
		template += "	<update insertIfNotExists=\"true\">";
		template += "      <ConceptReferenceTerm xmlns=\"http://santedb.org/model\">";
		template += "          <source>CONCEPT_UUID</source>";
		template += "          <term>REF_TERM_UUID</term>";
		template += "          <relationshipType>2c4dafc2-566a-41ae-9ebc-3097d7d22f4a</relationshipType>";
		template += "      </ConceptReferenceTerm>";
		template += "	</update>";
		
		template += "	<update insertIfNotExists=\"true\">";
		template += "		<IdentifierType xmlns=\"http://santedb.org/model\">";
		template += "			<id>ID_TYPE_UUID</id>";
		template += "			<scopeConcept>bacd9c6f-3fa9-481e-9636-37457962804d</scopeConcept>";
		template += "			<typeConcept>CONCEPT_UUID</typeConcept>";
		template += "		</IdentifierType>";
		template += "	</update>";
		
		List<Map<String, String>> data = new ArrayList<>();
		
		/*data.add(fastCreateMap("REF_TERM_MNEMONIC","OpenMRS_ID_NUM","REF_TERM_UUID","3fa2f0ec-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa2f18c-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa2f218-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NID_TARV","REF_TERM_UUID","3fa30a78-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa30b0e-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa30ba4-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","BI","REF_TERM_UUID","3fa30e06-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa30e9c-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa30f32-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","CDG_ATS","REF_TERM_UUID","3fa31108-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa3119e-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa3122a-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","CDG_PTV_PRE_NATAL","REF_TERM_UUID","3fa3140a-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa31496-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa3152c-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","CDG_ITS","REF_TERM_UUID","3fa316f8-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa31784-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa31810-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","CDG_PTV_MATERNIDADE","REF_TERM_UUID","3fa319d2-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa31a68-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa31afe-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NID_CCR","REF_TERM_UUID","3fa31cca-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa31d60-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa31dec-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","PCR_NUM_REG","REF_TERM_UUID","3fa31fb8-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa32044-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa320e4-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NIT_TB","REF_TERM_UUID","3fa3229c-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa32404-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa32490-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NUM_CANCRO_CERVICAL","REF_TERM_UUID","3fa32652-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa326de-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa32774-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NUIC","REF_TERM_UUID","3fa3292c-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa329c2-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa32a4e-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NID_DISA","REF_TERM_UUID","3fa32c06-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa32c9c-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa32d32-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","CRAM_ID","REF_TERM_UUID","3fa32ef4-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa32f8a-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa3300c-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","NID_PREP","REF_TERM_UUID","3fa331e2-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa3326e-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa33304-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","OpenEMPI_ID","REF_TERM_UUID","3fa334d0-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa33566-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa335f2-d878-11ed-b580-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC","OpenMRS_ID","REF_TERM_UUID","3fa337b4-d878-11ed-b580-0242c0a8b002","CONCEPT_UUID","3fa3384a-d878-11ed-b580-0242c0a8b002","ID_TYPE_UUID","3fa338e0-d878-11ed-b580-0242c0a8b002"));		   
		*/
		
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "OpenMRS_PATIENT_UUID", "REF_TERM_UUID",
		    "b1a6ae6a-d92f-11ed-922a-0242c0a8d002", "CONCEPT_UUID", "b1a6ae92-d92f-11ed-922a-0242c0a8d002", "ID_TYPE_UUID",
		    "b1a6aec4-d92f-11ed-922a-0242c0a8d002"));
		
		Path templateFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\idtypetemplate.txt")
		        .toPath();
		Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\idtypes.txt").toPath();
		
		Charset charset = StandardCharsets.UTF_8;
		
		for (Map<String, String> conceptSet : data) {
			String content = new String(Files.readAllBytes(templateFile), charset);
			
			content = content.replaceAll("REF_TERM_MNEMONIC", conceptSet.get("REF_TERM_MNEMONIC"));
			content = content.replaceAll("REF_TERM_UUID", conceptSet.get("REF_TERM_UUID"));
			content = content.replaceAll("CONCEPT_UUID", conceptSet.get("CONCEPT_UUID"));
			content = content.replaceAll("ID_TYPE_UUID", conceptSet.get("ID_TYPE_UUID"));
			
			//Files.write(destFile, content.getBytes(charset));
			
			//FileUtilities.write(destFile.toString(), content.getBytes(charset));
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
		Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\id_types.sql").toPath();
		
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
		
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "DTR", "REF_TERM_UUID", "74b459d0-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b45a7a-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "SPVR", "REF_TERM_UUID", "74b47582-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b4760e-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "HDNEIHBHD", "REF_TERM_UUID", "74b47870-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b47906-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "WKR", "REF_TERM_UUID", "74b47ae6-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b47b7c-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "OTHER", "REF_TERM_UUID", "74b47db6-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b47e4c-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "DPOWATT", "REF_TERM_UUID", "74b4800e-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b480a4-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "FRND", "REF_TERM_UUID", "74b4827a-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b48306-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "EMPLY", "REF_TERM_UUID", "74b484c8-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b4855e-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "PTNT", "REF_TERM_UUID", "74b48720-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b487ac-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "FAMDEP", "REF_TERM_UUID", "74b48978-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b48a04-d90c-11ed-9f54-0242c0a8b002"));
		data.add(fastCreateMap("REF_TERM_MNEMONIC", "RSIDT", "REF_TERM_UUID", "74b48bc6-d90c-11ed-9f54-0242c0a8b002",
		    "CONCEPT_UUID", "74b48c52-d90c-11ed-9f54-0242c0a8b002"));
		
		Path templateFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\relationshiptemplate.txt")
		        .toPath();
		Path destFile = new File("D:\\PROJECTOS\\FGH\\Centralizacao\\mpi\\santeMPI\\Datasets\\relationships.txt").toPath();
		
		Charset charset = StandardCharsets.UTF_8;
		
		for (Map<String, String> conceptSet : data) {
			String content = new String(Files.readAllBytes(templateFile), charset);
			
			content = content.replaceAll("REF_TERM_MNEMONIC", conceptSet.get("REF_TERM_MNEMONIC"));
			content = content.replaceAll("REF_TERM_UUID", conceptSet.get("REF_TERM_UUID"));
			content = content.replaceAll("CONCEPT_UUID", conceptSet.get("CONCEPT_UUID"));
			
			//FileUtilities.write(destFile.toString(), content.getBytes(charset));
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
