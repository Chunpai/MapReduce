/* This program will use read the data "products.csv", and each line of file is a product. 
 * We are trying to find products with very similar names that can be merged.
 * First, it uses the k-shingles set to represent the product names.
 * Second, it implements the min-hashing to construct signature matrix of all products name.
 * Third, LSH.
 */

import java.util.*;
import java.io.*;
import java.lang.*;


public class MergeProduct{
    public static void main(String[] args){
        String file_name = "products.csv";
        int k = 5;
        int permutations = 50;  // number of hash functions to do min-hashing
        int bands = 10;
        int rows = permutations / bands;
        try{
            FileReader fr = new FileReader(file_name);
            BufferedReader br = new BufferedReader(fr); 
            String line = null;
            LinkedHashSet shingles_set = new LinkedHashSet();
            HashMap doc_map = new HashMap();
            int count = 0;
            ArrayList lines = new ArrayList();
            while((line = br.readLine()) != null){
                //System.out.println(line);
                lines.add(line);
                LinkedHashSet shingles = shingle(line, k);
                //System.out.println(shingles);
                shingles_set.addAll(shingles);   //merge the shingles
                count++;
                doc_map.put(count, shingles);           //add shingles into a document set.
                /*test
                if (count == 4){
                    System.out.println(shingles_set);
                    System.out.println(doc_map);
                    System.out.println(shingles_set.size());
                    System.out.println(doc_map.size());
                    System.exit(0); 
                }
                */
            }
            br.close();
            fr.close();           
            HashMap index_map = generateIntVector(shingles_set, doc_map);
            //System.out.println(index_map.get(1));
            HashMap sig_map =  minHash(index_map, shingles_set, permutations);
            lsh(sig_map,bands,rows,lines);
            /*
            System.out.println(shingles_set.size());
            System.out.println(doc_map.size());
            System.out.println(doc_map.get(2173));
            */

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    
    //input: a line represents a product name, k is the number of shingle
    //output: return a k-shingles LinkedHashSet (preserved order of set).
    private static LinkedHashSet shingle(String line, int k){
        char[] charArray = line.toLowerCase().toCharArray();   //remove white spaces will get more shingles, why?, set lower case, convert String to char[]
        int size = charArray.length;
        LinkedHashSet shingles = new LinkedHashSet();
        for (int i=0; i <= size-k; i++){
            String s = new String( (char[]) Arrays.copyOfRange(charArray, i, k+i) );
            shingles.add(s);
        }
        return shingles;
    }

    
    
    
    //randomly generate a list of index list from 1 to size of shingles 
    //input: number of permutations
    //output: signature matrix
    private static HashMap generateIntVector(LinkedHashSet shingles_set, HashMap doc_map){
        HashMap shingles_map = new HashMap();
        Iterator iterator = shingles_set.iterator();
        int count = 0;
        while (iterator.hasNext()){
            count++;
            shingles_map.put(iterator.next(), count);
        }
        //System.out.println(shingles_map);
        //List<String> shingles_list = new ArrayList<String>(shingles_set);
        //System.out.println(shingles_list.get(0));
        //list_size = shingles_list.size();
        HashMap index_map = new HashMap();   //signature map, line number -> a HashSet consist of indices of rows have 1.
        int doc_size = doc_map.size();

        for(int i=1; i <= doc_size; i++){
            //System.out.println( ((HashSet) doc_map.get(i)).size());
            Iterator iter = ((HashSet) doc_map.get(i)).iterator();
            HashSet index_set = new HashSet();
            while(iter.hasNext()){
                index_set.add( shingles_map.get(iter.next()) );
            }
            index_map.put(i, index_set);
        }
        return index_map;
    }
    

    //return a signature map to represent the signature matrix.
    // key is the column index of document(line), value is the signature vector of that column.
    private static HashMap minHash(HashMap index_map, HashSet shingles_set, int permutations){
        HashMap sig_map = new HashMap();
        int set_size = shingles_set.size();
        int map_size = index_map.size();
        for(int i=1; i<= map_size;i++){
            ArrayList sig_vector = new ArrayList();
            sig_map.put(i, sig_vector);
        }
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i=1; i<= set_size; i++)
            numbers.add(i);
        for(int p=1; p <= permutations; p++){
            Collections.shuffle(numbers);
            for (int j=1; j <= map_size; j++){
                HashSet index_set = (HashSet) index_map.get(j);
                for (int k=1; k<= set_size; k++){
                    if( index_set.contains(numbers.get(k)) ){
                        ((ArrayList) sig_map.get(j)).add(numbers.get(k));
                        break;
                    }
                }
            }
        }
        return sig_map;
    }
    
    
    //split the signature into bands
    private static void lsh(HashMap sig_map, int bands, int rows, ArrayList lines){
        //System.out.println(sig_map.get(1));
        HashMap sig_band_map = new HashMap();
        int map_size = sig_map.size();
        for(int i= 1; i <= map_size; i++){
            HashMap band_map = new HashMap();
            ArrayList sig_vector = (ArrayList) sig_map.get(i);
            for(int j = 0; j< bands; j++){
                ArrayList band_vector = new ArrayList();
                for(int k=0; k < rows; k++){
                    band_vector.add(sig_vector.get(k+j*rows));
                }
                band_map.put(j, band_vector);
            }
            sig_band_map.put(i, band_map);
        }
        //System.out.println(sig_band_map.get(1)); 
             
        
        
        
        
        try{
            File file = new File("candidate_pairs.txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
                    
            for (int i=0; i< bands;i++){
                bw.write("Band: "+ String.valueOf(i));
                bw.write("\n------------------------------------------------------------");
                HashMap bucket_map = new HashMap();
                for(int j=1; j<= map_size; j++){
                    HashMap band_map = (HashMap) sig_band_map.get(j);
                    int bucket_number = hash_function( (ArrayList) band_map.get(i) );
                    if(bucket_map.containsKey(bucket_number)){
                        ((ArrayList)bucket_map.get(bucket_number)).add(j);
                    }
                    else{
                        ArrayList columns_list = new ArrayList();
                        columns_list.add(j);
                        bucket_map.put(bucket_number, columns_list);
                    }
                }
                Iterator iter = bucket_map.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry me = (Map.Entry) iter.next();
                    int bucket_size = ((ArrayList) me.getValue()).size();
                    if( bucket_size >= 2){
                        bw.write("\n");
                        for(int b=0; b< bucket_size; b++){
                            //System.out.println(((ArrayList) me.getValue()).get(b));
                            int number = (Integer) ((ArrayList) me.getValue()).get(b);
                            //System.out.println(number); 
                            bw.write((String) lines.get(number-1));
                            bw.write("\n");
                        }
                    }
                }
                bw.write("\n\n");
            }
        }catch(IOException ioe ){
            ioe.printStackTrace();
        }
    }


    private static int hash_function(ArrayList band_rows){
        int rows = band_rows.size();
        int number = 0;
        for(int i= 0; i < rows; i++){
            number += (i+531)* (Integer) band_rows.get(i);
        }
        number += 54321;
        return number % 10000000;
    }
       
    
    /*
    private void merge(){


    } 
    */   

}

