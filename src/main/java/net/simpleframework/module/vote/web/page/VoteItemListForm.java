package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.IVoteItemService;
import net.simpleframework.module.vote.VoteGroup;
import net.simpleframework.module.vote.VoteItem;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class VoteItemListForm extends OneTableTemplatePage implements IVoteContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"VoteItemListForm_tbl", VoteItemList.class).setEditable(true).setShowFilterBar(false)
				.setDblclickEdit(false).setNoResultDesc(null);
		tablePager.addColumn(
				new TablePagerColumn("text", $m("VoteForm.0")).setTextAlign(ETextAlign.left))
				.addColumn(TablePagerColumn.OPE().setWidth(130));

		// delete
		addDeleteAjaxRequest(pp, "VoteItemListForm_delete").setHandleMethod("doItemDelete");

		// edit
		addAjaxRequest(pp, "VoteItemListForm_editPage", VoteItemEditForm.class);
		addComponentBean(pp, "VoteItemListForm_editWin", WindowBean.class)
				.setContentRef("VoteItemListForm_editPage").setTitle($m("VoteForm.9")).setWidth(480)
				.setHeight(280);
	}

	public IForward doItemDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			context.getVoteItemService().delete(ids);
		}
		return new JavascriptForward("$Actions['VoteItemListForm_tbl']();");
	}

	@Override
	public Map<String, Object> getVariables(final PageParameter pp) {
		return ((KVMap) super.getVariables(pp)).add("tableId", "tbl_" + hashId);
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(
				new LinkButton($m("VoteItemListForm.0"))
						.setOnclick("$Actions['VoteItemListForm_tbl'].add_row();"),
				SpanElement.SPACE,
				LinkButton.deleteBtn().setOnclick(
						"$Actions['VoteItemListForm_tbl'].doAct('VoteItemListForm_delete');"),
				SpanElement.SPACE, LinkButton.closeBtn());
	}

	private static VoteGroup getVoteGroup(final PageParameter pp) {
		return getCacheBean(pp, context.getVoteGroupService(), "groupId");
	}

	public static class VoteItemList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final VoteGroup vg = getVoteGroup(cp);
			cp.addFormParameter("groupId", vg.getId());
			return context.getVoteItemService().query(vg);
		}

		protected ButtonElement getDeleteButton(final VoteItem vi) {
			return ButtonElement.deleteBtn().setOnclick(
					"$Actions['VoteItemListForm_delete']('id=" + vi.getId() + "');");
		}

		protected ButtonElement getEditButton(final VoteItem vi) {
			return ButtonElement.editBtn().setOnclick(
					"$Actions['VoteItemListForm_editWin']('itemId=" + vi.getId() + "');");
		}

		protected VoteItem createVoteItem(final ComponentParameter cp) {
			final VoteItem vi = new VoteItem();
			final VoteGroup vg = getVoteGroup(cp);
			vi.setVoteId(vg.getVoteId());
			vi.setGroupId(vg.getId());
			return vi;
		}

		@Override
		@Transaction(context = IVoteContext.class)
		public JavascriptForward doRowSave(final ComponentParameter cp,
				final Map<String, Map<String, Object>> insertRows,
				final Map<String, Map<String, Object>> updateRows) {
			final IVoteItemService viService = context.getVoteItemService();
			for (final Map.Entry<String, Map<String, Object>> e : insertRows.entrySet()) {
				final Map<String, Object> row = e.getValue();
				final String text = (String) row.get("text");
				if (!StringUtils.hasText(text)) {
					continue;
				}
				final VoteItem vi = createVoteItem(cp);
				vi.setText(text);
				viService.insert(vi);
			}
			return super.doRowSave(cp, insertRows, updateRows);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final VoteItem vi = (VoteItem) dataObject;
			final KVMap kv = new KVMap();
			kv.put("text", vi.getText());
			final StringBuilder sb = new StringBuilder();
			sb.append(getDeleteButton(vi)).append(SpanElement.SPACE);
			sb.append(getEditButton(vi));
			kv.add(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}