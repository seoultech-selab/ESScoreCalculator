package model;

import java.util.ArrayList;
import java.util.List;

public class TreeEdit {
	public NodeEdit nodeEdit;
	public List<TreeEdit> children;

	public TreeEdit(NodeEdit e) {
		this.nodeEdit = e;
		children = new ArrayList<>();

	}
}
