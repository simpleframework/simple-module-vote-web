package net.simpleframework.module.vote.web.plugin;

import net.simpleframework.module.vote.plugin.IVotePlugin;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IWebVotePlugin extends IVotePlugin {

	/**
	 * 
	 * @param pp
	 * @param contentId
	 * @return
	 */
	AbstractElement<?> toAddVoteElement(PageParameter pp, Object contentId);

	TablePagerBean addVoteComponent_Tbl(PageParameter pp);

	void addVoteComponent_addBtn(PageParameter pp);
}
