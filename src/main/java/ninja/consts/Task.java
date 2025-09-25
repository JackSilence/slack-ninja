package ninja.consts;

public enum Task {
	DELETE( "刪除訊息" );

	private final String desc;

	private Task( String desc ) {
		this.desc = desc;
	}

	public String desc() {
		return desc;
	}
}