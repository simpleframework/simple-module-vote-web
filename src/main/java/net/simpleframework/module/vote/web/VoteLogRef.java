package net.simpleframework.module.vote.web;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteLogRef extends LogRef {

	public void addLogComponent(final PageParameter pp) {
		pp.addComponentBean("AbstractWebVotePlugin_log2Page", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(VoteEditLogPage.class));
		pp.addComponentBean("AbstractWebVotePlugin_log2Win", WindowBean.class)
				.setContentRef("AbstractWebVotePlugin_log2Page").setHeight(540).setWidth(864);
	}

	public static class VoteEditLogPage extends EntityUpdateLogPage implements IVoteContextAware {

		@Override
		protected IIdBeanAware getBean(final PageParameter pp) {
			return context.getVoteService().getBean(pp.getParameter(getBeanIdParameter()));
		}
	}
}
