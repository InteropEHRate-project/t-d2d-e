package eu.interopehrate.d2d;

public class D2DResponseHeader extends D2DHeader{
	
	private String requestId;
	// current page
	private int page = 1;
	// total number of packets
	private int totalPages = 1;

	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	@Override
	public String toString() {
		return "D2DResponseHeader [requestId=" + requestId + ", page=" + page + ", totalPages=" + totalPages + "]";
	}
	
}
