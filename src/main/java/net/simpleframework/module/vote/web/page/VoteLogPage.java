package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.module.vote.VoteLog;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.GroupDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class VoteLogPage extends OneTableTemplatePage implements IVoteContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "VoteLogPage_table",
				VoteLogTable.class);
		tablePager
				.addColumn(
						new TablePagerColumn("itemId", $m("VoteLogPage.3")).setTextAlign(ETextAlign.left)
								.setFilter(false))
				.addColumn(
						new TablePagerColumn("userId", $m("VoteLogPage.0"), 110).setTextAlign(
								ETextAlign.left).setFilter(false))
				.addColumn(
						new TablePagerColumn("createDate", $m("VoteLogPage.1"), 120)
								.setPropertyClass(Date.class))
				.addColumn(new TablePagerColumn("ip", $m("VoteLogPage.2"), 110))
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// delete
		addDeleteAjaxRequest(pp, "VoteLogPage_delete");
	}

	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			context.getVoteLogService().delete(ids);
		}
		return new JavascriptForward("$Actions['VoteLogPage_table']();");
	}

	@Override
	public String getTitle(final PageParameter pp) {
		return $m("Button.Log") + " - " + getVote(pp).getText();
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(LinkButton.closeBtn(), SpanElement.SPACE, LinkButton.deleteBtn()
				.setOnclick("$Actions['VoteLogPage_table'].doAct('VoteLogPage_delete');"));
	}

	private static Option OPTION_1 = new Option("createDate", $m("VoteLogPage.1"));

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return ElementList.of(createGroupElement(pp, "VoteLogPage_table", OPTION_1));
	}

	private static Vote getVote(final PageParameter pp) {
		return getCacheBean(pp, context.getVoteService(), "voteId");
	}

	public static class VoteLogTable extends GroupDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Vote vote = getVote(cp);
			cp.addFormParameter("voteId", vote.getId());
			return context.getVoteLogService().query(vote);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final VoteLog log = (VoteLog) dataObject;
			final KVMap kv = new KVMap();
			kv.put("itemId", context.getVoteItemService().getBean(log.getItemId()));
			kv.put("createDate", log.getCreateDate());
			final ID userId = log.getUserId();
			kv.put("userId", toIconUser(cp, userId == null ? $m("VoteLogPage.4") : cp.getUser(userId)));
			kv.put("ip", log.getIp());
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['VoteLogPage_delete']('id=" + log.getId() + "');"));
			kv.put(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}
