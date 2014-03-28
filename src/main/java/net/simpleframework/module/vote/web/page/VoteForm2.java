package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteGroupService;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.module.vote.VoteGroup;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteForm2 extends VoteForm {

	@Override
	protected void addVoteComponents(final PageParameter pp) {
		final TablePagerBean tablePager = addTablePagerBean(pp, "VoteForm_tbl", VoteGroupList.class)
				.setEditable(true).setDblclickEdit(false);
		tablePager
				.addColumn(new TablePagerColumn("text", $m("VoteForm.6")).setTextAlign(ETextAlign.left))
				.addColumn(new TablePagerColumn("multiple", $m("VoteForm.7"), 100))
				.addColumn(TablePagerColumn.OPE().setWidth(160));

		// group delete
		addDeleteAjaxRequest(pp, "VoteForm_groupDelete").setHandlerMethod("doGroupDelete");

		// group edit
		addAjaxRequest(pp, "VoteForm_groupEditPage", VoteGroupEditForm.class);
		addComponentBean(pp, "VoteForm_groupEditWin", WindowBean.class)
				.setContentRef("VoteForm_groupEditPage").setTitle($m("VoteForm.9")).setWidth(480)
				.setHeight(280);

		// group item list
		addAjaxRequest(pp, "VoteForm_itemListPage", VoteItemListForm.class);
		addComponentBean(pp, "VoteForm_itemListWin", WindowBean.class)
				.setContentRef("VoteForm_itemListPage").setTitle($m("VoteForm.8")).setHeight(450)
				.setWidth(640);
	}

	@Transaction(context = IVoteContext.class)
	public IForward doGroupDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		context.getVoteGroupService().delete(ids);
		return new JavascriptForward("$Actions['VoteForm_tbl']();");
	}

	@Override
	protected ElementList getTableButtons(final PageParameter pp) {
		final String act = "$Actions['VoteForm_tbl']";
		return ElementList.of(new LinkButton($m("VoteForm.5")).setOnclick(act + ".add_row();"))
				.append(SpanElement.SPACE)
				.append(LinkButton.deleteBtn().setOnclick(act + ".doAct('VoteForm_groupDelete');"));
	}

	public static class VoteGroupList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Vote vote = getVote(cp);
			cp.addFormParameter("voteId", vote.getId());
			return context.getVoteGroupService().query(vote);
		}

		@Override
		@Transaction(context = IVoteContext.class)
		public JavascriptForward doRowSave(final ComponentParameter cp,
				final Map<String, Map<String, Object>> insertRows,
				final Map<String, Map<String, Object>> updateRows) {
			final IVoteGroupService vgService = context.getVoteGroupService();
			final Vote vote = getVote(cp);
			for (final Map.Entry<String, Map<String, Object>> e : insertRows.entrySet()) {
				final Map<String, Object> row = e.getValue();
				final String text = (String) row.get("text");
				if (!StringUtils.hasText(text)) {
					continue;
				}
				final VoteGroup vg = vgService.createBean();
				final String id = e.getKey();
				vg.setId(ID.of(id.substring(1)));
				vg.setVoteId(vote.getId());
				vg.setText(text);
				vg.setMultiple(Convert.toShort(row.get("multiple")));
				vgService.insert(vg);
			}
			for (final Map.Entry<String, Map<String, Object>> e : updateRows.entrySet()) {
				final Map<String, Object> row = e.getValue();
				final VoteGroup vg = vgService.getBean(e.getKey());
				if (vg == null) {
					continue;
				}
				final String text = (String) row.get("text");
				if (!StringUtils.hasText(text)) {
					continue;
				}
				vg.setText(text);
				vg.setMultiple(Convert.toShort(row.get("multiple")));
				vgService.update(vg);
			}
			return super.doRowSave(cp, insertRows, updateRows);
		}

		@Override
		public Object getRowBeanById(final ComponentParameter cp, final Object id) {
			return context.getVoteGroupService().getBean(id);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final VoteGroup vg = (VoteGroup) dataObject;
			final KVMap kv = new KVMap();
			final StringBuilder sb = new StringBuilder();
			sb.append(new LinkElement(vg.getText())
					.setOnclick("$Actions['VoteForm_groupEditWin']('groupId=" + vg.getId() + "');"));
			final int c = context.getVoteItemService().query(vg).getCount();
			if (c > 0) {
				sb.append(new SupElement(c).addStyle("margin-left: 4px;"));
			}
			kv.put("text", sb.toString());
			kv.put("multiple", vg.getMultiple());
			sb.setLength(0);
			sb.append(
					ButtonElement.deleteBtn().setOnclick(
							"$Actions['VoteForm_groupDelete']('id=" + vg.getId() + "');")).append(
					SpanElement.SPACE);
			sb.append(
					new ButtonElement($m("VoteForm.8"))
							.setOnclick("$Actions['VoteForm_itemListWin']('groupId=" + vg.getId() + "');"))
					.append(SpanElement.SPACE);
			kv.put(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}
