package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;

import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CloneJsonNode {
	
	public static void main(String[] args) {
		String[] toReplicate = {
				"65cc8835-28ff-11f1-9614-36778d7f18da",
				"65cc7e4d-28ff-11f1-9614-36778d7f18da",
				"65cc97d7-28ff-11f1-9614-36778d7f18da",
				"65cc9829-28ff-11f1-9614-36778d7f18da",
				"65cc8324-28ff-11f1-9614-36778d7f18da",
				"65cc9759-28ff-11f1-9614-36778d7f18da",
				"65cc9704-28ff-11f1-9614-36778d7f18da",
				"65cc8a87-28ff-11f1-9614-36778d7f18da",
				"65cc89a2-28ff-11f1-9614-36778d7f18da",
				"65cc892f-28ff-11f1-9614-36778d7f18da",
				"65cc88a9-28ff-11f1-9614-36778d7f18da",
				"65cc7ee2-28ff-11f1-9614-36778d7f18da",
				"65cc83ad-28ff-11f1-9614-36778d7f18da",
				"65cc874a-28ff-11f1-9614-36778d7f18da",
				"65cc86db-28ff-11f1-9614-36778d7f18da",
				"65cc87bc-28ff-11f1-9614-36778d7f18da",
				"65cc8423-28ff-11f1-9614-36778d7f18da",
				"65cc8496-28ff-11f1-9614-36778d7f18da",
				"65cc850b-28ff-11f1-9614-36778d7f18da",
				"65cc8582-28ff-11f1-9614-36778d7f18da",
				"65cc85f3-28ff-11f1-9614-36778d7f18da",
				"65cc8668-28ff-11f1-9614-36778d7f18da",
				"65cc7f6b-28ff-11f1-9614-36778d7f18da",
				"65cc8078-28ff-11f1-9614-36778d7f18da",
				"65cc80ef-28ff-11f1-9614-36778d7f18da",
				"65cc8168-28ff-11f1-9614-36778d7f18da",
				"65cc8236-28ff-11f1-9614-36778d7f18da",
				"65cc82af-28ff-11f1-9614-36778d7f18da"
		};
		
		String toClonFrom = "emergency.program.uuid";
		
		System.out.println(replicateMapping(toClonFrom, toReplicate));
	}
	
	public static String replicateMapping(String mappingToReplicate, String[] replicas) {
		
		try {
			
		
			ObjectMapper mapper = new ObjectMapper();
			
			String json = FileUtilities.realAllFileAsString(new File("/home/jpboane/prg/php/workspace/misau/bkps/mapping.json"));
			
			JsonNode root = mapper.readTree(json);
			JsonNode mapArray = root.get("map");
			
			if (mapArray == null || !mapArray.isArray()) {
				throw new RuntimeException("Invalid mapping structure: 'map' not found");
			}
			
			JsonNode originalMapping = null;
			
			for (JsonNode node : mapArray) {
				if (mappingToReplicate.equals(node.get("mapping").asText())) {
					originalMapping = node;
					break;
				}
			}
			
			if (originalMapping == null) {
				throw new RuntimeException("Mapping not found: " + mappingToReplicate);
			}
			
			ArrayNode resultArray = mapper.createArrayNode();
			
			// 🔹 2. Criar replicas
			for (String replica : replicas) {
				
				ObjectNode newNode = mapper.createObjectNode();
				
				newNode.put("mapping", replica);
				
				// copia profunda do array mapped
				newNode.set("mapped", originalMapping.get("mapped").deepCopy());
				
				resultArray.add(newNode);
			}
			
			// 🔹 3. Retornar como JSON string (sem colchetes externos, se quiser igual ao exemplo)
			String fullJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
			
			// opcional: remover [ ] para ficar como no teu exemplo
			return fullJson.substring(1, fullJson.length() - 1).trim();
			
		}
		catch (Exception e) {
			throw new RuntimeException("Error replicating mapping", e);
		}
	}
}
