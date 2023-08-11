package kr.ac.seoultech.selab.esscore.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hk.ust.cse.pishon.esgen.model.Change;
import kr.ac.seoultech.selab.esscore.model.Benchmark;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.Script;
import kr.ac.seoultech.selab.esscore.util.CodeHandler;
import kr.ac.seoultech.selab.esscore.util.FileHandler;
import kr.ac.seoultech.selab.esscore.util.ScriptConverter;

public class CreateIJMBenchmark {

	public static void main(String[] args) {
		String tool = "ijm";
		String path = null;
		if(args.length >= 1){
			path = args[0];
		}else{
			System.out.println("CreateBenchmark [path to files]");
			path = "Scripts";
		}
		Benchmark benchmark = new Benchmark();
		List<File> files = FileHandler.findChangeFiles(new File(path));
		System.out.println("Total "+files.size()+" Change Files.");
		List<Change> changes = new ArrayList<>();
		FileHandler.readChanges(files, changes);
		System.out.println("Total "+changes.size()+" Changes.");
		ScriptConverter converter = new ScriptConverter();
		List<ESNode> oldNodes = null;
		List<ESNode> newNodes = null;

		for(Change change : changes){
			try {
				String oldCode = FileHandler.convertToString(change.getOldFile().getContents());
				String newCode = FileHandler.convertToString(change.getNewFile().getContents());
				String changeName = change.getName();
				benchmark.addChange(changeName, oldCode, newCode);
				oldNodes = CodeHandler.parseIJM(oldCode);
				newNodes = CodeHandler.parseIJM(newCode);
				Script script = converter.convert(change.getScript(), oldNodes, newNodes);
				benchmark.addItem(changeName, script);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Set<String> changeNames = benchmark.getChangeNames();
		for(String changeName : changeNames){
			System.out.println("Change:"+changeName);
			System.out.println("Uniq. Script Count:"+benchmark.uniqueCount(changeName));
			System.out.println("Total Script Count:"+benchmark.totalCount(changeName));
			StringBuffer sb = new StringBuffer();
			for(Script s : benchmark.getScripts(changeName).elementSet()){
				sb.append("Script\n");
				sb.append(s.textScript+"\n");
				sb.append("Vote:"+benchmark.cardinality(changeName, s)+"\n");
				sb.append("Size:"+s.editOps.size()+"\n");
				sb.append(s.toString()+"\n");
			}
			FileHandler.storeContent("changes/"+changeName+"/benchmark_"+tool+".txt", sb.toString());
		}
		FileHandler.storeObject("benchmark_"+tool+".obj", benchmark);
	}

}
