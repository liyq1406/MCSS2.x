package com.v5kf.mcss.ui.adapter.tree;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.v5kf.mcss.ui.widget.PinnedSectionListView.PinnedSectionListAdapter;
import com.v5kf.mcss.utils.Logger;

public abstract class TreeListViewAdapter<T> extends BaseAdapter implements PinnedSectionListAdapter {

	protected Context mContext;
	/**
	 * 存储所有可见的Node
	 */
	protected List<Node> mNodes;
	protected LayoutInflater mInflater;
	/**
	 * 存储所有的Node
	 */
	protected List<Node> mAllNodes;
	
	protected List<T> mDatas;
	
	protected int list_mode;

	/**
	 * 点击的回调接口
	 */
	private OnTreeNodeClickListener onTreeNodeClickListener;
	private OnTreeNodeLongClickListener onTreeNodeLongClickListener;

	public interface OnTreeNodeClickListener
	{
		void onClick(Node node, int position);
	}

	public interface OnTreeNodeLongClickListener
	{
		void onLongClick(Node node, int position);
	}

	public void setOnTreeNodeClickListener(
			OnTreeNodeClickListener onTreeNodeClickListener)
	{
		this.onTreeNodeClickListener = onTreeNodeClickListener;
	}

	public void setOnTreeNodeLongClickListener(
			OnTreeNodeLongClickListener onTreeNodeLongClickListener)
	{
		this.onTreeNodeLongClickListener = onTreeNodeLongClickListener;
	}

	/**
	 * 
	 * @param mTree
	 * @param context
	 * @param datas
	 * @param defaultExpandLevel
	 *            默认展开几级树
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public TreeListViewAdapter(ListView mTree, Context context, List<T> datas,
			int defaultExpandLevel, int listMode) throws IllegalArgumentException,
			IllegalAccessException
	{
		list_mode = listMode;
		mContext = context;
		mDatas = datas;
		/**
		 * 对所有的Node进行排序
		 */
		mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel, list_mode);
		/**
		 * 过滤出可见的Node
		 */
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);
		mInflater = LayoutInflater.from(context);
		Logger.i("TreeListViewAdapter", "INIT----------");
		
		/**
		 * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
		 */
		mTree.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Logger.i("TreeListViewAdapter", "Click position:" + position);
				expandOrCollapse(position);
				for (Node n : mAllNodes) {
					n.setSelect(false);
				}

				if (onTreeNodeClickListener != null) {
					onTreeNodeClickListener.onClick(mNodes.get(position),
							position);
				}
			}

		});
		mTree.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Logger.i("TreeListViewAdapter", "LongClick position:" + position);
				for (Node n : mAllNodes) {
					n.setSelect(false);
				}
				
				if (onTreeNodeLongClickListener != null) {
					onTreeNodeLongClickListener.onLongClick(mNodes.get(position),
							position);
				}
				return false;
			}
		});
	}

	/**
	 * 相应ListView的点击事件 展开或关闭某节点
	 * 
	 * @param position
	 */
	public void expandOrCollapse(int position)
	{
		Node n = mNodes.get(position);

		if (n != null)// 排除传入参数错误异常
		{
			if (!n.isLeaf())
			{
				n.setExpand(!n.isExpand());
				mNodes = TreeHelper.filterVisibleNode(mAllNodes);
				notifyDataSetChanged();// 刷新视图
			}
		}
	}

	public void notifyTreeListDataChange() {
		try {
			mAllNodes = TreeHelper.getSortedNodes(mDatas, 1, list_mode);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);
		notifyDataSetChanged();// 刷新视图
	}
	
	
	@Override
	public int getCount()
	{
		return mNodes.size();
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		if (position >= mNodes.size()) {
			Logger.e("TreeListViewAdapter", "[getItemViewType] position out of bound");
			return Node.SECTION;
		}
		
		if (mNodes.get(position).getType().equals("group")) {
			return Node.SECTION;
		} else {
			return Node.ITEM;
		}
	}

	@Override
	public Object getItem(int position)
	{
		return mNodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (position >= mNodes.size()) {
			Logger.e("TreeListViewAdapter", "[getView] position out of bound");
			position = mNodes.size() - 1;
		}
		Node node = mNodes.get(position);
		convertView = getConvertView(node, position, convertView, parent);
		// 设置内边距
		convertView.setPadding(node.getLevel() * 30, 3, 3, 3);
		return convertView;
	}

	public abstract View getConvertView(Node node, int position,
			View convertView, ViewGroup parent);

	
}
