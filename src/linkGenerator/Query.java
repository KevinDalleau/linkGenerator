package linkGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;


public class Query {
	
	private String stitchValues;
	
	public String getEntrezId(String gene) {
		String query = "SELECT ?gene ?entrez_id\n" + 
				"WHERE {\n" + 
				" ?gene_uri <http://biodb.jp/mappings/to_entrez_id> ?entrez_id_uri.\n" + 
				" FILTER regex(str(?gene_uri), \"^http://biodb.jp/mappings/pharmgkb_id/"+gene+"\")\n" + 
				" BIND(REPLACE(str(?gene_uri), \"^http://biodb.jp/mappings/pharmgkb_id/\",\"\") AS ?gene)\n" + 
				" BIND(REPLACE(str(?entrez_id_uri), \"^http://biodb.jp/mappings/entrez_id/\",\"\") AS ?entrez_id)\n" + 
				"}";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", query);
		queryExec.addParam("timeout", "3600000");
		ResultSet entrezIdResultSet = queryExec.execSelect();
		QuerySolution entrezIdSolution = entrezIdResultSet.nextSolution();
		String entrezId = entrezIdSolution.get("entrez_id").toString();
		return entrezId;
	}
	
	public String getDrugBankID(String drugPharmGKBID) {
		String drugbankID= "";

		String query="SELECT ?drugbank_id\n" + 
				"WHERE { \n" + 
				"<http://biodb.jp/mappings/pharmgkb_id/"+drugPharmGKBID+"> <http://biodb.jp/mappings/to_drugbank_id> ?drugbank_id_uri\n" + 
				"FILTER regex(str(?drugbank_id_uri), \"http://biodb.jp/mappings/drugbank_id/\") \n" + 
				"BIND(REPLACE(str(?drugbank_id_uri), \"http://biodb.jp/mappings/drugbank_id/\",\"\") AS ?drugbank_id) \n" + 
				"}";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", query);
		queryExec.addParam("timeout", "3600000");
		ResultSet drugBankIDResultSet = queryExec.execSelect();
		while(drugBankIDResultSet.hasNext()) {
			QuerySolution drugBankIDSolution = drugBankIDResultSet.nextSolution();
			drugbankID= drugBankIDSolution.get("drugbank_id").toString();
		}
		return drugbankID;
	}
	
	
	public String getUniprotId(String genePharmGKBID) {
		String output = "";
		String queryLinks = "PREFIX biodb: <http://biodb.jp/mappings/>\n" + 
				"SELECT DISTINCT ?uniprot_id\n" + 
				"WHERE {\n" + 
				"<http://biodb.jp/mappings/pharmgkb_id/"+genePharmGKBID+"> biodb:to_uniprot_id ?uniprot_id_uri.\n" + 
				"BIND(replace(str(?uniprot_id_uri),\"http://biodb.jp/mappings/uniprot_id/\",\"\") AS ?uniprot_id)\n" + 
				"}\n" + 
				"\n" + 
				"LIMIT 10\n" + 
				"\n" + 
				"\n" + 
				"";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
		queryExec.addParam("timeout", "3600000");
		ResultSet uniProtIDResultSet = queryExec.execSelect();
		while(uniProtIDResultSet.hasNext()) {
			QuerySolution uniprotIDSolution = uniProtIDResultSet.nextSolution();
			output = uniprotIDSolution.get("uniprot_id").toString();
		}
		
		return output;
	}
	
	public ArrayList<String> getGeneAttributes(String geneEntrezId) {
		ArrayList<String> output = new ArrayList();
		String query = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"			PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
				"			PREFIX  ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
				"			PREFIX  sio:  <http://semanticscience.org/resource/>\n" + 
				"			PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
				"			PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
				"			PREFIX  wp:   <http://vocabularies.wikipathways.org/wp#>\n" + 
				"			PREFIX  void: <http://rdfs.org/ns/void#>\n" + 
				"			PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"			PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
				"			PREFIX  dcterms: <http://purl.org/dc/terms/>\n" + 
				"			SELECT DISTINCT ?attributes\n" + 
				"			WHERE {\n" + 
				"			 {\n" + 
				"			  ?gda sio:SIO_000253 ?source .\n" + 
				"			   ?source <http://www.w3.org/ns/prov#wasGeneratedBy> <http://purl.obolibrary.org/obo/ECO_0000218>. #Données CURATED\n" + 
				"			   ?gda sio:SIO_000628 <http://identifiers.org/ncbigene/"+geneEntrezId+">.\n" + 
				"               \n" + 
				"			   <http://identifiers.org/ncbigene/"+geneEntrezId+"> rdf:type ncit:C16612 .\n" + 
				"			  <http://identifiers.org/ncbigene/"+geneEntrezId+"> sio:SIO_000062 ?pathway_id .\n" + 
				"			  	?pathway_id dcterms:title ?attributes .\n" + 
				"			 }\n" + 
				"\n" + 
				"			 UNION {\n" + 
				"			?gda sio:SIO_000253 ?source .\n" + 
				"			   ?source <http://www.w3.org/ns/prov#wasGeneratedBy> <http://purl.obolibrary.org/obo/ECO_0000218>. #Données CURATED\n" + 
				"			   ?gda sio:SIO_000628 <http://identifiers.org/ncbigene/"+geneEntrezId+">.\n" + 
				"			   <http://identifiers.org/ncbigene/"+geneEntrezId+"> rdf:type ncit:C16612 .\n" + 
				"			  	<http://identifiers.org/ncbigene/"+geneEntrezId+"> sio:SIO_000095 ?class_id .\n" + 
				"			   ?class_id dcterms:title ?attributes.\n" + 
				"			}\n" + 
				"			 }\n" + 
				"\n";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", query);
		queryExec.addParam("timeout", "3600000");
		ResultSet geneAttributesRS = queryExec.execSelect();
		while(geneAttributesRS.hasNext()) {
			QuerySolution solution = geneAttributesRS.next();
			String attribute = solution.get("attributes").toString();
			output.add(attribute);
		}
		return output;
	}
	
	public ArrayList<String> drugStitchId(String drug) {
		ArrayList<String> output = new ArrayList();
		String query = "SELECT DISTINCT ?stitch_id\n" + 
				"	WHERE {\n" + 
				"	?drug_uri <http://biodb.jp/mappings/to_c_id> ?stitch_id_uri.\n" + 
				"	FILTER regex(str(?drug_uri), \"^http://biodb.jp/mappings/pharmgkb_id/"+drug+"\")\n" + 
				"	BIND(REPLACE(str(?drug_uri), \"^http://biodb.jp/mappings/pharmgkb_id/\",\"\") AS ?drug)\n" + 
				"	BIND(REPLACE(str(?stitch_id_uri), \"^http://biodb.jp/mappings/c_id/\",\"\") AS ?stitch_id)\n" + 
				"	}\n" + 
				"";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", query);
		queryExec.addParam("timeout", "3600000");
		ResultSet drugStitchIdsRS = queryExec.execSelect();
		while(drugStitchIdsRS.hasNext()) {
			QuerySolution solution = drugStitchIdsRS.next();
			String stitch = solution.get("stitch_id").toString();
			output.add(stitch);	
		}
		return output;
	}
	
	public ArrayList<String> getAtcCodes(String drugPharmgGKB_id) {
		ArrayList<String> output = new ArrayList<String>();
		if(drugPharmgGKB_id !=null) {
			String queryLinks = "PREFIX void: <http://rdfs.org/ns/void#> \n" + 
					"PREFIX dv: <http://bio2rdf.org/bio2rdf.dataset_vocabulary:> \n" + 
					"SELECT ?atc\n" + 
					"WHERE { \n" + 
					"<http://bio2rdf.org/pharmgkb:"+drugPharmgGKB_id+"> <http://bio2rdf.org/pharmgkb_vocabulary:x-drugbank> ?drugbank_uri.\n" + 
					"BIND(replace(str(?drugbank_uri), \"http://bio2rdf.org/drugbank:\", \"\") AS ?drugbank_id)\n" + 
					"?drugbank_uri <http://bio2rdf.org/drugbank_vocabulary:x-atc> ?atc_uri\n" + 
					"BIND(replace(str(?atc_uri), \"http://bio2rdf.org/atc:\", \"\") AS ?atc) \n" + 
					"}";
			QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
			queryExec.addParam("timeout","3600000");
			ResultSet drugATCRS = queryExec.execSelect();
			while(drugATCRS.hasNext()) {
				QuerySolution solution = drugATCRS.next();
				String atc = solution.get("atc").toString();
				output.add(atc);	
			}
			return output;
			
		}

		else return null;
		}
	
	public HashMap<String,String> getGeneDiseasesLinks(String geneEntrezId) {
		HashMap<String, String> output = new HashMap();
		String query = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
				"PREFIX  ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
				"PREFIX  sio:  <http://semanticscience.org/resource/>\n" + 
				"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX  wp:   <http://vocabularies.wikipathways.org/wp#>\n" + 
				"PREFIX  void: <http://rdfs.org/ns/void#>\n" + 
				"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
				"PREFIX  dcterms: <http://purl.org/dc/terms/>\n" + 
				"\n" + 
				"SELECT DISTINCT ?gene ?disease ?2_hops_links_1\n" + 
				"WHERE {\n" + 
				"{\n" + 
				"?a <http://bio2rdf.org/clinvar_vocabulary:assertion> ?2_hops_links_1 .\n" + 
				"?a <http://bio2rdf.org/clinvar_vocabulary:Variant_Gene> ?variant_gene .\n" + 
				"?variant_gene <http://bio2rdf.org/clinvar_vocabulary:x-gene> ?gene_bio .\n" + 
				"FILTER regex(str(?gene_bio),\"http://bio2rdf.org/ncbigene:"+geneEntrezId+"\").\n" + 
				"\n" + 
				"?a <http://bio2rdf.org/clinvar_vocabulary:Variant_Phenotype> ?phenotype .\n" + 
				"?phenotype <http://bio2rdf.org/clinvar_vocabulary:x-medgen> ?disease_bio\n" + 
				"      BIND(REPLACE(str(?gene_bio), \"http://bio2rdf.org/ncbigene:\", \"\") AS ?gene)\n" + 
				"      BIND(REPLACE(str(?disease_bio), \"http://bio2rdf.org/medgen:c\",\"C\") AS ?disease)\n" + 
				"}\n" + 
				"\n" + 
				"UNION {\n" + 
				"?a <http://bio2rdf.org/clinvar_vocabulary:Variant_Gene> ?variant_gene .\n" + 
				"?variant_gene <http://bio2rdf.org/clinvar_vocabulary:x-gene> ?gene_bio .\n" + 
				"FILTER regex(str(?gene_bio),\"http://bio2rdf.org/ncbigene:"+geneEntrezId+"\").\n" + 
				"?variant_gene <http://bio2rdf.org/clinvar_vocabulary:x-sequence_ontology> ?2_hops_links_1 .\n" + 
				"?a <http://bio2rdf.org/clinvar_vocabulary:Variant_Phenotype> ?phenotype .\n" + 
				"?phenotype <http://bio2rdf.org/clinvar_vocabulary:x-medgen> ?disease_bio\n" + 
				"      BIND(REPLACE(str(?gene_bio), \"http://bio2rdf.org/ncbigene:\", \"\") AS ?gene)\n" + 
				"      BIND(REPLACE(str(?disease_bio), \"http://bio2rdf.org/medgen:c\",\"C\") AS ?disease)\n" + 
				"}\n" + 
				"UNION {\n" + 
				"  ?gda sio:SIO_000253 ?source .\n" + 
				"    ?source <http://www.w3.org/ns/prov#wasGeneratedBy> <http://purl.obolibrary.org/obo/ECO_0000218>. #Données CURATED\n" + 
				"    ?gda sio:SIO_000628 ?gene_uri .\n" + 
				"FILTER regex(str(?gene_bio),\"http://bio2rdf.org/ncbigene/"+geneEntrezId+"\").\n" + 
				"    ?gda sio:SIO_000628 ?disease_uri .\n" + 
				"    ?gda sio:SIO_000001 ?a .\n" + 
				"    ?a skos:exactMatch ?2_hops_links_1 .\n" + 
				"    ?gene_uri rdf:type ncit:C16612 .\n" + 
				"    ?gene_uri sio:SIO_000062 ?pathway_id .\n" + 
				"    #?pathway_id foaf:name ?pathway .\n" + 
				"    #?gene_uri sio:SIO_000095 ?class_id .\n" + 
				"    #?class_id foaf:name ?class .\n" + 
				"    ?disease_uri rdf:type ncit:C7057\n" + 
				"    BIND((replace(str(?gene_uri), \"http://identifiers.org/ncbigene/\", \"\")) AS ?gene)\n" + 
				"    BIND((replace(str(?disease_uri), \"http://linkedlifedata.com/resource/umls/id/\", \"\")) AS ?disease)\n" + 
				"\n" + 
				"}\n" + 
				"}\n" +  
				"";
		QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", query);
		queryExec.addParam("timeout", "3600000");
		ResultSet gdLinksRS = queryExec.execSelect();
		while(gdLinksRS.hasNext()) {
			QuerySolution solution = gdLinksRS.next();
			String disease = solution.get("disease").toString();
			String linkLabel = solution.get("2_hops_links_1").toString();
			output.put(disease, linkLabel);
		}
		return output;
	}
	
	
	public HashMap<String,String> getDrugDiseaseRelationsFromSider(ArrayList<String> drug_Stitch_ids) {
		HashMap<String,String> output = new HashMap();
		stitchValues = "";
		for(int i=0; i < drug_Stitch_ids.size(); i++) {
			stitchValues += "(<http://bio2rdf.org/stitch:-".concat(Integer.toString((Integer.parseInt(drug_Stitch_ids.get(i))+100000000))).concat(">)");
		}
		
			String queryLinks = "SELECT DISTINCT ?stitch_id ?2_hops_links_2 ?disease\n" + 
					"WHERE {\n" + 
					"  {\n" + 
					"  VALUES(?stitch_id_uri) {\n" + 
					stitchValues +
					"    }\n" + 
					"  ?a <http://bio2rdf.org/sider_vocabulary:stitch-flat-compound-id> ?stitch_id_uri.\n" + 
					"  ?a <http://bio2rdf.org/sider_vocabulary:side-effect> ?disease_uri\n" + 
					"  BIND(\"side-effect\" AS ?2_hops_links_2)\n" + 
					"  BIND(REPLACE(str(?stitch_id_uri), \"http://bio2rdf.org/stitch:\",\"\") AS ?stitch_id)\n" + 
					"  BIND(REPLACE(str(?disease_uri), \"http://bio2rdf.org/umls:\",\"\") AS ?disease)\n" + 
					"  }\n" + 
					"  UNION {\n" + 
					"     VALUES(?stitch_id_uri) {\n" + 
					stitchValues +
					"     }\n" + 
					"  ?a <http://bio2rdf.org/sider_vocabulary:stitch-flat-compound-id> ?stitch_id_uri.\n" + 
					"  ?a <http://bio2rdf.org/sider_vocabulary:indication> ?disease_uri.\n" + 
					"  BIND(\"indication\" AS ?2_hops_links_2)\n" + 
					"  BIND(REPLACE(str(?stitch_id_uri), \"http://bio2rdf.org/stitch:\",\"\") AS ?stitch_id)\n" + 
					"  BIND(REPLACE(str(?disease_uri), \"http://bio2rdf.org/umls:\",\"\") AS ?disease)\n" + 
					"  }}\n" + 
					"";
				QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
				queryExec.addParam("timeout","3600000");
				ResultSet drugdisLinksRS = queryExec.execSelect();
				while(drugdisLinksRS.hasNext()) {
					QuerySolution solution = drugdisLinksRS.next();
					String disease = solution.get("disease").toString();
					String linkLabel = solution.get("2_hops_links_2").toString();
					output.put(disease, linkLabel);
				}
				return output;
		}
	
	public HashMap<String,String> getDrugDiseaseRelationsFromMedispan(String drugUMLS) {
		HashMap<String,String> output = new HashMap();
		String queryLinks = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT ?2_hops_links_2 ?disease\n" + 
				"WHERE {\n" + 
				"  <http://orpailleur.fr/medispan/"+drugUMLS+"> ?2_hops_links2_uri ?disease_uri.\n" + 
				"  #?drug_uri rdf:type <http://orpailleur.fr/medispan/drug>.\n" + 
				"  ?disease_uri rdf:type <http://orpailleur.fr/medispan/event>\n" + 
				"    BIND(REPLACE(str(?drug_uri), \"http://orpailleur.fr/medispan/\",\"\") AS ?drug)\n" + 
				"    BIND(REPLACE(str(?disease_uri), \"http://orpailleur.fr/medispan/\",\"\") AS ?disease)\n" + 
				"  BIND(REPLACE(str(?2_hops_links2_uri), \"http://orpailleur.fr/medispan/\", \"\") AS ?2_hops_links2)\n" + 
				"}";
				QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
				queryExec.addParam("timeout","3600000");
				ResultSet drugdisLinksRS = queryExec.execSelect();
				while(drugdisLinksRS.hasNext()) {
					QuerySolution solution = drugdisLinksRS.next();
					String disease = solution.get("disease").toString();
					String linkLabel = solution.get("2_hops_links_2").toString();
					output.put(disease, linkLabel);
				}
				return output;
		}
		
	
	
	
	public QueryEngineHTTP getDrugDiseaseRelations(String source) {
		if (source.equals("medispan")) {
			String queryLinks = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT ?drug ?2_hops_links2 ?disease\n" + 
				"WHERE {\n" + 
				"  ?drug_uri ?2_hops_links2_uri ?disease_uri.\n" + 
				"  ?drug_uri rdf:type <http://orpailleur.fr/medispan/drug>.\n" + 
				"  ?disease_uri rdf:type <http://orpailleur.fr/medispan/event>\n" + 
				"    BIND(REPLACE(str(?drug_uri), \"http://orpailleur.fr/medispan/\",\"\") AS ?drug)\n" + 
				"    BIND(REPLACE(str(?disease_uri), \"http://orpailleur.fr/medispan/\",\"\") AS ?disease)\n" + 
				"  BIND(REPLACE(str(?2_hops_links2_uri), \"http://orpailleur.fr/medispan/\", \"\") AS ?2_hops_links2)}\n";
			
			QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
			queryExec.addParam("timeout","3600000");
			return queryExec;
		}
		else if (source.equals("sider")) {
			String queryLinks = "SELECT ?stitch_id ?2_hops_links_2 ?disease\n" + 
					"					WHERE { \n" + 
					"					  { \n" + 
					"					   	?a <http://bio2rdf.org/sider_vocabulary:stitch-flat-compound-id> ?stitch_id_uri. \n" + 
					"					    ?a <http://bio2rdf.org/sider_vocabulary:side-effect> ?disease_uri\n" + 
					"     					  BIND(\"side-effect\" AS ?2_hops_links_2)\n" + 
					"					      BIND(REPLACE(str(?stitch_id_uri), \"http://bio2rdf.org/stitch:\",\"\") AS ?stitch_id) \n" + 
					"					      BIND(REPLACE(str(?disease_uri), \"http://bio2rdf.org/umls:\",\"\") AS ?disease) \n" + 
					"					  } \n" + 
					"					  UNION { \n" + 
					"					   ?a <http://bio2rdf.org/sider_vocabulary:stitch-flat-compound-id> ?stitch_id_uri. \n" + 
					"					   ?a <http://bio2rdf.org/sider_vocabulary:indication> ?disease_uri\n" + 
					"      					  BIND(\"indication\" AS ?2_hops_links_2)\n" + 
					"					      BIND(REPLACE(str(?stitch_id_uri), \"http://bio2rdf.org/stitch:\",\"\") AS ?stitch_id) \n" + 
					"    					  BIND(REPLACE(str(?disease_uri), \"http://bio2rdf.org/umls:\",\"\") AS ?disease) \n" + 
					"					  } \n" + 
					"					}\n";
			
			QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
			queryExec.addParam("timeout","3600000");
			return queryExec;
			
		}
		else {
			return null;
		}
	}
	
	public ArrayList<String> getDiseaseAttributes(String disease_UMLSID) {
		ArrayList<String> output = new ArrayList();
		if(disease_UMLSID !=null) {
			String queryLinks = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
					"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
					"PREFIX  ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
					"PREFIX  sio:  <http://semanticscience.org/resource/>\n" + 
					"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
					"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
					"PREFIX  wp:   <http://vocabularies.wikipathways.org/wp#>\n" + 
					"PREFIX  void: <http://rdfs.org/ns/void#>\n" + 
					"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
					"PREFIX  dcterms: <http://purl.org/dc/terms/>\n" + 
					"SELECT DISTINCT ?attributes\n" + 
					"WHERE {\n" + 
					" {\n" + 
					"  ?gda sio:SIO_000253 ?source .\n" + 
					"   ?source <http://www.w3.org/ns/prov#wasGeneratedBy> <http://purl.obolibrary.org/obo/ECO_0000218>. #Données CURATED\n" + 
					"   <http://linkedlifedata.com/resource/umls/id/"+disease_UMLSID+"> rdf:type ncit:C7057 .\n" + 
					"   <http://linkedlifedata.com/resource/umls/id/"+disease_UMLSID+"> sio:SIO_000008 ?attributes .\n" + 
					"	BIND(REPLACE(str(?disease_uri), \"http://linkedlifedata.com/resource/umls/id/\",\"\") AS ?disease)\n" + 
					" }\n" + 
					"	UNION {\n" + 
					"   ?gda sio:SIO_000253 ?source .\n" + 
					"   ?source <http://www.w3.org/ns/prov#wasGeneratedBy> <http://purl.obolibrary.org/obo/ECO_0000218>. #Données CURATED\n" + 
					"   <http://linkedlifedata.com/resource/umls/id/"+disease_UMLSID+"> rdf:type ncit:C7057 .\n" + 
					"   <http://linkedlifedata.com/resource/umls/id/"+disease_UMLSID+"> sio:SIO_000095 ?attributes_uri .\n" + 
					"   ?attributes_uri rdfs:isDefinedBy ?attributes.\n" + 
					"	 BIND(REPLACE(str(?disease_uri), \"http://linkedlifedata.com/resource/umls/id/\",\"\") AS ?disease)\n" + 
					" }\n" + 
					"	}\n" + 
					"";
			QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
			queryExec.addParam("timeout","3600000");
			ResultSet diseaseAttributesRS = queryExec.execSelect();
			while(diseaseAttributesRS.hasNext()) {
				QuerySolution solution = diseaseAttributesRS.next();
				String attributes = solution.get("attributes").toString();
				output.add(attributes);	
			}
	}
		return output;
}
	public ArrayList<String> getGeneDrugLinks(String geneUniprotId, String drugDBID) {
		ArrayList<String> output = new ArrayList<String>();
	
		if(geneUniprotId !="" && drugDBID != "") {
			String queryLinks = "PREFIX http: <http://www.w3.org/2011/http#>\n" + 
					"prefix owl: <http://www.w3.org/2002/07/owl#>\n" + 
					"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
					"SELECT DISTINCT ?target ?action\n" + 
					"WHERE {\n" + 
					"<http://bio2rdf.org/drugbank:"+drugDBID+"> <http://bio2rdf.org/drugbank_vocabulary:target> ?target_uri.\n" + 
					"?target_uri <http://bio2rdf.org/drugbank_vocabulary:x-uniprot> <http://bio2rdf.org/uniprot:"+geneUniprotId+">.\n" + 
					"?target_uri <http://bio2rdf.org/bio2rdf_vocabulary:identifier> ?target.\n" + 
					"BIND(uri(\"http://bio2rdf.org/drugbank_resource:"+drugDBID+"_\"+?target) AS ?relation_uri)\n" + 
					"?relation_uri <http://bio2rdf.org/drugbank_vocabulary:action> ?action_uri\n" + 
					"BIND(replace(str(?action_uri), \"http://bio2rdf.org/drugbank_vocabulary:\",\"\") AS ?action)\n" + 
					"}";
			QueryEngineHTTP queryExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:9999/blazegraph/namespace/kb/sparql", queryLinks);
			queryExec.addParam("timeout","3600000");
			ResultSet gdLinksRS = queryExec.execSelect();
			while(gdLinksRS.hasNext()) {
				QuerySolution solution = gdLinksRS.next();
				String action = solution.get("action").toString();
				output.add(action);	
			}
	}
		return output;
}
	
	
}
