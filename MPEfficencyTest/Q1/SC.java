import java.util.*;
import java.io.*;
import java.util.Hashtable;
import java.lang.*;

public class SC{
    public static void main(String[] args){
        String file_name = "data_inputs/BookReviewRating.txt";
        if(args.length == 0){
            System.out.println("You are using the whole dataset BookReviewRating.txt,\n since you don't pass input data file as argument !");
        }
        else{
            file_name = args[0];
            System.out.println("You are using the dataset : " + args[0]);
        }
        try{
            HashMap hmap = readAndHash(file_name);
            HashMap hmap_result = computeAndHash(hmap, args);
            System.out.println(hmap_result.size());
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    //function: readFile read file_path and return a hashtable with all at least ten reviews
    private static HashMap readAndHash(String input_file) throws IOException{
        //FileInputStream fis = new FileInputStream(fin);
        //construct BufferedReader from InputStreamReader
        //BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        FileReader fr = new FileReader(input_file);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        HashMap hmap  = new HashMap();                 // those two hashmap: key is book , value is a arraylist,  
        HashMap hmap_ten_reviews = new HashMap();      // first element is count of reviews, and second is sum of review rating.
        //int review_count_threshold = 2;
        //int review_count_threshold = 5;
        int review_count_threshold = 10;
        while((line = br.readLine()) != null){
            //System.out.println(line);
            String[] content = line.split(",");
            //System.out.println(content[0]);
            
            if (hmap_ten_reviews.get(content[0]) != null){
                //ArrayList<Double> ratings = (ArrayList<Double>) hmap_ten_reviews.get(content[0]);
                //ratings.add( Double.parseDouble( content[2] ));
                //hmap_ten_reviews.put(content[0], ratings);
                System.out.println( ((double[]) hmap_ten_reviews.get(content[0]))[0]) ;
                ((double[]) hmap_ten_reviews.get(content[0]))[0]++;
                System.out.println( ((double[]) hmap_ten_reviews.get(content[0]))[0]) ;
                ((double[]) hmap_ten_reviews.get(content[0]))[1] +=  Double.parseDouble(content[2]);
                //return hmap_ten_reviews;
                //countAndRate[0]++;               //count of review add 1
                //countAndRate[1] += content[2];   //sum of the review + current review. 
                //hmap_ten_reviews.put(content[0], countAndRate); //note average is not commulative and associate.
            }
            else if (hmap.get(content[0]) == null){
                //ArrayList<Double> ratings = new ArrayList<Double>();
                //ratings.add(Double.parseDouble(content[2]));     
                //hmap.put(content[0],ratings);
                double[] countAndRate = {0.0,0.0};
                countAndRate[0]++;
                countAndRate[1] += Double.parseDouble(content[2]);
                hmap.put(content[0],countAndRate);
            }
            else {
                /*
                ArrayList<Double> ratings = (ArrayList<Double>) hmap.get(content[0]);
                ratings.add( Double.parseDouble( content[2] ));  //must add first, and then hash seperately
                if ( ((ArrayList<Double>) hmap.get(content[0])).size() == 10 ){
                    hmap_ten_reviews.put(content[0],ratings) ;
                    //return hmap_ten_reviews;
                }
                else {
                    hmap.put(content[0], ratings);
                }
                */
                ((double[]) hmap.get(content[0]))[0]++;
                ((double[]) hmap.get(content[0]))[1] += Double.parseDouble(content[2]);
                if (((double[]) hmap.get(content[0]))[0] == review_count_threshold){
                    hmap_ten_reviews.put(content[0],(double[]) hmap.get(content[0]));
                    //return hmap_ten_reviews;
                }
            }
        }
        //fr.close();
        //br.close();    
        return hmap_ten_reviews;
    }

    
    //compute the average rating of books which received at least 10 reviews.
    //input: a hashmap consists of keys which are books received at least 10 reviews, and values ArrayLists of their ratings.
    //output: a hashmap that books rated at least 4.95 averagely. 
    private static HashMap computeAndHash(HashMap hmap, String[] args){
        HashMap hmap_result = new HashMap(); 
        String output_file = "SC_outputs/SC_whole2reviews.txt";
        if(args.length != 0){
            String[] path = args[0].split("/");
            output_file = "SC_outputs/SC_result_"+path[path.length -1];
        }

        try{
            File file = new File(output_file);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            //1. iterate over hash map *
            Iterator it = hmap.entrySet().iterator();
            double avg = 0;
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                avg = ((double [])pair.getValue())[1] / ((double[]) pair.getValue())[0];
                //System.out.println(((double [])pair.getValue())[0]);
                //System.out.println(((double [])pair.getValue())[1]);
                //if average is lower than threshold, it will be removed from the hashmap.
                if (avg >= 4.95){
                    //hmap.remove(pair.getKey()); //error ! cannot remove it while iterating it
                    hmap_result.put(pair.getKey(), avg); 
                    bw.write((String) pair.getKey());
                    bw.write("\n");
                    System.out.println(((double [])pair.getValue())[0]);
                    System.out.println(((double [])pair.getValue())[1]);
                    //return hmap_result;
                }
            }
            bw.close();
            fw.close();
            //1. end          
        }catch( IOException ioe  ){ 
            ioe.printStackTrace();  
        }    
        return hmap_result;
    }


}
    
    /*
    private static double average(ArrayList list){
        double sum = 0;
        int size = list.size();
        for (int i = 0; i < size; i++){
            sum += (Double) list.get(i);
        }
        double avg = sum / size ; 
        return avg; 
    }*/

