package slim.tools.slimsheet;

import java.util.ArrayList;

import slim.tools.slimsheet.actions.UndoableAction;

public class UndoManager {
	
	private ArrayList<UndoableAction> list = new ArrayList<UndoableAction>();
	private int pointer = 0;
	
	public UndoManager() {
		
	}
	
	public void push(UndoableAction action) {
		
	}
	
	public boolean canUndo() {
		return pointer!=0;
	}
	
	public boolean canRedo() {
		return pointer < list.size()-1;
	}
	
	class UndoState {
		
	}
}
