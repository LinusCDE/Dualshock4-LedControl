package linuscde98.dualshock4.ledcontrol;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class SimpleComboBoxModel <T> implements ComboBoxModel<T>{
	
	T[] data = null;
	Object selected = null;
	
	public SimpleComboBoxModel(T[] entrys) {
		this.data = entrys;
	}
	
	@Override
	public int getSize() {
		return data.length;
	}

	@Override
	public T getElementAt(int index) {
		return data[index];
	}

	@Override
	public void addListDataListener(ListDataListener l) { }

	@Override
	public void removeListDataListener(ListDataListener l) { }

	@Override
	public void setSelectedItem(Object anItem) {
		selected = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

}
