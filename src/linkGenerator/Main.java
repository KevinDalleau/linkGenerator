package linkGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
			
			/* Links from genes to disease */
			HashMap<String, String> geneDiseasesLinks = query.getGeneDiseasesLinks(geneEntrezId);
			//System.out.println(geneDiseasesLinks.toString());
			
			///////////////////
			/* Drug handling */
			///////////////////
			System.out.println("Drug "+drug);
			ArrayList<String> drugStitchIds = query.drugStitchId(drug);
			if(drugStitchIds != null) {
				System.out.println(drugStitchIds.toString());
			}
			
			
			
			
		}
		reader.close();
	}

}
