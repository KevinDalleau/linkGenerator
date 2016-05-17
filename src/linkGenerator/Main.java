package linkGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.com.bytecode.opencsv.CSVReader;
import fr.kevindalleau.Mapper.Mapper;


public class Main {

	public static void main(String[] args) throws IOException {
		Query query = new Query();
		Mapper mapper = new Mapper();
		Reader notLinkedFile = null;
		String file = args[0]; // File to parse
		
		/* File handling */
		
		try {
			notLinkedFile = new FileReader("./"+file+".tsv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CSVReader reader = new CSVReader(notLinkedFile,'\t');
		List<String[]> tsv = reader.readAll(); 
		
		/* Line by line computation */
		
		for(String[] pair : tsv) {
			System.out.println("New pair");
			String gene = pair[0];
			String drug = pair[1];
			
			///////////////////
			/* Gene handling */
			///////////////////
			
			String geneEntrezId = query.getEntrezId(gene); // Mapping from PharmGKB to Entrez ID
			//System.out.println(geneEntrezId);
			
			/* Gene attributes */
			ArrayList<String> geneAttributes = query.getGeneAttributes(geneEntrezId);
			//System.out.println("Gene Attributes "+geneAttributes.toString());
			
			/* Links from genes to diseases */
			HashMap<String, String> geneDiseasesLinks = query.getGeneDiseasesLinks(geneEntrezId);
			//System.out.println(geneDiseasesLinks.toString());
			
			///////////////////
			/* Drug handling */
			///////////////////
			
			ArrayList<String> drugStitchIds = query.drugStitchId(drug);
			String drugUMLS = mapper.getUMLS_from_PharmGKB(drug);
			System.out.println("Drug "+drug);
			System.out.println("UMLS id for this drug :"+ drugUMLS);
			
			/* Drug attributes */
			ArrayList<String> drugAttributes = query.getAtcCodes(drug);
			System.out.println(drug);
			System.out.println("Drug attributes :"+drugAttributes.toString());
			
			
			/* Links from drugs to diseases */
			
			HashMap<String,String> drugDiseasesLinks = new HashMap<String,String>(); // If a common key (i.e a disease) is found, the other is replaced, but it's not a problem here

			if(drugStitchIds != null) {
				HashMap<String,String> drugDiseasesLinksSider = query.getDrugDiseaseRelationsFromSider(drugStitchIds);
				drugDiseasesLinks.putAll(drugDiseasesLinksSider);
				//System.out.println(drugStitchIds.toString());
				//System.out.println(drugDiseasesLinks.toString());
			}
			if(drugUMLS != null) {
				HashMap<String,String> drugDiseasesLinksMedispan = query.getDrugDiseaseRelationsFromMedispan(drugUMLS);
				drugDiseasesLinks.putAll(drugDiseasesLinksMedispan);
			}
			System.out.println(drugDiseasesLinks.toString());

			///////////////////////
			/* Diseases handling */
			///////////////////////
			
			
			HashMap<String,String> diseaseGlobalMap = new HashMap<String,String>();
			diseaseGlobalMap = (HashMap<String, String>) Stream.of(geneDiseasesLinks, drugDiseasesLinks).flatMap(m -> m.entrySet().stream())
				       .collect(Collectors.toMap(Entry::getKey, Entry::getValue,(link1, link2) -> {
			                 System.out.println("duplicate key found!"+link1+link2);
			                 return link1;
			             }));
			System.out.println("Diseases linked :"+ diseaseGlobalMap.toString());
		}
		reader.close();
	}

}
