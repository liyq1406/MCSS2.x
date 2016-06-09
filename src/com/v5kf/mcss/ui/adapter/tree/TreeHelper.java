package com.v5kf.mcss.ui.adapter.tree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.litepal.crud.DataSupport;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerArch;
import com.v5kf.mcss.ui.fragment.WorkerTreeFragment;
import com.v5kf.mcss.utils.Logger;

public class TreeHelper {

	
	/**
	 * 传入我们的普通bean，转化为我们排序后的Node
	 * @param datas
	 * @param defaultExpandLevel
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T> List<Node> getSortedNodes(List<T> datas,
			int defaultExpandLevel, int list_mode) throws IllegalArgumentException,
			IllegalAccessException {
		List<Node> result = new ArrayList<Node>();
		//将用户数据转化为List<Node>以及设置Node间关系
		List<Node> nodes = convetData2Node(datas, list_mode);
		//拿到根节点
		List<Node> rootNodes = getRootNodes(nodes);
		//排序
		for (Node node : rootNodes)
		{
			addNode(result, node, defaultExpandLevel, 1);
		}
		return result;
	}
	
	
	/**
	 * 过滤出所有可见的Node
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> filterVisibleNode(List<Node> nodes)
	{
		List<Node> result = new ArrayList<Node>();

		for (Node node : nodes)
		{
			// 如果为跟节点，或者上层目录为展开状态
			if (node.isRoot() || node.isParentExpand())
			{
				setNodeIcon(node);
				result.add(node);
			}
		}
		return result;
	}
	
	
	/**
	 * 将我们的数据转化为树的节点
	 * 
	 * @param datas
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static <T> List<Node> convetData2Node(List<T> datas, int list_mode)
			throws IllegalArgumentException, IllegalAccessException
	{
		List<Node> nodes = new ArrayList<Node>();
		Node node = null;

		for (T t : datas)
		{
			long id = -1;
			long pId = -1;
			String label = null;
			String type = null;
			Class<? extends Object> clazz = t.getClass();
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field f : declaredFields)
			{
				if (f.getAnnotation(TreeNodeId.class) != null)
				{
					f.setAccessible(true);
					id = f.getLong(t);
				}
				if (f.getAnnotation(TreeNodePid.class) != null)
				{
					f.setAccessible(true);
					pId = f.getLong(t);
				}
				if (f.getAnnotation(TreeNodeLabel.class) != null)
				{
					f.setAccessible(true);
					label = (String) f.get(t);
				}
				if (f.getAnnotation(TreeNodeType.class) != null)
				{
					f.setAccessible(true);
					type = (String)f.get(t);
				}
				if (id != -1 && pId != -1 && label != null && type != null)
				{
					break;
				}
			}
			node = new Node(id, pId, type, label);
			Logger.i("Node Tree", ">add:" + node.getType() + " ID:" + id + " pid:" + pId + " name:" + node.getName());
			nodes.add(node);
			
			if (type.equals("group")) {
				List<Node> nodeSort = new ArrayList<Node>();
				if (Config.USE_DB) {
					/*db*/
					List<WorkerArch> archs = DataSupport.where("parentid = ?", String.valueOf(id)).find(WorkerArch.class);
					long i = -1;
					for (WorkerArch arch : archs) {
						// DataBean必须要有默认构造函数
						List<ArchWorkerBean> beans = DataSupport.where("w_id = ?", String.valueOf(arch.getObjId())).find(ArchWorkerBean.class);
						ArchWorkerBean bean = beans.get(0);
						if (list_mode == Config.LIST_MODE_SWITCH && bean.getStatus() == QAODefine.STATUS_OFFLINE) {
							continue;
						} else {
							Node n = new Node(i, id, "worker", bean.getDefaultName());
							n.setWorker(bean);
							Logger.d("Node Tree", "|add-类型:worker 父ID:" + id + " name:" + bean.getDefaultName() + " id:" + i);
							nodeSort.add(n);
							i--;
						}
					}
				} else {
					/*mm*/
					AppInfoKeeper appInfo = CustomApplication.getAppInfoInstance();
					List<WorkerArch> archs = appInfo.getWorkerArchs();
					long i = -1;					
					for (WorkerArch arch : archs) {
						if (arch.getParentId() == id) {
							ArchWorkerBean bean = appInfo.getCoWorker(String.valueOf(arch.getObjId()));
							if (list_mode == Config.LIST_MODE_SWITCH && bean.getStatus() == QAODefine.STATUS_OFFLINE) {
								continue;
							} else {
								Node n = new Node(i, id, "worker", bean.getDefaultName());
								n.setWorker(bean);
								Logger.d("Node Tree", "|add-类型:worker 父ID:" + id + " name:" + bean.getDefaultName() + " id:" + i);
								nodeSort.add(n);
								i--;
							}
						}
					}
				}
				
				
//				WorkerOrganization org = CustomApplication.getAppInfoInstance().getWorkerGroup(id);
//				if (null != org && org.getWorkerList() != null) {
//					long i = -1;
//					List<Node> nodeSort = new ArrayList<Node>();
//					for (ArchWorkerBean bean : org.getWorkerList()) {
//						if (list_mode == WorkerTreeFragment.LIST_MODE_SWITCH && bean.getStatus() == QAODefine.STATUS_OFFLINE) {
//							continue;
//						} else {
//							Node n = new Node(i, id, "worker", bean.getDefaultName());
//							n.setWorker(bean);
//							Logger.d("Node Tree", "|add-类型:worker 父ID:" + id + " name:" + bean.getDefaultName() + " id:" + i);
//							nodeSort.add(n);
//							i--;
//						}
//					}
				// 排序：在线最前，然后是忙碌，离开，离线
				WorkerStatusCompartor wsc = new WorkerStatusCompartor();
				Collections.sort(nodeSort, wsc);
				for (Node n : nodeSort) {
					nodes.add(n);
				}
			}
		}

		/**
		 * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
		 */
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				Node m = nodes.get(j);
				if (m.getpId() == n.getId()) {
					n.getChildren().add(m);
					m.setParent(n);
				} else if (m.getId() == n.getpId()) {
					m.getChildren().add(n);
					n.setParent(m);
				}
			}
		}

		// 设置图片
		for (Node n : nodes) {
			setNodeIcon(n);
		}
		return nodes;
	}

	private static List<Node> getRootNodes(List<Node> nodes) {
		List<Node> root = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node.isRoot())
				root.add(node);
		}
		return root;
	}

	/**
	 * 把一个节点上的所有的内容都挂上去
	 */
	private static void addNode(List<Node> nodes, Node node,
			int defaultExpandLeval, int currentLevel) {

		nodes.add(node);
		if (defaultExpandLeval >= currentLevel) {
			node.setExpand(true);
		}

		if (node.isLeaf())
			return;
		for (int i = 0; i < node.getChildren().size(); i++) {
			addNode(nodes, node.getChildren().get(i), defaultExpandLeval,
					currentLevel + 1);
		}
	}

	/**
	 * 设置节点的图标
	 * 
	 * @param node
	 */
	private static void setNodeIcon(Node node) {
		if (node.getChildren().size() > 0 && node.isExpand()) {
			node.setIcon(R.drawable.v5_indicator_up);
		} else if (node.getChildren().size() > 0 && !node.isExpand()) {
			node.setIcon(R.drawable.v5_indicator_down);
		} else {
			node.setIcon(-1);
		}
	}
	
	/**
	 * 节点坐席状态比较器
	 * @author Chenhy	
	 * @email chenhy@v5kf.com
	 * @version v1.0 2015-9-28 上午9:53:14
	 * @package com.v5kf.mcss.ui.adapter.tree of MCSS-Native
	 * @file TreeHelper.java 
	 *
	 */
	static class WorkerStatusCompartor implements Comparator<Node> {

		@Override
		public int compare(Node lhs, Node rhs) {
			// TODO Auto-generated method stub
			short r = lhs.getWorker().getStatus();
			short l = rhs.getWorker().getStatus();
			if (l == r) {
				return 0;
			} else {
				switch (l) {
				case QAODefine.STATUS_ONLINE:
					if (r == QAODefine.STATUS_ONLINE) {
						return 0;
					} else {
						return 1;
					}
				case QAODefine.STATUS_OFFLINE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 0;
					} else {
						return -1;
					}
				case QAODefine.STATUS_BUSY:
					if (r == QAODefine.STATUS_ONLINE) {
						return -1;
					} else {
						return 1;
					}
				case QAODefine.STATUS_LEAVE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 1;
					} else {
						return -1;
					}
				case QAODefine.STATUS_HIDE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 0;
					} else {
						return -1;
					}
				}
			}
			return 0;
		}		
	}
}
