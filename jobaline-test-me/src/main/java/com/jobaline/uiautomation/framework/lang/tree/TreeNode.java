package com.jobaline.uiautomation.framework.lang.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by damian on 1/8/15.
 */
public class TreeNode<T>
{

	private T                 data;
	private TreeNode<T>       parent;
	private List<TreeNode<T>> children;


	public boolean isRoot()
	{
		return parent == null;
	}


	public boolean isLeaf()
	{
		return children.size() == 0;
	}


	private List<TreeNode<T>> elementsIndex;


	public TreeNode(T data)
	{
		this.data = data;
		this.children = new LinkedList<>();
	}


	public TreeNode<T> addChild(T child)
	{
		TreeNode<T> childNode = new TreeNode<>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}


	public void addChild(TreeNode<T> childNode)
	{
		childNode.parent = this;
		this.children.add(childNode);
	}


	public TreeNode<T> getParent()
	{
		return parent;
	}


	public List<TreeNode<T>> getChildren()
	{
		return children;
	}


	public int getLevel()
	{
		if(this.isRoot())
		{
			return 0;
		}
		else
		{
			return parent.getLevel() + 1;
		}
	}


	public T getData()
	{
		return data;
	}


	@Override
	public String toString()
	{
		return data != null? data.toString() : "[null data]";
	}

}
