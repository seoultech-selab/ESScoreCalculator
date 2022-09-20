package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hk.ust.cse.pishon.esgen.model.Change;

public class FileHandler {

	public static List<Change> readChanges(List<File> files) {
		List<Change> changes = new ArrayList<>();
		FileInputStream fis = null;
		ObjectInputStream in = null;
		for(File f : files){
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

}
