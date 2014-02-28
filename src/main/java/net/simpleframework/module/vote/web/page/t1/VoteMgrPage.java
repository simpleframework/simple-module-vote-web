package net.simpleframework.module.vote.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.module.common.plugin.IModulePlugin;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.web.IVoteWebContext;
import net.simpleframework.module.vote.web.VoteListHandler;
import net.simpleframework.module.vote.web.VoteUrlsFactory;
import net.simpleframework.module.vote.web.page.VoteForm;
import net.simpleframework.module.vote.web.plugin.IWebVotePlugin;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.T1ResizedTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/vote/mgr")
public class VoteMgrPage extends T1ResizedTemplatePage implements IVoteContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(VoteForm.class, "/vote.css");

		final IWebVotePlugin vMark = getVoteMark(pp);
		vMark.addVoteComponent_addBtn(pp);
		addVoteTbl(pp, vMark);
	}

	protected void addVoteTbl(final PageParameter pp, final IWebVotePlugin vMark) {
		vMark.addVoteComponent_Tbl(pp).setPagerBarLayout(EPagerBarLayout.bottom)
				.setContainerId("list_" + hashId).setHandlerClass(MgrVoteListHandler.class);
	}

	protected static IWebVotePlugin getVoteMark(final PageParameter pp) {
		return (IWebVotePlugin) context.getPluginRegistry().getPlugin(pp.getIntParameter("voteMark"));
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new SpanElement($m("VoteContext.1")));
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final TabButtons tabs = TabButtons.of(new TabButton($m("VoteContext.2"),
				url(MyVotePage.class)), new TabButton($m("VoteContext.1"), url(VoteMgrPage.class)));
		return tabs;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(LinkButton.deleteBtn().setOnclick(
				"$Actions['AbstractWebVotePlugin_list'].doAct('AbstractWebVotePlugin_delete');"));
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final VoteUrlsFactory uFactory = ((IVoteWebContext) context).getUrlsFactory();
		final InputElement select = InputElement.select().setOnchange(
				"$Actions.loc('" + uFactory.getUrl(pp, VoteMgrPage.class) + "?voteMark=' + $F(this));");
		for (final IModulePlugin mark : context.getPluginRegistry().allPlugin()) {
			final int iMark = mark.getMark();
			select.addElements(new Option(iMark, mark.getText()).setSelected(iMark == pp
					.getIntParameter("voteMark")));
		}
		return ElementList.of(select);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='VoteMgrPage'>");
		sb.append(" <div id='list_").append(hashId).append("'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class MgrVoteListHandler extends VoteListHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final IWebVotePlugin vMark = getVoteMark(cp);
			return voteContext.getVoteService().queryVote(vMark == null ? 0 : vMark.getMark());
		}
	}
}
