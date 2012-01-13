/* SelectedTreeitemsConverter.java

	Purpose:
		
	Description:
		
	History:
		Aug 17, 2011 6:10:20 PM, Created by dennis

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.bind.converter.sys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.bind.impl.BinderImpl;
import org.zkoss.bind.sys.LoadPropertyBinding;
import org.zkoss.lang.Classes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.ext.TreeSelectionModel;

/**
 * Convert tree selected treeitem to bean and vice versa.
 * @author dennis
 * @since 6.0.0
 */
public class SelectedTreeitemsConverter implements Converter, java.io.Serializable {
	private static final long serialVersionUID = 201108171811L;
	
	@SuppressWarnings("unchecked")
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		Tree tree = (Tree) comp;
		final TreeModel<?> model = tree.getModel();
  		final TreeSelectionModel smodel = (model instanceof TreeSelectionModel)?(TreeSelectionModel)model:null;
  		final Set<Treeitem> items = new LinkedHashSet<Treeitem>();
		Set<Object> vals = val == null ? null : (Set<Object>) Classes.coerce(LinkedHashSet.class, val);
		
		if(smodel!=null && !smodel.isSelectionEmpty()){//clear the selection first
	  		smodel.clearSelection();
		}
		
	  	if (vals != null && vals.size()>0) {
	  		//there is a BUG on tree.getItems (http://tracker.zkoss.org/browse/ZK-766),
	  		//it doesn't return all descending children if it is not open yet
	  		//and if user want better performance, he should get the selection from model directly
		  	for (final Iterator<?> it = tree.getItems().iterator(); it.hasNext();) {
		  		final Treeitem ti = (Treeitem) it.next();
		  		final String varnm = (String) ti.getAttribute(BinderImpl.VAR);
		  		Object bean = null;
		  		int path[] = null;
		  		if (varnm != null) { //There is binding on template
		  			bean = ti.getAttribute(varnm);
		  		} else if(model!=null){ //no binding
		  			bean = model.getChild(path = toPath(ti));
		  		} else{
		  			bean = ti.getValue();
		  		}

		  		if (vals.contains(bean)) {
		  			if(smodel!=null){
		  				if(path==null){
		  					path = toPath(ti);
		  				}
		  				smodel.addSelectionPath(path);
		  			}
		  			items.add(ti);
		  		}
		  	}			
	  	}
	  	return smodel == null ? items : LoadPropertyBinding.LOAD_IGNORED;
	}

	@SuppressWarnings("unchecked")
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		Set<Object> vals = new LinkedHashSet<Object>();//the order
		if (val != null) {
			final Tree tree = (Tree) comp;
	  		final TreeModel<?> model = tree.getModel();
	  		final Set<Treeitem> items = (Set<Treeitem>)Classes.coerce(LinkedHashSet.class, val);
	  		for(Treeitem item : items){
		  		final String varnm = (String) item.getAttribute(BinderImpl.VAR);
		  		if (varnm != null) { //There is binding on template
		  			vals.add(item.getAttribute(varnm));
		  		} else if(model != null){ //from model value
		  			vals.add(model.getChild(toPath(item)));
		  		} else { //no binding
		  			vals.add(item.getValue());
		  		}
	  		}
	  		return vals;
	  	}
	 	return vals;
	}
	
	private int[] toPath(Treeitem item) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		while(item!=null){
			path.add(0,item.getIndex());
			item = item.getParentItem();
		}
		int[] p = new int[path.size()];
		for(int i=0;i<p.length;i++){
			p[i] = path.get(i).intValue();
		}
		return p;
	}

}