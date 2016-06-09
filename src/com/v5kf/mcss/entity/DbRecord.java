package com.v5kf.mcss.entity;


/**
 * 记录v5kf_worker数据库的保存数据信息
 * @author Chenhy	
 * @e-mail chenhy@v5kf.com
 * @version v1.0 2015-10-20 上午11:08:51
 * @package com.v5kf.mcss.entity of MCSS-Native
 * @file DbRecord.java 
 *
 */
public class DbRecord extends BaseBean {

	/* id为1的第一条数据保存内容 */
	private String userId;				/* db保存数据的所属坐席id */
	private String historyCstmStart;	/* db中保存到的历史客户的起始日期 */
	private String historyCstmEnd;		/* db中保存到的历史客户的结束日期 */
	
}
