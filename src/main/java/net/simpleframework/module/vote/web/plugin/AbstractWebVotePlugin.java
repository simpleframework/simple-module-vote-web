package net.simpleframework.module.vote.web.plugin;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.plugin.AbstractModulePlugin;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.module.vote.web.IVoteWebContext;
import net.simpleframework.module.vote.web.VoteLogRef;
import net.simpleframework.module.vote.web.page.VoteForm;
import net.simpleframework.module.vote.web.page.VoteForm2;
import net.simpleframework.module.vote.web.page.VoteLogPage;
import net.simpleframework.module.vote.web.page.VotePostPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.UrlForward;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractWebVotePlugin extends AbstractModulePlugin implements IWebVotePlugin,
		IVoteContextAware {

	@Override
	public AbstractElement<?> toAddVoteElement(final PageParameter pp, final Object contentId) {
		// 添加投票窗口
		addVoteComponent_addBtn(pp);
		return new LinkButton($m("AbstractWebVotePlugin.0"))
				.setOnclick("$Actions['VoteFormHandler_addWin']('voteMark=" + getMark() + "&contentId="
						+ StringUtils.blank(contentId) + "');");
	}

	@Override
	public TablePagerBean addVoteComponent_Tbl(final PageParameter pp) {
		// log
		pp.addComponentBean("VoteFormHandler_logPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(VoteLogPage.class));
		pp.addComponentBean("VoteFormHandler_logWin", WindowBean.class)
				.setContentRef("VoteFormHandler_logPage").setHeight(540).setWidth(864);

		// edit log
		final IModuleRef ref = ((IVoteWebContext) context).getLogRef();
		if (ref != null) {
			((VoteLogRef) ref).addLogComponent(pp);
		}

		// delete
		pp.addComponentBean("VoteFormHandler_delete", AjaxRequestBean.class)
				.setConfirmMessage($m("Confirm.Delete")).setHandleClass(DeleteAction.class);

		// preview
		pp.addComponentBean("VoteFormHandler_previewPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(VotePostPage.class));
		pp.addComponentBean("VoteFormHandler_previewWin", WindowBean.class)
				.setContentRef("VoteFormHandler_previewPage").setWidth(400).setHeight(480)
				.setTitle($m("Button.Preview"));

		final TablePagerBean tablePager = pp
				.addComponentBean("VoteFormHandler_list", TablePagerBean.class).setShowLineNo(true)
				.setShowCheckbox(true);
		tablePager
				.addColumn(new TablePagerColumn("text", $m("VoteForm.0")).setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("anonymous", $m("VoteForm.3"), 70)
								.setPropertyClass(Boolean.class))
				.addColumn(new TablePagerColumn("expiredDate", $m("VoteForm.1"), 95))
				.addColumn(
						new TablePagerColumn("userId", $m("AbstractWebVotePlugin.2"), 95)
								.setTextAlign(ETextAlign.left))
				.addColumn(new TablePagerColumn("createDate", $m("AbstractWebVotePlugin.1"), 120))
				.addColumn(TablePagerColumn.OPE().setWidth(160));
		return tablePager;
	}

	@Override
	public void addVoteComponent_addBtn(final PageParameter pp) {
		// 添加投票
		pp.addComponentBean("VoteFormHandler_addForm", AjaxRequestBean.class).setHandleClass(
				VoteFormAjaxRequest.class);
		pp.addComponentBean("VoteFormHandler_addWin", WindowBean.class)
				.setContentRef("VoteFormHandler_addForm").setTitle($m("AbstractWebVotePlugin.0"))
				.setHeight(540).setWidth(900);
	}

	public static class DeleteAction extends DefaultAjaxRequestHandler implements IVoteContextAware {
		@Transaction(context = IVoteContext.class)
		@Override
		public IForward ajaxProcess(final ComponentParameter cp) {
			final Object[] ids = StringUtils.split(cp.getParameter("id"));
			if (ids != null) {
				context.getVoteService().delete(ids);
			}
			return new JavascriptForward("$Actions['VoteFormHandler_list']();");
		}
	}

	public static class VoteFormAjaxRequest extends DefaultAjaxRequestHandler implements
			IVoteContextAware {

		@Override
		public IForward ajaxProcess(final ComponentParameter cp) {
			final Vote vote = context.getVoteService().getBean(cp.getParameter("voteId"));
			return new UrlForward(
					AbstractMVCPage.url(vote != null && vote.isGroups() ? VoteForm2.class
							: VoteForm.class));
		}
	}
}
