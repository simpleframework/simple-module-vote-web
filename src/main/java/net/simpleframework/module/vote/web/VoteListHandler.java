package net.simpleframework.module.vote.web;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.InjectCtx;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class VoteListHandler extends AbstractDbTablePagerHandler {

	@InjectCtx
	protected IVoteContext voteContext;

	private final MenuItems CONTEXT_MENUS = MenuItems
			.of()
			.append(MenuItem.itemEdit().setOnclick_act("AbstractWebVotePlugin_addWin", "voteId"))
			.append(
					MenuItem.of($m("Button.Preview")).setOnclick_act("AbstractWebVotePlugin_previewWin",
							"voteId"))
			.append(MenuItem.sep())
			.append(
					MenuItem.of($m("VoteListHandler.0")).setOnclick_act("AbstractWebVotePlugin_logWin",
							"voteId"))
			.append(MenuItem.sep())
			.append(
					MenuItem.itemLog().setTitle($m("VoteListHandler.1"))
							.setOnclick_act("AbstractWebVotePlugin_log2Win", "beanId"))
			.append(MenuItem.sep())
			.append(MenuItem.itemDelete().setOnclick_act("AbstractWebVotePlugin_delete", "id"))
			.append(MenuItem.sep())
			.append(
					MenuItem
							.of($m("Menu.move"))
							.addChild(
									MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
											"$pager_action(item).move(true, 'AbstractWebVotePlugin_move');"))
							.addChild(
									MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
											"$pager_action(item).move2(true, 'AbstractWebVotePlugin_move');"))
							.addChild(
									MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
											"$pager_action(item).move(false, 'AbstractWebVotePlugin_move');"))
							.addChild(
									MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
											"$pager_action(item).move2(false, 'AbstractWebVotePlugin_move');")));

	@Override
	public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
			final MenuItem menuItem) {
		return menuItem == null ? CONTEXT_MENUS : null;
	}

	@Override
	protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
		final Vote vote = (Vote) dataObject;
		final KVMap kv = new KVMap();
		final ID id = vote.getId();
		kv.put("text", new LinkElement(vote.getText())
				.setOnclick("$Actions['AbstractWebVotePlugin_addWin']('voteId=" + id + "');"));
		kv.put("userId", AbstractTemplatePage.toIconUser(cp, vote.getUserId()));
		kv.put("anonymous", vote.isAnonymous() ? $m("Yes") : $m("No"));
		kv.put("createDate", vote.getCreateDate());
		kv.put("expiredDate", Convert.toDateString(vote.getExpiredDate(), "yyyy-MM-dd"));
		final StringBuilder sb = new StringBuilder();
		sb.append(
				new ButtonElement($m("Button.Preview"))
						.setOnclick("$Actions['AbstractWebVotePlugin_previewWin']('voteId=" + id + "');"))
				.append(SpanElement.SPACE);
		sb.append(ButtonElement.logBtn().setText($m("VoteListHandler.0"))
				.setOnclick("$Actions['AbstractWebVotePlugin_logWin']('voteId=" + id + "');"));
		sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
		kv.put(TablePagerColumn.OPE, sb.toString());
		return kv;
	}
}
