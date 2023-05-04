import java.util.ArrayList;

public class List {
	private String title;
	private String UUID;
	private ArrayList<String> items = new ArrayList<String>();
	
	
	public String getTitle() {
		return title;
	}
	
	public String getID() {
		return UUID;
	}
	public String getItem(int index) {
		return items.get(index);
	}
	public ArrayList<String> getArrayList(){
		return items;
	}
	public void setID(String UUID) {
		this.UUID = UUID;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void addItem(String item) {
		items.add(item);
	}
	public void changeItem(String newItem, int index) {
		items.set(index, newItem);
	}
}
