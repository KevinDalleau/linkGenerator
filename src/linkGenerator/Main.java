package linkGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;


public class Main {

	public static void main(String[] args) throws IOException {
		Query query = new Query();
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
			String gene = pair[0];
			String drug = pair[1];
			String geneEntrezId = query.getEntrezId(gene); // Mapping from PharmGKB to Entrez ID
			System.out.println(geneEntrezId);
			HashMap<String, String> geneDiseasesLinks = query.getGeneDiseasesLinks(geneEntrezId);
			System.out.println(geneDiseasesLinks.toString());
			
			
		}
		reader.close();
	}

}
