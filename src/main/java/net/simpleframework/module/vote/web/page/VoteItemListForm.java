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
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteItemListForm extends OneTableTemplatePage implements IVoteContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"VoteItemListForm_tbl", VoteItemList.class).setEditable(true).setFilter(false)
				.setDblclickEdit(false).setNoResultDesc(null);
		tablePager.addColumn(new TablePagerColumn("text", $m("VoteForm.0"))).addColumn(
				TablePagerColumn.OPE(80));

		// delete
		addDeleteAjaxRequest(pp, "VoteItemListForm_delete").setHandlerMethod("doItemDelete");

		// edit
		addAjaxRequest(pp, "VoteItemListForm_editPage", VoteItemEditForm.class);
		addComponentBean(pp, "VoteItemListForm_editWin", WindowBean.class)
				.setContentRef("VoteItemListForm_editPage").setTitle($m("VoteForm.9")).setWidth(480)
				.setHeight(280);

		// exchange
		addAjaxRequest(pp, "VoteItemListForm_exchange").setHandlerMethod("doExchange");
	}

	public IForward doItemDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		voteContext.getVoteItemService().delete(ids);
		return new JavascriptForward("$Actions['VoteItemListForm_tbl']();");
	}

	public IForward doExchange(final ComponentParameter cp) {
		final IVoteItemService service = voteContext.getVoteItemService();
		service.exchange(TablePagerUtils.getExchangeBeans(cp, service));
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
						"$Actions['VoteItemListForm_tbl'].doAct('VoteItemListForm_delete');"));
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return ElementList.of(LinkButton.closeBtn());
	}

	private static VoteGroup getVoteGroup(final PageParameter pp) {
		return getCacheBean(pp, voteContext.getVoteGroupService(), "groupId");
	}

	public static class VoteItemList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final VoteGroup vg = getVoteGroup(cp);
			cp.addFormParameter("groupId", vg.getId());
			return voteContext.getVoteItemService().query(vg);
		}

		protected ButtonElement getDeleteButton(final VoteItem vi) {
			return ButtonElement.deleteBtn().setOnclick(
					"$Actions['VoteItemListForm_delete']('id=" + vi.getId() + "');");
		}

		protected MenuItem getDeleteMenuItem() {
			return MenuItem.itemDelete().setOnclick_act("VoteItemListForm_delete", "id");
		}

		protected ButtonElement getEditButton(final VoteItem vi) {
			return ButtonElement.editBtn().setOnclick(
					"$Actions['VoteItemListForm_editWin']('itemId=" + vi.getId() + "');");
		}

		protected String getExchangeAction() {
			return "VoteItemListForm_exchange";
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
			final IVoteItemService viService = voteContext.getVoteItemService();
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

		private final MenuItems CONTEXT_MENUS = MenuItems
				.of()
				.append(getDeleteMenuItem())
				.append(MenuItem.sep())
				.append(
						MenuItem
								.of($m("Menu.move"))
								.addChild(
										MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
												"$pager_action(item).move(true, '" + getExchangeAction()
														+ "');"))
								.addChild(
										MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
												"$pager_action(item).move2(true, '" + getExchangeAction()
														+ "');"))
								.addChild(
										MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
												"$pager_action(item).move(false, '" + getExchangeAction()
														+ "');"))
								.addChild(
										MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
												"$pager_action(item).move2(false, '" + getExchangeAction()
														+ "');")));

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			return menuItem == null ? CONTEXT_MENUS : null;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final VoteItem vi = (VoteItem) dataObject;
			final KVMap kv = new KVMap();
			kv.put("text", vi.getText());
			final StringBuilder sb = new StringBuilder();
			sb.append(getEditButton(vi));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			kv.add(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}