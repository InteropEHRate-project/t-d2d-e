package eu.interopehrate.d2d;

public class D2DRequestHeader extends D2DHeader {

	private int itemsPerPage = 100;

	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

}
