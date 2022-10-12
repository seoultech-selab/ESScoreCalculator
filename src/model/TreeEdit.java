package model;

import java.util.ArrayList;
import java.util.List;

public class TreeEdit {
	public ESNodeEdit nodeEdit;
	public List<TreeEdit> children;

	public TreeEdit(ESNodeEdit e) {
		this.nodeEdit = e;
		children = new ArrayList<>();

	}
}
