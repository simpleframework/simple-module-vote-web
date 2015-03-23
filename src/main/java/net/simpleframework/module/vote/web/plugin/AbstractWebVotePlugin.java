package net.simpleframework.module.vote.web.plugin;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.plugin.AbstractModulePlugin;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.IVoteService;
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
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
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
				.setOnclick("$Actions['AbstractWebVotePlugin_addWin']('voteMark=" + getMark()
						+ "&contentId=" + StringUtils.blank(contentId) + "');");
	}

	@Override
	public TablePagerBean addVoteComponent_Tbl(final PageParameter pp) {
		// log
		pp.addComponentBean("AbstractWebVotePlugin_logPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(VoteLogPage.class));
		pp.addComponentBean("AbstractWebVotePlugin_logWin", WindowBean.class)
				.setContentRef("AbstractWebVotePlugin_logPage").setHeight(540).setWidth(864);

		// edit log
		final IModuleRef ref = ((IVoteWebContext) voteContext).getLogRef();
		if (ref != null) {
			((VoteLogRef) ref).addLogComponent(pp);
		}

		// move
		pp.addComponentBean("AbstractWebVotePlugin_move", AjaxRequestBean.class).setHandlerClass(
				MoveAction.class);

		// delete
		pp.addComponentBean("AbstractWebVotePlugin_delete", AjaxRequestBean.class)
				.setConfirmMessage($m("Confirm.Delete")).setHandlerClass(DeleteAction.class);

		// preview
		pp.addComponentBean("AbstractWebVotePlugin_previewPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(VotePostPage.class));
		pp.addComponentBean("AbstractWebVotePlugin_previewWin", WindowBean.class)
				.setContentRef("AbstractWebVotePlugin_previewPage").setWidth(400).setHeight(480)
				.setTitle($m("Button.Preview"));

		final TablePagerBean tablePager = pp
				.addComponentBean("AbstractWebVotePlugin_list", TablePagerBean.class)
				.setShowLineNo(true).setShowCheckbox(true);
		tablePager
				.addColumn(new TablePagerColumn("text", $m("VoteForm.0")))
				.addColumn(
						new TablePagerColumn("anonymous", $m("VoteForm.3"), 70)
								.setPropertyClass(Boolean.class))
				.addColumn(
						new TablePagerColumn("expiredDate", $m("VoteForm.1"), 95)
								.setPropertyClass(Date.class))
				.addColumn(new TablePagerColumn("userId", $m("AbstractWebVotePlugin.2"), 95))
				.addColumn(
						new TablePagerColumn("createDate", $m("AbstractWebVotePlugin.1"), 120)
								.setPropertyClass(Date.class))
				.addColumn(TablePagerColumn.OPE().setWidth(160));
		return tablePager;
	}

	@Override
	public void addVoteComponent_addBtn(final PageParameter pp) {
		// 添加投票
		pp.addComponentBean("AbstractWebVotePlugin_addForm", AjaxRequestBean.class).setHandlerClass(
				VoteFormAjaxRequest.class);
		pp.addComponentBean("AbstractWebVotePlugin_addWin", WindowBean.class)
				.setContentRef("AbstractWebVotePlugin_addForm").setTitle($m("AbstractWebVotePlugin.0"))
				.setHeight(540).setWidth(900);
	}

	public static class MoveAction extends DefaultAjaxRequestHandler implements IVoteContextAware {
		@Transaction(context = IVoteContext.class)
		@Override
		public IForward ajaxProcess(final ComponentParameter cp) {
			final IVoteService service = voteContext.getVoteService();
			final Vote item = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID));
			final Vote item2 = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID2));
			if (item != null && item2 != null) {
				service.exchange(item, item2,
						Convert.toBool(cp.getParameter(TablePagerUtils.PARAM_MOVE_UP)));
			}
			return new JavascriptForward("$Actions['AbstractWebVotePlugin_list']();");
		}
	}

	public static class DeleteAction extends DefaultAjaxRequestHandler implements IVoteContextAware {
		@Transaction(context = IVoteContext.class)
		@Override
		public IForward ajaxProcess(final ComponentParameter cp) {
			final Object[] ids = StringUtils.split(cp.getParameter("id"));
			voteContext.getVoteService().delete(ids);
			return new JavascriptForward("$Actions['AbstractWebVotePlugin_list']();");
		}
	}

	public static class VoteFormAjaxRequest extends DefaultAjaxRequestHandler implements
			IVoteContextAware {

		@Override
		public IForward ajaxProcess(final ComponentParameter cp) {
			final Vote vote = voteContext.getVoteService().getBean(cp.getParameter("voteId"));
			return new UrlForward(
					AbstractMVCPage.url(vote != null && vote.isGroups() ? VoteForm2.class
							: VoteForm.class));
		}
	}
}
