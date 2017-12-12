package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import au.com.bytecode.opencsv.CSVWriter;

public class TextFileHandler {

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static ArrayList<ArrayList<String>> readCSV (String fileName) {
		ArrayList<ArrayList<String>> sheetData = new ArrayList<ArrayList<String>>();
		BufferedReader br = null;
		
		try {
			 // create BufferedReader to read csv file containing data
            br = new BufferedReader(new FileReader(fileName));
            String strLine = "";

            StringTokenizer st = null;

            // read comma separated file line by line
            while ((strLine = br.readLine()) != null) {

                // break comma separated line using ","
                st = new StringTokenizer(strLine, ",");

                ArrayList<String> data = new ArrayList<String>();

                while (st.hasMoreTokens()) {
                    // display csv file
                    String editedValue = String.valueOf(st.nextToken()).replace("\"", "").trim();
                	//String editedValue = st.nextToken();
                    data.add(editedValue);
                }

                sheetData.add(data);
            }
            br.close();
		} catch (Exception e) {
			System.out.println("Exception while reading csv file " + fileName);
			return null;
		}
		
		return sheetData;
	}
	
	/**
	 * 
	 * @param filename
	 * @param header
	 * @param dataout
	 */
	public static void writeToCSV(String filename, String[] header, List<String[]> dataout) {
		CSVWriter csvWriter = null;
		try {
			Writer fw = new BufferedWriter(new FileWriter(filename));
			csvWriter = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
			
			csvWriter.writeNext(header);
			csvWriter.writeAll(dataout);

			csvWriter.flush();
		} catch (IOException e) {
			System.out.println("Failed to write content to file:" + filename);
		} finally {
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (IOException e) {
					System.out.println("Failed to close file after writing: " + filename);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param filename
	 * @param header
	 * @param dataout
	 */
	public static void writeToCSV(String filename, String[] header, List<String[]> dataout, boolean append) {
		CSVWriter csvWriter = null;
		try {
			Writer fw = new BufferedWriter(new FileWriter(filename, append));
			csvWriter = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
			
			csvWriter.writeNext(header);
			csvWriter.writeAll(dataout);

			csvWriter.flush();
		} catch (IOException e) {
			System.out.println("Failed to write content to file:" + filename);
		} finally {
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (IOException e) {
					System.out.println("Failed to close file after writing: ");
				}
			}
		}
	}
}
