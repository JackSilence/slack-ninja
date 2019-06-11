package ninja.consts;

public enum Task {
	SHOPEE( "百米家新商品通知" ), HEROKU( "Heroku Usage" ), EPOINT( "點數查詢" ), NBA( "NBA BOX" ), DELETE( "刪除訊息" );

	private final String desc;

	private Task( String desc ) {
		this.desc = desc;
	}

	public String desc() {
		return desc;
	}
}