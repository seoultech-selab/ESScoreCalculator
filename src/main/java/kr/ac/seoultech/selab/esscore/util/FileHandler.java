package kr.ac.seoultech.selab.esscore.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import hk.ust.cse.pishon.esgen.model.Change;
import hk.ust.cse.pishon.esgen.model.Node;

public class FileHandler {

	public static HashMap<String, Node> readScripts(File f) {
		HashMap<String, Node> item = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(f);
			in = new ObjectInputStream(fis);
			Object obj = in.readObject();
			item = (HashMap<String, Node>)obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fis != null)
					fis.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return item;
	}

	public static void readChanges(File f, List<Change> changes) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(f);
			in = new ObjectInputStream(fis);
			Object obj = in.readObject();
			if(obj instanceof List){
				changes.addAll((List<Change>)obj);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fis != null)
					fis.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void readChanges(List<File> files, List<Change> changes) {
		for(File f : files) {
			readChanges(f, changes);
		}
	}

	public static List<Change> readChanges(List<File> files) {
		List<Change> changes = new ArrayList<>();
		readChanges(files, changes);
		return changes;
	}

	public static List<File> findChangeFiles(File path) {
		List<File> files = new ArrayList<>();
		if(path.isDirectory()){
			File[] targets = path.listFiles();
			Arrays.sort(targets, (f1, f2)->(f1.getName().compareTo(f2.getName())));
			for(File f : targets){
				if(f.isDirectory()){
					files.addAll(findChangeFiles(f));
				}else if(f.getName().equals("changes.obj")){
					files.add(f);
				}
			}
		}else{
			if(path.getName().equals("changes.obj")){
				files.add(path);
			}
		}
		return files;
	}

	public static String convertToString(InputStream is){
		String content = null;
		try {
			content = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static String readFile(File f){
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			char[] cbuf = new char[500];
			int len = 0;
			while((len=br.read(cbuf))>-1){
				sb.append(cbuf, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fr != null)	fr.close();
				if(br != null)	br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String readFile(String filePath){
		return readFile(new File(filePath));
	}

	public static void storeContent(String filePath, String content){
		storeContent(new File(filePath), content, false);
	}

	public static void storeContent(String filePath, String content, boolean append){
		storeContent(new File(filePath), content, append);
	}

	public static void storeContent(File f, String content, boolean append){
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = new FileOutputStream(f, append);
			pw = new PrintWriter(fos);
			pw.print(content);
			pw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fos != null)
					fos.close();
				if(pw != null)
					pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void storeObject(String filePath, Object obj){
		storeObject(new File(filePath), obj);
	}

	public static void storeObject(File f, Object obj){
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(f);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fos != null)
					fos.close();
				if(oos != null)
					oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
