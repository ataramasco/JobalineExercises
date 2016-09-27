package com.jobaline.uiautomation.framework.lang.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by damian on 1/8/15.
 */
public class Tree<T> implements Iterable<TreeNode<T>>
{
	private TreeNode<T> root;


	public Tree(T rootValue)
	{
		this.root = new TreeNode<>(rootValue);
	}


	public Tree(TreeNode<T> rootNode)
	{
		this.root = rootNode;
	}


	public TreeNode<T> getRoot()
	{
		return root;
	}


	/**
	 * This method follows the Post-Orden tree traversal algorithm.
	 * */
	private List<TreeNode<T>> asList(TreeNode<T> currentNode)
	{
		List<TreeNode<T>> nodes = new ArrayList<>();
		if(currentNode.isLeaf())
		{
			nodes.add(currentNode);
		}
		else
		{
			for(TreeNode<T> child : currentNode.getChildren())
			{
				nodes.addAll(asList(child));
			}

			nodes.add(currentNode);
		}

		return nodes;
	}


	/**
	 * Return a list of all the nodes.
	 * The list is created using the Post-Orden tree traversal algorithm.
	 * */
	public List<TreeNode<T>> asList()
	{
		return this.asList(getRoot());
	}


	@Override public Iterator<TreeNode<T>> iterator()
	{
		return asList().iterator();
	}


	public TreeNode<T> findNode(Comparable<T> nodeData)
	{
		for(TreeNode<T> node : this.asList())
		{
			T value = node.getData();
			if(nodeData.compareTo(value) == 0)
			{
				return node;
			}
		}

		return null;
	}


	/**
	 * Returns a new Tree<T> instance that will have as root a node of this Tree.
	 *
	 * It is very important to consider that the TreeNode<T> instances and the objects they are containing will be the same,
	 * this is, they are not cloned.
	 * */
	public Tree<T> getSubtree(Comparable<T> nodeData)
	{
		TreeNode<T> subTreeRoot = findNode(nodeData);

		return new Tree<>(subTreeRoot);
	}


	/**
	 * This method follows the Post-Orden tree traversal algorithm.
	 * */
	private void printToConsole(TreeNode<T> currentNode, int level)
	{
		String text = "";
		for(int i = 0; i < level; i++)
		{
			text += "|  ";
		}
		text += "|- " + currentNode.getData().toString();

		System.out.println(text);

		if(!currentNode.isLeaf())
		{
			for(TreeNode<T> child : currentNode.getChildren())
			{
				printToConsole(child, level + 1);
			}
		}
	}


	public void printToConsole()
	{
		System.out.println(getRoot().getData().toString());

		for(TreeNode<T> child : getRoot().getChildren())
		{
			printToConsole(child, 1);
		}
	}


	private boolean equals(TreeNode<T> node1, TreeNode<T> node2)
	{
		boolean equals = false; // need to initialize the variable to compile

		if(node1.getData().equals(node2.getData()))
		{
			List<TreeNode<T>> children1 = node1.getChildren();
			List<TreeNode<T>> children2 = node2.getChildren();

			if(children1.size() == children2.size())
			{
				if(children1.size() == 0)
				{
					equals = true;
				}
				else
				{
					for(int i = 0; i < children1.size(); i++)
					{
						equals = equals(children1.get(i), children2.get(i));

						if(!equals)
						{
							break;
						}
					}
				}
			}
			else
			{
				equals = false;
			}
		}
		else
		{
			equals = false;
		}

		return equals;
	}


	public boolean equals(Object another)
	{
		if(Tree.class.isInstance(another))
		{
			TreeNode<T> node1 = getRoot();
			TreeNode<T> node2 = ((Tree<T>)another).getRoot();

			return equals(node1, node2);
		}
		else
		{
			return false;
		}
	}
}
