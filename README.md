# MCSS2.x
MCSS2.x

## v2.0.4
* 优化对话列表排序
> 以最新接收消息时间对会话进行排序，网页状态为离开的客户排在最底部(判断away状态将改为online参数)
* 替换友盟更新服务
> 每次更新在服务端修改内容../msgd/app/download/version.xml，按照level等级更新：
	level > 3 自动检查更新，使用自带更新系统更新，不使用友盟自动更新
	level = 5 自动检查更新，强制更新(强制提示)，不提供"以后再说"选项
	level = 4 自动检查更新，建议更新(强制提示)，提供"以后再说"选项，不提供"忽略该版"选项
	level = 3 自动检查更新，普通更新，提供"不再提示"或者"忽略该版"选项
	#level = 2 自动检查更新，采用友盟更新，可选是否更新
	#level = 1 不自动检查更新
	level = 0 默认值，自动检查更新，但不提示更新，仅改变本地缓存的level值
* 增加应用上线渠道
> 目前渠道：open_download、qq_download、wandoujia，待增加：360_download、huawei、xiaomi
* 修复一些问题
> 修复表情显示错误并增加缺失的5个表情、修复历史消息查询显示问题(历史消息列表可查看正在对话中消息)、修复其他一些问题

