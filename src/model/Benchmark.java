package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class Benchmark implements Serializable {

	private static final long serialVersionUID = -5864105750199165462L;
	private Map<String, Multiset<Script>> items;
	private Map<String, String> oldCode;
	private Map<String, String> newCode;

	public Benchmark(){
		items = new HashMap<>();
		oldCode = new HashMap<>();
		newCode = new HashMap<>();
	}

	public void addChange(String changeName, String oldCode, String newCode){
		this.oldCode.put(changeName, oldCode);
		this.newCode.put(changeName, newCode);
	}

	public String getOldCode(String changeName){
		return oldCode.get(changeName);
	}

	public String getNewCode(String changeName){
		return newCode.get(changeName);
	}

	public boolean addItem(String changeName, Script script){
		if(!items.containsKey(changeName)){
			items.put(changeName, HashMultiset.create());
		}
		Multiset<Script> scripts = items.get(changeName);
		return scripts.add(script);
	}

	public int changeCount(){
		return items.size();
	}

	public Set<String> getChangeNames(){
		return items.keySet();
	}

	public int totalCount(String changeName){
		if(items.containsKey(changeName)){
			return items.get(changeName).size();
		}else{
			return 0;
		}
	}

	public int maxCount(String changeName){
		if(items.containsKey(changeName)){
			Multiset<Script> set = items.get(changeName);
			int maxCount = 0;
			int count = 0;
			for(Script s : set.elementSet()){
				count = set.count(s);
				if(count > maxCount){
					maxCount = count;
				}
			}
			return maxCount;
		}else{
			return 0;
		}
	}

	public int cardinality(String changeName, Script script){
		if(items.containsKey(changeName)){
			Multiset<Script> set = items.get(changeName);
			return set.count(script);
		}else{
			return 0;
		}
	}

	public int uniqueCount(String changeName){
		if(items.containsKey(changeName)){
			return items.get(changeName).elementSet().size();
		}else{
			return 0;
		}
	}

	public int find(String changeName, Script script){
		if(items.containsKey(changeName)){
			return items.get(changeName).count(script);
		}else{
			return 0;
		}
	}

	public Multiset<Script> getScripts(String changeName){
		if(items.containsKey(changeName))
			return HashMultiset.create(items.get(changeName));
		return null;
	}
}