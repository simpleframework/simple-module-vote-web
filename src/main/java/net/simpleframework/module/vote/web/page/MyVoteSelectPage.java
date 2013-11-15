package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.web.IVoteWebContext;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class MyVoteSelectPage extends AbstractTemplatePage implements IVoteContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"MyVoteSelectPage_list", TablePagerBean.class).setContainerId(hashId).setHandleClass(
				MyVoteList.class);
		tablePager.addColumn(new TablePagerColumn("topic"));
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		return ((KVMap) super.createVariables(pp)).add("listId", hashId);
	}

	public ElementList getButtons(final PageParameter pp) {
		return ElementList.of(new LinkButton($m("MyVoteSelectPage.0")).setOnclick("$Actions.loc('"
				+ ((IVoteWebContext) context).getUrlsFactory().getVoteMgrUrl() + "', true);"));
	}

	public static class MyVoteList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return super.createDataObjectQuery(cp);
		}
	}
}
