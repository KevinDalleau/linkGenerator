package linkGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.com.bytecode.opencsv.CSVReader;
import fr.kevindalleau.Mapper.Mapper;


public class Main {

	private static String[] values;

	public static void main(String[] args) throws IOException {
		Query query = new Query();
		Mapper mapper = new Mapper();
		Reader notLinkedFile = null;
		ArrayList<String[]> output = new ArrayList<String[]>();
		
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
			ArrayList<String[]> outputPair = new ArrayList<String[]>(); // Each element is a row of attributes for this pair
			String gene = pair[0];
			String drug = pair[1];
			
			///////////////////
			/* Gene handling */
			///////////////////
			
			String geneEntrezId = query.getEntrezId(gene); // Mapping from PharmGKB to Entrez ID
			//System.out.println(geneEntrezId);
			
			/* Gene attributes */
			ArrayList<String> geneAttributes = query.getGeneAttributes(geneEntrezId);
			
			/* Links from genes to diseases */
			HashMap<String, ArrayList<String>> geneDiseasesLinks = query.getGeneDiseasesLinks(geneEntrezId);
			
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
//			System.out.println("Drug attributes :"+drugAttributes.toString());
			
			
			/* Links from drugs to diseases */
			
			HashMap<String,String> drugDiseasesLinks = new HashMap<String,String>(); // If a common key (i.e a disease) is found, the other is replaced, but it's not a problem here

			if(drugStitchIds != null) {
				HashMap<String,String> drugDiseasesLinksSider = query.getDrugDiseaseRelationsFromSider(drugStitchIds);
				drugDiseasesLinks.putAll(drugDiseasesLinksSider);
				
			if(drugUMLS != null) {
				HashMap<String,String> drugDiseasesLinksMedispan = query.getDrugDiseaseRelationsFromMedispan(drugUMLS);
				drugDiseasesLinks.putAll(drugDiseasesLinksMedispan);
			}

			///////////////////////
			/* Diseases handling */
			///////////////////////
			
			
//			HashMap<String,String> diseaseGlobalMap = new HashMap<String,String>();
//			diseaseGlobalMap = (HashMap<String, String>) Stream.of(geneDiseasesLinks, drugDiseasesLinks).flatMap(m -> m.entrySet().stream())
//				       .collect(Collectors.toMap(Entry::getKey, Entry::getValue,(link1, link2) -> {
//			                 System.out.println("duplicate key found!"+link1+link2);
//			                 return link1+","+link2;
//			             }));
//			
//			System.out.println(geneDiseasesLinks.toString());
			
			HashMap<String, ArrayList<String[]>> diseasesLinks = new HashMap<String,ArrayList<String[]>>();
			for(String disease : drugDiseasesLinks.keySet()) {
				ArrayList<String[]> connections = new ArrayList<String[]>();
				String[] values = new String[2];
				values[0] = "N,A";
				values[1] = drugDiseasesLinks.get(disease);
				connections.add(values);
				diseasesLinks.put(disease, connections);
			}
			
			for(String disease : geneDiseasesLinks.keySet()) {
//				ArrayList<String[]> connections = new ArrayList<String[]>();
				if(diseasesLinks.containsKey(disease)){
					ArrayList<String[]> connections = diseasesLinks.get(disease);
					String[] initialValue = diseasesLinks.get(disease).get(0);
					diseasesLinks.get(disease).remove(0);
					for(String thl1 : geneDiseasesLinks.get(disease)) {
						String[] values = new String[2];
						values[0] = thl1;
						values[1] = initialValue[1];
						connections.add(values);
					}
					
					diseasesLinks.put(disease, connections);
				}
				else {
					ArrayList<String[]> connections = new ArrayList<String[]>();
					for(String thl1 : geneDiseasesLinks.get(disease)) {
						String[] values = new String[2];
						values[0] = thl1;
						values[1] = "N.A";
						connections.add(values);
					}
					diseasesLinks.put(disease, connections);
				}
			}
				
//			for(String[] rel : diseasesLinks.get("C0003811")) {
//				System.out.println(rel[0]+" "+rel[1]);
//			}
 			
			
			//////////////////////////
			/* 1-hop links handling */
			//////////////////////////
			
			String geneUniprotID = query.getUniprotId(gene);
			String drugDBID = query.getDrugBankID(drug);
			ArrayList<String> one_hops_links = query.getGeneDrugLinks(geneUniprotID, drugDBID);
			
			//////////////////////////
			/* Output handling *//////
			//////////////////////////
			
			String id = gene+"-"+drug;			
			

		    LinkedList<List <String>> lists = new LinkedList<List <String>>();
		    if(geneAttributes.isEmpty()) {
		    	geneAttributes.add("NA");
		    }
		    if(drugAttributes.isEmpty()) {
		    	drugAttributes.add("NA");
		    }
		    if(one_hops_links.isEmpty()) {
		    	one_hops_links.add("NA");
		    }
//		    ArrayList<String> geneDiseaseLinksList = new ArrayList<String>();
//		    for(String disease : geneDiseasesLinks.keySet()) {
//		    	geneDiseaseLinksList.add(geneDiseasesLinks.get(disease)+","+disease);
//		    }
//		    if(geneDiseaseLinksList.isEmpty()) {
//		    	geneDiseaseLinksList.add("NA,NA");
//		    }
//		    
//		    ArrayList<String> drugDiseaseLinksList = new ArrayList<String>();
//		    for(String disease : drugDiseasesLinks.keySet()) {
//		    	drugDiseaseLinksList.add(disease+","+drugDiseasesLinks.get(disease));
//		    }
//		    if(drugDiseaseLinksList.isEmpty()) {
//		    	drugDiseaseLinksList.add("NA,NA");
//		    }
		    
		    ArrayList<String> two_hops_links = new ArrayList<String>();
		    for(String disease : diseasesLinks.keySet()) {
		    	String toBeAdded = disease;
		    	for(String[] relations : diseasesLinks.get(disease)) {
		    		toBeAdded+=","+relations[0]+","+relations[1];
		    		two_hops_links.add(toBeAdded);
		    	}
		    }
		    
		    
		    lists.add(geneAttributes);
		    lists.add(drugAttributes);
		    lists.add(one_hops_links);
		    lists.add(two_hops_links);
		    
		    System.out.println("GA"+geneAttributes);
		    System.out.println("DA"+drugAttributes);
		    System.out.println("OHL"+one_hops_links);
		    System.out.println("THL"+two_hops_links);


		    Set<String> combinations = new TreeSet<String>();
		    Set<String> newCombinations;
//
		    for (String s: lists.removeFirst()) {
		        combinations.add(s);
		    }

		    while (!lists.isEmpty()) {
		        List<String> next = lists.removeFirst();
		        newCombinations =  new TreeSet<String>();
		        for (String s1: combinations) 
		            for (String s2 : next) 
		              newCombinations.add(s1+","+s2);               

		        combinations = newCombinations;
		    }
		    for (String s: combinations) {
		        System.out.println(s+" ");    
		    }
			
			
			
		}
		reader.close();
	}

}
}
